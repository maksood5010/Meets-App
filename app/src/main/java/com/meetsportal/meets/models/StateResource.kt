package com.meetsportal.meets.models

import com.google.gson.annotations.SerializedName

class StateResource<out T>(
    val status: Status? = null,
    val message: T? = null,
    val error: MyError? = null
){
    companion object {
        inline fun <T> success(mag:T): StateResource<T>? {
            return StateResource(Status.SUCCESS, mag)
        }

        fun <T>error(error: MyError): StateResource<T>? {
            return StateResource<T>(Status.ERROR, null,error)
        }

        /*fun <T>loading(): StateResource<T>? {
            return StateResource(Status.LOADING, null)
        }*/
    }

    override fun toString(): String {
        return "StateResource(status=$status, message=$message, error=$error)"
    }


}

data class MyError(
    @SerializedName("code")
    val code : String? = null,

    @SerializedName("message")
    val message : String? = null
)

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}