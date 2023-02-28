package com.meetsportal.meets.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.*
import androidx.paging.rxjava2.flowable
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.meetsportal.meets.adapter.SavedPlacePagingDataSource
import com.meetsportal.meets.models.Category
import com.meetsportal.meets.models.Restaurant
import com.meetsportal.meets.networking.ErrorResponse
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.exception.ApiException
import com.meetsportal.meets.networking.places.*
import com.meetsportal.meets.networking.places.datasource.PlacePagingDataSource
import com.meetsportal.meets.networking.places.datasource.ReviewPagingDatasource
import com.meetsportal.meets.repository.AppRepository
import com.meetsportal.meets.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.StringBuilder
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class PlacesViewModel @Inject constructor(var repository: AppRepository) :
    ViewModel() {

    private val TAG = PlacesViewModel::class.java.simpleName

    private val disposable = CompositeDisposable()
    private val initRestaurant: MediatorLiveData<ArrayList<Restaurant>?> =
        MediatorLiveData<ArrayList<Restaurant>?>()
    private val onBestPlaces: MediatorLiveData<FetchPlacesResponse> = MediatorLiveData()
    private val nearPlaceCount: MediatorLiveData<Int?> = MediatorLiveData()
    private val onNearByPlaces: MediatorLiveData<FetchPlacesResponse> = MediatorLiveData()
    private lateinit var newrByPlaceDisposable: Disposable

    private val onCategory: MediatorLiveData<CategoryResponse> = MediatorLiveData()
    private val onOffers: MediatorLiveData<CategoryResponse> = MediatorLiveData()
    private val onFacility: MediatorLiveData<FacilityResponse> = MediatorLiveData()
    private val onCuisine: MediatorLiveData<CuisineResponse> = MediatorLiveData()
    private val onPlaceSearch: MediatorLiveData<SearchPlaceResponse> = MediatorLiveData()
    private val onSavedPlace: MediatorLiveData<FetchPlacesResponse> = MediatorLiveData()
    private var onSinglePlace: MediatorLiveData<ResultHandler<GetSinglePlaceResponse?>> = MediatorLiveData()
    private var onPlaceChekIn: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var onBookmarkAdd: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var onBookmarkDelete: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var onMyReview: MediatorLiveData<FetchReviewResponse?> = MediatorLiveData()
    private var onReviewCount: MediatorLiveData<Int?> = MediatorLiveData()
    private var onReviewPlace: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()

    private val placesPagingdata: MediatorLiveData<PagingData<FetchPlacesResponseItem>> = MediatorLiveData()
    private val reviewPagingdata: MediatorLiveData<PagingData<FetchReviewResponseItem>> = MediatorLiveData()


    private val errorResponse: MutableLiveData<ErrorResponse?> = MutableLiveData()


    private val onLatLong : MutableLiveData<LatLng?> = MutableLiveData()


    init {
        //loadRetaurant()
//        loadCategory()
    }


    /*fun getAllOfferCard() : MutableLiveData<ArrayList<Offer>>? {
        Log.i("DashboardViewModel"," getAllOfferCard:: ")
        return repository.getOfferListMutableLivedata()
    }*/

    fun getAllCategoryCard(): MutableLiveData<ArrayList<Category>>? {
        Log.i("DashboardViewModel", " getAllCategoryCard:: ")
        return repository.getCategoryListMutableLivedata()
    }


    fun getInitRestaurantInfo(): LiveData<ArrayList<Restaurant>?> {
        return initRestaurant
    }

    /*fun loadRetaurant(){
        repository.getBestMeetUpInitMutableLivedata()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.toObservable()
            ?.subscribe(object : Observer<QuerySnapshot?> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onNext(querySnapshot: QuerySnapshot) {
                    var postArray:ArrayList<Restaurant>? = ArrayList<Restaurant>()
                    for(doc in querySnapshot){
                        //Log.i(TAG," document:: $doc ")

                        postArray?.add(doc.toObject(Restaurant::class.java)!!)
                    }
                    initRestaurant.value = postArray
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onError: $e")
                }

                override fun onComplete() {
                    ////TODO("Not yet implemented")
                }
            })
    }*/

    /*fun loadCategory(){
        repository.getCategoryListMutableLivedata()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.toObservable()
            ?.subscribe(object : Observer<QuerySnapshot?> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onNext(querySnapshot: QuerySnapshot) {
                    var postArray:ArrayList<Restaurant>? = ArrayList<Restaurant>()
                    for(doc in querySnapshot){
                        Log.i(TAG," document:: $doc ")

                        postArray?.add(doc.toObject(Restaurant::class.java)!!)
                    }
                    initRestaurant.value = postArray
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onError: $e")
                }

                override fun onComplete() {
                    ////TODO("Not yet implemented")
                }
            })
    }*/


    fun getBestMeetUp(limit: Int?, lat: Double?, long: Double?, isbestPlace: Boolean) {
        disposable.add(
            repository.getBestMeetUp(limit, lat, long, isbestPlace, 1)
                .subscribe({
                    Log.i(TAG, " checkingMeetup: $it")
                    if (it.isSuccessful)
                        onBestPlaces.value = it.body()
                }, {
                    Log.e(TAG, " fetching BestMeetup failed $it")
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun getNearByPlaceCount(lat: Double, long: Double) {
        disposable.add(
            repository.getNearByPlaceCount(lat, long)
                .subscribe({
                    Log.i(TAG, " checkingMeetup: $it")
                    if (it.isSuccessful)
                        nearPlaceCount.value = it.body()
                }, {
                    Log.e(TAG, " fetching BestMeetup failed $it")
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }


    fun getCloseByPlaceUp(limit: Int?, lat: Double?, long: Double?, isbestPlace: Boolean?,fields:String? = null,maxDistance : Int? = null,filter:String? = null) {
        Log.i(TAG," CheckingLATLONG::: $lat  $long")

        newrByPlaceDisposable = repository.getBestMeetUp(limit, lat, long, isbestPlace, 1,fields, maxDistence = maxDistance,filter = filter)
                .subscribe({
                    Log.i(TAG, " checkingMeetup: $it")
                    if (it.isSuccessful)
                        onNearByPlaces.value = it.body()
                }, {

                    Log.e(TAG, " fetching BestMeetup failed ${it.printStackTrace()}")
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        disposable.add(newrByPlaceDisposable)
    }

    fun disposeNearByPlace(){
        if(this::newrByPlaceDisposable.isInitialized){
            if(!newrByPlaceDisposable.isDisposed)
                newrByPlaceDisposable.dispose()
        }
    }

    fun getCategories() {
        disposable.add(
            repository.getCategories()
                .subscribe({
                    if (it.isSuccessful)
                        onCategory.value = it.body()
                }, {
                    Log.i(TAG, " gettingCategory::: $it")
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun getOffers() {
        Log.d(TAG, "addObserver: response?.definition called")
        disposable.add(
            repository.getOffers()
                .subscribe({
                    if (it.isSuccessful)
                        onOffers.value = it.body()
                }, {
                    Log.e(TAG, " getOffers error::: $it")
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun getFacilities() {
        disposable.add(
            repository.getFacilities()
                .subscribe({
                    if (it.isSuccessful){
                        onFacility.value = it.body()
                        Log.i(TAG, "getFacilities: ${it.body()}")
                    }
                }, {
                    Log.i(TAG, " gettingCategory::: $it")
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun getCuisine() {
        disposable.add(
            repository.getCuisine()
                .subscribe({
                    if (it.isSuccessful)
                        onCuisine.value = it.body()
                }, {
                    Log.i(TAG, " gettingCategory::: $it")
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }


    fun getBestMeetPagingData(
        facilities: StringBuilder,
        cuisines: StringBuilder,
        categories: StringBuilder,
        isBestPlace: Boolean?,
        countryCode : String?
    ) {


        Log.i(TAG," isBestPlaceData:::: $isBestPlace ")

        disposable.add(Pager(
            config = PagingConfig(
                pageSize = 2,
                enablePlaceholders = false,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                PlacePagingDataSource(repository).apply {
                    setfilter(facilities, cuisines, categories,if(isBestPlace == true) true else null,countryCode)
                }
            }
        ).flowable
            .subscribe({
                placesPagingdata.value = it
            }, {
                Log.i(TAG, " Something went wrong feetching post ${it}")
            })
        )
    }

    fun getSavedplacesPagingData(){
        Log.d(TAG, "getSavedplacesPagingData1: ")
        disposable.add(Pager(
            config = PagingConfig(
                pageSize = 2,
                enablePlaceholders = false,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                SavedPlacePagingDataSource(repository)
            }
        ).flowable
            .subscribe({
                Log.d(TAG, "getSavedplacesPagingData2: ")
                placesPagingdata.value = it
            }, {
                Log.i(TAG, " Something went wrong feetching post ${it}")
            })
        )
    }


    fun getSavedPlaceWOpaging(limit: Int =1000){
        disposable.add(
            repository.getSavedPlaces(1,limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onSavedPlace.value = it
                },{
                    it.printStackTrace()
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun searchPlace(searchString: String?) {
        disposable.add(
            repository.searchPlace(searchString)
                .subscribe({
                    if (it.isSuccessful)
                        onPlaceSearch.value = it.body()
                }, {
                    it.printStackTrace()
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun getPlace(_id: String?,mode:String?) {
        disposable.add(
            repository.getPlace(_id,mode)
                .subscribe({
                    Log.i(TAG, "onSinglePlace: 0 - ${onSinglePlace.hashCode()}")
                    if (it.isSuccessful){
                        Log.i(TAG," Resultcame")
                        onSinglePlace.value = ResultHandler.Success(it.body())
                    }
                }, {
                    Log.i(TAG, "onSinglePlace: 1 - ${onSinglePlace.hashCode()}")
                    Utils.handleException(it,onSinglePlace,"getPlace")
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }



    fun reviewPlace(_id: String?, request: JsonObject) {
        disposable.add(
            repository.reviewPlace(_id, request)
                .subscribe({
                    if (it.isSuccessful)
                        onReviewPlace.value = ResultHandler.Success(it.body())
                }, {
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun editReview(_id: String?, reviewId: String?, request: JsonObject) {
        disposable.add(
            repository.editReview(_id, reviewId, request)
                .subscribe({
                    if (it.isSuccessful)
                        onReviewPlace.value = ResultHandler.Success(it.body())
                }, {
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun checkInPlace(placeId: String?, lat: Double,lng: Double,isShareble :Boolean? = true) {
        val request = JsonObject()
        request.addProperty("lat",lat)
        request.addProperty("lng",lng)
        request.addProperty("post_enabled",isShareble)
        disposable.add(
            repository.checkInPlace(placeId, request)
                .subscribe({
                    if (it.isSuccessful)
                        onPlaceChekIn.value = ResultHandler.Success(it.body())
                }, {
                    if (it is ApiException) {
                        if(it.errorResponse != null && it.errorResponse?.errorCode == 400){
                            onPlaceChekIn.value = ResultHandler.Failure(it.errorResponse?.code,it.errorResponse?.message,it)
                        }else{
                            errorResponse.value = it.errorResponse
                        }
                    }
                })
        )
    }

    fun addBookmark(placeId: String?) {
        disposable.add(
            repository.addBookmark(placeId)
                .subscribe({
                    if (it.isSuccessful)
                        onBookmarkAdd.value = ResultHandler.Success(it.body())
                }, {
                    if (it is ApiException) {
                        if(it.errorResponse != null && it.errorResponse?.errorCode == 400){
                            onBookmarkAdd.value = ResultHandler.Failure(it.errorResponse?.code,it.errorResponse?.message,it)
                        }else{
                            errorResponse.value = it.errorResponse
                        }
                    }
                })
        )
    }

    fun deleteBookmark(placeId: String?) {
        disposable.add(
            repository.deleteBookmark(placeId)
                .subscribe({
                    if (it.isSuccessful)
                        onBookmarkDelete.value = ResultHandler.Success(it.body())
                }, {
                    if (it is ApiException) {
                        if(it.errorResponse != null && it.errorResponse?.errorCode == 400){
                            onBookmarkDelete.value = ResultHandler.Failure(it.errorResponse?.code,it.errorResponse?.message,it)
                        }else{
                            errorResponse.value = it.errorResponse
                        }
                    }
                })
        )
    }






    fun getMyReview(placeId: String?, userId: String?) {
        disposable.add(
            repository.getMyReview(placeId, userId)
                .subscribe({
                    if (it.isSuccessful)
                        onMyReview.value = it.body()
                }, {
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun getReviewPagingDataSource(placeID : String?) {
        disposable.add(Pager(
            config = PagingConfig(
                pageSize = 15,
                enablePlaceholders = false,
                prefetchDistance = 1,
            ),
            pagingSourceFactory = {
                ReviewPagingDatasource(repository,placeID )
            }
        ).flowable
            .subscribe({
                reviewPagingdata.value = it
            }, {
                Log.i(TAG, " Something went wrong feetching post ${it}")
            })
        )
    }

    fun getReviewCount(placeId: String?){
        disposable.add(
            repository.getReviewCount(placeId)
                .subscribe({
                    if (it.isSuccessful)
                        onReviewCount.value = it.body()?.get("review_count")?.asInt
                }, {
                    if (it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun observerSavedPlace(): LiveData<FetchPlacesResponse> {
        return onSavedPlace
    }


    fun observeAddBookMarked():LiveData<ResultHandler<JsonObject?>>{
        return onBookmarkAdd
    }

    fun observeDeleteBookMarked():LiveData<ResultHandler<JsonObject?>>{
        return onBookmarkDelete
    }

    fun observeReviewData():LiveData<PagingData<FetchReviewResponseItem>>{
        return reviewPagingdata
    }

    fun observeNearPlaceCount(): LiveData<Int?> {
        return nearPlaceCount
    }

    fun observeMyReview(): LiveData<FetchReviewResponse?> {
        return onMyReview
    }

    fun observeReviewCount(): LiveData<Int?> {
        return onReviewCount
    }

    fun observeCheckIn():LiveData<ResultHandler<JsonObject?>>{
        return onPlaceChekIn
    }

    fun observeReview(): LiveData<ResultHandler<JsonObject?>> {
        return onReviewPlace
    }

    fun observeSinglePlace(): LiveData<ResultHandler<GetSinglePlaceResponse?>> {
        return onSinglePlace
    }

    fun observeCategory(): LiveData<CategoryResponse> {
        return onCategory
    }
    fun observeOnOffers(): LiveData<CategoryResponse> {
        return onOffers
    }

    fun observeFacility(): LiveData<FacilityResponse> {
        return onFacility
    }

    fun observeCuisine(): LiveData<CuisineResponse> {
        return onCuisine
    }


    fun observePlacePagingDaraSource(): LiveData<PagingData<FetchPlacesResponseItem>> {
        return placesPagingdata
    }

    fun observerPlaces(): LiveData<FetchPlacesResponse> {
        return onBestPlaces
    }

    fun closeByPlace(): LiveData<FetchPlacesResponse> {
        return onNearByPlaces
    }

    fun observeException(): LiveData<ErrorResponse?> {
        return errorResponse
    }

    fun observePlaceSearch(): LiveData<SearchPlaceResponse> {
        return onPlaceSearch
    }



    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}