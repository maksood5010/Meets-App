package com.meetsportal.meets.models

import android.os.Parcelable
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

class BoostPricingModelResponse :ArrayList<PricingModelItem>(){}
class TopUpBoostModelResponse :ArrayList<JsonObject>(){}

@Parcelize
data class PricingModelItem (
    @SerializedName("_id")
    var _id: String?  = null,
    @SerializedName("elastic_id")
    var elastic_id: String?  = null,
    @SerializedName("product_id")
    var product_id: String?  = null,
    @SerializedName("discount")
    var discount: Int?  = null,
    @SerializedName("quantity")
    var quantity: Int?  = null,
    @SerializedName("label")
    var label: String?  = null,
//    var duration: Long?  = null,
    @SerializedName("amount")
    var amount: Int?  = null,

    ): Parcelable

@Parcelize
data class ActiveItemResponse (
    @SerializedName("meta")
    var meta: MetaItem?  = null,
    @SerializedName("initiated_at")
    var initiated_at: String?  = null,
    @SerializedName("terminating_at")
    var terminating_at: String?  = null,

    ): Parcelable

@Parcelize
data class MetaItem (
    @SerializedName("user_id")
    var user_id: String?  = null,
    @SerializedName("product_meta")
    var product_meta: ProductMeta?  = null,

    ): Parcelable

@Parcelize
data class ProductMeta (
    @SerializedName("post_id")
    var post_id: String?  = null,
    ): Parcelable