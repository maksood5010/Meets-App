package com.meetsportal.meets.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.meetsportal.meets.models.ConfirmPurchaseModel
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.repository.AppRepository
import com.meetsportal.meets.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class BillingViewModal @Inject constructor(var repository: AppRepository) : ViewModel() {

    private val disposable = CompositeDisposable()

    private val onBuyPackage: MediatorLiveData<ResultHandler<List<SkuDetails>?>> = MediatorLiveData()
    private val onPurchaseResponse: MediatorLiveData<ResultHandler<Int>> = MediatorLiveData()
    private val mintsBuy: MediatorLiveData<ConfirmPurchaseModel> = MediatorLiveData()
    private val verifyTransaction : MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private val skus : MediatorLiveData<ResultHandler<JsonArray?>> = MediatorLiveData()


    fun setBuyPackag(skuDetailsList: List<SkuDetails>?) {
        if(skuDetailsList != null)
            onBuyPackage.value = ResultHandler.Success(skuDetailsList)
        else{
            onBuyPackage.value = ResultHandler.Failure(null,"Something went wrong",Exception("Somethingwentwrong"))
        }
    }

    fun setPurchaseResponse(responseCode: Int, purchases: MutableList<Purchase>?) {
        Log.i("TAG"," setting ResponseCode in viewModel")
        onPurchaseResponse.value = ResultHandler.Success(responseCode)
        if(responseCode == BillingClient.BillingResponseCode.OK){
            Log.d("TAG", "setPurchaseResponse: purchaselength:::   ${purchases?.size}")
            purchases?.forEach {
                mintsBuy.value =
                    ConfirmPurchaseModel(token = it.purchaseToken, sku = it.skus.getOrNull(0))
            }
        }
    }

    fun verifyTransaction(confirmPurchaseModel: ConfirmPurchaseModel) {
        disposable.add(repository.verifyTransaction(confirmPurchaseModel)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it.isSuccessful){
                    verifyTransaction.value = ResultHandler.Success(it.body())
                }
            },{
                Utils.handleException(it,verifyTransaction,"verifyTransaction")
            })
        )
    }

    fun getSkus(){
        disposable.add(repository.getSkus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it.isSuccessful){
                    skus.value = ResultHandler.Success(it.body()?.getAsJsonArray("definition"))
                }
            },{
                Utils.handleException(it,skus,"verifyTransaction")
            })
        )
    }
    //-------------------------------------------------------

    fun observerSkus(): LiveData<ResultHandler<JsonArray?>> {
        return skus
    }

    fun observeVerifyTransaction(): LiveData<ResultHandler<JsonObject?>> {
        return verifyTransaction
    }

    fun observeBuyMintsResponse(): MediatorLiveData<ConfirmPurchaseModel> {
        return mintsBuy
    }

    fun observePurchaseResponse(): LiveData<ResultHandler<Int>> {
        Log.i("TAG"," getting ResponseCode in viewModel")
        return onPurchaseResponse
    }

    fun observeBuyPackage(): LiveData<ResultHandler<List<SkuDetails>?>> {
        return onBuyPackage
    }


}