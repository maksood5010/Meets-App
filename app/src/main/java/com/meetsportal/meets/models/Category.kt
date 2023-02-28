package com.meetsportal.meets.models

import com.google.gson.annotations.SerializedName


enum class FilterType {
    cuisine,
    facility,
    kindOfPlace,
    delivery,
}

/*data class Category (
    var label:String,
    var icon: String,
    var filterType: FilterType,
    var filterValue: String)*/


//above is permaanant below is temporary
data class Category (
    @SerializedName("label")
    var label:String,
    @SerializedName("icon")
    var icon: Int,
    @SerializedName("filterType")
    var filterType: FilterType,
    @SerializedName("filterValue")
    var filterValue: String)

