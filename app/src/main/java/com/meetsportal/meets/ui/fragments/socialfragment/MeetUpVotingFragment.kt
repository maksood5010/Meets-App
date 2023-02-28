package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fenchtose.tooltip.Tooltip
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.*
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetupVotingBinding
import com.meetsportal.meets.models.ItemModel
import com.meetsportal.meets.models.MeetFireData
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.firebase.FireBaseUtils
import com.meetsportal.meets.networking.meetup.*
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.ui.dialog.*
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.NewBottomSheet
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.FireBaseViewModal
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.meetsportal.meets.viewmodels.PlacesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.content_restaurant_detail.*
import lecho.lib.hellocharts.listener.PieChartOnValueSelectListener
import lecho.lib.hellocharts.model.PieChartData
import lecho.lib.hellocharts.model.SliceValue
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class MeetUpVotingFragment : BaseMeetFragment() {

    private var meetId: String? = null
    var placeShares : ArrayList<Triple<String?,Int,Double>> = ArrayList()
    //first{placeId :: }
    var highestVotes : Triple<String?,Int,Double>? = null
    var chartSlices : ArrayList<SliceValue> = ArrayList()
    lateinit var colorArray : IntArray

   // override val TAG = MeetUpVotingFragment::class.simpleName!!

    var placeDetailAlert : PlaceDetailAlert? = null
    var customPlaceAlert : CustomPlaceDetailAlert? = null
    var voteCountAlert : VotedPlaceAlert? = null
    lateinit var placeAdpter : MeetUpChatChosenPlacesAdapter

    override val meetUpViewModel: MeetUpViewModel by viewModels()
    val placeViewModel: PlacesViewModel by viewModels()
    val fireViewModel: FireBaseViewModal by viewModels()
    lateinit var layoutManager : LinearLayoutManager

    private var _binding: FragmentMeetupVotingBinding? = null
    private val binding get() = _binding!!

    var mostPreviousTimeStamp: Timestamp? = null
    lateinit var dmAdapter: MeetChatAdapter
    var meetUpDetailDialog : MeetUpDetailDialog? = null
    var participants: List<MeetPerson>? = null
    var chatListener: ListenerRegistration? = null
    var votingListener : ListenerRegistration? = null
    var fireMeetDetail : MeetFireData? = null
    var maxRemainVote : Int? = null

    var loading = false
    var MEETDATA : GetMeetUpResponseItem? = null
    lateinit var deleteSheet: NewBottomSheet
    var meetOptions =  Constant.meetOption.apply {
        setBottomSheetLitener(object : BottomSheetOptions.BottomSheetListener{
            override fun bottomSheetClickedOption(buttonClicked: String) {
                when(buttonClicked){
                    BottomSheetOptions.BUTTON1 -> changeName(MEETDATA)
                    BottomSheetOptions.BUTTON2 -> addToCalendar(MEETDATA)
                    //BottomSheetOptions.BUTTON3 -> addToCalendar(meetUpdata)
                    BottomSheetOptions.BUTTON4 -> addfriend(MEETDATA,TAG)
                    BottomSheetOptions.BUTTON5 -> {}
                    BottomSheetOptions.BUTTON6 -> {
                        Log.e(TAG," Options::: Opt Out ")
                        val proceedDialog = ProceedDialog(requireActivity()) { b: Boolean ->
                            if(b) {
                                optOut(MEETDATA)
                            }
                        }
                        proceedDialog.setMessage("Opt out","Are you sure you want to opt out?")
                    }
                    BottomSheetOptions.BUTTON7 -> {
                        Log.e(TAG," Options::: Edit Rule ")
                        Navigation.addFragment(requireActivity(),EditRuleFragment.getInstance(MeetUpVotingFragment.TAG,MEETDATA?._id,MEETDATA?.fb_chat_id),EditRuleFragment.TAG,R.id.homeFram,true,false)
                    }
                    BottomSheetOptions.BUTTON8 -> {
                        Log.e(TAG," Options::: Cancelling ")
                        val showProceed = showProceed {
                            cancleMeetUp(MEETDATA)
                        }
                        showProceed.setMessage("Cancel Meetup","Are you sure you want to cancel this Meetup?")
                    }

                }
            }
        })
    }

    companion object{
        val TAG = MeetUpVotingFragment::class.simpleName!!
        fun getInstance(meetUpId:String):MeetUpVotingFragment{
            return MeetUpVotingFragment().apply {
                arguments = bundleOf("meetUpId" to meetUpId)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMeetupVotingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()
        addObserver()
    }

    private fun initView(view: View) {
        //binding.etMsg.setRoundedColorBackground(requireActivity(),R.color.gray1)
        meetId = arguments?.getString("meetUpId")
        deleteSheet= NewBottomSheet(requireActivity(), arrayListOf(ItemModel("delete","Delete"))){
            if(it=="delete"){
                val proceedDialog = ProceedDialog(requireActivity()) { b: Boolean ->
                    if(b) {
                        if(MEETDATA?.user_id == MyApplication.SID)
                            deleteMeetUp(meetId,MeetScope.PREVIOUS.scope)
                        else
                            optOut(MEETDATA)
                    }
                }
                proceedDialog.setMessage("Delete","Are you sure you want to delete?")
            }
        }
        binding.lottiAnim.setRoundedColorBackground(requireActivity(),R.color.gray1)
        binding.send.setOnTouchListener(null)
        binding.maxVote.setRoundedColorBackground(requireActivity(), corner = 40f)
        colorArray = requireContext().resources.getIntArray(R.array.rainbow)
        meetUpDetailDialog=MeetUpDetailDialog(requireActivity()){isItJoin,invitationId,meetUpId->

        }
        binding.rvParticipant.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.HORIZONTAL,false)
        binding.rvPlaces.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.HORIZONTAL,false)
        layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, true)
            .apply { stackFromEnd = true }
        binding.rvChat.layoutManager = layoutManager
        dmAdapter = MeetChatAdapter(requireActivity(),binding.rvChat,layoutManager,binding,{ id ->
            Log.i(TAG," PreviewPlaceId::: $id  ")
            dmAdapter.places.firstOrNull { it?._id == id }?.let{
                MyApplication.putTrackMP(Constant.AcMeetUpChatPlaceName, JSONObject(mapOf("placeId" to id)))
                placeDetailAlert?.showDialog(null)
                placeViewModel.getPlace(id,null)
            }
            dmAdapter.customPlaces.firstOrNull{ it._id == id}?.let {
                MyApplication.putTrackMP(Constant.AcMeetUpChatPlaceName, JSONObject(mapOf("placeId" to id)))
                showCustomPlaceAlert(LatLng(it.getLongitude(),it.getLatitude()))
            }
        },{
            MyApplication.putTrackMP(Constant.AcMeetUpChatUserName, JSONObject(mapOf("sid" to it)))
            openProfile(it,Constant.Source.MEETUPCHAT.sorce.plus(meetId))
        })
//        binding.rvChat.adapter = TempRecyclerViewAdapter(requireContext(), R.layout.card_contact)
        binding.rvChat.adapter = dmAdapter
        Log.i(TAG," meetUpId::: $meetId")
        getMeetUpDet()

    }

    private fun setUp() {
        setFragmentResultListener(MeetUpVotingFragment.TAG) {key, result->
            Log.i(TAG," checkingResult::: $key -- $result")
            if(result.getBoolean("refresh")){
                getMeetUpDet()
            }
        }

        voteCountAlert = VotedPlaceAlert {  }
        binding.cancel.setOnClickListener { activity?.onBackPressed() }
        binding.imHere.onClick( {
            if (Utils.checkPermission(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                (activity as MainActivity?)?.enableLocationStuff(654, 655){
                    iamHere(meetId)
                }
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    655
                )
            }


        })

        //------------------- for making constrain layout heign according to screen size which reside in nestedScrollView -------
        val viewTreeObserver = requireView().viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    requireView().viewTreeObserver.removeOnGlobalLayoutListener(this)
                    var param = binding.clContent.layoutParams
                    param.height = binding.tocheckheight.height
                    binding.clContent.layoutParams = param
                }
            })
        }
        //--------------------------------------------------------------------------------------------------------------------------


        binding.chart.onValueTouchListener = ValueTouchListener(requireActivity(),voteCountAlert!!,this)
        val messageSwipeController = MessageSwipeController(requireContext()) {
            //binding.rootReply.visibility = View.VISIBLE
            binding.replyinto.text =
                if (dmAdapter.getItem(it)?.senderSid == MyApplication.SID) "Replying to Yourself" else "Replying to ".plus(
                    //otherprofile?.cust_data?.username
                    participants?.firstOrNull{ participant -> participant.sid == dmAdapter.getItem(it)?.senderSid}?.username
                )

            //binding.replyMsg.text = dmAdapter.getItem(it)?.body
            when(dmAdapter.getItem(it)?.type){
                "text"->{
                    binding.rootReply.visibility = View.VISIBLE
                    binding.replyImage.visibility = View.GONE
                    binding.replyMsg.text = dmAdapter.getItem(it)?.body
                    dmAdapter.initReply(true, it)
                }
                "image"->{
                    binding.rootReply.visibility = View.VISIBLE
                    binding.replyImage.visibility = View.VISIBLE
                    var text : String = ""
                    dmAdapter.getItem(it)?.let { parent ->
                        if(parent.senderSid.equals(MyApplication.SID)){
                            dmAdapter.places.firstOrNull { place-> place?._id == parent.body }?.let {
                                text = "You".plus(" voted ").plus(it.name.en)
                            }?:run{
                                text = "You".plus(" voted ").plus(dmAdapter.customPlaces.firstOrNull { cutomPlace ->  cutomPlace._id == parent.body }?.name)
                            }
                        }else{
                            dmAdapter.places.firstOrNull { it?._id == parent.body }?.let {
                                text  = dmAdapter.participants?.firstOrNull { it.sid == parent.senderSid }?.username.plus(" voted ").plus(it?.name?.en)
                            }?:run{
                                text  = dmAdapter.participants?.firstOrNull { it.sid == parent.senderSid }?.username.plus(" voted ").plus(dmAdapter.customPlaces.firstOrNull { it._id == parent.body }?.name)
                            }
                        }
                    }
                    binding.replyMsg.text = text
                    Utils.stickImage(requireContext(),binding.replyImage,dmAdapter.getItem(it)?.body,null)
                    dmAdapter.initReply(true, it)
                }else->{
                    binding.rootReply.visibility = View.GONE
                    dmAdapter.initReply(false)
                }
            }

            binding.etMsg.requestFocus()
            val inputMethodManager = ContextCompat.getSystemService(requireContext(),
                InputMethodManager::class.java)
            inputMethodManager?.showSoftInput(binding.etMsg, InputMethodManager.SHOW_IMPLICIT)
        }
        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(binding.rvChat)

        binding.cancelReply.setOnClickListener {
            binding.rootReply.visibility = View.GONE
            dmAdapter.initReply(false)
        }
    }

    override fun populateView(data: GetMeetUpResponseItem?) {
        MEETDATA = data
        setHeaderUI(data)
        Log.i(TAG, " populatView  1 ${data?.custom_places}")
        binding.meetUpName.text = data?.name?.capitalize()
        binding.tvOptions.onClick({
            MyApplication.putTrackMP(Constant.AcMeetUpChatMore, JSONObject(mapOf("meetId" to data?._id)))
            openOption(data)
        },1000)

        if(data?.voting_closed != true){
            Utils.showToolTips(requireActivity(),binding.rvPlaces,binding.root,Tooltip.TOP,"Click on any of the card to preview a place or vote for it.","rvPlaces"){

            }
        }
        binding.meetUpName.onClick( {
            MyApplication.putTrackMP(Constant.AcMeetUpChatName, JSONObject(mapOf("meetId" to data?._id)))
            Log.d("TAG", "populateView: showDialog")
            meetUpDetailDialog?.showDialog(data)
            meetUpViewModel.showMeetDetailUpDetail(data?._id)
        })
        setImHereVisiBility(data)
        /*if(data?.voting_closed == true) {
            binding.rvPlaces.visibility = View.GONE
            binding.confirm.visibility = View.GONE

        }*/
        var take = data?.participants?.take(4)
        var type = Constant.EnumMeetPerson.PARTICIPANT
        if(DateUtils.isToday(data?.date?.toDate()?.time?:0L)||data?.date?.toDate()?.after(Date())==true){
            //today
            type = Constant.EnumMeetPerson.PARTICIPANT
            take = data?.participants?.take(4)
        }else if(data?.date?.toDate()?.before(Date())==true){
            //Previous
            type = Constant.EnumMeetPerson.ATTENDEE
            take = data?.attendees?.take(4)
        }
        binding.rvParticipant.adapter = MeetPeopleAdapter(requireActivity(), take,data){
            MyApplication.putTrackMP(Constant.AcMeetUpChatParticipant, JSONObject(mapOf("meetId" to data?._id)))
            var fragment = MeetAttendeeList.getInstance(data?._id, type)
            Navigation.addFragment(requireActivity(),fragment,MeetAttendeeList.TAG,R.id.homeFram,true,false)
        }
        if(!data?.user_id.equals(MyApplication.SID))
            binding.confirm.visibility = View.GONE
        placeDetailAlert = PlaceDetailAlert(requireActivity(),data?.places,data?.custom_places){
            MEETDATA?.fb_chat_id?.let {fbid->
                votePlace(it,fbid)
            }
        }
        customPlaceAlert = CustomPlaceDetailAlert(requireActivity(),data?.places,data?.custom_places){
            data?.fb_chat_id?.let {fbid-> votePlace(it,fbid) }
        }
        placeAdpter = MeetUpChatChosenPlacesAdapter(requireActivity(),MyApplication.SID == data?.user_id,data?.places,data?.custom_places?.map { it.selected = false; it },colorArray,
            {id ,postion->
            //Place -- Voted
//            data?.fb_chat_id?.let {fbid-> votePlace(id,fbid) }
                MyApplication.putTrackMP(Constant.AcMeetUpChatChosenPlace, JSONObject(mapOf("placeId" to id)))
            placeDetailAlert?.showDialog(postion)
            placeViewModel.getPlace(id,null)
        },{ address, position ->
            //customPlaceAlert?.showDialog(LatLng(it?.getLatitude()?:0.0,it?.getLongitude()?:0.0))
            MyApplication.putTrackMP(Constant.AcMeetUpChatChosenPlace, JSONObject(mapOf("placeId" to id)))
            showCustomPlaceAlert(LatLng(address?.getLongitude()?:0.0,address?.getLatitude()?:0.0),position)
        },{
            MyApplication.putTrackMP(Constant.AcMeetUpChatAddPlace,null)
            var fragment = AddPlaceToMeetUp.getInstance(data?._id,ArrayList(data?.places?.map { it._id }),ArrayList(data?.custom_places?.map { it._id }),ArrayList(data?.places?.map { it.elastic_id }))
            Navigation.addFragment(requireActivity(),fragment,AddPlaceToMeetUp.TAG,R.id.homeFram,true,needAnimation = true)
            // add more Place
        })
        binding.finalParticipant.text = data?.participants?.filter { it.sid != MyApplication.SID }?.map { it.username }?.joinToString(",")
        binding.rvPlaces.adapter = placeAdpter
        binding.confirm.onClick( {
            MyApplication.putTrackMP(Constant.AcMeetUpConfirm, JSONObject(mapOf("meetId" to data?._id)))
            highestVotes?.let { highestVotes->
                data?.places?.firstOrNull{ it._id == highestVotes.first}?.let{
                    meetUpViewModel.confirmMeetPlace(data?._id,"meets",highestVotes.first?:data.places.getOrNull(0)?._id)
                }
                data?.custom_places?.firstOrNull{ it._id == highestVotes.first}?.let {
                    meetUpViewModel.confirmMeetPlace(data?._id,"custom",highestVotes.first?:data.custom_places.getOrNull(0)?._id)
                }

            }?:run{
                Log.i(TAG,"countingCOnfirm::: ${data?.places?.size?.plus(data.custom_places?.size?:0)}")
                if(data?.places?.size?.plus(data.custom_places?.size?:0)?.compareTo(1) == 0){
                    data?.places?.getOrNull(0)?.let{
                        meetUpViewModel.confirmMeetPlace(data?._id,"meets",data.places.getOrNull(0)?._id)
                    }
                    data?.custom_places?.getOrNull(0)?.let {
                        meetUpViewModel.confirmMeetPlace(data?._id,"custom",data.custom_places.getOrNull(0)?._id)
                    }
                    //meetUpViewModel.confirmMeetPlace(data?._id,"meets",data.places.getOrNull(0)?._id?:data.custom_places?.getOrNull(0)?._id)
                }else{
                    MyApplication.showToast(requireActivity(),"No place selected... ")
                }
            }
        })
        participants = data?.participants
        dmAdapter.setAdditionData(data?.participants,data?.places,data?.custom_places,colorArray)
        data?.fb_chat_id?.let { fbChatId ->
            chartSlices.clear()
            data.places?.forEachIndexed {index,element->
                chartSlices.add(SliceValue(1f,colorArray[index]))
            }
            data.custom_places?.forEachIndexed{ index,element ->
                chartSlices.add(SliceValue(1f,colorArray[index.plus(data.places?.size?:0)]))
            }
            var pieData = PieChartData(chartSlices)
            val bolder = ResourcesCompat.getFont(requireContext(),R.font.poppins_bold)
            val normal = ResourcesCompat.getFont(requireContext(),R.font.poppins)

            Log.i(TAG, " populatViewOnen:::   4")
            pieData.setHasLabels(true)
            pieData.setHasLabelsOnlyForSelected(true);
            pieData.setHasLabelsOutside(false);
            pieData.setHasCenterCircle(true);
            pieData.centerCircleScale=0.75f

            pieData.slicesSpacing=0
            pieData.centerText1Typeface= bolder
            pieData.centerText1FontSize = 20
            pieData.centerText1Color = ContextCompat.getColor(requireActivity(), R.color.primary)

            pieData.centerText2Typeface= bolder
            pieData.centerText2FontSize = 15
            pieData.centerText2Color = ContextCompat.getColor(requireActivity(), R.color.primary)
            pieData.centerText1 = "0%"

            Log.i(TAG, " populatViewOnen::: 5 $mostPreviousTimeStamp fbChatID $fbChatId")
            binding.chart.pieChartData = pieData
            binding.chart.startDataAnimation()


            fireViewModel.getGroupChat(mostPreviousTimeStamp, fbChatId)
            chatListener?.remove()
            chatListener = FireBaseUtils.getMeetChatListener(fbChatId) {
                if (dmAdapter.getList().size > 0)
                    fireViewModel.getMeetChatChanges(fbChatId, dmAdapter.getList().firstOrNull() { it?.timestamp != null })
                Log.i(TAG, " populatViewOnen:::   2")
            }
            votingListener?.remove()
            votingListener = FireBaseUtils.getMeetVoteListener(fbChatId){ fireMeetDetail ->
                fireMeetDetail?.let {
                    this.fireMeetDetail = fireMeetDetail
                    //if(maxRemainVote == null || fireMeetDetail?.votes?.get(MyApplication.SID)?.maxVotUpdated == true ){
                        Log.i(TAG," trying to Update:: 0")
                        maxRemainVote = fireMeetDetail?.votes?.get(MyApplication.SID)?.max_vote_changes?:fireMeetDetail.max_vote_changes
                        val remainVote = if(maxRemainVote?.compareTo(10) == 1) "∞" else maxRemainVote.toString()
                        setRemailVote(remainVote)
                       /* if(fireMeetDetail?.votes?.get(MyApplication.SID)?.maxVotUpdated == true){
                            Log.i(TAG," trying to Update:: 1")
                            meetUpViewModel.updateMaxVotUpdateToFalse(fbChatId,fireMeetDetail?.votes?.values?.firstOrNull { it.sid == MyApplication.SID })

                        }*/
                    //}
                    voteCountAlert?.setData(fireMeetDetail,placeShares,participants,data.places,data.custom_places)
                    Log.i(TAG," changingListened::: 0")
                    Log.i(TAG,"  checkingComingParticipant::: ${data.participants.size} ${fireMeetDetail?.participants?.size} -- ${fireMeetDetail.participants} -- ")

                    /**
                     * check firebase participant and meet detail participant to check if there is any new participant
                     * so that we update our participant list if not done it will show show "null voted" and "null message"
                     */

                    Log.i(TAG, "participantCount: ${data.participants.size} -- firecount-- ${fireMeetDetail?.participants?.size}")
                    if(data.participants.size != fireMeetDetail?.participants?.size){
                        getMeetUpDet()
                    }

                    Log.i(TAG," changingListened::: 020")
                    if(fireMeetDetail?.votes == null)
                        return@getMeetVoteListener

                    Log.i(TAG," changingListened::: 2")
                    Log.i(TAG," changingListened::: 3")
                    Log.i(TAG," changingListened::: 4")
                    //var myVote = list.firstOrNull{ it.sid == MyApplication.SID}
                    var myVote = fireMeetDetail?.votes.values.firstOrNull { it.sid == MyApplication.SID }
                    Log.i(TAG," changingListened::: 5")
                    myVote?.let {
                        placeAdpter.setSelectedPlace(it.id)
                    }
                    Log.i(TAG," changingListened::: 6")

                    Log.i(TAG," changingListened::: 0")

                    //var isVotingClosed =  fireMeetDetail?.voting_closed
                    fireMeetDetail?.voting_closed?.let {
                        if(it){
                            binding.rvPlaces.visibility = View.GONE
                            //binding.llVoteCLose.visibility = View.VISIBLE
                            binding.confirm.visibility = View.GONE
                            setImHereVisiBility(data)

                            myVote?.let {
                                Log.i(TAG," myVote.animSeen::: ${myVote.animSeen}")
                                if(data.status.equals(MeetStatus.ACTIVE.status) && (myVote.animSeen == false || myVote.animSeen == null)){
                                    MeetConfirmAlert(activity).showDialog(data.participants)
                                    fireViewModel.confirmAnimSeen(myVote.apply { animSeen = true },data.fb_chat_id)
                                }
                            }
                            //var myVote = list.firstOrNull{ it.sid == MyApplication.SID}
                        }
                    }

                    placeShares.clear()
                    data.places?.forEach{ place->
                        var count = fireMeetDetail?.votes.count { it.value.id == place?._id }
                        var percent = 0.0
                        participants?.let {
                            percent = (count.toDouble() / fireMeetDetail?.votes.size) * 100
                        }
                        placeShares.add(Triple(place?._id,count,percent))
                        Log.i(TAG," placeCounr::: ${place?._id} -- $count")
                    }
                    data.custom_places?.forEach { customPlace ->
                        var count = fireMeetDetail?.votes.count { it.value.id == customPlace._id }
                        var percent = 0.0
                        participants?.let {
                            percent = (count.toDouble() / fireMeetDetail?.votes.size) * 100
                        }
                        placeShares.add(Triple(customPlace._id?:"",count,percent))
                        Log.i(TAG," cutomPlacePlaceCount::: ${customPlace._id} -- $count")
                    }
                    if(placeShares.maxByOrNull { it.second }?.second?.compareTo(0) == 1)
                        processVotes(data,pieData)
                    else{
                        if(placeShares.size == 1){
                            binding.ivName.visibility = View.GONE
                            data.places?.getOrNull(0)?.let {
                                Utils.stickImage(requireContext(),binding.votedPlace,it.main_image_url?:it.main_web_image_url,null)
                                binding.name.text=it.name.en
                                binding.votedPlace.onClick({
                                    Log.d(TAG, "votedPlaceClick: 0")
                                    MyApplication.putTrackMP(Constant.AcMeetUpVotedPlace, JSONObject(mapOf("placeId" to it._id)))
                                    placeDetailAlert?.showDialog(null)
                                    placeViewModel.getPlace(it._id,null)
                                })

                            }
                            data.custom_places?.getOrNull(0)?.let{
                                binding.votedPlace.setImageResource(R.drawable.ic_bg_default_place_h)
                                binding.name.text = it.name
                                binding.votedPlace.onClick({
                                    Log.d(TAG, "votedPlaceClick: 1")
                                    showCustomPlaceAlert(LatLng(it?.getLongitude()?:0.0,it?.getLatitude()?:0.0))
                                })
                            }
                        }
                    }
                    Log.i(TAG, " populatViewOnen:::   10")
                    Log.i(TAG," highestPlace::: ${placeShares.maxByOrNull { it.second }}")
                }?:run{
                    MyApplication.showToast(requireActivity(),"This meet is invalid...")
                    activity?.onBackPressed()
                }
            }
        }

        Utils.onClick(binding.send, 1000) {
            Log.i(TAG, " msg::: ${data?.fb_chat_id}")
            data?.fb_chat_id?.let {
                MyApplication.putTrackMP(Constant.AcMeetUpChatSend, JSONObject(mapOf("meetId" to data._id)))
                sendMessage(it)
            }
        }


        binding.rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                //if (!isLoading() && !isLastPage()) {
                if (!loading) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                        && firstVisibleItemPosition >= 0
                    ) {
                        Log.i(TAG," mostPreviousTimeStamp:: ${mostPreviousTimeStamp} -- ${data?.fb_chat_id}")
                        loading = true
                        fireViewModel.getGroupChat(
                            mostPreviousTimeStamp,
                            data?.fb_chat_id ?: ""
                        )
                    }
                }
            }
        })
    }

    fun setHeaderUI(data: GetMeetUpResponseItem?){
        val placeCount = (data?.places?.size?:0).plus(data?.custom_places?.size?:0)
        if(placeCount <=1){
           var param =  binding.giude7.layoutParams as ConstraintLayout.LayoutParams
            param.guidePercent = 0.99f
            binding.giude7.layoutParams = param
            binding.chart.visibility = View.GONE
            binding.line.visibility = View.GONE
            binding.maxVote.visibility = View.GONE

            var heightparam = binding.cardHeight.layoutParams as ConstraintLayout.LayoutParams
            heightparam.height = 170.px
            binding.cardHeight.layoutParams = heightparam
        } else{
            var param =  binding.giude7.layoutParams as ConstraintLayout.LayoutParams
            param.guidePercent = 0.63f
            binding.giude7.layoutParams = param
            binding.chart.visibility = View.VISIBLE
            binding.line.visibility = View.VISIBLE
            binding.maxVote.visibility = View.VISIBLE

            var heightparam = binding.cardHeight.layoutParams as ConstraintLayout.LayoutParams
            heightparam.height = 50.px
            binding.cardHeight.layoutParams = heightparam
        }
    }

    private fun setRemailVote(remainVote: String) {
        val remainVote = remainVote.plus(" Votes remaining")
        val ss1 = SpannableString(remainVote)
        ss1.setSpan(RelativeSizeSpan(1.8f), 0, remainVote.indexOf("Votes"), 0) // set size
        ss1.setSpan(ForegroundColorSpan(Color.BLACK), 0, remainVote.indexOf("Votes"), 0) // set color
        binding.maxVote.setText(ss1)
    }

    fun showCustomPlaceAlert(latLng: LatLng,position:Int? = null) {
        var fragment = customPlaceAlert
        customPlaceAlert?.setLatLng(latLng,position)
        if(fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(childFragmentManager, fragment.tag)

    }

    fun setImHereVisiBility(data: GetMeetUpResponseItem?) {


        Log.i(TAG," setImHereVisiBility:: 0")
        Log.i(TAG," datadata:: ${data}")
        if(data?.status.equals(MeetStatus.CANCELLED.status)){
            Log.i(TAG," setImHereVisiBility:: 1")
            binding.llVoteCLose.visibility = View.GONE
            binding.rvPlaces.visibility = View.GONE
            binding.confirm.visibility = View.GONE
        }
        else if(data?.voting_closed == true){
            Log.i(TAG," setImHereVisiBility:: 2")
            binding.rvPlaces.visibility = View.GONE
            binding.confirm.visibility = View.GONE
            binding.llVoteCLose.visibility = View.VISIBLE
            if(!DateUtils.isToday(data?.date?.toDate()?.time?:0L)){
                Log.i(TAG," setImHereVisiBility:: 3")
                binding.imHere.visibility = View.GONE
                binding.llScheduleMsg.visibility = View.VISIBLE
            }else{
                Log.i(TAG," setImHereVisiBility:: 4")
                binding.imHere.visibility = View.VISIBLE
                binding.llScheduleMsg.visibility = View.GONE
            }
            data?.attendees?.firstOrNull { it.sid ==  MyApplication.SID}?.let {
                binding.llVoteCLose.visibility = View.GONE
            }
        }
        Log.i(TAG," heckingdate::: ${data?.date?.toDate()?.compareTo(Date())}")
        //if(data?.date?.toDate()?.compareTo(Date()) == -1){
        if(data?.date?.toDate()?.compareTo(Date()) == -1 && !DateUtils.isToday(data?.date?.toDate()?.time?:0L)){
            Log.i(TAG," setImHereVisiBility:: 5")
            binding.llVoteCLose.visibility = View.GONE
            binding.rvPlaces.visibility = View.GONE
        }
    }

    fun processVotes(data: GetMeetUpResponseItem,pieData: PieChartData) {
        highestVotes  = placeShares.maxByOrNull { it.second }
        Log.i(TAG," highestVotes::: ${highestVotes}")
        data.places?.firstOrNull{ it?._id == highestVotes?.first}?.let {
            Log.i(TAG," highestVotes::: 1")
            //binding.llHighPlaceDetail.visibility = View.VISIBLE
            binding.ivName.visibility = View.GONE
            Utils.stickImage(requireContext(),binding.votedPlace,it.main_image_url?:it.main_web_image_url,null)
            binding.votedPlace.onClick({
                Log.d(TAG, "votedPlaceClick: 2")
                placeDetailAlert?.showDialog(null)
                placeViewModel.getPlace(it._id,null)
            })
            binding.name.text=it.name.en
            pieData.centerText1 = "${highestVotes?.third?.toInt()}%"
            val get = colorArray[data.places?.indexOfFirst { it?._id == highestVotes?.first }?:0]
            pieData.centerText1Color = get
            pieData.centerText2Color = get
            //pieData.centerText2= it.name.en?.take(2)
            binding.chart.pieChartData=pieData
        }?:run{
            Log.i(TAG," highestVotes::: 2")
            data.custom_places?.firstOrNull{ it._id == highestVotes?.first}?.let {
                Log.i(TAG," highestVotes::: 3")
                //binding.llHighPlaceDetail.visibility = View.VISIBLE
                //binding.ivName.visibility = View.VISIBLE
                //binding.ivName.text = it.name
                binding.votedPlace.setImageResource(R.drawable.ic_bg_default_place_h)
                binding.name.text = it.name
                binding.votedPlace.onClick({
                    Log.d(TAG, "votedPlaceClick: 3")
                    showCustomPlaceAlert(LatLng(it?.getLongitude()?:0.0,it?.getLatitude()?:0.0))
                })
                pieData.centerText1 = "${highestVotes?.third?.toInt()}%"
                val get = colorArray[data.custom_places.indexOfFirst { it._id == highestVotes?.first }.plus(data.places?.size?:0)]
                pieData.centerText1Color = get
                pieData.centerText2Color = get
                binding.chart.pieChartData=pieData


            }
        }
        if(placeShares?.filter{ it.second.toFloat() > 0.5f}.isNotEmpty()){
            placeShares.forEachIndexed {index,element->
                Log.i(TAG," size:: ${chartSlices.size} indexed:: ${index}  placeShareSize:: ${placeShares.size}")
                chartSlices.get(index).setTarget(element.second.toFloat())
            }
            binding.chart.startDataAnimation()
        }else{
            Log.i(TAG," No Vote At all ")
        }
    }

    fun votePlace(id: String?, fbid: String) {

        if(maxRemainVote == 0){
            Toast.makeText(requireContext(),"Changing vote attempts finished!!",Toast.LENGTH_SHORT).show()
            return
        }
        maxRemainVote = maxRemainVote?.minus(1)
        val remainVote = if(maxRemainVote?.compareTo(10) == 1) "∞" else maxRemainVote.toString()
        setRemailVote(remainVote)
        //binding.maxVote.text = if(maxRemainVote?.compareTo(10) == 1) "∞" else maxRemainVote.toString()
        var ducument = fireViewModel.sendMeetMessage(
            fbid,
            //place?.main_image_url?:place?.main_web_image_url?:"",
            id,
            null,
            "image"
        ) {
            Log.i(TAG, "Messaged Sent")
        }
        fireViewModel.votePlace(id,fbid,maxRemainVote)
        dmAdapter.addNewDummyMessage(
            ChatDM(
                id = ducument.id,
                body = id,
                type="image",
                senderSid = MyApplication.SID
            )
        )

    }

    fun sendMessage(fbChatId: String) {
        if (binding.etMsg.text.toString().trim().isNotEmpty()) {
            var ducument = fireViewModel.sendMeetMessage(
                fbChatId,
                binding.etMsg.text.toString().trim(),
                if (dmAdapter.isItReply) dmAdapter.parentMsg?.apply {
                    //parentMsg = null
                } else null,
                "text"
            ) {
                Log.i(TAG, "Messaged Sent")
            }
            dmAdapter.initReply(false)
            binding.rootReply.visibility = View.GONE
            dmAdapter.addNewDummyMessage(
                ChatDM(
                    //id = ducument.id,
                    id = "ducument.id",
                    body = binding.etMsg.text.toString().trim(),
                    type="text",
                    senderSid = MyApplication.SID
                )
            )
            binding.etMsg.setText("")

        }
    }

    fun openOption(data: GetMeetUpResponseItem?) {
        val meetDate = data?.date?.toDate("yyyy-MM-dd")
        val isCanceled= data?.status == MeetStatus.CANCELLED.status
        if(meetDate?.before(Date())==true && !DateUtils.isToday(meetDate.time) || isCanceled){
            deleteSheet.show()
            return
        }
        var invisibleOption = arrayListOf<Int>()
        invisibleOption.add(4)
        invisibleOption.add(2)//make Open meetUp

        if(data?.user_id.equals(MyApplication.SID)){
//            if(!DateUtils.isToday(data?.date?.toDate()?.time?: Date().time) || (data?.attendees?.firstOrNull{ it.sid == MyApplication.SID } != null))
//                invisibleOption.add(4)
            when(data?.status){
                MeetStatus.CANCELLED.status -> invisibleOption.add(6)//cancel meetUp
            }
            invisibleOption.add(5) //OptOut
            meetOptions.hideOption(invisibleOption.toTypedArray())
            showOption(meetOptions)
        }else{
            invisibleOption.add(0)//change Name
            invisibleOption.add(6)//Edit Rule
            invisibleOption.add(7)//cancel meetUp
            Log.i(TAG,"  checkAttendee:: ${(data?.attendees?.firstOrNull{ it.sid == MyApplication.SID } != null)}")
//            if((!DateUtils.isToday(data?.date?.toDate()?.time?: Date().time)) || (data?.attendees?.firstOrNull{ it.sid == MyApplication.SID } != null))
//                invisibleOption.add(4) //I am here
            meetOptions.hideOption(invisibleOption.toTypedArray())
            showOption(meetOptions)
        }
    }

    fun showOption(sheet: BottomSheetDialogFragment?){
        if (sheet == null || sheet.isAdded) {
            return
        }
        sheet.show(childFragmentManager, sheet.tag)
    }

    override fun addObserver() {
        super.addObserver()

        meetUpViewModel.observeMeetUpDetailForDialog().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                    meetUpDetailDialog?.showData(it.value)
                }
                is ResultHandler.Failure -> {
                    showToast("Something went wrong!!")
                    meetUpDetailDialog?.hideDialog()
                }
            }
        })
        placeViewModel.observeSinglePlace().observe(viewLifecycleOwner,  {
            Log.i(TAG, "onSinglePlace: 3 - ${placeViewModel.observeSinglePlace().hashCode()}")
            when (it) {
                is ResultHandler.Success -> {
                    Log.i("TAG", "populateView: onSinglePlace  -1")
                    placeDetailAlert?.populateView(it.value)
                }
                is ResultHandler.Failure->{
                    Log.i("TAG", "populateView: onSinglePlace  error")
                    MyApplication.showToast(requireActivity(),"Something went wrong",Toast.LENGTH_SHORT)
                    placeDetailAlert?.hideDialog()
                }
            }
        })

        meetUpViewModel.observeMeetUpConfirm().observe(viewLifecycleOwner,{
            when (it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity()," MeetPlace Chosen!!! ",Toast.LENGTH_SHORT)
//                    ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager,1) as MeetUpNew).getMeetUps()

                }
                is ResultHandler.Failure->{
                    MyApplication.showToast(requireActivity(),"Something went wrong",Toast.LENGTH_SHORT)
                }
            }
        })
        meetUpViewModel.observeDeleteMeetUp().observe(viewLifecycleOwner,{
            when (it) {
                is ResultHandler.Success -> {
                    showToast("Meetup Deleted!! ",Toast.LENGTH_SHORT)
//                    ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 1) as MeetUpNew).refresh()
                    activity?.onBackPressed()
                }
                is ResultHandler.Failure->{
                    showToast("Something went wrong",Toast.LENGTH_SHORT)
                }
            }
        })

        fireViewModel.observerMeetChatChangesMessage().observe(viewLifecycleOwner,  {
            when (it) {
                is ResultHandler.Success -> {
                    if (it.value.isNotEmpty()) {
                       // requireActivity().runOnUiThread(Runnable {
                            dmAdapter.addReplaceMessage(ArrayList(it.value))
                        //})

                    }
                }
                is ResultHandler.Failure -> {
                    Log.e(TAG, "${it.throwable?.printStackTrace()}")
                }
            }
        })


        fireViewModel.observeMeetChat().observe(viewLifecycleOwner,{
            when (it) {
                is ResultHandler.Success -> {
                    if (it.value.isNotEmpty()) {
                        mostPreviousTimeStamp = it.value.last()?.timestamp
                        dmAdapter.addItems(it.value)
                    }
                }
                is ResultHandler.Failure -> {
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
            loading = false
        })
        meetUpViewModel.observeNameChange().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(),"Name changed!!")
                    meetUpViewModel.getMeetUpDetail(meetId)
                }
                is ResultHandler.Failure ->{
                    MyApplication.showToast(requireActivity(),"Something went wrong!!!")
                }
            }
        })
        meetUpViewModel.observeOptOut().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(),"Opt Out from Meetup!!")
                    activity?.onBackPressed()
                }
                is ResultHandler.Failure ->{
                    MyApplication.showToast(requireActivity(),"Something went wrong!!!")
                }
            }
        })
        meetUpViewModel.observeCancelMeetUp().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(),"MeetUp Cancelled!!")
                    getMeetUpDet()
                }
                is ResultHandler.Failure ->{
                    MyApplication.showToast(requireActivity(),"Something went wrong!!!")
                }
            }
        })

    }

    fun getMeetUpDet() {
        meetUpViewModel.getMeetUpDetail(meetId)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG," onRequestPermissionsResult::: $requestCode  ")
        when (requestCode) {
            655 -> {
                var location = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (grantResults.isNotEmpty() && location) {
                    (activity as MainActivity?)?.enableLocationStuff(654, 655){
                        iamHere(meetId)
                    }
                } else if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    rationale()
                } else { }
            }


        }
    }

    class ValueTouchListener(val myContext : Activity ,val voteCountAlert : VotedPlaceAlert,var fragment : MeetUpVotingFragment) : PieChartOnValueSelectListener {

        override fun onValueSelected(arcIndex: Int, value: SliceValue) {
            MyApplication.putTrackMP(Constant.AcPMViewAll, JSONObject(mapOf("meetId" to fragment.MEETDATA?._id)))
            voteCountAlert.meetVoteDocumemt?.let {
                voteCountAlert.showDialog(myContext,arcIndex){
                    fragment.openProfile(it,Constant.Source.MEETUPCHAT.sorce.plus(fragment.MEETDATA?._id))
                }
            }

        }

        override fun onValueDeselected() {
            Log.i(ValueTouchListener::class.simpleName," OnValueDesected::")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        chatListener?.remove()
        votingListener?.remove()
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

}