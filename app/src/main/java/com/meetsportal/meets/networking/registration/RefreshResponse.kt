package com.meetsportal.meets.networking.registration

import com.google.gson.annotations.SerializedName

data class RefreshResponse(
    @SerializedName("access_token")
    val access_token: String,
    @SerializedName("expires_in")
    val expires_in: Int,
    @SerializedName("id_token")
    val id_token: String,
    @SerializedName("refresh_expires_in")
    val refresh_expires_in: Int,
    @SerializedName("refresh_token")
    val refresh_token: String,
    @SerializedName("scope")
    val scope: String,
    @SerializedName("session_state")
    val session_state: String,
    @SerializedName("token_type")
    val token_type: String
)