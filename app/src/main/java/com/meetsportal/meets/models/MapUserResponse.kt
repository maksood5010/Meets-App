package com.meetsportal.meets.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

class MapUserResponse : ArrayList<MapUserResponseItem?>()

@Parcelize
data class MapUserResponseItem(

    @SerializedName("_id")
    val _id: String?=null,

    @SerializedName("address")
    val address: Address?=null,

    @SerializedName("distance")
    val distance: Int?=null,

    @SerializedName("first_name")
    val first_name: String?=null,

    @SerializedName("interests")
    val interests: List<Interest>?=null,

    @SerializedName("last_name")
    val last_name: String?=null,

    @SerializedName("location")
    val location: UserLocation?=null,

    @SerializedName("location_captured_at")
    val location_captured_at:String?=null,

    @SerializedName("profile_image_url")
    val profile_image_url: String?=null,

    @SerializedName("badge")
    val badge: String?=null,

    @SerializedName("boosted")
    val boosted: Boolean?=null,

    @SerializedName("sid")
    val sid: String?=null,

    @SerializedName("username")
    val username: String?=null
):Parcelable

@Parcelize
data class Address(

    @SerializedName("formatted_address")
    val formatted_address: String?=null
):Parcelable

@Parcelize
data class Interest(
    @SerializedName("en")
    val en: String?=null,
    @SerializedName("icon")
    val icon: String?=null,
    @SerializedName("key")
    val key: String?=null
):Parcelable

@Parcelize
data class UserLocation(
    @SerializedName("coordinates")
    var coordinates: List<Double>?=null,
    @SerializedName("type")
    val type: String= "point"
):Parcelable