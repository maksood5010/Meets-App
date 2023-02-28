package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.ListenerRegistration
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.NotificationPagingAdapter
import com.meetsportal.meets.adapter.ViewsProfileAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.firebase.FireBaseUtils
import com.meetsportal.meets.repository.AppRepository
import com.meetsportal.meets.ui.dialog.MeetUpInvitationAlert
import com.meetsportal.meets.ui.dialog.ProceedDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.AcProfileVisitNoti
import com.meetsportal.meets.utils.Constant.VwNotiViewed
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.viewmodels.FireBaseViewModal
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

@AndroidEntryPoint
class NotificationFragment : BaseFragment() {

    private var profileVisits: Int= 0
    val TAG = NotificationFragment::class.simpleName

    val fireViewModel: FireBaseViewModal by viewModels()
    val meetUpViewModel: MeetUpViewModel by viewModels()
    val userviewModel: UserAccountViewModel by viewModels()


    var notificationListener: ListenerRegistration? = null
    lateinit var layoutManager: LinearLayoutManager
    lateinit var rvNotifications: RecyclerView
    lateinit var adapter: NotificationPagingAdapter
    val defaulterNotification = ArrayList<String?>()
    lateinit var adapter2: ViewsProfileAdapter
    lateinit var refresh: SwipeRefreshLayout
    lateinit var noDataLayout: ConstraintLayout
    lateinit var root : ConstraintLayout

    // lateinit var switcher:ViewSwitcher
    lateinit var shimmer: ShimmerFrameLayout

    var meetUpInvitation: MeetUpInvitationAlert? = null

