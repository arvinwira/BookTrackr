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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = book?.title ?: "Reading"

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
            setSupportMultipleWindows(true)
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return !request.url.toString().contains("dbooks.org")
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE

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

        viewModel.readingProgress.observe(this) { progress ->
            if (progress > 0) {
                tryToRestorePdfPosition(progress)
                Log.d("ReadingActivity", "Attempting to restore reading position: $progress%")
            }
        }

        book?.id?.let { bookId ->
            viewModel.getReadingProgress(bookId, userId!!)
        }

        val bookTitle = book?.title?.lowercase()?.replace(" ", "-")?.replace(Regex("[^a-z0-9-]"), "") ?: ""
        val pdfUrl = "https://www.dbooks.org/$bookTitle-${book?.id}/pdf/"
        Log.d("ReadingActivity", "Loading PDF URL: $pdfUrl")
        binding.webView.loadUrl(pdfUrl)
    }

    private var progressTrackingHandler: Handler? = null
    private var progressTrackingRunnable: Runnable? = null

    private fun startPdfProgressTracking() {
        readingStartTime = System.currentTimeMillis()

        progressTrackingTimer?.cancel()
        progressTrackingTimer = null

        progressTrackingHandler?.removeCallbacksAndMessages(null)

        if (progressTrackingHandler == null) {
            progressTrackingHandler = Handler(Looper.getMainLooper())
        }

        progressTrackingRunnable = object : Runnable {
            override fun run() {
                checkAndUpdatePdfProgress()
                progressTrackingHandler?.postDelayed(this, 5000)
            }
        }

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
                        updateProgressBasedOnTime()
                        Log.d("TrackingMethod", "Using Time-based tracking (pages info unavailable)")

                    }
                } catch (e: Exception) {
                    Log.e("ReadingActivity", "Error getting PDF page: ${e.message}")
                    updateProgressBasedOnTime()
                }
            }
        }
    }
    private fun updateProgressBasedOnTime() {
        val totalReadingTime = (System.currentTimeMillis() - readingStartTime) / 1000 // in seconds
        val totalPages = book?.pages?.toIntOrNull() ?: 100 // Default to 100 if unknown

        val readingSpeed = 30 // seconds per page
        val estimatedPagesRead = (totalReadingTime / readingSpeed).toInt()
        val progressPercentage = (estimatedPagesRead.toFloat() / totalPages * 100).toInt()

        val boundedProgress = progressPercentage.coerceIn(0, 100)

        book?.id?.let { bookId ->
            viewModel.updateReadingProgress(bookId, userId!!, boundedProgress)
            Log.d("ReadingActivity", "Time-based progress: ${boundedProgress}% (est. $estimatedPagesRead of $totalPages pages)")
        }
    }

    private fun tryToRestorePdfPosition(progress: Int) {

        Handler(Looper.getMainLooper()).postDelayed({
            val totalPages = book?.pages?.toIntOrNull() ?: 0

            if (totalPages > 0) {
                val targetPage = (totalPages * progress / 100.0).toInt().coerceAtLeast(1)

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
        }, 3000)
    }

    override fun onPause() {
        super.onPause()
        progressTrackingRunnable?.let { runnable ->
            progressTrackingHandler?.removeCallbacks(runnable)
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.webView.url != null) {
            startPdfProgressTracking()
        }
    }

    override fun onDestroy() {
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