package com.example.haptix.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.haptix.databinding.ItemVideoCardBinding
import com.example.haptix.model.VideoItem
import com.example.haptix.util.Logger

/**
 * Adapter for displaying video cards in a RecyclerView
 */
class VideoListAdapter(
    private val onVideoClick: (VideoItem) -> Unit
) : ListAdapter<VideoItem, VideoListAdapter.VideoViewHolder>(VideoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding, onVideoClick)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder for video card items
     */
    class VideoViewHolder(
        private val binding: ItemVideoCardBinding,
        private val onVideoClick: (VideoItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(video: VideoItem) {
            binding.apply {
                // Set video title
                textViewTitle.text = video.title

                // Load thumbnail with Glide
                Glide.with(imageViewThumbnail.context)
                    .load(video.thumbnailUrl)
                    .centerCrop()
                    .into(imageViewThumbnail)

                // Set click listener
                root.setOnClickListener {
                    Logger.d("Video clicked: ${video.title}")
                    onVideoClick(video)
                }
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private class VideoDiffCallback : DiffUtil.ItemCallback<VideoItem>() {
        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem == newItem
        }
    }
}