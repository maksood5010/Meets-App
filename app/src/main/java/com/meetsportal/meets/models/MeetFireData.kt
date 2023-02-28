package com.meetsportal.meets.models

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.networking.meetup.ChosenPlace
import com.meetsportal.meets.networking.meetup.MaxJoinTime
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class MeetFireData(

    @PropertyName("id")
    @SerializedName("chosen_place")
    val chosen_place: ChosenPlace? = null,

    @PropertyName("createdAt")
    @SerializedName("createdAt")
    val createdAt : Long? = null,

    @PropertyName("count")
    @SerializedName("count")
    val count : Int? = null,

    @PropertyName("custom_places")
    @SerializedName("custom_places")
    val custom_places : List<AddressModel?>? = null,

    @PropertyName("description")
    @SerializedName("description")
    val description : String? = null,

    @PropertyName("max_join_time")
    @SerializedName("max_join_time")
    val max_join_time : MaxJoinTime? = null,

    @PropertyName("max_vote_changes")
    @SerializedName("max_vote_changes")
    val max_vote_changes : Int? = null,

    @PropertyName("participants")
    @SerializedName("participants")
    val participants : List<String>? = null,

    @PropertyName("pending_invite_count")
    @SerializedName("pending_invite_count")
    val pending_invite_count : Int? = null,

    @PropertyName("user_id")
    @SerializedName("user_id")
    val user_id : String? = null,

    @PropertyName("votes")
    @SerializedName("votes")
    val votes : Map<String,Vote>? = null,

    @PropertyName("voting_closed")
    @SerializedName("voting_closed")
    val voting_closed : Boolean? = null

    ):Parcelable

@Parcelize
data class Vote(

    @PropertyName("animSeen")
    @SerializedName("animSeen")
    var animSeen : Boolean? = null,

    @PropertyName("id")
    @SerializedName("id")
    val id : String? = null,

    @ServerTimestamp
    @PropertyName("timestamp")
    @SerializedName("timestamp")
    val timestamp : Date? = null,

    @PropertyName("sid")
    @SerializedName("sid")
    val sid : String? = null,

    @PropertyName("type")
    @SerializedName("type")
    val type : String? = null,

    @PropertyName("max_vote_changes")
    @SerializedName("max_vote_changes")
    var max_vote_changes: Int? = null,

    @PropertyName("maxVotUpdated")
    @SerializedName("maxVotUpdated")
    var maxVotUpdated : Boolean? = null

):Parcelable