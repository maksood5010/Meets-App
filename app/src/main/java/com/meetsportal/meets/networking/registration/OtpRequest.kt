package com.meetsportal.meets.networking.registration

import com.google.gson.annotations.SerializedName

data class OtpRequest (
    @SerializedName("phone_number")
    val phone_number : String?,
    @SerializedName("code")
    val code : String?,
    @SerializedName("username")
    val username  :String?
    )