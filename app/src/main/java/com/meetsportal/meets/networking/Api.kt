package com.meetsportal.meets.networking

import com.google.gson.JsonObject
import com.meetsportal.meets.models.*
import com.meetsportal.meets.networking.meetup.CreateMeetRequest
import com.meetsportal.meets.networking.meetup.GetMeetUpResponse
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.meetup.MeetPerson
import com.meetsportal.meets.networking.places.*
import com.meetsportal.meets.networking.post.*
import com.meetsportal.meets.networking.profile.*
import com.meetsportal.meets.networking.registration.*
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.formatTo
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*
import java.util.*

interface Api {

    @POST("/user/auth/authorize/trigger")
    //fun getOTP(@Body request: RegistrationRequest): Observable<Response<RegistrationResponse?>>
    fun getOTP(@Body request: RegistrationRequest): Observable<Response<RegistrationResponse?>>

    @POST("/user/api/account/generate_smart_otp")
    fun generateOtp(@Body request: JsonObject): Observable<Response<JsonObject?>>

    @POST("/user/auth/recover_account/trigger")
    fun recoverAccountOtp(@Body request: JsonObject): Observable<Response<JsonObject?>>

    @POST("/user/auth/recover_account/verify")
    fun recoverVerifyOtp(@Body request: JsonObject): Observable<Response<JsonObject?>>

    @POST("/user/api/account/change_credential")
    fun changeCredential(@Body request: JsonObject): Observable<Response<JsonObject?>>

    @POST("/user/auth/authorize/exchange")
    fun verifyOTP(@Body request: OtpRequest): Observable<Response<OtpResponse?>>

    @POST("/user/auth/google/authorize/exchange")
    fun verifyGoogle(@Body request: JsonObject): Observable<Response<OtpResponse?>>

    @POST("/user/auth/facebook/authorize/exchange")
    fun verifyFacebook(@Body request: JsonObject): Observable<Response<OtpResponse?>>

    @PUT("/user/api/account/set_username")
    fun setUserName(@Body request: JsonObject): Observable<Response<OtpResponse?>>

    @POST("/user/auth/refresh")
    fun refreshToken(
        @Body request: RefreshRequest = RefreshRequest(refresh_token = (PreferencesManager.get<OtpResponse>(
            Constant.PREFRANCE_OTPRESPONSE
        ))?.auth?.refresh_token)
    ): Call<RefreshResponse>



    @GET("/user/auth/username_lookup")
    fun getIsUserNameExist(
        @Query("term") username: String,
        @Query("fields") field: String
    ): Observable<Response<UserNameExistResponse?>>


    /////////////////////////////profile///////////////////////////////

    @PUT("/social/api/profile")
    fun updateProfile(@Body request: ProfileUpdateRequest): Observable<Response<ProfileGetResponse?>>

    @PUT("/social/api/profile")
    fun updateCoverPicture(@Body request: JsonObject): Observable<Response<ProfileGetResponse?>>

    @PUT("/user/api/account/update_profile_image")
    fun updateProfilePic(@Body request: JsonObject): Observable<Response<ProfileGetResponse?>>


    @POST("/user/api/account/document_upload")
    fun uploadDocument(@Body request: UploadDocument): Observable<Response<JsonObject?>>

    @GET("/social/api/profile/full_info")
    fun getFullProfile(): Observable<Response<ProfileGetResponse?>>

    @GET("/social/api/profile/{sid}")
    fun getOtherProfile(@Path("sid") userId: String?,@Header("x-api-context") actionSource: String? = null): Observable<Response<OtherProfileGetResponse?>>

    @GET("/user/api/account/definitions/{key}")
    fun getGenericList(@Path("key") key: String): Observable<Response<FullInterestGetResponse?>>

    @GET("/user/api/account/definitions/{key}")
    fun getPreVerifyData(@Path("key") key: String = "verification_poses"): Observable<Response<PreVerifyDataRes?>>



    @POST("/user/api/account/emergency_whistle")
    fun sendEmergency(): Observable<Response<JSONObject?>>

    @GET("/social/api/profile/quotes/random")
    fun getRandomQuote(): Observable<Response<JsonObject?>>

    @GET("/social/api/profile//insights/new_views")
    fun getNewVisits(): Observable<Response<JsonObject?>>

