package com.meetsportal.meets.networking.meetup

import com.google.gson.annotations.SerializedName


class MaatPlaceVoting( val map:Map<String,Any?> , sid : String?){
    @SerializedName("sid")
    val sid : String? = sid
    val id by map
    val timestamp by map
    val type by map
    var animSeen :Boolean? = map["animSeen"] as Boolean?

}

/*
data class Votes(
    val id : String? = null,
    val timestamp: Timestamp? =  null,
    val type : String? = null
)
*/


