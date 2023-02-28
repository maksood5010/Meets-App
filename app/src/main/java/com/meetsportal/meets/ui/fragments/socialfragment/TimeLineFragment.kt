package com.meetsportal.meets.ui.fragments.socialfragment

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.fenchtose.tooltip.Tooltip
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.TimeLineAdapter
import com.meetsportal.meets.adapter.TimelineFooterStateAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.ViewRecyclerBinding
import com.meetsportal.meets.models.Post
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.post.FetchPostResponseItem
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.networking.profile.SuggestionResponse
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.ui.bottomsheet.ChooseTimeLineFeedSheet.Companion.GLOBAL_TIMELINE
import com.meetsportal.meets.ui.bottomsheet.ChooseTimeLineFeedSheet.Companion.MY_TIMELINE
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.*
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import org.json.JSONObject

@AndroidEntryPoint
class TimeLineFragment : BaseFragment() {


    val TAG = TimeLineFragment::class.java.simpleName
    var pagingDataSource: PagingData<FetchPostResponseItem>? = null

    val postViewModel: PostViewModel by viewModels()
    val newsFeedViewmodel: NewsFeedViewmodel by viewModels()
    val meetUpViewModel: MeetUpViewModel by viewModels()
    val profileViewmodel: UserAccountViewModel by viewModels()
    val placeViewModel: PlacesViewModel by viewModels()

    private var _binding: ViewRecyclerBinding? = null
    val binding get() = _binding!!
    lateinit var timeLineLayoutmanager: LinearLayoutManager
    lateinit var timeLineAdapter: TimeLineAdapter

