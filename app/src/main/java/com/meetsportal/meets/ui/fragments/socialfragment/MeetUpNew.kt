package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.MeetNewOpenMeetAdapter
import com.meetsportal.meets.adapter.MeetPreviousAdapter
import com.meetsportal.meets.adapter.SwipeMeetUpCardAdapter
import com.meetsportal.meets.adapter.TempRecyclerViewAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetUpBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.meetup.GetMeetUpResponse
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.meetup.MeetScope
import com.meetsportal.meets.overridelayout.SpaceItemDecoration
import com.meetsportal.meets.ui.activities.HomeActivityNew
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.ui.bottomsheet.CreateMeetUpBottomSheeet
import com.meetsportal.meets.ui.dialog.ProceedDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.TAG_OPEN_FOR_MEETUP_MAP
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.yuyakaido.android.cardstackview.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeetUpNew : BaseMeetFragment(), CardStackListener, View.OnClickListener {

    var rootCo: ViewGroup? = null
    private var selectedMeetId: String? = null
    override var TAG = MeetUpNew::class.java.simpleName

    lateinit var layoutManager: LinearLayoutManager
    lateinit var upcomingAdapter: SwipeMeetUpCardAdapter
    lateinit var previousAdapter: MeetPreviousAdapter
    lateinit var openAdapter: MeetNewOpenMeetAdapter
    var previousMeetList = GetMeetUpResponse()
    var openMeetList = GetMeetUpResponse()
    // lateinit var addNameAlert : AddNameAlert

    override val meetUpViewModel: MeetUpViewModel by viewModels()
    override fun populateView(value: GetMeetUpResponseItem?) {
        Log.d(TAG, "populateView: MeetUpNew")
    }

    private var _binding: FragmentMeetUpBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_meet_up, container, false)
        _binding = FragmentMeetUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setupUI(binding.root)>
        initView(view)
        setUp()
        setObserver()
    }


    private fun initView(view: View) {
        rootCo = binding.rootCo
        binding.llNoMeetup.visibility = View.VISIBLE
        binding.llNoOpenMeet.visibility = View.VISIBLE
        binding.llNoPrevMeet.visibility = View.VISIBLE
        binding.rvUpcomingPmeet.visibility = View.INVISIBLE
        binding.rvOpen.visibility = View.INVISIBLE
        binding.tvUpcomingCount.setOnClickListener(this)
        binding.llUpcoming.setOnClickListener(this)
        binding.tvSeeThem.setOnClickListener(this)
        binding.allOpenForMeetUp.setOnClickListener(this)
        binding.seeAll.setRoundedColorBackground(requireActivity(), R.color.white, enbleDash = true, strokeHeight = 1f, Dashgap = 0f, stripSize = 0f, strokeColor = R.color.primaryDark)
        binding.seeAllOpenMeet.setRoundedColorBackground(requireActivity(), R.color.white, enbleDash = true, strokeHeight = 1f, Dashgap = 0f, stripSize = 0f, strokeColor = R.color.primaryDark)
        binding.seeAllPrevious.setRoundedColorBackground(requireActivity(), R.color.white, enbleDash = true, strokeHeight = 1f, Dashgap = 0f, stripSize = 0f, strokeColor = R.color.primaryDark)
        //binding.reload.setRoundedColorBackground(requireActivity(), R.color.transparent, enbleDash = true, strokeHeight = 2f, Dashgap = 0f, stripSize = 0f, strokeColor = R.color.white)
        binding.rvOpen.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvOpen.addItemDecoration(SpaceItemDecoration(0, Utils.dpToPx(20f, resources)))
        binding.rvOpen.adapter = TempRecyclerViewAdapter(requireActivity(), R.layout.card_core_open_meet)
        binding.refresh.setOnRefreshListener {
            refresh()
            binding.refresh.isRefreshing = false
        }
        initstack()
    }
    fun clickToolBar(){

        binding.toolbar.ivCreateMeetUp.visibility=View.VISIBLE
        binding.toolbar.ivCreateMeetUp.onClick({
            getMain()?.meetOption?.showDialog()
        })
        binding.toolbar.rlChat.visibility=View.GONE
        binding.toolbar.rlNotification.visibility=View.GONE
    }
    private fun setUp() {
        clickToolBar()
        refresh()
        binding.rvPrevious.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        previousAdapter = MeetPreviousAdapter(requireActivity(), this, previousMeetList)
        openAdapter = MeetNewOpenMeetAdapter(requireActivity(), this, openMeetList) {
            binding.toolOpen.root.visibility = View.VISIBLE
            binding.toolOpen.desc.text = "View all the people who have requested to join this meet up"
            binding.toolOpen.root.setOnClickListener {
                binding.toolOpen.root.visibility = View.GONE
            }
        }
        binding.rvPrevious.adapter = previousAdapter
        binding.rvOpen.adapter = openAdapter
        //if(layoutManager.itemCount == 0) binding.reload.visibility = View.VISIBLE else binding.reload.visibility = View.GONE
        Utils.onClick(binding.tvCreateOne, 1000) {
            var fragment = CreateMeetUpBottomSheeet(requireActivity())
            if(!fragment.isAdded) {
                Log.i(TAG, " tv_createOne:: ")
                fragment.show(activity?.supportFragmentManager!!, fragment.tag)
            }
        }

        Utils.onClick(binding.allOpenForMeetUp, 1000) {
            Navigation.addFragment(requireActivity(), MeetupOpenMemberMapFragment(), TAG_OPEN_FOR_MEETUP_MAP, R.id.homeFram, true, false)
        }
        /*binding.reload.setOnClickListener {
            binding.rvUpcomingPmeet.adapter?.notifyDataSetChanged()
            getMeetUps()
            binding.reload.visibility = View.GONE
        }*/
    }

    fun refresh() {
        getMeetUps()
        getPreViousMeetUp()
        getOpenMeet()
    }

    fun getMeetUps() {
        Log.i(TAG, " getMeetUps::  ")
        meetUpViewModel.fetchMeetUP(null, MeetScope.UPCOMING.scope)
    }

    fun getPreViousMeetUp() {
        meetUpViewModel.fetchPreViousMeetUp(null, MeetScope.PREVIOUS.scope)
    }

    fun getOpenMeet() {
        meetUpViewModel.fetchOpenMeetUp(null, MeetScope.UPCOMING.scope)
    }

    fun refreshThispage() {
        Log.d(TAG, "refreshThispage: from home")
        getMeetUps()
        getPreViousMeetUp()
        getOpenMeet()
    }

    private fun setObserver() {
        meetUpViewModel.observeMeetUpResponse().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    Log.i(TAG, " Success:: it.value?.size::${it.value?.size}")
                    //binding.reload.visibility = View.GONE
                    upcomingAdapter.setData(it.value)
                    if(it.value?.isEmpty() == true) {
                        binding.llNoMeetup.visibility = View.VISIBLE
                        binding.rvUpcomingPmeet.visibility = View.INVISIBLE
                    } else {
                        binding.llNoMeetup.visibility = View.GONE
                        binding.rvUpcomingPmeet.visibility = View.VISIBLE
                    }
//                    binding.seeAll.text = "View all ".plus(it.value?.size).plus(" Meetups")
                    if(it.value?.isNullOrEmpty() == true) {
                        binding.seeAll.visibility = View.GONE
                    } else {
                        binding.seeAll.visibility = View.VISIBLE
                    }
                    binding.seeAll.onClick({
                        MyApplication.putTrackMP(Constant.AcPMViewAll, null)
                        val meetUpUpComing = MeetUpUpComing().apply { arguments = bundleOf(MeetUpListFragment.TYPE to 0) }
                        Navigation.addFragment(requireActivity(), meetUpUpComing, MeetUpUpComing::class.simpleName!!, R.id.homeFram, true, true)
                    })
                }

                is ResultHandler.Failure -> {
                    Log.e(TAG, " Something Went wrong:: ${it.throwable?.printStackTrace()}")
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        meetUpViewModel.observepreviousMeetUp().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    Log.i(TAG, " previousMeetUp:: ${it.value?.size}")
                    it.value?.let {
                        if(it.isEmpty()) {
                            binding.llNoPrevMeet.visibility = View.VISIBLE
                            binding.rvPrevious.visibility = View.INVISIBLE
                        } else {
                            binding.llNoPrevMeet.visibility = View.GONE
                            binding.rvPrevious.visibility = View.VISIBLE
                        }
                        previousAdapter.setData(it)
//                        binding.seeAllPrevious.text = "View all ".plus(it.size).plus(" Meetups")
                        if(it.isNullOrEmpty()) {
                            binding.seeAllPrevious.visibility = View.INVISIBLE
                        } else {
                            binding.seeAllPrevious.visibility = View.VISIBLE
                        }
                        binding.seeAllPrevious.onClick({
                            MyApplication.putTrackMP(Constant.AcPreviousMeetViewAll, null)
                            val meetUpUpComing = MeetUpUpComing().apply { arguments = bundleOf(MeetUpListFragment.TYPE to 1) }
                            Navigation.addFragment(requireActivity(), meetUpUpComing, MeetUpUpComing::class.simpleName!!, R.id.homeFram, true, true)
                        })
                    }

                }

                is ResultHandler.Failure -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        meetUpViewModel.observeOpenMeetUp().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    Log.i(TAG, " previousMeetUp:: ${it.value?.size}")
                    it.value?.let {
//                        binding.seeAllOpenMeet.text = "View all ".plus(it.size).plus(" Open Meetups")
                        if(it?.isNullOrEmpty()) {
                            binding.seeAllOpenMeet.visibility = View.INVISIBLE
                        } else {
                            binding.seeAllOpenMeet.visibility = View.VISIBLE
                        }
                        binding.seeAllOpenMeet.onClick({
                            MyApplication.putTrackMP(Constant.AcOMViewAll, null)
                            Navigation.addFragment(requireActivity(), OpenMeetUpcoming(), OpenMeetUpcoming.TAG, R.id.homeFram, true, true)
                        })
                        if(it.isEmpty() == true) {
                            binding.llNoOpenMeet.visibility = View.VISIBLE
                            binding.rvOpen.visibility = View.INVISIBLE
                        } else {
                            binding.llNoOpenMeet.visibility = View.GONE
                            binding.rvOpen.visibility = View.VISIBLE
                        }
                        openAdapter.setData(it)
                    }
                }

                is ResultHandler.Failure -> {
                    Log.i(TAG, " OpenMeetGivesError:: ${it.throwable?.message}")
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        meetUpViewModel.observeNameChange().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "Name changed!!")
                    getMeetUps()
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
                    getMeetUps()
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
                    getMeetUps()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })
        meetUpViewModel.observeDeleteMeetUp().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "Meetup Deleted")
                    getMeetUps()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })


    }


    private fun initstack() {
        val rewindSetting = RewindAnimationSetting.Builder().setDirection(Direction.Top)
                .setDuration(200).setInterpolator(DecelerateInterpolator()).build()

        layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        upcomingAdapter = SwipeMeetUpCardAdapter(requireActivity(), this, GetMeetUpResponse()) { option, meetUpdata ->
            Log.i(TAG, " checking ${option}")
            selectedMeetId = meetUpdata?._id
            when(option) {
                "Change Name"     -> changeName(meetUpdata)
                "Add to calendar" -> addToCalendar(meetUpdata)

                "I’m here"        -> {
                    if(Utils.checkPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                        (activity as MainActivity?)?.enableLocationStuff(654, 655) {
                            iamHere(selectedMeetId)
                        }
                    } else {
                        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 655)
                    }
                }

                "Add friend"      -> addfriend(meetUpdata, TAG)

                "Opt Out"         -> {
                    optOut(meetUpdata)
                }

                "Cancel Meetup"   -> {
                    val showProceed = showProceed {
                        cancleMeetUp(meetUpdata)
                    }
                    showProceed.setMessage("Cancel Meetup", "Are you sure you want to cancel this Meetup?")
                }

                "delete"          -> {
                    val proceedDialog = ProceedDialog(requireActivity()) { b: Boolean ->
                        if(b) {
                            if(meetUpdata?.user_id.equals(MyApplication.SID)) {
                                deleteMeetUp(selectedMeetId, MeetScope.UPCOMING.scope)
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
        }
        //layoutManager.setDirections(arrayListOf(Direction.Top, Direction.Bottom))
        /*layoutManager.setDirections(arrayListOf(Direction.Top))
        layoutManager.setCanScrollHorizontal(false)
        layoutManager.setCanScrollVertical(true)
        layoutManager.setVisibleCount(3)
        layoutManager.setStackFrom(StackFrom.Bottom)
        layoutManager.setTranslationInterval(8.0f)
        layoutManager.setRewindAnimationSetting(rewindSetting)

        layoutManager.setMaxDegree(90f)*/
        binding.rvUpcomingPmeet.layoutManager = layoutManager
        binding.rvUpcomingPmeet.adapter = upcomingAdapter

    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {
        Log.i(TAG, " StackViewis::: onCardDragging:: $ratio")

    }

    override fun onCardSwiped(direction: Direction?) {
        Log.i(TAG, "  StackViewis::: cards::: onCardSwiped:: ")
    }


    override fun onCardRewound() {
        Log.i(TAG, " StackViewis::: cards::: onCardRewound:: ${layoutManager}")
    }

    override fun onCardCanceled() {
        Log.i(TAG, " StackViewis::: cards::: onCardCanceled:: ")
    }

    override fun onCardAppeared(view: View?, position: Int) {
        Log.i(TAG, " StackViewis::: onCardAppeared $position")

    }

    override fun onCardDisappeared(view: View?, position: Int) {
        //Log.i(TAG, " cards::: onCardDisappeared:: ${layoutManager.topPosition}  ${layoutManager.itemCount} $position")
        //if(layoutManager.topPosition == (layoutManager.itemCount - 1)) {
            //binding.reload.visibility = View.VISIBLE
        //}
    }


    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, " onRequestPermissionsResult::: $requestCode  ")
        when(requestCode) {
            655 -> {
                var location = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if(grantResults.isNotEmpty() && location) {
                    (activity as MainActivity?)?.enableLocationStuff(654, 655) {
                        iamHere(selectedMeetId)
                    }
                } else if(Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    rationale()
                } else {
                }
            }


        }
    }

    override fun onClick(v: View?) {
        //
    }
}