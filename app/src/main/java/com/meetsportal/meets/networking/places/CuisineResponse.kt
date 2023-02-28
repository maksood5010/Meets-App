package com.meetsportal.meets.networking.places

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CuisineResponse(
    @SerializedName("__v")
    val __v: Int,
    @SerializedName("_id")
    val _id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("definition")
    var definition: List<Cuisine>? = null ,
    @SerializedName("entity_id")
    val entity_id: String,
    @SerializedName("group_id")
    val group_id: String,
    @SerializedName("regenerate")
    val regenerate: Boolean,
    @SerializedName("updatedAt")
    val updatedAt: String
):Parcelable


@Parcelize
data class Cuisine(
    @SerializedName("ar")
    val ar: String,
    @SerializedName("en")
    val en: String,
    @SerializedName("fr")
    val fr: String,
    @SerializedName("key")
    val key: String,
    @SerializedName("isSelected")
    var isSelected : Boolean = false
):Parcelable