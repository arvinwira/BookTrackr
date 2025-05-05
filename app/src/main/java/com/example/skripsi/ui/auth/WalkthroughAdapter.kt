package com.example.skripsi.ui.auth

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsi.R

class WalkthroughAdapter(private val walkthroughItems: List<WalkthroughItem>) :
    RecyclerView.Adapter<WalkthroughAdapter.WalkthroughViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkthroughViewHolder {
        return WalkthroughViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_walkthrough_slide,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: WalkthroughViewHolder, position: Int) {
        holder.bind(walkthroughItems[position])
    }

    override fun getItemCount(): Int = walkthroughItems.size

    inner class WalkthroughViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView = view.findViewById<ImageView>(R.id.walkthroughImage)
        private val titleView = view.findViewById<TextView>(R.id.walkthroughTitle)
        private val descriptionView = view.findViewById<TextView>(R.id.walkthroughDescription)

        fun bind(walkthroughItem: WalkthroughItem) {
            try {
                if (walkthroughItem.imageResId != 0) {
                    imageView.setImageResource(walkthroughItem.imageResId)
                }
            } catch (e: Exception) {
                Log.e("WalkthroughAdapter", "Error setting image: ${e.message}")
                imageView.setImageResource(R.drawable.baseline_broken_image_24)
            }

            titleView.text = walkthroughItem.title
            titleView.setTextColor(itemView.context.getColor(android.R.color.black))

            descriptionView.text = walkthroughItem.description
            descriptionView.setTextColor(itemView.context.getColor(android.R.color.black))

            Log.d("WalkthroughAdapter", "Binding text: ${walkthroughItem.title}")
        }
    }

    data class WalkthroughItem(
        val imageResId: Int,
        val title: String,
        val description: String
    )
}