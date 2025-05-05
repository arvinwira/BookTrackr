package com.example.skripsi.ui.reading

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.skripsi.data.remote.Book
import com.example.skripsi.data.repository.Repository
import com.example.skripsi.databinding.ActivityReadingBinding
import com.example.skripsi.ui.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.util.Timer

class ReadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadingBinding
    private val viewModel: ReadingViewModel by viewModels {
        ViewModelFactory(Repository(applicationContext))
    }

    private var book: Book? = null
    private var userId: String? = null
    private var progressTrackingTimer: Timer? = null
    private var readingStartTime: Long = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = FirebaseAuth.getInstance().currentUser?.uid
        book = intent.getSerializableExtra("book") as? Book

        if (book == null || userId == null) {
            Log.e("ReadingActivity", "Book or user ID is null")
            finish()
            return
        }

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = book?.title ?: "Reading"

        // Configure WebView for PDF viewing
        binding.webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            domStorageEnabled = true
            allowContentAccess = true
            allowFileAccess = true
            // For PDF loading
            setSupportMultipleWindows(true)
        }

        // Set WebView client for handling page loading
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                // Only load URLs from dbooks.org domain to prevent navigation away
                return !request.url.toString().contains("dbooks.org")
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE

                // Start tracking reading progress for PDF
                startPdfProgressTracking()
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress < 100) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress = progress
                }
            }
        }

        // Observe reading progress
        viewModel.readingProgress.observe(this) { progress ->
            if (progress > 0) {
                // Try to restore position for PDF
                tryToRestorePdfPosition(progress)
                Log.d("ReadingActivity", "Attempting to restore reading position: $progress%")
            }
        }

        // Get last reading position from database
        book?.id?.let { bookId ->
            viewModel.getReadingProgress(bookId, userId!!)
        }

        // Load book URL with correct format
        val bookTitle = book?.title?.lowercase()?.replace(" ", "-")?.replace(Regex("[^a-z0-9-]"), "") ?: ""
        val pdfUrl = "https://www.dbooks.org/$bookTitle-${book?.id}/pdf/"
        Log.d("ReadingActivity", "Loading PDF URL: $pdfUrl")
        binding.webView.loadUrl(pdfUrl)
    }

    private var progressTrackingHandler: Handler? = null
    private var progressTrackingRunnable: Runnable? = null

    private fun startPdfProgressTracking() {
        // Record reading session start time
        readingStartTime = System.currentTimeMillis()

        // Cancel any existing timers
        progressTrackingTimer?.cancel()
        progressTrackingTimer = null

        // Clean up any existing handler callbacks
        progressTrackingHandler?.removeCallbacksAndMessages(null)

        // Initialize handler if needed
        if (progressTrackingHandler == null) {
            progressTrackingHandler = Handler(Looper.getMainLooper())
        }

        // Create the runnable for progress tracking
        progressTrackingRunnable = object : Runnable {
            override fun run() {
                checkAndUpdatePdfProgress()
                // Schedule the next check after 5 seconds
                progressTrackingHandler?.postDelayed(this, 5000)
            }
        }

        // Start the periodic checks
        progressTrackingHandler?.post(progressTrackingRunnable!!)
    }

    private fun checkAndUpdatePdfProgress() {
        binding.webView.post {
            binding.webView.evaluateJavascript(
                """
            (function() {
                if (typeof PDFViewerApplication !== 'undefined') {
                    var currentPage = PDFViewerApplication.page;
                    var totalPages = PDFViewerApplication.pagesCount;
                    return JSON.stringify({currentPage: currentPage, totalPages: totalPages});
                }
                return JSON.stringify({currentPage: 0, totalPages: 0});
            })();
            """
            ) { result ->
                try {
                    val cleanResult = result.trim('"').replace("\\", "")
                    val jsonObject = JSONObject(cleanResult)
                    val currentPage = jsonObject.optInt("currentPage", 0)
                    val totalPages = jsonObject.optInt("totalPages", 0)

                    if (totalPages > 0 && currentPage > 0) {
                        val progressPercentage = (currentPage.toFloat() / totalPages * 100).toInt()
                        val boundedProgress = progressPercentage.coerceIn(0, 100)
                        book?.id?.let { bookId ->
                            viewModel.updateReadingProgress(bookId, userId!!, boundedProgress)
                            Log.d("ReadingActivity", "PDF Progress updated: Page $currentPage of $totalPages ($boundedProgress%)")
                        }

                        Log.d("TrackingMethod", "Using JavaScript page tracking")

                    } else {
                        // Fallback to time-based progress estimation
                        updateProgressBasedOnTime()
                        Log.d("TrackingMethod", "Using Time-based tracking (pages info unavailable)")

                    }
                } catch (e: Exception) {
                    Log.e("ReadingActivity", "Error getting PDF page: ${e.message}")
                    // Fallback to time-based progress estimation
                    updateProgressBasedOnTime()
                }
            }
        }
    }
    private fun updateProgressBasedOnTime() {
        // If we can't detect PDF pages, use time as a rough approximation
        val totalReadingTime = (System.currentTimeMillis() - readingStartTime) / 1000 // in seconds
        val totalPages = book?.pages?.toIntOrNull() ?: 100 // Default to 100 if unknown

        // Estimate reading speed as 30 seconds per page (adjust as needed)
        val readingSpeed = 30 // seconds per page
        val estimatedPagesRead = (totalReadingTime / readingSpeed).toInt()
        val progressPercentage = (estimatedPagesRead.toFloat() / totalPages * 100).toInt()

        // Limit progress to reasonable bounds (0-100%)
        val boundedProgress = progressPercentage.coerceIn(0, 100)

        // Update reading progress
        book?.id?.let { bookId ->
            viewModel.updateReadingProgress(bookId, userId!!, boundedProgress)
            Log.d("ReadingActivity", "Time-based progress: ${boundedProgress}% (est. $estimatedPagesRead of $totalPages pages)")
        }
    }

    private fun tryToRestorePdfPosition(progress: Int) {
        // For PDFs, we need to try to set the page based on the progress percentage
        // This is challenging since most PDF viewers in WebView are third-party

        // Wait a moment for the PDF viewer to initialize
        Handler(Looper.getMainLooper()).postDelayed({
            // Get the total pages from the book metadata if available
            val totalPages = book?.pages?.toIntOrNull() ?: 0

            if (totalPages > 0) {
                // Calculate the target page
                val targetPage = (totalPages * progress / 100.0).toInt().coerceAtLeast(1)

                // Try to navigate to the page using JavaScript
                binding.webView.evaluateJavascript(
                    """
                    (function() {
                        if (typeof PDFViewerApplication !== 'undefined') {
                            PDFViewerApplication.page = $targetPage;
                            return true;
                        }
                        return false;
                    })();
                    """,
                    { result ->
                        Log.d("ReadingActivity", "PDF page navigation result: $result")
                    }
                )
            }
        }, 3000) // Give the PDF viewer 3 seconds to initialize
    }

    override fun onPause() {
        super.onPause()
        // Remove callbacks to pause tracking - handle the nullable case properly
        progressTrackingRunnable?.let { runnable ->
            progressTrackingHandler?.removeCallbacks(runnable)
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.webView.url != null) {
            // Restart tracking
            startPdfProgressTracking()
        }
    }

    override fun onDestroy() {
        // Clean up all resources
        progressTrackingTimer?.cancel()
        progressTrackingTimer = null
        progressTrackingHandler?.removeCallbacksAndMessages(null)
        progressTrackingHandler = null
        progressTrackingRunnable = null
        binding.webView.destroy()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


}