package com.meetsportal.meets.models

import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.networking.post.UserMeta


class GetJoinRequestModel : ArrayList<GetJoinRequestModelItem>()

data class GetJoinRequestModelItem(

    @SerializedName("__v")
    val __v: Int,

    @SerializedName("_id")
    val _id: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("followed_by_user")
    val followed_by_user: Boolean,

    @SerializedName("meetup_id")
    val meetup_id: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("updatedAt")
    val updatedAt: String,

    @SerializedName("user_id")
    val user_id: String,

    @SerializedName("user_meta")
    val user_meta: UserMeta
)

/*data class UserMeta(
    val _id: String,
    val badge: String,
    val email: String,
    val firebase_id: String,
    val first_name: String,
    val interests: List<Interest>,
    val last_name: String,
    val lite_profile_image_url: String,
    val location: Location,
    val mints: Double,
    val profile_image_url: String,
    val sid: String,
    val username: String,
    val verified_user: Boolean
)*/

/*data class Interest(
    val en: String,
    val icon: String,
    val key: String,
    val svg_url: String
)*/

/*
data class Location(
    val coordinates: List<Double>,
    val type: String
)*/
