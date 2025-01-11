package com.dicoding.picodiploma.loginwithanimation.view.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.dicoding.picodiploma.loginwithanimation.R
import android.widget.Button
import android.widget.Toast
import com.dicoding.picodiploma.loginwithanimation.view.main.MainActivity

class LanguageSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_settings)

        val btnIndonesian = findViewById<Button>(R.id.btn_language_indonesia)
        val btnEnglish = findViewById<Button>(R.id.btn_language_english)

        btnIndonesian.setOnClickListener {
            setLanguage("id")
            Toast.makeText(this, "Language changed to Indonesian", Toast.LENGTH_SHORT).show()
        }

        btnEnglish.setOnClickListener {
            setLanguage("en")
            Toast.makeText(this, "Language changed to English", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLanguage(languageCode: String) {
        val locale = java.util.Locale(languageCode)
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

        // Restart activity to apply the language change
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}