    @GET("/user/api/account/minimum_version")
    fun getMinVersion(): Observable<Response<JsonObject?>>

    @GET("/user/api/account/search/live")
    fun searchPeople(
        @Query("term") searchString: String?,
        @Query("fields") fields: String = "username",
        @Query("type") type: String = "phrase_prefix",

    ):Observable<Response<SearchPeopleResponse?>>

    @GET("/social/api/profile/{user_id}/relations")
    fun getRelations(
        @Path("user_id") sid : String?,
        @Query("relation") relation :String?,
        @Query("page") page :Int?,
        @Query("match") match :String?=null,
        @Query("limit") limit :Int? = 100,
    ): Single<FollowerFollowingResponse>


    @GET("/social/api/leadboard")
    fun getLeaders(
        @Query("region_code") region_code : String?,
        @Query("sid") sid : String?,
    ): Single<Response<LeaderboardResponse>>
    @GET("/social/api/leadboard/lookup")
    fun getLookUserName(
        @Query("username") username : String?
    ): Single<Response<LeaderboardResponse>>

    @GET("/rewards/api/rewards")
    fun getRewardComponents(): Observable<Response<RewardsResponse>>

    @GET("/social/api/profile/relations/search")
    fun searchRelation(
        @Query("sid") sid : String?,
        @Query("relation") relation :String?,
        @Query("term") searchString :String?,
        @Query("type") type :String? = "phrase_prefix",
        @Query("fields[]") fields : String? = "username",
        @Query("page") page :Int?,
        @Query("limit") limit :Int? = 100,
    ):Single<FollowerFollowingResponse>

    @POST("/social/api/profile/{user_id}/block")
    fun blockUser(
        @Path("user_id") sid: String?
    ):Observable<Response<JsonObject>>

    @GET("/consumables/api/consumables")
    fun getChargeCount(
        @Query("product_id") product_id: String?= "boost"
    ):Observable<Response<JsonObject>>

    @GET("/consumables/api/consumables/pricings")
    fun getBoostPricing(
        @Query("product_id") product_id: String?
    ):Observable<Response<BoostPricingModelResponse>>

    @POST("/consumables/api/consumables/topup")
    fun topUpBoost(
        @Body request: JsonObject
    ):Observable<Response<TopUpBoostModelResponse>>

    @GET("/consumables/api/consumables/active_product_info")
    fun getActiveBoost(
        @Query("product_id") product_id: String?= "boost"
    ):Observable<Response<ActiveItemResponse>>

    @GET("/social/api/profile/insights")
    fun getProfileInsight(
        @Query("dates") dateString: String? = "",
        @Query("page") page: Int = 1
    ):Single<Response<ProfileInsight?>>

    @GET("/social/api/profile/insights/viewed_me")
    fun getViewedMe(
        @Query("page") page: Int = 1
    ):Single<Response<ViewdMe?>>




    @POST("/consumables/api/consumables/consume")
    fun boostPost(
        @Body request: JsonObject
    ):Observable<Response<JsonObject>>

    @GET("/user/api/map")
    fun getMapUsers(
        @Query("current_lat") latitude:Double?,
        @Query("current_lng") longitude:Double?,
        @Query("max_distance") maxDistance:Int? = 100000,
        @Query("interests.key") interesrt:String? = null
    ):Observable<Response<MapUserResponse?>>

    @POST("/user/api/account/verify_profile")
    fun verifyProfile(
        @Body request: JsonObject
    ):Single<Response<JsonObject?>>


    /////////////////////////////Post//////////////////////////////
    @POST("/social/api/post")
    fun addPost(@Body request: PostRequest): Flowable<Response<JsonObject?>>

    @GET("/social/api/post")
    fun fetchPost(
        @Query("sort") sort: String?,
        @Query("user_id") userId: String?,
        @Query("page") page: String?,
        @Query("limit") limit: String?,
        @Query("type") type: String? = "open_meetup,check_in,text_post,default"

    ): Single<FetchPostResponse>

    @GET("/social/api/post/generated")
    fun fetchGlobal(
        @Query("page") page: String?,
        @Query("type") type: String? = "open_meetup,check_in,text_post,default"

    ): Single<FetchPostResponse>



