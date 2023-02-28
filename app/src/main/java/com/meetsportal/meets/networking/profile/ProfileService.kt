package com.meetsportal.meets.networking.profile

import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.gson.JsonObject
import com.meetsportal.meets.BuildConfig
import com.meetsportal.meets.models.*
import com.meetsportal.meets.networking.Api
import com.meetsportal.meets.networking.ApiClient
import com.meetsportal.meets.networking.ErrorResponse
import com.meetsportal.meets.networking.FileUploadStatus
import com.meetsportal.meets.networking.exception.ApiException
import com.meetsportal.meets.networking.places.LeaderboardResponse
import com.meetsportal.meets.networking.places.RewardsResponse
import com.meetsportal.meets.networking.post.PostLikerResponse
import com.meetsportal.meets.service.FirebaseAuthSource
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.DataConverter
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import org.reactivestreams.Publisher
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList


@Singleton
class ProfileService @Inject constructor(val storageReference: StorageReference){


    @Inject
    lateinit var firebaseAuth: FirebaseAuthSource

    @Inject
    lateinit var fireStore: FirebaseFirestore

    private var api : Api = ApiClient.getRetrofit()

    private var apiChache : Api = ApiClient.getCacheRetrofit()





    fun uploadProfilePicture(image: Bitmap):Flowable<FileUploadStatus<ProfileGetResponse?>> {
        return Flowable.create(FlowableOnSubscribe{ imageUploadStatus ->


                var percent: String? = null

                MediaManager.get().upload(DataConverter.convertImage2ByteArray(image))
                    .unsigned(BuildConfig.SERVER_CODE)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {
                            Log.i("TAG", " onStart:::   ")
                        }

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                            var p = ((bytes * 100) / totalBytes).toString()
                            if(p != percent){
                                Log.i("TAG", " PROGRESS:::   ${p}")
                                percent = p
                                imageUploadStatus.onNext(FileUploadStatus(percent = percent))
                            }
                        }

                        override fun onSuccess(
                            requestId: String?,
                            resultData: MutableMap<Any?, Any?>?
                        ) {
                            Log.i("TAG", " success ${resultData?.keys}")
                            Log.i("TAG", " success:: ${resultData.toString()}")

                            var url = resultData?.get("secure_url").toString()
                            var request = JsonObject()
                            request.addProperty("profile_image_url",url)
                            request.addProperty("lite_profile_image_url",url)
                            var d =api?.updateProfilePic(request)
                                ?.doOnSubscribe {  }
                                ?.doOnTerminate {  }
                                ?.subscribeOn(Schedulers.io())
                                ?.observeOn(AndroidSchedulers.mainThread())
                                ?.toFlowable(BackpressureStrategy.BUFFER)
                                ?.subscribe({
                                    if (it.isSuccessful) {
                                        imageUploadStatus.onNext(FileUploadStatus<ProfileGetResponse?>(response = it.body()))
                                    }
                                }, {
                                    Log.i("TAG", " error:: ${it.stackTrace} 2:: ${it.message}")
                                    imageUploadStatus.onError(it)
                                })
                            //returnResult.onComplete()
                            //emitter.onNext(ImageUploadStatus(url = url))
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            Log.i("TAG", " onError:::  ${error?.code}--${error?.description}")
                            imageUploadStatus.onError(Exception(error?.description))


                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            Log.i("TAG", " onReschedule:::   ")
                        }

                    }).dispatch()

        },BackpressureStrategy.DROP)

    }

    fun uploadCoverPicture(image: Bitmap):Flowable<FileUploadStatus<ProfileGetResponse?>> {
        return Flowable.create(FlowableOnSubscribe{ imageUploadStatus ->


                var percent: String? = null

                MediaManager.get().upload(DataConverter.convertImage2ByteArray(image))
                    .unsigned(BuildConfig.SERVER_CODE)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {
                            Log.i("TAG", " onStart:::   ")
                        }

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                            var p = ((bytes * 100) / totalBytes).toString()
                            if(p != percent){
                                Log.i("TAG", " PROGRESS:::   ${p}")
                                percent = p
                                imageUploadStatus.onNext(FileUploadStatus(percent = percent))
                            }
                        }
                        override fun onSuccess(
                            requestId: String?,
                            resultData: MutableMap<Any?, Any?>?
                        ) {
                            val url = resultData?.get("secure_url").toString()
                            val request = JsonObject()
                            request.addProperty("wallpaper_url",url)
                            var d =api?.updateCoverPicture(request)
                                ?.doOnSubscribe {  }
                                ?.doOnTerminate {  }
                                ?.subscribeOn(Schedulers.io())
                                ?.observeOn(AndroidSchedulers.mainThread())
                                ?.toFlowable(BackpressureStrategy.BUFFER)
                                ?.subscribe({
                                    if (it.isSuccessful) {
                                        imageUploadStatus.onNext(FileUploadStatus<ProfileGetResponse?>(response = it.body()))
                                    }
                                }, {
                                    Log.i("TAG", " error:: ${it.stackTrace} 2:: ${it.message}")
                                    imageUploadStatus.onError(it)
                                })
                            //returnResult.onComplete()
                            //emitter.onNext(ImageUploadStatus(url = url))
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            Log.i("TAG", " onError:::  ${error?.code}--${error?.description}")
                            imageUploadStatus.onError(Exception(error?.description))


                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            Log.i("TAG", " onReschedule:::   ")
                        }

                    }).dispatch()

        },BackpressureStrategy.DROP)

    }

    fun updateSpotLightPicture(image: Bitmap, spotlights: ArrayList<Spotlight?>, index: Int):Flowable<Response<ProfileGetResponse?>> {
        return Flowable.create(FlowableOnSubscribe<String> { emitter ->
            //create new user

            MediaManager.get().upload(DataConverter.convertImage2ByteArray(image))
                .unsigned(BuildConfig.SERVER_CODE)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.i("TAG", " onStart:::   ")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        Log.i("TAG", " PROGRESS:::   ${bytes}")
                    }

                    override fun onSuccess(
                        requestId: String?,
                        resultData: MutableMap<Any?, Any?>?
                    ) {
                        Log.i("TAG", " success ${resultData?.keys}")
                        Log.i("TAG", " success:: ${resultData.toString()}")

                        emitter.onNext(resultData?.get("secure_url").toString())
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.i("TAG", " onError:::   ")

                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        Log.i("TAG", " onReschedule:::   ")
                    }

                }).dispatch()
        }, BackpressureStrategy.BUFFER).concatMap { url ->
            //todo:didnt dispose
            Publisher{ returnResult ->
                var removingSpotlight = ArrayList<Spotlight?>()

                when(index){
                    1 -> {
                        spotlights.map { spotlight ->
                            spotlight?.one?.let {
                                removingSpotlight.add(spotlight)
                            }
                        }
                        spotlights.removeAll(removingSpotlight)
                        spotlights.add(0,Spotlight(one = url))
                    }
                    2 -> {
                        spotlights.map { spotlight ->
                            spotlight?.two?.let {
                                removingSpotlight.add(spotlight)
                            }
                        }
                        spotlights.removeAll(removingSpotlight)
                        spotlights.add(1,Spotlight(two = url))
                    }
                    3 -> {
                        spotlights.map { spotlight ->
                            spotlight?.three?.let {
                                removingSpotlight.add(spotlight)
                            }
                        }
                        spotlights.removeAll(removingSpotlight)
                        spotlights.add(2,Spotlight(three = url))
                    }
                }

                var finallist  = arrayOf(Spotlight(),Spotlight(),Spotlight())

                spotlights.map{ spotlight ->
                    spotlight?.one?.let{
                        finallist.set(0,spotlight)
                    }
                    spotlight?.two?.let{
                        finallist.set(1,spotlight)
                    }
                    spotlight?.three?.let{
                        finallist.set(2,spotlight)
                    }
                }


                var finalSpotlight = finallist.toCollection(ArrayList<Spotlight?>())

                Log.i("TAG"," checking:: finalspotlight::  ${finalSpotlight}")
                api.updateProfile(ProfileUpdateRequest(spotlights = finalSpotlight))
                    .doOnSubscribe {  }
                    .doOnTerminate {  }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .toFlowable(BackpressureStrategy.BUFFER)
                    .subscribe({
                        if (it.isSuccessful) {
                            Log.i("TAG", " isSuccessful::: ${url} ")
                            returnResult.onNext(it)
                        }
                    }, {
                        Log.i("TAG", " error:: ${it.stackTrace} 2:: ${it.message}")
                        returnResult.onError(it)
                    })
            }
        }

    }

    private fun fillUpSpotlight(spotlights: ArrayList<Spotlight?>,index: Int) {
        when(spotlights.size){
            0 -> {
                spotlights.add(Spotlight())
                spotlights.add(Spotlight())
            }
            1 ->{
                spotlights.add(Spotlight())
            }
        }
    }


    fun uploadVaccineCard(uri: Uri,type:String):Flowable<FileUploadStatus<Response<ProfileGetResponse?>>> {
        return Flowable.create(FlowableOnSubscribe{ imageUploadStatus ->


            var percent: String? = null

            MediaManager.get().upload(uri)
                .option("resource_type", "auto")
                .unsigned(BuildConfig.SERVER_CODE)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.i("TAG", " onStart:::   ")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        var p = ((bytes * 100) / totalBytes).toString()
                        if(p != percent){
                            Log.i("TAG", " PROGRESS:::   ${p}")
                            percent = p
                            imageUploadStatus.onNext(FileUploadStatus(percent = percent))
                        }
                    }

                    override fun onSuccess(
                        requestId: String?,
                        resultData: MutableMap<Any?, Any?>?
                    ) {
                        Log.i("TAG", " success ${resultData?.keys}")
                        Log.i("TAG", " success:: ${resultData.toString()}")

                        var url = resultData?.get("secure_url").toString()
                        var d =api?.uploadDocument(
                            UploadDocument(type = type,
                                urls = listOf<String>(resultData?.get("secure_url").toString())
                            )
                        )
                            .doOnSubscribe {  }
                            .doOnTerminate {  }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .toFlowable(BackpressureStrategy.BUFFER)
                            .subscribe({
                                if (it.isSuccessful) {
                                    Log.i("TAG"," file:: Uploaded")
                                    api.getFullProfile()
                                        .doOnSubscribe {  }
                                        .doOnTerminate {  }
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .toFlowable(BackpressureStrategy.BUFFER)
                                        .subscribe({
                                            if(it.isSuccessful){
                                                imageUploadStatus.onNext(FileUploadStatus(response = it))
                                            }
                                        })

                                }
                            }, {
                                Log.i("TAG", " error:: ${it.stackTrace} 2:: ${it.message}")
                                imageUploadStatus.onError(it)
                            })
                        //returnResult.onComplete()
                    //emitter.onNext(ImageUploadStatus(url = url))
                }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.i("TAG", " onError:::  ${error?.code}--${error?.description}")
                        imageUploadStatus.onError(Exception(error?.description))


                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        Log.i("TAG", " onReschedule:::   ")
                    }

                }).dispatch()

        },BackpressureStrategy.DROP)

    }

    fun updateInterest(definition: ArrayList<Definition?>):Flowable<Response<ProfileGetResponse?>>{
        return api.updateProfile(ProfileUpdateRequest(cust_data = CustomerData(interests = definition)))
            .doOnSubscribe { }
            .doOnTerminate { }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun deleteSpotLight(spotlights: ArrayList<Spotlight?>):Flowable<Response<ProfileGetResponse?>> {

        when(spotlights.size){
            1 -> {
                spotlights.add(Spotlight())
                spotlights.add(Spotlight())
            }
            2 ->{
                spotlights.add(Spotlight())
            }
        }
        return api.updateProfile(ProfileUpdateRequest(spotlights = spotlights))
            .doOnSubscribe { }
            .doOnTerminate { }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)

    }


    fun getFullProfile():Flowable<Response<ProfileGetResponse?>>{
        return api?.getFullProfile()
            ?.doOnSubscribe{}
            ?.doOnTerminate{}
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.toFlowable(BackpressureStrategy.BUFFER)!!

    }

    fun getOtherProfile(userId: String?, actionSource: String?):Flowable<Response<OtherProfileGetResponse?>>{
//        return api?.getOtherProfile(userId,"view_profile:post:6194cf8cb0e599001351b57a:sid-930-dsb9drhcgzxteu")
        return api?.getOtherProfile(userId,actionSource)
            ?.doOnSubscribe{}
            ?.doOnTerminate{}
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.toFlowable(BackpressureStrategy.BUFFER)!!

    }
    fun getNewVisits():Flowable<Response<JsonObject?>>{
        return api?.getNewVisits()
            ?.doOnSubscribe{}
            ?.doOnTerminate{}
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.toFlowable(BackpressureStrategy.BUFFER)!!

    }
    fun getMinVersion():Flowable<Response<JsonObject?>>{
        return api?.getMinVersion()
            ?.doOnSubscribe{}
            ?.doOnTerminate{}
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.toFlowable(BackpressureStrategy.BUFFER)!!

    }



    fun updateProfile(request: ProfileUpdateRequest):Flowable<Response<ProfileGetResponse?>>{
        return api.updateProfile(request)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)

    }

    fun getFullGenericList(key: String):Flowable<Response<FullInterestGetResponse?>>{
        return api.getGenericList(key)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)

    }

    fun getPreVerifyData():Flowable<Response<PreVerifyDataRes?>>{
        return api.getPreVerifyData()
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)

    }



    fun sendEmergency():Flowable<Response<JSONObject?>>{
        return api.sendEmergency()
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun getRandomQuote():Flowable<Response<JsonObject?>>{
        return api.getRandomQuote()
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun followUser(userId: String?):Flowable<Response<JsonObject>>{
        return api.followUser(userId)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun unBlockUser(userId: String?):Flowable<Response<JsonObject>>{
        return api.unBlockUser(userId)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)
    }


    fun sendLocation(location: Location):Flowable<Response<JsonObject>>{
        var body = JsonObject()
        body.addProperty("lat",location.latitude)
        body.addProperty("lng",location.longitude)
        return api.sendLocation(body)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun getSuggestion():Flowable<Response<SuggestionResponse>>{

        return api.getSuggestion()
            .doOnError {  }
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)
    }




    fun unfollowUser(userId: String?):Flowable<Response<JsonObject>>{
        return api.unfollowUser(userId)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun searchPeople(searchString:String?):Flowable<Response<SearchPeopleResponse?>>{
        return api.searchPeople(searchString)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable(BackpressureStrategy.DROP)

    }

    fun getPostLiker(postId: String?,page:Int):Single<PostLikerResponse>{
        return api.getPostLiker(postId = postId,page = page)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun saveAddress(request: JsonObject): Observable<Response<JsonObject?>> {
        return api.saveAddress(request)

    }
    fun generateOtp(request:JsonObject):Observable<Response<JsonObject?>>{
        return api.generateOtp(request)
    }
    fun changeCredential(request:JsonObject):Observable<Response<JsonObject?>>{
        return api.changeCredential(request)
    }
    fun getInSights(post_id: String?): Observable<Response<InsightResponse?>> {
        return api.getInSights(post_id)

    }
    fun getSavedAddress(page: Int, limit: Int): Observable<Response<AddressModelResponse?>> {
        return api.getSavedAddress(page,limit)
    }
    fun deleteSavedAddress(id: String): Observable<Response<JsonObject?>> {
        return api.deleteAddress(id)
    }

    fun getRelations(sid: String?,relation:String?,page :Int?,limit:Int,match:String?):Single<FollowerFollowingResponse>{
        return api.getRelations(sid = sid,relation = relation,page =page,limit=limit,match = match)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
    fun getLeader(region: String?, otherUserID: String?):Flowable<Response<LeaderboardResponse>>{
        return apiChache.getLeaders(region_code = region,sid = otherUserID)
            .mergeWith(api.getLeaders(region_code = region,sid = otherUserID))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
    fun getLookUserName(username: String?):Single<Response<LeaderboardResponse>>{
        return api.getLookUserName(username = username)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
    fun getReward(): Observable<Response<RewardsResponse>> {
        return api.getRewardComponents()
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun searchRelation(sid: String?,relation:String?,page :Int?,searchString: String?):Single<FollowerFollowingResponse>{
        return api.searchRelation(sid = sid,relation = relation,page =page,searchString = searchString)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }



    fun blockUser(sid:String?):Flowable<Response<JsonObject>>{
        return api.blockUser(sid)
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }

    fun getChargeCount():Flowable<Response<JsonObject>>{
        return api.getChargeCount()
                .toFlowable(BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getBoostPricing(product_id: String?= "boost"): Flowable<Response<BoostPricingModelResponse>>{
        return api.getBoostPricing(product_id)
                .toFlowable(BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
    fun getActiveBoost(): Flowable<Response<ActiveItemResponse>>{
        return api.getActiveBoost()
                .toFlowable(BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getProfileInsight(dateString: String, page: Int):Single<Response<ProfileInsight?>>{
        return api.getProfileInsight(dateString,page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
    fun getViewedMe(page: Int):Single<Response<ViewdMe?>>{
        return api.getViewedMe(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }



    fun topUpBoost(request: JsonObject): Flowable<Response<TopUpBoostModelResponse>> {
        return api.topUpBoost(request)
                .toFlowable(BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
    fun boostPost(request: JsonObject): Flowable<Response<JsonObject>> {
        return api.boostPost(request)
                .toFlowable(BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getMapUsers(latLng: LatLng?,interests : String?):Flowable<Response<MapUserResponse?>>{
        return api.getMapUsers(latLng?.latitude,latLng?.longitude,interesrt = interests)
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }

    fun uploadVerifyProfile(list: Array<Uri?>) :Flowable<Map<Int, String>>{

        var flowable: Flowable<Map<Int,String>> = Flowable.create({},BackpressureStrategy.BUFFER)

        for(i in 0 until list.size) {
            flowable = flowable?.mergeWith(makeCall(list.get(i),i).subscribeOn(Schedulers.io()))
        }
        return flowable//.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    }

    fun makeCall(image : Uri?,index:Int):Flowable<Map<Int,String>> {
        return Flowable.create( FlowableOnSubscribe<Map<Int,String>>{ emitter ->
            try {
                MediaManager.get().upload(image)
                    .unsigned(BuildConfig.SERVER_CODE)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {
                            Log.i("TAG", " onStart:::   ")
                        }

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {

                        }

                        override fun onSuccess(
                            requestId: String?,
                            resultData: MutableMap<Any?, Any?>?
                        ) {
                            Log.i("TAG"," gettingurl:: ${resultData?.get("secure_url").toString()}")
                            var b: HashMap<Int,String>  = HashMap<Int,String> ()
                            b.put(index,resultData?.get("secure_url").toString())
                            emitter.onNext(b)
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            Log.i("TAG", " onError:::  ${error?.code}--${error?.description}")
                            emitter.onError(
                                ApiException(
                                    ErrorResponse(
                                        error?.code,
                                        error?.description
                                    )
                                )
                            );
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            Log.i("TAG", " onReschedule:::   ")
                        }

                    }).dispatch()
            }catch (e : Exception){
                emitter.onError(e)
            }

        },BackpressureStrategy.BUFFER)
    }

    fun verifyProfile(request: JsonObject): Single<Response<JsonObject?>> {
        return api.verifyProfile(request)
    }

    fun verifyTransaction(confirmPurchaseModel: ConfirmPurchaseModel): Single<Response<JsonObject?>> {
        return api.verifyTransaction(confirmPurchaseModel)
    }

    fun getSkus(): Single<Response<JsonObject?>> {
        return api.getSkus()
    }






}