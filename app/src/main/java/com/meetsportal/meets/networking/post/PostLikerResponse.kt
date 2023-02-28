package com.meetsportal.meets.networking.post

import com.google.gson.annotations.SerializedName

class PostLikerResponse : ArrayList<PostLikerResponseItem>()

data class PostLikerResponseItem(
    @SerializedName("__v")
    val __v: Int,
    @SerializedName("_id")
    val _id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("entity_id")
    val entity_id: String,
    @SerializedName("followed_by_user")
    val followed_by_user: Boolean,
    @SerializedName("type")
    val type: String,
    @SerializedName("blocked_by_you")
    val blocked_by_you :Boolean = false,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("user_id")
    val user_id: String,
    @SerializedName("user_meta")
    val user_meta: UserMeta
)

/*
data class UserMeta(
    val _id: String,
    val first_name: String,
    val last_name: String,
    val lite_profile_image_url: Any,
    val profile_image_url: String,
    val sid: String,
    val username: String,
    val verified_user: Boolean
)*/
