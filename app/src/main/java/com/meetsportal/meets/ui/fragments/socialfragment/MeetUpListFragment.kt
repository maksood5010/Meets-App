package com.meetsportal.meets.ui.fragments.socialfragment

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.MeetUpListAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetupListBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.meetup.GetMeetUpResponse
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.meetup.MeetScope
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.ui.dialog.ProceedDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MeetUpListFragment : BaseMeetFragment() {

    override val TAG = MeetUpListFragment::class.simpleName!!

    lateinit var adapter: MeetUpListAdapter
    //lateinit var addNameAlert: AddNameAlert

    var page = 0
    var type_ = 0
    var filter = -1
    override val meetUpViewModel: MeetUpViewModel by viewModels()
    override fun populateView(value: GetMeetUpResponseItem?) {
        //TODO("Not yet implemented")
    }

    private var _binding: FragmentMeetupListBinding? = null
    private val binding get() = _binding!!

    companion object {

        val PAGE = "page"
        val TYPE = "type"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_upcoming, container, false)
        _binding = FragmentMeetupListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()
        setObserver()
    }


    private fun initView(view: View) {
        arguments?.let {
            page = it.getInt(PAGE)
            type_ = it.getInt(TYPE)
        }
        binding.tvCreateOne.onClick({
            MyApplication.putTrackMP(Constant.AcMeetUpCreateOne, null)
            requireActivity().onBackPressed()
            (activity as MainActivity?)?.meetOption?.showDialog()
        })
        binding.rvMeetUp.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        adapter = MeetUpListAdapter(requireActivity(), GetMeetUpResponse(), type_, this, { option, meetUpdata ->
            Log.i(TAG, " MeetUpListAdapter:: BottomOption:: ${option}")
            when(option) {
                BottomSheetOptions.BUTTON1 -> changeName(meetUpdata)
                BottomSheetOptions.BUTTON2 -> addToCalendar(meetUpdata)
                //BottomSheetOptions.BUTTON3 -> addToCalendar(meetUpdata)
                BottomSheetOptions.BUTTON4 -> addfriend(meetUpdata, TAG)

                BottomSheetOptions.BUTTON5 -> {
                }

                BottomSheetOptions.BUTTON6 -> {
                    val proceedDialog = ProceedDialog(requireActivity()) { b: Boolean ->
                        if(b) {
                            optOut(meetUpdata)
                        }
                    }
                    proceedDialog.setMessage("Opt out", "Are you sure you want to opt out?")
                }

                BottomSheetOptions.BUTTON7 -> {
                    val showProceed = showProceed {
                        cancleMeetUp(meetUpdata)
                    }
                    showProceed.setMessage("Cancel Meetup", "Are you sure you want to cancel this Meetup?")
                }

                BottomSheetOptions.BUTTON8 -> {
                }

                "delete"                   -> {
                    val proceedDialog = ProceedDialog(requireActivity()) { b: Boolean ->
                        if(b) {
                            if(meetUpdata?.user_id.equals(MyApplication.SID)) {
                                deleteMeetUp(meetUpdata?._id, MeetScope.PREVIOUS.scope)
                            } else {
                                optOut(meetUpdata)
                            }
                        }
                    }
                    if(meetUpdata?.user_id.equals(MyApplication.SID)) {
                        proceedDialog.setMessage("Delete", "Are you sure you want to delete?")
                    } else {
                        proceedDialog.setMessage("Opt out", "Are you sure you want to Opt out?")
                    }
                }

            }
        }, {
            if(type_ == 0) {
                var baseFragment: BaseFragment = ParticipantInviteeFragment.getInstance(it)
                Navigation.addFragment(requireActivity(), baseFragment, ParticipantInviteeFragment::class.simpleName!!, R.id.homeFram, true, true)
            } else {
                var fragment = MeetAttendeeList.getInstance(it, Constant.EnumMeetPerson.ATTENDEE)
                Navigation.addFragment(requireActivity(), fragment, MeetAttendeeList.TAG, R.id.homeFram, true, false)
            }
        })
        adapter.addLoadStateListener { loadState ->
            if(loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && adapter.itemCount < 1) {
                binding.rvMeetUp.isVisible = false
                binding.rlNoMeetUp.isVisible = true
            } else {
                binding.rvMeetUp.isVisible = true
                binding.rlNoMeetUp.isVisible = false
            }
        }
        binding.rvMeetUp.adapter = adapter/*.withLoadStateFooter(TimelineFooterStateAdapter{
            adapter.retry()
        })*/
        //binding.rvMeetUp.adapter = TempRecyclerViewAdapter(requireContext(),R.layout.card_meetup)

        getMeetUpData()
    }

    fun getMeetUpData() {
        binding.rlNoMeetUp.visibility = View.VISIBLE
        var filterStr: String? = null
        when(filter) {
            -1 -> {
                filterStr = null
            }

            0  -> {
                //For all
                filterStr = null
            }

            1  -> {
                filterStr = "voting_closed:true"
            }

            2  -> {
                filterStr = "voting_closed:false"
            }

            3  -> {
                filterStr = "status:cancelled"
            }
        }
        val range = if(type_ == 0) {
            MeetScope.UPCOMING.scope
        } else {
            MeetScope.PREVIOUS.scope
        }
        if(page == 0) meetUpViewModel.getMeetUpListdataSource("user_created", range, filterStr)
        else if(page == 1) meetUpViewModel.getMeetUpListdataSource("user_invited", range, filterStr)
    }

    private fun setUp() {

    }

    /*private fun changeName(meetUpdata: GetMeetUpResponseItem?) {
        Log.i(TAG, " changename:: ${meetUpdata}")
        addNameAlert = AddNameAlert(requireActivity()) {
            if(meetUpdata?.name?.equals(it.trim()) == false && it.trim()
                        .isNotEmpty()) meetUpViewModel.changeMeetName(it, meetUpdata._id)
        }
        Log.i(TAG, " checkingMeetname:: ${meetUpdata?.name}")
        addNameAlert.showDialog(meetUpdata?.name)
    }*/

    /*private fun addToCalendar(meetUpdata: GetMeetUpResponseItem?) {
        if(Utils.checkPermission(activity, android.Manifest.permission.WRITE_CALENDAR)) {
            try {
                pushAppointmentsToCalender(meetUpdata)?.let {
                    MyApplication.showToast(requireActivity(), " Added to Calendar...")
                }
            } catch(e: Exception) {
                FirebaseCrashlytics.getInstance().log("exception while adding to calendar ")
                FirebaseCrashlytics.getInstance().recordException(RuntimeException(e))
                MyApplication.showToast(requireActivity(), " Something went Wrong...")
            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_CALENDAR), 1255)
        }
    }*/

    /*private fun addfriend(meetUpdata: GetMeetUpResponseItem?) {
        var fragment = AddFriendToMeetUp.getInstance(meetUpdata)
        Navigation.addFragment(requireActivity(), fragment, AddFriendToMeetUp::class.simpleName!!, R.id.homeFram, true, true)
    }*/

    /*private fun optOut(meetUpdata: GetMeetUpResponseItem?, isUpcoming: Boolean) {
        meetUpViewModel.optOut(meetUpdata?._id)
    }*/

    /*private fun cancleMeetUp(meetUpdata: GetMeetUpResponseItem?) {
        meetUpViewModel.cancleMeetUp(meetUpdata?._id)
    }*/


    private fun setObserver() {
        meetUpViewModel.observeMeetPageDataSource().observe(viewLifecycleOwner, {
            adapter.submitData(lifecycle, it)
        })
        meetUpViewModel.observeNameChange().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "Name changed!!")
//                    ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 1) as MeetUpNew).getMeetUps()
                    adapter.refresh()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })

        meetUpViewModel.observeOptOut().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "Opt Out from Meetup!!")
//                    ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 1) as MeetUpNew).getMeetUps()
                    adapter.refresh()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })

        meetUpViewModel.observeCancelMeetUp().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "MeetUp Cancelled!!")
//                    ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 1) as MeetUpNew).getMeetUps()
                    adapter.refresh()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })

        meetUpViewModel.observeDeleteMeetUp().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "MeetUp Cancelled!!")
//                    ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 1) as MeetUpNew).getMeetUps()
                    adapter.refresh()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })


    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, " onRequestPermissionsResult::: $requestCode  ")
        when(requestCode) {
            1255 -> {
                Log.i(TAG, " loging::: 1")
                var write = grantResults[0] == PackageManager.PERMISSION_GRANTED

                Log.i(TAG, " loging::: 4")
                if(grantResults.isNotEmpty() && write) {
                    //selectImage()

                    Log.i(TAG, " Permission Granted")
                } else if(Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_CALENDAR)) {
                    Log.i(TAG, " loging::: 5")
                    rationale()
                }
            }


        }
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }
}