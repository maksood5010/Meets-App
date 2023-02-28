package com.meetsportal.meets.networking.places

import com.google.gson.JsonObject
import com.meetsportal.meets.networking.Api
import com.meetsportal.meets.networking.ApiClient
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceService @Inject constructor(){

    private var api : Api = ApiClient.getRetrofit()


    fun getBestMeetUp(limit: Int?,lat :Double?,long:Double?,isbestPlace:Boolean?,fields:String?,maxDistence : Int?,filter:String? = null): Flowable<Response<FetchPlacesResponse>> {
        return api.getBestMeetUp(limit,lat,long,isbestPlace,fields = fields,maxDistence = maxDistence,filter = filter)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun getBestMeetUpPage(limit: Int?,lat :Double?,long:Double?,countryCode:String?,isbestPlace:Boolean?,page:Int
                          ,facilities:String?
                          ,categories:String?
                          ,cuisines:String?): Single<FetchPlacesResponse> {
        return api.getBestMeetUpPage(limit,lat,long,isbestPlace,page = page,
            countryCode= countryCode
            ,facilities = facilities
            ,categories = categories
            , cuisines = cuisines)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getSavedPlaces(page: Int,limit : Int):Single<FetchPlacesResponse>{
        return api.getSavedPlaces(page,limit)
    }

    fun getNearByPlaceCount(lat :Double,long:Double): Flowable<Response<Int?>> {
        return api.getNearByPlaceCount(lat,long)
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }



    fun getCategories(): Flowable<Response<CategoryResponse>> {
        return api.getCategories()
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getOffers(): Flowable<Response<CategoryResponse>> {
        return api.getOffers()
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getFacilities(): Flowable<Response<FacilityResponse>> {
        return api.getFacilities()
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getCuisine(): Flowable<Response<CuisineResponse>> {
        return api.getCuisine()
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun searchPlace(searchString:String?):Flowable<Response<SearchPlaceResponse>>{
        return api.searchPlace(searchString)
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun getPlace(id:String?,mode:String?):Flowable<Response<GetSinglePlaceResponse>>{
        return api.getPlace(id,mode)
            .toFlowable(BackpressureStrategy.BUFFER)
    }


    fun reviewPlace(id:String?,request:JsonObject):Flowable<Response<JsonObject>>{
        return api.reviewPlace(id,request)
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun editReview(placeId:String?,reviewId:String?,request:JsonObject):Flowable<Response<JsonObject>>{
        return api.editReview(placeId,reviewId,request)
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun checkInPlace(placeId:String?,request:JsonObject):Flowable<Response<JsonObject>>{
        return api.checkInPlace(placeId,request)
            .toFlowable(BackpressureStrategy.BUFFER)
    }
    fun addBookmark(placeId:String?):Flowable<Response<JsonObject?>>{
        return api.addBookmark(placeId)
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun deleteBookmark(placeId:String?):Flowable<Response<JsonObject?>>{
        return api.deleteBookmark(placeId)
            .toFlowable(BackpressureStrategy.BUFFER)
    }



    fun getMyReview(placeId:String?,userId:String?):Flowable<Response<FetchReviewResponse?>>{
        return api.getMyReview(placeId,userId)
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun getReviewCount(placeId:String?):Flowable<Response<JsonObject?>>{
        return api.getReviewCount(placeId)
            .toFlowable(BackpressureStrategy.BUFFER)
    }



    fun getMyReviewSingle(placeId:String?,page:Int):Single<FetchReviewResponse?>{
        return api.getMyReviewSingle(placeId,page)

    }






}