package com.meetsportal.meets.networking.meetup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.networking.places.Location
import com.meetsportal.meets.networking.places.Name
import com.meetsportal.meets.networking.post.JoinRequests
import com.meetsportal.meets.networking.post.OpenMeetUp
import kotlinx.android.parcel.Parcelize

class GetMeetUpResponse : ArrayList<GetMeetUpResponseItem>() {
    companion object {
        val USER_CREATED = "user_created"
        val USER_INVITED = "user_invited"
        val PREVIOUS = "previous"
        val UPCOMING = "upcoming"
    }
}

@Parcelize
data class GetMeetUpResponseItem(
    @SerializedName("attendees")
    val attendees: List<MeetPerson>?,
    @SerializedName("__v")
    val __v: Int,
    @SerializedName("_id")
    val _id: String,
    @SerializedName("chosen_place")
    val chosen_place: ChosenPlace? = null,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("custom_places")
    val custom_places: List<AddressModel>? = null,
    @SerializedName("date")
    val date: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("duration")
    val duration : String?,
    @SerializedName("elastic_id")
    val elastic_id: String?,
    @SerializedName("fb_chat_id")
    val fb_chat_id: String,
    @SerializedName("post_id")
    val post_id : String,
    @SerializedName("invitees")
    val invitees: List<MeetPerson>,
    @SerializedName("join_requests")
    val join_requests : JoinRequests? = null,
    @SerializedName("max_vote_changes")
    val max_vote_changes: Int? = null,
    @SerializedName("max_join_time")
    val max_join_time: MaxJoinTime? = null,
    @SerializedName("name")
    val name: String?,
    @SerializedName("min_badge")
    val min_badge: String?,
    @SerializedName("participants")
    val participants: List<MeetPerson>,
    @SerializedName("places")
    val places: List<Place>?,
    @SerializedName("status")
    val status: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("join_requested_by_user")
    val join_requested_by_user :Boolean? =null,
    @SerializedName("join_accepted_by_user")
    val join_accepted_by_user :Boolean? = null,
    @SerializedName("unregistered_invitees")
    val unregistered_invitees: List<String>,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("user_id")
    val user_id: String,
    @SerializedName("user_meta")
    val user_meta: UserMeta,
    @SerializedName("voting_closed")
    val voting_closed: Boolean
) : Parcelable{

    fun toOpemMeet():OpenMeetUp?{
        return OpenMeetUp(chosen_place,createdAt,date,description,
             join_requests,_id,true,join_accepted_by_user,name,places,status,voting_closed,custom_places)
    }
}

@Parcelize
data class ChosenPlace(
    @SerializedName("type")
    var type: String? = null,
    @SerializedName("id")
    var id: String? = null,
) : Parcelable


@Parcelize
data class MeetJoinRequest(
    @SerializedName("accept")
    val accept:Boolean?= null,
    @SerializedName("isOpen")
    val isOpen:Boolean?= null,
    @SerializedName("invitationId")
    val invitationId : String?= null,
    @SerializedName("meetUpId")
    val meetUpId : String?= null,
    @SerializedName("postId")
    val postId : String?= null,
) : Parcelable



@Parcelize
data class MeetPerson(
    @SerializedName("_id")
    val _id: String,
    @SerializedName("firebase_id")
    val firebase_id: String,
    @SerializedName("first_name")
    val first_name: String,
    @SerializedName("followed_by_user")
    val followed_by_user: Boolean,
    @SerializedName("last_name")
    val last_name: String,
    @SerializedName("lite_profile_image_url")
    val lite_profile_image_url: String?,
    @SerializedName("location")
    val location: Location,
    @SerializedName("profile_image_url")
    val profile_image_url: String,
    @SerializedName("sid")
    val sid: String,
    @SerializedName("rated_by_user")
    val rated_by_user : Boolean? = null,
    @SerializedName("username")
    val username: String,
    @SerializedName("verified_user")
    val verified_user: Boolean,
    @SerializedName("badge")
    val badge : String?
) : Parcelable

@Parcelize
data class Place(
    @SerializedName("_id")
    val _id: String?,
    @SerializedName("elastic_id")
    val elastic_id : String?,
    @SerializedName("featured_image_url")
    val featured_image_url: String,
    @SerializedName("is_bookable")
    val is_bookable: Boolean,
    @SerializedName("main_image_url")
    val main_image_url: String?,
    @SerializedName("main_web_image_url")
    val main_web_image_url: String,
    @SerializedName("primary_kind_of_place")
    val primary_kind_of_place: List<String?>?,
    @SerializedName("name")
    val name: Name,
    @SerializedName("rating")
    val rating: Double,
    @SerializedName("selected")
    var selected: Boolean = false
) : Parcelable

@Parcelize
data class UserMeta(
    @SerializedName("firebase_id")
    val firebase_id: String,
    @SerializedName("first_name")
    val first_name: String,
    @SerializedName("last_name")
    val last_name: String,
    @SerializedName("profile_image_url")
    val profile_image_url: String,
    @SerializedName("sid")
    val sid: String,
    @SerializedName("username")
    val username: String
) : Parcelable


enum class MeetScope(
    @SerializedName("scope")
    val scope :String
    ){
    PREVIOUS("previous"),
    UPCOMING("upcoming")
}

enum class MeetStatus(
    @SerializedName("status")
    val status: String
    ) {
    ACTIVE("active"),
    CANCELLED("cancelled"),
}





