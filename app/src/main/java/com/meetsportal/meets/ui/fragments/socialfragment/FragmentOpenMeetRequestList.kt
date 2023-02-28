package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.adapter.OpenMeetParticipantsAdapter
import com.meetsportal.meets.adapter.OpenMeetRequestAdapter
import com.meetsportal.meets.databinding.LayoutRecyclerViewBinding
import com.meetsportal.meets.models.GetJoinRequestModel
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.meetup.MeetPerson
import com.meetsportal.meets.ui.activities.HomeActivityNew
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentOpenMeetRequestList : BaseMeetFragment() {

    override val meetUpViewModel: MeetUpViewModel by viewModels()
    val participants: ArrayList<MeetPerson> = arrayListOf()
    val attendees: ArrayList<MeetPerson> = arrayListOf()
    override fun populateView(value: GetMeetUpResponseItem?) {
        value?.let { it ->
            participants.clear()
            participants.addAll(it?.participants)
            attendees.clear()
            it?.attendees?.let{
                attendees.addAll(it)
            }
            searchRequest(searchQuery)
//            adapter2?.submitList(participants,it?.attendees,it?.date,false)
//            binding.recycler.adapter = adapter2
//            if(participants?.isEmpty()){
//                binding.noData.noDataRoot.visibility = View.VISIBLE
//            }else{
//                binding.noData.noDataRoot.visibility = View.GONE
//            }
//                        participants.clear()
//                        participants.addAll(it.value?.participants)
//                        attendees.clear()
//                        attendees.addAll(it.value?.attendees ?: ArrayList())
        }?:run{
            showToast("Something went wrong!!!")
        }
    }
    var searchQuery : String?= null
    var adapter : OpenMeetRequestAdapter? = null
    var adapter2: OpenMeetParticipantsAdapter? = null
    var joinRequestList = GetJoinRequestModel()

    private var _binding: LayoutRecyclerViewBinding? = null
    private val binding get() = _binding!!

    val REQUESTIG_TYPE : String? by lazy{ arguments?.getString("status") }
    val MEETID : String? by lazy{ arguments?.getString("meetId") }
    val parentFragment : RequestToJoinOpenFragment by lazy {
            activity?.supportFragmentManager?.findFragmentByTag(
                RequestToJoinOpenFragment.TAG
            ) as RequestToJoinOpenFragment
    }
    companion object{
        val TAG = FragmentOpenMeetRequestList::class.simpleName

        fun getInstance(meetId : String?,status: Constant.RequestType):FragmentOpenMeetRequestList{
            return FragmentOpenMeetRequestList().apply {
                arguments = Bundle().apply {
                    putString("meetId",meetId)
                    putString("status",status.value)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = LayoutRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setUp()
        setObserver()
    }


    fun searchRequest(search : String?){
        searchQuery=search
        if(!search.isNullOrEmpty()){
            if(REQUESTIG_TYPE==Constant.RequestType.PENDING.value){
                val searchList = joinRequestList.filter { it.user_meta.username?.contains(search,true) == true }
                adapter?.submitList(searchList)
//                if(adapter?.itemCount?.compareTo(0) == 1) {
//                    binding.noData.noDataRoot.visibility = View.GONE
//                } else {
//                    binding.noData.noDataRoot.visibility = View.VISIBLE
//                }
            }else {
                val searchPart = participants.filter { it.username?.contains(search,true) }
                adapter2?.submit(searchPart,attendees)

//                if(adapter2?.itemCount?.compareTo(0) == 1) {
//                    binding.noData.noDataRoot.visibility = View.GONE
//                } else {
//                    binding.noData.noDataRoot.visibility = View.VISIBLE
//                }
            }
        }else{
            if(REQUESTIG_TYPE==Constant.RequestType.PENDING.value){
                adapter?.submitList(joinRequestList)
//                if(adapter?.itemCount?.compareTo(0) == 1) {
//                    binding.noData.noDataRoot.visibility = View.GONE
//                } else {
//                    binding.noData.noDataRoot.visibility = View.VISIBLE
//                }
            }else{
                adapter2?.submit(participants,attendees)
//                if(adapter2?.itemCount?.compareTo(0) == 1) {
//                    binding.noData.noDataRoot.visibility = View.GONE
//                } else {
//                    binding.noData.noDataRoot.visibility = View.VISIBLE
//                }
            }
        }
        Log.d(TAG, "searchRequest: REQUESTIG_TYPE 1::$REQUESTIG_TYPE, ${adapter?.itemCount} ${adapter2?.itemCount}")
        Log.d(TAG, "searchRequest: REQUESTIG_TYPE 2::$REQUESTIG_TYPE, ${adapter?.itemCount?.compareTo(0) == 1} ${adapter2?.itemCount?.compareTo(0) == 1}")
//        if(REQUESTIG_TYPE==Constant.RequestType.PENDING.value){
//            if(adapter?.itemCount?.compareTo(0) == 1) {
//                binding.noData.noDataRoot.visibility = View.GONE
//            } else {
//                binding.noData.noDataRoot.visibility = View.VISIBLE
//            }
//        }else{
//            if(adapter2?.itemCount?.compareTo(0) == 1) {
//                binding.noData.noDataRoot.visibility = View.GONE
//            } else {
//                binding.noData.noDataRoot.visibility = View.VISIBLE
//            }
//        }
    }

    private fun initView() {

        Log.i(TAG," MeetId ${arguments?.getString("meetId")}")
        Log.i(TAG," requestType ${arguments?.getString("status")}")

        if(REQUESTIG_TYPE.equals(Constant.RequestType.PENDING.value)) {
            binding.noData.noDataHeading.text = "Requested to Join"
            binding.noData.noDataDesc.text = "People who have requested\nto join will show here"
        }else if(REQUESTIG_TYPE.equals(Constant.RequestType.ACCEPTED.value)){
            binding.noData.noDataHeading.text = "Accepted to Join"
            binding.noData.noDataDesc.text = "PPeople you have accepted\nto join will show here"
        }

        binding.recycler.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false)
        adapter = OpenMeetRequestAdapter(requireActivity(), joinRequestList,true,{ id,nothing->
            Log.d(TAG, "initView:OpenMeetRequest Approve Accept $REQUESTIG_TYPE")
            when(REQUESTIG_TYPE){
                Constant.RequestType.PENDING.value -> {
                    meetUpViewModel.opMeetRqApprove(MEETID,id)
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        joinRequestList.removeIf { it._id==id }
                    }
                    adapter?.submitList(joinRequestList)
                }
//                Constant.RequestType.ACCEPTED.value -> {
//                    meetUpViewModel.opMeetRevertAccept(MEETID,id)
//                }

            }
        },{
          openProfile(it,Constant.Source.MEETUPCHAT.sorce.plus(MEETID))
        },{
            Log.d(TAG, "searchRequest: REQUESTIG_TYPE  OpenMeetRequestAdapter ::$it ")
            if(it==null){
                binding.noData.noDataRoot.visibility = View.GONE
            } else{
                binding.noData.noDataRoot.visibility = View.VISIBLE
            }
        })
        adapter2 = OpenMeetParticipantsAdapter(requireActivity(),true, {
            openProfile(it, Constant.Source.MEETUPCHAT.sorce.plus(FragmentOMInterestList.MEETID))
        }, {
            if(it==null) {
                binding.noData.noDataRoot.visibility = View.GONE
            }else if(it=="empty"){
                binding.noData.noDataRoot.visibility = View.VISIBLE
            }else if(it=="iam_here"){
                if (Utils.checkPermission(
                            requireActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                                         )
                ) {
                    (activity as MainActivity?)?.enableLocationStuff(654, 655){
                        iamHere(MEETID)
                    }
                } else {
                    requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            655
                                      )
                }
            }else{
                Log.d(TAG, "initView:OpenMeetRequest Remove Accept")
                meetUpViewModel.opMeetRevertAccept(MEETID,null,type = "user_id",user_id = it)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    participants.removeIf {it1-> it1.sid==it }
                }
                adapter2?.submit(participants,attendees)
            }
        })
        //binding.recycler.adapter = TempRecyclerViewAdapter(requireActivity(), R.layout.card_open_meet_request_item)
        if(REQUESTIG_TYPE==Constant.RequestType.PENDING.value){
            binding.recycler.adapter = adapter
        }else{
            binding.recycler.adapter = adapter2
        }
    }

    fun setUp() {
        Log.d(TAG, "initView:OpenMeetRequest setup called:${REQUESTIG_TYPE}")

        when(REQUESTIG_TYPE){
            Constant.RequestType.PENDING.value -> {
                meetUpViewModel.getJoinRequest(MEETID,Constant.RequestType.PENDING)
                adapter?.setRequestType(Constant.RequestType.PENDING)
            }
            Constant.RequestType.ACCEPTED.value -> {
                meetUpViewModel.getMeetUpDetail(MEETID)
                adapter?.setRequestType(Constant.RequestType.ACCEPTED)
            }

        }
    }

    private fun setObserver() {
        addObserver()
//        meetUpViewModel.observeMeetUpDetail().observe(viewLifecycleOwner,{
//            when(it){
//                is ResultHandler.Success -> {
//                    // adapter?.setData(it.value?: GetJoinRequestModel())
//                    Log.d("TAG", "addObserver: after iam here")
//                    it.value?.let { it1 ->
//                        adapter2?.submitList(it.value?.participants,it.value?.attendees,it.value?.date,false)
//                        binding.recycler.adapter = adapter2
////                        participants.clear()
////                        participants.addAll(it.value?.participants)
////                        attendees.clear()
////                        attendees.addAll(it.value?.attendees ?: ArrayList())
//                    }?:run{
//                        showToast("Something went wrong!!!")
//                    }
//                }
//                is ResultHandler.Failure ->{
//                    showToast("Something went wrong!!!")
//                }
//            }
//        })
        meetUpViewModel.observeOpenMeetJoinRequest().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                   // adapter?.setData(it.value?: GetJoinRequestModel())
                    joinRequestList.clear()
                    joinRequestList.addAll(it.value?:GetJoinRequestModel())
                    searchRequest(searchQuery)
//                    adapter?.submitList(it.value?: GetJoinRequestModel())
//                    if(joinRequestList.isEmpty()){
//                        binding.noData.noDataRoot.visibility = View.VISIBLE
//                    }else{
//                        binding.noData.noDataRoot.visibility = View.GONE
//                    }
//                    binding.recycler.adapter = adapter

                }
                is ResultHandler.Failure ->{
                    Log.e(TAG,"  OpenMeetJoinRequest Failed :: ")
                }
            }
        })

        meetUpViewModel.observerSingleMeetApproval().observe(viewLifecycleOwner,{
            parentFragment.setUp()
            //setUp()
            (parentFragment.viewPagerAdapter.instantiateItem(
                parentFragment.binding.pager,
                    if(REQUESTIG_TYPE == Constant.RequestType.PENDING.value) 1 else 0
            ) as FragmentOpenMeetRequestList?)?.setUp()

        })
        meetUpViewModel.observeOpMeeetRevertAccept().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                    parentFragment.setUp()
                    //setUp()
                    (parentFragment.viewPagerAdapter.instantiateItem(
                            parentFragment.binding.pager,
                            if(REQUESTIG_TYPE == Constant.RequestType.PENDING.value) 1 else 0
                                                                    ) as FragmentOpenMeetRequestList?)?.setUp()
                }
                is ResultHandler.Failure -> {
                    showToast("Something went wrong")
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
                                           ) {
        Log.i(TAG," onRequestPermissionsResult::: $requestCode  ")
        when (requestCode) {
            655 -> {
                val location = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (grantResults.isNotEmpty() && location) {
                    (activity as MainActivity?)?.enableLocationStuff(654, 655){
                        iamHere(MEETID)
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
    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }
}