package com.meetsportal.meets.networking

import com.google.gson.annotations.SerializedName

sealed class ResultHandler<out T> {
    data class Loading(
        @SerializedName("loading")
        val loading : Boolean
    ):ResultHandler<Nothing>()
    data class Success<out R>(
        @SerializedName("value")
        val value: R
        ): ResultHandler<R>()
    data class Failure(
        @SerializedName("code")
        val code:String?,
        @SerializedName("message")
        val message: String?,
        @SerializedName("throwable")
        val throwable: Throwable?
    ): ResultHandler<Nothing>()
}

