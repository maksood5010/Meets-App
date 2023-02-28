package com.meetsportal.meets.models

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName

sealed class SelectedContactPeople {

    @SerializedName("sortTimestamp")
    var sortTimestamp : Long? = null

    @SerializedName("contactName")
    var contactName : String? = null

    constructor(timestamp :Long?,name:String?){
        sortTimestamp = timestamp
        contactName = name

    }

    data class SelectedContact(

        @SerializedName("id")
        val id: String,

        @SerializedName("name")
        var name: String? = null,

        @SerializedName("number")
        var number: String? = null,

        @SerializedName("image")
        var image: Bitmap? = null,

        @SerializedName("selected")
        var selected : Boolean = false,

        @SerializedName("timeStamp")
        var timeStamp : Long? = null
        ) : SelectedContactPeople(timeStamp,name?:number)

    data class SelectedPeople(

        @SerializedName("first_name")
        val first_name: String? = null,

        @SerializedName("last_name")
        val last_name: String? = null,

        @SerializedName("lite_profile_image_url")
        val lite_profile_image_url: String? = null,

        @SerializedName("profile_image_url")
        val profile_image_url: String? = null,

        @SerializedName("sid")
        val sid: String? = null,

        @SerializedName("username")
        val username: String? = null,

        @SerializedName("verified_user")
        val verified_user: Boolean? = null,

        @SerializedName("selected")
        var selected: Boolean? = null,

        @SerializedName("timeStamp")
        var timeStamp : Long? = null
    ) : SelectedContactPeople(timeStamp,username)

}