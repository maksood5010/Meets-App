package com.meetsportal.meets.networking.profile

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ProfileGetResponse(
    @SerializedName("cust_data")
    val cust_data: CustData? = null,
    @SerializedName("social")
    val social: Social? = null
) : Parcelable

@Parcelize
data class CustData(
    @SerializedName("_id")
    val _id: String? = null,
    @SerializedName("document_stage_status")
    val document_stage_status: DocumentStageStatus?,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("first_name")
    val first_name: String? = null,
    @SerializedName("last_name")
    val last_name: String? = null,
    @SerializedName("last_verification_status")
    val last_verification_status: String? = null,
    @SerializedName("lite_profile_image_url")
    val lite_profile_image_url: String? = null,
    @SerializedName("location_check_in_enabled")
    val location_check_in_enabled: Boolean? = null,
    @SerializedName("location_meet_up_enabled")
    val location_meet_up_enabled: Boolean? = null,
    @SerializedName("phone_number")
    val phone_number: String? = null,
    @SerializedName("region_code")
    val region_code: String? = null,
    @SerializedName("profile_image_url")
    val profile_image_url: String? = null,
    @SerializedName("settings")
    val settings: Settings,
    @SerializedName("interests")
    val interests: List<Definition?>,
    @SerializedName("emergency_contact")
    val emergency_contact: String? = null,
    @SerializedName("sid")
    val sid: String? = null,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("account_type")
    val account_type: String? = null,
    @SerializedName("verified_user")
    val verified_user: Boolean? = null
) : Parcelable//"google", phone_

@Parcelize
data class Social(
    @SerializedName("__v")
    val __v: Int? = null,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("bio")
    val bio: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("elastic_id")
    val elastic_id: String? = null,
    @SerializedName("followers_count")
    val followers_count: Int? = null,
    @SerializedName("followings_count")
    val followings_count: Int? = null,
    @SerializedName("motto")
    val motto: String? = null,
    @SerializedName("posts_count")
    val posts_count: Int? = null,
    @SerializedName("spotlights_count")
    val spotlights_count: Int? = null,
    @SerializedName("sid")
    val sid: String? = null,
    @SerializedName("wallpaper_url")
    val wallpaper_url: String? = null,
    @SerializedName("spotlights")
    val spotlights: List<Spotlight?>?,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("mints")
    val mints: String? = null,
    @SerializedName("badge")
    val badge: String? = null,
    @SerializedName("meetup_positive_experience_count")
    val meetup_positive_experience_count: Int? = null,
    @SerializedName("meetup_attendance_count")
    val meetup_attendance_count: Int? = null,
) : Parcelable {
    fun getbadge(): String {
        if (badge == null) {
            return "bronze"

        } else {
            return badge
        }
    }

    fun getmints(): String {
        if (mints == null) {
            return "0"

        } else {
            return mints
        }
    }
}

@Parcelize
data class DocumentStageStatus(
    @SerializedName("completed")
    val completed: Boolean? = null,
    @SerializedName("missing_documents")
    val missing_documents: List<String?>,
    @SerializedName("pending_verifications")
    val pending_verifications: List<String?>,
    @SerializedName("rejected_documents")
    val rejected_documents: List<String?>
) : Parcelable

@Parcelize
data class Settings(
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
    val dark_mode_enabled: Boolean? = null
) : Parcelable

@Parcelize
data class Badge(
    @SerializedName("key")
    val key: String = "bronze",
    @SerializedName("name")
    val name: String = "Bronze",
    @SerializedName("mintsForNext")
    val mintsForNext: Int = 100,
    @SerializedName("level")
    val level: Int = 1,
    @SerializedName("background")
    val background: Int = R.drawable.badge_level1,
    @SerializedName("foreground")
    val foreground: Int = R.drawable.bronze_badge,
    @SerializedName("mapBg")
    val mapBg: Int = R.drawable.pin_bronze1,
    @SerializedName("light")
    val light: Int = R.color.level1s,
    @SerializedName("dark")
    val dark: Int = R.color.level1e,
    @SerializedName("style")
    var style: Int,
    @SerializedName("selected")
    var selected: Boolean? = false,
    @SerializedName("count")
    var count: Int? = 0,
) : Parcelable


@Parcelize
@IgnoreExtraProperties
data class Spotlight(
    @SerializedName("one")
    val one: String? = null,
    @SerializedName("two")
    val two: String? = null,
    @SerializedName("three")
    val three: String? = null,
) : Parcelable



