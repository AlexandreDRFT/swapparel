package fr.swapparel.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather
import com.kwabenaberko.openweathermaplib.models.threehourforecast.ThreeHourForecast
import com.mikhaellopez.circularimageview.CircularImageView
import fr.swapparel.R
import fr.swapparel.data.Apparel
import fr.swapparel.data.ApparelLoader
import fr.swapparel.data.LoadFromDatabase
import fr.swapparel.extensions.*
import fr.swapparel.temp.MigrationApparel
import info.hoang8f.widget.FButton
import kotlinx.android.synthetic.main.fragment_dress.*
import libs.mjn.prettydialog.PrettyDialog
import net.hockeyapp.android.metrics.MetricsManager
import spencerstudios.com.bungeelib.Bungee
import java.util.*

class DressFragment : Fragment() {
    var isExtraSentenceActivated = false
    private lateinit var v: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_dress, container, false)
        this.v = v

        val sharedPrefs = context!!.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)
        if (!sharedPrefs.getBoolean("proUser", false)) {
            hideShoes()
        }
        setup()

        val layout = v.findViewById<ConstraintLayout>(R.id.constraintDress)
        layout.visibility = View.INVISIBLE

        if(arguments == null) {
            getLocation(RetrieveWeather { response ->
                val lottieObject =
                    v.findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.loading_view)
                lottieObject.cancelAnimation()
                lottieObject.visibility = View.INVISIBLE
                //The change button respawns the validate button if already pressed, and generates new clothing advice.
                val change = v.findViewById<FButton>(R.id.change_button)
                change.setOnClickListener {
                    generateClothingAdvice(response, false)
                    isExtraSentenceActivated = false
                    v.findViewById<FButton>(R.id.validate_button).visibility = View.VISIBLE
                    v.findViewById<LottieAnimationView>(R.id.animation_view).visibility =
                        View.INVISIBLE
                }
                if (response.highestWindSpeed != -69)
                    generateClothingAdvice(response, true)
                layout.visibility = View.VISIBLE
            })
        }
        else //Il ya des paramètres
        {
            val cityLongitude = arguments!!.getDouble("LONGITUDE")
            val cityLatitude = arguments!!.getDouble("LATITUDE")

            getCurrentWeatherState(cityLatitude, cityLongitude)
            getWeatherVariables(
                cityLatitude,
                cityLongitude,
                RetrieveWeather { response ->
                    val lottieObject =
                        v.findViewById<com.airbnb.lottie.LottieAnimationView>(R.id.loading_view)
                    lottieObject.cancelAnimation()
                    lottieObject.visibility = View.INVISIBLE
                    //The change button respawns the validate button if already pressed, and generates new clothing advice.
                    val change = v.findViewById<FButton>(R.id.change_button)
                    change.setOnClickListener {
                        generateClothingAdvice(response, false)
                        isExtraSentenceActivated = false
                        v.findViewById<FButton>(R.id.validate_button).visibility = View.VISIBLE
                        v.findViewById<LottieAnimationView>(R.id.animation_view).visibility =
                            View.INVISIBLE
                    }
                    if (response.highestWindSpeed != -69)
                        generateClothingAdvice(response, true)
                    layout.visibility = View.VISIBLE
                })
        }

        return v
    }

    private fun setup() {
        //Gestion du bouton réglages
        val settings = v.findViewById<ConstraintLayout>(R.id.touchListenerInfo)
        settings.setOnClickListener { MetricsManager.trackEvent("SHOW_SETTINGS")
            showSettings() }

        val clothes = v.findViewById<ConstraintLayout>(R.id.touchListenerClothes)
        clothes.setOnClickListener { MetricsManager.trackEvent("SHOW_CLOTHES")
            showClothes() }

        //Supprime le background qui ne sert qu'au design
        val background = v.findViewById<ConstraintLayout>(R.id.bcground)
        background.setBackgroundResource(0)

        val validate = v.findViewById<FButton>(R.id.validate_button)
        validate.setOnClickListener {
            MetricsManager.trackEvent("VALIDATED")
            val anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
            anim.duration = 250
            validate.startAnimation(anim)
            validate.postDelayed({
                validate.visibility = View.INVISIBLE
                val lot = v.findViewById<LottieAnimationView>(R.id.animation_view)
                lot.visibility = View.VISIBLE
                lot.playAnimation()
                val sharedPrefs = activity!!.applicationContext.getSharedPreferences(
                    "SNAPPAREL",
                    Context.MODE_PRIVATE
                )
                if (!sharedPrefs.contains("hasSeenValidateMessage")) {
                    val pDialog = PrettyDialog(context)
                    pDialog
                        .setTitle(getString(R.string.validated))
                        .setMessage(getString(R.string.validating_helps_us))
                        .setIcon(R.drawable.launcher)
                        .addButton(
                            "OK",
                            R.color.hockeyapp_text_white,
                            R.color.fbutton_color_green_sea
                        ) { pDialog.dismiss() }
                        .show()
                    sharedPrefs.edit()
                        .putBoolean("hasSeenValidateMessage", true)
                        .apply()
                }
            }, 250)
        }
    }

    private fun showClothes() {
        val intent = Intent(activity, DressingListActivity::class.java)
        startActivity(intent)
        Bungee.zoom(context)
    }

    private fun showSettings() {
        val intent = Intent(activity, InfoActivity::class.java)
        startActivity(intent)
        Bungee.zoom(context)
    }

    private fun getLocation(weatherRetriever: RetrieveWeather) {
        //Handle the permission thingy
        val fineNotOK = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        val coarseNotOK = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

        if (fineNotOK || coarseNotOK) {
            if (fineNotOK)
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    0
                )
            if (coarseNotOK)
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    0
                )
        } else {
            val locationResult: MyLocation.LocationResult = object : MyLocation.LocationResult() {
                override fun gotLocation(location: Location) {
                    getCurrentWeatherState(location.latitude, location.longitude)
                    getWeatherVariables(
                        location.latitude,
                        location.longitude,
                        RetrieveWeather { response ->
                            weatherRetriever.onWeatherReceived(response)
                        })
                }
            }
            val myLocation = MyLocation()
            myLocation.getLocation(context, locationResult)
        }
    }

    //The user has responded to the permissions ask
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun getCurrentWeatherState(latitude: Double, longitude: Double) {
        val sharedPrefs =
            activity!!.applicationContext.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)

        //New weather library
        val helper = OpenWeatherMapHelper()
        helper.setApiKey("c08c7c9f9e80827d2b3b3b818a77c856")
        helper.setLang(sharedPrefs.getString("lang", null))
        helper.getCurrentWeatherByGeoCoordinates(
            latitude,
            longitude,
            object : OpenWeatherMapHelper.CurrentWeatherCallback {
                override fun onSuccess(response: CurrentWeather) {
                    //In case the user recovers his internet connection, we show dat layout again
                    v.findViewById<ConstraintLayout>(R.id.constraintDress).visibility = View.VISIBLE

                    v.findViewById<TextView>(R.id.textView16).text =
                            response.weatherArray[0].description.toUpperCase()

                    val preText =
                        v.findViewById<TextView>(R.id.justification) //the text that will be changed afterwards in generateClothingAdvice
                    when (response.weatherArray[0].main) {
                        "Rain" -> {  //its cold AND it's raining
                            if (convertNumToCelsius(response.main.temp.toString()) <= 10) {
                                preText.text = getString(R.string.and_it_rains)
                                isExtraSentenceActivated = true
                            } else if (convertNumToCelsius(response.main.temp.toString()) >= 25) {
                                preText.text = getString(R.string.but_it_rains)
                                isExtraSentenceActivated = true
                            }
                        }
                        "Snow" -> {
                            if (convertNumToCelsius(response.main.temp.toString()) <= 10) {
                                preText.text = getString(R.string.and_its_snow)
                                isExtraSentenceActivated = true
                            } else if (convertNumToCelsius(response.main.temp.toString()) >= 25) {
                                preText.text = getString(R.string.wtf_snow)
                                isExtraSentenceActivated = true
                            }
                        }
                        "Clear" -> {
                            if (convertNumToCelsius(response.main.temp.toString()) <= 10) {
                                preText.text = getString(R.string.but_sunny)
                                isExtraSentenceActivated = true
                            }
                        }
                    }

                    val temp = response.main.temp
                    val texteTemp = v.findViewById<TextView>(R.id.textView19)
                    if (sharedPrefs.getString("unit", null) == "C") {
                        texteTemp.text = convertToCelsius(temp.toString())
                    } else {
                        texteTemp.text = convertToFahrenheit(temp.toString())
                    }
                }

                override fun onFailure(message: Throwable) {
                    v.findViewById<ConstraintLayout>(R.id.constraintDress).visibility =
                            View.INVISIBLE
                    Toast.makeText(context, message.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    fun convertNumToCelsius(temp: String): Int {
        return Math.round(TempUnitConverter.convertToCelsius(temp)).toInt()
    }

    fun convertToCelsius(temp: String): String {
        return Math.round(TempUnitConverter.convertToCelsius(temp)).toString() + "°"
    }

    fun convertToFahrenheit(temp: String): String {
        return Math.round(TempUnitConverter.convertToFahrenheit(temp)).toString() + "°"
    }

    //Takes a number and returns a random choice between 0 and this number INCLUDED
    private fun randomChoice(num: Int): Int {
        return Random().nextInt(num + 1)
    }

    //isFirstDisplay : hen you press the change button, there shouldn't be a string concatenation to get the part about the current weather.
    private fun generateClothingAdvice(data: WeatherPrediction, isFirstDisplay: Boolean) {
        val heureActuelle = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val justification = v.findViewById<TextView>(R.id.justification)
        var justifTexte = ""
        val top = v.findViewById<CircularImageView>(R.id.topImage)
        val pants = v.findViewById<CircularImageView>(R.id.pantsImage)
        val combi = v.findViewById<CircularImageView>(R.id.combiImage)

        val m = MigrationApparel()

        //Composition of the recommendation sentence :
        //(1) It's cold/hot/... (2) AND/OR it's raining /this morning/ (optional)
        //(3) I recommend/advise (4) un warm pullover, raincoat, sunglasses... (5) Have a good day/Good night ... !

        var sentencePart1 = ""
        var sentencePart2 = ""
        var sentencePart3: String = when (randomChoice(4)) {
            0 -> getString(R.string.i_advise)
            1 -> getString(R.string.i_recommand)
            2 -> getString(R.string.todays_advice)
            3 -> getString(R.string.a_perfect_weather)
            4 -> getString(R.string.advice_hours)
            else -> ""
        }
        var sentencePart4 = ""
        var sentencePart5 = ""

        //Create sentence part 3

        if (top.visibility == View.VISIBLE) {
            val anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
            anim.duration = 250
            top.startAnimation(anim)
            pants.startAnimation(anim)
            top.postDelayed({
                top.visibility = View.INVISIBLE
                pants.visibility = View.INVISIBLE
            }, 250)
        } else {
            val anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
            anim.duration = 250
            combi.startAnimation(anim)
            combi.postDelayed({
                combi.visibility = View.INVISIBLE
            }, 250)
        }


        //Variables aidant à choisir les vêtements adaptés
        val targetedHeaviness: Int

        //Gestion de l'heure actuelle
        when (heureActuelle) {
            in 0..4 -> {
                sentencePart2 = getString(R.string.tonight)
                sentencePart5 = getString(R.string.goodnite)
            }
            in 5..12 -> {
                sentencePart2 = getString(R.string.this_mornin)
                sentencePart5 = getString(R.string.goodday)
            }
            in 13..18 -> {
                sentencePart2 = getString(R.string.this_afternoon)
                sentencePart5 = getString(R.string.good_afternoon)
            }
            in 19..24 -> {
                sentencePart2 = getString(R.string.tonite)
                sentencePart5 = getString(R.string.good_tonight)
            }
        }

        //Cette targetedHeaviness est migrée !
        when (data.targetTemperatures[0]) {
            in 35..99 -> {
                targetedHeaviness = 90
                sentencePart1 = getString(R.string.superhot)
                sentencePart4 = getString(R.string.debardeur)
            }
            in 28..34 -> {
                targetedHeaviness = 90
                sentencePart1 = getString(R.string.veryhot)
                sentencePart4 = getString(R.string.tshirt)
            }
            in 20..27 -> {
                targetedHeaviness = 10
                sentencePart1 = getString(R.string.agradable)
                sentencePart4 = getString(R.string.classic_tee)
            }
            in 10..19 -> {
                targetedHeaviness = 20
                sentencePart1 = getString(R.string.mediocre)
                sentencePart4 = getString(R.string.longsleeves)
            }
            in 0..9 -> {
                targetedHeaviness = 30
                sentencePart1 = getString(R.string.low_temperatures)
                sentencePart4 = getString(R.string.pull)
            }
            in -10..-1 -> {
                targetedHeaviness = 40
                sentencePart1 = getString(R.string.under0)
                sentencePart4 = getString(R.string.bigcoat)
            }
            in -99..-11 -> {
                targetedHeaviness = 40
                justifTexte = getString(R.string.minus_30_dressing)
            }
            else -> targetedHeaviness = 40
        }

        //Spaces management because Google
        sentencePart1 = "$sentencePart1 "
        sentencePart2 = "$sentencePart2 "
        sentencePart3 = "$sentencePart3 "
        sentencePart5 = " $sentencePart5"

        if (!isExtraSentenceActivated) { //Meaning we changed it because it's raining/snowing and unusual temps
            when (data.objectiveWeather[1]) {
                "Rain" -> {
                    if (m.getMaxTempFromHeaviness(targetedHeaviness) in 2..4) {
                        sentencePart2 = getString(R.string.upcoming_rain) + " " + sentencePart2
                    } else if (m.getMaxTempFromHeaviness(targetedHeaviness) in 0..1) {
                        sentencePart2 = getString(R.string.upcoming_rain_but) + " " + sentencePart2
                    }
                }

                "Snow" -> {
                    if (m.getMaxTempFromHeaviness(targetedHeaviness) in 2..4) {
                        sentencePart2 = getString(R.string.upcoming_snow) + " " + sentencePart2
                    } else if (m.getMaxTempFromHeaviness(targetedHeaviness) in 0..1) {
                        sentencePart2 = getString(R.string.upcoming_snow_but) + " " + sentencePart2
                    }
                }
            }
        } else if (isFirstDisplay && justification.text.length < 50) {
            sentencePart2 = justification.text.toString() + ". "
        }

        //If justifText was changed beforehand, it means there is a special text like the freezing warning, so we'll change the display texxt
        //only if its value is "" later on
        if (justifTexte == "" && isFirstDisplay) {
            MetricsManager.trackEvent("STYLE_GENERATED")
            val heresYourPlaceholderStringFuckinGoogle =
                sentencePart1 + sentencePart2 + sentencePart3 + sentencePart4 + sentencePart5
            justification.text = heresYourPlaceholderStringFuckinGoogle
        } else if (!isFirstDisplay) {
            //Do nothin' because we don't need to change the text.
        } else {
            justification.text = justifTexte
        }

        LoadFromDatabase().loadApparel(context!!, ApparelLoader { fringues ->
            val databaseTops = mutableListOf<Apparel>()
            val databasePants = mutableListOf<Apparel>()
            val databaseCombis = mutableListOf<Apparel>()
            val databaseShoes = mutableListOf<Apparel>()

            fringues.forEach {
                try {
                    if(it.type != getString(R.string.shoes)) {
                        var heavinessLowIndex = m.getMaxTempFromHeaviness(targetedHeaviness) - 1
                        if (heavinessLowIndex < 0)
                            heavinessLowIndex = 0
                        heavinessLowIndex = m.migrateHeaviness(heavinessLowIndex)

                        var heavinessHighIndex = m.getMaxTempFromHeaviness(targetedHeaviness) + 1
                        heavinessHighIndex = m.migrateHeaviness(heavinessHighIndex)

                        if (m.isTargetInRange(targetedHeaviness, it.heaviness) || m.isTargetInRange(
                                heavinessHighIndex,
                                it.heaviness
                            ) || (it.type == getString(
                                R.string.pants
                            ) && m.isTargetInRange(heavinessLowIndex, it.heaviness))
                        ) {
                            when (it.type) {
                                getString(R.string.top) -> databaseTops.add(it)
                                getString(R.string.pants) -> databasePants.add(it)
                                getString(R.string.combi) -> databaseCombis.add(it)
                                getString(R.string.shoes) -> databaseShoes.add(it)
                            }
                        }
                    }
                    else
                    {
                        databaseShoes.add(it)
                    }
                } catch (e: Exception) { //Exception avec les moins chelous
                    //On ne fait rien, ignore le vêtement en question
                }
            }

            val rnd = Random()
            var isWearingCombi = !databaseCombis.isEmpty()
            if (isWearingCombi) isWearingCombi = rnd.nextInt(7) == 4

            if (!databaseTops.isEmpty() && !databasePants.isEmpty()) {
                if (!isWearingCombi) {
                    val choosenTopList = databaseTops[rnd.nextInt(databaseTops.size)]
                    val choosenPantsList = mutableListOf<Apparel>()

                    val complementaryColors = ColorMatching.matchPinterest(choosenTopList.color)

                    //Recherche les couleurs les + proches des couleurs complémentaires dans les pantalons
                    databasePants.forEach {
                        it.lowestScoreMatching = 100000000.0
                        complementaryColors.forEach { col ->
                            val z = ColorMatching.calculateSimilarity(it.color, col).toDouble()
                            if (z < it.lowestScoreMatching)
                                it.lowestScoreMatching = z
                        }
                        choosenPantsList.add(it)
                    }

                    if (choosenPantsList.size >= 3) {
                        val sharedPrefs =
                            v.context.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)
                        sharedPrefs.edit()
                            .remove("objective")
                            .putBoolean("userDidExperienceApp", true)
                            .apply()
                        val result =
                            choosenPantsList.sortedWith(compareBy { it.lowestScoreMatching })
                        val choosenPants = result[rnd.nextInt(3)]

                        top.postDelayed({
                            val anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
                            anim.duration = 250
                            Glide.with(context!!)
                                .load(
                                    ImageLoader(context).setFileName(
                                        choosenTopList.drawablePath
                                    ).setDirectoryName("Snapparel").filePath
                                )
                                .into(top)
                            Glide.with(context!!)
                                .load(
                                    ImageLoader(context).setFileName(
                                        choosenPants.drawablePath
                                    ).setDirectoryName("Snapparel").filePath
                                )
                                .into(pants)
                            top.startAnimation(anim)
                            pants.startAnimation(anim)

                            top.setBorderColor(Color.parseColor(choosenTopList.color))
                            pants.setBorderColor(Color.parseColor(choosenPants.color))

                            //Handle the shoe choosing
                            //If any error we catch the exception and reset the layout to the non-pro mode
                            val shoes = v.findViewById<CircularImageView>(R.id.shoesImage)
                            try {
                                val complementaryShoeColors =
                                    ColorMatching.matchPinterest(choosenPants.color)
                                val choosenShoesList = mutableListOf<Apparel>()
                                databaseShoes.forEach {
                                    it.lowestScoreMatching = 100000000.0
                                    complementaryShoeColors.forEach { col ->
                                        val z = ColorMatching.calculateSimilarity(it.color, col)
                                            .toDouble()
                                        if (z < it.lowestScoreMatching)
                                            it.lowestScoreMatching = z
                                    }
                                    choosenShoesList.add(it)
                                }

                                //If there's only one shoe only display that one, else display the first three (or two if there's two)
                                var rndSize = 3
                                if (choosenShoesList.size == 1)
                                    rndSize = 1
                                else if (choosenShoesList.size == 2)
                                    rndSize = 2

                                //Sort shoes by best matching
                                val resultShoes =
                                    choosenShoesList.sortedWith(compareBy { it.lowestScoreMatching })
                                val choosenShoe = resultShoes[rnd.nextInt(rndSize)]
                                Glide.with(context!!)
                                    .load(
                                        ImageLoader(context).setFileName(
                                            choosenShoe.drawablePath
                                        ).setDirectoryName("Snapparel").filePath
                                    )
                                    .into(shoes)
                                shoes.setBorderColor(Color.parseColor(choosenShoe.color))
                                reshowShoes()
                            } catch (e: Exception) {
                                //Reset the layout to no shoes
                                hideShoes()
                            }

                            top.postDelayed({
                                top.visibility = View.VISIBLE
                            }, 250)
                            pants.postDelayed({
                                pants.visibility = View.VISIBLE
                            }, 250)
                        }, 250)
                    } else //There's not enough pants to make a recommandation.
                    {
                        summonEmptyState(data)
                    }
                } else {
                    combi.postDelayed({
                        hideShoesForCombi()
                        val choosenCombi = databaseCombis[rnd.nextInt(databaseCombis.size)]
                        combi.setBorderColor(Color.parseColor(choosenCombi.color))
                        Glide.with(context!!)
                            .load(
                                ImageLoader(context).setFileName(
                                    choosenCombi.drawablePath
                                ).setDirectoryName("Snapparel").filePath
                            )
                            .into(combi)
                        val anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
                        anim.duration = 250
                        combi.startAnimation(anim)
                        combi.postDelayed({
                            combi.visibility = View.VISIBLE
                        }, 250)
                    }, 250)
                }
            } else {
                summonEmptyState(data)
            }
        })
    }

    private fun hideShoesForCombi() {
        val shoes = v.findViewById<CircularImageView>(R.id.shoesImage)
        shoes.visibility = View.INVISIBLE
    }

    private fun reshowShoes() {
        shoesImage.visibility = View.VISIBLE
    }

    private fun hideShoes() {
        val shoes = v.findViewById<CircularImageView>(R.id.shoesImage)
        shoes.visibility = View.INVISIBLE
        //Move the layout
        val cs = ConstraintSet()
        val layout = v.findViewById<View>(R.id.constraintDress) as ConstraintLayout
        cs.clone(layout)
        cs.setVerticalBias(R.id.layout_clothes, 1.0f)
        cs.applyTo(layout)
    }

    private fun summonEmptyState(data: WeatherPrediction) {
        val sharedPrefs = v.context.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)
        v.findViewById<FButton>(R.id.change_button).visibility = View.INVISIBLE
        v.findViewById<FButton>(R.id.validate_button).visibility = View.INVISIBLE
        val explanation = v.findViewById<androidx.cardview.widget.CardView>(R.id.objective)
        val explanation2 = v.findViewById<androidx.cardview.widget.CardView>(R.id.objective2)
        var subTextAdd: String

        if (sharedPrefs.getString("unit", null) == "C") {
            subTextAdd = when (data.targetTemperatures[0]) {
                in 30..99 -> "30°C +"
                in 20..29 -> "20-30°C"
                in 10..19 -> "10-20°C"
                in 0..9 -> "0-10°C"
                in -99..-1 -> "- 0°C"
                else -> ""
            }
        } else {
            subTextAdd = when (data.targetTemperatures[0]) {
                in 30..99 -> "90°F+"
                in 20..29 -> "70 to 90°F"
                in 10..19 -> "50 to 70°F"
                in 0..9 -> "30 to 50°F"
                in -99..-1 -> "30°F and less"
                else -> ""
            }
        }

        sharedPrefs.edit()
            .putString("objective", " $subTextAdd")
            .apply()
        subTextAdd = getString(R.string.add_ths) + " $subTextAdd"
        if (!sharedPrefs.getBoolean("userDidExperienceApp", false))
            v.findViewById<TextView>(R.id.once_your).text = getString(R.string.hey)
        v.findViewById<TextView>(R.id.subText).text = subTextAdd
        explanation.visibility = View.VISIBLE
        explanation2.visibility = View.VISIBLE
    }

    //Retourne les variables des 3 prochaines temp et de la + haute vitesse du vent
    //IMPORTANT : la température est gérée ici en degrés Celsius pour utiliser le même code !!
    private fun getWeatherVariables(
        latitude: Double,
        longitude: Double,
        weatherRetriever: RetrieveWeather
    ) {
        val temperatures = mutableListOf<Int>()
        val winds = mutableListOf<Int>()
        val weathers = mutableListOf<String>()
        var data: WeatherPrediction
        val sharedPrefs = v.context.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)

        val helper = OpenWeatherMapHelper()
        helper.setApiKey("c08c7c9f9e80827d2b3b3b818a77c856")
        //helper.setUnits(Units.METRIC)
        helper.setLang(sharedPrefs.getString("lang", null))
        helper.getThreeHourForecastByGeoCoordinates(
            latitude,
            longitude,
            object : OpenWeatherMapHelper.ThreeHourForecastCallback {
                override fun onSuccess(response: ThreeHourForecast) {
                    //Récupération des 4 prochaines instances de météo
                    for (i in 0..2) {
                        //On multiplie index par 2 pour avoir la bonne heure
                        val temp =
                            Math.round(TempUnitConverter.convertToCelsius(response.threeHourWeatherArray[i * 2].main.temp.toString()))
                        val w = Math.round(response.threeHourWeatherArray[i * 2].wind.speed)
                        val we = response.threeHourWeatherArray[i * 2].weatherArray[0].main
                        temperatures.add(temp.toInt())
                        winds.add(w.toInt())
                        weathers.add(we)
                    }
                    val targetTemperatures =
                        listOf(temperatures[0], temperatures[1], temperatures[2])
                    val highestWindSpeed = winds.max()
                    data = WeatherPrediction(targetTemperatures, weathers, highestWindSpeed!!)
                    weatherRetriever.onWeatherReceived(data)
                }

                override fun onFailure(message: Throwable) {
                    Toast.makeText(context, message.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    companion object {


        fun newInstance(cityLatitude: Double, cityLongitude:Double): DressFragment {

            val f = DressFragment()
            val b = Bundle()

            b.putDouble("LONGITUDE", cityLongitude)
            b.putDouble("LATITUDE", cityLatitude)

            f.arguments = b

            return f
        }
    }
}
