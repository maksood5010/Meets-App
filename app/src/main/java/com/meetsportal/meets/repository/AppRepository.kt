package com.meetsportal.meets.repository

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.gson.JsonObject
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.application.MyApplication.Companion.SID
import com.meetsportal.meets.database.appdatabase.AppDatabase
import com.meetsportal.meets.database.entity.Contact
import com.meetsportal.meets.database.entity.Pinned
import com.meetsportal.meets.models.*
import com.meetsportal.meets.models.Category
import com.meetsportal.meets.networking.FileUploadStatus
import com.meetsportal.meets.networking.directmessage.DM
import com.meetsportal.meets.networking.directmessage.DMModel
import com.meetsportal.meets.networking.meetup.*
import com.meetsportal.meets.networking.notifications.Notification

import com.meetsportal.meets.networking.places.*
import com.meetsportal.meets.networking.post.*
import com.meetsportal.meets.networking.post.datasource.CommentPagingDataSource
import com.meetsportal.meets.networking.post.datasource.PostPagingDataSource
import com.meetsportal.meets.networking.profile.*
import com.meetsportal.meets.networking.profile.dastasource.ProfileInsightPeosonDataSource
import com.meetsportal.meets.networking.registration.*
import com.meetsportal.meets.overridelayout.mention.MentionPerson
import com.meetsportal.meets.service.FirebaseAuthSource
import com.meetsportal.meets.service.FirebaseService
import com.meetsportal.meets.utils.Constant
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList


