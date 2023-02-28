package com.meetsportal.meets.models

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
@IgnoreExtraProperties
data class Comment(
    @SerializedName("id")
    var id: String? = null,
    @SerializedName("user")
    var user: UserProfile? = null,
    @SerializedName("caption")
    var caption: String? = null,
    @SerializedName("time")
    var time: Date? = null,
    @SerializedName("type")
    var type: String? = null,
    @SerializedName("replies")
    var replies: List<Comment>? = null,
    @SerializedName("likeCount")
    var likeCount: Int? = null,
    @SerializedName("commentCount")
    var commentCount: Int? = null,
) : Parcelable