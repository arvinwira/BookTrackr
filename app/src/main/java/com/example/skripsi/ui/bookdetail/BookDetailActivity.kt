package com.example.skripsi.ui.bookdetail

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.skripsi.R
import com.example.skripsi.data.remote.Book
import com.example.skripsi.data.repository.Repository
import com.example.skripsi.databinding.ActivityBookDetailBinding
import com.example.skripsi.ui.ViewModelFactory
import com.example.skripsi.ui.reading.ReadingActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.view.ContextThemeWrapper


class BookDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookDetailBinding
    private val viewModel: BookDetailViewModel by viewModels {
        ViewModelFactory(Repository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val book: Book? = intent.getSerializableExtra("book") as? Book
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        book?.let {
            viewModel.setBookData(it)
        } ?: Log.e("BookDetailActivity", "Book is null!")

        observeViewModel()

        binding.manageBookButton.setOnClickListener { view ->
            showManageBookMenu(view, book, userId)
        }

        binding.readButton.setOnClickListener {
            if (book != null && userId != null) {
                viewModel.onCurrentlyReadingButtonClicked(book, userId)
                Toast.makeText(this, "Book added to Currently Reading", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ReadingActivity::class.java)
                intent.putExtra("book", book)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Book information is not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.book.observe(this) { book ->
            displayBookDetails(book)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            Log.e("BookDetailActivity", errorMessage)
        }
    }

    private fun displayBookDetails(book: Book) {
        binding.tvTitle.text = book.title ?: "No Title Available"
        binding.tvAuthors.text = book.authors ?: "Unknown Author"
        binding.tvPublisher.text = book.publisher ?: "No Publisher Info"
        binding.tvPages.text = book.pages?.let { "$it pages" } ?: "Page count unavailable"
        binding.tvYear.text = book.year ?: "Year unknown"
        binding.tvDescription.text = book.description ?: "No Description Available"
        binding.tvSubtitle.text = book.subtitle ?: "No Subtitle Available"

        Glide.with(this)
            .load(book.image)
            .into(binding.ivBookImage)
    }

    private fun showManageBookMenu(view: View, book: Book?, userId: String?) {
        // Create the popup menu
        val popupMenu = PopupMenu(this, view, Gravity.END)
        popupMenu.menuInflater.inflate(R.menu.track_book_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            handleMenuItemClick(menuItem, book, userId)
            true
        }

        popupMenu.show()

        view.animate().scaleX(1.05f).scaleY(1.05f).setDuration(150).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
        }.start()
    }

    private fun handleMenuItemClick(menuItem: MenuItem, book: Book?, userId: String?) {
        lifecycleScope.launch {
            when (menuItem.itemId) {
                R.id.action_currently_reading -> {
                    book?.let { userId?.let { uid ->
                        viewModel.onCurrentlyReadingButtonClicked(it, uid)
                        Toast.makeText(this@BookDetailActivity, "Book added to Currently Reading", Toast.LENGTH_SHORT).show()
                    }}
                }
                R.id.action_want_to_read -> {
                    book?.let { userId?.let { uid ->
                        viewModel.onWantToReadButtonClicked(it, uid)
                        Toast.makeText(this@BookDetailActivity, "Book added to Want to Read", Toast.LENGTH_SHORT).show()
                    }}
                }
                R.id.action_finished_reading -> {
                    book?.let { userId?.let { uid ->
                        viewModel.onFinishedReadingButtonClicked(it, uid)
                        Toast.makeText(this@BookDetailActivity, "Book added to Finished Reading", Toast.LENGTH_SHORT).show()
                    }}
                }
            }
        }
    }
}