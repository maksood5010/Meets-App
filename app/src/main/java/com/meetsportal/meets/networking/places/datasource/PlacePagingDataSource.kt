package com.meetsportal.meets.networking.places.datasource

import android.location.Location
import android.util.Log
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.google.gson.JsonObject
import com.meetsportal.meets.networking.places.FetchPlacesResponse
import com.meetsportal.meets.networking.places.FetchPlacesResponseItem

import com.meetsportal.meets.repository.AppRepository
import com.meetsportal.meets.utils.Constant.LATITUDE
import com.meetsportal.meets.utils.Constant.LONGITUDE
import com.meetsportal.meets.utils.Constant.PREFRENCE_LOCATION
import com.meetsportal.meets.utils.PreferencesManager
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.lang.StringBuilder

class PlacePagingDataSource(val service: AppRepository) :
    RxPagingSource<Int, FetchPlacesResponseItem>() {

    private val TAG = PlacePagingDataSource::class.java.simpleName


    var facilities : StringBuilder?  = null
    var cuisine : StringBuilder? = null
    var categories : StringBuilder? = null
    var isBestPlaces : Boolean? = null
    var countryCode : String? = null

    fun setfilter(facilities :StringBuilder,cuisine : StringBuilder,categories:StringBuilder?,isBestPlaces : Boolean?, countryCode: String?){
        Log.i(TAG,"  setfilter:: $categories")
        this.facilities = facilities
        this.cuisine = cuisine
        this.categories = categories
        this.isBestPlaces = isBestPlaces
        this.countryCode = countryCode

    }

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, FetchPlacesResponseItem>> {

        val page = params.key ?: 1

        Log.i(TAG," locations::: ${PreferencesManager.get<JsonObject>(PREFRENCE_LOCATION)?.get(LATITUDE)?.asDouble} ")
        Log.i(TAG," categories.toStri::  $categories")

        PreferencesManager.get<Location>(PREFRENCE_LOCATION)
        return service.getBestMeetUpPage(
            10,
            PreferencesManager.get<JsonObject>(PREFRENCE_LOCATION)?.get(LATITUDE)?.asDouble,
            PreferencesManager.get<JsonObject>(PREFRENCE_LOCATION)?.get(LONGITUDE)?.asDouble,
            isBestPlaces, page,
            this.facilities.toString(),
            this.categories.toString(),
            this.cuisine.toString(),
            countryCode

        )
            .subscribeOn(Schedulers.io())
            .map { toLoadResult(it, page) }
            .onErrorReturn {
                LoadResult.Error(it)
            }
    }

    private fun toLoadResult(
        data: FetchPlacesResponse,
        page: Int
    ): LoadResult<Int, FetchPlacesResponseItem> {

        return LoadResult.Page(
            data = data,
            prevKey = if (page <= 1) null else page - 1,
            //nextKey = if (page >= totalPage!!) null else page + 1
            nextKey = if (data.size == 0) null else page + 1
        )
    }


    override fun getRefreshKey(state: PagingState<Int, FetchPlacesResponseItem>): Int? {
        Log.i("TAG", " getRefreshKey:: ${state.anchorPosition}")
        return state.anchorPosition
    }
}