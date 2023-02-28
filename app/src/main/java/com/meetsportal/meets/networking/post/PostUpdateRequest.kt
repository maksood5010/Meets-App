package com.meetsportal.meets.networking.post

import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.overridelayout.mention.MentionPerson

data class PostUpdateRequest(
    @SerializedName("body")
    val body :String? = null,
    @SerializedName("mentions")
    val mentions : List<MentionPerson>? = null,
    @SerializedName("comments_enabled")
    val comments_enabled :Boolean? = null
)
