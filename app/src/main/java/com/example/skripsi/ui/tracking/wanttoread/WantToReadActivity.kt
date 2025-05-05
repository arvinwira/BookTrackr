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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.button_Want_To_Read)

        adapter = WantToReadAdapter(
            onDeleteClick = { book -> viewModel.deleteBook(book) },
            onBookClick = { book -> navigateToBookDetail(book) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        userId?.let { viewModel.loadWantToReadBooks(it) }

        viewModel.books.observe(this) { books ->
            binding.emptyStateLayout.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (books.isEmpty()) View.GONE else View.VISIBLE

            adapter.submitList(books)
        }
    }

    private fun navigateToBookDetail(bookEntity: BookEntity) {
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

    override fun onResume() {
        super.onResume()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { viewModel.loadWantToReadBooks(it) }
    }

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