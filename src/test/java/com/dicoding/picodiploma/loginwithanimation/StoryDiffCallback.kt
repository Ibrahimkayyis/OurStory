package com.dicoding.picodiploma.loginwithanimation



import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryEntity

class StoryDiffCallback : DiffUtil.ItemCallback<StoryEntity>() {
    override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
        return oldItem == newItem
    }
}


class NoopListUpdateCallback : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) = Unit
    override fun onRemoved(position: Int, count: Int) = Unit
    override fun onMoved(fromPosition: Int, toPosition: Int) = Unit
    override fun onChanged(position: Int, count: Int, payload: Any?) = Unit
}
