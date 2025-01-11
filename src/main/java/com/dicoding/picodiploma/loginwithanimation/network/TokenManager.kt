package com.dicoding.picodiploma.loginwithanimation.network

object TokenManager {
    @Volatile
    private var token: String? = null

    fun setToken(newToken: String) {
        token = newToken
    }

    fun getToken(): String? {
        return token
    }
}
