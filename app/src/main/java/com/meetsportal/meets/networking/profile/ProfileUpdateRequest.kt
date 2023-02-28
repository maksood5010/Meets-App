package com.meetsportal.meets.networking.profile

import com.google.gson.annotations.SerializedName

data class ProfileUpdateRequest(
    @SerializedName("motto")
    val motto: String? = null,
    @SerializedName("spotlights")
    val spotlights: List<Any?>? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("cust_data")
    val cust_data: CustomerData? = null
)


data class CustomerData(
    @SerializedName("_id")
    val _id: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("emergency_contact")
    val emergency_contact: String? = null,
    @SerializedName("first_name")
    val first_name: String? = null,
    @SerializedName("last_name")
    val last_name: String? = null,
    @SerializedName("lite_profile_image_url")
    val lite_profile_image_url: String? = null,
    @SerializedName("location_check_in_enabled")
    val location_check_in_enabled: Boolean? = null,
    @SerializedName("location_meet_up_enabled")
    val location_meet_up_enabled: Boolean? = null,
    @SerializedName("interests")
    val interests: List<Definition?>? = null,
    @SerializedName("phone_number")
    val phone_number: String? = null,
    @SerializedName("profile_image_url")
    val profile_image_url: String? = null,
    @SerializedName("settings")
    val settings: SettingsRequest? = null,
    @SerializedName("sid")
    val sid: String? = null,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("verified_user")
    val verified_user: Boolean? = null
)

data class SettingsRequest(
    @SerializedName("language")
    val language: String? = null,
    @SerializedName("notifications_enabled")
    val notifications_enabled: Boolean? = null,
    @SerializedName("followings_posts_enabled")
    val followings_posts_enabled: Boolean? = null,
    @SerializedName("public_posts_enabled")
    val public_posts_enabled: Boolean? = null,
    @SerializedName("show_interests")
    val show_interests: Boolean? = null,
    @SerializedName("dark_mode_enabled")
    val dark_mode_enabled: Boolean? = null,
)



