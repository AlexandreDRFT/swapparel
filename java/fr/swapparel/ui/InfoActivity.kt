package fr.swapparel.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import fr.swapparel.R
import kotlinx.android.synthetic.main.activity_info.*
import spencerstudios.com.bungeelib.Bungee


class InfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        about.setOnClickListener {
            showApropos()
        }

        reglages.setOnClickListener {
            showSettings()
        }

        rateApp()
    }

    private fun showApropos() {
        val intent = Intent(this, AproposActivity::class.java)
        startActivity(intent)
        Bungee.slideLeft(this)
    }

    private fun showSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        Bungee.slideLeft(this)
    }

    private fun rateApp() {
        playstore.setOnClickListener {
            try {
                var playstoreuri1: Uri = Uri.parse("market://details?id=$packageName")
                //or you can add
                //var playstoreuri:Uri=Uri.parse("market://details?id=manigautam.app.myplaystoreratingapp")
                var playstoreIntent1: Intent = Intent(Intent.ACTION_VIEW, playstoreuri1)
                startActivity(playstoreIntent1)
                //it genrate exception when devices do not have playstore
            }catch (exp:Exception){
                var playstoreuri2: Uri = Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                //var playstoreuri:Uri=Uri.parse("http://play.google.com/store/apps/details?id=manigautam.app.myplaystoreratingapp")
                var playstoreIntent2: Intent = Intent(Intent.ACTION_VIEW, playstoreuri2)
                startActivity(playstoreIntent2)
            }
        }
    }

    //Enlève les barres inutiles et met en fullscreen
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}
