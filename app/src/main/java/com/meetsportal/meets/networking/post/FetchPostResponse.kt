package com.meetsportal.meets.networking.post

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.models.ActiveItemResponse
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.networking.meetup.ChosenPlace
import com.meetsportal.meets.networking.meetup.MaxJoinTime
import com.meetsportal.meets.networking.meetup.Place
import com.meetsportal.meets.networking.places.Name
import com.meetsportal.meets.networking.places.Timing
import com.meetsportal.meets.overridelayout.mention.MentionPerson
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

open class FetchPostResponse : ArrayList<FetchPostResponseItem>()


@Parcelize
@IgnoreExtraProperties
data class FetchPostResponseItem(
    @SerializedName("__v")
    var __v: Int? = null,
    @SerializedName("_id")
    var _id: String? = null,
    @SerializedName("body")
    var body: String? = null,
    @SerializedName("body_obj")
    var body_obj: ExtraInfo? = null,
    @SerializedName("createdAt")
    var createdAt: String? = null,
    @SerializedName("deleted")
    var deleted: Boolean? = false,
    @SerializedName("hidden")
    var hidden: Boolean? = false,
    @SerializedName("boosted")
    var boosted: Boolean? = false,
    @SerializedName("boost_meta")
    var boost_meta: ActiveItemResponse? = null,
    @SerializedName("comments_enabled")
    var comments_enabled: Boolean? = null,
    @SerializedName("elastic_id")
    var elastic_id: String? = null,
    @SerializedName("liked_by_user")
    var liked_by_user: Boolean? = null,
    @SerializedName("media")
    var media: List<String>? = null,
    @SerializedName("mentions")
    var mentions: List<MentionPerson>? = null,
    @SerializedName("stats")
    var stats: Stats? = null,
    @SerializedName("type")
    var type: String? = "default",
    @SerializedName("updatedAt")
    var updatedAt: String? = null,
    @SerializedName("user_id")
    var user_id: String? = null,
    @SerializedName("user_meta")
    var user_meta: UserMeta? = null,
) : Parcelable {
    fun toSearchPost(): SearchPostResponseItem {
        return SearchPostResponseItem(
            body = body, createdAt = createdAt, elastic_id = elastic_id,
            updatedAt = updatedAt, user_id = user_id, media = media, user_meta = user_meta,
            comments_count = stats?.comments, likes_count = stats?.likes
        )
    }
}

@Parcelize
data class Stats(
    @SerializedName("comments")
    var comments: Int? = null,
    @SerializedName("likes")
    var likes: Int? = null,
) : Parcelable

@Parcelize
data class UserMeta(
    @SerializedName("_id")
    var _id: String? = null,
    @SerializedName("first_name")
    var first_name: String? = null,
    @SerializedName("last_name")
    var last_name: String? = null,
    @SerializedName("lite_profile_image_url")
    var lite_profile_image_url: String? = null,
    @SerializedName("profile_image_url")
    var profile_image_url: String? = null,
    @SerializedName("sid")
    var sid: String? = null,
    @SerializedName("username")
    var username: String? = null,
    @SerializedName("badge")
    var badge: String? = null,
    @SerializedName("mints")
    var mints: Double? = null,
    @SerializedName("verified_user")
    var verified_user: Boolean? = null
) : Parcelable{
    fun getName(): String? {
        if(first_name.isNullOrEmpty()){
            return username
        }else{
            return first_name?.plus(" $last_name")
        }
    }
}

/*@Parcelize
data class ExtraInfo(
    var name : Name? = null,
    var rating : Float? = null,
    val featured_image_url : String? = null,
    val unique_check_in_count : Int? = null,
    val id : String? = null,
    val timings : List<Timing>? = null,
    val gradient_type : String? =  null

):Parcelable*/
@Parcelize
data class ExtraInfo(
    @SerializedName("default")
    var default: @RawValue Any?,
    @SerializedName("media_video")
    var media_video: @RawValue Any?,
    @SerializedName("open_meetup")
    var open_meetup: OpenMeetUp?,
    @SerializedName("check_in")
    var check_in: CheckIn?,
    @SerializedName("text_post")
    var text_post: TextPost?


) : Parcelable

@Parcelize
data class OpenMeetUp(
    @SerializedName("chosen_place")
    val chosen_place: ChosenPlace?,
    @SerializedName("created_at")
    val created_at: String?,
    @SerializedName("date")
    val date: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("join_requests")
    val join_requests: JoinRequests?,
    @SerializedName("meetup_id")
    val meetup_id: String?,
    @SerializedName("join_requested_by_user")
    val join_requested_by_user: Boolean? = null,
    @SerializedName("join_accepted_by_user")
    val join_accepted_by_user: Boolean? = null,
    @SerializedName("name")
    val name: String?,
    @SerializedName("places")
    val places: List<Place>?,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("voting_closed")
    val voting_closed: Boolean?,
    @SerializedName("custom_places")
    val custom_places: List<AddressModel>?,
    @SerializedName("duration")
    val duration: String? = null,
    @SerializedName("max_join_time")
    val max_join_time : MaxJoinTime? = null,
    @SerializedName("interests")
    val interests: List<String>?= arrayListOf(),
    @SerializedName("min_badge")
    val min_badge:String? =null
) : Parcelable

@Parcelize
data class JoinRequests(
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("requests")
    val requests: List<Request?>
) : Parcelable

@Parcelize
data class Request(
    @SerializedName("meetup_id")
    val meetup_id: String? = null,
    @SerializedName("user_id")
    val user_id: String? = null,
    @SerializedName("user_meta")
    val user_meta: UserMeta? = null
) : Parcelable

@Parcelize
data class CheckIn(
    @SerializedName("featured_image_url")
    val featured_image_url: String?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: Name?,
    @SerializedName("rating")
    val rating: Float?,
    @SerializedName("timings")
    val timings: List<Timing>?,
    @SerializedName("unique_check_in_count")
    val unique_check_in_count: Int?
) : Parcelable

@Parcelize
data class TextPost(
    @SerializedName("gradient_type")
    var gradient_type: String?
) : Parcelable





