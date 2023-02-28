package com.meetsportal.meets.models

import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.networking.places.Name
import com.meetsportal.meets.networking.places.Timing

data class MeetsPlace (
    @SerializedName("id")
    val id : String?,
    @SerializedName("name")
    val name : Name?,
    @SerializedName("isElastic")
    val isElastic : Boolean,
    @SerializedName("imageUrl")
    val imageUrl : String?,
    @SerializedName("primary_kind_place")
    val primary_kind_place : List<String>? = null,
    @SerializedName("timing")
    val timing : List<Timing>? = null,
    @SerializedName("address")
    val address : String? = null,
    @SerializedName("rating")
    var rating: Double? = null,
)