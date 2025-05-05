package com.example.skripsi.ui.tracking.wanttoread

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skripsi.R
import com.example.skripsi.data.local.BookEntity
import com.example.skripsi.data.remote.Book
import com.example.skripsi.databinding.ItemBookWantToReadBinding
import com.example.skripsi.ui.bookdetail.BookDetailActivity

class WantToReadAdapter(
    private val onDeleteClick: (BookEntity) -> Unit,
    private val onBookClick: (BookEntity) -> Unit
) : ListAdapter<BookEntity, WantToReadAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookWantToReadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding, onDeleteClick, onBookClick)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookViewHolder(
        private val binding: ItemBookWantToReadBinding,
        private val onDeleteClick: (BookEntity) -> Unit,
        private val onBookClick: (BookEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: BookEntity) {
            binding.titleTextView.text = book.title
            binding.authorTextView.text = book.authors

            Glide.with(binding.bookImageView.context)
                .load(book.image)
                .placeholder(R.drawable.baseline_broken_image_24)
                .into(binding.bookImageView)

            binding.deleteButton.setOnClickListener {
                onDeleteClick(book)
            }

            // Set click listener on the entire item
            binding.root.setOnClickListener {
                onBookClick(book)
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<BookEntity>() {
        override fun areItemsTheSame(oldItem: BookEntity, newItem: BookEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BookEntity, newItem: BookEntity): Boolean {
            return oldItem == newItem
        }
    }
}