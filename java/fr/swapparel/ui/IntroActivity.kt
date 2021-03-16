package fr.swapparel.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import fr.swapparel.R
import info.hoang8f.widget.FButton
import kotlinx.android.synthetic.main.activity_intro.*
import spencerstudios.com.bungeelib.Bungee
import java.util.*

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreenCall()
        setContentView(R.layout.activity_intro)

        val lang = Resources.getSystem().configuration.locale.language

        val sharedPrefs = applicationContext.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("lang", lang)
            .apply()

        press_start.visibility = View.INVISIBLE
        access_settings.visibility = View.INVISIBLE

        celsius.setOnClickListener {
            sharedPrefs.edit()
                .putBoolean("firstTimeDone", true)
                .putString("unit", "C")
                .apply()
            nextStep()
        }

        fahrenheit.setOnClickListener {
            sharedPrefs.edit()
                .putBoolean("firstTimeDone", true)
                .putString("unit", "F")
                .apply()
            nextStep()
        }

        findViewById<FButton>(R.id.press_start).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            Bungee.zoom(this)
            finish()
        }

        access_settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            Bungee.zoom(this)
            finish()
        }
    }

    private fun nextStep() {
        celsius.visibility = View.INVISIBLE
        fahrenheit.visibility = View.INVISIBLE
        textView25.visibility = View.INVISIBLE
        access_settings.visibility = View.VISIBLE
        press_start.visibility = View.VISIBLE
    }

    private fun fullScreenCall() {
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions
    }
}
