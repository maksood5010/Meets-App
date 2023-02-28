package com.meetsportal.meets.networking.notifications

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class Notification (
    @PropertyName("entity_id")
    @SerializedName("entity_id")
    val entity_id : String? = null,

    @PropertyName("firebase_id")
    @SerializedName("firebase_id")
    val firebase_id : String? = null,

    @PropertyName("payload")
    @SerializedName("payload")
    val payload : Map<String,Any>? = null,

    @PropertyName("singleton")
    @SerializedName("singleton")
    val singleton : Boolean? = null,

    @PropertyName("timestamp")
    @SerializedName("timestamp")
    val timestamp: String? = null,

    @PropertyName("type")
    @SerializedName("type")
    val type : String? = null,

    @PropertyName("user_id")
    @SerializedName("user_id")
    val user_id : String? = null,

    @PropertyName("seen")
    @SerializedName("seen")
    var seen : Boolean? = null,

    @PropertyName("seen_at")
    @SerializedName("seen_at")
    var seen_at : String? = null,

    @PropertyName("document_id")
    @SerializedName("document_id")
    var document_id : String? = null
):Serializable

sealed class NotificationModal{

    data class MyNotification (
        @PropertyName("entity_id")
        @SerializedName("entity_id")
        val entity_id : String? = null,

        @PropertyName("firebase_id")
        @SerializedName("firebase_id")
        val firebase_id : String? = null,

        @PropertyName("payload")
        @SerializedName("payload")
        val payload : Map<String,Any>? = null,

        @PropertyName("singleton")
        @SerializedName("singleton")
        val singleton : Boolean? = null,

        @PropertyName("timestamp")
        @SerializedName("timestamp")
        val timestamp: String? = null,

        @PropertyName("type")
        @SerializedName("type")
        val type : String? = null,

        @PropertyName("user_id")
        @SerializedName("user_id")
        val user_id : String? = null,

        @PropertyName("seen")
        @SerializedName("seen")
        var seen : Boolean? = null,

        @PropertyName("seen_at")
        @SerializedName("seen_at")
        var seen_at : String? = null,

        @PropertyName("document_id")
        @SerializedName("document_id")
        var document_id : String? = null


    ) :NotificationModal(){
        constructor( noti : Notification):this(
            noti.entity_id,
            noti.firebase_id,
            noti.payload,
            noti.singleton,
            noti.timestamp,
            noti.type,
            noti.user_id,
            noti.seen,
            noti.seen_at,
            noti.document_id
        )
    }

    data class SeparatorItem(

        @PropertyName("description")
        @SerializedName("description")
        val description: String
        ) : NotificationModal()
}

