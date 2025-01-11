package com.dicoding.picodiploma.loginwithanimation

import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryEntity

object DataDummy {
    fun generateDummyStoriesEntity(): List<StoryEntity> {
        val list = ArrayList<StoryEntity>()
        for (i in 0 until 10) {
            val story = StoryEntity(
                id = "story-$i",
                name = "User $i",
                description = "Description $i",
                photoUrl = "https://example.com/photo/$i.jpg",
                createdAt = "2024-01-01T00:00:00Z",
                lat = null,
                lon = null
            )
            list.add(story)
        }
        return list
    }

    fun generateEmptyStoriesEntity(): List<StoryEntity> {
        return emptyList()
    }
}
