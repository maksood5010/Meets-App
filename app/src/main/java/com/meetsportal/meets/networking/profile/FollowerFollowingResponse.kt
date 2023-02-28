package com.meetsportal.meets.networking.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.networking.post.UserMeta
import kotlinx.android.parcel.Parcelize

class FollowerFollowingResponse : ArrayList<FollowerFollowingResponseItem>()

@Parcelize
data class FollowerFollowingResponseItem(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("followed_by_user")
    val followed_by_user: Boolean,
    @SerializedName("sid")
    val sid: String,
    @SerializedName("user_meta")
    val user_meta: UserMeta,
    @SerializedName("selected")
    var selected :Boolean? = null,
    @SerializedName("selectedTimeStamp")
    var selectedTimeStamp : Long?
): Parcelable {
    fun toSearchPeopleresponseItem():SearchPeopleResponseItem{
        return SearchPeopleResponseItem(
            user_meta.first_name,user_meta.last_name,user_meta.lite_profile_image_url,user_meta.profile_image_url,sid
        ,user_meta.username,user_meta.verified_user,selected,selectedTimeStamp
        )
    }
}

