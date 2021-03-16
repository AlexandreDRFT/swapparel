package fr.swapparel.ui

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.mikhaellopez.circularimageview.CircularImageView
import com.thanosfisherman.mayi.Mayi
import com.thanosfisherman.mayi.PermissionBean
import com.thanosfisherman.mayi.PermissionToken
import fr.swapparel.R
import fr.swapparel.data.SavedApparelDB
import fr.swapparel.extensions.ColorMatching
import fr.swapparel.extensions.ImageLoader
import fr.swapparel.data.SavedApparel
import info.hoang8f.widget.FButton
import libs.mjn.prettydialog.PrettyDialog
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import spencerstudios.com.bungeelib.Bungee
import top.defaults.colorpicker.ColorPickerPopup
import java.io.File
import java.util.*

class AddShoesActivity : AppCompatActivity() {
    var isPhotoSelected = false
    var photoFilePath = ""
    var color = 0
    private var mDb: SavedApparelDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreenCall()
        setContentView(R.layout.activity_add_shoes)

        val sharedPrefs = this.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)

        val r = Random().nextInt(16)
        val background = findViewById<ConstraintLayout>(R.id.cstr)
        when (r) {
            1 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle7)
            2 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle8)
            3 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle9)
            4 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle10)
            5 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle11)
            6 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle12)
            7 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle13)
            8 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle14)
            9 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle15)
            10 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle16)
            11 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle17)
            12 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle18)
            13 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle19)
            14 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle21)
            15 -> background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle22)
            else -> {
                background.background = ContextCompat.getDrawable(applicationContext, R.drawable.rectangle_nuit)
            }
        }

        //Get thread ready to load database
        mDb = SavedApparelDB.getInstance(this)

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
            Bungee.slideRight(this)
        }

        //User isn't pro, so we can't grant him access :(
        if (!sharedPrefs.getBoolean("proUser", false)) {
            val pDialog = PrettyDialog(this)
            pDialog
                    .setTitle(getString(R.string.reserved))
                    .setMessage(getString(R.string.reserved_long))
                    .setIcon(R.drawable.launcher)
                    .addButton("OK", R.color.hockeyapp_text_white, R.color.fbutton_color_green_sea) {
                        pDialog.dismiss()
                        val intent = Intent(this, DressingListActivity::class.java)
                        startActivity(intent)
                        Bungee.slideRight(this)
                        finish()
                    }
                    .show()
        }

        val imageChooser = findViewById<CircularImageView>(R.id.image)
        imageChooser.setOnClickListener {
            Mayi.withActivity(this)
                    .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                    .onRationale(this::permissionRationaleMulti)
                    .onResult(this::permissionResultMulti)
                    .check()
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

    override fun onDestroy() {
        SavedApparelDB.destroyInstance()
        super.onDestroy()
    }

    //Permission was granted
    private fun permissionResultMulti(permissions: Array<PermissionBean>) {
        EasyImage.configuration(this)
                .setImagesFolderName("Snapparel") // images folder name, default is "EasyImage"
                .setCopyPickedImagesToPublicGalleryAppFolder(true)
                .setCopyTakenPhotosToPublicGalleryAppFolder(true)
        EasyImage.openChooserWithGallery(this, "Choisissez un document !", 0)
    }

    //Permission was not granted
    private fun permissionRationaleMulti(permissions: Array<PermissionBean>, token: PermissionToken) {
        token.continuePermissionRequest()
    }

    fun addThis(v: View) {
        if (!isPhotoSelected) {
            Toast.makeText(this, getString(R.string.choose_photo), Toast.LENGTH_LONG).show()
        } else {
            val addedApparel = SavedApparel()

            addedApparel.drawablePath = photoFilePath
            addedApparel.heaviness = -69
            addedApparel.type = "Shoes"
            addedApparel.color = String.format("#%06X", 0xFFFFFF and color)
            insertApparelDataInDb(addedApparel)
            Toast.makeText(this, getString(R.string.new_shoes), Toast.LENGTH_LONG).show()
            val intent = Intent(this, DressingListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Bungee.slideRight(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {
                //Some error handling
                isPhotoSelected = false
            }

            override fun onImagesPicked(imagesFile: List<File>, source: EasyImage.ImageSource, type: Int) {
                isPhotoSelected = true
                val image = imagesFile[0]
                val d = Drawable.createFromPath(image.path)
                val i = findViewById<CircularImageView>(R.id.image)
                i.setImageDrawable(d)
                val bitmap = (d as BitmapDrawable).bitmap
                //Resize the bitmap to get only the central color
                val resizedBitmap1 = Bitmap.createBitmap(bitmap, bitmap.width / 3, bitmap.height / 3, 2 * bitmap.width / 3, 2 * bitmap.height / 3)
                val palette = androidx.palette.graphics.Palette.from(resizedBitmap1).maximumColorCount(16).generate()
                val colorTemp = palette.getLightMutedColor(0x000000)
                color = colorTemp
                i.setBorderColor(colorTemp)
                val sentientText = findViewById<TextView>(R.id.explanation)
                sentientText.text = getString(R.string.touch_me_senpai)
                val fleche = findViewById<ImageView>(R.id.lookatthis_arrow)
                fleche.setImageResource(R.drawable.ic_leftarrowadd)
                fleche.rotation = 0f
                val blinker = findViewById<FButton>(R.id.colorBlinker)
                blinker.visibility = View.VISIBLE
                blinker.setOnClickListener {
                    showMyDialog(this@AddShoesActivity, bitmap)
                }

                //Save to internal memory
                ImageLoader(this@AddShoesActivity).setFileName(image.name).setDirectoryName("Snapparel").save(bitmap)
                //The filePath is actually the name of the picture, as the new implementation of ImageSaver will handle things well
                photoFilePath = image.name

                //Show the dialog to choose the color
                showMyDialog(this@AddShoesActivity, bitmap)
            }

            override fun onCanceled(source: EasyImage.ImageSource, type: Int) {
                // Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA_IMAGE) {
                    val photoFile = EasyImage.lastlyTakenButCanceledPhoto(applicationContext)
                    photoFile?.delete()
                }
                isPhotoSelected = false
            }
        })
    }

    //Shows a dialog, asking the user to pick the dominant color of the garment.
    private fun showMyDialog(context: Context, bitmap: Bitmap) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.color_picker_dialog)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        dialog.findViewById<TextView>(R.id.textView22).text = getString(R.string.touch_shoe)

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
                this@AddShoesActivity.color = pxl
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

            val complementaryColors = ColorMatching.matchPinterest(String.format("#%06X", 0xFFFFFF and color))
            val colorHolders = listOf(R.id.colorBlinker2, R.id.colorBlinker3, R.id.colorBlinker4, R.id.colorBlinker5, R.id.colorBlinker6)
            var index = 0
            while(index < complementaryColors.size && index < colorHolders.size)
            {
                findViewById<ImageView>(colorHolders[index]).setColorFilter(Color.parseColor(complementaryColors[index]))
                index++
            }
            while(index < colorHolders.size)
            {
                findViewById<ImageView>(colorHolders[index]).visibility = View.INVISIBLE
                index++
            }

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

                            val complementaryColors = ColorMatching.matchPinterest(String.format("#%06X", 0xFFFFFF and color))
                            val colorHolders = listOf(R.id.colorBlinker2, R.id.colorBlinker3, R.id.colorBlinker4, R.id.colorBlinker5, R.id.colorBlinker6)
                            var index = 0
                            while(index < complementaryColors.size && index < colorHolders.size)
                            {
                                findViewById<ImageView>(colorHolders[index]).setColorFilter(Color.parseColor(complementaryColors[index]))
                                index++
                            }
                            while(index < colorHolders.size)
                            {
                                findViewById<ImageView>(colorHolders[index]).visibility = View.INVISIBLE
                                index++
                            }

                            dialog.dismiss()
                        }

                        override fun onColor(color: Int, fromUser: Boolean) {

                        }
                    })
        }

        //Show the dialog at 90% of the screen
        val displayMetrics = context.resources.displayMetrics
        val dialogWidth = displayMetrics.widthPixels * 0.95
        val dialogHeight = displayMetrics.heightPixels * 0.95
        dialog.window!!.setLayout(dialogWidth.toInt(), dialogHeight.toInt())

        dialog.show()
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
