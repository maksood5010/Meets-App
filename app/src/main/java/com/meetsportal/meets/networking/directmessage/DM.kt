package com.meetsportal.meets.networking.directmessage

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.text.Html
import android.util.Base64
import android.util.Log
import com.cossacklabs.themis.SecureCell
import com.cossacklabs.themis.SecureCellException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.android.parcel.Parcelize
import java.lang.IndexOutOfBoundsException
import android.graphics.Typeface

import android.text.style.StyleSpan

import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

@Parcelize
data class DM(
    @PropertyName("id")
    @SerializedName("id")
    val id: String? = null,

    @PropertyName("type")
    @SerializedName("type")
    val type: String? = null,

    @PropertyName("timestamp")
    @SerializedName("timestamp")
    val timestamp: Timestamp? = null,

    @PropertyName("body")
    @SerializedName("body")
    val body: String? = null,

    @PropertyName("body_encrypted")
    @SerializedName("body_encrypted")
    val body_encrypted: String? = null,

    @PropertyName("senderFid")
    @SerializedName("senderFid")
    val senderFid: String? = null,

    @PropertyName("senderSid")
    @SerializedName("senderSid")
    val senderSid: String? = null,

    @PropertyName("parentMsg")
    @SerializedName("parentMsg")
    var parentMsg: DMModel.MyDM? = null,

    @PropertyName("thumbnail")
    @SerializedName("thumbnail")
    var thumbnail: String? = null,

    @PropertyName("bitmap")
    @SerializedName("bitmap")
    var bitmap: Bitmap? = null,

    @PropertyName("uri")
    @SerializedName("uri")
    var uri: Uri? = null,

    @PropertyName("duration")
    @SerializedName("duration")
    var duration: String? = null,

    @PropertyName("bool")
    @SerializedName("bool")
    var bool: Boolean = false,
):Parcelable

sealed class DMModel {

    @Parcelize
    @IgnoreExtraProperties
    data class MyDM(

        @PropertyName("id")
        @SerializedName("id")
        val id: String? = null,

        @PropertyName("type")
        @SerializedName("type")
        val type: String? = null,

        @PropertyName("timestamp")
        @SerializedName("timestamp")
        val timestamp: Timestamp? = null,

        @PropertyName("body")
        @SerializedName("body")
        val body: String? = null,

        @PropertyName("body_encrypted")
        @SerializedName("body_encrypted")
        val body_encrypted: String? = null,

        @PropertyName("senderFid")
        @SerializedName("senderFid")
        val senderFid: String? = null,

        @PropertyName("senderSid")
        @SerializedName("senderSid")
        val senderSid: String? = null,

        @PropertyName("parentMsg")
        @SerializedName("parentMsg")
        var parentMsg: MyDM? = null,

        @PropertyName("bitmap")
        @SerializedName("bitmap")
        var bitmap: Bitmap? = null,

        @PropertyName("thumbnail")
        @SerializedName("thumbnail")
        var thumbnail: String? = null,

        @PropertyName("uri")
        @SerializedName("uri")
        var uri: Uri? = null,

        @PropertyName("duration")
        @SerializedName("duration")
        var duration: String? = null,

        @PropertyName("bool")
        @SerializedName("bool")
        var bool: Boolean = false,
    ) : DMModel(), Parcelable {

        constructor(dm: DM?) : this(
                dm?.id,
                dm?.type,
                dm?.timestamp,
                dm?.body,
                dm?.body_encrypted,
                dm?.senderFid,
                dm?.senderSid,
                dm?.parentMsg,
                dm?.bitmap,
                dm?.thumbnail,
                dm?.uri,
                dm?.duration,
                dm?.bool!!,
                                   )
        fun getMessage(passPhrase:String?): String? {
//            return body
            body_encrypted?.let {
                try{
                    val split = passPhrase?.split(",")
                    val cell = SecureCell.SealWithPassphrase(split?.get(0))
                    val plaintext = Base64.decode(body_encrypted, Base64.NO_WRAP)
                    val context = split?.get(1)?.toByteArray()
                    val decrypted = cell.decrypt(plaintext, context)
                    return String(decrypted, charset("UTF-8"))
                }
                catch(e: SecureCellException){
                    Log.d("TAG", "getMessage: SecureCellException ${e.printStackTrace()}")
                    return "waiting for message this may take a while"
                }
                catch(e: IndexOutOfBoundsException){
                    Log.e("TAG", "getMessage: passPhrase split index out of bound $passPhrase $body_encrypted")
                    return "something went wrong!!"
                }
            }?:run{
                 return body
            }
        }
    }

    data class SeparatorItem(
        @SerializedName("description")
        val description: String
        ) : DMModel()

}
