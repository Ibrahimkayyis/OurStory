package com.dicoding.picodiploma.loginwithanimation.view.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryEntity
import com.dicoding.picodiploma.loginwithanimation.databinding.ItemStoryBinding

class StoryPagingAdapter(
    private val onClick: (StoryEntity, View, View) -> Unit
) : PagingDataAdapter<StoryEntity, StoryPagingAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    class StoryViewHolder(
        private val binding: ItemStoryBinding,
        private val onClick: (StoryEntity, View, View) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(story: StoryEntity) {
            binding.tvStoryName.text = story.name
            binding.tvStoryDescription.text = story.description
            Glide.with(binding.ivStoryPhoto.context)
                .load(story.photoUrl)
                .into(binding.ivStoryPhoto)

            // Panggil onClick ketika item di-klik
            itemView.setOnClickListener {
                onClick(story, binding.ivStoryPhoto, binding.tvStoryName)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}
