package com.meetsportal.meets.models

import com.google.gson.annotations.SerializedName

data class Story (

    @SerializedName("image")
    var image : Int?,

    @SerializedName("name")
    var name : String?,

    @SerializedName("followCount")
    var followCount : String?,

)