package com.meetsportal.meets.viewmodels

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.meetsportal.meets.database.entity.Contact
import com.meetsportal.meets.models.*
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.exception.ApiException
import com.meetsportal.meets.networking.meetup.*
import com.meetsportal.meets.networking.meetup.datasource.DiscoverMeetPagerDataSource
import com.meetsportal.meets.networking.meetup.datasource.MeetUpListPagerDataSource
import com.meetsportal.meets.repository.AppRepository
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpRulesBottomFragment
import com.meetsportal.meets.ui.fragments.socialfragment.OpenMeetRuleFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.RuntimeException
import javax.inject.Inject
import kotlin.collections.ArrayList
@HiltViewModel
class MeetUpViewModel @Inject constructor(var repository: AppRepository) : ViewModel() {

    val TAG = MeetUpViewModel::class.java.simpleName
    private val disposable = CompositeDisposable()

    val contactList = ArrayList<Contact>()
    private val listContact: MediatorLiveData<List<Contact>> = MediatorLiveData()
    private var createMeetUp: MediatorLiveData<ResultHandler<GetMeetUpResponseItem?>> = MediatorLiveData()
    private var updateMeetUp: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var createOpenMeet: MediatorLiveData<ResultHandler<GetMeetUpResponseItem?>> = MediatorLiveData()
    private var addPlaceInMeetUp: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var inviteFriend: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var meetUpResponse: MediatorLiveData<ResultHandler<GetMeetUpResponse?>> = MediatorLiveData()
    private var previousMeetUp: MediatorLiveData<ResultHandler<GetMeetUpResponse?>> = MediatorLiveData()
    private var openMeetUp: MediatorLiveData<ResultHandler<GetMeetUpResponse?>> = MediatorLiveData()
    private var joinMeetUp: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var openMeetJoinRequest: MediatorLiveData<ResultHandler<GetJoinRequestModel?>> = MediatorLiveData()
    private var meetRqCountByBadge: MediatorLiveData<ResultHandler<MeetRqCountByBadge?>> = MediatorLiveData()
    private var opMeetReApproveSingle: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var opMeetRevertAccept: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var nameChange: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var optOut: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var cancelMeetUp: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var deleteMeetUp: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var meetUpPageDataSource: MediatorLiveData<PagingData<GetMeetUpResponseItem>> = MediatorLiveData()
    private var discoverMeetUpPageDataSource: MediatorLiveData<PagingData<GetMeetUpResponseItem>> = MediatorLiveData()
    private var meetUpDetail: MediatorLiveData<ResultHandler<GetMeetUpResponseItem?>> = MediatorLiveData()
    private var meetUpDetailForDialog: MediatorLiveData<ResultHandler<GetMeetUpResponseItem?>> = MediatorLiveData()

    /**
     * Param Triple<acceptOrreject : Boolean, response :JsonObject?, meetUpId : String?>
     */
    private var meetUpInvitation: MediatorLiveData<ResultHandler<MeetJoinRequest>> = MediatorLiveData()
    private var meetUpConfirm: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var meetMarkAttendance: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var closeOpenMeet: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    private var meetRateAttendance: MediatorLiveData<ResultHandler<List<MeetPerson?>?>> = MediatorLiveData()

    val serachedContactList: MediatorLiveData<List<Contact>> = MediatorLiveData()

