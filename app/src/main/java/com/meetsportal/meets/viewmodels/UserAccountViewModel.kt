package com.meetsportal.meets.viewmodels

import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.meetsportal.meets.models.*
import com.meetsportal.meets.networking.ErrorResponse
import com.meetsportal.meets.networking.FileUploadStatus
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.exception.ApiException
import com.meetsportal.meets.networking.places.LeaderboardResponse
import com.meetsportal.meets.networking.places.RewardsResponse
import com.meetsportal.meets.networking.profile.*
import com.meetsportal.meets.networking.profile.dastasource.FollowerFollowingPagerDataSource
import com.meetsportal.meets.repository.AppRepository
import com.meetsportal.meets.utils.Constant.PREFRENCE_INTEREST
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import com.onesignal.OneSignal
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import java.util.HashMap
import javax.inject.Inject
import com.google.gson.JsonArray
import com.meetsportal.meets.networking.post.FetchPostResponseItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.meetsportal.meets.application.MyApplication
import kotlin.math.log


@HiltViewModel
class UserAccountViewModel @Inject constructor(var repository: AppRepository) : ViewModel() {

    private val disposable = CompositeDisposable()
    private val onUser: MediatorLiveData<UserProfile> = MediatorLiveData<UserProfile>()
    private val onProfileChange = MediatorLiveData<StateResource<String>>()
    private val onProfilePictureChange = MediatorLiveData<ResultHandler<FileUploadStatus<ProfileGetResponse?>>>()
    private val onUploadDocument = MediatorLiveData<FileUploadStatus<Response<ProfileGetResponse?>>>()
    private val onSuggestion = MediatorLiveData<ResultHandler<SuggestionResponse?>>()
    private val onQuoteChange = MediatorLiveData<String?>()
    private val onSpotLightPictureChange = MediatorLiveData<ProfileGetResponse>()
    private val onInterestChange = MediatorLiveData<ResultHandler<ProfileGetResponse?>>()
    private val onFollowUnfollow = MediatorLiveData<Boolean>()
    private val onUnblockUser = MediatorLiveData<Boolean>()
    private val onBlockUser = MediatorLiveData<Boolean>()
    private val onChargeCount = MediatorLiveData<ResultHandler<JsonObject?>>()
    private val onTopUp = MediatorLiveData<ResultHandler<TopUpBoostModelResponse?>>()
    private val onConsumeProduct = MediatorLiveData<ResultHandler<JsonObject?>>()
    private val onBoostPricing = MediatorLiveData<ResultHandler<BoostPricingModelResponse?>>()
    private val onGetActiveBoost = MediatorLiveData<ResultHandler<ActiveItemResponse?>>()
    private val onProfileInsight = MediatorLiveData<ResultHandler<ProfileInsight?>>()
    private val onMapUserChange = MediatorLiveData<ResultHandler<MapUserResponse?>>()
    private val verifyProfile = MediatorLiveData<ResultHandler<JsonObject?>>()
    private lateinit var mapUserDisposable: Disposable
    private val onFollowFollowingDataSource:MediatorLiveData<PagingData<FollowerFollowingResponseItem>> = MediatorLiveData()
//    private val onLeaderDataSource:MediatorLiveData<ResultHandler<LeaderboardResponse>> = MediatorLiveData()
    private val onLeaderDataSource = MediatorLiveData<ResultHandler<LeaderboardResponse?>>()
    private val onLookUpUsername = MediatorLiveData<ResultHandler<LeaderboardResponse?>>()
    private val onBadgeRelation:MediatorLiveData<Map<Int,FollowerFollowingResponse>> = MediatorLiveData()
    private val onReward:MediatorLiveData<RewardsResponse> = MediatorLiveData()

