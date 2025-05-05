package com.example.skripsi.ui.tracking.finishedreading

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
import com.example.skripsi.databinding.ItemBookFinishedBinding
import com.example.skripsi.ui.bookdetail.BookDetailActivity

class FinishedReadingAdapter (
    private val onDeleteClick: (BookEntity) -> Unit
) : ListAdapter<BookEntity, FinishedReadingAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookFinishedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BookViewHolder(
        private val binding: ItemBookFinishedBinding,
        private val onDeleteClick: (BookEntity) -> Unit
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

            binding.root.setOnClickListener {
                val bookData = Book(
                    id = book.id,
                    title = book.title,
                    subtitle = book.subtitle,
                    authors = book.authors,
                    publisher = book.publisher,
                    pages = book.pages,
                    year = book.year,
                    description = book.description,
                    image = book.image,
                    url = book.url,
                    download = book.download
                )
                val intent = Intent(binding.root.context, BookDetailActivity::class.java).apply {
                    putExtra("book", bookData)
                }
                binding.root.context.startActivity(intent)

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


