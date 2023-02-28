package com.meetsportal.meets.networking.profile

import com.google.gson.annotations.SerializedName

data class ProfileUpdateResponse(
    @SerializedName("spotlights")
    val spotlights: List<String>? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("sid")
    val sid: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("motto")
    val motto: String? = null,
    @SerializedName("elastic_id")
    val elastic_id: String? = null,
    @SerializedName("followers_count")
    val followers_count: Int?,
    @SerializedName("followings_count")
    val followings_count: Int? = null,
    @SerializedName("posts_count")
    val posts_count: Int? = null,
)