    @GET("/social/api/post")
    fun fetchRecentPost(
        @Query("sort") sort: String?="-createdAt",
        @Query("page") page: String?="1",
        @Query("type") type: String?="default",
        @Query("limit") limit: String?
    ): Observable<Response<FetchPostResponse>>

    @GET("/social/api/post/{post_id}")
    fun fetchSinglePost(
        @Path("post_id") postId: String?,
        @Query("mode") mode :String?,
    ): Observable<Response<SinglePostResponse>>

    @GET("/social/api/post/{parent_id}/comments/post")
    fun fetchCommnets(
        @Path("parent_id") postId: String?,
        @Query("id_type") mode :String?,
        @Query("entity_type") entityType :String?,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10 ,
        @Query("reference_comment_id") commentId : String? =null
    ): Single<SinglePostComments>

    @GET("/social/api/post/{parent_id}/comments/replies")
    fun fetchReplies(
        @Path("parent_id") commentID: String?,
        @Query("id_type") mode :String?,
        @Query("entity_type") entityType :String?,
        @Query("page") page: Int? = 1,
        @Query("limit") limit: Int? = 10 ,
        @Query("reference_comment_id") replyId : String? =null
    ): Single<SinglePostComments>


    @GET("/social/api/post/timeline")
    fun fetchTimeLine(
        @Query("id") bdId: String?,
        @Query("page") page: String?,
        @Query("limit") limit: String?,
        @Query("type") type: String? = "open_meetup,check_in,text_post,default"
    ): Single<FetchPostResponse>


    @PUT("/social/api/post/{post_id}/toggle_like")
    fun toggleLike(@Path("post_id") postId: String?): Observable<Response<JsonObject?>>

    @PUT("/social/api/post/comment/{comment_id}/toggle_like")
    fun toggleCommentLike(@Path("comment_id") commentID: String?): Observable<Response<JsonObject?>>

    @GET("/social/api/post/search/live")
    fun searchPost(
        @Query("term") searchString: String?,
        @Query("fields") fields: String? = "body",
    ):Observable<Response<SearchPostResponse>>

    @POST("/social/api/post/{post_id}/comment")
    fun publishComment(
        @Path("post_id") post_id: String?,
        @Body body: JsonObject
    ): Observable<Response<JsonObject>>

    @POST("/social/api/post/{comment_id}/reply")
    fun replyComment(
        @Path("comment_id") commentId: String?,
        @Body body: JsonObject
    ): Observable<Response<JsonObject>>

    @POST("/social/api/profile/{user_id}/follow_user")
    fun followUser(@Path("user_id") userId: String?): Observable<Response<JsonObject>>

    @DELETE("/social/api/profile/{user_id}/unblock")
    fun unBlockUser(@Path("user_id") userId: String?): Observable<Response<JsonObject>>

    @PUT("/user/api/account/location")
    fun sendLocation(@Body request: JsonObject): Observable<Response<JsonObject>>

    @GET("/social/api/profile/suggested_users")
    fun getSuggestion(): Observable<Response<SuggestionResponse>>

    @DELETE("social/api/profile/{user_id}/unfollow_user")
    fun unfollowUser(@Path("user_id") userId: String?): Observable<Response<JsonObject>>

    @GET("/social/api/post/likes")
    fun getPostLiker(
        @Query("sort") sort:String? = "-createdAt",
        @Query("entity_id") postId: String?,
        @Query("page") page: Int?,
        @Query("limit") limit :Int? =100
    ):Single<PostLikerResponse>

    @DELETE("/social/api/post/{id}/comment/")
    fun deleteComment(
        @Path("id") commentId: String?):Observable<Response<JsonObject>>

    @DELETE("/social/api/post/{id}/comment/reply")
    fun deleteCommentReply(
        @Path("id") commentId: String?):Observable<Response<JsonObject>>

    @DELETE("/social/api/post/{id}")
    fun deletePost(
        @Path("id") postId: String?):Observable<Response<JsonObject>>

    @PUT("/social/api/post/{id}")
    fun updatePost(
        @Path("id") postId: String?,
        @Body request: PostUpdateRequest
    ):Observable<Response<JsonObject>>