    var newFeedArrayList = ArrayList<Post>()
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var suggestionResponse: SuggestionResponse = SuggestionResponse()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.i(TAG, "onCreateView::")
        //return inflater.inflate(R.layout.fragment_news_feed, container, false)
        _binding = ViewRecyclerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i(TAG, "onViewCreated:: newsFeedViewmodel ${newsFeedViewmodel.hashCode()}")
        initView(view)
        initSetUp()
        setUp()
        subscribrUserInfo()
        subscribeFeedIndo()
        subscribeObservers()

    }

    fun refreshImage(){
        binding.rvNewsFeed.adapter?.notifyDataSetChanged()
        populateData()
    }

    private fun subscribrUserInfo() {

    }

    private fun subscribeFeedIndo() {


    }

    private fun initView(view: View) {
        binding.noDataLayout.root.visibility = View.GONE
        binding.noDataLayout.noDataHeading.text = "No Posts yet"
        binding.swipeRefresh.setOnRefreshListener {
            Log.i(TAG, " refreshing:: ")
            refreshTimeLine()
            binding.swipeRefresh.isRefreshing = false
        }
//        (activity as MainActivity?)?.binding?.bottomNavigation?.setOnNavigationItemReselectedListener {
//            binding.rvNewsFeed.smoothScrollToPosition(0)
//        }

        timeLineLayoutmanager = LinearLayoutManager(activity)
        binding.rvNewsFeed.layoutManager = timeLineLayoutmanager
        binding.rvNewsFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                //super.onScrollStateChanged(recyclerView, newState)
                when(newState) {
                    SCROLL_STATE_IDLE -> {
                        if(!arrayListOf(-1, 0).contains(timeLineLayoutmanager.findFirstCompletelyVisibleItemPosition())) {
                            Log.i(TAG, " postPosition::  POSTID-- ${
                                timeLineAdapter.getPostIdFromIndex(timeLineLayoutmanager.findFirstCompletelyVisibleItemPosition())
                            }")
                            timeLineAdapter.getPostIdFromIndex(timeLineLayoutmanager.findFirstCompletelyVisibleItemPosition())
                                    ?.let {
                                        postViewModel.ingestPost(it)
                                    }
                        }
                    }
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        Utils.onClick(binding.collapsingToolbar.rlNotification, 1000) {
            MyApplication.putTrackMP(Constant.AcNotiOnHome, null)
            binding.collapsingToolbar.newNoti.visibility = View.INVISIBLE
            PreferencesManager.put<Boolean>(false, Constant.PREFERENCE_SHOW_NOTI)
            var baseFragment: BaseFragment = NotificationFragment()
            //var baseFragment: BaseFragment = NonUiFragment.getInstance("HeadlessFragment")
            Navigation.addFragment(requireActivity(), baseFragment, Constant.TAG_NOTIFICATION_FRAGMENT, R.id.homeFram, true, false)

            /*Navigation.addFragment(
                this@HomeActivityNew,
                ProfileViewFragment(),
                TAG_NOTIFICATION_FRAGMENT,
                R.id.homeFram,
                true,
                false
            )*/

        }
//        binding.collapsingToolbar.rlMap.onClick({
//            MyApplication.putTrackMP(Constant.AcMapOnHome, null)
////            enableLocationStuff(2000, 1242) {
////                fetchLastLocation()
////                Navigation.addFragment(this, MeetupOpenMemberMapFragment(), Constant.TAG_OPEN_FOR_MEETUP_MAP, R.id.homeFram, true, false)
////            }
//        }, 2000)
        binding.collapsingToolbar.ivChat.onClick({
            MyApplication.putTrackMP(Constant.AcDmOnHome, null)
            binding.collapsingToolbar.newMessage.visibility = View.GONE
            PreferencesManager.put<Boolean>(false, Constant.PREFERENCE_MSG_NOTI)
            MyApplication.smallVibrate()
            Navigation.addFragment(requireActivity(), ChatDashboardFragment(), Constant.TAG_CHAT_DASHBOARD_FRAGMENT, R.id.homeFram, stack = true, needAnimation = false)
        })

        binding.rlToast.background = Utils.getToastBacGround(requireActivity())
        binding.collapsingToolbar.tvLocation.text = ""
        binding.collapsingToolbar.civImage.onClick({
            MyApplication.smallVibrate()
            MyApplication.putTrackMP(Constant.AcHomePageDp, null)
            openProfile()
        })
        binding.collapsingToolbar.tvGreet.onClick({
            MyApplication.smallVibrate()
            MyApplication.putTrackMP(Constant.AcHomePageDp, null)
            openProfile()
        })
        binding.collapsingToolbar.ivSearch.onClick({
            MyApplication.smallVibrate()
            Navigation.addFragment(requireActivity(), SearchFragment.getInstance(0), Constant.TAG_SEARCH_FRAGMENT, R.id.homeFram, stack = true, needAnimation = false)
        })

//        Utils.showToolTips(requireActivity(), binding.collapsingToolbar.rlMap, binding.root, Tooltip.BOTTOM, "View people who are available for Meetup on the map", "rlMap") {}

    }


    private fun openProfile() {
        Navigation.addFragment(requireActivity(), ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false)
    }

    fun refreshTimeLine() {
        timeLineAdapter.refresh()
        profileViewmodel.getSuggestion()
        PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.let {
            populateData()
        }
    }

    fun scrollUpPage() {
        Log.i(TAG, " scrollingUp::: ")
        if(timeLineLayoutmanager.findFirstVisibleItemPosition().compareTo(16) == 1) {
            timeLineLayoutmanager.scrollToPosition(20)
        }
        timeLineLayoutmanager.smoothScrollToPosition(binding.rvNewsFeed, RecyclerView.State(), 0)

    }

    private fun initSetUp() {
        if(PreferencesManager.get<Boolean>(Constant.PREFERENCE_SHOW_NOTI) == true) binding.collapsingToolbar.newNoti.visibility = View.VISIBLE

        if(PreferencesManager.get<Boolean>(Constant.PREFERENCE_MSG_NOTI) == true) binding.collapsingToolbar.newMessage.visibility = View.VISIBLE

        profileViewmodel.getFullProfile()
        PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.let {
            populateData()
        }

        binding.collapsingToolbar.llCreateMeetUp.onClick({
            MyApplication.putTrackMP(Constant.AcCreatMeetHome, null)
            (activity as MainActivity?)?.meetOption?.showDialog()
        })
        binding.collapsingToolbar.llCreateMeetUp2.onClick({
            MyApplication.putTrackMP(Constant.AcCreatMeetHome, null)
            (activity as MainActivity?)?.meetOption?.showDialog()
        })
        profileViewmodel.observeFullProfileChange().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.let {
                        MyApplication.SID = it.cust_data?.sid
                        Log.i(TAG, " custdataInterest::MyApplication.SID ${MyApplication.SID}")
                        PreferencesManager.put(it, Constant.PREFRENCE_PROFILE)
                        showUpdateEmailSnake()
                        populateData()
                    }
                }

                is ResultHandler.Failure -> {
                    showToast("Something went wrong!!!")
                }
            }

        })

        MyApplication.observeShowNoti().observe(viewLifecycleOwner, {
            if(it?.get("type")
                        ?.equals(NotificationServiceExtension.NotificationTypes.IN_APP_MESSAGE.type) == true) {
                binding.collapsingToolbar.newMessage.visibility = View.VISIBLE
            } else {
                binding.collapsingToolbar.newNoti.visibility = View.VISIBLE
            }

        })

    }

    private fun showUpdateEmailSnake() {
        var email = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.email ?: ""
        Log.d(TAG, "showUpdateEmailSnak: $email")
        if(!email.trim().equals("")) return
        val snackbar = Snackbar.make(binding.rootCo, "", Snackbar.LENGTH_LONG).setDuration(10000)
            .setAction("OK") {
                var baseFragment = UpdateMailFragment.getInstance("email")
                Navigation.addFragment(requireActivity(), baseFragment, UpdateMailFragment.TAG, R.id.homeFram, true, false)
            }
        snackbar.setActionTextColor(ContextCompat.getColor(requireActivity(), R.color.primaryDark))
        val sbView = snackbar.view
        val textView = sbView.findViewById<View>(R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        val s = "Please update your email\nThis will help you recover your account"
        val ss1 = SpannableString(s)
        ss1.setSpan(RelativeSizeSpan(1.2f), 0, s.indexOf("This"), 0) // set size
        textView.setText(ss1)
        snackbar.show()
    }

    fun populateData() {
        val profile = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
       // profile?.cust_data?.profile_image_url?.let {
            binding.collapsingToolbar.civImage.loadImage(requireActivity(), profile?.cust_data?.profile_image_url, R.drawable.ic_default_person, false)
//            binding.collapsingToolbar.civImage.loadImage(requireActivity(), "https://gateway-dev.shisheo.com/cdn/profile_resolver/sid-734-73846840.png", R.drawable.ic_default_person, false)
        //}
        profile?.social?.getbadge()?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            binding.collapsingToolbar.ivDpBadge.setImageResource(firstOrNull.foreground)
        }
        binding.collapsingToolbar.tvLocation.text = "@${profile?.cust_data?.username}"
        if(profile?.cust_data?.first_name.isNullOrEmpty()) {
            binding.collapsingToolbar.tvGreet.text = profile?.cust_data?.username
        } else {
            binding.collapsingToolbar.tvGreet.text = profile?.cust_data?.first_name.plus(" ")
                    .plus(profile?.cust_data?.last_name)
        }

//        PreferencesManager.get<OtpResponse>(Constant.PREFRANCE_OTPRESPONSE).auth.first_time_login==true


        //checkIntent()

    }

    private fun setUp() {

        timeLineAdapter = getTimelineAdapter(GLOBAL_TIMELINE)
        binding.rvNewsFeed.adapter = timeLineAdapter.withLoadStateFooter(TimelineFooterStateAdapter {
            updateSuggestion()
            timeLineAdapter.retry()
        })
        postViewModel.fetchPost(null, GLOBAL_TIMELINE, true)
        updateSuggestion()

        showNoData()
        /* BillingManager(requireActivity(),viewLifecycleOwner).apply {
             startProcess()
         }*/

    }

    private fun showNoData() {
        lifecycleScope.launchWhenCreated {
            timeLineAdapter.loadStateFlow.filter {
                it.refresh is LoadState.NotLoading
            }.collect {
                if(it.append is LoadState.NotLoading && it.append.endOfPaginationReached && timeLineAdapter.itemCount < 2) {
                    binding.shimmer.stopShimmer()
                    binding.shimmer.visibility = View.GONE
                    binding.noDataLayout.root.visibility = View.VISIBLE
                } else if(it.append is LoadState.NotLoading && it.append.endOfPaginationReached && timeLineAdapter.itemCount > 1) {
                    binding.shimmer.stopShimmer()
                    binding.shimmer.visibility = View.GONE
                    binding.noDataLayout.root.visibility = View.GONE
                }
                if(timeLineAdapter.itemCount > 1) {
                    binding.shimmer.stopShimmer()
                    binding.shimmer.visibility = View.GONE
                    binding.noDataLayout.root.visibility = View.GONE
                }
                Log.i(TAG, " distinctUntilChangedBy:::  2 --  ${timeLineAdapter.itemCount}")
            }
        }
    }


    fun updateSuggestion() {
        profileViewmodel.getSuggestion()
    }

    fun getTimelineAdapter(TIMELINE: Int): TimeLineAdapter {
        binding.shimmer.startShimmer()
        binding.shimmer.visibility = View.VISIBLE
        return TimeLineAdapter(requireActivity(), this, TIMELINE, suggestionResponse, postViewModel, meetUpViewModel, {
            Log.i("TAG", " nameClick:: 1")
            postViewModel.toggleLike(it)
        }, { id, forfollow ->
            followuser(id, forfollow)
        }, {
            MyApplication.putTrackMP(Constant.AcDeletePost, JSONObject(mapOf("postId" to it)))
            val showProceed = showProceed { postViewModel.deletePost(it) }
            showProceed.setMessage("Delete", "Are you sure you want to delete this post?")
        }, { sid, postId ->
            openProfile(sid, Constant.Source.POST.sorce.plus(postId))
        })
    }

    fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if(fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(activity?.supportFragmentManager!!, fragment.tag)
    }

    fun changeTimeLine(type: Int) {
        if(type == GLOBAL_TIMELINE) {
            timeLineAdapter = getTimelineAdapter(GLOBAL_TIMELINE)
            binding.rvNewsFeed.adapter = timeLineAdapter.withLoadStateFooter(TimelineFooterStateAdapter {
                updateSuggestion()
                timeLineAdapter.retry()
            })
            //timeLineAdapter.notifyDataSetChanged()
            postViewModel.fetchPost(null, GLOBAL_TIMELINE, true)
            showNoData()
        } else if(type == MY_TIMELINE) {
            PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.let {
                timeLineAdapter = getTimelineAdapter(MY_TIMELINE)
                binding.rvNewsFeed.adapter = timeLineAdapter.withLoadStateFooter(TimelineFooterStateAdapter {
                    updateSuggestion()
                    timeLineAdapter.retry()
                })
                postViewModel.fetchPost(it.social?.id, MY_TIMELINE, true)
                showNoData()
            }
        }
    }

    private fun followuser(id: String, forfollow: Boolean) {
        if(forfollow) {
            profileViewmodel.followUser(id)
        } else {
            profileViewmodel.unfollowUser(id)
        }
    }

    private fun subscribeObservers() {
        postViewModel.observePostChange().observe(viewLifecycleOwner, Observer {
            pagingDataSource = it
            timeLineAdapter.submitData(lifecycle, it)
        })
        postViewModel.onCommentStatusUpdate().observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    refreshTimeLine()
                    if(it.value == false) {
                        showToast("Comments disabled")
                    } else {
                        showToast("Comments ena")
                    }
                }

                is ResultHandler.Failure -> {
                    Log.e(TAG, "subscribeObservers: onCommentStatusUpdate failed")
                }
            }
        })

        profileViewmodel.observeOnSuggestion().observe(viewLifecycleOwner, Observer {
            Log.i(TAG, " suggestionChanges:: 0 $it")
            when(it){
                is ResultHandler.Success-> {
                    it?.value?.let { it1 ->
                        suggestionResponse.clear()
                        suggestionResponse.addAll(it1)
                        timeLineAdapter.headerHolder?.suggestionAdapter?.notifyDataSetChanged()
                    }
                }
                is ResultHandler.Failure->{
                    Log.e(TAG, "setObserver:ResultHandler.Failure in suggestion api")
                }
            }
        })

        profileViewmodel.observeFollowUnFollow().observe(viewLifecycleOwner, Observer {

        })

        postViewModel.observePostLikeDislike().observe(viewLifecycleOwner,{
            timeLineAdapter.refresh()
        })

        postViewModel.observePostDelete().observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
            timeLineAdapter.refresh()
        })

        postViewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity, it)
        })

        profileViewmodel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity, it)
        })

        postViewModel.observerReportContent().observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    Toast.makeText(requireContext(), "Thank you for reporting, ${it.value?.get("content_type")?.asString} is under review", Toast.LENGTH_SHORT)
                            .show()
                }

                is ResultHandler.Failure -> {
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
        meetUpViewModel.observeJoinMeetUp().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "Meetup joined...")
                    refreshTimeLine()
                }

                is ResultHandler.Failure -> {
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onStart() {
        super.onStart()
        binding.shimmer.startShimmer()
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause::")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume::")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView::")

    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }


}