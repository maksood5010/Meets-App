package com.meetsportal.meets.networking.post

import com.google.gson.annotations.SerializedName

class SearchPostResponse : ArrayList<SearchPostResponseItem>()

data class SearchPostResponseItem(
    @SerializedName("__v")
    val __v: Int? = null,
    @SerializedName("body")
    val body: String?= null,
    @SerializedName("comments_count")
    val comments_count: Int? = null,
    @SerializedName("createdAt")
    val createdAt: String?= null,
    @SerializedName("elastic_id")
    val elastic_id: String?= null,
    @SerializedName("featured")
    val featured: Boolean?= null,
    @SerializedName("likes_count")
    val likes_count: Int?= null,
    @SerializedName("media_count")
    val media_count: Int?= null,
    @SerializedName("updatedAt")
    val updatedAt: String?= null,
    @SerializedName("user_id")
    val user_id: String?= null,
    @SerializedName("media")
    val media : List<String?>? = null,
    @SerializedName("user_meta")
    val user_meta: UserMeta?= null
)

/*data class UserMetaX(
    val _id: String?= null,
    val document_stage_status: DocumentStageStatus?= null,
    val email: String?= null,
    val emergency_contact: String?= null,
    val first_name: String?= null,
    val interests: List<Interest?>?= null,
    val last_name: String?= null,
    val lite_profile_image_url: String?= null,
    val location_check_in_enabled: Boolean?= null,
    val location_meet_up_enabled: Boolean?= null,
    val phone_number: String?= null,
    val profile_image_url: String?= null,
    val settings: Settings?= null,
    val sid: String?= null,
    val username: String?= null,
    val verified_user: Boolean?= null
)*/


data class Interest(
    @SerializedName("en")
    val en: String?,
    @SerializedName("icon")
    val icon: String?,
    @SerializedName("key")
    val key: String?
)

