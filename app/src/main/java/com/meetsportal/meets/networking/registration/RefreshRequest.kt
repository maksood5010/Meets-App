package com.meetsportal.meets.networking.registration

import com.google.gson.annotations.SerializedName

data class RefreshRequest(
    @SerializedName("client_id")
    val client_id : String = "api-service",
    @SerializedName("client_secret")
    val client_secret :String = "54c7cc09-84d4-40cb-a140-8f4d97f10c61",
    @SerializedName("scope")
    val scope:String = "openid",
    @SerializedName("grant_type")
    val grant_type:String ="refresh_token",
    @SerializedName("refresh_token")
    val refresh_token :String?

)