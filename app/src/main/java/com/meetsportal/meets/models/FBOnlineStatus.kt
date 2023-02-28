package com.meetsportal.meets.models

import com.google.gson.annotations.SerializedName

data class FBOnlineStatus (

    @SerializedName("lastOnline")
    val lastOnline : Long? = null,

    @SerializedName("profileImage")
    val profileImage : String? = null,

    @SerializedName("sid")
    val sid: String? = null,

    @SerializedName("status")
    val status : String? = null,

    @SerializedName("userName")
    val userName : String? = null
)