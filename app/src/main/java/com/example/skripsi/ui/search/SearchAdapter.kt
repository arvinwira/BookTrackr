package com.example.skripsi.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skripsi.R
import com.example.skripsi.data.remote.Book
import com.example.skripsi.databinding.ItemBookBinding

class SearchAdapter(
    private val books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<SearchAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    inner class BookViewHolder(private val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Book) {
            binding.titleTextView.text = book.title ?: "No Title Available"
            binding.authorTextView.text = book.authors ?: "Unknown Author"

            Glide.with(binding.bookImageView.context)
                .load(book.image)
                .placeholder(R.drawable.baseline_broken_image_24)
                .into(binding.bookImageView)

            binding.root.setOnClickListener {
                onBookClick(book)
            }
        }
    }
}