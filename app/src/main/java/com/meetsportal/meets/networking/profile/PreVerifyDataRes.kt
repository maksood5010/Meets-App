package com.meetsportal.meets.networking.profile

import com.google.gson.annotations.SerializedName

data class PreVerifyDataRes(
    val __v: Int,
    @SerializedName("_id")
    val _id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("definition")
    val definition: Urls,
    @SerializedName("entity_id")
    val entity_id: String,
    @SerializedName("group_id")
    val group_id: String,
    @SerializedName("regenerate")
    val regenerate: Boolean,
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class Urls(
    @SerializedName("pose_1")
    val pose_1: String,
    @SerializedName("pose_2")
    val pose_2: String,
    @SerializedName("pose_description_1")
    val pose_description_1 :String?,
    @SerializedName("pose_description_2")
    val pose_description_2 :String?,
)