@Singleton
class AppRepository @Inject constructor(
    var application: Application,
    var fireService: FirebaseService,
    var registrationService: RegistrationApiService,

    ) {


    val TAG = AppRepository::class.simpleName

    var lastDocument: DocumentSnapshot? = null
    var timestamp: String? = null
    var messageTimeStamp: Timestamp? = null

    var latlong: LatLng? = null
    var db: AppDatabase


    @Inject
    lateinit var firebaseService: FirebaseService

    @Inject
    lateinit var firebaseAuthSource: FirebaseAuthSource

    @Inject
    lateinit var profileService: ProfileService

    @Inject
    lateinit var postService: PostService

    @Inject
    lateinit var placeService: PlaceService

    @Inject
    lateinit var meetService: MeetUpService


    init {
        db = AppDatabase.getInstance(application)
    }


    fun getBestMeetUpInitMutableLivedata(): Flowable<QuerySnapshot?>? {

        return firebaseService.getBestMeetUpInitMutableLivedata()
    }

    fun getCategoryListMutableLivedata(): MutableLiveData<ArrayList<Category>> {

        val livePost = MutableLiveData<ArrayList<Category>>()
        livePost.value = arrayListOf(
            Category(
                "Restaurant",
                icon = R.drawable.ic_restaurant,
                filterType = FilterType.cuisine,
                filterValue = ""
            ),
            Category(
                "Coffee shop",
                icon = R.drawable.ic_coffee_shop,
                filterType = FilterType.cuisine,
                filterValue = ""
            ),
            Category(
                "Beach Club",
                icon = R.drawable.ic_beach_club,
                filterType = FilterType.cuisine,
                filterValue = ""
            ),
            Category(
                "Lounge",
                icon = R.drawable.ic_lounge,
                filterType = FilterType.cuisine,
                filterValue = ""
            )
        );
        // livePost.postValue(post);

        return livePost;

        //return firebaseService.getCategoryListMutableLivedata()
    }

    /*fun getAllNewsFeedCard(): MutableLiveData<ArrayList<Post>> {

        return firebaseService.getAllNewsFeedCard()
    }*/

    fun getNewsFeed(): Flowable<QuerySnapshot?>? {
        return firebaseService.getNewsFeed()
    }

    fun getAllNewsFeedSotoryCard(): MutableLiveData<ArrayList<Story>> {

        return firebaseService.getAllNewsFeedSotoryCard()
    }

    /*fun getComments(potId: String): MutableLiveData<ArrayList<Comment>> {

        return firebaseService.getComments(potId)
    }*/

    /*fun uploadPost(post: Post, list: ArrayList<Bitmap>): Completable {
        return firebaseService.uploadPost(post, list)
    }*/


    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuthSource.getCurrentUser()
    }

    fun getCurrentUid(): String? {
        return firebaseAuthSource.getCurrentUid()
    }

    fun getUserinfo(uid: String?): Flowable<DocumentSnapshot?>? {
        return firebaseService.getUserInfo(uid)
    }

    fun updateProfile(profile: UserProfile?): Completable? {
        return firebaseService.updateProfile(profile)
    }

    fun register(email: String?, password: String?, profile: UserProfile?): Completable {
        return firebaseAuthSource.register(email, password, profile)
    }

    //userd with newConcept
    fun login(firebaseToken: String?): Completable? {
        return firebaseAuthSource.login(firebaseToken)
    }

    fun signOut() {
        firebaseAuthSource.signOut()
    }

    fun getNotification() {
        fireService.getNotification()
    }

    /////////---------------------------AWS started-----------------------////

    /////////////////////////////////////ON BOARD //////////////////////////////
    fun getOtp(request: RegistrationRequest): Flowable<Response<RegistrationResponse?>> {
        return registrationService.getOTP(request)
    }

    fun verifyOtp(request: OtpRequest): Flowable<Response<OtpResponse?>> {
        return registrationService.verifyOtp(request)
    }
    fun verifyGoogle(request: JsonObject): Flowable<Response<OtpResponse?>> {
        return registrationService.verifyGoogle(request)
    }
    fun verifyFacebook(request: JsonObject): Flowable<Response<OtpResponse?>> {
        return registrationService.verifyFacebook(request)
    }

    fun setUserName(request: JsonObject): Flowable<Response<OtpResponse?>> {
        return registrationService.setUserName(request)
    }

    fun recoverAccountOtp(request: JsonObject): Observable<Response<JsonObject?>> {
        return registrationService.recoverAccountOtp(request)
    }
    fun recoverVerifyOtp(request: JsonObject): Observable<Response<JsonObject?>> {
        return registrationService.recoverVerifyOtp(request)
    }

    fun getIsUserNameExist(username: String): Flowable<Response<UserNameExistResponse?>> {
        return registrationService.getIsUserNameExist(username)
    }


    ///////////////////////////////////// PROFILE//////////////////////////////

    fun uploadProfilePicture(image: Bitmap): Flowable<FileUploadStatus<ProfileGetResponse?>> {
        return profileService.uploadProfilePicture(image)
    }

    fun uploadCoverPicture(image: Bitmap): Flowable<FileUploadStatus<ProfileGetResponse?>> {
        return profileService.uploadCoverPicture(image)
    }

    fun updateSpotLightPicture(
        image: Bitmap,
        spotlights: ArrayList<Spotlight?>,
        index: Int
    ): Flowable<Response<ProfileGetResponse?>> {
        return profileService.updateSpotLightPicture(image, spotlights, index)
    }

    fun uploadVaccineCard(
        uri: Uri,
        type: String
    ): Flowable<FileUploadStatus<Response<ProfileGetResponse?>>> {
        return profileService.uploadVaccineCard(uri, type)
    }


    fun deleteSpotLight(spotlights: ArrayList<Spotlight?>): Flowable<Response<ProfileGetResponse?>> {
        return profileService.deleteSpotLight(spotlights)
    }

    fun getFullProfile(): Flowable<Response<ProfileGetResponse?>> {
        return profileService.getFullProfile()
    }

    fun getOtherProfile(userId: String?, actionSource: String?): Flowable<Response<OtherProfileGetResponse?>> {
        return profileService.getOtherProfile(userId,actionSource)
    }

    fun getNewVisits(): Flowable<Response<JsonObject?>> {
        return profileService.getNewVisits()
    }

    fun getMinVersion(): Flowable<Response<JsonObject?>> {
        return profileService.getMinVersion()
    }

    fun updateProfile(request: ProfileUpdateRequest): Flowable<Response<ProfileGetResponse?>> {
        return profileService.updateProfile(request)
    }

    fun getFullGenericList(key: String): Flowable<Response<FullInterestGetResponse?>> {
        return profileService.getFullGenericList(key)
    }

    fun getPreVerifyData(): Flowable<Response<PreVerifyDataRes?>> {
        return profileService.getPreVerifyData()
    }

    fun updateInterest(definition: ArrayList<Definition?>): Flowable<Response<ProfileGetResponse?>> {
        return profileService.updateInterest(definition)
    }

    fun sendEmergency(): Flowable<Response<JSONObject?>> {
        return profileService.sendEmergency()
    }

    fun getRandomQuote(): Flowable<Response<JsonObject?>> {
        return profileService.getRandomQuote()
    }

    fun sendLocation(location: Location): Flowable<Response<JsonObject>> {
        return profileService.sendLocation(location)
    }

    fun getSuggestion(): Flowable<Response<SuggestionResponse>> {
        return profileService.getSuggestion()
    }

    fun followUser(userId: String?): Flowable<Response<JsonObject>> {
        return profileService.followUser(userId)
    }

    fun unBlockUser(userId: String?): Flowable<Response<JsonObject>> {
        return profileService.unBlockUser(userId)
    }


    fun unfollowUser(userId: String?): Flowable<Response<JsonObject>> {
        return profileService.unfollowUser(userId)
    }

    fun getRelations(
        sid: String?,
        relations: String?,
        page: Int?,
        limit: Int = 100,
        match: String? = null,
    ): Single<FollowerFollowingResponse> {
        return profileService.getRelations(sid, relations, page, limit, match)
    }

    fun getLeaders(
        code: String?,
        otherUserID: String?,
    ): Flowable<Response<LeaderboardResponse>> {
        return profileService.getLeader(code, otherUserID)
    }

    fun getLookUserName(
        user: String?
    ): Single<Response<LeaderboardResponse>> {
        return profileService.getLookUserName(user)
    }

    fun getReward(): Observable<Response<RewardsResponse>> {
        return profileService.getReward()
    }

    fun searchRelation(
        sid: String?,
        relations: String?,
        page: Int?,
        searchString: String?
    ): Single<FollowerFollowingResponse> {
        return profileService.searchRelation(sid, relations, page, searchString)
    }


    fun blockUser(sid: String?): Flowable<Response<JsonObject>> {
        return profileService.blockUser(sid)
    }

    fun getChargeCount(): Flowable<Response<JsonObject>> {
        return profileService.getChargeCount()
    }

    fun getBoostPricing(product_id: String?= "boost"): Flowable<Response<BoostPricingModelResponse>> {
        return profileService.getBoostPricing(product_id)
    }
    fun getActiveBoost(): Flowable<Response<ActiveItemResponse>> {
        return profileService.getActiveBoost()
    }

    fun getProfileInsight(dateString: String,page: Int = 1):Single<Response<ProfileInsight?>>{
        return profileService.getProfileInsight(dateString,page)
    }

    fun getViewedMe(page: Int = 1):Single<Response<ViewdMe?>>{
        return profileService.getViewedMe(page)
    }





    fun getProfileInsightPersonListDataSource():Flowable<PagingData<ViewdMeItem>>{
        return Pager(
            config = PagingConfig(
                pageSize = 100,
                enablePlaceholders = false,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                ProfileInsightPeosonDataSource(this).apply {
                    //setConfigration(uid, feedOption, isItTimeLine)
                }
            }
        ).flowable
    }

    fun topUpBoost(request: JsonObject): Flowable<Response<TopUpBoostModelResponse>> {
        return profileService.topUpBoost(request)
    }

    fun boostPost(request: JsonObject): Flowable<Response<JsonObject>> {
        return profileService.boostPost(request)
    }

    fun getMapUsers(latLng: LatLng?, interests: String?): Flowable<Response<MapUserResponse?>> {
        return profileService.getMapUsers(latLng, interests)
    }

    fun uploadVerifyProfile(list: Array<Uri?>): Flowable<Map<Int, String>> {
        return profileService.uploadVerifyProfile(list)

    }

    fun verifyProfile(request: JsonObject): Single<Response<JsonObject?>> {
        return profileService.verifyProfile(request)
    }


    /////////////////////////////////////////////////// POST ///////////////////////////////////////////////
    fun uploadPostImages(caption: String, list: ArrayList<Bitmap>): Flowable<Map<Int, String>> {
        return postService.uploadPostImages(caption, list)
    }

    fun uploadPost(
        caption: String,
        list: ArrayList<String>,
        mentionList: ArrayList<MentionPerson>?
    ): Flowable<Response<JsonObject?>> {
        return postService.uploadPost(caption, list, mentionList)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun uploadTextPost(
        text: String?,
        gradientPosition: Int,
        allMentionPerson: ArrayList<MentionPerson>? = null
    ): Flowable<Response<JsonObject?>> {
        return postService.uploadTextPost(text, gradientPosition, allMentionPerson)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun fetchPostFromAws(
        feedOption: Int,
        uid: String?,
        page: String?,
        limit: String?
    ): Single<FetchPostResponse> {

        return postService.fetchPostFromAws(feedOption, uid, page, limit)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    //fun fetchPost(userId:String,page:String?): Flowable<PagingData<FetchPostResponse>> {
    fun fetchPost(
        uid: String?,
        feedOption: Int,
        isItTimeLine: Boolean
    ): Flowable<PagingData<FetchPostResponseItem>> {

        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                PostPagingDataSource(this).apply {
                    setConfigration(uid, feedOption, isItTimeLine)
                }
            }
        ).flowable
    }

    fun toggleLike(appId: String?): Observable<Response<JsonObject?>> {
        return postService.toggleLike(appId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun toggleCommentLike(commentId: String?): Observable<Response<JsonObject?>> {
        return postService.toggleCommentLike(commentId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }


    fun fetchSinglePost(postId: String?, mode: String?): Flowable<Response<SinglePostResponse>> {
        return postService.fetchSinglePost(postId, mode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getComments(
        postId: String?,
        mode: String?,
        entityType: String?,
        page: Int?,
        limit: Int?,
        referenceId: String?
    ): Single<SinglePostComments> {
        return postService.getComments(postId, mode, entityType,page, limit,referenceId)
            .subscribeOn(Schedulers.io())
    }


    fun fetchRecentPost(limit: String = "10"): Observable<Response<FetchPostResponse>> {
        return postService.fetchRecentPost(limit)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

   // var a = CommentPagingDataSource()
    fun fetchComments(postId: String?, mode: String?, entity_type: String?, referenceId: String?,parentCommentPublisher : PublishSubject<SinglePostCommentsItem?>?): Flowable<PagingData<SinglePostCommentsItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 100,
                enablePlaceholders = false,
                prefetchDistance = 1,
            ),
            pagingSourceFactory = {
                CommentPagingDataSource(this).apply {
                    setPost(postId,mode,entity_type,referenceId,parentCommentPublisher)
                }
            }
        ).flowable
    }


    fun publishComment(postId: String?, body: JsonObject): Flowable<Response<JsonObject>> {
        return postService.publishComment(postId, body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun replyComment(postId: String?, body: JsonObject): Flowable<Response<JsonObject>> {
        return postService.replyComment(postId, body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun searchPeople(searchString: String?): Flowable<Response<SearchPeopleResponse?>> {
        return profileService.searchPeople(searchString)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }

    fun getPostLiker(postId: String?, page: Int): Single<PostLikerResponse> {
        return profileService.getPostLiker(postId, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }

    fun saveAddress(request: JsonObject): Observable<Response<JsonObject?>> {
        return profileService.saveAddress(request)
    }

    fun generateOtp(request: JsonObject): Observable<Response<JsonObject?>> {
        return profileService.generateOtp(request)
    }

    fun changeCredential(request: JsonObject): Observable<Response<JsonObject?>> {
        return profileService.changeCredential(request)
    }

    fun getInSights(post_id: String?): Observable<Response<InsightResponse?>> {
        return profileService.getInSights(post_id)
    }

    fun getSavedAddress(page: Int, limit: Int): Observable<Response<AddressModelResponse?>> {
        return profileService.getSavedAddress(page, limit)
    }

    fun deleteSavedAddress(id: String): Observable<Response<JsonObject?>> {
        return profileService.deleteSavedAddress(id)
    }


    fun searchPost(searchString: String?): Flowable<Response<SearchPostResponse>> {
        return postService.searchPost(searchString)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun deleteComment(commentId: String?): Flowable<Response<JsonObject>> {
        return postService.deleteComment(commentId)
    }

    fun deleteCommentReply(commentId: String?): Flowable<Response<JsonObject>> {
        return postService.deleteCommentReply(commentId)
    }

    fun deletePost(postId: String?): Flowable<Response<JsonObject>> {
        return postService.deletePost(postId)
    }

    fun updatePost(postId: String?, request: PostUpdateRequest, ): Flowable<Response<JsonObject>> {
        return postService.updatePost(postId, request)
    }

    fun reportContent(body: JsonObject): Flowable<Response<JsonObject>> {
        return postService.reportContent(body)
    }

    fun ingestPost(body: JsonObject): Flowable<Response<JsonObject>> {
        return postService.ingestPost(body)
    }


    /////////////////////////////////////       Places    //////////////////////////////

    fun getBestMeetUp(
        limit: Int?,
        lat: Double?,
        long: Double?,
        isbestPlace: Boolean?,
        page: Int,
        fields: String? = null,
        filter: String? = null,
        maxDistence: Int? = 20000
    ): Flowable<Response<FetchPlacesResponse>> {
        return placeService.getBestMeetUp(
            limit,
            lat,
            long,
            isbestPlace,
            fields,
            maxDistence = maxDistence,
            filter = filter
        )
    }

    fun getBestMeetUpPage(
        limit: Int?,
        lat: Double?,
        long: Double?,
        isbestPlace: Boolean?,
        page: Int,
        facilities: String?,
        categories: String?,
        cuisines: String?,
        countryCode: String?
    ): Single<FetchPlacesResponse> {
        return placeService.getBestMeetUpPage(
            limit,
            lat,
            long,
            countryCode,
            isbestPlace,
            page,
            facilities,
            categories,
            cuisines,
        )
    }

    fun getSavedPlaces(page: Int, limit: Int = 10): Single<FetchPlacesResponse> {
        return placeService.getSavedPlaces(page, limit)
    }

    fun getNearByPlaceCount(
        lat: Double,
        long: Double,
    ): Flowable<Response<Int?>> {
        return placeService.getNearByPlaceCount(
            lat,
            long,
        )
    }

    fun getCategories(): Flowable<Response<CategoryResponse>> {
        return placeService.getCategories()
    }
    fun getOffers(): Flowable<Response<CategoryResponse>> {
        return placeService.getOffers()
    }

    fun getFacilities(): Flowable<Response<FacilityResponse>> {
        return placeService.getFacilities()
    }

    fun getCuisine(): Flowable<Response<CuisineResponse>> {
        return placeService.getCuisine()
    }

    fun searchPlace(searchString: String?): Flowable<Response<SearchPlaceResponse>> {
        return placeService.searchPlace(searchString)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getPlace(id: String?, mode: String?): Flowable<Response<GetSinglePlaceResponse>> {
        return placeService.getPlace(id, mode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun reviewPlace(id: String?, request: JsonObject): Flowable<Response<JsonObject>> {
        return placeService.reviewPlace(id, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun editReview(
        placeId: String?,
        reviewId: String?,
        request: JsonObject
    ): Flowable<Response<JsonObject>> {
        return placeService.editReview(placeId, reviewId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun checkInPlace(placeId: String?, request: JsonObject): Flowable<Response<JsonObject>> {
        return placeService.checkInPlace(placeId, request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun addBookmark(placeId: String?): Flowable<Response<JsonObject?>> {
        return placeService.addBookmark(placeId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun deleteBookmark(placeId: String?): Flowable<Response<JsonObject?>> {
        return placeService.deleteBookmark(placeId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }


    fun getMyReview(placeId: String?, userId: String?): Flowable<Response<FetchReviewResponse?>> {
        return placeService.getMyReview(placeId, userId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun getReviewCount(placeId: String?): Flowable<Response<JsonObject?>> {
        return placeService.getReviewCount(placeId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }


    fun getMyReviewSingle(placeId: String?, page: Int): Single<FetchReviewResponse?> {
        return placeService.getMyReviewSingle(placeId, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }


    fun getFireNotification(i: Int): Single<Pair<List<Notification>, Boolean>> {
        Log.i(TAG, " page:  $i")
        Log.i(TAG, " getFireNotification:::::: $i  ${timestamp}")

        Log.i(TAG, " loggong:: 21")


        /* if(i ==1 ){
             return Single.create<QuerySnapshot?>({emitter->

             })

         }*/
        return Single.create<Pair<List<Notification>, Boolean>>(SingleOnSubscribe { emitter ->
            Log.i(TAG, " emitter.isDisposed 0::: ${emitter.isDisposed}")
            Log.i(TAG, " FID::: ${FirebaseAuth.getInstance().uid}")

            if (i == 1) {
                getFirstPageNtification(emitter, Source.CACHE)
            } else {
                Log.i(TAG, " secondTimeSuccessfull:: 1 ${timestamp}")
                FirebaseFirestore.getInstance()
                    .collection(Constant.NOTIFICATION_NODE)
                    .document(FirebaseAuth.getInstance().currentUser.uid)
                    .collection(Constant.USER_NOTIFICATION_NODE)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(timestamp)
                    .limit(15)
                    .get(Source.CACHE)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (it.result.documents.isNotEmpty()) {
                                var list = ArrayList<Notification>();
                                try {
                                    it.result?.documents?.map {
                                        //if(!it.getString("type").equals("welcome")){
                                        if (Constant.NOTIFICATION_TYPES.contains(it.getString("type"))) {
                                            list.add(it.toObject(Notification::class.java)!!.apply {
                                                document_id = it.id
                                            })
                                        }
                                    }
                                } catch (e: java.lang.Exception) {
                                    Log.e(TAG, " missMappingHappened::  $e")
                                    e.printStackTrace()
                                }
                                emitter.onSuccess(Pair(list, false))
                                timestamp =
                                    it.result.documents.last().data?.get("timestamp").toString()
                            }
                            Log.i(TAG, "  timestamp::  $timestamp")
                        }
                    }
                    .addOnFailureListener {
                        //emitter.onSuccess(Pair(list, false))
                        emitter.onError(it)
                        Log.i(TAG, " addOnFailureListener:: 2 $it")
                    }
            }

        })


    }

    fun getFirstPageNtification(
        emitter: SingleEmitter<Pair<List<Notification>, Boolean>>,
        source: Source
    ) {
        Log.i(TAG, " This isAlsoWorking:: ${source} ")
        FirebaseFirestore.getInstance()
            .collection(Constant.NOTIFICATION_NODE)
            .document(FirebaseAuth.getInstance().currentUser.uid)
            .collection(Constant.USER_NOTIFICATION_NODE)
            .limit(15)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get(source)
            .addOnSuccessListener {
                if (it.documents.size == 0 && source == Source.CACHE) {
                    getFirstPageNtification(emitter, Source.SERVER)
                    return@addOnSuccessListener
                }
                var list = ArrayList<Notification>();
                Log.i(TAG, " loggong:: 22  ")
                try {
                    it.documents?.map {
                        if (Constant.NOTIFICATION_TYPES.contains(it.getString("type"))) {
                            list.add(it.toObject(Notification::class.java)!!.apply {
                                document_id = it.id
                            })
                        }
                    }
                    Log.i(TAG, " loggong:: 3  ${it.documents.size}")
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, " missMappingHappened::  $e")
                    e.printStackTrace()
                }
                emitter.onSuccess(Pair(list, false))
                if (it.documents.isNotEmpty()) {
                    timestamp = it.documents.last()?.data?.get("timestamp").toString()
                }
            }
            .addOnFailureListener {
                Log.i(TAG, " loggong:: 23  ")
                if (source == Source.CACHE) {
                    Log.e(TAG, " No result from CACHE ::: starting from server::")
                    getFirstPageNtification(emitter, Source.SERVER)
                } else {
                    //emitter.onSuccess(Pair(ArrayList(),false))
                    emitter.onError(it)
                }
                //emitter.onError(it)
            }
    }

    fun getDirectMessage(i: Int, messageThred: String): Single<QuerySnapshot> {
        Log.i(TAG, " page:  $i")
        Log.i(TAG, " getFireNotification:::::: $i  ${timestamp}")


        return Single.create<QuerySnapshot>(SingleOnSubscribe { emitter ->
            if (i == 1) {
                FirebaseFirestore.getInstance()
                    .collection(Constant.DIRECTMESSAGE_NODE).document(messageThred)
                    .collection(Constant.MESSAGE_NODE)
                    .limit(200)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get(Source.CACHE)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            emitter.onSuccess(it.result)
                            //Log.i(TAG," firstTimeSuccessfull:: 3 ${it.result.documents.size}")
                            if (it.result.documents.isNotEmpty()) {
                                Log.i(TAG, " timeStamp::: ${it.result.documents.last()?.data}")
                                messageTimeStamp =
                                    it.result.documents.last()?.data?.get("timestamp") as Timestamp
                            }

                        }
                    }
                    .addOnFailureListener {
                        emitter.onError(it)
                        Log.i(TAG, " addOnFailureListener:: $it")
                    }
            } else {
                Log.i(TAG, " secondTimeSuccessfull:: 1 ${timestamp}")
                FirebaseFirestore.getInstance()
                    .collection(Constant.DIRECTMESSAGE_NODE).document(messageThred)
                    .collection(Constant.MESSAGE_NODE)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(messageTimeStamp)
                    .limit(200)
                    .get(Source.CACHE)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (it.result.documents.isNotEmpty()) {
                                //Log.i(TAG," secondTimeSuccessfull:: 33 notempty ${it.result.documents}")
                                emitter.onSuccess(it.result)
                                messageTimeStamp =
                                    it.result.documents.last().data?.get("timestamp") as Timestamp
                            }
                            Log.i(TAG, "  timestamp::  $timestamp")
                        }
                    }
                    .addOnFailureListener {
                        emitter.onError(it)
                        Log.i(TAG, " addOnFailureListener:: 2 $it")
                    }
            }
        })
    }

    fun setDMProfilePic(prifilePic: String?, messageThread: String) {


        var document = FirebaseFirestore.getInstance()
            .collection(Constant.DIRECTMESSAGE_NODE).document(messageThread)


        document.get(Source.CACHE)
            .addOnSuccessListener {

                var gg = it.toObject(DMParent::class.java)
                Log.i(TAG, " setProfilePic:: $gg")
                gg?.profiles?.let {
                    var profiles = ArrayList(it)
                    var myProfile = it.find { it?.sid == SID }
                    profiles.remove(it.find { it?.sid == SID })
                    profiles.add(myProfile?.apply { profile_image_url = prifilePic })
                    document.update(
                        mapOf(
                            "profiles" to profiles
                        )
                    )
                    //}
                }
            }
    }

    fun sendMessage(
        messageThred: String,
        message: String,
        parentMsg: DMModel.MyDM? = null,
        type: String = "text",
        messageSent: () -> Unit,
        thumbUrl: String? = null,
        passPhrase: String? = null,
        ): DocumentReference {
        return fireService.sendMessage(
            messageThred,
            message,
            parentMsg,
            type,
            messageSent,
            thumbUrl,
                passPhrase=passPhrase
        )
    }


    fun sendMeetMessage(
        messageThred: String,
        message: String?,
        parentMsg: ChatDM? = null,
        type: String = "text",
        messageSent: () -> Unit
    ): DocumentReference {
        Log.i(TAG, " sendMeetMessage::: 1")
        var document = FirebaseFirestore.getInstance()
            .collection(Constant.MEETUP_CHATS).document(messageThred)
            .collection(Constant.MESSAGE_NODE)
            .document()


        Log.i(TAG, " sendMeetMessage::: 2")
        var messageMap = mutableMapOf(
            "id" to document.id,
            "type" to type,
            "timestamp" to FieldValue.serverTimestamp(),
            "body" to message,
            "senderFid" to FirebaseAuth.getInstance().currentUser!!.uid,
            "senderSid" to SID
        )

        Log.i(TAG, " sendMeetMessage::: 3")
        parentMsg?.let {
            messageMap.put("parentMsg", it)
        }

        Log.i(TAG, " sendMeetMessage::: 4")
        document.set(
            messageMap
        ).addOnSuccessListener {
            Log.i(TAG, " message sent Successfully")
            messageSent.invoke()
        }

        Log.i(TAG, " sendMeetMessage::: 5")
        FirebaseFirestore.getInstance()
            .collection(Constant.MEETUP_CHATS).document(messageThred)
            .update(
                mapOf(
                    "lastMessage" to messageMap,
                    "lastMessageAddedAt" to FieldValue.serverTimestamp(),
                )
            )
        Log.i(TAG, " sendMeetMessage::: 5")

        return document
    }

    fun getDeletedTime(messageThred: String): Single<Timestamp> {
        Log.i(TAG, " getMessageOfPage::: ")


        return Single.create<Timestamp>(SingleOnSubscribe { emitter ->
            //    val key = if(messageThread.indexOf(FirebaseAuth.getInstance().uid!!) == 0) "deleted1" else "deleted2"
            getDeletedTimeStamp(messageThred, emitter, Source.CACHE)

        })
    }

    private fun getDeletedTimeStamp(
        messageThred: String,
        emitter: SingleEmitter<Timestamp>,
        source: Source
    ) {
        FirebaseFirestore.getInstance().collection(Constant.DIRECTMESSAGE_NODE)
            .document(messageThred)
            .get(source).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.exists()) {
                        val key =
                            if (messageThred.indexOf(FirebaseAuth.getInstance().uid!!) == 0) "deleted1" else "deleted2"
                        val deletedTimestamp: Timestamp? = it.result.get(key) as Timestamp?
                        Log.i(TAG, " getChatChanges:: 2 :deletedTimestamp : $deletedTimestamp")
                        emitter.onSuccess(deletedTimestamp ?: Timestamp(0, 0))
                    }
                }
            }.addOnFailureListener {
                if (source == Source.CACHE)
                    getDeletedTimeStamp(messageThred, emitter, Source.SERVER)
                else
                    emitter.onError(it)
                Log.i(TAG, " addOnFailureListener:: 2 $it")
            }

    }


    fun getMessageOfPage(
        timestamp: Timestamp?,
        messageThred: String,
        deletedTimestamp: Timestamp?
    ): Single<List<DM>> {
        return Single.create<List<DM>>(SingleOnSubscribe { emitter ->
            getMessageOfFirstPageService(
                timestamp,
                messageThred,
                deletedTimestamp,
                emitter,
                Source.CACHE
            )
        })
    }

    private fun getMessageOfFirstPageService(
        timestamp: Timestamp?,
        messageThred: String,
        deletedTimestamp: Timestamp?,
        emitter: SingleEmitter<List<DM>>,
        source: Source
    ) {
        timestamp?.let {
            Log.i(TAG, " timestamp is not null:: 1 ${deletedTimestamp}")
            FirebaseFirestore.getInstance().collection(Constant.DIRECTMESSAGE_NODE)
                .document(messageThred).collection(Constant.MESSAGE_NODE)
                .whereGreaterThan("timestamp", deletedTimestamp ?: Timestamp(0, 0))
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(it)
                .limit(30)
                .get(source).addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result.documents.isNotEmpty()) {
                            emitter.onSuccess(it.result.toObjects(DM::class.java))
                        }
                        Log.i(TAG, "  timestamp::  $timestamp")
                    }
                }.addOnFailureListener {
                    if (source == Source.CACHE)
                        getMessageOfFirstPageService(
                            timestamp,
                            messageThred,
                            deletedTimestamp,
                            emitter,
                            Source.SERVER
                        )
                    else
                        emitter.onError(it)
                    Log.i(TAG, " addOnFailureListener:: 2 $it")
                }
            Log.i(TAG, " secondTimeSuccessfull:: end")

        } ?: run {
            Log.i(TAG, " timestamp is null:: 1 ${deletedTimestamp}")
            FirebaseFirestore.getInstance().collection(Constant.DIRECTMESSAGE_NODE)
                .document(messageThred).collection(Constant.MESSAGE_NODE)
                .whereGreaterThan("timestamp", deletedTimestamp ?: Timestamp(0, 0))
//                        .whereNotEqualTo(blockKey,true)
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(30).get(source)
                .addOnSuccessListener {
                    if (it.documents.isNotEmpty()) {
                        emitter.onSuccess(it.toObjects(DM::class.java))
                    } else {
                        if (source == Source.CACHE)
                            getMessageOfFirstPageService(
                                timestamp,
                                messageThred,
                                deletedTimestamp,
                                emitter,
                                Source.SERVER
                            )
                    }
                }.addOnFailureListener {
                    Log.i(TAG, " OnComplete::: Faile value came $$source")
                    if (source == Source.CACHE)
                        getMessageOfFirstPageService(
                            timestamp,
                            messageThred,
                            deletedTimestamp,
                            emitter,
                            Source.SERVER
                        )
                    else {
                        emitter.onError(it)
                    }
                    Log.i(TAG, " addOnFailureListener:: 2 $it")
                }
        }
    }


    fun getChatChanges(
        messageThread: String,
        myDM: Timestamp?,
        deletedMsgTimestamp: Timestamp?
    ): Single<List<DM>> {
        return Single.create<List<DM>>(SingleOnSubscribe { emitter ->
            var previousTimeStamp: Timestamp? = myDM
            if (myDM == null && deletedMsgTimestamp == null) {
                previousTimeStamp = Timestamp(Date())
            }
            Log.d(TAG, "getChatChanges: deletedMsgTimestamp: $deletedMsgTimestamp ")
            Log.d(TAG, "getChatChanges: previousTimeStamp: $previousTimeStamp ")
            FirebaseFirestore.getInstance().collection(Constant.DIRECTMESSAGE_NODE)
                .document(messageThread).collection(Constant.MESSAGE_NODE)
                .whereGreaterThan(
                    "timestamp",
                    deletedMsgTimestamp ?: previousTimeStamp ?: Timestamp(0, 0)
                )
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .startAfter(previousTimeStamp)
                .get(Source.CACHE).addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result.documents.isNotEmpty()) {
                            emitter.onSuccess(it.result.toObjects(DM::class.java))
                        }
                        Log.i(TAG, "  timestamp::  $timestamp")
                    }
                }.addOnFailureListener {
                    emitter.onError(it)
                    Log.i(TAG, " addOnFailureListener:: 2 $it")
                }
        })
    }

    fun getMeetChatChanges(messageThread: String, myDM: ChatDM?): Single<List<ChatDM>> {
        return Single.create<List<ChatDM>>(SingleOnSubscribe { emitter ->

            FirebaseFirestore.getInstance()
                .collection(Constant.MEETUP_CHATS).document(messageThread)
                .collection(Constant.MESSAGE_NODE)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .startAfter(myDM?.timestamp)
                .get(Source.CACHE)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result.documents.isNotEmpty()) {
                            try {
                                emitter.onSuccess(it.result.toObjects(ChatDM::class.java))
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Log.e(TAG, " ${e.message}")
                            }

                        }
                        Log.i(TAG, "  timestamp::  $timestamp")
                    }
                }
                .addOnFailureListener {
                    emitter.onError(it)
                    Log.i(TAG, " addOnFailureListener:: 2 $it")
                }
        })
    }

    fun getAllChat(limit: Long? = 1000): Single<List<DMParent>> {
        return Single.create<List<DMParent>>(SingleOnSubscribe { emitter ->

            FirebaseFirestore.getInstance().collection(Constant.DIRECTMESSAGE_NODE)
                .orderBy("lastMessageAddedAt", Query.Direction.DESCENDING)
                .whereArrayContains("sids", SID.toString()).limit(limit ?: 1000L)
                .get(Source.CACHE).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.i(TAG, " getChatChanges:: 2")
                        if (it.result.documents.isNotEmpty()) {
                            Log.i(TAG, " getAllChat:::   ${it.result.documents.size}")
                            //var list = it.result.toObjects(DMParent::class.java)
                            var list = ArrayList<DMParent>()
                            it.result.documents.map {

                                val temp = it.toObject(DMParent::class.java)?.apply {
                                    id = it.id
                                }
                                if (temp?.id?.indexOf(FirebaseAuth.getInstance().uid!!) == 0) {
                                    temp.deleted1?.let {
                                        if (temp.lastMessageAddedAt?.compareTo(temp.deleted1) == 1) {
                                            list.add(temp)
                                        }
                                    } ?: run {
                                        list.add(temp)
                                    }
                                } else {
                                    temp?.deleted2?.let {
                                        if (temp.lastMessageAddedAt?.compareTo(temp.deleted2) == 1) {
                                            list.add(temp)
                                        }
                                    } ?: run {
                                        list.add(temp ?: DMParent())
                                    }
                                }


                                /* list.add(it.toObject(DMParent::class.java)!!.apply {
                                     id = it.id
                                 })*/
                            }
                            emitter.onSuccess(list)
                        }
                    }
                }.addOnFailureListener {
                    emitter.onError(it)
                    Log.i(TAG, " addOnFailureListener:: 2 $it")
                }
        })
    }

    fun getAllUnreadChat(limit: Long? = 1000): Single<List<DMParent>> {
        return Single.create<List<DMParent>>(SingleOnSubscribe { emitter ->

            FirebaseFirestore.getInstance()
                .collection(Constant.DIRECTMESSAGE_NODE)
                .orderBy("lastMessageAddedAt", Query.Direction.DESCENDING)
                .whereArrayContains("sids", SID.toString())
                .limit(limit ?: 1000L)
                .get(Source.CACHE)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result.documents.isNotEmpty()) {
                            Log.i(TAG, " getAllChat:::   ${it.result.documents.size}")
                            //var list = it.result.toObjects(DMParent::class.java)
                            var list = ArrayList<DMParent>()
                            it.result.documents.map {
                                list.add(it.toObject(DMParent::class.java)!!.apply {
                                    id = it.id
                                })
                            }
                            emitter.onSuccess(list)
                        }
                    }
                }
                .addOnFailureListener {
                    emitter.onError(it)
                    Log.i(TAG, " addOnFailureListener:: 2 $it")
                }
        })
    }

    fun getOnlineStatus(fidList: List<String?>): Single<List<FBOnlineStatus?>> {

        Log.i(TAG, " GettingOnline status:: ")
        return Single.create<List<FBOnlineStatus?>>(SingleOnSubscribe { emitter ->
            getOnlineStatusService(fidList, emitter, Source.CACHE)

        })
    }

    fun getOnlineStatusService(
        fidList: List<String?>,
        emitter: SingleEmitter<List<FBOnlineStatus?>>,
        source: Source
    ) {

        FirebaseFirestore.getInstance()
            .collection(Constant.STATUS_NODE)
            .whereIn(FieldPath.documentId(), fidList)
            .get(source)
            .addOnSuccessListener {
                if (it.documents.isNotEmpty()) {
                    Log.i(TAG, " getOnlineStatus:::   ${it.documents.size}")
                    var list = ArrayList<FBOnlineStatus?>()
                    it.documents.map {
                        list.add(it.toObject(FBOnlineStatus::class.java))
                    }
                    emitter.onSuccess(list)
                } else {
                    Log.e(TAG, " Data nay alla:: ")
                    if (source == Source.CACHE) {
                        getOnlineStatusService(fidList, emitter, Source.SERVER)
                    }
                }
            }.addOnFailureListener {
                if (source == Source.CACHE) {
                    getOnlineStatusService(fidList, emitter, Source.SERVER)
                } else {
                    emitter.onError(it)
                }
                Log.i(TAG, " addOnFailureListener:: 2 $it")
            }
    }

    fun readMessages(messageThred: String) {

        val key = if(messageThred.indexOf(FirebaseAuth.getInstance().uid!!) == 0) "unread1" else "unread2"

        FirebaseFirestore.getInstance()
            .collection(Constant.DIRECTMESSAGE_NODE).document(messageThred)
            .update(
                mapOf(
                    key to 0
                )
            )
    }

    fun setPinned(sid: String?): Observable<Unit> {
        return Observable.create<Unit> { emitter ->
            sid?.let {
                db.pinDao().insert(Pinned(sid, Date().time))
                emitter.onNext(Unit)
            }
        }

    }

    fun deleteChat(messageThread: String): Observable<Unit> {
        val key =
            if (messageThread.indexOf(FirebaseAuth.getInstance().uid!!) == 0) "deleted1" else "deleted2"
        return Observable.create<Unit> { emitter ->
            FirebaseFirestore.getInstance().collection(Constant.DIRECTMESSAGE_NODE)
                .document(messageThread).update(mapOf(key to FieldValue.serverTimestamp()))
            emitter.onNext(Unit)
        }

    }

    fun setUnPinned(sid: String?): Observable<Unit> {
        return Observable.create<Unit> { emitter ->
            sid?.let {
                db.pinDao().delete(sid)
                emitter.onNext(Unit)
            }
        }

    }

    fun getAllPin(): Observable<List<Pinned>> {
        return Observable.create<List<Pinned>> { emitter ->
            emitter.onNext(db.pinDao().getAllNotes())
        }
    }

    fun uploaDMImages(list: ArrayList<Bitmap?>): Flowable<Map<Int, Any>> {
        return fireService.uploadPDmImages(list)
    }

    fun uploadDMVideo(
        context: Context,
        uri: Uri,
        thumb: Bitmap?,
        messageId: String,
        myDM: DMModel.MyDM?,
        passPhrase: String? = null,
    ): Flowable<Map<Int, Any>> {
        return fireService.uploadDmVideo(context, uri, thumb, messageId, myDM,passPhrase)
    }

    fun getGroupChat(
        timestamp: Timestamp?,
        messageThred: String
    ): Single<List<ChatDM>> {
        return Single.create<List<ChatDM>>(SingleOnSubscribe { emitter ->

            Log.i(TAG, " getGroupChat:: 1 ${timestamp}")
            Log.i(TAG, " getGroupChat:: 2 ${messageThred}")
            timestamp?.let {
                Log.i(TAG, " pageNumber:::: 1 ")
                FirebaseFirestore.getInstance()
                    .collection(Constant.MEETUP_CHATS).document(messageThred)
                    .collection(Constant.MESSAGE_NODE)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(it)
                    .limit(30)
                    .get(Source.CACHE)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (it.result.documents.isNotEmpty()) {
                                //Log.i(TAG," secondTimeSuccessfull:: 33 notempty ${it.result.documents}")
                                //it.result.documents.map {  }
                                emitter.onSuccess(it.result.toObjects(ChatDM::class.java))
                            }
                            Log.i(TAG, "  timestamp::  $timestamp")
                        }
                    }
                    .addOnFailureListener {
                        emitter.onError(it)
                        Log.i(TAG, " addOnFailureListener:: 2 $it")
                    }

            } ?: run {
                Log.i(TAG, " pageNumber:::: 2 ")
                FirebaseFirestore.getInstance()
                    .collection(Constant.MEETUP_CHATS).document(messageThred)
                    .collection(Constant.MESSAGE_NODE)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(30)
                    .get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.i(TAG, " secondTimeSuccessfull:: 2 ${it.result.documents.size}")
                            if (it.result.documents.isNotEmpty()) {
                                //Log.i(TAG," secondTimeSuccessfull:: 33 notempty ${it.result.documents}")
                                //it.result.documents.map {  }
                                emitter.onSuccess(it.result.toObjects(ChatDM::class.java))
                            }
                            Log.i(TAG, "  timestamp::  $timestamp")
                        }
                    }
                    .addOnFailureListener {
                        emitter.onError(it)
                        Log.i(TAG, " addOnFailureListener:: 2 $it")
                    }
            }
        })
    }


    fun votePlace(id: String?, fbChatId: String, maxRemainVote: Int?): Single<Unit> {

        return Single.create<Unit>(SingleOnSubscribe { emitter ->
            var data = mapOf<String, Any?>(
                "type" to "meets",
                "id" to id,
                "timestamp" to FieldValue.serverTimestamp(),
                "animSeen" to false,
                "sid" to MyApplication.SID,
                "max_vote_changes" to maxRemainVote
            )

            FirebaseFirestore.getInstance()
                .collection(Constant.MEETUP_CHATS)
                .document(fbChatId)
                .set(
                    mapOf(
                        "votes" to mapOf<String?, Any>(MyApplication.SID to data),
                    ), SetOptions.merge()
                )
                .addOnSuccessListener {
                    emitter.onSuccess(Unit)
                    Log.i(TAG, " sucessFully:::: ")
                }.addOnFailureListener {
                    emitter.onError(it)
                    Log.i(TAG, " failed to Vote ${it.message}--${it.printStackTrace()}")
                }
        })
    }

    fun updateMeetUpinFireBase(fbChatId: String, voteChnge: Int) {
        FirebaseFirestore.getInstance()
            .collection(Constant.MEETUP_CHATS)
            .document(fbChatId)
            .get()
            .addOnSuccessListener {
                var meetFireData = it.toObject(MeetFireData::class.java)
                meetFireData?.votes?.map { it.value.max_vote_changes =  voteChnge; it.value.maxVotUpdated = true }
                FirebaseFirestore.getInstance()
                    .collection(Constant.MEETUP_CHATS)
                    .document(fbChatId)
                    .update(
                        mapOf("votes" to meetFireData?.votes,
                        "lastUpdated" to FieldValue.serverTimestamp())
                    )
                    .addOnSuccessListener {
                        Log.e(TAG," vote count update successfully!!!")
                    }.addOnFailureListener {
                        Log.e(TAG," vote count update Failure!!!")
                    }
            }
    }

    fun updateMaxVotUpdateToFalse(fbChatId: String, myVote: Vote) {

        FirebaseFirestore.getInstance()
            .collection(Constant.MEETUP_CHATS)
            .document(fbChatId)
            .set(
                mapOf(
                    "votes" to mapOf<String?, Any>(MyApplication.SID to myVote),
                ), SetOptions.merge()
            )
            .addOnSuccessListener {
                Log.e(TAG," maxVotedUpdate:: updated successfully")
            }
            .addOnFailureListener {
                Log.e(TAG," maxVotedUpdate:: failed to update")
            }

    }

    fun confirmAnimSeen(
        myVote: Vote,
        fbChatId: String
    ): Single<Unit> {

        return Single.create<Unit>(SingleOnSubscribe { emitter ->
            FirebaseFirestore.getInstance()
                .collection(Constant.MEETUP_CHATS)
                .document(fbChatId)
                .set(
                    mapOf(
                        "votes" to mapOf<String?, Any>(MyApplication.SID to myVote),
                    ), SetOptions.merge()
                )
                .addOnSuccessListener {
                    Log.i(TAG, " Successfullt seen animation ")
                }.addOnFailureListener {
                    Log.i(TAG, " failed to Vote ${it.message}--${it.printStackTrace()}")
                }
        })
    }

    //-----------------------------Meetup Module ---------------------------------

    fun addContactInDb(contact: Contact): Observable<Unit> {
        return Observable.create<Unit> { emitter ->
            Log.i(TAG, " tryingtoInsertContact:: $contact")
            emitter.onNext(db.contactDao().insert(contact))
        }
    }

    fun getAllContactDB(): Observable<List<Contact>> {
        return Observable.create { emitter ->
            Log.i(TAG, " getting All comntact from db")
            emitter.onNext(db.contactDao().getAllContacts())
        }
    }

    fun findContact(query: String): Observable<List<Contact>> {
        return Observable.create { emitter ->
            emitter.onNext(db.contactDao().search(query))
        }
    }

    fun createMeetUp(request: CreateMeetRequest): Flowable<Response<GetMeetUpResponseItem?>> {
        return meetService.createMeetUp(request)
    }

    fun updateMeetUp(request: CreateMeetRequest, meetId: String?): Flowable<Response<JsonObject?>> {
        return meetService.updateMeetUp(request,meetId)
    }



    fun addPlaceInMeetUp(meetId :String?,request: CreateMeetRequest): Flowable<Response<JsonObject?>> {
        return meetService.addPlaceInMeetUp(meetId,request)
    }
    fun inviteFriend(meetId: String, request: JsonObject): Flowable<Response<JsonObject?>> {
        return meetService.inviteFriend(meetId, request)

    }

    fun fetchMeetUP(
        scope: String?,
        dateRange: String?,
        page: Int = 1,
        limit: Int = 10,
        confirmed: Boolean? = null,
        type: String?,
        sortedBy: String? = null
    ): Single<Response<GetMeetUpResponse?>> {
        return meetService.fetchMeetUP(scope, dateRange, confirmed, page, limit, type,sortedBy)
    }

    fun fetchMeetUPFilter(
        scope: String?,
        dateRange: String?,
        type: String?,
        page: Int = 1,
        limit: Int = 10,
        filter: String? = null
    ): Single<Response<GetMeetUpResponse?>> {
        return meetService.fetchMeetUPFilter(scope, dateRange, type, filter, page, limit)
    }

    fun discoverMeetUp(
        countryCode: String?,
        city: String?,
        to: String?,
        categoryFilter : String?,
        minBadge: String?,
        page: Int = 1,
    ): Single<Response<GetMeetUpResponse?>> {
        return meetService.fetchDiscoverMeetUP(countryCode, city, to,categoryFilter,minBadge, page)
    }

    fun joinOpenMeet(meetId: String?): Single<Response<JsonObject?>> {
        return meetService.joinOpenMeet(meetId)
    }

    fun getJoinRequest(
        meetId: String?,
        status: Constant.RequestType?
    ): Single<Response<GetJoinRequestModel?>> {
        return meetService.getJoinRequest(meetId, status)
    }

    fun countRequestbyBadge(meetId: String?): Single<Response<MeetRqCountByBadge?>> {
        return meetService.countRequestbyBadge(meetId)
    }


    fun opMeetRqApproveSingle(meetId: String?, request: JsonObject): Single<Response<JsonObject?>> {
        return meetService.opMeetRqApproveSingle(meetId, request)
    }

    fun opMeetRevertAccept(meetId: String?, request: JsonObject): Single<Response<JsonObject?>> {
        return meetService.opMeetRevertAccept(meetId, request)
    }


    fun changeMeetName(request: JsonObject, meetId: String): Flowable<Response<JsonObject?>> {
        return meetService.changeMeetName(request, meetId)
    }

    fun optOut(meetId: String?, request: JsonObject): Flowable<Response<JsonObject?>> {
        return meetService.optOut(meetId, request)
    }

    fun cancleMeetUp(meetId: String?): Flowable<Response<JsonObject?>> {
        return meetService.cancleMeetUp(meetId)
    }

    fun deleteMeetUp(meetId: String?, scope: String): Flowable<Response<JsonObject?>> {
        return meetService.deleteMeetUp(meetId, scope)
    }


    fun getMeetUpDetail(id: String?): Flowable<Response<GetMeetUpResponseItem?>> {
        return meetService.getMeetUpDetail(id)
    }

    fun acceptInvitaion(
        invitationId: String?,
        request: JsonObject
    ): Flowable<Response<JsonObject?>> {
        return meetService.acceptInvitaion(invitationId, request)
    }

    fun confirmMeetPlace(meetId: String?, request: JsonObject): Observable<Response<JsonObject?>> {
        return meetService.confirmMeetPlace(meetId, request)
    }

    fun markAttendance(meetId: String?, request: JsonObject): Observable<Response<JsonObject?>> {
        return meetService.markAttendance(meetId, request)
    }

    fun rateAttendee(
        meetId: String?,
        request: JsonObject
    ): Observable<Response<List<MeetPerson?>?>> {
        return meetService.rateAttendee(meetId, request)
    }

    fun closeOpenMeet(meetId: String?): Observable<Response<JsonObject?>> {
        return meetService.closeOpenMeet(meetId)
    }

    //----------------------------- Billing Module ---------------------------------

    fun verifyTransaction(confirmPurchaseModel: ConfirmPurchaseModel): Single<Response<JsonObject?>> {
        return profileService.verifyTransaction(confirmPurchaseModel)
    }

    fun getSkus(): Single<Response<JsonObject?>> {
        return profileService.getSkus()
    }


}