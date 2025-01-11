package com.dicoding.picodiploma.loginwithanimation.data.remotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.RemoteKeys
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryEntity
import com.dicoding.picodiploma.loginwithanimation.data.local.room.StoryDatabase
import com.dicoding.picodiploma.loginwithanimation.network.ApiService

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService
) : androidx.paging.RemoteMediator<Int, StoryEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val pageSize = state.config.pageSize
            val response = apiService.getStories(page, pageSize)

            val endOfPaginationReached = response.listStory.isEmpty()

            val listStoryEntity = response.listStory.map { item ->
                StoryEntity(
                    id = item.id ?: "",
                    name = item.name ?: "Unknown",
                    description = item.description ?: "No description",
                    photoUrl = item.photoUrl ?: "",
                    createdAt = item.createdAt ?: "",
                    lat = item.lat ?: 0.0,
                    lon = item.lon ?: 0.0
                )
            }

            storyDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    storyDatabase.remoteKeysDao().clearRemoteKeys()
                    storyDatabase.storyDao().clearAll()
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val keys = listStoryEntity.map { story ->
                    RemoteKeys(
                        id = story.id,
                        prevKey = prevKey,
                        nextKey = nextKey
                    )
                }

                storyDatabase.remoteKeysDao().insertAll(keys)
                storyDatabase.storyDao().insertStories(listStoryEntity)
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.lastOrNull()?.data?.lastOrNull()?.let { story ->
            storyDatabase.remoteKeysDao().getRemoteKeysId(story.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.firstOrNull()?.data?.firstOrNull()?.let { story ->
            storyDatabase.remoteKeysDao().getRemoteKeysId(story.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, StoryEntity>
    ): RemoteKeys? {
        val anchorPosition = state.anchorPosition ?: return null
        val closestItem = state.closestItemToPosition(anchorPosition) ?: return null
        return storyDatabase.remoteKeysDao().getRemoteKeysId(closestItem.id)
    }
}
