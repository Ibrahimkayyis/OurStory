package com.dicoding.picodiploma.loginwithanimation.view.addstory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.network.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddStoryViewModel(application: Application) : AndroidViewModel(application) {

    private val _uploadResult = MutableLiveData<Result<String>>()
    val uploadResult: LiveData<Result<String>> = _uploadResult

    private val userPreference = UserPreference.getInstance(application.dataStore)

    private suspend fun getToken(): String {
        val userSession = userPreference.getSession().first()
        return userSession.token
    }

    fun uploadStory(file: File, description: String, lat: Double? = null, lon: Double? = null) {
        viewModelScope.launch {
            try {
                val token = getToken()

                if (token.isEmpty()) {
                    _uploadResult.postValue(Result.failure(Exception("Token is missing")))
                    return@launch
                }

                // Set token ke TokenManager untuk memastikan Interceptor menggunakannya
                com.dicoding.picodiploma.loginwithanimation.network.TokenManager.setToken(token)

                val descriptionBody = description.toRequestBody("text/plain".toMediaType())

                val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
                val multipartBody =
                    MultipartBody.Part.createFormData("photo", file.name, requestImageFile)

                val latBody = lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
                val lonBody = lon?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

                val apiService = ApiConfig.getApiService() // Tidak perlu token sebagai argumen

                val response = apiService.uploadStory(
                    file = multipartBody,
                    description = descriptionBody,
                    lat = latBody,
                    lon = lonBody
                )

                if (response.error == false) {
                    _uploadResult.postValue(Result.success("Story uploaded successfully"))
                } else {
                    _uploadResult.postValue(Result.failure(Exception("Error: ${response.message}")))
                }
            } catch (e: Exception) {
                _uploadResult.postValue(Result.failure(e))
            }
        }
    }
}
