package com.meetsportal.meets.models

import android.os.Parcelable
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RestaurantTime(

    @SerializedName("boosted_views")
    var opentime: String?=null,

    @SerializedName("boosted_views")
    var closetime: String? = null
):Parcelable

@Parcelize
@IgnoreExtraProperties
data class Restaurant(

    @SerializedName("boosted_views")
    var id: String?=null,

    @SerializedName("boosted_views")
    var legacyId: String?=null,

    @SerializedName("boosted_views")
    var name: Map<String,String>?=null,

    @SerializedName("boosted_views")
    var description: Map<String,String>?=null,

    @SerializedName("boosted_views")
    var slug: String?=null,

    @SerializedName("boosted_views")
    var phone: String?=null,

    @SerializedName("boosted_views")
    var email: String?=null,

    @SerializedName("boosted_views")
    var postCode: String?=null,

    @SerializedName("boosted_views")
    var website: String?=null,

    @SerializedName("boosted_views")
    var city: String?=null,

    @SerializedName("boosted_views")
    var country: String?=null,

    @SerializedName("boosted_views")
    var state: String?=null,

    @SerializedName("boosted_views")
    var street: Map<String,String>?=null,

    @SerializedName("boosted_views")
    var street2: Map<String,String>?=null,

    @SerializedName("boosted_views")
    var timings: List<RestaurantTime>?=null,

    @SerializedName("boosted_views")
    var shishaPrice: Double?=null,

    @SerializedName("boosted_views")
    var primaryKindOfPlace: String?=null,

    @SerializedName("boosted_views")
    var cuisines: List<String>?=null,

    @SerializedName("boosted_views")
    var facilities: List<String>?=null,

    @SerializedName("boosted_views")
    var kindOfPlaces: List<String>?=null,

    @SerializedName("boosted_views")
    var ratingAverage: Float?=null,

    @SerializedName("boosted_views")
    var ratingCount: Float?=null,

    @SerializedName("boosted_views")
    var deliveryOnly: Boolean?=null,

    @SerializedName("boosted_views")
    var isBestPlace: Boolean?=null,

    @SerializedName("boosted_views")
    var isFeatured: Boolean?=null,

    @SerializedName("boosted_views")
    var location: Location?=null,

    @SerializedName("boosted_views")
    var gallery: List<RestaurantImage>?=null,

    @SerializedName("boosted_views")
    var menu: List<RestaurantImage>?=null,

    @SerializedName("boosted_views")
    var restaurantLogoUrl: String?=null,

    @SerializedName("boosted_views")
    var featuredImageUrl: String?=null,

    @SerializedName("boosted_views")
    var mainImageUrl: String?=null,

    @SerializedName("boosted_views")
    var mainWebImageUrl: String? = null
) : Parcelable{

    companion object{
        //var kindOfPlacesMap = mapOf("Coffee Shop-Café" to R.id.cofee_shop,"Lounge" to R.id.lounge,"bar_club" to R.id.bar_club,"Beach Club-Pool" to R.id.beach_pool,"Fine Dining" to R.id.fining_dining)
    }
}

@Parcelize
@IgnoreExtraProperties
data class RestaurantImage(

    @SerializedName("boosted_views")
    var imageUrl: String?=null,

    @SerializedName("boosted_views")
    var index: Int? = null): Parcelable

@Parcelize
@IgnoreExtraProperties
data class Location(

    @SerializedName("boosted_views")
    var latitude: Double?=null,

    @SerializedName("boosted_views")
    var longitude: Double? = null) :Parcelable




