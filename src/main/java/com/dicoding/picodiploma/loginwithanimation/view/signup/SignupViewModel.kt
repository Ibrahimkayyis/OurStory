package com.dicoding.picodiploma.loginwithanimation.view.signup

import androidx.lifecycle.ViewModel
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository

class SignupViewModel(private val repository: UserRepository) : ViewModel() {
    suspend fun register(name: String, email: String, password: String) =
        repository.register(name, email, password)
}
