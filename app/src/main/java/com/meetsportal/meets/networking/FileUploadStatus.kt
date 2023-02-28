package com.meetsportal.meets.networking

import com.google.gson.annotations.SerializedName

data class FileUploadStatus<T> (
    @SerializedName("url")
    val url :String? = null,
    @SerializedName("percent")
    val percent : String? = null,
    @SerializedName("response")
    val response : T? = null
){

}