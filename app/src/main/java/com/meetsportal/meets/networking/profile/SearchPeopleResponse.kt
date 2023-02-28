package com.meetsportal.meets.networking.profile

import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.models.SelectedContactPeople
import com.meetsportal.meets.overridelayout.mention.MentionPerson

class SearchPeopleResponse : ArrayList<SearchPeopleResponseItem>()

data class SearchPeopleResponseItem(
    @SerializedName("first_name")
    val first_name: String?= null,
    @SerializedName("last_name")
    val last_name: String?= null,
    @SerializedName("SerializedName")
    val lite_profile_image_url: String?= null,
    @SerializedName("profile_image_url")
    val profile_image_url: String?= null,
    @SerializedName("sid")
    val sid: String?= null,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("verified_user")
    val verified_user: Boolean? = null,
    @SerializedName("selected")
    var selected :Boolean? = null,
    @SerializedName("selectedTimeStamp")
    var selectedTimeStamp :Long? = null,
    @SerializedName("badge")
    var badge :String? = null,
){

    fun toSelectedPeople(): SelectedContactPeople.SelectedPeople {
        return SelectedContactPeople.SelectedPeople(
            first_name,last_name,lite_profile_image_url,profile_image_url,sid,username,verified_user,selected,selectedTimeStamp
        )
    }

    fun toMentionPeople():MentionPerson{
        return MentionPerson(alt_id = username,id = sid)
    }
}