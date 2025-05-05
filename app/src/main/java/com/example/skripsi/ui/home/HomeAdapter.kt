package com.example.skripsi.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skripsi.R
import com.example.skripsi.data.remote.Book
import com.example.skripsi.databinding.ItemBookHomeBinding

class HomeAdapter(
    private val books: List<Book>,
    private val onBookClick: (Book) -> Unit
) : RecyclerView.Adapter<HomeAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookHomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding, onBookClick)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    class BookViewHolder(
        private val binding: ItemBookHomeBinding,
        private val onBookClick: (Book) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Book) {
            binding.titleTextView.text = book.title
            binding.authorTextView.text = book.authors

            Glide.with(binding.bookImageView.context)
                .load(book.image)
                .placeholder(R.drawable.baseline_broken_image_24)
                .into(binding.bookImageView)

            if (book.image.isNullOrEmpty()) {
                binding.ImageUnknown.visibility = View.VISIBLE
            } else {
                binding.ImageUnknown.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onBookClick(book)
            }
        }
    }
}