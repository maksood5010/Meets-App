package com.meetsportal.meets.models

import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.networking.post.UserMeta

class ViewdMe : ArrayList<ViewdMeItem>()

data class ViewdMeItem(

    @SerializedName("followed_by_you")
    val followed_by_you: Boolean,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("user_id")
    val user_id: String,

    @SerializedName("user_meta")
    val user_meta: UserMeta,

    @SerializedName("views_count")
    val views_count: Int
)