    fun addContactInDb(contact: Contact) {

        disposable.add(
            repository.addContactInDb(contact)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.i("TAG", " dataInserted::: ")
                }, {
                    Log.i("TAG", "${it.printStackTrace()}")
                })
        )
    }

    fun getAllContact() {
        disposable.add(
            repository.getAllContactDB()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    listContact.value = it
                }, {
                    Log.i(TAG, " error in getting contact from DB ${it.printStackTrace()}")
                })
        )
    }


    fun findContact(query: String?) : Disposable{
        val queryWithEscapedQuotes = query?.replace(Regex.fromLiteral("\""), "\"\"")
       // disposable.add(

            var d = repository.findContact("*\"$queryWithEscapedQuotes\"*")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    serachedContactList.value = it
                }, {
                    it.printStackTrace()
                })
        //)
        return d
    }

    fun createMeetUp(
        selectedPlaceList: MutableMap<String?, MeetsPlace?>,
        customPlace: ArrayList<AddressModel>,
        selectedPeopleList: ArrayList<SelectedContactPeople>?,
        rules: MeetUpRulesBottomFragment.Rules?
    ) {
        var invitees  = ArrayList<String?>()
        selectedPeopleList?.filter { it is SelectedContactPeople.SelectedContact }
            ?.map { (it as SelectedContactPeople.SelectedContact).number }?.forEach {
                invitees.add(it)
                //inviteeJsonArray.add(it)
            }
        selectedPeopleList?.filter { it is SelectedContactPeople.SelectedPeople }
            ?.map { (it as SelectedContactPeople.SelectedPeople).sid }?.forEach {
                invitees.add(it)
                //inviteeJsonArray.add(it)
            }
        customPlace.map { it.selected = null }
        var request = CreateMeetRequest(invitees = invitees,
            places = selectedPlaceList.values.map { it?.id },
            date = rules?.date,
            name = rules?.name,
            duration = rules?.duration,
//            duration = rules?.date,
            max_vote_changes = rules?.voteChnge,
            description = rules?.desc,
            max_join_time = MaxJoinTime(rules?.joinTime?.type,rules?.joinTime?.value),
            custom_places = customPlace
        )
        disposable.add(
            repository.createMeetUp(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isSuccessful)
                        createMeetUp.value = ResultHandler.Success(it.body())
                }, {
                    Utils.handleException(it,createMeetUp,"createMeetUp")
                })
        )
    }

    fun updateMeetUp(meetId: String?, fbChatId:String?,rule: MeetUpRulesBottomFragment.Rules) {
        Log.i(TAG," chekcingVoteChange:: ${rule.voteChnge}")
        val request = CreateMeetRequest(
            name = rule.name,
            max_vote_changes = rule.voteChnge,
            max_join_time = if(rule.joinTime == null) null else MaxJoinTime(rule.joinTime?.type,rule.joinTime?.value),
            description = rule.desc,
            duration =  rule.duration
        )
        disposable.add(
            repository.updateMeetUp(request,meetId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isSuccessful){
                        updateMeetUp.value = ResultHandler.Success(it.body())
                        if(rule.voteChnge != null && fbChatId != null){
                            repository.updateMeetUpinFireBase(fbChatId,rule.voteChnge!!)
                        }
                    }
                }, {
                    Utils.handleException(it,updateMeetUp,"updateMeetUp")
                })
        )
    }

    fun updateMaxVotUpdateToFalse(fbChatId: String,myVote: Vote?) {
        myVote?.maxVotUpdated = false
        myVote?.let {
            repository.updateMaxVotUpdateToFalse(fbChatId,myVote)
        }
    }

    fun createOpenMeet(
        selectedPlaceList: MutableMap<String?, MeetsPlace?>,
        customPlace: ArrayList<AddressModel>,
        selectedPeopleList: ArrayList<SelectedContactPeople>?,
        rules: OpenMeetRuleFragment.Rules?
    ){

        Log.d(TAG, "createOpenMeet: min_badge${rules?.min_badge}")
        customPlace.map { it.selected = null }

        var choosenPlace =ChosenPlace ()
        if(selectedPlaceList.isNotEmpty()){
            choosenPlace.type = "meets"
            choosenPlace.id = selectedPlaceList.values.take(1).getOrNull(0)?.id
        }else if(customPlace.isNotEmpty()){
            choosenPlace.type = "custom"
            choosenPlace.id = customPlace.get(0)._id
        }

        var request = CreateMeetRequest(
            places = selectedPlaceList.values.map { it?.id },
            custom_places = customPlace,
            date = rules?.date,
            name = rules?.name,
            duration = rules?.duration,
            interests = rules?.categories?.map { it?.key },
            description = rules?.desc,
            max_join_time = MaxJoinTime(rules?.joinTime?.typeJoin?.type,rules?.joinTime?.value),
            min_badge = rules?.min_badge,
            type = "open",
            chosen_place = choosenPlace

        )
        disposable.add(
            repository.createMeetUp(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isSuccessful)
                        createOpenMeet.value = ResultHandler.Success(it.body())
                }, {
                    Utils.handleException(it,createOpenMeet,"createOpenMeet")
                })
        )
    }

    fun addPlace(
        meetId :String? = null,
        selectedPlaceList: MutableMap<String?, MeetsPlace?>,
        customPlace: ArrayList<AddressModel>,
    ){

        val request = CreateMeetRequest(
            places = selectedPlaceList.values.map { it?.id },
            custom_places = customPlace,
            min_badge = null,
            type = null
        )

        Log.i(TAG," checkingAddPlacerequesr:: $request")
        disposable.add(repository.addPlaceInMeetUp(meetId,request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.isSuccessful)
                    addPlaceInMeetUp.value = ResultHandler.Success(it.body())
            }, {
                Utils.handleException(it,addPlaceInMeetUp,"createOpenMeet")
            }))
    }

    fun inviteFriend(meetId: String, selected: ArrayList<SelectedContactPeople>,) {

        val request = JsonObject()
        var inviteeJsonArray = JsonArray()

        selected?.filter { it is SelectedContactPeople.SelectedContact }
            ?.map { (it as SelectedContactPeople.SelectedContact).number }?.forEach {
                inviteeJsonArray.add(it)
            }
        selected?.filter { it is SelectedContactPeople.SelectedPeople }
            ?.map { (it as SelectedContactPeople.SelectedPeople).sid }?.forEach {
                inviteeJsonArray.add(it)
            }

        request.add("invitees", inviteeJsonArray)

        //Log.i(TAG," inviteeJsonArray::: ${inviteeJsonArray}")

        disposable.add(
            repository.inviteFriend(meetId,request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isSuccessful)
                        inviteFriend.value = ResultHandler.Success(it.body())
                },{
                    Utils.handleException(it,inviteFriend,"inviteFriend")
                })
        )
    }

    fun fetchMeetUP(scope: String?, dateRange: String?) {
        disposable.add(repository.fetchMeetUP(scope, dateRange,type = Constant.MeetType.PRIVATE.type)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                if (it.isSuccessful)
                    meetUpResponse.value = ResultHandler.Success(it.body())
            }, {
                Utils.handleException(it,meetUpResponse,"fetchMeetUP")
                /*if (it is ApiException) {
                    if (it.errorResponse != null && it.errorResponse?.errorCode == 400) {
                        meetUpResponse.value = ResultHandler.Failure(
                            it.errorResponse?.code,
                            it.errorResponse?.message,
                            it
                        )
                    } else {
                        meetUpResponse.value = getUnknownError(it)
                        Log.e(TAG, "Something went Wrong ${it.printStackTrace()}")
                    }
                } else {
                    meetUpResponse.value = getUnknownError(it)
                }*/
            }))
    }

    fun fetchPreViousMeetUp(scope: String?, dateRange: String?) {
        disposable.add(repository.fetchMeetUP(scope, dateRange,confirmed = true, type = Constant.MeetType.PRIVATE.type,sortedBy = "-date")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                if (it.isSuccessful)
                    previousMeetUp.value = ResultHandler.Success(it.body())
            }, {
                Utils.handleException(it,previousMeetUp,"fetchPreViousMeetUp")
            }))
    }

    fun fetchOpenMeetUp(scope: String?, dateRange: String?) {
        disposable.add(repository.fetchMeetUP(scope, dateRange,type = Constant.MeetType.OPEN.type)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                if (it.isSuccessful)
                    openMeetUp.value = ResultHandler.Success(it.body())
            }, {
                Utils.handleException(it,openMeetUp,"fetchPreViousMeetUp")
            }))
    }

    fun joinOpenMeet(meetId: String?){
        disposable.add(repository.joinOpenMeet(meetId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                if (it.isSuccessful)
                    joinMeetUp.value = ResultHandler.Success(it.body())
            }, {
                Utils.handleException(it,joinMeetUp,"joinOpenMeet")
            }))
    }
    fun countRequestbyBadge(meetId : String?){
        disposable.add(repository.countRequestbyBadge(meetId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                if (it.isSuccessful)
                    meetRqCountByBadge.value = ResultHandler.Success(it.body())
            }, {
                Utils.handleException(it,meetRqCountByBadge,"countRequestbyBadge")
            }))
    }

    fun getJoinRequest(meetId: String?,status : Constant.RequestType?){
        disposable.add(repository.getJoinRequest(meetId,status)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                if (it.isSuccessful)
                    openMeetJoinRequest.value = ResultHandler.Success(it.body())
            }, {
                Utils.handleException(it,openMeetJoinRequest,"getJoinRequest")
            }))
    }

    fun opMeetRqApprove(meetId: String?, requestId : String? = null, badgeList: List<String>? = null){
        val request = JsonObject()
        requestId?.let {
            request.addProperty("type","singular")
            request.addProperty("join_request_id",requestId)
        }
        badgeList?.let {
            request.addProperty("type","badge")
           var  badgeListArray  = JsonArray().apply {
               badgeList.forEach{
                   add(it)
               }
           }
            request.add("allowed_badges",badgeListArray)
        }


        disposable.add(repository.opMeetRqApproveSingle(meetId,request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                if (it.isSuccessful)
                    opMeetReApproveSingle.value = ResultHandler.Success(it.body())
            }, {
                Utils.handleException(it,opMeetReApproveSingle,"opMeetRqApproveSingle")
            }))
    }

    fun opMeetRevertAccept(meetId: String?, requestId : String? = null, type : String? = null, user_id : String? = null){
        Log.i(TAG," opMeetRevertAccept::: ${requestId}")
        val request = JsonObject()
        request.addProperty("join_request_id",requestId)
        request.addProperty("type",type)
        request.addProperty("user_id",user_id)
        Log.i(TAG," opMeetRevertAccept::: 2  ${request}")
        disposable.add(repository.opMeetRevertAccept(meetId,request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                Log.i(TAG," opMeetRevertAccept::: 3")
                if (it.isSuccessful)
                    opMeetRevertAccept.value = ResultHandler.Success(it.body())
            }, {
                Log.i(TAG," opMeetRevertAccept::: 4")
                Utils.handleException(it,opMeetRevertAccept,"opMeetRevertAccept")
            }))
    }




    fun changeMeetName(name : String,meetId : String){
       /* val request = JsonObject()
        request.addProperty("name",name)
        disposable.add(
            repository.changeMeetName(request,meetId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    nameChange.value = ResultHandler.Success(it.body())
                },{
                    Utils.handleException(it,nameChange,"changeMeetName")
                })

        )*/


        val request = CreateMeetRequest(
            name = name,
        )
        disposable.add(
            repository.updateMeetUp(request,meetId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.isSuccessful){
                        nameChange.value = ResultHandler.Success(it.body())
                    }
                }, {
                    Utils.handleException(it,nameChange,"changeMeetName")
                })
        )

    }

    fun optOut(meetId: String?, isUpcoming: Boolean? = null){
        val request = JsonObject()
       // request.addProperty("notify_opt_out",isUpcoming)
        disposable.add(
            repository.optOut(meetId,request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    optOut.value = ResultHandler.Success(it.body())
                },{
                    Utils.handleException(it,optOut,"optOut")
                })

        )
    }

    fun cancleMeetUp(meetId : String?){
        disposable.add(
            repository.cancleMeetUp(meetId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    cancelMeetUp.value = ResultHandler.Success(it.body())
                },{
                    Utils.handleException(it,cancelMeetUp,"cancleMeetUp")
                })

        )
    }

    fun deleteMeetUp(meetId: String?, scope: String){
        disposable.add(
            repository.deleteMeetUp(meetId,scope)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    deleteMeetUp.value = ResultHandler.Success(it.body())
                },{
                    Utils.handleException(it,deleteMeetUp,"cancleMeetUp")
                })

        )
    }



    fun getMeetUpListdataSource(scope: String?, dateRange: String?,filter: String?,type:String?=Constant.MeetType.PRIVATE.type){
        disposable.add(Pager(
            config = PagingConfig(
                pageSize = 2,
                enablePlaceholders = false,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                MeetUpListPagerDataSource(repository).apply {
                    this.scope = scope
                    this.dateRange = dateRange
                    this.filter = filter
                    this.type = type
                }
            }
        ).flowable
            .subscribe({
                meetUpPageDataSource.value = it
            }, {
                Log.i(TAG, " Something went wrong feetching post ${it}")
            })
        )
    }

    fun getOpenMeetListDataSource(){

    }

    fun discoverOpenMeet(countryCode:String?,city:String?,to:String?,categoryFilter : String?,minBadge :String? = null){
        disposable.add(Pager(
            config = PagingConfig(
                pageSize = 2,
                enablePlaceholders = false,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                DiscoverMeetPagerDataSource(repository).apply {
                    setExtradData(countryCode,city,to,categoryFilter,minBadge)
                }
            }
        ).flowable
            .subscribe({
                discoverMeetUpPageDataSource.value = it
            }, {
                Log.i(TAG, " Something went wrong feetching post ${it}")
            })
        )
    }

    fun getMeetUpDetail(id:String?){
        Log.d(TAG, "getMeetUpDetail: GetMeetUpResponseItem called")
        disposable.add(repository.getMeetUpDetail(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it.isSuccessful)
                    meetUpDetail.value = ResultHandler.Success(it.body())
            },{
                Utils.handleException(it,meetUpDetail,"getMeetUpDetail")
            }))
    }

    fun showMeetDetailUpDetail(id:String?){

        disposable.add(repository.getMeetUpDetail(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it.isSuccessful)
                    meetUpDetailForDialog.value = ResultHandler.Success(it.body())
            },{
                Utils.handleException(it,meetUpDetailForDialog,"getMeetUpDetail")
            }))
    }



    fun acceptInvitaion(isJoin: MeetJoinRequest) {
        val request = JsonObject()
        request.addProperty("status",if(isJoin.accept==true) "accepted" else "rejected")
        disposable.add(repository.acceptInvitaion(isJoin.invitationId,request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it.isSuccessful)
                    meetUpInvitation.value = ResultHandler.Success(isJoin)
            },{
                Utils.handleException(it,meetUpInvitation,"acceptInvitaion")
            }))
    }

    fun confirmMeetPlace(meetId: String?, type: String, placeId: String?) {
        val request = JsonObject()
        val chosenPlace = JsonObject()
        chosenPlace.addProperty("type",type)
        chosenPlace.addProperty("id",placeId)
        request.add("chosen_place",chosenPlace)

        disposable.add(repository.confirmMeetPlace(meetId,request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it.isSuccessful)
                    meetUpConfirm.value = ResultHandler.Success(it.body())
            },{
                Utils.handleException(it,meetUpConfirm,"confirmMeetPlace")
            })
        )
    }

    fun markAttendance(meetId : String?,location: Location) {
        val request = JsonObject()
        request.addProperty("current_lat",location.latitude)
        request.addProperty("current_lng",location.longitude)
        disposable.add(repository.markAttendance(meetId,request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it.isSuccessful)
                    meetMarkAttendance.value = ResultHandler.Success(it.body())
            },{
                Utils.handleException(it,meetMarkAttendance,"markAttendance")
            })
        )
    }

    fun rateAttendee(meetId: String?, sid: String?){
        val request = JsonObject()
        request.addProperty("attendee",sid)
        disposable.add(repository.rateAttendee(meetId,request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it.isSuccessful)
                    meetRateAttendance.value = ResultHandler.Success(it.body())
            },{
                Utils.handleException(it,meetRateAttendance,"rateAttendee")
            })
        )
    }

    fun closeOpenMeet(meetId: String?){
        disposable.add(repository.closeOpenMeet(meetId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if(it.isSuccessful)
                    closeOpenMeet.value = ResultHandler.Success(it.body())
            },{
                Utils.handleException(it,closeOpenMeet,"rateAttendee")
            })
        )
    }

    fun observeAddPlaceMeetUp(): LiveData<ResultHandler<JsonObject?>> {
        return addPlaceInMeetUp
    }

    fun observeCloseOpenMeet(): LiveData<ResultHandler<JsonObject?>> {
        return closeOpenMeet
    }

    fun observeRateAttendee(): LiveData<ResultHandler<List<MeetPerson?>?>> {
        return meetRateAttendance
    }
    fun observeMarkAttandance():LiveData<ResultHandler<JsonObject?>>{
        return meetMarkAttendance
    }

    fun observeMeetUpConfirm():LiveData<ResultHandler<JsonObject?>>{
        return meetUpConfirm
    }

    fun observeMeetUpInvitaion(): LiveData<ResultHandler<MeetJoinRequest>> {
        return meetUpInvitation
    }

    fun observeMeetUpDetail(): LiveData<ResultHandler<GetMeetUpResponseItem?>> {
        return meetUpDetail
    }

    fun observeMeetUpDetailForDialog(): LiveData<ResultHandler<GetMeetUpResponseItem?>> {
        return meetUpDetailForDialog
    }



    fun observeMeetUpResponse(): LiveData<ResultHandler<GetMeetUpResponse?>> {
        return meetUpResponse
    }
    fun observepreviousMeetUp():LiveData<ResultHandler<GetMeetUpResponse?>>{
        return previousMeetUp
    }

    fun observeOpenMeetUp(): LiveData<ResultHandler<GetMeetUpResponse?>> {
        return openMeetUp
    }

    fun observeJoinMeetUp():LiveData<ResultHandler<JsonObject?>>{
        return joinMeetUp
    }

    fun observeOpenMeetJoinRequest(): LiveData<ResultHandler<GetJoinRequestModel?>> {
        return openMeetJoinRequest
    }

    fun obsrveMeetRqCountByBadge(): LiveData<ResultHandler<MeetRqCountByBadge?>> {
        return meetRqCountByBadge
    }

    fun observerSingleMeetApproval(): LiveData<ResultHandler<JsonObject?>> {
        return opMeetReApproveSingle
    }

    fun observeOpMeeetRevertAccept(): LiveData<ResultHandler<JsonObject?>> {
        return opMeetRevertAccept
    }

    fun observeNameChange(): LiveData<ResultHandler<JsonObject?>> {
        return nameChange
    }

    fun observeOptOut(): LiveData<ResultHandler<JsonObject?>>{
        return optOut
    }

    fun observeCancelMeetUp(): LiveData<ResultHandler<JsonObject?>>{
        return cancelMeetUp
    }

    fun observeDeleteMeetUp(): LiveData<ResultHandler<JsonObject?>>{
        return deleteMeetUp
    }


    fun observeMeetPageDataSource(): LiveData<PagingData<GetMeetUpResponseItem>> {
        return meetUpPageDataSource
    }

    fun observeDiscoverMeetPageDataSource(): LiveData<PagingData<GetMeetUpResponseItem>> {
        return discoverMeetUpPageDataSource
    }



    fun observeCreateMeetUp(): LiveData<ResultHandler<GetMeetUpResponseItem?>> {
        return createMeetUp
    }

    fun observeUpdateMeetUp(): LiveData<ResultHandler<JsonObject?>> {
        return updateMeetUp
    }

    fun observeOpenCreateMeet():LiveData<ResultHandler<GetMeetUpResponseItem?>>{
        return createOpenMeet
    }

    fun observerInviteUser() : LiveData<ResultHandler<JsonObject?>>{
        return inviteFriend
    }

    fun observerSearchedContact(): LiveData<List<Contact>> {
        return serachedContactList
    }

    fun observeList(): LiveData<List<Contact>> {
        return listContact
    }

    fun removeObserver() {
        disposable.clear()
    }

    fun getUnknownError(it: Throwable): ResultHandler.Failure {
        return ResultHandler.Failure(
            "500",
            "Something went Wrong",
            it
        )
    }



    inline fun <reified T> handleException(it:Throwable, livedata: MutableLiveData<ResultHandler<T>>, apiName:String){

        if (it is ApiException) {
            if (it.errorResponse != null && it.errorResponse?.errorCode == 400) {
                FirebaseCrashlytics.getInstance().log("$apiName 400 ${it.errorResponse?.errorCode}")
                FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                livedata.value = ResultHandler.Failure(
                    it.errorResponse?.code,
                    it.errorResponse?.message,
                    it
                )
            } else {
                FirebaseCrashlytics.getInstance().log("$apiName ${it.errorResponse?.errorCode}")
                FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                livedata.value = getUnknownError(it)
                Log.e(TAG, "Something went Wrong ${it.printStackTrace()}")
            }
        } else {
            FirebaseCrashlytics.getInstance().log("$apiName ${it.localizedMessage}")
            FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
            livedata.value = getUnknownError(it)
        }

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}