    @POST("/social/api/post/views_ingest")
    fun ingestPost(
        @Body request: JsonObject
    ):Observable<Response<JsonObject>>


    @POST("/social/api/post/report")
    fun reportContent(
        @Body request : JsonObject
    ):Observable<Response<JsonObject>>



    ///////////////////////////////      Places   ////////////////////////////////////

    @GET("/places/api/places")
    fun getBestPlaces(
        @Query("sort") sort: String?,
        @Query("limit") limit: String?,
        @Query("limit") is_best_place: Boolean?
    )

    @GET("/places/api/places/definitions/seed_banners")
    fun getOffers(): Observable<Response<CategoryResponse>>

    @GET("/places/api/places")
    fun getBestMeetUp(
        @Query("limit") limit: Int?,
        @Query("current_lat") latitude: Double?,
        @Query("current_lng") longitude: Double?,
        @Query("is_best_place") is_best_place: Boolean?,
        @Query("max_distance") maxDistence: Int? = 100000,
        @Query("region_code") regionCode:String? = PreferencesManager.get<ProfileGetResponse>(
            Constant.PREFRENCE_PROFILE
        )?.cust_data?.region_code,
        @Query("primary_kind_of_place") filter:String? = null,
        @Query("fields") fields: String? = "name,location,is_best_place,is_meetable,featured_image_url,rating,main_image_url,timings,street,city,state,country"
    ): Observable<Response<FetchPlacesResponse>>

    @GET("/places/api/places")
    fun getBestMeetUpPage(
        @Query("limit") limit: Int?,
        @Query("current_lat") latitude: Double?,
        @Query("current_lng") longitude: Double?,
        @Query("is_best_place") is_best_place: Boolean?, //null
        @Query("page") page: Int,
        @Query("facilities") facilities : String?,
        @Query("cuisines") cuisines : String?,
        @Query("kind_of_places") categories : String?, //category Key
        @Query("max_distance") maxDistence: Int? = 500000,
        @Query("country_code") countryCode :String? = "AE",
        @Query("fields") fields: String = "name,location,is_best_place,main_image_url,slug,timings,is_meetable,primary_kind_of_place,rating,is_bookable,kind_of_places",
        @Query("and_keys") and_keys:String = "facilities,cuisines,kind_of_places"
    ): Single<FetchPlacesResponse>

    @GET("/places/api/places/nearest_count")
    fun getNearByPlaceCount(
        @Query("current_lat") latitude: Double?,
        @Query("current_lng") longitude: Double?,
    ):Observable<Response<Int?>>


    @GET("/places/api/places/definitions/categories")
    fun getCategories(): Observable<Response<CategoryResponse>>

    @GET("/places/api/places/definitions/facilities")
    fun getFacilities(): Observable<Response<FacilityResponse>>

    @GET("/places/api/places/definitions/cuisines")
    fun getCuisine(): Observable<Response<CuisineResponse>>


    @GET("/places/api/places/search/live")
    fun searchPlace(
        @Query("term") searchString: String?,
        @Query("fields") fields: String? = "name_en,name_fr_name_ar,of_en,of_ar,of_fr,cuisines",
        @Query("limit") limit: Int? = 20,
        @Query("page") page: Int? = 1,
        @Query("type") type: String = "phrase_prefix"

    ):Observable<Response<SearchPlaceResponse>>

    @GET("/places/api/places/{id}")
    fun getPlace(
        @Path("id") id: String?,
        @Query("mode") mode :String? = null,
    ):Observable<Response<GetSinglePlaceResponse>>


    @POST("/places/api/places/{id}/review")
    fun reviewPlace(
        @Path("id") id: String?,
        @Body request : JsonObject
    ):Observable<Response<JsonObject>>

    @GET("/places/api/places/reviews")
    fun getMyReview(
        @Query("place_id") placeId: String?,
        @Query("user_id") sid: String?,
    ):Observable<Response<FetchReviewResponse?>>


    @GET("/places/api/places/{id}/review_count")
    fun getReviewCount(
        @Path("id") placeId: String?,
    ):Observable<Response<JsonObject?>>


    @GET("/places/api/places/reviews")
    fun getMyReviewSingle(
        @Query("place_id") placeId: String?,
        @Query("page") page :Int?,
        @Query("limit") limit :Int? = 100,
    ):Single<FetchReviewResponse?>

