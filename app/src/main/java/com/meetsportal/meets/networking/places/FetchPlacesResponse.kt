package com.meetsportal.meets.networking.places

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.models.MeetsPlace
import kotlinx.android.parcel.Parcelize

class FetchPlacesResponse : ArrayList<FetchPlacesResponseItem>()

@Parcelize
data class FetchPlacesResponseItem(
    @SerializedName("__v")
    var __v: Int? = null,
    @SerializedName("_id")
    var _id: String? = null,
    // Any to String
    @SerializedName("bookings")
    var bookings: List<String>? = null,

    // Any to String
    @SerializedName("check_ins")
    var check_ins: List<String>? = null,
    @SerializedName("city")
    var city: String? = null,
    @SerializedName("country")
    var country: String? = null,
    @SerializedName("country_code")
    var country_code: String? = null,
    @SerializedName("createdAt")
    var createdAt: String? = null,
    @SerializedName("cuisines")
    var cuisines: List<String>? = null,
    @SerializedName("delivery_only")
    var delivery_only: Boolean? = null,
    @SerializedName("description")
    var description: Description? = null,
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("facilities")
    var facilities: List<String>? = null,
    @SerializedName("featured_image_url")
    var featured_image_url: String? = null,
    @SerializedName("gallery")
    var gallery: List<Gallery>? = null,
    @SerializedName("is_best_place")
    var is_best_place: Boolean? = null,
    @SerializedName("is_bookable")
    var is_bookable: Boolean? = null,
    @SerializedName("is_featured")
    var is_featured: Boolean? = null,
    @SerializedName("is_meetable")
    var is_meetable: Boolean? = null,
    @SerializedName("kind_of_places")
    var kind_of_places: List<String>? = null,

    // Any to String
   // var latest_reviews: List<String>? = null,
    @SerializedName("location")
    var location: Location? = null,
    @SerializedName("main_image_url")
    var main_image_url: String? = null,
    @SerializedName("main_web_image_url")
    var main_web_image_url: String? = null,

    // Any to String
    @SerializedName("menus")
    var menus: List<Gallery>? = null,
    @SerializedName("name")
    var name: Name? = null,

    // Any to String
    @SerializedName("offers")
    var offers: List<String>? = null,
    @SerializedName("phone")
    var phone: String? = null,
    @SerializedName("primary_kind_of_place")
    var primary_kind_of_place: List<String>? = null,

    // Any to String
    @SerializedName("rating")
    var rating: Float? = null,

    // Any to String
    @SerializedName("reviews")
    var reviews: List<String>? = null,
    @SerializedName("slug")
    var slug: String? = null,
    @SerializedName("state")
    var state: String? = null,
    @SerializedName("street")
    var street : Name? = null,
    @SerializedName("timings")
    var timings: List<Timing>? = null,
    @SerializedName("updatedAt")
    var updatedAt: String? = null,
    @SerializedName("selected")
    var selected :Boolean = false

): Parcelable{

    fun toSearchPlace(): SearchPlaceResponseItem {
        return SearchPlaceResponseItem(_idLocal = _id,featured_image_url = featured_image_url,
                main_image_url=main_image_url,slug=slug, name_en=name?.en,timings = timings,
                kind_of_places = kind_of_places ,rating = rating?.toDouble(),offers_count = offers?.size, of_en = offers)
    }
    fun toMeetUpPlace():MeetsPlace{
        return MeetsPlace(_id,name,false,featured_image_url?:main_image_url,primary_kind_of_place,timings,
            street?.en?:"".plus(city?:"").plus(state?:"").plus(country?:""))
    }

}

@Parcelize
@IgnoreExtraProperties
data class Description(
    @SerializedName("ar")
    var ar: String? = null,
    @SerializedName("en")
    var en: String? = null,
    @SerializedName("fr")
    var fr: String? = null
): Parcelable

@Parcelize
data class Gallery(
    @SerializedName("imageUrl")
    var imageUrl: String? = null,
    @SerializedName("index")
    var index: Int? = null
): Parcelable

@Parcelize
data class Location(
    @SerializedName("coordinates")
    var coordinates: List<Double>? = null,
    @SerializedName("type")
    var type: String? = null
): Parcelable

@Parcelize
data class Name(
    @SerializedName("ar")
    var ar: String? = null,
    @SerializedName("en")
    var en: String? = null,
    @SerializedName("fr")
    var fr: String? = null
): Parcelable

@Parcelize
data class Timing(
    @SerializedName("closetime")
    var closetime: String? = null,
    @SerializedName("opentime")
    var opentime: String? = null
): Parcelable