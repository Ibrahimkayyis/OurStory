package com.dicoding.picodiploma.loginwithanimation.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryEntity
import com.dicoding.picodiploma.loginwithanimation.data.local.room.StoryDatabase
import com.dicoding.picodiploma.loginwithanimation.data.remotemediator.StoryRemoteMediator
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.model.Story
import com.dicoding.picodiploma.loginwithanimation.network.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.network.ApiService
import com.dicoding.picodiploma.loginwithanimation.network.TokenManager
import com.dicoding.picodiploma.loginwithanimation.view.login.LoginResponse
import com.dicoding.picodiploma.loginwithanimation.view.main.ListStoryItem
import com.dicoding.picodiploma.loginwithanimation.view.main.StoryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val storyDatabase: StoryDatabase
) {

    // Ambil ApiService secara dinamis agar token selalu up-to-date
    private val apiService: ApiService
        get() = ApiConfig.getApiService()

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun login(email: String, password: String): LoginResponse {
        val response = apiService.login(email, password)
        if (response.error == false) {
            val token = response.loginResult?.token.orEmpty()
            saveSessionAfterLogin(UserModel(response.loginResult?.name.orEmpty(), token, true))
            TokenManager.setToken(token) // Simpan token ke TokenManager
        }
        return response
    }

    suspend fun saveSessionAfterLogin(user: UserModel) {
        userPreference.saveSession(user)
        TokenManager.setToken(user.token) // Perbarui token di TokenManager
        Log.d("UserRepository", "Session saved with token: ${user.token}")
    }

    suspend fun getStories(): StoryResponse {
        return apiService.getStories()
    }

    suspend fun logout() {
        userPreference.logout()
        TokenManager.setToken("") // Reset token di TokenManager
    }

    suspend fun register(name: String, email: String, password: String) =
        apiService.register(name, email, password)

    suspend fun getStoriesWithLocation(): List<Story> {
        return withContext(Dispatchers.IO) {
            val response = apiService.getStoriesWithLocation(1)
            val listFromApi = response.listStory

            listFromApi.map { item ->
                Story(
                    userName = item.name ?: "Unknown",
                    description = item.description ?: "No description",
                    imageUrl = item.photoUrl ?: "",
                    lat = item.lat ?: 0.0,
                    lon = item.lon ?: 0.0
                )
            }
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getAllStoriesPaged(): Flow<PagingData<StoryEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 5,
                enablePlaceholders = false
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStories()
            }
        ).flow
    }

    // Fungsi untuk membersihkan dan memasukkan ulang cerita ke database
    suspend fun clearAndInsertStories(stories: List<ListStoryItem>) {
        storyDatabase.withTransaction {
            storyDatabase.storyDao().clearAll()
            storyDatabase.storyDao().insertStories(stories.map {
                StoryEntity(
                    id = it.id ?: "",
                    name = it.name ?: "",
                    description = it.description ?: "",
                    photoUrl = it.photoUrl ?: "",
                    createdAt = it.createdAt ?: "",
                    lat = it.lat,
                    lon = it.lon
                )
            })
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            storyDatabase: StoryDatabase
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, storyDatabase)
            }.also { instance = it }
    }
}
