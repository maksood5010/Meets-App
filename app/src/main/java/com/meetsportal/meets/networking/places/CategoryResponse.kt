package com.meetsportal.meets.networking.places

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CategoryResponse(
    @SerializedName("__v")
    var __v: Int? = null,
    @SerializedName("_id")
    var _id: String? = null,
    @SerializedName("createdAt")
    var createdAt: String? = null,
    @SerializedName("definition")
    var definition: List<Category>? = null,
    @SerializedName("entity_id")
    var entity_id: String? = null,
    @SerializedName("group_id")
    var group_id: String? = null,
    @SerializedName("regenerate")
    var regenerate: Boolean? = null,
    @SerializedName("updatedAt")
    var updatedAt: String? = null
):Parcelable


@Parcelize
data class Category(
    @SerializedName("en")
    var en: String? = null,
    @SerializedName("key")
    var key: String? = null,
    @SerializedName("svg_url")
    var svg_url: String? = null,
    @SerializedName("color_svg_url")
    var color_svg_url :String? = null,
    @SerializedName("color_png_url")
    var color_png_url : String? = null,
    @SerializedName("isSelected")
    var isSelected :Boolean = false,
    @SerializedName("image_url")
    var image_url : String? = null,
    @SerializedName("place_id")
    var place_id : String? = null,

):Parcelable