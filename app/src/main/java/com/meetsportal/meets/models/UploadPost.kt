package com.meetsportal.meets.models

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName

data class UploadPost(

    @SerializedName("caption")
    var caption:String?  =null,

    @SerializedName("images")
    var images : ArrayList<Bitmap>? = ArrayList()
)

data class UploadPostResource(

    @SerializedName("status")
    var status: UploadPostStatus?,

    @SerializedName("messege")
    var messege: String?
){
    companion object {
        fun success(): UploadPostResource? {
            return UploadPostResource(UploadPostStatus.SUCCESS, null)
        }

        fun error(msg: String): UploadPostResource? {
            return UploadPostResource(UploadPostStatus.ERROR, msg)
        }

        fun loading(): UploadPostResource? {
            return UploadPostResource(UploadPostStatus.LOADING, null)
        }
    }
}

enum class UploadPostStatus {
    SUCCESS,
    ERROR,
    LOADING
}

