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
import com.meetsportal.meets.adapter.OpenMeetInterestAdapter
import com.meetsportal.meets.adapter.OpenMeetParticipantsAdapter
import com.meetsportal.meets.databinding.LayoutRecyclerViewBinding
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
class FragmentOMInterestList : BaseMeetFragment() {

    //    var response: GetMeetUpResponseItem? = null
    var status: String? = null
    override val meetUpViewModel: MeetUpViewModel by viewModels()
    override fun populateView(value: GetMeetUpResponseItem?) {
        popView(value)
    }

    val participants: ArrayList<MeetPerson> = ArrayList()
    val attendees: ArrayList<MeetPerson> = ArrayList()
    private var _binding: LayoutRecyclerViewBinding? = null
    var adapter1: OpenMeetInterestAdapter? = null
    var adapter2: OpenMeetParticipantsAdapter? = null
    private val binding get() = _binding!!

    companion object {

        val TAG = FragmentOMInterestList::class.simpleName

        var MEETID: String? = null
        fun getInstance(meetId: String?, status: Constant.RequestType): FragmentOMInterestList {
            return FragmentOMInterestList().apply {
                MEETID = meetId
                arguments = Bundle().apply {
                    putString("status", status.value)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = LayoutRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setUp()
        setObservers()
    }

    private fun setUp() {
        if(status == Constant.RequestType.PENDING.value) {
            meetUpViewModel.getJoinRequest(MEETID, Constant.RequestType.PENDING)
        } else {
            meetUpViewModel.getMeetUpDetail(MEETID)
        }
    }

    private fun setObservers() {
        addObserver()
        meetUpViewModel.observeOpenMeetJoinRequest().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
//                    adapter?.setData(it.value?: GetJoinRequestModel())
//                    joinRequestList.clear()
//                    joinRequestList.addAll(it.value?: GetJoinRequestModel())
//                    adapter?.submitList(it.value?: GetJoinRequestModel())
                    adapter1?.submitList(it?.value)
                    binding.recycler.adapter = adapter1

                }

                is ResultHandler.Failure -> {

                }
            }
        })
//        meetUpViewModel.observeMeetUpDetail().observe(viewLifecycleOwner,{
//            when(it){
//                is ResultHandler.Success -> {
//                    // adapter?.setData(it.value?: GetJoinRequestModel())
//                    Log.d("TAG", "addObserver: ")
//                    it.value?.let { it1 ->
//                        popView(it1)
//                    }?:run{
//                        showToast("Something went wrong!!!")
//                    }
//                }
//                is ResultHandler.Failure ->{
//                    showToast("Something went wrong!!!")
//                    Log.e("TAG","  OpenMeetJoinRequest Failed :: ")
//                }
//            }
//        })
    }

    private fun popView(it1: GetMeetUpResponseItem?) {
        Log.d(TAG, "iamHere: geting popView: calld")
        adapter2?.submitList(it1?.participants, it1?.attendees, it1?.date, false)
        binding.recycler.adapter = adapter2
        participants.clear()
        participants.addAll(it1?.participants?: ArrayList())
        attendees.clear()
        attendees.addAll(it1?.attendees ?: ArrayList())
    }


    private fun initView() {
//        response=arguments?.getParcelable("meetResponse")
        status = arguments?.getString("status")
        if(status == Constant.RequestType.PENDING.value) {
            binding.noData.noDataHeading.text = "Requested to Join"
            binding.noData.noDataDesc.text = "People who have requested\nto join will show here"
        } else {
            binding.noData.noDataHeading.text = "Accepted to Join"
            binding.noData.noDataDesc.text = "People you have accepted\nto join will show here"
        }
//        if(response.join_requests?.requests?.isEmpty()==true){
//        }
        binding.recycler.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        adapter1 = OpenMeetInterestAdapter(requireActivity(), {
            if(!it) {
                binding.noData.noDataRoot.visibility = View.GONE
            }
        }, {
            openProfile(it, Constant.Source.MEETUPCHAT.sorce.plus(MEETID))
        })
        adapter2 = OpenMeetParticipantsAdapter(requireActivity(),false, {
            openProfile(it, Constant.Source.MEETUPCHAT.sorce.plus(MEETID))
        }, {
            if(it==null) {
                binding.noData.noDataRoot.visibility = View.GONE
            }else if(it=="empty"){
                binding.noData.noDataRoot.visibility = View.VISIBLE
            }else if(it=="iam_here"){
                if(Utils.checkPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    (activity as MainActivity?)?.enableLocationStuff(654, 655) {
                        iamHere(MEETID)

                    }
                } else {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 655)
                }
            }
        })

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i("TAG", " onRequestPermissionsResult::: $requestCode  ")
        when(requestCode) {
            655 -> {
                val location = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if(grantResults.isNotEmpty() && location) {
                    (activity as MainActivity?)?.enableLocationStuff(654, 655) {
                        iamHere(MEETID)

                    }
                } else if(Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    rationale()
                } else {
                }
            }


        }
    }

    override fun onBackPageCome() {
        Log.i(FragmentOpenMeetRequestList.TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }
}