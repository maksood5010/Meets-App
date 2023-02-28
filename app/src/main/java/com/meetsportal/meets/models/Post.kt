package com.meetsportal.meets.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@IgnoreExtraProperties
data class Post(

    @SerializedName("id")
    var id: String? = null,

    @SerializedName("user")
    var user : UserProfile? = null,

    @SerializedName("type")
    var type: String? = "USER",

    @SerializedName("images")
    var images : List<Map<String,String>>? = null,

    @SerializedName("videos")
    var videos: List<String>? = null,

    @SerializedName("mediaType")
    var mediaType: String? = null,

    @SerializedName("caption")
    var caption: String? = null,

    @SerializedName("comments")
    var comments: List<Comment>? = null,

    @SerializedName("likeCount")
    var likeCount : Int? = null,

    @SerializedName("commentCount")
    var commentCount: Int? = null,

    @SerializedName("createdOn")
    var createdOn: Timestamp? = null,

) : Parcelable{

    override fun equals(other: Any?): Boolean {
        if(javaClass != other?.javaClass)
            return false

       other as Post
        if(id.equals(other.id))
            return false

        if(!user?.equals(other.user)!!)
            return false

        if(!type.equals(other.type))
            return false

        if(!caption.equals(other.caption))
            return false

        if(likeCount != other.likeCount )
            return false

        if(commentCount != other.commentCount )
            return false

        return true
    }

}