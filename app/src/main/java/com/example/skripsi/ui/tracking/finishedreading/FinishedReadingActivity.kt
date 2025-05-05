package com.example.skripsi.ui.tracking.finishedreading

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsi.R
import com.example.skripsi.data.repository.Repository
import com.example.skripsi.databinding.ActivityFinishedReadingBinding
import com.example.skripsi.ui.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class FinishedReadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFinishedReadingBinding
    private val viewModel: FinishedReadingViewModel by viewModels {
        ViewModelFactory(Repository(applicationContext))
    }
    private lateinit var adapter: FinishedReadingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinishedReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.button_Finished_Reading)
        }

        adapter = FinishedReadingAdapter { book -> viewModel.deleteBook(book) }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { viewModel.loadFinishedReadingBooks(it) }

        viewModel.books.observe(this) { books ->
            adapter.submitList(books)
            updateEmptyState(books.isEmpty())
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyStateLayout.visibility = View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}