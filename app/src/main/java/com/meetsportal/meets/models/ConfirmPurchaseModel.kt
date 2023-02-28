package com.meetsportal.meets.models

import com.google.gson.annotations.SerializedName

data class ConfirmPurchaseModel(
    @SerializedName("platform")
    val platform: String = "android",
    @SerializedName("token")
    val token : String ,
    @SerializedName("sku")
    val sku : String?
)