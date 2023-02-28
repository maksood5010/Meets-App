package com.meetsportal.meets.networking.profile

import com.google.gson.annotations.SerializedName

data class UploadDocument(
    @SerializedName("type")
    val type: String,
    @SerializedName("urls")
    val urls: List<String>
)