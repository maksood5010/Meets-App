package com.meetsportal.meets.networking.registration

import com.google.gson.annotations.SerializedName

data class RegistrationRequest(
    @SerializedName("username")
    private val username : String?,
    @SerializedName("phone_number")
    private val phone_number : String?,
    @SerializedName("access_type")
    private val access_type : String?,
)