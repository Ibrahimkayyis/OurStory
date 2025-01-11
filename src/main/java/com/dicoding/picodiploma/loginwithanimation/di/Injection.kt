package com.dicoding.picodiploma.loginwithanimation.di

import android.content.Context
import android.util.Log
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.local.room.StoryDatabase
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.network.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val userPreference = UserPreference.getInstance(context.dataStore)

        // Ambil token dari sesi pengguna
        val user = runBlocking { userPreference.getSession().first() }
        Log.d("Injection", "Providing repository with token: '${user.token}'")

        // Perbarui TokenManager dengan token terbaru
        TokenManager.setToken(user.token)

        // Dapatkan instance database
        val database = StoryDatabase.getInstance(context)

        // Kembalikan instance UserRepository
        return UserRepository.getInstance(userPreference, database)
    }
}
