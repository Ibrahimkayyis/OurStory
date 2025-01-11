package com.dicoding.picodiploma.loginwithanimation.view.login

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.utils.EspressoIdlingResource
import com.dicoding.picodiploma.loginwithanimation.view.main.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.rule.ActivityTestRule

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    var activityRule = ActivityTestRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        // Inisialisasi Intents jika kita mau cek perpindahan activity
        Intents.init()

        // Daftarkan Idling Resource agar Espresso "tahu" kapan idle
        androidx.test.espresso.IdlingRegistry.getInstance().register(
            EspressoIdlingResource.countingIdlingResource
        )
    }

    @After
    fun tearDown() {
        Intents.release()

        // Unregister Idling Resource
        androidx.test.espresso.IdlingRegistry.getInstance().unregister(
            EspressoIdlingResource.countingIdlingResource
        )
    }

    @Test
    fun login_successFlow_showsMainActivity() {
        // Perbaiki ID menjadi emailEditText dan passwordEditText
        onView(withId(R.id.emailEditText))
            .perform(typeText("ryomen@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.passwordEditText))
            .perform(typeText("tes12345"), closeSoftKeyboard())

        // Tekan tombol login (ID: loginButton)
        onView(withId(R.id.loginButton)).perform(click())

        // Periksa apakah berpindah ke MainActivity
        Intents.intended(hasComponent(MainActivity::class.java.name))
    }

    @Test
    fun login_errorFlow_showsToastOrErrorMessage() {
        // Input email & password yang salah
        onView(withId(R.id.emailEditText))
            .perform(typeText("salah@domain.com"), closeSoftKeyboard())
        onView(withId(R.id.passwordEditText))
            .perform(typeText("wrongpass"), closeSoftKeyboard())

        // Tekan tombol login
        onView(withId(R.id.loginButton)).perform(click())

        // Cek apakah menampilkan error (misalnya text "Login failed")
        onView(withText("Login failed"))
            .check(matches(isDisplayed()))
    }
}