    @Inject
    lateinit var repository: AppRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MyApplication.putTrackMP(VwNotiViewed,null)
        initView(view)
        setUp()
        setObserver()

    }

    private fun initView(view: View) {
        view.findViewById<ImageView>(R.id.back).setOnClickListener { activity?.onBackPressed() }
        rvNotifications = view.findViewById(R.id.rv_notification)
        root = view.findViewById(R.id.mainRoot)
        view.findViewById<TextView>(R.id.no_data_heading).text="No Notifications yet"
        noDataLayout = view.findViewById<ConstraintLayout>(R.id.no_data_root)
                .apply {
                    visibility = View.GONE }
        shimmer = view.findViewById(R.id.shimmer)
        //switcher = view.findViewById(R.id.switcher)
        refresh = view.findViewById<SwipeRefreshLayout>(R.id.refresh).apply {
            setOnRefreshListener {
                Log.i("TAG", " refreshing:: ")
                refreshNotification()
                refresh.isRefreshing = false
            }
        }
        layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvNotifications.layoutManager = layoutManager
    }

    private fun setUp() {
        userviewModel.getNewVisits()
        adapter2=ViewsProfileAdapter(requireActivity(),profileVisits,root){
            MyApplication.putTrackMP(AcProfileVisitNoti,null)
            Navigation.addFragment(requireActivity(), ProfileViewFragment(), ProfileViewFragment.TAG, R.id.homeFram, true, false)
        }
        adapter = NotificationPagingAdapter(requireActivity()) { meetUpId, invitationId, type, postId,inviteeName ->
            Log.i(TAG, " ShowingInvitaionPopUp::")
            Utils.cancelNotification(requireContext(),invitationId)
            meetUpInvitation?.showDialog(invitationId, meetUpId,inviteeName)
            meetUpViewModel.getMeetUpDetail(meetUpId)
            if(type != null && type == "open") {
                meetUpInvitation?.setOpenMeet(postId)
            }
        }
        meetUpInvitation = MeetUpInvitationAlert(requireActivity()) { join ->
            if(join.accept == true) {
                meetUpViewModel.acceptInvitaion(join)
            } else {
                val proceedDialog = ProceedDialog(requireActivity()) { b: Boolean ->
                    if(b) {
                        meetUpViewModel.acceptInvitaion(join)
                    }
                }
                proceedDialog.setMessage("Reject", "Are you sure you want to reject this invitation?")
            }
        }
        val concat=ConcatAdapter(adapter2,adapter)
        rvNotifications.adapter = concat
        fireViewModel.getNotification(defaulterNotification)
        notificationListener?.remove()
        notificationListener = FireBaseUtils.getNotificationListener(){
            adapter.refresh()
        }


        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.filter {
                Log.i(TAG, "setUp: NotificationPagingAdapter 6 $it ")
                (it.refresh is LoadState.NotLoading ||it.refresh is LoadState.Error)
            }.collect {
                Log.d(TAG, "setUp: NotificationPagingAdapter 1 :: ${adapter.itemCount} , ${it.append is LoadState.NotLoading}, ${it.append.endOfPaginationReached}, ${adapter.itemCount < 2}")
                Log.d(TAG, "setUp: NotificationPagingAdapter it value::: :: $it")
                if(it.append is LoadState.NotLoading && it.append.endOfPaginationReached && adapter.itemCount < 2) {
                    Log.d(TAG, "setUp: NotificationPagingAdapter 2 :: ${adapter.itemCount}")
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                    //rvNotifications.visibility = View.GONE
                    noDataLayout.visibility = View.VISIBLE
                    return@collect
                } else if(it.append is LoadState.NotLoading && it.append.endOfPaginationReached && adapter.itemCount >= 1) {
                    Log.d(TAG, "setUp: NotificationPagingAdapter 3 :: ${adapter.itemCount}")
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                    noDataLayout.visibility = View.GONE
                    return@collect
                }else if(it.refresh is LoadState.Error){
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                    noDataLayout.visibility = View.VISIBLE
                    return@collect
                }
                if(adapter.itemCount >= 1){
                    Log.d(TAG, "setUp: NotificationPagingAdapter 4 :: ${adapter.itemCount}")
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                    noDataLayout.visibility = View.GONE
                }/*else if(adapter.itemCount <= 1){
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                    //rvNotifications.visibility = View.GONE
                    noDataLayout.visibility = View.VISIBLE
                }*/
                Log.i(TAG, " distinctUntilChangedBy:::  2 --  ${adapter.itemCount}")
            }
        }


        /*adapter.addLoadStateListener { combineState ->
            Log.i(TAG," CombinedListener::: $combineState")
           *//* if(combineState.refresh is LoadState.Error){
                if((combineState.refresh as LoadState.Error).error is EmptyListException){
                    shimmer.stopShimmer()
                    shimmer.visibility = View.GONE
                    rvNotifications.visibility = View.GONE
                    noDataLayout.visibility = View.VISIBLE
                }

            }*//*
            *//*if(combineState.refresh is LoadState.NotLoading && adapter.itemCount == 0){
                Log.i(TAG," checking:: ")
                //shimmer.stopShimmer()
                //shimmer.visibility = View.GONE
                rvNotifications.visibility = View.GONE
                //noDataLayout.visibility = View.VISIBLE
            }*//**//*else if(combineState.source.refresh is LoadState.NotLoading || adapter.itemCount > 1 ){
                Log.i(TAG, " dataCeme:: ")
            }*//*

        }*/
    }

    fun refreshNotification() {
        // switcher.showNext()
        userviewModel.getNewVisits()
        adapter.refresh()
    }

    private fun setObserver() {
        fireViewModel.observeNotificationPagingDataSource().observe(viewLifecycleOwner, Observer {

            Log.d(TAG, "submitData: setUp: NotificationPagingAdapter")
            adapter.submitData(lifecycle, it)
        })

        userviewModel.observeNewVisits().observe(viewLifecycleOwner,{
            when(it) {
                is ResultHandler.Success -> {
                    profileVisits = it.value?.get("count")?.asInt ?:0
                    adapter2.setCount(profileVisits)
                }
                is ResultHandler.Failure -> {
//                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })
        meetUpViewModel.observeMeetUpDetail().observe(viewLifecycleOwner, Observer {
            Log.i(TAG, " meetDetailCame::: ")
            when(it) {
                is ResultHandler.Success -> {
                    Log.i(TAG, " meetDetail::: ${it.value}")
                    meetUpInvitation?.showData(it.value)
                }

                is ResultHandler.Failure -> {
                    Log.i(TAG, "setObserver: ${it.message} -- ${it.code} -- ${it.throwable}")
                    if(it.code == "404"){
                        MyApplication.showToast(requireActivity(), "Meetup is deleted!!!")
                    }
                    else{
                        MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                    }
                    meetUpInvitation?.hideDialog()

                }
            }
        })
        meetUpViewModel.observeMeetUpInvitaion().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    Log.i(TAG, " Invitation Accepted::: ${it.value}")
                    meetUpInvitation?.hideDialog()
                    defaulterNotification.add(it.value.invitationId)
                    Utils.cancelNotification(requireActivity(),it.value.invitationId)
                    adapter.refresh()
                    if(it.value.accept == true) {
                        MyApplication.showToast(requireActivity(), "Invitation Accepted!!!")
                        if(it.value.isOpen == true) {
                            it.value.postId?.let {
                                var baseFragment: BaseFragment = DetailPostFragment()
                                Navigation.setFragmentData(baseFragment, "post_id", it)
                                Navigation.addFragment(requireActivity(), baseFragment, Constant.TAG_DETAIL_POST_FRAGMENT, R.id.homeFram, true, false)
                            }
                        } else {
                            it.value.meetUpId?.let {
                                var baseFragment: BaseFragment = MeetUpVotingFragment.getInstance(it)
                                Navigation.addFragment(requireActivity(), baseFragment, MeetUpVotingFragment.TAG, R.id.homeFram, true, true)
                            }
                        }
                    } else {
                        MyApplication.showToast(requireActivity(), "Invitation Declined!!!")
                    }


                    // ((activity as MainActivity).viewPageAdapter.instantiateItem((activity as MainActivity).viewPager,1) as MeetUpNew).getMeetUps()

                }

                is ResultHandler.Failure -> {
                    meetUpInvitation?.hideDialog()
                    MyApplication.showToast(requireActivity(), "Something went wrong...")
                }
            }
        })
    }

    override fun onResume() {
        shimmer.startShimmer()
        super.onResume()
    }

    override fun onPause() {
        shimmer.stopShimmer()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notificationListener?.remove()
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }
}