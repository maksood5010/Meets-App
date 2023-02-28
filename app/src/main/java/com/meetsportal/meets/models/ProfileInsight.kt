package com.meetsportal.meets.models

import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.networking.post.UserMeta


data class ProfileInsight(

    @SerializedName("profile_views")
    val profile_views: List<ProfileView>,

    @SerializedName("region_views")
    val region_views: List<RegionView>,

    @SerializedName("weekly_metrics")
    val weekly_metrics: List<WeeklyMetric>,

    @SerializedName("profile_views_count")
    val profile_views_count : Int?,

    @SerializedName("is_subscribed")
    val is_subscribed : Boolean?
)

data class ProfileView(

    @SerializedName("followed_by_you")
    val followed_by_you: Boolean,

    @SerializedName("user_meta")
    val user_meta: UserMeta
)

data class RegionView(

    @SerializedName("region")
    val region: String,

    @SerializedName("stats")
    val stats: Stats
)

data class WeeklyMetric(

    @SerializedName("boosted_views")
    val boosted_views: Int,

    @SerializedName("date")
    val date: String,

    @SerializedName("normal_views")
    val normal_views: Int,

    @SerializedName("total_views")
    val total_views: Int
)

data class Stats(

    @SerializedName("boosted_views")
    val boosted_views: Int,

    @SerializedName("normal_views")
    val normal_views: Int,

    @SerializedName("total_views")
    val total_views: Int
)