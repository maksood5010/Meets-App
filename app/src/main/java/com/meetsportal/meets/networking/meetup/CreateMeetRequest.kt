package com.meetsportal.meets.networking.meetup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.models.AddressModel
import kotlinx.android.parcel.Parcelize

data class CreateMeetRequest(
    @SerializedName("chosen_place")
    var chosen_place: ChosenPlace? = null,
    @SerializedName("custom_places")
    var custom_places: List<AddressModel?>? = null,
    @SerializedName("date")
    var date: String? = null,
    @SerializedName("interests")
    var interests : List<String?>? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("duration")
    var duration : String? = null,
    @SerializedName("invitees")
    var invitees: List<String?>? = null,
    @SerializedName("max_join_time")
    var max_join_time: MaxJoinTime? = null,
    @SerializedName("max_vote_changes")
    var max_vote_changes: Int? = null,
    @SerializedName("min_badge")
    var min_badge: String? = "bronze",
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("places")
    var places: List<String?>? = null,
    @SerializedName("type")
    var type: String? = "private"
)

/*@Parcelize
data class ChosenPlace(
    var id: String? = null,
    var type: String? = null
):Parcelable*/


@Parcelize
data class MaxJoinTime(
    @SerializedName("type")
    var type: String? = null,
    @SerializedName("value")
    var value: String? = null
):Parcelable