package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.adapter.OpenMeetUpListAdapter
import com.meetsportal.meets.adapter.TimelineFooterStateAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentOpenMeetupListBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.meetup.GetMeetUpResponse
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.meetup.MeetScope
import com.meetsportal.meets.overridelayout.SpaceItemDecoration
import com.meetsportal.meets.ui.activities.HomeActivityNew
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.ui.dialog.ProceedDialog
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OpenMeetUpListFragment:BaseMeetFragment() {

    var page = 0
    var filter = -1
    lateinit var adapter: OpenMeetUpListAdapter

    override val meetUpViewModel:MeetUpViewModel by viewModels()
    private var _binding: FragmentOpenMeetupListBinding? = null
    private val binding get() = _binding!!

    companion object{
        val TAG = OpenMeetUpListFragment::class.simpleName
        val PAGE = "pageNumber"
        fun getInstance(page: Int):OpenMeetUpListFragment{
            return OpenMeetUpListFragment().apply {
                arguments = bundleOf(PAGE to page)
            }
        }
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_upcoming, container, false)
        _binding = FragmentOpenMeetupListBinding.inflate(inflater, container, false)
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
        }
        binding.tvCreateOne.onClick({
            requireActivity().onBackPressed()
            (activity as MainActivity?)?.meetOption?.showDialog()
        })
        binding.rvMeetUp.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMeetUp.addItemDecoration(
            SpaceItemDecoration(Utils.dpToPx(20f,resources),0)
        )

        adapter = OpenMeetUpListAdapter(requireActivity(), GetMeetUpResponse(),this){ option, meetUpdata ->
            when(option) {
                BottomSheetOptions.BUTTON1 -> optOut(meetUpdata)
                BottomSheetOptions.BUTTON2 -> closeMeetUp(meetUpdata?._id)
                BottomSheetOptions.BUTTON3 -> {
                    val showProceed = showProceed {
                        cancleMeetUp(meetUpdata)
                    }
                    showProceed.setMessage("Cancel Meetup","Are you sure you want to cancel this Meetup?")
                }
                BottomSheetOptions.BUTTON4 -> {
                    val proceedDialog = ProceedDialog(requireActivity()) { b: Boolean ->
                        if(b) {
                            if(meetUpdata?.user_id.equals(MyApplication.SID)){
                                deleteMeetUp(meetUpdata?._id, MeetScope.UPCOMING.scope)
                            }else{
                                optOut(meetUpdata)
                            }
                        }
                    }
                    if(meetUpdata?.user_id.equals(MyApplication.SID)){
                        proceedDialog.setMessage("Delete","Are you sure you want to delete?")
                    }else{
                        proceedDialog.setMessage("Opt out","Are you sure you want to Opt out?")
                    }
                }
            }
        }
        adapter.addLoadStateListener { loadState ->
            if(loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && adapter.itemCount < 1) {
                binding.rvMeetUp.isVisible = false
                binding.rlNoMeetUp.isVisible = true
            } else {
                binding.rvMeetUp.isVisible = true
                binding.rlNoMeetUp.isVisible = false
            }
        }
        binding.rvMeetUp.adapter = adapter.withLoadStateFooter(TimelineFooterStateAdapter{
            adapter.retry()
        })

    }

    override fun populateView(value: GetMeetUpResponseItem?) {
        //refresh()
    }


    private fun setUp() {
        getOpenMeet()
    }
    fun getOpenMeet(){
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
                filterStr = "attendees:".plus(MyApplication.SID)
            }
            3  -> {
                filterStr = "status:cancelled"
            }
        }
        Log.i(TAG," checkingPageNumber:: $page")
        if(page == 0) meetUpViewModel.getMeetUpListdataSource("user_created", MeetScope.UPCOMING.scope, filterStr,
            Constant.MeetType.OPEN.type)
        else if(page == 1) meetUpViewModel.getMeetUpListdataSource("user_invited", MeetScope.UPCOMING.scope, filterStr,Constant.MeetType.OPEN.type)
    }

    private fun setObserver() {
        meetUpViewModel.observeMeetPageDataSource().observe(viewLifecycleOwner, {
            adapter.submitData(lifecycle, it)
        })
        meetUpViewModel.observeOptOut().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(),"Opt Out from Meetup!!")
//                    ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 1) as MeetUpNew).getOpenMeet()
                    refresh()
                }
                is ResultHandler.Failure ->{
                    MyApplication.showToast(requireActivity(),"Something went wrong!!!")
                }
            }
        })
        meetUpViewModel.observeCloseOpenMeet().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success->{
                    MyApplication.showToast(requireActivity(),"Open meetup closed!!!")
//                    ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 1) as MeetUpNew).getOpenMeet()
                    refresh()
                }
                is ResultHandler.Failure -> {
                    Log.e(TAG," Close open Meet Fail:: ${it.throwable?.printStackTrace()}")
                }
            }
        })
        meetUpViewModel.observeCancelMeetUp().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "MeetUp Cancelled!!")
//                    ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 1) as MeetUpNew).getOpenMeet()
                    refresh()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })
        meetUpViewModel.observeDeleteMeetUp().observe(viewLifecycleOwner,{
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "MeetUp Deleted!!")
//                    ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 1) as MeetUpNew).getOpenMeet()
                    refresh()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })
    }
    fun refresh(){
        adapter.refresh()
    }
    override fun onBackPageCome() {
    }
}