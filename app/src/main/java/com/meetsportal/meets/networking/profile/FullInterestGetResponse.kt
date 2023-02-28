package com.meetsportal.meets.networking.profile

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
@Parcelize
data class FullInterestGetResponse(
    @SerializedName("__v")
    val __v: Int,
    @SerializedName("_id")
    val _id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("definition")
    val definition: List<Definition?>,
    @SerializedName("entity_id")
    val entity_id: String,
    @SerializedName("group_id")
    val group_id: String,
    @SerializedName("regenerate")
    val regenerate: Boolean,
    @SerializedName("updatedAt")
    val updatedAt: String
):Parcelable

@IgnoreExtraProperties
@Parcelize
data class Definition(
    @SerializedName("en")
    val en: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("key")
    val key: String,
    @SerializedName("svg_url")
    val svg_url : String,
    @SerializedName("color_svg_url")
    val color_svg_url : String?,
    @SerializedName("selected")
    var selected : Boolean? = null
): Parcelable
