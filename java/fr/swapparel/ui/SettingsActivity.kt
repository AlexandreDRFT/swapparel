package fr.swapparel.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.swapparel.R
import info.hoang8f.widget.FButton
import spencerstudios.com.bungeelib.Bungee

class SettingsActivity : AppCompatActivity(), View.OnClickListener {
    private var lastButton = 0
    private var isLanguageSelected = false
    private var language = "en"
    private var unit = "C"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreenCall()
        setContentView(R.layout.activity_settings)

        findViewById<FButton>(R.id.save_button).setOnClickListener(this)

        findViewById<FButton>(R.id.germanButton).setOnClickListener(this)
        findViewById<FButton>(R.id.netherlandsButton).setOnClickListener(this)
        findViewById<FButton>(R.id.englishButton).setOnClickListener(this)
        findViewById<FButton>(R.id.polishButton).setOnClickListener(this)
        findViewById<FButton>(R.id.italianButton).setOnClickListener(this)
        findViewById<FButton>(R.id.frenchButton).setOnClickListener(this)
        findViewById<FButton>(R.id.spanishButton).setOnClickListener(this)

        findViewById<FButton>(R.id.japaneseButton).setOnClickListener(this)
        findViewById<FButton>(R.id.chineseButton).setOnClickListener(this)
        findViewById<FButton>(R.id.koreanButton).setOnClickListener(this)

        findViewById<FButton>(R.id.unit_1).setOnClickListener(this)
        findViewById<FButton>(R.id.unit_2).setOnClickListener(this)
    }

    // default method for handling onClick Events..
    override fun onClick(v: View) {
        if (lastButton != v.id && lastButton != 0 && lastButton != R.id.save_button && v.id != R.id.unit_1 && v.id != R.id.unit_2 && lastButton != R.id.unit_1 && lastButton != R.id.unit_2) {
            findViewById<FButton>(lastButton).buttonColor = Color.parseColor("#ECECEC")
        }

        try {
            val b = findViewById<FButton>(v.id)
            b.buttonColor = Color.parseColor("#19B5FE")
            lastButton = v.id
            if (v.id != R.id.save_button && v.id != R.id.unit_1 && v.id != R.id.unit_2) {
                isLanguageSelected = true
            }
        } catch (e: Exception) {
        }

        when (v.id) {
            R.id.save_button -> {
                if (isLanguageSelected) {
                    val sharedPrefs = applicationContext.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)
                    sharedPrefs.edit()
                            .putString("lang", language)
                            .putString("unit", unit)
                            .apply()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Choisissez votre langue !", Toast.LENGTH_LONG).show()
                }
            }

            R.id.germanButton -> language = "de"
            R.id.englishButton -> language = "en"
            R.id.frenchButton -> language = "fr"
            R.id.italianButton -> language = "it"
            R.id.japaneseButton -> language = "ja"
            R.id.koreanButton -> language = "kr"
            R.id.netherlandsButton -> language = "nl"
            R.id.polishButton -> language = "pl"
            R.id.spanishButton -> language = "es"
            R.id.chineseButton -> language = "zh_cn"

            R.id.unit_1 -> {
                findViewById<FButton>(R.id.unit_2).buttonColor = Color.parseColor("#ECECEC")
                unit = "C"
            }
            R.id.unit_2 -> {
                findViewById<FButton>(R.id.unit_1).buttonColor = Color.parseColor("#ECECEC")
                unit = "F"
            }
            else -> {
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Bungee.slideRight(this) //fire the slide left animation
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