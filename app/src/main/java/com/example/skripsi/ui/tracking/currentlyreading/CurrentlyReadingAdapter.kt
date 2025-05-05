package com.example.skripsi.ui.tracking.currentlyreading

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skripsi.R
import com.example.skripsi.data.local.BookEntity
import com.example.skripsi.data.local.ReadingProgressEntity
import com.example.skripsi.data.remote.Book
import com.example.skripsi.databinding.ItemBookCurrentlyBinding
import com.example.skripsi.ui.bookdetail.BookDetailActivity
import com.example.skripsi.ui.reading.ReadingActivity

class CurrentlyReadingAdapter(
    private val onDeleteClick: (BookEntity) -> Unit,
    private val onBookClick: (BookEntity) -> Unit
) : ListAdapter<CurrentlyReadingAdapter.BookWithProgress, CurrentlyReadingAdapter.BookViewHolder>(BookDiffCallback()) {

    // Data class to hold book with its reading progress
    data class BookWithProgress(
        val book: BookEntity,
        val progress: Int = 0
    )

    private var bookProgressMap = mutableMapOf<String, Int>()

    fun updateProgressData(progressMap: Map<String, Int>) {
        bookProgressMap.clear()
        bookProgressMap.putAll(progressMap)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookCurrentlyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding, onDeleteClick, onBookClick)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val bookWithProgress = getItem(position)
        holder.bind(bookWithProgress.book, bookWithProgress.progress)
    }

    class BookViewHolder(
        private val binding: ItemBookCurrentlyBinding,
        private val onDeleteClick: (BookEntity) -> Unit,
        private val onBookClick: (BookEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(book: BookEntity, progress: Int) {
            binding.titleTextView.text = book.title
            binding.authorTextView.text = book.authors
            binding.progressBar.progress = progress
            binding.progressText.text = "$progress%"

            Glide.with(binding.bookImageView.context)
                .load(book.image)
                .placeholder(R.drawable.baseline_broken_image_24)
                .into(binding.bookImageView)

            binding.deleteButton.setOnClickListener {
                onDeleteClick(book)
            }

            // Set click listener on the item view
            binding.root.setOnClickListener {
                onBookClick(book)
            }

            // Set click listener for direct reading button
            binding.readNowButton.setOnClickListener {
                // Convert BookEntity to Book for ReadingActivity
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

                val intent = Intent(binding.root.context, ReadingActivity::class.java).apply {
                    putExtra("book", bookData)
                }
                binding.root.context.startActivity(intent)
            }
        }
    }

    class BookDiffCallback : DiffUtil.ItemCallback<BookWithProgress>() {
        override fun areItemsTheSame(oldItem: BookWithProgress, newItem: BookWithProgress): Boolean {
            return oldItem.book.id == newItem.book.id
        }

        override fun areContentsTheSame(oldItem: BookWithProgress, newItem: BookWithProgress): Boolean {
            return oldItem == newItem
        }
    }
}