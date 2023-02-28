package com.meetsportal.meets.utils

import com.google.maps.GeoApiContext
import com.google.maps.PlacesApi
import com.google.maps.errors.ApiException
import com.google.maps.model.LatLng
import com.google.maps.model.PlacesSearchResponse
import com.google.maps.model.RankBy
import java.io.IOException

class NearBySearch {
    var API_KEY: String? = null
    private var location: LatLng? = null
    var lan: String? = null
    fun setValues(api: String?, loc: LatLng, language: String?) {

        API_KEY = api
        location = loc
        lan = language
    }

    fun run(): PlacesSearchResponse {
        var request = PlacesSearchResponse()
        val context = GeoApiContext.Builder()
            .apiKey(API_KEY)
            .build()
        try {
            request = PlacesApi.nearbySearchQuery(context, location)
//                .radius(500)
//                .type(PlaceType.PARK,PlaceType.TOURIST_ATTRACTION,PlaceType.ESTABLISHMENT,PlaceType.SHOPPING_MALL)
                .rankby(RankBy.DISTANCE)
                .keyword("nearby")
                .language(lan)
                .await()
        } catch (e: ApiException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            return request
        }
    }
}

/*
.type(PlaceType.REAL_ESTATE_AGENCY,
PlaceType.ATM,
PlaceType.BAKERY,
PlaceType.BANK,
PlaceType.BUS_STATION,
PlaceType.CAFE,
PlaceType.CAR_DEALER,
PlaceType.CAR_RENTAL,
PlaceType.CAR_REPAIR,
PlaceType.CAR_WASH,
PlaceType.CLOTHING_STORE,
PlaceType.CONVENIENCE_STORE,
PlaceType.DEPARTMENT_STORE,
PlaceType.EMBASSY,
PlaceType.GAS_STATION,
PlaceType.HOSPITAL,
PlaceType.LAUNDRY,
PlaceType.RESTAURANT,
PlaceType.GROCERY_OR_SUPERMARKET,
PlaceType.SUPERMARKET,
PlaceType.MOSQUE,
PlaceType.NIGHT_CLUB,
PlaceType.PARKING,
PlaceType.POST_OFFICE,
PlaceType.RV_PARK,
PlaceType.SCHOOL,
PlaceType.STADIUM,
PlaceType.TAXI_STAND,
PlaceType.ACCOUNTING,
PlaceType.GYM)*/
