package com.meetsportal.meets.networking.post

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/*class SinglePostComments : ArrayList<SinglePostCommentsItem>()*/

@Parcelize
data class SinglePostCommentsItem(
    @SerializedName("__v")
    val __v: Int,
    @SerializedName("_id")
    val _id: String,
    @SerializedName("body")
    val body: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("liked_by_user")
    val liked_by_user: Boolean,
    @SerializedName("parent_id")
    val parent_id: String,
    @SerializedName("post_id")
    val post_id: String,
    @SerializedName("stats")
    val stats: Stats,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("user_id")
    val user_id: String,
    @SerializedName("user_meta")
    val user_meta: UserMeta
): Parcelable

@Parcelize
data class SinglePostComments(
    @SerializedName("comments")
    val comments: List<SinglePostCommentsItem>,
    @SerializedName("parent_comment")
    val parent_comment: SinglePostCommentsItem
):Parcelable

/*data class SinglePostCommentsItem(
    val __v: Int,
    val _id: String,
    val body: String,
    val createdAt: String,
    val liked_by_user: Boolean,
    val parent_id: String,
    val post_id: String,
    val stats: Stats,
    val updatedAt: String,
    val user_id: String,
    val user_meta: UserMeta
)*/
