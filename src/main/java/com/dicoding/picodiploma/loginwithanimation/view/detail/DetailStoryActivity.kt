package com.dicoding.picodiploma.loginwithanimation.view.detail

import android.os.Build
import android.os.Bundle
import android.transition.TransitionInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryEntity
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityDetailStoryBinding
import com.dicoding.picodiploma.loginwithanimation.view.main.ListStoryItem

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Transisi animasi (jika ada)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val transition = TransitionInflater.from(this).inflateTransition(android.R.transition.move)
            window.sharedElementEnterTransition = transition
            window.sharedElementReturnTransition = transition
        }

        val story = intent.getParcelableExtra<StoryEntity>("EXTRA_STORY") // <--- Ganti ke StoryEntity

        if (story != null) {
            displayStoryDetail(story)
        } else {
            binding.tvUserName.text = "Story tidak ditemukan"
            binding.tvDescription.text = "Deskripsi tidak tersedia"
        }
    }

    private fun displayStoryDetail(story: StoryEntity) {
        binding.tvUserName.text = story.name
        binding.tvDescription.text = story.description
        Glide.with(this)
            .load(story.photoUrl)
            .into(binding.ivStoryImage)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Set up the shared element callback to map the shared element view names
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterSharedElementCallback(object : SharedElementCallback() {
                override fun onMapSharedElements(names: List<String>?, sharedElements: MutableMap<String, android.view.View>?) {
                    super.onMapSharedElements(names, sharedElements)
                    // Map the transition names to the views
                    names?.let {
                        if (it.contains("story_image")) {
                            sharedElements?.put("story_image", binding.ivStoryImage)
                        }
                        if (it.contains("story_name")) {
                            sharedElements?.put("story_name", binding.tvUserName)
                        }
                    }
                }
            })
        }
    }
}
