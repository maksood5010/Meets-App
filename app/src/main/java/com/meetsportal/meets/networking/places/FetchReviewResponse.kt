package com.meetsportal.meets.networking.places

import com.google.gson.annotations.SerializedName

class FetchReviewResponse : ArrayList<FetchReviewResponseItem>()

data class FetchReviewResponseItem(
    @SerializedName("__v")
    val __v: Int,
    @SerializedName("_id")
    val _id: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("place_id")
    val place_id: String,
    @SerializedName("rating")
    val rating: Int,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("user_id")
    val user_id: String,
    @SerializedName("user_meta")
    val user_meta: UserMeta
)

data class UserMeta(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("first_name")
    val first_name: String,
    @SerializedName("last_name")
    val last_name: String,
    @SerializedName("location")
    val location: Location,
    @SerializedName("profile_image_url")
    val profile_image_url: String,
    @SerializedName("sid")
    val sid: String,
    @SerializedName("badge")
    val badge: String,
    @SerializedName("username")
    val username: String?,
    @SerializedName("rank")
    val rank: Int?,
    @SerializedName("verified_user")
    val verified_user: Boolean
)
