package com.meetsportal.meets.networking.registration

import com.google.gson.annotations.SerializedName

data class RegistrationResponse(
    @SerializedName("msg")
    val msg : String?,
    @SerializedName("new_user")
    val new_user : Boolean?,
    @SerializedName("code")
    val code : String?
)