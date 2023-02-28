package com.meetsportal.meets.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InsightResponse(

    @SerializedName("default")
    var default: Default? = null,

    @SerializedName("boosted")
    var boosted: Default? = null,

    @SerializedName("is_boosted")
    var is_boosted: Boolean? = false,
                          ) : Parcelable

@Parcelize
data class Default(

    @SerializedName("content_interactions")
    var content_interactions: Interactions? = null,

    @SerializedName("content_reach")
    var content_reach: Reach? = null,

    @SerializedName("profile_acivtity")
    var profile_acivtity: Acivtity? = null,
                  ) : Parcelable

@Parcelize
data class Interactions(

    @SerializedName("likes")
    var likes: Int? = null,

    @SerializedName("comments")
    var comments: Int? = null,

    @SerializedName("total")
    var total: Int? = null,
                       ) : Parcelable

@Parcelize
data class Reach(

    @SerializedName("followers")
    var followers: Int? = null,

    @SerializedName("non_followers")
    var non_followers: Int? = null,

    @SerializedName("total")
    var total: Int? = null,
                ) : Parcelable

@Parcelize
data class Acivtity(

    @SerializedName("new_follows")
    var new_follows: Int? = null,

    @SerializedName("visits")
    var visits: Int? = null,

    @SerializedName("total")
    var total: Int? = null,
                   ) : Parcelable