    @PUT("/places/api/places/{id}/review/{review_id}")
    fun editReview(
        @Path("id") placeId: String?,
        @Path("review_id") reviewId: String?,
        @Body request : JsonObject
    ):Observable<Response<JsonObject>>

    @POST("/places/api/places/{id}/check_in")
    fun checkInPlace(
        @Path("id") placeId: String?,
        @Body request : JsonObject
    ):Observable<Response<JsonObject>>

    @POST("/places/api/places/{id}/bookmark")
    fun addBookmark(
        @Path("id") placeId: String?
    ):Observable<Response<JsonObject?>>

    @DELETE("/places/api/places/bookmark/{id}")
    fun deleteBookmark(
        @Path("id") placeId: String?
    ):Observable<Response<JsonObject?>>

    @GET("/places/api/places/bookmarks")
    fun getSavedPlaces(
        @Query("page") page :Int?,
        @Query("limit") limit :Int?,
    ):Single<FetchPlacesResponse>

    //***************************************MeetUp Module**********************************//

    @POST("/meetup/api/meetup")
    fun createMeetUp(
        @Body request : CreateMeetRequest
    ):Observable<Response<GetMeetUpResponseItem?>>

    @PUT("/meetup/api/meetup/{id}")
    fun updateMeetUp(
        @Body request: CreateMeetRequest,
        @Path("id") meetId: String?,
    ):Observable<Response<JsonObject?>>



    @PUT("/meetup/api/meetup/{id}/place")
    fun addPlaceInMeetUp(
        @Path("id") meetId: String?,
        @Body request : CreateMeetRequest
    ):Observable<Response<JsonObject?>>



    @PUT("/meetup/api/meetup/{id}/invite")
    fun inviteFriend(
        @Path("id") meetId: String?,
        @Body request : JsonObject
    ):Observable<Response<JsonObject?>>

    @GET("/meetup/api/meetup")
    fun fetchMeetUP(
        @Query("scope") scope :String?,
        @Query("date_range") dateRange :String?,
        @Query("page") page :Int?,
        @Query("limit") limit :Int?,
        @Query("voting_closed") confirmed:Boolean? = null,
        @Query("type") type:String? = Constant.MeetType.PRIVATE.type,
        @Query("sort") sort:String?,
        @Query("date_now") dateNow : String  = Calendar.getInstance().apply {
            this.setTime(Date());
            this.set(Calendar.HOUR_OF_DAY, 0)
            this.set(Calendar.MINUTE, 0)
            this.set(Calendar.SECOND, 0)
            this.set(Calendar.MILLISECOND, 0)
        }.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")),

    ):Single<Response<GetMeetUpResponse?>>

    @GET("/meetup/api/meetup/discover")
    fun fetchDiscoverMeetUP(
        @Query("country_code") country_code :String?,
        @Query("city") city :String?,
        @Query("to") to :String?,
        @Query("interests")categoryFilter : String?,
        @Query("min_badge")minBadge : String?,
        @Query("page") page :Int?,
        @Query("limit") limit :Int?= 20,
        @Query("from") from :String?  = Calendar.getInstance().apply {
            this.setTime(Date());
            this.set(Calendar.HOUR_OF_DAY, 0);
            this.set(Calendar.MINUTE, 0);
            this.set(Calendar.SECOND, 0);
            this.set(Calendar.MILLISECOND, 0);
        }.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
    ):Single<Response<GetMeetUpResponse?>>

    @GET("/meetup/api/meetup")
    fun fetchMeetUPFilter(
        @Query("scope") scope :String?,
        @Query("date_range") dateRange :String?,
        @Query("page") page :Int?,
        @Query("limit") limit :Int?,
        @Query("filter") filter:String? = null,
        @Query("type") type:String? = Constant.MeetType.PRIVATE.type,
        @Query("date_now") dateNow : String  = Calendar.getInstance().apply {
            this.setTime(Date());
            this.set(Calendar.HOUR_OF_DAY, 0);
            this.set(Calendar.MINUTE, 0);
            this.set(Calendar.SECOND, 0);
            this.set(Calendar.MILLISECOND, 0);
        }.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
    ):Single<Response<GetMeetUpResponse?>>


