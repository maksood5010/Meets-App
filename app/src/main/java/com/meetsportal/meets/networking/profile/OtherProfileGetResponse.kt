package com.meetsportal.meets.networking.profile

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@IgnoreExtraProperties
data class OtherProfileGetResponse(
    @SerializedName("cust_data")
    val cust_data: OtherCustData,
    @SerializedName("social")
    val social: OtherSocial,
    @SerializedName("selected")
    var selected : Boolean = false,
    @SerializedName("selectedTimeStamp")
    var selectedTimeStamp : Long? = null
):Parcelable{

    fun toSearchPeopleresponseItem():SearchPeopleResponseItem{
        return SearchPeopleResponseItem(
            cust_data.first_name,cust_data.last_name,cust_data.lite_profile_image_url,cust_data.profile_image_url,cust_data.sid
            ,cust_data.username,cust_data.verified_user,selected,selectedTimeStamp
        )
    }
}

@Parcelize
@IgnoreExtraProperties
data class OtherCustData(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("firebase_id")
    val firebase_id: String,
    @SerializedName("first_name")
    val first_name: String,
    @SerializedName("interests")
    val interests: List<Definition>,
    @SerializedName("last_name")
    val last_name: String,
    @SerializedName("lite_profile_image_url")
    val lite_profile_image_url: String,
    @SerializedName("location_check_in_enabled")
    val location_check_in_enabled: Boolean,
    @SerializedName("location_meet_up_enabled")
    val location_meet_up_enabled: Boolean,
    @SerializedName("profile_image_url")
    val profile_image_url: String?,
    @SerializedName("sid")
    val sid: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("verified_user")
    val verified_user: Boolean
):Parcelable


@Parcelize
@IgnoreExtraProperties
data class OtherSocial(
    @SerializedName("__v")
    val __v: Int,
    @SerializedName("bio")
    val bio: String?,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("elastic_id")
    val elastic_id: String,
    @SerializedName("followed_by_you")
    val followed_by_you: Boolean,
    @SerializedName("followers")
    val followers: Int,
    @SerializedName("followers_count")
    val followers_count: Int,
    @SerializedName("followings")
    val followings: Int,
    @SerializedName("followings_count")
    val followings_count: Int,
    @SerializedName("motto")
    val motto: String,
    @SerializedName("message_thread_id")
    val message_thread_id : String? = null,
    @SerializedName("message_thread_secrets")
    val message_thread_secrets : String? = null,
    @SerializedName("posts")
    val posts: Int,
    @SerializedName("matching_interests")
    val matching_interests :Double?,
    @SerializedName("posts_count")
    val posts_count: Int,
    @SerializedName("sid")
    val sid: String,
//    val spotlights: List<Spotlight>,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("wallpaper_url")
    val wallpaper_url: String? = null,
    @SerializedName("badge")
    val badge: String? = null,
    @SerializedName("mints")
    val mints: String? = null,
    @SerializedName("meetup_positive_experience_count")
    val meetup_positive_experience_count: Int? = null,
    @SerializedName("meetup_attendance_count")
    val meetup_attendance_count: Int? = null,
    @SerializedName("vaccinated")
    val vaccinated: Boolean,
    @SerializedName("blocked_by_you")
    val blocked_by_you : Boolean,
    @SerializedName("blocked")
    val blocked : Boolean,
):Parcelable
{
    fun getbadge(): String {
        if(badge==null){
            return "bronze"

        }else{
            return badge
        }
    }
    fun getmints(): String{
        if(mints==null){
            return "0"

        }else{
            return mints
        }
    }
}

