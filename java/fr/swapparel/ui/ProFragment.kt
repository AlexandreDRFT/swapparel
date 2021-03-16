package fr.swapparel.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.android.billingclient.api.*
import fr.swapparel.R
import info.hoang8f.widget.FButton
import kotlinx.android.synthetic.main.fragment_pro.view.*
import net.hockeyapp.android.metrics.MetricsManager
import java.util.*
import kotlin.collections.ArrayList

class ProFragment : Fragment(), PurchasesUpdatedListener {
    private lateinit var billingClient: BillingClient
    lateinit var v : View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_pro, container, false)
        this.v = v

        val sharedPrefs = context!!.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)

        //Supprime le background qui ne sert qu'au design
        val background = v.findViewById<ConstraintLayout>(R.id.bcground)
        background.setBackgroundResource(0)

        if (!sharedPrefs.getBoolean("proUser", false)) {
            val r = Random()
            v.findViewById<TextView>(R.id.questionmark).text = when(r.nextInt(2))
            {
                0 -> getString(R.string.questionmark_1)
                1 -> getString(R.string.questionmark_2)
                else -> getString(R.string.questionmark_2)
            }

            setupBilling()
            setupBuyButton()
        } else { //User is pro
            v.findViewById<TextView>(R.id.questionmark).text = getString(R.string.yourepro)
            v.pro_text.visibility = View.INVISIBLE
            v.pro_validated.visibility = View.VISIBLE
            v.findViewById<TextView>(R.id.pro_text_2).visibility = View.INVISIBLE
            v.findViewById<FButton>(R.id.buy).visibility = View.INVISIBLE
            val lottieObject = v.findViewById<LottieAnimationView>(R.id.loading_view)
            lottieObject.setAnimation("yourepro.json")
            lottieObject.playAnimation()

            v.pro_text.textSize = 20f
        }

        return v
    }

    private fun setupBuyButton() {
        v.findViewById<FButton>(R.id.buy).setOnClickListener {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSku("swapparel_pro")
                    .setType(BillingClient.SkuType.INAPP) // SkuType.SUB for subscription
                    .build()
            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    //The user comes back from the Google Play window
    @SuppressLint("ApplySharedPref")
    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            for (purchase in purchases) {
                val sharedPrefs = v.context.getSharedPreferences("SNAPPAREL", Context.MODE_PRIVATE)
                sharedPrefs.edit()
                        .putBoolean("proUser", true)
                        .commit()
                v.findViewById<FButton>(R.id.buy).visibility = View.INVISIBLE
                MetricsManager.trackEvent("PRO_BUYER")
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    private fun setupBilling() {
        billingClient = BillingClient.newBuilder(context!!).setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    val skuList = ArrayList<String>()
                    skuList.add("swapparel_pro")
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                    billingClient.querySkuDetailsAsync(params.build()) { responseCode, skuDetailsList ->
                        // Process the result.
                        if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                            for (skuDetails in skuDetailsList) {
                                val sku = skuDetails.sku
                                val price = skuDetails.price
                            }
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    companion object {
        fun newInstance(text: String): ProFragment {

            val f = ProFragment()
            val b = Bundle()
            b.putString("msg", text)

            f.arguments = b

            return f
        }
    }
}
