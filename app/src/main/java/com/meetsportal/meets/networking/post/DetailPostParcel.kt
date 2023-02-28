package com.meetsportal.meets.networking.post

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
@IgnoreExtraProperties
data class DetailPostParcel(
    @SerializedName("_id")
    var _id: String? = null,
    @SerializedName("media")
    var media: List<String?>? = null,
    @SerializedName("stats")
    var stats: Stats? = null,
    @SerializedName("body")
    var body: String? = null,
    @SerializedName("user_meta")
    var user_meta: UserMeta? = null,
    @SerializedName("createdAt")
    var createdAt: String? = null,
    @SerializedName("mode")
    var mode : String? = null,
    @SerializedName("type")
    var type : String? = null
): Parcelable