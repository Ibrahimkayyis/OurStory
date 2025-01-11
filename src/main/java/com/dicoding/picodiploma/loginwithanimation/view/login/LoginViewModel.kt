package com.dicoding.picodiploma.loginwithanimation.view.login

import androidx.lifecycle.ViewModel
import com.dicoding.picodiploma.loginwithanimation.data.UserRepository
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserModel
import com.dicoding.picodiploma.loginwithanimation.utils.EspressoIdlingResource

class LoginViewModel(private val repository: UserRepository) : ViewModel() {


    suspend fun login(email: String, password: String) =
        try {

            EspressoIdlingResource.increment()
            repository.login(email, password)
        } finally {

            EspressoIdlingResource.decrement()
        }


    suspend fun saveSession(user: UserModel) {
        repository.saveSessionAfterLogin(user)
    }
}
