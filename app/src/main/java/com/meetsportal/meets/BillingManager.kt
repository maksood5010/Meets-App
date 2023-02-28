package com.meetsportal.meets

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import com.google.gson.JsonArray
import com.meetsportal.meets.viewmodels.BillingViewModal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager() {
    /**
     * @purchaseToken is a string that represents a buyer's entitlement to a product on Google Play. It indicates that a Google user is entitled to a specific product that is represented by a SKU. You can use the purchase token with the Google Play Developer API.
     * @OrderID is a string that represents a financial transaction on Google Play. This string is included in a receipt that is emailed to the buyer. You can use the Order ID to manage refunds in the used in sales and payout reports.
    */




    companion object{
        var billingClient: BillingClient? = null
        val TAG = BillingManager::class.simpleName
        var purchasesUpdatedListener : PurchasesUpdatedListener? = null
        var billingViewModal: BillingViewModal? = null
        var lifecycle : LifecycleOwner? = null


        private fun getBillingClient(activity: Activity,lifecycle : LifecycleOwner,billingViewModel: BillingViewModal? = null){
            //if(billingClient == null){
            this.billingViewModal = billingViewModel
            this.lifecycle = lifecycle
            billingClient?.let {
                return
            }?:run{
                purchasesUpdatedListener =
                        PurchasesUpdatedListener { billingResult, purchases ->
                            Log.i(TAG," PurchasesUpdatedListener::: -- ${billingResult.responseCode} ")
                            billingViewModal?.setPurchaseResponse(billingResult.responseCode,purchases)
                            if((billingResult.responseCode == BillingClient.BillingResponseCode.OK || billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) && purchases != null){
                                Log.i(TAG," purchases::: $purchases ")
                                for(purchase in purchases){
                                    //if(purchase.purchaseState == Purchase.PurchaseState.PENDING || purchase.purchaseState == Purchase.PurchaseState.PURCHASED){
                                    //billingViewModel?.setBuyMintPurchaseResponse(purchase)
                                    this.lifecycle?.lifecycleScope?.launch {
                                        val consumeParams =
                                                ConsumeParams.newBuilder()
                                                        .setPurchaseToken(purchase.getPurchaseToken())
                                                        .build()
                                        val consumeResult = withContext(Dispatchers.IO) {
                                            billingClient?.consumePurchase(consumeParams)
                                        }
                                        Log.i(TAG," consumeResulr::: ${consumeResult?.billingResult?.responseCode}")
                                        if(consumeResult?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK){

                                        }else{
                                            val consumeResult = withContext(Dispatchers.IO) {
                                                billingClient?.consumePurchase(consumeParams)
                                            }
                                        }
                                    }
                                    //}
                                }
                            }
                        }


                billingClient = BillingClient.newBuilder(activity)
                        .enablePendingPurchases()
                        .setListener(purchasesUpdatedListener!!)
                        .build()
            }
        //}
        }

        fun startProcess(
            activity: Activity,
            lifecycle: LifecycleOwner,
            billingViewModel: BillingViewModal,
            value: JsonArray?
        ){
            getBillingClient(activity,lifecycle)
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    Log.i(TAG," billingResult::: ${billingResult.responseCode}")
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query purchases here.
                        Log.i(TAG," billingResult:::--  1 ")
                        lifecycle.lifecycleScope.launch {
                            Log.i(TAG," billingResult:::--  2 ")
                            querySkuDetails(activity,billingViewModel,value)
                        }
                    }
                }
                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })
        }

        suspend fun querySkuDetails(
            activity: Activity,
            billingViewModel: BillingViewModal,
            value: JsonArray?
        ) {

            Log.i(TAG," billingResult:::--  3 ")
            val skuList = ArrayList<String>()
            value?.forEach {
                skuList.add(it.asString)
            }

           /* skuList.add("101_mints")
            skuList.add("500_mints")*/
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

            Log.i(TAG," billingResult:::--  4 ")
            // leverage querySkuDetails Kotlin extension function
            val skuDetailsResult = withContext(Dispatchers.IO) {
                billingClient?.querySkuDetails(params.build())
            }
            Log.i(TAG," billingResult:::--  5 ${skuDetailsResult?.billingResult?.responseCode}")
            if(skuDetailsResult?.billingResult?.responseCode == BillingClient.BillingResponseCode.OK ){
                Log.i(TAG,"checking All skuDetil::: ${skuDetailsResult.skuDetailsList}")
                billingViewModel.setBuyPackag(skuDetailsResult.skuDetailsList)
            }
            else{
                billingViewModel.setBuyPackag(null)
            }

            //return skuDetailsResult?.skuDetailsList

            /*Log.i(TAG,"  skuDetailsResult::: $skuDetailsResult")
            // Process the result.

            skuDetailsResult?.skuDetailsList?.getOrNull(0)?.let {
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(it)
                    .build()
                val responseCode = billingClient?.launchBillingFlow(activity, flowParams)
                Log.i(TAG," chekcingPurchaseItem:: ${responseCode?.debugMessage}")
                Log.i(TAG," chekcingPurchaseItem:: ${responseCode?.responseCode}")

            }*/

        }

        fun buyPackage(
            activity: Activity,
            lifecycle: LifecycleOwner,
            billingViewModel: BillingViewModal,
            sku : SkuDetails
        ){
            getBillingClient(activity,lifecycle,billingViewModel)
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    Log.i(TAG," buyPackage::: ${billingResult.responseCode}")
                    if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query purchases here.
                        Log.i(TAG," buyPackage:::--  1 ")
                        lifecycle.lifecycleScope.launch {
                            Log.i(TAG," buyPackage:::--  2 ")
                            val flowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(sku)
                                .build()
                            val responseCode = billingClient?.launchBillingFlow(activity, flowParams)
                            //querySkuDetails(activity,billingViewModel)
                        }
                    }
                }
                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })
        }
    }









   /* init {

        purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->

                Log.i(TAG," PurchasesUpdatedListener::: -- ${billingResult.responseCode} ")
                Log.i(TAG," PurchasesUpdatedListener::: **  ${purchases} ")
                // To be implemented in a later section.
            }


        billingClient = BillingClient.newBuilder(activity)
            .enablePendingPurchases()
            .setListener(purchasesUpdatedListener)
            .build()

    }*/







}