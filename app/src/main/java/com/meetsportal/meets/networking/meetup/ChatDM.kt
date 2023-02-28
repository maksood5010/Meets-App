package com.meetsportal.meets.networking.meetup

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@IgnoreExtraProperties
@Parcelize
data class ChatDM(
    @PropertyName("id")
    @SerializedName("id")
    var id:String? = null,


    @PropertyName("type")
    @SerializedName("type")
    var type:String? = null,

    @PropertyName("timestamp")
    @SerializedName("timestamp")
    var timestamp : Timestamp? = null,

    @PropertyName("body")
    @SerializedName("body")
    var body : String? = null,

    @PropertyName("senderFid")
    @SerializedName("senderFid")
    var senderFid : String? = null,

    @PropertyName("senderSid")
    @SerializedName("senderSid")
    var senderSid : String? = null,

    @PropertyName("parentMsg")
    @SerializedName("parentMsg")
    var parentMsg : ChatDM? = null,
):Parcelable