package fr.swapparel.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.airbnb.lottie.LottieAnimationView
import com.bartoszlipinski.recyclerviewheader2.RecyclerViewHeader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import fr.swapparel.R
import fr.swapparel.data.*
import kotlinx.android.synthetic.main.activity_dressinglist.*
import libs.mjn.prettydialog.PrettyDialog
import spencerstudios.com.bungeelib.Bungee


class DressingListActivity : AppCompatActivity() {
    private var mDb: SavedApparelDB? = null
    private lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreenCall()
        setContentView(R.layout.activity_dressinglist)

        val sharedPrefs = this.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        //If is pro user
        if (sharedPrefs.getBoolean("proUser", false)) {
            mAdView.visibility = View.INVISIBLE

            //Move custom fab to the bottom
            val cs = ConstraintSet()
            val layout = findViewById<View>(R.id.root) as ConstraintLayout
            cs.clone(layout)
            cs.setVerticalBias(R.id.custom_fab, 1.0f)
            cs.applyTo(layout)
        }

        mDb = SavedApparelDB.getInstance(this)

        //Setup the recyclerview and fastadapter
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyler)
        val fastAdapter = FastItemAdapter<Apparel>()
        fastAdapter.setHasStableIds(true)
        recyclerView.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)
        recyclerView.adapter = fastAdapter

        //Setup the header
        val header = findViewById<View>(R.id.header) as RecyclerViewHeader
        header.attachTo(recyclerView)

        //Load the different apparels from user database
        LoadFromDatabase().loadApparel(this, ApparelLoader { mangoes ->
            //The list is well populated
            if (mangoes != null && mangoes.isNotEmpty()) {
                fastAdapter.add(mangoes)
                fastAdapter.withSelectable(true)
                val floatingActionButton = findViewById<View>(R.id.custom_fab)
                floatingActionButton.setOnClickListener {
                    val pDialog = PrettyDialog(this)
                    pDialog
                        .setTitle(getString(R.string.what_to_add))
                        .setIcon(R.drawable.ic_add_dialog)
                        .setAnimationEnabled(true)
                        .addButton(
                            getString(R.string.clothes),
                            R.color.hockeyapp_text_white,
                            R.color.clothes_button
                        ) {
                            pDialog.dismiss()
                            val intent = Intent(this, AddActivity::class.java)
                            startActivity(intent)
                            Bungee.slideLeft(this)
                        }
                        .addButton(
                            getString(R.string.shoes_translatable),
                            R.color.hockeyapp_text_white,
                            R.color.shoes_button
                        ) {
                            pDialog.dismiss()
                            val intent = Intent(this, AddShoesActivity::class.java)
                            startActivity(intent)
                            Bungee.slideLeft(this)
                        }
                        .show()
                }

                //If >30, don't show the annoying notification
                if(mangoes.size >= 30)
                {
                    objective.visibility = View.GONE
                }
                else
                {
                    val subTextHeader = findViewById<TextView>(R.id.subText)
                    val obj = Math.round((mangoes.size - 1).toDouble() / 10) * 10
                    subTextHeader.text =
                        "${getString(R.string.the_more_you_add)} $obj ${getString(R.string.end_more)}"
                }

                fastAdapter.withOnClickListener { v, adapter, item, position ->
                    val intent = Intent(this, SingleApparelActivity::class.java)
                    intent.putExtra("serialize_data", item)
                    startActivity(intent)
                    Bungee.slideLeft(this)
                    true
                }

                //If the user long presses, ask if he wants to delete the current item.
                fastAdapter.withOnPreLongClickListener { v, adapter, item, position ->
                    val pDialog = PrettyDialog(this)
                    pDialog
                        .setTitle(getString(R.string.delete))
                        .setMessage(getString(R.string.are_you_sure))
                        .setIcon(R.drawable.launcher)
                        .addButton(
                            getString(R.string.delete_caps),
                            R.color.hockeyapp_text_white,
                            R.color.pdlg_color_green
                        ) {
                            LoadFromDatabase()
                                .loadRawData(this,
                                    ApparelRawDataLoader { mangoes ->
                                        deleteThisNephew(mangoes[position])
                                        startActivity(
                                            Intent(
                                                this,
                                                DressingListActivity::class.java
                                            )
                                        )
                                        finish()
                                    })
                        }
                        .addButton(
                            getString(R.string.cancel),
                            R.color.hockeyapp_text_white,
                            R.color.pdlg_color_red
                        ) { pDialog.dismiss() }
                    pDialog.show()
                    true
                }
            } else { //No data, so we place an empty state
                logo404.playAnimation()
                findViewById<ConstraintLayout>(R.id.root).visibility = View.INVISIBLE
                findViewById<ConstraintLayout>(R.id.emptyState).visibility = View.VISIBLE
                findViewById<View>(R.id.addbtn).setOnClickListener {
                    val intent = Intent(this, AddActivity::class.java)
                    startActivity(intent)
                    Bungee.slideLeft(this)
                    finish()
                }
            }
        })
    }

    private fun deleteThisNephew(nephew: SavedApparel) //https://knowyourmeme.com/photos/1231838-delete-this-nephew
    {
        val handlerThread = HandlerThread("swappa")
        handlerThread.start()
        val looper = handlerThread.looper
        val handler = Handler(looper)
        val task = Runnable { mDb?.apparelDataDao()?.delete(nephew) }
        handler.post(task)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        SavedApparelDB.destroyInstance()
        super.onDestroy()
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
