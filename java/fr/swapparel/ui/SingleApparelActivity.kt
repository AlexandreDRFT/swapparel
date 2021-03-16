package fr.swapparel.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.mikhaellopez.circularimageview.CircularImageView
import fr.swapparel.R
import fr.swapparel.data.Apparel
import fr.swapparel.data.ApparelLoader
import fr.swapparel.data.LoadFromDatabase
import fr.swapparel.extensions.*
import fr.swapparel.temp.MigrationApparel
import info.hoang8f.widget.FButton
import spencerstudios.com.bungeelib.Bungee
import java.util.*

class SingleApparelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreenCall()
        setContentView(R.layout.activity_single_apparel)

        setup()
    }

    private fun setup() {
        val apparel = intent.getSerializableExtra("serialize_data") as Apparel
        val img = findViewById<CircularImageView>(R.id.image)
        Glide.with(this)
            .load(
                ImageLoader(this).setFileName(
                    apparel.drawablePath
                ).setDirectoryName("Snapparel").filePath
            )
            .into(img)
        img.setBorderColor(Color.parseColor(apparel.color))

        findViewById<FButton>(R.id.edit_button).setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("serialize_data", apparel)
            startActivity(intent)
            Bungee.slideLeft(this)
        }

        findViewById<FButton>(R.id.type).text = when (apparel.type) {
            getString(R.string.top) -> getString(R.string.top_edit)
            getString(R.string.pants) -> getString(R.string.pants_edit)
            getString(R.string.combi) -> getString(R.string.combi_edit)
            getString(R.string.shoes) -> getString(R.string.shoes_translatable)
            else -> getString(R.string.top_edit)
        }

        if (apparel.type != getString(R.string.shoes)) { //If it's not a shoe
            val sharedPrefs =
                applicationContext.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)
            val m = MigrationApparel()
            if (sharedPrefs.getString("unit", null) == "C") {
                findViewById<FButton>(R.id.temp).text =
                        when (m.getMaxTempFromHeaviness(apparel.heaviness)) {
                            0 -> "30°C+"
                            1 -> "20 - 30°C"
                            2 -> "10 - 20°C"
                            3 -> "0 - 10°C"
                            4 -> getString(R.string.zero_and_less)
                            else -> getString(R.string.top_edit)
                        }
            } else {
                findViewById<FButton>(R.id.temp).text =
                        when (m.getMaxTempFromHeaviness(apparel.heaviness)) {
                            0 -> "90°F+"
                            1 -> "70 to 90°F"
                            2 -> "50 to 70°F"
                            3 -> "30 to 50°F"
                            4 -> "30°F and less"
                            else -> "30°F and less"
                        }
            }
        } else {
            findViewById<FButton>(R.id.temp).visibility = View.INVISIBLE
            findViewById<FButton>(R.id.edit_button).visibility = View.INVISIBLE
        }

        //TODO créer une version pour les nombres impairs
        val colorHolders = listOf(
            R.id.color4,
            R.id.color3,
            R.id.color5,
            R.id.color2,
            R.id.color6,
            R.id.color1,
            R.id.color7
        )
        colorHolders.forEach { findViewById<ImageView>(it).visibility = View.INVISIBLE }
        var i = 0
        val myRunnable = Runnable {
            while (i < apparel.complementaryColors.size - 1 && i < colorHolders.size - 1) {
                val c = findViewById<ImageView>(colorHolders[i])
                c.visibility = View.VISIBLE
                c.setColorFilter(Color.parseColor(apparel.complementaryColors[i]))
                i++
            }
        }
        Thread(myRunnable).start()

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
            Bungee.slideRight(this)
        }

        val r = Random().nextInt(16)
        val background = findViewById<ConstraintLayout>(R.id.cstr)
        when (r) {
            1 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle7)
            2 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle8)
            3 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle9)
            4 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle10)
            5 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle11)
            6 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle12)
            7 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle13)
            8 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle14)
            9 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle15)
            10 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle16)
            11 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle17)
            12 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle18)
            13 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle19)
            14 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle21)
            15 -> background.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.rectangle22)
            else -> {
                background.background =
                        ContextCompat.getDrawable(applicationContext, R.drawable.rectangle_nuit)
            }
        }
        if (apparel.type != "Shoes") {
            generatePerfectMatches(apparel)
        } else {
            findViewById<TextView>(R.id.compl2).visibility = View.INVISIBLE
            findViewById<HorizontalScrollView>(R.id.scroller).visibility = View.INVISIBLE
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Bungee.slideRight(this)
    }

    private fun generatePerfectMatches(apparel: Apparel) {
        //This boolean helps me know if there are enough pants or tops to make recommandations, because this code is a mess
        //TODO rewrite this code
        var isProcessViable = true
        val m = MigrationApparel()

        LoadFromDatabase().loadApparel(this, ApparelLoader { fringues ->
            var choosen = listOf<Apparel>()

            if (fringues != null && !fringues.isEmpty() && fringues.size >= 10) {
                when {
                    apparel.type == getString(R.string.top) -> {

                            val resultPants = mutableListOf<Apparel>()
                            val complementaryColors = ColorMatching.matchPinterest(apparel.color)
                            fringues.forEach {
                                try {
                                if (it.type == getString(R.string.pants) && (m.hasCollisionToHeaviness(
                                        apparel.heaviness,
                                        it.heaviness
                                    ) || m.hasCollisionToHeaviness(
                                        apparel.heaviness + 1,
                                        it.heaviness
                                    ) || m.hasCollisionToHeaviness(
                                        apparel.heaviness - 1,
                                        it.heaviness
                                    ))
                                ) {
                                    it.lowestScoreMatching = 100000000.0
                                    complementaryColors.forEach { col ->
                                        val z =
                                            ColorMatching.calculateSimilarity(it.color, col)
                                                .toDouble()
                                        if (z < it.lowestScoreMatching)
                                            it.lowestScoreMatching = z
                                    }
                                    resultPants.add(it)
                                }
                            } catch(e: Exception)
                                {
                                    //Pass
                                }
                        }
                        //Pour chaque pants/top on prend le score le + faible de similarity avec une complementaryColor
                        //On assigne à chaque pants/top ce score
                        //On compare la liste par score
                        if (resultPants.size >= 4) {
                            val resultPantsSorted =
                                resultPants.sortedWith(compareBy { it.lowestScoreMatching })
                            choosen = listOf(
                                resultPantsSorted[0],
                                resultPantsSorted[1],
                                resultPantsSorted[2],
                                resultPantsSorted[3]
                            )
                        } else {
                            isProcessViable = false
                        }
                    }
                    apparel.type == getString(R.string.pants) -> {
                        val resultTops = mutableListOf<Apparel>()
                        val complementaryColors = ColorMatching.matchPinterest(apparel.color)
                        fringues.forEach {
                            if (it.type == getString(
                                    R.string.top
                                ) && (m.hasCollisionToHeaviness(
                                    apparel.heaviness,
                                    it.heaviness
                                ) || m.hasCollisionToHeaviness(
                                    apparel.heaviness + 1,
                                    it.heaviness
                                ) || m.hasCollisionToHeaviness(
                                    apparel.heaviness - 1,
                                    it.heaviness
                                ))
                            ) {
                                it.lowestScoreMatching = 100000000.0
                                complementaryColors.forEach { col ->
                                    val z =
                                        ColorMatching.calculateSimilarity(it.color, col).toDouble()
                                    if (z < it.lowestScoreMatching)
                                        it.lowestScoreMatching = z
                                }
                                resultTops.add(it)
                            }
                        }
                        //Pour chaque pants/top on prend le score le + faible de similarity avec une complementaryColor
                        //On assigne à chaque pants/top ce score
                        //On compare la liste par score
                        if (!resultTops.isEmpty() && resultTops.size > 3) {
                            val resultTopsSorted =
                                resultTops.sortedWith(compareBy { it.lowestScoreMatching })
                            choosen = listOf(
                                resultTopsSorted[0],
                                resultTopsSorted[1],
                                resultTopsSorted[2],
                                resultTopsSorted[3]
                            )
                        } else {
                            isProcessViable = false
                        }
                    }
                    else -> {
                        findViewById<TextView>(R.id.compl2).visibility = View.INVISIBLE
                        findViewById<HorizontalScrollView>(R.id.scroller).visibility =
                            View.INVISIBLE
                    }
                }
                if (isProcessViable && choosen.isNotEmpty()) {
                    val scores = listOf(R.id.score, R.id.score2, R.id.score3, R.id.score4)
                    val a = listOf(
                        R.id.image_imbr,
                        R.id.image_imbr2,
                        R.id.image_imbr3,
                        R.id.image_imbr4
                    )
                    val surroundingCards =
                        listOf(R.id.list_item, R.id.list_item2, R.id.list_item3, R.id.list_item4)

                    for (i in 0..(a.size - 1)) {
                        val view = findViewById<CircularImageView>(a[i])
                        view.setBorderColor(Color.parseColor(choosen[i].color))
                        Glide.with(this)
                            .load(
                                ImageLoader(this).setFileName(
                                    choosen[i].drawablePath
                                ).setDirectoryName("Snapparel").filePath
                            )
                            .into(view)
                        Log.d("SWP", "Score: " + choosen[i].lowestScoreMatching)
                        if (choosen[i].lowestScoreMatching <= 1500) {
                            findViewById<TextView>(scores[i]).text = "100%"
                        } else {
                            val sc =
                                Math.round(160 - 0.04 * choosen[i].lowestScoreMatching).toString() + "%"
                            findViewById<TextView>(scores[i]).text = sc
                        }

                        val card = findViewById<CardView>(surroundingCards[i])
                        card.setOnClickListener {
                            val intent = Intent(this, SingleApparelActivity::class.java)
                            intent.putExtra("serialize_data", choosen[i])
                            startActivity(intent)
                            Bungee.zoom(this)
                            finish()
                        }
                    }
                } else {
                    //Not enough pants or shirts, display appropriate message
                    val d = findViewById<TextView>(R.id.errortxt)
                    d.visibility = View.VISIBLE
                    val targetWeather = when (m.getMaxTempFromHeaviness(apparel.heaviness)) {
                        0 -> "(30°C +)"
                        1 -> "(20-30°C)"
                        2 -> "(10-20°C)"
                        3 -> "(0-10°C)"
                        4 -> "(- 0°C)"
                        else -> ""
                    }

                    //Get the opposite of the current apparel
                    var targetClothes = getString(R.string.top)
                    if (apparel.type == getString(R.string.top)) {
                        targetClothes = getString(R.string.pants)
                    }

                    d.text = getString(R.string.not_enough_pairs) + " $targetClothes " +
                            targetWeather
                    findViewById<HorizontalScrollView>(R.id.scroller).visibility = View.INVISIBLE
                }
            } else { //Not enough clothes, add at least 10, user!
                val d = findViewById<TextView>(R.id.errortxt)
                d.visibility = View.VISIBLE
                d.text = getString(R.string.not_enough_clothes)
                findViewById<HorizontalScrollView>(R.id.scroller).visibility = View.INVISIBLE
            }
        })
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