    @POST("/meetup/api/meetup/{id}/join_request")
    fun joinOpenMeet(
        @Path("id") meetId: String?,
    ):Single<Response<JsonObject?>>

    @GET("/meetup/api/meetup/{id}/join_request")
    fun getJoinRequest(
        @Path("id") meetId: String?,
        @Query("status") scope :String?,
    ):Single<Response<GetJoinRequestModel?>>

    @GET("/meetup/api/meetup/{id}/join_request/badge_breakdown")
    fun countRequestbyBadge(
        @Path("id") meetId: String?,
    ):Single<Response<MeetRqCountByBadge?>>



    @PUT("/meetup/api/meetup/{id}/join_request_approval")
    fun opMeetRqApproveSingle(
        @Path("id") meetId: String?,
        @Body request : JsonObject
    ):Single<Response<JsonObject?>>

    @HTTP(method = "DELETE", path = "/meetup/api/meetup/{id}/join_request_revert", hasBody = true)
    fun opMeetRevertAccept(
        @Path("id") meetId: String?,
        @Body request : JsonObject
    ):Single<Response<JsonObject?>>

    @PUT("/meetup/api/meetup/{id}/rename")
    fun changeMeetName(
        @Body request : JsonObject,
        @Path("id") meetId: String?
    ):Observable<Response<JsonObject?>>

    @HTTP(method = "DELETE", path = "meetup/api/meetup/{id}/opt_out", hasBody = true)
    fun optOut(
        @Path("id") meetId: String?,
        @Body request : JsonObject,
    ):Observable<Response<JsonObject?>>

    @DELETE("/meetup/api/meetup/{id}/cancel")
    fun cancleMeetUp(
        @Path("id") meetId: String?
    ):Observable<Response<JsonObject?>>

    @DELETE("/meetup/api/meetup/{id}")
    fun deleteMeetUp(
        @Path("id") meetId: String?,
        @Query("scope") scope :String?
    ):Observable<Response<JsonObject?>>


    @GET("/meetup/api/meetup/{id}")
    fun getMeetUpDetail(
        @Path("id") meetUpid: String?
    ):Observable<Response<GetMeetUpResponseItem?>>

    @PUT("/meetup/api/meetup/{id}/confirm")
    fun confirmMeetPlace(
        @Path("id") meetUpid: String?,
        @Body request : JsonObject
    ):Observable<Response<JsonObject?>>

    @PUT("/meetup/api/meetup/{id}/mark_attendance")
    fun markAttendance(
        @Path("id") meetUpid: String?,
        @Body request : JsonObject
    ):Observable<Response<JsonObject?>>

    @POST("/meetup/api/meetup/{id}/rate_attendee")
    fun rateAttendee(
        @Path("id") meetUpid: String?,
        @Body request : JsonObject
    ):Observable<Response<List<MeetPerson?>?>>

    @PUT("/meetup/api/meetup/{id}/close")
    fun closeOpenMeet(
        @Path("id") meetUpid: String?,
    ):Observable<Response<JsonObject?>>

    @PUT("/meetup/api/meetup/{id}/invitation")
    fun acceptInvitaion(
        @Path("id") meetUpid: String?,
        @Body request : JsonObject
    ):Observable<Response<JsonObject?>>

    @POST("/user/api/account/addresses")
    fun saveAddress(
        @Body request : JsonObject
    ):Observable<Response<JsonObject?>>


    @GET("/social/api/post/insights")
    fun getInSights(
        @Query("post_id") post_id :String?,
    ):Observable<Response<InsightResponse?>>

    @GET("/user/api/account/addresses")
    fun getSavedAddress(
        @Query("page") page :Int?,
        @Query("limit") limit :Int?,
    ):Observable<Response<AddressModelResponse?>>


    @DELETE("/user/api/account/addresses/{id}")
    fun deleteAddress(
        @Path("id") id: String?
    ):Observable<Response<JsonObject?>>


    //------------------  Billing --------------------

    @POST("/consumables/api/purchases/verify")
    fun verifyTransaction(
        @Body request : ConfirmPurchaseModel
    ):Single<Response<JsonObject?>>

    @GET("/consumables/api/purchases/skus")
    fun getSkus():Single<Response<JsonObject?>>

}