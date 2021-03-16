package fr.swapparel.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import fr.swapparel.BuildConfig
import fr.swapparel.R
import info.hoang8f.widget.FButton
import kotlinx.android.synthetic.main.activity_apropos.*
import net.hockeyapp.android.metrics.MetricsManager
import spencerstudios.com.bungeelib.Bungee
import android.content.DialogInterface
import android.widget.EditText
import androidx.appcompat.app.AlertDialog


class AproposActivity : AppCompatActivity(), PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fullScreenCall()
        setContentView(R.layout.activity_apropos)
        MetricsManager.trackEvent("APROPOS")

        findViewById<TextView>(R.id.textView17).text = "Version " + BuildConfig.VERSION_NAME

        findViewById<FButton>(R.id.libraries).setOnClickListener {
            LibsBuilder()
                    .withActivityStyle(Libs.ActivityStyle.LIGHT)
                    .start(this)
        }

        findViewById<FButton>(R.id.intro).setOnClickListener {
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
        }

        findViewById<FButton>(R.id.privacy).setOnClickListener {
            val intent = Intent(this, PolicyActivity::class.java)
            startActivity(intent)
        }

        restore_purchase.setOnClickListener {
            setupBilling()
            Toast.makeText(applicationContext, "Querying in-app purchases...", Toast.LENGTH_LONG).show()
        }

        animation_view.setOnClickListener {
            val txtUrl = EditText(this)

            txtUrl.hint = "Enter developer passcode"

            AlertDialog.Builder(this)
                    .setTitle("Developer code")
                    .setView(txtUrl)
                    .setPositiveButton("OK") { dialog, whichButton ->
                        val url = txtUrl.text.toString()
                        if(url == "777sano") {
                            val sharedPrefs = getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)
                            sharedPrefs.edit()
                                    .putBoolean("proUser", true)
                                    .commit()
                            MetricsManager.trackEvent("PRO_RESTORE")
                            Toast.makeText(applicationContext, "Pro functionnality has been restored !", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    .setNegativeButton("Cancel", { dialog, whichButton -> })
                    .show()
        }
    }

    private fun setupBilling() {
        billingClient = BillingClient.newBuilder(applicationContext).setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    val purchasesResult: Purchase.PurchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                    if(purchasesResult.purchasesList.size > 0)
                    {
                        val sharedPrefs = getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)
                        sharedPrefs.edit()
                                .putBoolean("proUser", true)
                                .commit()
                        //findViewById<FButton>(R.id.buy).visibility = View.INVISIBLE
                        MetricsManager.trackEvent("PRO_RESTORE")
                        Toast.makeText(applicationContext, "Pro functionnality has been restored !", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@AproposActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                    else
                        Toast.makeText(applicationContext, "You didn't buy the pro version !", Toast.LENGTH_LONG).show()
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Toast.makeText(applicationContext, "There was an error with the billing service. Please try later", Toast.LENGTH_LONG).show()
            }
        })
    }

    //The user comes back from the Google Play window
    @SuppressLint("ApplySharedPref")
    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {

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
