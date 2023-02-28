package com.meetsportal.meets.networking.post

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.overridelayout.mention.MentionPerson
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class SinglePostResponse(
    @SerializedName("__v")
    var __v: Int,
    @SerializedName("_id")
    var _id: String,
    @SerializedName("body")
    var body: String,
    @SerializedName("mentions")
    var mentions : List<MentionPerson>? = null,
    @SerializedName("comments_enabled")
    var comments_enabled : Boolean,
    @SerializedName("comments")
    var comments: List<Comment>,
    @SerializedName("createdAt")
    var createdAt: String,
    @SerializedName("elastic_id")
    var elastic_id: String,
    @SerializedName("liked_by_user")
    var liked_by_user: Boolean,
    @SerializedName("likes_count")
    var likes_count: Int,
    @SerializedName("media")
    var media: List<String>,
    @SerializedName("type")
    var type : String?,
    @SerializedName("body_obj")
    var body_obj : ExtraInfo?,
    @SerializedName("stats")
    var stats: Stats? = null,
    @SerializedName("updatedAt")
    var updatedAt: String,
    @SerializedName("user")
    var user: UserXX,
    @SerializedName("user_id")
    var user_id: String,
    @SerializedName("user_meta")
    var user_meta: SinglePostUserMeta
):Parcelable
@Parcelize
data class Comment(
    @SerializedName("__v")
    var __v: Int,
    @SerializedName("_id")
    var _id: String,
    @SerializedName("body")
    var body: String,
    @SerializedName("comments")
    var comments: List<Comment>,
    @SerializedName("createdAt")
    var createdAt: String,
    @SerializedName("liked_by_user")
    var liked_by_user: Boolean,
    @SerializedName("likes_count")
    var likes_count: Int,
    @SerializedName("post_id")
    var post_id: String,
    @SerializedName("updatedAt")
    var updatedAt: String,
    @SerializedName("user")
    var user: UserX,
    @SerializedName("user_id")
    var user_id: String,
    @SerializedName("makeExpanded")
    var makeExpanded:Boolean? = false,
    @SerializedName("repliesId")
    var repliesId : String? = null,
    @SerializedName("start")
    var start : Int? = 0,
    @SerializedName("end")
    var end : Int? = 2

):Parcelable
@Parcelize
data class UserXX(
    @SerializedName("_id")
    var _id: String,
    @SerializedName("first_name")
    var first_name: String,
    @SerializedName("last_name")
    var last_name: String,
    @SerializedName("profile_image_url")
    var profile_image_url: String,
    @SerializedName("sid")
    var sid: String,
    @SerializedName("username")
    var username: String,
    @SerializedName("verified_user")
    var verified_user: Boolean

):Parcelable
@Parcelize
data class SinglePostUserMeta(
    @SerializedName("_id")
    var _id: String,
    @SerializedName("document_stage_status")
    var document_stage_status: DocumentStageStatus,
    @SerializedName("email")
    var email: String,
    @SerializedName("emergency_contact")
    var emergency_contact: String,
    @SerializedName("first_name")
    var first_name: String,
    @SerializedName("interests")
    var interests: @RawValue List<Any>,
    @SerializedName("last_name")
    var last_name: String,
    @SerializedName("lite_profile_image_url")
    var lite_profile_image_url: String,
    @SerializedName("location_check_in_enabled")
    var location_check_in_enabled: Boolean,
    @SerializedName("location_meet_up_enabled")
    var location_meet_up_enabled: Boolean,
    @SerializedName("phone_number")
    var phone_number: String,
    @SerializedName("profile_image_url")
    var profile_image_url: String,
    @SerializedName("settings")
    var settings: Settings,
    @SerializedName("sid")
    var sid: String,
    @SerializedName("username")
    var username: String,
    @SerializedName("verified_user")
    var verified_user: Boolean,
    @SerializedName("badge")
    var badge: String

):Parcelable

/*data class CommentX(
    var __v: Int,
    var _id: String,
    var body: String,
    var comments: List<String>,
    var createdAt: String,
    var liked_by_user: Boolean,
    var likes_count: Int,
    var parent_id: String,
    var post_id: String,
    var updatedAt: String,
    var user: User,
    var user_id: String
)*/
@Parcelize
data class UserX(
    @SerializedName("_id")
    var _id: String,
    @SerializedName("first_name")
    var first_name: String,
    @SerializedName("last_name")
    var last_name: String,
    @SerializedName("profile_image_url")
    var profile_image_url: String,
    @SerializedName("sid")
    var sid: String,
    @SerializedName("username")
    var username: String,
    @SerializedName("verified_user")
    var verified_user: Boolean,
    @SerializedName("badge")
    var badge: String
):Parcelable

data class User(
    @SerializedName("_id")
    var _id: String,
    @SerializedName("first_name")
    var first_name: String,
    @SerializedName("last_name")
    var last_name: String,
    @SerializedName("profile_image_url")
    var profile_image_url: String,
    @SerializedName("sid")
    var sid: String,
    @SerializedName("username")
    var username: String,
    @SerializedName("verified_user")
    var verified_user: Boolean
)
@Parcelize
data class DocumentStageStatus(
    @SerializedName("approved_documents")
    var approved_documents: @RawValue List<Any>,
    @SerializedName("completed")
    var completed: Boolean,
    @SerializedName("missing_documents")
    var missing_documents: @RawValue List<Any>,
    @SerializedName("pending_verifications")
    var pending_verifications: List<String>,
    @SerializedName("rejected_documents")
    var rejected_documents: @RawValue List<Any>
):Parcelable
@Parcelize
data class Settings(
    @SerializedName("dark_mode_enabled")
    var dark_mode_enabled: Boolean,
    @SerializedName("followings_posts_enabled")
    var followings_posts_enabled: Boolean,
    @SerializedName("language")
    var language: String,
    @SerializedName("notifications_enabled")
    var notifications_enabled: Boolean,
    @SerializedName("public_posts_enabled")
    var public_posts_enabled: Boolean,
    @SerializedName("show_interests")
    var show_interests: Boolean
):Parcelable