package com.meetsportal.meets.networking.places

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meetsportal.meets.models.MeetsPlace
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetSinglePlaceResponse(
    @SerializedName("__v")
    var __v: Int? = null,
    @SerializedName("_id")
    var _id: String? = null,
    /*@SerializedName("bookings")
    var bookings: List<Any>? = null,*/
    /*@SerializedName("check_ins")
    var check_ins: List<Any>? = null,*/
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
    @SerializedName("elastic_id")
    var elastic_id: String? = null,
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
    @SerializedName("latest_reviews")
    var latest_reviews: List<PlaceReviewListItem>? = null,
    @SerializedName("location")
    var location: Location? = null,
    @SerializedName("main_image_url")
    var main_image_url: String? = null,
    @SerializedName("main_web_image_url")
    var main_web_image_url: String? = null,
    @SerializedName("menus")
    var menus: List<Gallery>? = null,
    @SerializedName("name")
    var name: Name? = null,
    /*@SerializedName("offers")
    var offers: List<Any>? = null,*/
    @SerializedName("phone")
    var phone: String? = null,
    @SerializedName("primary_kind_of_place")
    var primary_kind_of_place: List<String>? = null,
    @SerializedName("rating")
    var rating: Double? = null,
    /*@SerializedName("reviews")
    var reviews: List<Any>? = null,*/
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
    @SerializedName("unique_check_in_count")
    var unique_check_in_count : Int? = null,
    @SerializedName("bookmarked")
    var bookmarked: Boolean? = null,
):Parcelable{
    fun toMeetUpPlace(): MeetsPlace {
        return MeetsPlace(_id,name,false,featured_image_url?:main_image_url,primary_kind_of_place,timings,
            street?.en?:"".plus(city?:"")?.plus(state?:"").plus(country?:""))
    }
}


class PlaceReviewList : ArrayList<PlaceReviewListItem>()

@Parcelize
data class PlaceReviewListItem(
    @SerializedName("review")
    var review: Review,
    @SerializedName("user")
    var user: User
):Parcelable

@Parcelize
data class Review(
    @SerializedName("__v")
    var __v: Int,
    @SerializedName("_id")
    var _id: String,
    @SerializedName("body")
    var body: String,
    @SerializedName("createdAt")
    var createdAt: String,
    @SerializedName("place_id")
    var place_id: String,
    @SerializedName("rating")
    var rating: Int,
    @SerializedName("updatedAt")
    var updatedAt: String,
    @SerializedName("user_id")
    var user_id: String
):Parcelable

@Parcelize
data class User(
    @SerializedName("_id")
    var _id: String,
    @SerializedName("document_stage_status")
    var document_stage_status: DocumentStageStatus,
    @SerializedName("email")
    var email: String,
    @SerializedName("first_name")
    var first_name: String,
    @SerializedName("interests")
    var interests: List<Interest>,
    @SerializedName("last_name")
    var last_name: String,
    @SerializedName("lite_profile_image_url")
    var lite_profile_image_url: String,
    @SerializedName("location")
    var location: Location,
    @SerializedName("location_check_in_enabled")
    var location_check_in_enabled: Boolean,
    @SerializedName("location_meet_up_enabled")
    var location_meet_up_enabled: Boolean,
    @SerializedName("phone_number")
    var phone_number: String,
    @SerializedName("profile_image_url")
    var profile_image_url: String,
    @SerializedName("settings")
    var settings: Settings,
    @SerializedName("sid")
    var sid: String,
    @SerializedName("username")
    var username: String,
    @SerializedName("badge")
    var badge: String,
    @SerializedName("verified_user")
    var verified_user: Boolean

):Parcelable

@Parcelize
data class DocumentStageStatus(
    @SerializedName("approved_documents")
    var approved_documents: List<String>,
    @SerializedName("completed")
    var completed: Boolean,
    @SerializedName("missing_documents")
    var missing_documents: List<String>,
    @SerializedName("pending_verifications")
    var pending_verifications: List<String>,
    @SerializedName("rejected_documents")
    var rejected_documents: List<String>
):Parcelable

@Parcelize
data class Interest(
    @SerializedName("en")
    var en: String,
    @SerializedName("icon")
    var icon: String,
    @SerializedName("key")
    var key: String
):Parcelable


@Parcelize
data class Settings(
    @SerializedName("dark_mode_enabled")
    var dark_mode_enabled: Boolean,
    @SerializedName("followings_posts_enabled")
    var followings_posts_enabled: Boolean,
    @SerializedName("language")
    var language: String,
    @SerializedName("notifications_enabled")
    var notifications_enabled: Boolean,
    @SerializedName("public_posts_enabled")
    var public_posts_enabled: Boolean,
    @SerializedName("show_interests")
    var show_interests: Boolean
):Parcelable