    private val onProfileDataChange = MediatorLiveData<ResultHandler<ProfileGetResponse?>>()
    private val onOtherProfileDataChange = MediatorLiveData<ResultHandler<OtherProfileGetResponse?>>()
    private val onFullInterestDataChanges = MediatorLiveData<ResultHandler<FullInterestGetResponse?>>()
    private val onPreVerifydatares = MediatorLiveData<ResultHandler<PreVerifyDataRes?>>()
    private val onEmergencyMessageSent = MediatorLiveData<StateResource<String>>()
    private val onPeopleSearch = MediatorLiveData<ResultHandler<SearchPeopleResponse?>>()
    private val onSaveAddress = MediatorLiveData<Response<JsonObject?>>()
    private val onNewVisits = MediatorLiveData<ResultHandler<JsonObject?>>()
    private val onMinVersion = MediatorLiveData<ResultHandler<JsonObject?>>()
    private val onSendOtp = MediatorLiveData<ResultHandler<JsonObject?>>()
    private val onChangeCredential = MediatorLiveData<ResultHandler<JsonObject?>>()
    private val onGetAddress = MediatorLiveData<ResultHandler<Pair<AddressModelResponse?,AddressModel?>>>()
    private val onGetInsight = MediatorLiveData<ResultHandler<InsightResponse?>>()
    private val onDeleteAddress = MediatorLiveData<ResultHandler<JsonObject?>>()
    private val profileViewPagingData : MediatorLiveData<PagingData<ViewdMeItem>> = MediatorLiveData()


    private val onError = MediatorLiveData<String>()

    private val errorResponse: MutableLiveData<ErrorResponse?> = MutableLiveData()


    val TAG = UserAccountViewModel::class.java.simpleName


    init {
        //uid = repository.getCurrentUid()
        loadUserInfo(repository.getCurrentUid())
    }

