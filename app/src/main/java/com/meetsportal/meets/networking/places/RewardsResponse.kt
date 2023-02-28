package com.meetsportal.meets.networking.places

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

class RewardsResponse : ArrayList<RewardsResponseItem>()

@Parcelize
data class RewardsResponseItem(
    @SerializedName("_id")
    var _id: String? = null,
    @SerializedName("component_id")
    var component_id: String? = null,
    @SerializedName("previous_mints")
    var previous_mints: Double? = null,
    @SerializedName("total_mints")
    var total_mints: Double? = null,
    @SerializedName("meta")
    var meta: Meta? = null,

): Parcelable
@Parcelize
data class Meta(
    @SerializedName("name")
    var name: Name? = null,
    @SerializedName("color")
    var color: String? = null,
    @SerializedName("icon_url")
    var icon_url: String? = null,
): Parcelable