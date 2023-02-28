package com.meetsportal.meets.models

import com.google.gson.annotations.SerializedName

data class FriendsSuggestion (

    @SerializedName("id")
    var id: String?,

    @SerializedName("name")
    var name: String?,

    @SerializedName("followers")
    var followers: Int?,

    @SerializedName("imageUrl")
    var imageUrl: String?,

    @SerializedName("verified")
    var verified: Boolean?,
)