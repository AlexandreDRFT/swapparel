package fr.swapparel.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mikhaellopez.circularimageview.CircularImageView
import fr.swapparel.R
import fr.swapparel.data.Apparel
import fr.swapparel.data.SavedApparelDB
import fr.swapparel.data.ApparelRawDataLoader
import fr.swapparel.data.LoadFromDatabase
import fr.swapparel.data.SavedApparel
import fr.swapparel.extensions.*
import fr.swapparel.temp.MigrationApparel
import info.hoang8f.widget.FButton
import kotlinx.android.synthetic.main.activity_edit.*
import spencerstudios.com.bungeelib.Bungee
import top.defaults.colorpicker.ColorPickerPopup

class EditActivity : AppCompatActivity() {
    private var idButtonSelectedType = 0
    private var idSelectedTemperatures = mutableListOf<Int>()
    private var isPhotoSelected = true
    private var photoFilePath = ""
    var color = 0
    private var mDb: SavedApparelDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        fullScreenCall()
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
        photoFilePath = apparel.drawablePath

        val migrator = MigrationApparel()

        val buttonList = listOf(over30_b, from20to30_b, from10to20_b, from0to10_b, under0_b)
        //Test pour le crash du -
        try {
            for (i in migrator.getMaxTempFromHeaviness(apparel.heaviness)..migrator.getMinTempFromHeaviness(apparel.heaviness)) {
                UIFunctions().makeBlue(buttonList[i])
                idSelectedTemperatures.add(i)
            }
        } catch(e: Exception) {
            for(i in 0..0)
            {
                UIFunctions().makeBlue(buttonList[i])
                idSelectedTemperatures.add(i)
            }
        }



        when (apparel.type) {
            getString(R.string.top) -> UIFunctions().makeBlue(top_b)
            getString(R.string.pants) -> UIFunctions().makeBlue(pants_b)
            getString(R.string.combi) -> UIFunctions().makeBlue(combi_b)
        }

        idButtonSelectedType = when (apparel.type) {
            getString(R.string.top) -> 1
            getString(R.string.pants) -> 2
            getString(R.string.combi) -> 3
            else ->
                0
        }

