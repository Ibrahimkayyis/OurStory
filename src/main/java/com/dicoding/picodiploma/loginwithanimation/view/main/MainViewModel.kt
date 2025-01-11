package com.dicoding.picodiploma.loginwithanimation.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.local.entity.StoryEntity
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import com.dicoding.picodiploma.loginwithanimation.network.ApiConfig
import com.dicoding.picodiploma.loginwithanimation.network.TokenManager

class MainViewModel(private val repository: UserRepository) : ViewModel() {
    private val _stories = MutableLiveData<List<ListStoryItem>>()
    val stories: LiveData<List<ListStoryItem>> = _stories

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Paging
    fun getAllStoriesPaged(): Flow<PagingData<StoryEntity>> =
        repository.getAllStoriesPaged().cachedIn(viewModelScope)

    fun fetchStories() {
        viewModelScope.launch {
            try {
                // Pastikan token di TokenManager selalu diperbarui
                val userSession = repository.getSession().first()
                val token = userSession.token
                TokenManager.setToken(token) // Update TokenManager dengan token terbaru

                val apiService = ApiConfig.getApiService() // Tidak perlu token sebagai argumen

                val response = apiService.getStories()
                Log.d("MainViewModel", "API Response: $response")
                if (!response.error!!) {
                    // Perbarui database lokal dengan data baru
                    repository.clearAndInsertStories(response.listStory)

                    // Perbarui LiveData agar tampilan terbaru
                    _stories.postValue(response.listStory)
                } else {
                    _error.postValue(response.message ?: "Unknown error")
                    Log.e("MainViewModel", "API returned error: ${response.message}")
                }
            } catch (e: Exception) {
                _error.postValue("Error fetching stories: ${e.message}")
                Log.e("MainViewModel", "Error fetching stories: ${e.message}", e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
}
