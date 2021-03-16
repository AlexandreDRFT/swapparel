package fr.swapparel.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Place
import com.taskail.googleplacessearchdialog.SimplePlacesSearchDialog
import com.taskail.googleplacessearchdialog.SimplePlacesSearchDialogBuilder
import fr.swapparel.R
import kotlinx.android.synthetic.main.activity_cities.*
import spencerstudios.com.bungeelib.Bungee
import java.util.*

class CitiesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cities)
        setup()
        fullScreenCall()

        launchSearch(ctrCities)
    }

    fun launchSearch(v: View){
        //TODO afficher les autres adresses

        val searchDialog = SimplePlacesSearchDialogBuilder(this)
            .setResultsFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
            .setLocationListener(object : SimplePlacesSearchDialog.PlaceSelectedCallback {
                override fun onPlaceSelected(place: Place) {
                    //TODO sauvegarder la localisation dans Rooms et passer en bundle
                    val intent = Intent(this@CitiesActivity, MainActivity::class.java)
                    intent.putExtra("NAME", place.name)
                    intent.putExtra("LATITUDE", place.latLng.latitude)
                    intent.putExtra("LONGITUDE", place.latLng.longitude)
                    startActivity(intent)
                    Bungee.slideLeft(this@CitiesActivity)
                    finish()
                }
            }).build()

        searchDialog.show()
    }

    private fun setup() {
        //Gestion du background
        val background = ctrCities
        val heureActuelle = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (heureActuelle) {
            7 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle7)
            8 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle8)
            9 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle9)
            10 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle10)
            11 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle11)
            12 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle12)
            13 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle13)
            14 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle14)
            15 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle15)
            16 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle16)
            17 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle17)
            18 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle18)
            19 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle19)
            20 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle20)
            21 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle21)
            22 -> background.background =
                ContextCompat.getDrawable(applicationContext, R.drawable.rectangle22)
            else -> {
                background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle_nuit)
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