        val sentientText = findViewById<TextView>(R.id.explanation)
        sentientText.text = getString(R.string.touch_me_senpai)
        val fleche = findViewById<ImageView>(R.id.lookatthis_arrow)
        fleche.setImageResource(R.drawable.ic_leftarrowadd)
        fleche.rotation = 0f
        val blinker = findViewById<FButton>(R.id.colorBlinker)
        blinker.buttonColor = Color.parseColor(apparel.color)
        blinker.visibility = View.VISIBLE
        val extractedPhoto =
            ImageLoader(this).setFileName(apparel.drawablePath).setDirectoryName("Snapparel").load()
        blinker.setOnClickListener {
            showMyDialog(extractedPhoto)
        }

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
            Bungee.slideRight(this)
        }
    }

    fun onClickTop(v: View) {
        idButtonSelectedType = 1
        top_b.buttonColor = Color.parseColor("#19B5FE")
        pants_b.buttonColor = Color.parseColor("#ECECEC")
        combi_b.buttonColor = Color.parseColor("#ECECEC")
    }

    fun onClickPants(v: View) {
        idButtonSelectedType = 2
        top_b.buttonColor = Color.parseColor("#ECECEC")
        pants_b.buttonColor = Color.parseColor("#19B5FE")
        combi_b.buttonColor = Color.parseColor("#ECECEC")
    }

    fun onClickCombi(v: View) {
        idButtonSelectedType = 3
        top_b.buttonColor = Color.parseColor("#ECECEC")
        pants_b.buttonColor = Color.parseColor("#ECECEC")
        combi_b.buttonColor = Color.parseColor("#19B5FE")
    }

    fun updateThisApparel(v: View) {
        if (idButtonSelectedType == 0) {
            Toast.makeText(this, getString(R.string.choose_type), Toast.LENGTH_LONG).show()
        } else if (idSelectedTemperatures.isEmpty()) {
            Toast.makeText(this, getString(R.string.choose_weather), Toast.LENGTH_LONG).show()
        } else if (!isPhotoSelected) {
            Toast.makeText(this, getString(R.string.choose_photo), Toast.LENGTH_LONG).show()
        } else {
            val apparel = intent.getSerializableExtra("serialize_data") as Apparel

            LoadFromDatabase().loadRawData(this, ApparelRawDataLoader { mangoes ->
                //Props to my algorithmic teacher for this useful loop
                var index = 0
                var found = false
                while (!found && index < mangoes.size) {
                    found = mangoes[index].drawablePath == apparel.drawablePath
                    if (!found)
                        index++
                }
                if (found) {
                    val addedApparel = mangoes[index]
                    addedApparel.drawablePath = photoFilePath
                    val sorted = idSelectedTemperatures.sortedBy { it }
                    //Je prends la maxTemp et la minTemp pour en faire ma heaviness
                    addedApparel.heaviness =
                        MigrationApparel().convertOptimumsToHeaviness(sorted[0], sorted.lastIndex)
                    addedApparel.type = when (idButtonSelectedType) {
                        1 -> getString(R.string.top)
                        2 -> getString(R.string.pants)
                        3 -> getString(R.string.combi)
                        else -> "Top"
                    }
                    addedApparel.color = String.format("#%06X", 0xFFFFFF and color)
                    LoadFromDatabase().update(addedApparel, this)
                    Toast.makeText(this, getString(R.string.new_element), Toast.LENGTH_LONG).show()
                    val intent = Intent(this, DressingListActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            })
        }
    }

    private fun insertApparelDataInDb(apparel: SavedApparel) {
        val handlerThread = HandlerThread("snappaThreadAdder")
        handlerThread.start()
        val looper = handlerThread.looper
        val handler = Handler(looper)
        val task = Runnable { mDb?.apparelDataDao()?.insert(apparel) }
        handler.post(task)
    }

    fun onClickOver30(v: View) {
        val b = over30_b
        if (!idSelectedTemperatures.contains(0)) {
            idSelectedTemperatures.add(0)
            b.buttonColor = Color.parseColor("#19B5FE")
        } else {
            idSelectedTemperatures.remove(0)
            b.buttonColor = Color.parseColor("#ECECEC")
        }
    }

    fun onClickFrom20to30(v: View) {
        val b = from20to30_b
        if (!idSelectedTemperatures.contains(1)) {
            idSelectedTemperatures.add(1)
            b.buttonColor = Color.parseColor("#19B5FE")
        } else {
            idSelectedTemperatures.remove(1)
            b.buttonColor = Color.parseColor("#ECECEC")
        }
    }

    fun onClickFrom10to20(v: View) {
        val b = from10to20_b
        if (!idSelectedTemperatures.contains(2)) {
            idSelectedTemperatures.add(2)
            b.buttonColor = Color.parseColor("#19B5FE")
        } else {
            idSelectedTemperatures.remove(2)
            b.buttonColor = Color.parseColor("#ECECEC")
        }
    }

    fun onClickFrom0to10(v: View) {
        val b = from0to10_b
        if (!idSelectedTemperatures.contains(3)) {
            idSelectedTemperatures.add(3)
            b.buttonColor = Color.parseColor("#19B5FE")
        } else {
            idSelectedTemperatures.remove(3)
            b.buttonColor = Color.parseColor("#ECECEC")
        }
    }

    fun onClickUnder0(v: View) {
        val b = under0_b
        if (!idSelectedTemperatures.contains(4)) {
            idSelectedTemperatures.add(4)
            b.buttonColor = Color.parseColor("#19B5FE")
        } else {
            idSelectedTemperatures.remove(4)
            b.buttonColor = Color.parseColor("#ECECEC")
        }
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

    //Shows a dialog, asking the user to pick the dominant color of the garment.
    private fun showMyDialog(bitmap: Bitmap) {
        val dialog = Dialog(this@EditActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.color_picker_dialog)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        val imageView = dialog.findViewById<ImageView>(R.id.garmentTouchDis)
        imageView.setImageBitmap(bitmap)

        imageView.setOnTouchListener { v, event ->
            val img = v as ImageView

            val evX = event.x.toInt()
            val evY = event.y.toInt()

            img.isDrawingCacheEnabled = true
            val imgbmp = Bitmap.createBitmap(img.drawingCache)
            img.isDrawingCacheEnabled = false

            try {
                val pxl = imgbmp.getPixel(evX, evY)
                this@EditActivity.color = pxl
                dialog.findViewById<TextView>(R.id.textView22).setTextColor(color)

            } catch (ignore: Exception) {
            }

            imgbmp.recycle()
            true
        }

        val b = dialog.findViewById<FButton>(R.id.validate_button)
        b.setOnClickListener {
            val i = findViewById<CircularImageView>(R.id.image)
            findViewById<FButton>(R.id.colorBlinker).buttonColor = color
            i.setBorderColor(color)
            dialog.dismiss()
        }

        val chooseYoself = dialog.findViewById<FButton>(R.id.select_button)
        chooseYoself.setOnClickListener {
            ColorPickerPopup.Builder(this)
                .initialColor(Color.WHITE) // Set initial color
                .enableBrightness(true) // Enable brightness slider or not
                .enableAlpha(false) // Enable alpha slider or not
                .okTitle(getString(R.string.validate))
                .cancelTitle(getString(R.string.cancel))
                .showIndicator(true)
                .showValue(false)
                .build()
                .show(it, object : ColorPickerPopup.ColorPickerObserver {
                    override fun onColorPicked(color: Int) {
                        val i = findViewById<CircularImageView>(R.id.image)
                        findViewById<FButton>(R.id.colorBlinker).buttonColor = color
                        i.setBorderColor(color)
                        dialog.dismiss()
                    }

                    override fun onColor(color: Int, fromUser: Boolean) {

                    }
                })
        }

        //Show the dialog at 90% of the screen
        val displayMetrics = applicationContext.resources.displayMetrics
        val dialogWidth = displayMetrics.widthPixels * 0.95
        val dialogHeight = displayMetrics.heightPixels * 0.95
        dialog.window!!.setLayout(dialogWidth.toInt(), dialogHeight.toInt())

        dialog.show()
    }
}
