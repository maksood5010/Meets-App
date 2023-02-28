package com.meetsportal.meets.networking.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

class SuggestionResponse : ArrayList<SuggestionResponseItem>()

@Parcelize
data class SuggestionResponseItem(
    /*val __v: Int? = null,
    val bio: String? = null,
    val createdAt: String? = null,
    val elastic_id: String? = null,
    val followed_by_user: Boolean? = null,
    val followers_count: Int? = null,
    val followings_count: Int? = null,
    val motto: String? = null,
    val posts_count: Int? = null,
    val sid: String? = null,
    val spotlights: List<Any>? = null,
    val updatedAt: String? = null,
    val user: User? = null*/

    @SerializedName("_id")
    val _id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("firebase_id")
    val firebase_id: String,
    @SerializedName("first_name")
    val first_name: String,
    @SerializedName("followed_by_user")
    val followed_by_user: Boolean,
    @SerializedName("interests")
    val interests: List<Interest>,
    @SerializedName("last_name")
    val last_name: String,
    @SerializedName("lite_profile_image_url")
    val lite_profile_image_url: String,
    @SerializedName("phone_number")
    val phone_number: String,
    @SerializedName("profile_image_url")
    val profile_image_url: String,
    @SerializedName("sid")
    val sid: String,
    @SerializedName("social")
    val social: Social,
    @SerializedName("username")
    val username: String,
    @SerializedName("verified_user")
    val verified_user: Boolean

):Parcelable{
    fun toPeople():SearchPeopleResponseItem{
        return SearchPeopleResponseItem(first_name,last_name,lite_profile_image_url, profile_image_url,sid, username, verified_user,null,null,social.getbadge())
    }
}

@Parcelize
data class User(
    @SerializedName("_id")
    val _id: String? = null,
    @SerializedName("first_name")
    val first_name: String? = null,
    @SerializedName("last_name")
    val last_name: String? = null,
    @SerializedName("lite_profile_image_url")
    val lite_profile_image_url: String? = null,
    @SerializedName("profile_image_url")
    val profile_image_url: String? = null,
    @SerializedName("sid")
    val sid: String? = null,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("verified_user")
    val verified_user: Boolean? = null
):Parcelable



/*class AiseHi : ArrayList<AiseHiItem>()

data class AiseHiItem(
    val _id: String,
    val email: String,
    val firebase_id: String,
    val first_name: String,
    val followed_by_user: Boolean,
    val interests: List<Interest>,
    val last_name: String,
    val lite_profile_image_url: String,
    val phone_number: String,
    val profile_image_url: String,
    val sid: String,
    val social: Social,
    val username: String,
    val verified_user: Boolean
)*/

@Parcelize
data class Interest(
    @SerializedName("en")
    val en: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("key")
    val key: String
):Parcelable