    private fun loadUserInfo(currentUid: String?) {
        repository.getUserinfo(currentUid)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.toObservable()
            ?.subscribe(object : Observer<DocumentSnapshot?> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onNext(documentSnapshot: DocumentSnapshot) {
                    onUser.value = documentSnapshot.toObject(UserProfile::class.java)
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onError: $e")
                    if(e is ApiException){
                        errorResponse.value = e.errorResponse
                    }
                }

                override fun onComplete() {
                    //TODO("Not yet implemented")
                }
            })
    }


    /*fun updateProfile(userProfile: UserProfile?) {
        repository.updateProfile(userProfile)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                    onProfileChange.setValue(StateResource.loading())
                }

                override fun onComplete() {
                    onProfileChange.setValue(StateResource.success(null))
                }

                override fun onError(e: Throwable) {
                    onProfileChange.setValue(StateResource.error(e.message!!))
                }
            })
    }*/

    fun updateProfilePicture(image: Bitmap) {
        disposable.add(repository.uploadProfilePicture(image).subscribe({
            it?.let {
                it.response?.let { it1 ->
                    onProfilePictureChange.value = ResultHandler.Success(it)
                }
                Log.i(TAG, " Body::updateProfilePicture : percent: ${it.percent}")
            }
        }, {
            Log.i(TAG, " error comes in updating profile pic $it")
            if(it is ApiException) {
                ResultHandler.Failure(it.errorResponse?.code, it.message, it)
            } else {
                ResultHandler.Failure("500", it.message, it)
            }
        }))
    }

    fun uploadCoverPicture(image: Bitmap) {
        Log.d(TAG, "uploadCoverPicture: calling")
        disposable.add(repository.uploadCoverPicture(image).subscribe({
            it?.let {
                it.response?.let { it1 ->
                    onProfilePictureChange.value = ResultHandler.Success(it)
                }
                Log.i(TAG, " Body::uploadCoverPicture : percent: ${it.percent}")
            }
        }, {
            if(it is ApiException) {
                ResultHandler.Failure(it.errorResponse?.code, it.message, it)
            } else {
                ResultHandler.Failure("500", it.message, it)
            }
        }))
    }

    fun saveAddress(addressFromLatLong: AddressModel) {
        if(addressFromLatLong.location==null){
            Log.e(TAG, "saveAddress: location null")
            return
        }
        addressFromLatLong.custom_uid = System.currentTimeMillis().toString()
        val request = JsonObject()
        val loc = JsonObject()
        val array = JsonArray()
        array.add(addressFromLatLong.location?.coordinates?.get(1))
        array.add(addressFromLatLong.location?.coordinates?.get(0))
        loc.addProperty("type","Point")
        loc.add("coordinates",array)
        request.addProperty("name", addressFromLatLong?.name)
        request.addProperty("address", addressFromLatLong?.address)
        request.addProperty("image_url", addressFromLatLong?.image_url)
        request.addProperty("external_id", addressFromLatLong?._id)
        request.add("location",loc)
//        request.addProperty("lng", addressFromLatLong?.lon)
        request.addProperty("country_code", addressFromLatLong?.country_code)
        request.addProperty("city", addressFromLatLong?.city?.capitalize())
        request.addProperty("country", addressFromLatLong?.country)
        request.addProperty("custom_uid",addressFromLatLong.custom_uid )
        disposable.add(repository.saveAddress(request).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it?.let {
                        onSaveAddress.value = it
                        Log.i(TAG, " Body:: $it")
                    }

                }, {
                    Log.i(TAG, " error comes in updating profile pic $it")
                    if(it is ApiException) {
                        errorResponse.value = it.errorResponse
                    }
                    //throw it
                }))
    }
    fun generateOtp(channel:String,email:String?,phone:String?) {
        val request = JsonObject()
        request.addProperty("channel",channel)
        request.addProperty("phone_number",phone)
        request.addProperty("email",email)
        disposable.add(repository.generateOtp(request).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it?.let {
                        onSendOtp.value = ResultHandler.Success(it.body())
                        Log.i(TAG, " Body:: $it")
                    }

                }, {
                    Utils.handleException(it,onSendOtp,"onSendOtp")
                }))
    }
    fun changeCredential(channel:String,email:String?,phone:String?,code:String?) {
        val request = JsonObject()
        request.addProperty("type",channel)
        request.addProperty("phone_number",phone)
        request.addProperty("email",email)
        request.addProperty("code",code?.toIntOrNull())
        disposable.add(repository.changeCredential(request).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    it?.let {
                        onChangeCredential.value = ResultHandler.Success(it.body())
                        Log.i(TAG, " Body:: $it")
                    }

                }, {
                    Utils.handleException(it,onChangeCredential,"onChangeCredential")
                }))
    }

    fun getAddresses(addressFromLatLong: AddressModel?, limit: Int = 10000) {

        disposable.add(repository.getSavedAddress(1, limit).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    if(it.isSuccessful) {
                        onGetAddress.value = ResultHandler.Success(Pair(it.body(), addressFromLatLong))
                        Log.i(TAG, " Body:: $it")
                    }

                }, {
                    Utils.handleException(it, onGetAddress, "getMeetUpDetail")
                    //throw it
                }))
    }

    fun getInsight(postId: String?) {
        disposable.add(repository.getInSights(postId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it.isSuccessful){
                    onGetInsight.value = ResultHandler.Success(it.body())
                    Log.i(TAG," Body:: $it")
                }

            },{
                Utils.handleException(it,onGetInsight,"getInsight")
                //throw it
            })
        )
    }

    fun deleteAddress(id: String) {
        disposable.add(repository.deleteSavedAddress(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it?.let {
                    if(it.isSuccessful)
                        onDeleteAddress.value = ResultHandler.Success(it.body())
                        Log.i(TAG," Body:: $it")
                }

            },{
                Log.i(TAG," error comes in getSavedAddress $it")
                if(it is ApiException){
                    //errorResponse.value = it.errorResponse
                    onDeleteAddress.value = ResultHandler.Failure(it.errorResponse?.code,it.message,it)
                }else{
                    onDeleteAddress.value = ResultHandler.Failure("500",it.message,it)
                }


                //throw it
            })
        )
    }

    fun updateSpotLightPicture(image:Bitmap,spotlights:ArrayList<Spotlight?>,index:Int){
        disposable.add(repository.updateSpotLightPicture(image,spotlights,index)
            .subscribe({
                Log.i(TAG," comming:: 1")
                if(it.isSuccessful){
                    Log.i(TAG," comming:: 2")
                    onSpotLightPictureChange.value =it.body()
                }
            },{
                Log.i(TAG," comming:: 2")
                Log.e(TAG," error Uploading spotligh: ${it.message}")
                if(it is ApiException){
                   errorResponse.value = it.errorResponse
                }

            })
        )
    }

    fun uploadVaccineCard(image: Uri, type : String){
        disposable.add(repository.uploadVaccineCard(image,type)
            .subscribe({
                it?.let {
                    onUploadDocument.value = it
                    Log.i(TAG," Body:: $it")
                }
            },{
                Log.i(TAG," error comes in updating profile pic $it")
                if(it is ApiException){
                    errorResponse.value = it.errorResponse
                }
            })
        )
    }



    fun updateInterest(definition: ArrayList<Definition?>){
        disposable.add( repository.updateInterest(definition)
            .subscribe({
                if(it.isSuccessful){
                    Log.i(TAG," comming:: 2")
                    onInterestChange.value = ResultHandler.Success(it.body())
                }
            },{
                /*Log.e(TAG," error Uploading spotligh ${it.message}")
                if(it is ApiException){
                    errorResponse.value = it.errorResponse
                }*/
                Utils.handleException(it,onInterestChange,"updateInterest")
            })

        )
    }

    fun deleteSpotLight(spotlights:ArrayList<Spotlight?>){
        Log.i(TAG," deleteSpotLight:: deleteSpotLight deleteSpotLight")
        disposable.add(repository.deleteSpotLight(spotlights)
            .subscribe({
                Log.i(TAG," comming:: 1")
                if(it.isSuccessful){
                    Log.i(TAG," comming:: 2")
                    onSpotLightPictureChange.value =it.body()
                }
            },{
                Log.i(TAG," comming:: 2")
                Log.e(TAG," error Uploading spotligh")
                if(it is ApiException){
                    errorResponse.value = it.errorResponse
                }
            })
        )
    }


    fun updateProfile(request:ProfileUpdateRequest){
        disposable.add(repository.updateProfile(request)
            .subscribe({
                if(it.isSuccessful){
//                    onProfileDataChange.value = it.body()
                    onProfileDataChange.value = ResultHandler.Success(it.body())
                }
            },{
                Log.i(TAG,"Update profile api faild::${it.message} ")
                Utils.handleException(it,onProfileDataChange,"updateProfile")
                if(it is ApiException){
                    errorResponse.value = it.errorResponse
                }
            })
        )
    }

    fun getFullProfile(){
        disposable.add(
            repository.getFullProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.i(TAG, "getFullProfile: getting fullInfor:::")
                    if (it.isSuccessful)
//                        onProfileDataChange.value = it.body()
                        onProfileDataChange.value = ResultHandler.Success(it.body())
                }, {
                    Log.i(TAG, "Unable to fetch profile:: ${it.message} ")
                    Utils.handleException(it,onProfileDataChange,"updateProfile")
                    if(it is ApiException){
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun getOtherProfile(userId:String?,actionSource : String? = null){
       // "view_profile:post:6194cf8cb0e599001351b57a:sid-930-dsb9drhcgzxteu"
        var analysis  :String? = null
        actionSource?.let {
            analysis = "view_profile:".plus(actionSource).plus(":").plus(userId)
        }

        disposable.add(
            repository.getOtherProfile(userId,analysis)
                .subscribe({
                    if (it.isSuccessful)
                        onOtherProfileDataChange.value = ResultHandler.Success(it.body())
                }, {
                    Log.i(TAG, "Unable to fetch profile:: ${it.message} ")
                    Utils.handleException(it,onOtherProfileDataChange,"getOtherProfile")
                    if(it is ApiException){
                        errorResponse.value = it.errorResponse
                    }
                })
        )

    }
    fun getNewVisits(){
        disposable.add(
            repository.getNewVisits()
                .subscribe({
                    if (it.isSuccessful)
                        onNewVisits.value = ResultHandler.Success(it.body())
                }, {
                    Log.i(TAG, "Unable to get onNewVisits:: ${it.message} ")
                    Utils.handleException(it,onNewVisits,"getNewVisits")
                })
        )

    }
    fun getMinVersion(){
        disposable.add(
            repository.getMinVersion()
                .subscribe({
                    if (it.isSuccessful)
                        onMinVersion.value = ResultHandler.Success(it.body())
                }, {
                    Log.i(TAG, "Unable to get onMinVersion:: ${it.message} ")
                    Utils.handleException(it,onMinVersion,"getMinVersion")
                })
        )

    }


    fun getFullGenericList(key:String,wantResult : Boolean = true){
        disposable.add(repository.getFullGenericList(key)
            .subscribe({
                if(it.isSuccessful){
                    if(wantResult)
                        onFullInterestDataChanges.value = ResultHandler.Success(it.body())
                    if(key.equals("interests")){
                        PreferencesManager.put(it.body(),PREFRENCE_INTEREST)
                    }
                }
            },{
                /*Log.i(TAG,"Get Full Interest api faild::${it.message} ")
                if(it is ApiException){
                    errorResponse.value = it.errorResponse
                }*/
                //onError.value = String()"Something went wrong Please try again later..."
                Utils.handleException(it,onFullInterestDataChanges,"getFullGenericList")
            })
        )
    }

    fun getPreVerificationData(){
        disposable.add(repository.getPreVerifyData()
            .subscribe({
                if(it.isSuccessful){
                    onPreVerifydatares.value = ResultHandler.Success(it.body())
                }
            },{
                Utils.handleException(it,onPreVerifydatares,"getPreVerificationData")
            })
        )
    }

    fun sendEmergency(){
        disposable.add(repository.sendEmergency()
            .subscribe({
                if(it.isSuccessful){
                    onEmergencyMessageSent.value = StateResource.success("Alert Send to Emergency Contact")
                }
            },{
                Log.i(TAG,"Alert Emergency api faild::${it.message} ")
                //onEmergencyMessageSent.value = StateResource.error("Please add Emergency contact number first...")
                onEmergencyMessageSent.value = StateResource.error(MyError(message = "Please add Emergency contact number first..."))
                if(it is ApiException){
                    errorResponse.value = it.errorResponse
                }
            })
        )
    }


    fun getRandomQuote(){
        disposable.add(repository.getRandomQuote()
            .subscribe({
                if(it.isSuccessful){
                    onQuoteChange.value = it.body()?.get("quoteText").toString()
                    Log.i(TAG," quoteObject ${it.body().toString()}")
                    Log.i(TAG," quote ${it.body()?.get("quoteText")}")
                }
            },{
                Log.e(TAG," get quote failed ::${it.message} ")
                if(it is ApiException){
                    errorResponse.value = it.errorResponse
                }
                //onError.value = String()"Something went wrong Please try again later..."
            })
        )
    }

    fun followUser(userId: String?){
        disposable.add(repository.followUser(userId)
            .subscribe({
                if(it.isSuccessful){
                    onFollowUnfollow.value = true
                }
            },{
                Log.i(TAG," followuserfailed:: ")
                if(it is ApiException){
                    errorResponse.value = it.errorResponse
                }
            })
        )
    }

    fun unBlockUser(userId: String?){
        disposable.add(repository.unBlockUser(userId)
            .subscribe({
                if(it.isSuccessful){
                    onUnblockUser.value = true
                }
            },{
                Log.i(TAG," followuserfailed:: ")
                if(it is ApiException){
                    errorResponse.value = it.errorResponse
                }
            })
        )
    }

    fun unfollowUser(userId: String?){
        disposable.add(repository.unfollowUser(userId)
            .subscribe({
                if(it.isSuccessful){
                    onFollowUnfollow.value = false
                }
            },{
                Log.i(TAG," followuserfailed:: ")
                if(it is ApiException){
                    errorResponse.value = it.errorResponse
                }
            })
        )
    }

    fun searchPeople(searchString : String?):Disposable{
        //disposable.add(
            var d = repository.searchPeople(searchString)
                .subscribe({
                    if(it.isSuccessful){
                        onPeopleSearch.value = ResultHandler.Success(it.body())
                    }
                },{
                    Utils.handleException(it,onPeopleSearch,"searchPeople")
                    /*Log.i(TAG," errorCome:: $it")
                    if(it is ApiException){
                        errorResponse.value = it.errorResponse
                    }*/
                })
        //)
        disposable.add(d)
        return d
    }

    fun sendLocation(location:Location){
        disposable.add(
            repository.sendLocation(location)
                .subscribe({

                },{
                    Log.e(TAG," Location api not worked $it")
                    if(it is ApiException){
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun getSuggestion(){
        disposable.add(
            repository.getSuggestion()
                .subscribe({
                    if(it.isSuccessful){
                        Log.i(TAG," suggestionCome:: ")
                        onSuggestion.value = ResultHandler.Success(it.body())
                    }
                },{
                    Log.e(TAG," suggestion Api giving error $it")
                    Utils.handleException(it,onSuggestion,"getSuggestion")
                })
        )
    }
    fun getBadgeRelation(sid: String?,relations : String?,badge :String?,index: Int){
        val resultHashMap = HashMap<Int,FollowerFollowingResponse>()
        disposable.add(
            repository.getRelations(sid = sid,relations=relations,page=1,limit = 21,"badge:${badge?.toLowerCase()}")
                    .subscribe({
                        resultHashMap[index] = it
                        onBadgeRelation.value = resultHashMap
                    }, {
                        Log.i(TAG, " Something went wrong getBadgeRelation ${it}")
                    })
        )
    }
    fun getRewardComp(){
        disposable.add(
            repository.getReward()
                    .subscribe({
                        if(it.isSuccessful){
                            onReward.value=it.body()
                        }
                    }, {
                        Log.i(TAG, " Something went wrong getBadgeRelation ${it}")
                        if(it is ApiException){
                            errorResponse.value = it.errorResponse
                        }
                    })
        )
    }

    fun getFollowFollowingDataSource(sid: String?,relations : String?,searchString :String?){
        disposable.add(Pager(
            config = PagingConfig(
                pageSize = 2,
                enablePlaceholders = false,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                FollowerFollowingPagerDataSource(repository).apply {
                    this.sid = sid
                    this.relation = relations
                    this.searchString = searchString
                }
            }
        ).flowable
            .subscribe({
                onFollowFollowingDataSource.value = it
            }, {
                Log.i(TAG, " Something went wrong feetching post ${it}")
            })
        )
    }

    fun getLeaders(region: String?, otherUserID: String?){
        Log.d(TAG, "getLeaderDataSource:region@ $region ")
        disposable.add(repository.getLeaders(region,otherUserID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    Log.i(TAG," checkingNumber Of Times data came")
                    if (it.isSuccessful)
                        onLeaderDataSource.value = ResultHandler.Success(it.body())
                }, {
                    Utils.handleException(it,onLeaderDataSource,"getLeaders")
                }))
    }
    fun getLookUserName(user: String?){
        disposable.add(repository.getLookUserName(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    if (it.isSuccessful)
                        onLookUpUsername.value = ResultHandler.Success(it.body())
                }, {
                    Utils.handleException(it,onLookUpUsername,"getLeaders")
                }))
    }

    fun blockUser(userId: String?){
        disposable.add(repository.blockUser(userId)
            .subscribe({
                if(it.isSuccessful){
                    onBlockUser.value = false
                }
            },{
                Log.i(TAG," blockUser failed:: ")
                if(it is ApiException){
                    errorResponse.value = it.errorResponse
                }
            })
        )
    }
    fun getChargeCount(){
        disposable.add(repository.getChargeCount()
            .subscribe({
                if(it.isSuccessful){
                    onChargeCount.value = ResultHandler.Success(it.body())
                }
            },{
                Log.i(TAG, " error comes in getChargeCount :: $it")
                if(it is ApiException) {
                    onChargeCount.value=ResultHandler.Failure(it.errorResponse?.code, it.message, it)
                } else {
                    onChargeCount.value= ResultHandler.Failure("500", it.message, it)
                }
            })
        )
    }
    fun getBoostPricing(product_id: String?= "boost"){
        disposable.add(repository.getBoostPricing(product_id)
            .subscribe({
                if(it.isSuccessful){
                    onBoostPricing.value = ResultHandler.Success(it.body())
                }
            },{
                Log.i(TAG, " error comes in onBoostPricing :: $it")
                if(it is ApiException) {
                    onBoostPricing.value=ResultHandler.Failure(it.errorResponse?.code, it.message, it)
                } else {
                    onBoostPricing.value= ResultHandler.Failure("500", it.message, it)
                }
            })
        )
    }
    fun getActiveBoost() {
        disposable.add(repository.getActiveBoost()
            .subscribe({
                if(it.isSuccessful){
                    onGetActiveBoost.value = ResultHandler.Success(it.body())
                }
            },{
                Log.i(TAG, " error comes in onBoostPricing :: $it")
                if(it is ApiException) {
                    onGetActiveBoost.value=ResultHandler.Failure(it.errorResponse?.code, it.message, it)
                } else {
                    onGetActiveBoost.value= ResultHandler.Failure("500", it.message, it)
                }
            })
        )
    }
    fun topUpProduct(pricing_id:String?){
        val request=JsonObject()
        request.addProperty("pricing_id",pricing_id)
        disposable.add(repository.topUpBoost(request)
            .subscribe({
                if(it.isSuccessful){
                    onTopUp.value = ResultHandler.Success(it.body())
                }
            },{
                Log.i(TAG, " error comes in onTopUpBoost :: $it")
                Utils.handleException(it,onTopUp,"onTopUp")
//                if(it is ApiException) {
//                    onTopUp.value=ResultHandler.Failure(it.errorResponse?.code, it.message, it)
//                } else {
//                    onTopUp.value= ResultHandler.Failure("500", it.message, it)
//                }
            })
        )
    }
    fun consumeProduct(post_id:String?,product_id:String="boost"){
        val request=JsonObject()
        val meta=JsonObject()
        request.addProperty("product_id",product_id)
        post_id?.let{
            meta.addProperty("post_id", post_id)
        }?:run{
            meta.addProperty("user_id",MyApplication.SID)
        }
        request.add("meta",meta)
        disposable.add(repository.boostPost(request)
            .subscribe({
                if(it.isSuccessful){
                    onConsumeProduct.value = ResultHandler.Success(it.body())
                }
            },{
                Log.i(TAG, " error comes in onBoostPost:: $it")
                if(it is ApiException) {
                    onConsumeProduct.value=ResultHandler.Failure(it.errorResponse?.code, it.message, it)
                } else {
                    onConsumeProduct.value= ResultHandler.Failure("500", it.message, it)
                }
            })
        )
    }

    fun getMapUsers(location : LatLng,interests : String?){
        disposeMapUser()
        mapUserDisposable = repository.getMapUsers(location,interests)
            .subscribe({
                if(it.isSuccessful){
                    onMapUserChange.value = ResultHandler.Success(it.body())
                }
            },{
                Log.i(TAG," getMapUsersFailed:: ${it}")
                Utils.handleException(it,onMapUserChange,"getMapUsers")
                if(it is ApiException){
                    errorResponse.value = it.errorResponse
                }
            })

        disposable.add(mapUserDisposable)
    }

    fun verifyProfile(list : Array<Uri?>){
        Log.i(TAG," verifyProfile:::: $list")
        verifyProfile.value = ResultHandler.Loading(true)
        var count = 0
        var resultHashMap = HashMap<Int,String>()
        //var reseultArray: ArrayList<String> = ArrayList()
        disposable.add(repository.uploadVerifyProfile(list)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i(TAG," verifyProfile:::: 1")
                count++
                resultHashMap.put((it as HashMap<Int,String>).keys.first(),(it as HashMap<Int,String>).values.first())
                if(resultHashMap.size == list.size){
                    /*for (i in 0 until resultHashMap.size){
                        reseultArray.add(resultHashMap.get(i)!!)
                    }*/
                    Log.i(TAG," verifyProfile:::: 2")
                    val request = JsonObject()
                    request.addProperty("eyes_closed_image_url",resultHashMap.get(0))
                    request.addProperty("tilted_shoulder_image_url",resultHashMap.get(1))

                    Log.i(TAG," verifyProfile:::: 3")
                    disposable.add(repository.verifyProfile(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            if(it.isSuccessful){
                                Log.i(TAG," verifyProfile:::: 4")
                                verifyProfile.value = ResultHandler.Loading(false)
                                verifyProfile.value = ResultHandler.Success(it.body())
                            }
                        },{
                            Utils.handleException(it,verifyProfile,"verifyProfile")
                        }))
                }

            },{
                Utils.handleException(it,verifyProfile,"verifyProfile")
            }))

    }

    fun disposeMapUser(){
        if(this::mapUserDisposable.isInitialized)
            if(mapUserDisposable.isDisposed){
                Log.i(TAG," peopleDisposed::: ")
                mapUserDisposable.dispose()
            }
    }

    fun getProfileInsight(dateString: String) {
        disposable.add(repository.getProfileInsight(dateString)
            .subscribe({
                if(it.isSuccessful){
                    onProfileInsight.value = ResultHandler.Success(it.body())
                }
            },{
                Utils.handleException(it,onProfileInsight,"getProfileInsight")
            })
        )
    }

    fun getProfileViewPeoplwInsight(){
        disposable.add(
            repository.getProfileInsightPersonListDataSource()
                .subscribe({
                    profileViewPagingData.value = it
                },{
                    Log.i(TAG," Something went wrong feetching post ${it}")
                    if(it is ApiException){
                        errorResponse.value = it.errorResponse
                    }
                })

        )
    }

    fun getProfileInsightPersonList(){
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val current = Calendar.getInstance();
        disposable.add(repository.getProfileInsight(sdf.format(current.time))
            .subscribe({
                if(it.isSuccessful){
                    onProfileInsight.value = ResultHandler.Success(it.body())
                }
            },{
                Utils.handleException(it,onProfileInsight,"getProfileInsight")
            })
        )
    }

    fun observeProfileViewChange(): LiveData<PagingData<ViewdMeItem>> {
        return profileViewPagingData
    }


    fun observerProfileInsight(): LiveData<ResultHandler<ProfileInsight?>> {
        return onProfileInsight
    }

    fun observeVerifyProfile(): LiveData<ResultHandler<JsonObject?>> {
        return verifyProfile
    }

    fun observeMapUsers(): LiveData<ResultHandler<MapUserResponse?>> {
        return onMapUserChange
    }

    fun observeUnblockUser():LiveData<Boolean>{
        return onUnblockUser
    }

    fun observeOnBlockUser():LiveData<Boolean>{
        return onBlockUser
    }

    fun observeOnChargeCount(): LiveData<ResultHandler<JsonObject?>> {
        return onChargeCount
    }

    fun observeOnBoostPricing(): LiveData<ResultHandler<BoostPricingModelResponse?>> {
        return onBoostPricing
    }
    fun observeOnGetActiveBoost(): LiveData<ResultHandler<ActiveItemResponse?>> {
        return onGetActiveBoost
    }
    fun observeOnTopUp(): LiveData<ResultHandler<TopUpBoostModelResponse?>> {
        return onTopUp
    }
    fun observeOnConsumeProduct(): LiveData<ResultHandler<JsonObject?>> {
        return onConsumeProduct
    }

    fun observeFollowFollowingDataSource():LiveData<PagingData<FollowerFollowingResponseItem>>{
        return onFollowFollowingDataSource
    }
    fun observeLeaderDataSource(): LiveData<ResultHandler<LeaderboardResponse?>> {
        return onLeaderDataSource
    }
    fun observeLookUpUsername(): LiveData<ResultHandler<LeaderboardResponse?>> {
        return onLookUpUsername
    }
    fun getBadgeRelation(): LiveData<Map<Int, FollowerFollowingResponse>> {
        return onBadgeRelation
    }

    fun getReward(): LiveData<RewardsResponse> {
        return onReward
    }

    fun observerProfileChange(): LiveData<StateResource<String>> {
        return onProfileChange
    }

    fun observeInterestChange(): LiveData<ResultHandler<ProfileGetResponse?>> {
        return onInterestChange
    }

    fun observeProfilePictureChange(): LiveData<ResultHandler<FileUploadStatus<ProfileGetResponse?>>> {
        return onProfilePictureChange
    }

    fun observeSpotlightChange():LiveData<ProfileGetResponse?>{
        return onSpotLightPictureChange
    }


    fun observeFullProfileChange(): LiveData<ResultHandler<ProfileGetResponse?>> {
        return onProfileDataChange
    }
    fun observeSavedAddress(): MediatorLiveData<Response<JsonObject?>> {
        return onSaveAddress
    }
    fun observeNewVisits(): LiveData<ResultHandler<JsonObject?>> {
        return onNewVisits
    }
    fun observeMinVersion(): LiveData<ResultHandler<JsonObject?>> {
        return onMinVersion
    }
    fun observeOnSendOtp(): LiveData<ResultHandler<JsonObject?>> {
        return onSendOtp
    }
    fun observeOnChangeCred(): LiveData<ResultHandler<JsonObject?>> {
        return onChangeCredential
    }

    fun observeGetAddress(): MediatorLiveData<ResultHandler<Pair<AddressModelResponse?,AddressModel?>>> {
        return onGetAddress
    }
    fun observeDeleteAddress(): MediatorLiveData<ResultHandler<JsonObject?>> {
        return onDeleteAddress
    }
    fun observeGetInsight(): MediatorLiveData<ResultHandler<InsightResponse?>> {
        return onGetInsight
    }

    fun observerFullOtherProfileChange(): LiveData<ResultHandler<OtherProfileGetResponse?>> {
        return onOtherProfileDataChange
    }

    fun observeFullInterestChange(): LiveData<ResultHandler<FullInterestGetResponse?>> {
        return onFullInterestDataChanges
    }

    fun observePreVerifyChange(): LiveData<ResultHandler<PreVerifyDataRes?>> {
        return onPreVerifydatares
    }


    fun observeEmergencyApi():LiveData<StateResource<String>>{
        return onEmergencyMessageSent
    }

    fun observeQuoteChange():LiveData<String?>{
        return onQuoteChange
    }


    fun observeErro():LiveData<String>{
        return onError
    }

    fun observeUploadDocument():LiveData<FileUploadStatus<Response<ProfileGetResponse?>>>{
        return onUploadDocument
    }

    fun observeFollowUnFollow():LiveData<Boolean>{
        return onFollowUnfollow
    }

    fun observeOnSuggestion(): LiveData<ResultHandler<SuggestionResponse?>> {
        return onSuggestion
    }

    fun observeException(): LiveData<ErrorResponse?> {
        return errorResponse
    }

    fun observePeopleSearch(): LiveData<ResultHandler<SearchPeopleResponse?>>{
        return onPeopleSearch
    }



    //logout
    fun signOut() {
        repository.signOut()
        PreferencesManager.deleteSavedData()
        OneSignal.disablePush(true)
    }


    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}