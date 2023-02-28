package com.meetsportal.meets.networking.places

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FacilityResponse(
    @SerializedName("__v")
    val __v: Int? = null,
    @SerializedName("_id")
    val _id: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("definition")
    var definition: List<Facility>? = null,
    @SerializedName("entity_id")
    val entity_id: String? = null,
    @SerializedName("group_id")
    val group_id: String? = null,
    @SerializedName("regenerate")
    val regenerate: Boolean? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
):Parcelable

@Parcelize
data class Facility(
    @SerializedName("ar")
    val ar: String,
    @SerializedName("en")
    val en: String,
    @SerializedName("fr")
    val fr: String,
    @SerializedName("key")
    val key: String? = null,
    @SerializedName("isSelected")
    var isSelected : Boolean = false
):Parcelable