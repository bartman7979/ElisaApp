package com.bartman79.elisa

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.ivSplashLogo)
        logo.alpha = 0f
        logo.animate().alpha(1f).setDuration(1000).start()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)
    }

    // Применяем сохранённый язык
    override fun attachBaseContext(newBase: Context) {
        val lang = getSavedLanguage(newBase)
        super.attachBaseContext(updateBaseContextLocale(newBase, lang))
    }

    private fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        return prefs.getString("app_lang", "ru") ?: "ru"
    }

    private fun updateBaseContextLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}