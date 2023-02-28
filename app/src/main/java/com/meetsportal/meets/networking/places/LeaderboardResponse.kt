package com.meetsportal.meets.networking.places

import com.google.gson.annotations.SerializedName

data class LeaderboardResponse (val leaderboard: ArrayList<LeaderboardResponseItem>,val user: LeaderboardResponseItem)

data class LeaderboardResponseItem(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("sid")
    val sid: String,
    @SerializedName("region_code")
    val region_code: String,
    @SerializedName("mints")
    val mints: Double,
    @SerializedName("badge")
    val badge: String,
    @SerializedName("rank")
    val rank: String,
    @SerializedName("user_meta")
    val user_meta: UserMeta
)
