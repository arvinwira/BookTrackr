package com.example.skripsi.ui.tracking.wanttoread

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsi.R
import com.example.skripsi.data.local.BookEntity
import com.example.skripsi.data.remote.Book
import com.example.skripsi.data.repository.Repository
import com.example.skripsi.databinding.ActivityWantToReadBinding
import com.example.skripsi.ui.ViewModelFactory
import com.example.skripsi.ui.bookdetail.BookDetailActivity
import com.google.firebase.auth.FirebaseAuth

class WantToReadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWantToReadBinding
    private val viewModel: WantToReadViewModel by viewModels {
        ViewModelFactory(Repository(applicationContext))
    }
    private lateinit var adapter: WantToReadAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWantToReadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.button_Want_To_Read)

        // Set up adapter with delete and click listeners
        adapter = WantToReadAdapter(
            onDeleteClick = { book -> viewModel.deleteBook(book) },
            onBookClick = { book -> navigateToBookDetail(book) }
        )

        // Set up RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Get current user
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Load books if user is logged in
        userId?.let { viewModel.loadWantToReadBooks(it) }

        // Observe books from ViewModel
        viewModel.books.observe(this) { books ->
            // Toggle empty state visibility based on book list
            binding.emptyStateLayout.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (books.isEmpty()) View.GONE else View.VISIBLE

            adapter.submitList(books)
        }
    }

    // Function to navigate to book details
    private fun navigateToBookDetail(bookEntity: BookEntity) {
        // Convert BookEntity to Book for BookDetailActivity
        val book = Book(
            id = bookEntity.id,
            title = bookEntity.title,
            subtitle = bookEntity.subtitle,
            authors = bookEntity.authors,
            publisher = bookEntity.publisher,
            pages = bookEntity.pages,
            year = bookEntity.year,
            description = bookEntity.description,
            image = bookEntity.image,
            url = bookEntity.url,
            download = bookEntity.download
        )

        val intent = Intent(this, BookDetailActivity::class.java).apply {
            putExtra("book", book)
        }
        startActivity(intent)
    }

    // Refresh data when returning to activity
    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { viewModel.loadWantToReadBooks(it) }
    }

    // Handle back button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}