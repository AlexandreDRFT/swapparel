package fr.swapparel.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.thanosfisherman.mayi.Mayi
import com.thanosfisherman.mayi.PermissionBean
import com.thanosfisherman.mayi.PermissionToken
import fr.swapparel.BuildConfig
import fr.swapparel.R
import fr.swapparel.data.ApparelRawDataLoader
import fr.swapparel.data.LoadFromDatabase
import fr.swapparel.extensions.MyLocation
import fr.swapparel.temp.MigrationApparel
import fr.swapparel.extensions.whatsnew.WhatsNew
import fr.swapparel.extensions.whatsnew.listener.WhatsNewListener
import fr.swapparel.extensions.whatsnew.model.Feature
import fr.swapparel.extensions.whatsnew.util.FeatureItemAnimator
import kotlinx.android.synthetic.main.activity_main.*
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import spencerstudios.com.bungeelib.Bungee
import java.util.*


class MainActivity : AppCompatActivity() {
    private var cityName = ""
    private var cityLongitude: Double = 0.0
    private var cityLatitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPrefs = applicationContext.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)
        setContentView(R.layout.activity_main)
        setupBackground()

        AppCenter.start(
            application,
            "e7744472-e8ac-4763-9f87-b50f7d5d3152",
            Analytics::class.java,
            Crashes::class.java
        )
        MobileAds.initialize(this, "ca-app-pub-6874203165727037~3943313046")

        //createNotificationChannel()

        //TODO on récup les params dans Rooms et on fait un Bundle avec les coords, qui seront utilisées dans Rooms
        val extras = intent.extras
        try{
            if (extras != null) {
                cityName = extras.getString("NAME")!!
                cityLongitude = extras.getDouble("LONGITUDE")
                cityLatitude = extras.getDouble("LATITUDE")
            }
            else if(!isLocationEnabled(this))
            {
                val intent = Intent(this, CitiesActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                getLocation()
            }
        } catch(e: KotlinNullPointerException){
            if(!isLocationEnabled(this))
            {
                val intent = Intent(this, CitiesActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                getLocation()
            }
        }


        checkForFirstTime()
        setWeatherLanguage()

        val myRunnable = Runnable {
            while(cityName == "")
                Thread.sleep(100)
            runOnUiThread {
                animation_view.cancelAnimation()
                animation_view.visibility = View.GONE
                setup()
            }
        }
        Thread(myRunnable).start()

        if (sharedPrefs.getInt("updateVersion", -1) < BuildConfig.VERSION_CODE) {
            displayChangelog()
            //TODO changelog
            sharedPrefs.edit()
                .putInt("updateVersion", BuildConfig.VERSION_CODE)
                .apply()
        }

        //We check if migration as already been done; if not, we initiate it to fix the database and apologize to the user :(
        if(!sharedPrefs.contains("hasMigrationCompleted")) {
            LoadFromDatabase().loadRawData(this, ApparelRawDataLoader { mangoes ->
                mangoes.forEach {
                    if (!MigrationApparel().checkIfMigrated(it.heaviness)) {
                        it.heaviness = MigrationApparel().migrateHeaviness(it.heaviness)
                        LoadFromDatabase().update(it, this)
                    }
                    sharedPrefs.edit()
                        .putBoolean("hasMigrationCompleted", true)
                        .apply()
                }
            })
        }
    }

    private fun getLocation() {
        Mayi.withActivity(this)
            .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            .onRationale(this::permissionRationaleMulti)
            .onResult(this::permissionResultMulti)
            .check()
    }

    //Permission was not granted
    private fun permissionRationaleMulti(permissions: Array<PermissionBean>, token: PermissionToken) {
        token.continuePermissionRequest()

        val intent = Intent(this, CitiesActivity::class.java)
        startActivity(intent)
        finish()
    }

    //Permission was granted
    private fun permissionResultMulti(permissions: Array<PermissionBean>) {
        runOnUiThread{
            val locationResult: MyLocation.LocationResult = object : MyLocation.LocationResult() {
                override fun gotLocation(location: Location?) {
                    try {// Stuff that updates the UI
                        val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(location!!.latitude, location.longitude, 1)
                        cityName = addresses[0].locality

                        cityLongitude = location.longitude
                        cityLatitude = location.latitude
                    } catch (e: Exception) {
                        val intent = Intent(this@MainActivity, CitiesActivity::class.java)
                        startActivity(intent)
                        Bungee.slideDown(this@MainActivity)
                    }
                }
            }
            val myLocation = MyLocation()
            myLocation.getLocation(this, locationResult)
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationMode: Int

        try {
            locationMode = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)

        } catch (e: Settings.SettingNotFoundException) {
            return false
        }

        return locationMode != Settings.Secure.LOCATION_MODE_OFF
    }

    //TODO changelog
    private fun displayChangelog() {
        //Displays the changelog on each new update
        val features = ArrayList<Feature>().apply {
            this.add(
                Feature.Builder()
                    .setIconRes(R.drawable.ic_update)
                    .setTitleRes(R.string.update_features)
                    .setTitleTextColor(Color.BLACK)
                    .setDescriptionRes("A new temporary icon to celebrate Swapparel's 1st birthday anniversary !")
                    .setDescriptionTextColor(Color.BLACK)
                    .build()
            )
            this.add(
                Feature.Builder()
                    .setIconRes(R.drawable.ic_update)
                    .setTitleRes(R.string.update_fix)
                    .setTitleTextColor(Color.BLACK)
                    .setDescriptionRes("Small stability improvements.")
                    .setDescriptionTextColor(Color.BLACK)
                    .build()
            )
        }
        WhatsNew.Builder(this)
            .setTitleRes(R.string.update)
            .setTitleColor(Color.BLACK)
            .setBackgroundRes(android.R.color.white)
            .setPrimaryButtonBackgroundColor(Color.RED)
            .setPrimaryButtonTextColor(Color.WHITE)
            .setPrimaryButtonTextRes(R.string.press_start)
            .enableSecondaryButtonAllCaps(false)
            .enableFadeAnimation(true)
            .setFadeAnimationDuration(500L)
            .setFeatureItemAnimator(FeatureItemAnimator.FADE_IN_UP)
            .setFeatureItemAnimatorDuration(500L)
            .setFeatures(features)
            .setListener(object : WhatsNewListener {
                override fun onWhatsNewShowed(whatsNew: WhatsNew) {

                }

                override fun onWhatsNewDismissed() {

                }

                override fun onPrimaryButtonClicked(whatsNew: WhatsNew) {

                }

                override fun onSecondaryButtonClicked(whatsNew: WhatsNew) {

                }
            })
            .build()
    }

    private fun setWeatherLanguage() {
        val sharedPrefs = applicationContext.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)
        var languageToLoad = sharedPrefs.getString("lang", "en") // your language
        //Change the EasyWeather code to android locale codes when they are different
        if (languageToLoad == "zh_cn")
            languageToLoad = "zh"
        else if (languageToLoad == "kr")
            languageToLoad = "ko"
        val locale = Locale(languageToLoad)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    //Check if user has set its language before
    private fun checkForFirstTime() {
        val sharedPrefs = applicationContext.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)
        if (!sharedPrefs.contains("firstTimeDone")) {
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
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

    private fun setup() {
        //Set the viewpager
        val dotsIndicator = findViewById<DotsIndicator>(R.id.dots_indicator)
        val pager = findViewById<View>(R.id.viewPager) as androidx.viewpager.widget.ViewPager
        val myAdapter = MyPagerAdapter(supportFragmentManager)
        myAdapter.setCity(cityName, cityLatitude, cityLongitude)
        pager.adapter = myAdapter
        pager.offscreenPageLimit = 2
        dotsIndicator.setViewPager(pager)
    }

    private fun setupBackground()
    {
        //Gestion du background
        val background = findViewById<ConstraintLayout>(R.id.constraintMain)
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

    private class MyPagerAdapter(fm: androidx.fragment.app.FragmentManager) :
        androidx.fragment.app.FragmentPagerAdapter(fm) {

        override fun getCount(): Int {
            return 3
        }

        private var cityName: String = ""
        private var cityLatitude: Double = 0.0
        private var cityLongitude: Double = 0.0

        fun setCity(cityName : String, cityLatitude: Double, cityLongitude:Double)
        {
            this.cityName = cityName
            this.cityLatitude = cityLatitude
            this.cityLongitude = cityLongitude
        }

        override fun getItem(pos: Int): androidx.fragment.app.Fragment {
            return when (pos) {

                0 -> HomeFragment.newInstance(cityName, cityLatitude, cityLongitude)
                1 -> DressFragment.newInstance(cityLatitude, cityLongitude)
                2 -> ProFragment.newInstance("ProFragment")
                else -> HomeFragment.newInstance(cityName, cityLatitude, cityLongitude)
            }

        }
    }
}
