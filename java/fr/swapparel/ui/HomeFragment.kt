package fr.swapparel.ui

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView
import com.kwabenaberko.openweathermaplib.implementation.OpenWeatherMapHelper
import com.kwabenaberko.openweathermaplib.models.currentweather.CurrentWeather
import com.kwabenaberko.openweathermaplib.models.threehourforecast.ThreeHourForecast
import fr.swapparel.R
import fr.swapparel.extensions.TempUnitConverter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_home.view.textView5
import net.hockeyapp.android.metrics.MetricsManager
import spencerstudios.com.bungeelib.Bungee
import java.text.DateFormat
import java.util.*
import kotlin.system.measureTimeMillis


class HomeFragment : androidx.fragment.app.Fragment() {
    private lateinit var v: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_home, container, false)
        this.v = v
        val time = measureTimeMillis {
            setupScreen()

        //Il n'y a pas d'adresse
        if (arguments == null) {
            val intent = Intent(context!!, CitiesActivity::class.java)
            startActivity(intent)
            Bungee.slideDown(context!!)
        } else { //L'adresse est demandée par MainActivity

            val cityName = arguments!!.getString("NAME")
            val cityLongitude = arguments!!.getDouble("LONGITUDE")
            val cityLatitude = arguments!!.getDouble("LATITUDE")
            v.textView5.text = cityName!!.toUpperCase()

            try {
                 getCurrentWeather(cityLatitude, cityLongitude, v)
                /*GlobalScope.launch {*/ getDayWeather(cityLatitude, cityLongitude, v) //}


            } catch (e: Exception) {

            }
            }
        }
        Log.e("SWP", "Fin HomeFragment $time")

        return v
    }

    //Initialise les éléments à l'écran
    private fun setupScreen() {
        //Supprime le background qui ne sert qu'au design
        val background = v.findViewById<ConstraintLayout>(R.id.background)
        background.setBackgroundResource(0)
        background.visibility = View.INVISIBLE

        val jour = v.findViewById<TextView>(R.id.textView4)
        val df = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault())
        val formattedCurrentDate = df.format(Date())
        jour.text = formattedCurrentDate.toUpperCase()

        v.optionCity.setOnClickListener {
            val intent = Intent(context!!, CitiesActivity::class.java)
            startActivity(intent)
            Bungee.slideDown(context!!)
        }
    }

    //Obtient et gère le titre du menu avec la météo
    private fun getCurrentWeather(latitude: Double, longitude: Double, v: View) {
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
                    MetricsManager.trackEvent("GOT_WEATHER")
                    val texteMeteo = v.findViewById<TextView>(R.id.textView3)
                    val currentWeatherState = response.weatherArray[0].description.toUpperCase()
                    texteMeteo.text = currentWeatherState

                    val temp = response.main.temp
                    val texteTemp = v.findViewById<TextView>(R.id.textView)

                    if (sharedPrefs.getString("unit", null) == "C") {
                        texteTemp.text = convertToCelsius(temp.toString())
                    } else {
                        texteTemp.text = convertToFahrenheit(temp.toString())
                    }

                    val imageMatin = v.findViewById<ImageView>(R.id.imageView3)
                    var weatherMatin = response.weatherArray[0].icon
                    val heureActuelle = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

                    if (weatherMatin == "01n" && (heureActuelle >= 22 || heureActuelle <= 6)) weatherMatin =
                        "sleepyboi"
                    when (weatherMatin) {
                        "01d" -> imageMatin.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity!!.applicationContext,
                                R.drawable.ic_sun
                            )
                        )
                        "02d" -> imageMatin.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity!!.applicationContext,
                                R.drawable.ic_eclaircie
                            )
                        )
                        "03d", "04d" -> imageMatin.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity!!.applicationContext,
                                R.drawable.ic_cloudy
                            )
                        )
                        "09d", "10d", "09n", "10n" -> imageMatin.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity!!.applicationContext,
                                R.drawable.ic_rain
                            )
                        )
                        "11d", "11n" -> imageMatin.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity!!.applicationContext,
                                R.drawable.ic_storm
                            )
                        )
                        "13d" -> imageMatin.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity!!.applicationContext,
                                R.drawable.ic_snowflake
                            )
                        )
                        "13n" -> imageMatin.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity!!.applicationContext,
                                R.drawable.ic_snow
                            )
                        )
                        "01n" -> imageMatin.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity!!.applicationContext,
                                R.drawable.ic_moon
                            )
                        )
                        "02n", "03n", "04n" -> imageMatin.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity!!.applicationContext,
                                R.drawable.ic_cloudy_moon
                            )
                        )
                        "50d", "50n" -> imageMatin.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity!!.applicationContext,
                                R.drawable.ic_haze
                            )
                        )
                        else -> { //This case is for sleepyboi
                            imageMatin.visibility = View.INVISIBLE
                            val lottie = v.findViewById<LottieAnimationView>(R.id.animation_view2)
                            lottie.playAnimation()
                            lottie.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onFailure(message: Throwable) {
                    Toast.makeText(context, message.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun convertToCelsius(temp: String): String {
        return Math.round(TempUnitConverter.convertToCelsius(temp)).toString() + "°"
    }

    fun convertToFahrenheit(temp: String): String {
        return Math.round(TempUnitConverter.convertToFahrenheit(temp)).toString() + "°"
    }

    //Gère les icônes de météo pour les prévisions de la journée
    private fun getDayWeather(latitude: Double, longitude: Double, v: View) {
        val errorObject = v.findViewById<LottieAnimationView>(R.id.errorview)
        val sharedPrefs =
            activity!!.applicationContext.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)

        val helper = OpenWeatherMapHelper()
        helper.setApiKey("c08c7c9f9e80827d2b3b3b818a77c856")
        helper.setLang(sharedPrefs.getString("lang", null))
        helper.getThreeHourForecastByGeoCoordinates(
            latitude,
            longitude,
            object : OpenWeatherMapHelper.ThreeHourForecastCallback {
                override fun onSuccess(response: ThreeHourForecast) {
                    //In case the user just recovered his connection
                    v.findViewById<TextView>(R.id.textView7).visibility = View.INVISIBLE
                    errorObject.visibility = View.INVISIBLE

                    var weatherMatin: String
                    var weatherIcon = v.findViewById<ImageView>(R.id.imageView4)
                    var temperature = v.findViewById<TextView>(R.id.textView9)

                    val heureArrondie = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                        in 2..4 -> 5
                        in 5..7 -> 8
                        in 8..10 -> 11
                        in 11..13 -> 14
                        in 14..16 -> 17
                        in 17..19 -> 20
                        in 20..22 -> 23
                        else -> 2
                    }

                    //Récupération des 4 prochaines instances de météo
                    for (i in 0..3) {
                        weatherMatin = response.threeHourWeatherArray[i * 2].weatherArray[0].icon
                        when (i) {
                            0 -> weatherIcon = v.findViewById(R.id.imageView4)
                            1 -> weatherIcon = v.findViewById(R.id.imageView5)
                            2 -> weatherIcon = v.findViewById(R.id.imageView6)
                            3 -> weatherIcon = v.findViewById(R.id.imageView7)
                        }

                        when (weatherMatin) {
                            "01d" -> weatherIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    activity!!.applicationContext,
                                    R.drawable.ic_sun
                                )
                            )
                            "02d" -> weatherIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    activity!!.applicationContext,
                                    R.drawable.ic_eclaircie
                                )
                            )
                            "03d", "04d" -> weatherIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    activity!!.applicationContext,
                                    R.drawable.ic_cloudy
                                )
                            )
                            "09d", "10d", "09n", "10n" -> weatherIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    activity!!.applicationContext,
                                    R.drawable.ic_rain
                                )
                            )
                            "11d", "11n" -> weatherIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    activity!!.applicationContext,
                                    R.drawable.ic_storm
                                )
                            )
                            "13d" -> weatherIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    activity!!.applicationContext,
                                    R.drawable.ic_snowflake
                                )
                            )
                            "13n" -> weatherIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    activity!!.applicationContext,
                                    R.drawable.ic_snow
                                )
                            )
                            "01n" -> weatherIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    activity!!.applicationContext,
                                    R.drawable.ic_moon
                                )
                            )
                            "02n", "03n", "04n" -> weatherIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    activity!!.applicationContext,
                                    R.drawable.ic_cloudy_moon
                                )
                            )
                            "50d", "50n" -> weatherIcon.setImageDrawable(
                                ContextCompat.getDrawable(
                                    activity!!.applicationContext,
                                    R.drawable.ic_haze
                                )
                            )
                            else -> {
                                weatherIcon.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        activity!!.applicationContext,
                                        R.drawable.ic_city
                                    )
                                )
                            }
                        }

                        when (i) {
                            0 -> temperature = v.findViewById(R.id.textView9)
                            1 -> temperature = v.findViewById(R.id.textView11)
                            2 -> temperature = v.findViewById(R.id.textView13)
                            3 -> temperature = v.findViewById(R.id.textView15)
                        }

                        //On multiplie index par 2 pour avoir la bonne heure
                        val temp = response.threeHourWeatherArray[i * 2].main.temp

                        if (sharedPrefs.getString("unit", null) == "C") {
                            temperature.text = convertToCelsius(temp.toString())
                        } else {
                            temperature.text = convertToFahrenheit(temp.toString())
                        }

                        when (heureArrondie) {
                            in 0..5, in 22..24 -> {
                                v.findViewById<TextView>(R.id.textView8).text =
                                    getString(R.string.night)
                                v.findViewById<TextView>(R.id.textView10).text =
                                    getString(R.string.morning)
                                v.findViewById<TextView>(R.id.textView12).text =
                                    getString(R.string.afternoon)
                                v.findViewById<TextView>(R.id.textView14).text =
                                    getString(R.string.evening)
                            }
                            in 6..11 -> {
                                v.findViewById<TextView>(R.id.textView8).text =
                                    getString(R.string.morning)
                                v.findViewById<TextView>(R.id.textView10).text =
                                    getString(R.string.afternoon)
                                v.findViewById<TextView>(R.id.textView12).text =
                                    getString(R.string.evening)
                                v.findViewById<TextView>(R.id.textView14).text =
                                    getString(R.string.night)
                            }
                            in 12..17 -> {
                                v.findViewById<TextView>(R.id.textView8).text =
                                    getString(R.string.afternoon)
                                v.findViewById<TextView>(R.id.textView10).text =
                                    getString(R.string.evening)
                                v.findViewById<TextView>(R.id.textView12).text =
                                    getString(R.string.night)
                                v.findViewById<TextView>(R.id.textView14).text =
                                    getString(R.string.morning)
                            }
                            in 18..21 -> {
                                v.findViewById<TextView>(R.id.textView8).text =
                                    getString(R.string.evening)
                                v.findViewById<TextView>(R.id.textView10).text =
                                    getString(R.string.night)
                                v.findViewById<TextView>(R.id.textView12).text =
                                    getString(R.string.morning)
                                v.findViewById<TextView>(R.id.textView14).text =
                                    getString(R.string.afternoon)
                            }
                        }

                        val content = v.findViewById<ConstraintLayout>(R.id.background)
                        content.startAnimation(
                            AnimationUtils.loadAnimation(
                                context,
                                android.R.anim.fade_in
                            )
                        )
                        content.visibility = View.VISIBLE
                    }
                }

                override fun onFailure(message: Throwable) {
                    errorObject.visibility = View.VISIBLE
                    errorObject.playAnimation()
                    v.findViewById<TextView>(R.id.textView7).visibility = View.VISIBLE
                    Toast.makeText(context, message.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    companion object {
        //Récupère les données du Bundle passé par MainActivity
        fun newInstance(
            cityName: String,
            cityLatitude: Double,
            cityLongitude: Double
        ): HomeFragment {

            val f = HomeFragment()
            val b = Bundle()
            b.putString("NAME", cityName)
            b.putDouble("LONGITUDE", cityLongitude)
            b.putDouble("LATITUDE", cityLatitude)

            f.arguments = b

            return f
        }
    }
}