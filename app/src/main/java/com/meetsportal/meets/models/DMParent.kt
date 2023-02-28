package com.meetsportal.meets.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DMParent (
    @PropertyName("id")
    @SerializedName("id")
    var id: String? = null,
    @PropertyName("lastMessage")
    @SerializedName("lastMessage")
    val lastMessage : LastMessage? = null,

    @PropertyName("participants")
    @SerializedName("participants")
    val participants : List<String>? = null,

    @PropertyName("sids")
    @SerializedName("sids")
    val sids : List<String>? = null,

    @PropertyName("blocked")
    @SerializedName("blocked")
    val blocked : List<String>? = null,

    @PropertyName("profiles")
    @SerializedName("profiles")
    val profiles : List<Profiles?>? = null,

    @PropertyName("lastMessageAddedAt")
    @SerializedName("lastMessageAddedAt")
    val lastMessageAddedAt : Timestamp? = null,

    @PropertyName("unread1")
    @SerializedName("unread1")
    val unread1 : Int? = null,

    @PropertyName("unread2")
    @SerializedName("unread2")
    val unread2 : Int? = null,

    @PropertyName("deleted1")
    @SerializedName("deleted1")
    val deleted1 : Timestamp? = null,

    @PropertyName("deleted2")
    @SerializedName("deleted2")
    val deleted2 : Timestamp? = null,

    @PropertyName("pinned")
    @SerializedName("pinned")
    var pinned : Boolean = false
):Parcelable

@Parcelize
data class Profiles(

    @PropertyName("sid")
    @SerializedName("sid")
    val sid : String? = null,

    @PropertyName("profile_image_url")
    @SerializedName("profile_image_url")
    var profile_image_url : String? = null,

    @PropertyName("username")
    @SerializedName("username")
    val username : String? = null,

    @PropertyName("verified_user")
    @SerializedName("verified_user")
    val verified_user : Boolean = false
):Parcelable
@Parcelize
data class LastMessage(

    @PropertyName("body")
    @SerializedName("body")
    val body : String? = null,

    @PropertyName("body_encrypted")
    @SerializedName("body_encrypted")
    val body_encrypted : String? = null,

    @PropertyName("id")
    @SerializedName("id")
    val id : String? = null,

    @PropertyName("senderFid")
    @SerializedName("senderFid")
    val senderFid : String? = null,

    @PropertyName("senderSid")
    @SerializedName("senderSid")
    val senderSid : String? = null,

    @PropertyName("timestamp")
    @SerializedName("timestamp")
    val timestamp : Timestamp? = null,

    @PropertyName("type")
    @SerializedName("type")
    val type : String? = null,
):Parcelable {
    fun getMessage(): String? {
//        body_encrypted?.let {
//            val split = passPhrase?.split(",")
//            val cell = SecureCell.SealWithPassphrase(split?.get(0))
//            val plaintext = Base64.decode(body_encrypted, Base64.NO_WRAP)
//            val context = split?.get(1)?.toByteArray()
//            val decrypted = cell.decrypt(plaintext, context)
//            return String(decrypted, charset("UTF-8"))
//        }?:run{
//            return body
//        }

        return body
    }
}


