package com.meetsportal.meets.networking.meetup

import com.google.gson.JsonObject
import com.meetsportal.meets.models.GetJoinRequestModel
import com.meetsportal.meets.models.MeetRqCountByBadge
import com.meetsportal.meets.networking.Api
import com.meetsportal.meets.networking.ApiClient
import com.meetsportal.meets.utils.Constant
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeetUpService @Inject constructor() {

    private var api : Api = ApiClient.getRetrofit()

    fun createMeetUp(request: CreateMeetRequest): Flowable<Response<GetMeetUpResponseItem?>> {
        return api.createMeetUp(request)
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun updateMeetUp(request: CreateMeetRequest, meetId: String?): Flowable<Response<JsonObject?>> {
        return api.updateMeetUp(request,meetId)
            .toFlowable(BackpressureStrategy.BUFFER)
    }



    fun addPlaceInMeetUp(meetId :String?,request: CreateMeetRequest): Flowable<Response<JsonObject?>> {
        return api.addPlaceInMeetUp(meetId,request)
            .toFlowable(BackpressureStrategy.BUFFER)
    }


    fun inviteFriend(meetId: String?, request: JsonObject): Flowable<Response<JsonObject?>> {
        return api.inviteFriend(meetId,request)
            .toFlowable(BackpressureStrategy.BUFFER)

    }
    fun fetchMeetUP(
        scope: String?,
        dateRange: String?,
        confirmed: Boolean? = null,
        page: Int,
        limit: Int,
        type: String?,
        sortedBy: String?
    ): Single<Response<GetMeetUpResponse?>> {
        return api.fetchMeetUP(scope,dateRange,page,limit,confirmed,type,sortedBy)
    }
    fun fetchMeetUPFilter(scope: String?, dateRange: String?,type:String?,confirmed : String? = null, page: Int, limit: Int): Single<Response<GetMeetUpResponse?>> {
        return api.fetchMeetUPFilter(scope,dateRange,page,limit,confirmed,type = type)
    }

    fun fetchDiscoverMeetUP(contryCode: String?, city: String?,to:String?,categoryFilter : String?,minBadge : String?,page: Int): Single<Response<GetMeetUpResponse?>> {
        return api.fetchDiscoverMeetUP(contryCode,city,to,categoryFilter,minBadge,page)
        //return api.fetchDiscoverMeetUP()
    }

    fun joinOpenMeet(meetId: String?):Single<Response<JsonObject?>>{
        return api.joinOpenMeet(meetId)
    }

    fun getJoinRequest(meetId: String?,status : Constant.RequestType?):Single<Response<GetJoinRequestModel?>>{
        return api.getJoinRequest(meetId,status?.value)
    }

    fun countRequestbyBadge(meetId: String?):Single<Response<MeetRqCountByBadge?>>{
        return api.countRequestbyBadge(meetId)
    }



    fun opMeetRqApproveSingle(meetId: String?,request: JsonObject ):Single<Response<JsonObject?>>{
        return api.opMeetRqApproveSingle(meetId,request)
    }

    fun opMeetRevertAccept(meetId: String?,request: JsonObject ):Single<Response<JsonObject?>>{
        return api.opMeetRevertAccept(meetId,request)
    }



    fun changeMeetName(request: JsonObject,meetId : String): Flowable<Response<JsonObject?>> {
        return api.changeMeetName(request,meetId)
            .toFlowable(BackpressureStrategy.BUFFER)

    }

    fun optOut(meetId: String?, request: JsonObject): Flowable<Response<JsonObject?>> {
        return api.optOut(meetId,request)
            .toFlowable(BackpressureStrategy.BUFFER)

    }

    fun cancleMeetUp(meetId : String?): Flowable<Response<JsonObject?>> {
        return api.cancleMeetUp(meetId)
            .toFlowable(BackpressureStrategy.BUFFER)

    }

    fun deleteMeetUp(meetId: String?, scope: String): Flowable<Response<JsonObject?>> {
        return api.deleteMeetUp(meetId,scope)
            .toFlowable(BackpressureStrategy.BUFFER)
    }


    fun getMeetUpDetail(Id :String?): Flowable<Response<GetMeetUpResponseItem?>> {
        return api.getMeetUpDetail(Id)
            .toFlowable(BackpressureStrategy.BUFFER)
    }


    fun acceptInvitaion(invitationId: String?, request: JsonObject): Flowable<Response<JsonObject?>> {
        return api.acceptInvitaion(invitationId,request)
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun confirmMeetPlace(meetId: String?, request: JsonObject): Observable<Response<JsonObject?>> {
        return api.confirmMeetPlace(meetId,request)

    }

    fun markAttendance(meetId: String?, request: JsonObject): Observable<Response<JsonObject?>> {
        return api.markAttendance(meetId,request)

    }

    fun rateAttendee(meetId: String?, request: JsonObject): Observable<Response<List<MeetPerson?>?>> {
        return api.rateAttendee(meetId,request)
    }

    fun closeOpenMeet(meetId: String?): Observable<Response<JsonObject?>> {
        return api.closeOpenMeet(meetId)
    }









}