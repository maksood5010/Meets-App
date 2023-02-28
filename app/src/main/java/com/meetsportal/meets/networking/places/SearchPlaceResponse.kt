package com.meetsportal.meets.networking.places

import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.models.MeetsPlace

class SearchPlaceResponse : ArrayList<SearchPlaceResponseItem>()

data class SearchPlaceResponseItem(

    @SerializedName("__v")
    val __v: Int? = null,
    @SerializedName("_idLocal")
    val _idLocal:String? = null,
    @SerializedName("bookings_count")
    val bookings_count: Int? = null,
    @SerializedName("check_ins_count")
    val check_ins_count: Int? = null,
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("country")
    val country: String? = null,
    @SerializedName("country_code")
    val country_code: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("cuisines")
    val cuisines: List<String>? = null,
    @SerializedName("delivery_only")
    val delivery_only: Boolean? = null,
    @SerializedName("description_ar")
    val description_ar: String? = null,
    @SerializedName("description_en")
    val description_en: String? = null,
    @SerializedName("description_fr")
    val description_fr: String? = null,
    @SerializedName("elastic_id")
    val elastic_id: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("facilities")
    val facilities: List<String>? = null,
    @SerializedName("featured_image_url")
    val featured_image_url: String?? = null,
    @SerializedName("gallery_count")
    val gallery_count: Int? = null,
    @SerializedName("is_best_place")
    val is_best_place: Boolean? = null,
    @SerializedName("is_bookable")
    val is_bookable: Boolean? = null,
    @SerializedName("is_featured")
    val is_featured: Boolean? = null,
    @SerializedName("is_meetable")
    val is_meetable: Boolean? = null,
    @SerializedName("kind_of_places")
    val kind_of_places: List<String>? = null,
    //val latest_reviews: List<String>? = null,
    @SerializedName("main_image_url")
    val main_image_url: String? = null,
    @SerializedName("main_web_image_url")
    val main_web_image_url: String? = null,
    @SerializedName("menus_count")
    val menus_count: Int? = null,
    @SerializedName("name_ar")
    val name_ar: String? = null,
    @SerializedName("name_en")
    val name_en: String? = null,
    @SerializedName("name_fr")
    val name_fr: String? = null,
    @SerializedName("of_ar")
    val of_ar: List<String>? = null,
    @SerializedName("of_en")
    val of_en: List<String>? = null,
    @SerializedName("of_fr")
    val of_fr: List<String>? = null,
    @SerializedName("offers_count")
    val offers_count: Int? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("primary_kind_of_place")
    val primary_kind_of_place: List<String>? = null,
    @SerializedName("rating")
    val rating: Double? = null,
    @SerializedName("reviews_count")
    val reviews_count: Int? = null,
    @SerializedName("slug")
    val slug: String? = null,
    @SerializedName("state")
    val state: String? = null,
    @SerializedName("timings")
    val timings: List<Timing>? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("selected")
    var selected : Boolean = false
){
    fun toMeetUpPlace():MeetsPlace{
        return MeetsPlace(elastic_id,Name(name_ar,name_en,name_fr),true,featured_image_url?:main_image_url,primary_kind_of_place,
            timings,"".plus(city).plus(" ").plus(state).plus(" ").plus(country),rating)
    }
}

data class Timings(
    @SerializedName("closetime")
    val closetime: String? = null,
    @SerializedName("opentime")
    val opentime: String? = null
)