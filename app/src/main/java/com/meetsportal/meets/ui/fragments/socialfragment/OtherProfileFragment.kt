package com.meetsportal.meets.ui.fragments.socialfragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.tabs.TabLayout
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.OtherPostAdapter
import com.meetsportal.meets.adapter.ProfileInterestAdapter
import com.meetsportal.meets.adapter.TimelineFooterStateAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.profile.OtherProfileGetResponse
import com.meetsportal.meets.overridelayout.AppBarLayoutBehavior
import com.meetsportal.meets.ui.bottomsheet.ChooseTimeLineFeedSheet
import com.meetsportal.meets.ui.bottomsheet.ReportSheet
import com.meetsportal.meets.ui.dialog.ExperienceDialog
import com.meetsportal.meets.ui.dialog.LoaderDialog
import com.meetsportal.meets.ui.dialog.MeetsCredibilityStatusDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.FilterBottomSheet
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.TAG_ONE_N_ONE_CHAT
import com.meetsportal.meets.utils.Constant.VwOtherProfileView
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.meetsportal.meets.viewmodels.PostViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_profile.*
import org.json.JSONObject


@AndroidEntryPoint
class OtherProfileFragment : BaseFragment() {

    private lateinit var meetsCredibilityStatusDialog: MeetsCredibilityStatusDialog


    lateinit var loader: LoaderDialog
    var image_uri: Uri? = null

    //lateinit var profileSetting: ImageView
    lateinit var interestIcon: ImageView
    lateinit var ivProfileSetting: ImageView

    val userAccountViewModel: UserAccountViewModel by viewModels()
    val postViewModel: PostViewModel by viewModels()
    val meetUpViewModel: MeetUpViewModel by viewModels()

    lateinit var profile_pic: ImageView
    lateinit var postCount: TextView
    lateinit var post: TextView
    lateinit var followerCount: TextView
    lateinit var follower: TextView
    lateinit var followingCount: TextView
    lateinit var follwing: TextView
    lateinit var bio: TextView
    lateinit var gp_bio: Group
    lateinit var name: TextView
    lateinit var dp: CircleImageView
    lateinit var poster: ImageView
    lateinit var rvPost: RecyclerView
    var mBottomSheetOptions: BottomSheetOptions? = null
    lateinit var postAdapter: OtherPostAdapter
    var isSpotlight1Present = false
    var isSpotlight2Present = false
    var isSpotlight3Present = false
    lateinit var tvVaccine: TextView
    lateinit var spotlight1: ImageView
    lateinit var spotlight2: ImageView
    lateinit var spotlight3: ImageView
    lateinit var verified: ImageView
    lateinit var message: TextView
    lateinit var noPostScreen: ConstraintLayout
    lateinit var headerInterest: TextView
    var intentFrom: String? = null
    val FROM_CAMERA = "camera"
    val FROM_SPOTLIGHT1 = "spotlight1"
    val FROM_SPOTLIGHT2 = "spotlight2"
    val FROM_SPOTLIGHT3 = "spotlight3"

    lateinit var followFollowing: ConstraintLayout
    lateinit var gpPost: Group
    lateinit var gpFollower: Group
    lateinit var gpFollowing: Group
    lateinit var interest: RecyclerView
    lateinit var tvinterest: TextView
    lateinit var follow: TextView
    lateinit var tabLayout: TabLayout
    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    lateinit var thisview: View
    lateinit var shimmer: ShimmerFrameLayout
    lateinit var llBadge: LinearLayout
    lateinit var ivBadge: ImageView
    lateinit var ivDpBadge: ImageView
    lateinit var tvCurrentBadge: TextView
    lateinit var tvCurrentLevel: TextView
    lateinit var tvMeetups: TextView
    lateinit var tvPositive: TextView
    lateinit var llPositive: LinearLayout
    lateinit var llMeetups: LinearLayout
    lateinit var ivPositive: ImageView
    lateinit var ivMeetup: ImageView
    lateinit var tvWorth: TextView
    lateinit var tvCreateMeet: TextView

    var list: ArrayList<String> = arrayListOf("Block")
    var sheet: FilterBottomSheet? = null
    lateinit var userId: String
    var blockedByYou: Boolean=false
    var otherProfile: OtherProfileGetResponse? = null

    companion object {

        val TAG = OtherProfileFragment::class.java.simpleName!!
        val OTHER_USER_ID = "otherUserId"
        val ACTION_SOURCE = "actionSource"
        fun getSpotLightUrl(i: Int, otherProfileGetResponse: OtherProfileGetResponse): String? {
//            otherProfileGetResponse?.social?.spotlights?.let {
//                for(item in it) {
//                    when(i) {
//                        1 -> item.one?.let { return@getSpotLightUrl it }
//                        2 -> item.two?.let { return@getSpotLightUrl it }
//                        3 -> item.three?.let { return@getSpotLightUrl it }
//                    }
//                }
//            }
            return null
        }

        fun getInstance(sid : String?,actionSource: String?=null):OtherProfileFragment{
            return OtherProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(OTHER_USER_ID, sid)
                    putString(ACTION_SOURCE, actionSource)
                }
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_other_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp(view)
    }



    private fun initView(view: View) {

        MyApplication.putTrackMP(VwOtherProfileView, JSONObject(mapOf("sid" to arguments?.getString(OTHER_USER_ID))))
        thisview = view
        sheet = FilterBottomSheet(requireActivity(), list) {
            when(it)
            {
                0-> {
                    otherProfile?.let {
                        if (blockedByYou) {
                            val showProceed =
                                showProceed { userAccountViewModel.unBlockUser(it?.cust_data?.sid) }
                            showProceed.setMessage(
                                "Unblock",
                                "Are you sure you want to Unblock this user?"
                            )
                        } else {
                            val showProceed =
                                showProceed { userAccountViewModel.blockUser(it?.cust_data?.sid) }
                            showProceed.setMessage(
                                "Block",
                                "Are you sure you want to block this user?"
                            )
                        }
                    }
                }
                1->{
                    showOption(ReportSheet(postViewModel, activity, arguments?.getString(OTHER_USER_ID), "user"),this)
                }
            }
        }
        //profileSetting = view.findViewById(R.id.iv_profile_setting)
        interestIcon = view.findViewById(R.id.iv_interest)
        ivProfileSetting = view.findViewById(R.id.iv_profile_setting)
        profile_pic = view.findViewById(R.id.app_image)
        postCount = view.findViewById(R.id.tv_post_count)
        post = view.findViewById(R.id.tv_post)
        followerCount = view.findViewById(R.id.tv_follow_count)
        follower = view.findViewById(R.id.tv_follower)
        followingCount = view.findViewById(R.id.tv_following_count)
        follwing = view.findViewById(R.id.tv_following)

        bio = view.findViewById(R.id.tv_bio)
        gp_bio = view.findViewById(R.id.gp_bio)
        name = view.findViewById(R.id.tv_username)
        dp = view.findViewById(R.id.civ_image)

        followFollowing = view.findViewById<ConstraintLayout>(R.id.cl_follow)
        gpPost = view.findViewById(R.id.gp_post)
        gpFollower = view.findViewById(R.id.gp_follower)
        gpFollowing = view.findViewById(R.id.gp_following)
        rvPost = view.findViewById(R.id.rv_post)
        tvVaccine = view.findViewById(R.id.tv_vaccine_status)
        spotlight1 = view.findViewById(R.id.spotlight1)
        spotlight2 = view.findViewById(R.id.spotlight2)
        spotlight3 = view.findViewById(R.id.spotlight3)
        interest = view.findViewById(R.id.rv_interest)
        tvinterest = view.findViewById(R.id.tv_interest)
        tabLayout = view.findViewById(R.id.tl_tabs)
        follow = view.findViewById<TextView>(R.id.tv_follow)
        verified = view.findViewById<ImageView>(R.id.iv_verified)
        message = view.findViewById<TextView>(R.id.message).apply { visibility = View.VISIBLE }
        headerInterest = view.findViewById<TextView>(R.id.tv_Interest)
        noPostScreen = view.findViewById(R.id.no_post)

        shimmer = view.findViewById(R.id.shimmer)
        llBadge = view.findViewById<LinearLayout>(R.id.llBadge)
        ivBadge = view.findViewById<ImageView>(R.id.ivBadge)
        ivDpBadge = view.findViewById<ImageView>(R.id.ivDpBadge)
        tvCurrentLevel = view.findViewById(R.id.tvCurrentLevel)
        tvCurrentBadge = view.findViewById(R.id.tvCurrentBadge)
        tvWorth = view.findViewById(R.id.tv_worth)
        tvCreateMeet = view.findViewById(R.id.tvCreateMeet)
        tvPositive = view.findViewById(R.id.tvPositive)
        tvMeetups = view.findViewById(R.id.tvMeetups)
        llPositive = view.findViewById(R.id.llPositive)
        llMeetups = view.findViewById(R.id.llMeetups)
        ivMeetup = view.findViewById(R.id.ivMeetup)
        ivPositive = view.findViewById(R.id.ivPositive)

        spotlight1.setBackgroundResource(R.drawable.ic_other_spot_bg)
        spotlight2.setBackgroundResource(R.drawable.ic_other_spot_bg)
        spotlight3.setBackgroundResource(R.drawable.ic_other_spot_bg)



        //view.findViewById<RelativeLayout>(R.id.rl_back).setOnClickListener{ activity?.onBackPressed() }

        val params: CoordinatorLayout.LayoutParams = appbar.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = AppBarLayoutBehavior(requireContext(),null)
        appbar.requestLayout()
        tvWorth.setGradient(requireActivity(), GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.parseColor("#FF7272"), Color.parseColor("#32BFC9")), 20f)

        var layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW;
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        interest.layoutManager = layoutManager
        loader = LoaderDialog(requireActivity())
    }

    private fun setUp(view: View) {

        //profileSetting.visibility = View.GONE
        interestIcon.visibility = View.GONE

        userId = Navigation.getFragmentData(this, OTHER_USER_ID)
        Log.i(TAG, " otherUserIc:: ${userId}")
        addObserver()
        userAccountViewModel.getOtherProfile(userId,Navigation.getFragmentData(this, ACTION_SOURCE))



        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.i(TAG, " tab:: ${tab?.text} ${tab}")
                //if(tab?.text?.equals("Places") == true){
                if(tabLayout.selectedTabPosition == 1) {
                    Log.i(TAG, "  Places:::: ")
                    postAdapter.setViewtypes("linear")
                    rvPost.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    rvPost.adapter = postAdapter.withLoadStateFooter(TimelineFooterStateAdapter {
                        postAdapter.retry()
                    })
                } else if(tabLayout.selectedTabPosition == 0) {
                    Log.i(TAG, "  Posts:::: ")
                    //rvPost.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
                    postAdapter.setViewtypes("grid")
                    rvPost.layoutManager = GridLayoutManager(requireContext(), 3)
                    rvPost.adapter = postAdapter.withLoadStateFooter(TimelineFooterStateAdapter {
                        postAdapter.retry()
                    })
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

    }

    private fun spotlight3Clicked(otherProfileGetResponse: OtherProfileGetResponse) {
        if(isSpotlight3Present) {
            Log.i(TAG, "SpotLight3 Present")
            Log.i(TAG, "SpotLight1 Present")
            var url = getSpotLightUrl(3, otherProfileGetResponse)
            var baseFragment: BaseFragment = ProfileEditPicPage()
            baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.TYPE, ProfileEditPicPage.SPOTLIGHT3)
            baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.IMAGE_URL, url)
            baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.PROFILE_TYPE, ProfileEditPicPage.OTHER)
            Navigation.addFragment(requireActivity(), baseFragment, "ProfileEditPicPage", R.id.homeFram, true, false)

        }
    }

    private fun spotlight2Clicked(otherProfileGetResponse: OtherProfileGetResponse) {
        if(isSpotlight2Present) {
            Log.i(TAG, "SpotLight2 Present")
            Log.i(TAG, "SpotLight1 Present")
            var url = getSpotLightUrl(2, otherProfileGetResponse)
            var baseFragment: BaseFragment = ProfileEditPicPage()
            baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.TYPE, ProfileEditPicPage.SPOTLIGHT2)
            baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.IMAGE_URL, url)
            baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.PROFILE_TYPE, ProfileEditPicPage.OTHER)
            Navigation.addFragment(requireActivity(), baseFragment, "ProfileEditPicPage", R.id.homeFram, true, false)

        }
    }

    private fun spotlight1Clicked(otherProfileGetResponse: OtherProfileGetResponse) {
        if(isSpotlight1Present) {
            Log.i(TAG, "SpotLight1 Present")
            var url = getSpotLightUrl(1, otherProfileGetResponse)
            var baseFragment: BaseFragment = ProfileEditPicPage()
            baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.TYPE, ProfileEditPicPage.SPOTLIGHT1)
            baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.IMAGE_URL, url)
            baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.PROFILE_TYPE, ProfileEditPicPage.OTHER)
            Navigation.addFragment(requireActivity(), baseFragment, "ProfileEditPicPage", R.id.homeFram, true, false)

        }
    }


    fun populateView(response: OtherProfileGetResponse) {
        otherProfile = response
//        it.cust_data
        response.social.badge?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            llBadge.setBackgroundResource(firstOrNull.background)
            ivBadge.setImageResource(firstOrNull.foreground)
            ivDpBadge.setImageResource(firstOrNull.foreground)
            tvCurrentBadge.text = firstOrNull.name
            tvCurrentLevel.text = "Level ${firstOrNull.level}"
        }

        response.social.getmints()?.let {
            val prettyCount = Utils.prettyCount(it.toDouble())
            tvWorth.text = "Worth: $prettyCount mints"
        }?:run{
            tvWorth.text = "Worth: 0 mints"
        }
        tvWorth.onClick({
            Log.d(TAG, "populateView: onViewCreated:data $userId")
//                val leaderBoardFragment:BaseFragment = LeaderBoardFragment()
//                Navigation.setFragmentData(leaderBoardFragment,"otherProfileSID",userId)
            val bundle = Bundle()
            bundle.putString("otherProfileSID", userId)
            navigate(LeaderBoardFragment(), bundle)
        })
        tvCreateMeet.onClick({
            MyApplication.smallVibrate()
            response?.let {
                val baseFragment: BaseFragment = MeetUpViewPageFragment.getInstance(it?.social?.sid, false)
                Navigation.addFragment(requireActivity(), baseFragment, MeetUpViewPageFragment.TAG, R.id.homeFram, true, true)
            }
        })

        val positiveDialog = ExperienceDialog(requireActivity(), response.social.meetup_positive_experience_count ?: 0, 1, response.cust_data.username)
        llPositive.onClick({
            positiveDialog.show()
        })
        val meetupDialog = ExperienceDialog(requireActivity(), response.social.meetup_attendance_count ?: 0, 0, response.cust_data.username)
        llMeetups.onClick({
            meetupDialog.show()
        })
        response.social.meetup_attendance_count?.let {
            if(it > 0) {
                llMeetups.setBackgroundResource(R.drawable.bg_meetups_color)
                ivMeetup.setImageResource(R.drawable.table02)
            } else {
                llMeetups.setBackgroundResource(R.drawable.bg_postive_gray)
                ivMeetup.setImageResource(R.drawable.table)
            }
            tvMeetups.text = "$it Meetups"
        } ?: run {
            tvMeetups.text = "0 Meetups"
            llMeetups.setBackgroundResource(R.drawable.bg_postive_gray)
            ivMeetup.setImageResource(R.drawable.table)
        }

        response.social.meetup_positive_experience_count?.let {
            if(it > 0) {
                llPositive.setBackgroundResource(R.drawable.bg_positive)
                ivPositive.setImageResource(R.drawable.thump01)
            } else {
                llPositive.setBackgroundResource(R.drawable.bg_postive_gray)
                ivPositive.setImageResource(R.drawable.thumb_gray)
            }
            tvPositive.text = "$it Positive Exp"
        } ?: run {
            tvPositive.text = "0 Positive Exp"
            llPositive.setBackgroundResource(R.drawable.bg_postive_gray)
            ivPositive.setImageResource(R.drawable.thumb_gray)
        }
        meetsCredibilityStatusDialog = MeetsCredibilityStatusDialog(requireActivity(), false) { s: String, i: Int ->
        }
        meetsCredibilityStatusDialog?.initDialog(badge = Utils.getBadge(response.social.badge), mints = response.social.getmints())
        llBadge.setOnClickListener { it1: View? ->
            meetsCredibilityStatusDialog.show()
        }
        followFollowing.setOnClickListener {
            /*var baseFragment: BaseFragment = FollowFollowerFragment()
            Navigation.setFragmentData(baseFragment,FollowFollowerFragment.SID,otherProfile?.cust_data?.sid)
            Navigation.setFragmentData(baseFragment,FollowFollowerFragment.USERNAME,otherProfile?.cust_data?.username)
            Navigation.setFragmentData(baseFragment,FollowFollowerFragment.FOLLOWER_COUNT,otherProfile?.social?.followers_count.toString())
            Navigation.setFragmentData(baseFragment,FollowFollowerFragment.FOLLOWING_COUNT,otherProfile?.social?.followings_count.toString())
            Navigation.addFragment(requireActivity(),baseFragment,
                Constant.TAG_FOLLOW_FOLLOWING_FRAGMENT,R.id.homeFram,true,false)*/
        }
        Log.i(TAG, " adding ClickListner 01")

        //----------------click Post Count-----------------------
        postCount.setOnClickListener {
            Log.i(TAG, " adding ClickListner  1")
            appbar.setExpanded(false, true)
        }
        post.setOnClickListener {
            Log.i(TAG, " adding ClickListner  1")
            appbar.setExpanded(false, true)
        }

        //--------------------------------------------

        //---------------click follower------------------------
        followerCount.setOnClickListener {
            var baseFragment: BaseFragment = FollowFollowerFragment()
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.SID, otherProfile?.cust_data?.sid)
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.USERNAME, otherProfile?.cust_data?.username)
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWER_COUNT, otherProfile?.social?.followers_count.toString())
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWING_COUNT, otherProfile?.social?.followings_count.toString())
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.TAB, "0")
            Navigation.addFragment(requireActivity(), baseFragment, Constant.TAG_FOLLOW_FOLLOWING_FRAGMENT, R.id.homeFram, true, false)
        }
        follower.setOnClickListener {
            var baseFragment: BaseFragment = FollowFollowerFragment()
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.SID, otherProfile?.cust_data?.sid)
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.USERNAME, otherProfile?.cust_data?.username)
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWER_COUNT, otherProfile?.social?.followers_count.toString())
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWING_COUNT, otherProfile?.social?.followings_count.toString())
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.TAB, "0")
            Navigation.addFragment(requireActivity(), baseFragment, Constant.TAG_FOLLOW_FOLLOWING_FRAGMENT, R.id.homeFram, true, false)
        }

        //--------------------------------------------

        //---------------click followeing------------------------


        followingCount.setOnClickListener {
            var baseFragment: BaseFragment = FollowFollowerFragment()
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.SID, otherProfile?.cust_data?.sid)
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.USERNAME, otherProfile?.cust_data?.username)
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWER_COUNT, otherProfile?.social?.followers_count.toString())
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWING_COUNT, otherProfile?.social?.followings_count.toString())
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.TAB, "1")
            Navigation.addFragment(requireActivity(), baseFragment, Constant.TAG_FOLLOW_FOLLOWING_FRAGMENT, R.id.homeFram, true, false)
        }
        follwing.setOnClickListener {
            var baseFragment: BaseFragment = FollowFollowerFragment()
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.SID, otherProfile?.cust_data?.sid)
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.USERNAME, otherProfile?.cust_data?.username)
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWER_COUNT, otherProfile?.social?.followers_count.toString())
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWING_COUNT, otherProfile?.social?.followings_count.toString())
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.TAB, "1")
            Navigation.addFragment(requireActivity(), baseFragment, Constant.TAG_FOLLOW_FOLLOWING_FRAGMENT, R.id.homeFram, true, false)
        }
        ivProfileSetting.setOnClickListener {
            sheet?.show()
        }


        message.onClick( {
            var baseFragment: BaseFragment = OnenOneChat.getInstance(otherProfile?.cust_data?.sid)
            Navigation.addFragment(requireActivity(), baseFragment, TAG_ONE_N_ONE_CHAT, R.id.homeFram, true, false)
        })


        postAdapter.notifyItemChanged(0)
        response?.let {
            //vaccine Status
            compositeDisposable.add(Utils.onClick(follow, 1000) {
                if(follow.text.equals("Unfollow")) userAccountViewModel.unfollowUser(it?.cust_data?.sid)
                else if(follow.text.equals("Follow")) {
                    if(it.social.blocked == false) {
                        userAccountViewModel.followUser(it?.cust_data?.sid)
                    } else {
                        Toast.makeText(requireContext(), "You cannot follow...", Toast.LENGTH_SHORT)
                                .show()
                    }
                } else if(follow.text.equals("UnBlock")){
                    val showProceed = showProceed { userAccountViewModel.unBlockUser(it?.cust_data?.sid) }
                    showProceed.setMessage("Unblock", "Are you sure you want to Unblock this user?")
                }
            })
            setFolowed(it.social.followed_by_you, it.social.blocked_by_you)
            //Utils.stickImage(requireContext(), dp, it.cust_data.profile_image_url, null)
            dp.loadImage(requireContext(),it.cust_data.profile_image_url,R.drawable.ic_default_person)

            dp.onClick({
                var baseFragment: BaseFragment = ProfileEditPicPage()
                baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.TYPE, ProfileEditPicPage.PROFILE_PIC)
                baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.IMAGE_URL, response.cust_data.profile_image_url)
                baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.PROFILE_TYPE, ProfileEditPicPage.OTHER)
                Navigation.addFragment(requireActivity(), baseFragment, "ProfileEditPicPage", R.id.homeFram, true, false)
            })
            it?.social?.bio?.let {
                bio.text = it
            } ?: run {
                gp_bio.visibility = View.GONE
            }


            var layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW;
                justifyContent = JustifyContent.FLEX_START
                alignItems = AlignItems.FLEX_START
            }
            interest.layoutManager = layoutManager
            interest.adapter = ProfileInterestAdapter(requireActivity(), ArrayList(it.cust_data?.interests))
            Log.i(TAG, " interestSize:: ${it.cust_data?.interests?.size}")
            if(it.cust_data?.interests?.size == 0) {

            }

            if(it.cust_data.interests.size == 0) {
                interest.visibility = View.GONE
                headerInterest.visibility = View.GONE
            }
            //name.text = it.cust_data.first_name ?: it?.cust_data?.username
            name.text = it.cust_data.username

            /*verified.onClick({
                if(it?.cust_data?.verified_user != true)
                it.cust_data.profile_image_url?.let {
                    Navigation.addFragment(requireActivity(),
                        VerificationPagerFragment(),
                        VerificationPagerFragment.TAG,R.id.homeFram,true,true)
                }?:run{
                    MyApplication.showToast(requireActivity(),"First upload profile picture...")
                }
            },1000)*/
            Log.i(TAG, " verified_userverified_user:: ${it.cust_data.verified_user}")
            if(it.cust_data.verified_user == true) verified.setImageResource(R.drawable.ic_verified_tick)
            else verified.setImageResource(R.drawable.ic_unverified)
            //if(it?.cust_data?.verified_user == true) verified.visibility = View.VISIBLE else verified.visibility = View.GONE

            if(it?.social?.vaccinated == true) {
                tvVaccine.text = "I am vaccinated"
            } else {
                tvVaccine.visibility = View.GONE
            }
            isSpotlight1Present = false
            isSpotlight2Present = false
            isSpotlight3Present = false
            spotlight1.setOnClickListener { view -> spotlight1Clicked(it) }
            spotlight2.setOnClickListener { view -> spotlight2Clicked(it) }
            spotlight3.setOnClickListener { view -> spotlight3Clicked(it) }
//            for(item in it?.social?.spotlights!!) {
//                item?.one?.let {
//                    Log.i(TAG, " SPotlight::  1")
//                    isSpotlight1Present = true
//                    Utils.stickImage(requireContext(), spotlight1, it, null)
//                }
//                item?.two?.let {
//                    Log.i(TAG, " SPotlight::  2")
//                    isSpotlight2Present = true
//                    Utils.stickImage(requireContext(), spotlight2, it, null)
//                }
//                item?.three?.let {
//                    Log.i(TAG, " SPotlight::  3")
//                    isSpotlight3Present = true
//                    Utils.stickImage(requireContext(), spotlight3, it, null)
//                }
//                Log.i("TAG", " ${item.toString()}")
//            }
            Log.i(TAG, "Response:: ${it.toString()}")
            //name.text = it.cust_data.first_name ?: it.cust_data.username
            it.social.wallpaper_url?.let { url ->
                Utils.stickImage(requireContext(), profile_pic, url, null)
            }?:run{
                Utils.stickImage(requireContext(), profile_pic, it.cust_data.profile_image_url, null)
            }

            //bio.text = it.social.bio
            followerCount.text = Utils.prettyCount(it.social.followers_count)
            followingCount.text = Utils.prettyCount(it.social.followings_count)
            postCount.text = Utils.prettyCount(it.social.posts_count)




            Log.i(TAG, " postCount:: ${it.social.posts_count}")
            if(it.social.posts_count > 0) {
                postViewModel.fetchPost(it.social.sid, ChooseTimeLineFeedSheet.GLOBAL_TIMELINE, false)
            } else {

            }
        }
    }

    private fun addObserver() {
        userAccountViewModel.observerFullOtherProfileChange().observe(viewLifecycleOwner, Observer {

            when(it){
                is ResultHandler.Success ->{
                    shimmer.visibility = View.GONE
                    it.value?.let {
                        Log.i(TAG, "Response:::: $it")
                        postAdapter = OtherPostAdapter(requireActivity(), this, it, postViewModel, meetUpViewModel, {
                            postViewModel.toggleLike(it)
                        })
                        postAdapter.setViewtypes("grid")
                        rvPost.layoutManager = GridLayoutManager(requireContext(), 3)
                        //rvPost.adapter = TempRecyclerViewAdapter(requireContext(), R.layout.card_news_feed)
                        rvPost.adapter = postAdapter.withLoadStateFooter(TimelineFooterStateAdapter {
                            postAdapter.retry()
                        })
                        populateView(it)
                    }
                }
                is ResultHandler.Failure -> {
                    Log.e(TAG," GetOtherProfileApi Failed::: ")
                }
            }
        })

        meetUpViewModel.observeJoinMeetUp().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "Meetup joined...")
                    postAdapter.refresh()
                }

                is ResultHandler.Failure -> {
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })

        postViewModel.observePostChange().observe(viewLifecycleOwner, Observer {
            postAdapter.submitData(lifecycle, it)
            rvPost.scrollToPosition(0)

        })

        userAccountViewModel.observeFollowUnFollow().observe(viewLifecycleOwner, Observer {
            Log.i(TAG, " foollwed:: $it")
            setFolowed(it, false)
        })

        userAccountViewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity, it)
        })

        userAccountViewModel.observeUnblockUser().observe(viewLifecycleOwner, Observer {
            Log.i(TAG, " Unblocked::: ")
            showToast("you have Unblocked ${otherProfile?.cust_data?.username}")
            setFolowed(false, false)
        })
        userAccountViewModel.observeOnBlockUser().observe(viewLifecycleOwner, Observer {
            Log.i(TAG, " Unblocked::: ")
            showToast("you have Blocked ${otherProfile?.cust_data?.username}")
            setFolowed(false, true)
        })

        postViewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity , it)
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
            //Toast.makeText(requireContext(),"Thank you for reporting, ${it.get("content_type")} is under review",Toast.LENGTH_SHORT).show()
        })

    }

    fun setFolowed(following: Boolean?, isBlocked: Boolean?) {
        blockedByYou=isBlocked?:false
        if(isBlocked == false) {
            list = arrayListOf("Block","Report")
            if(following == true) {
                followerCount.text = (followerCount.text.toString().toInt() + 1).toString()
                follow.text = "Unfollow"
                follow.setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryDark))
                follow.background = ContextCompat.getDrawable(requireContext(), R.drawable.horizontal_round_shape)
            } else {
//                if(followerCount.text.toString().toInt()==0) return
                followerCount.text = (followerCount.text.toString().toInt() - 1).toString()
                follow.text = "Follow"
                follow.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                follow.background = ContextCompat.getDrawable(requireContext(), R.drawable.round_border_primary_bg)
            }
        } else {
            list = arrayListOf("UnBlock","Report")
            followerCount.text = (followerCount.text.toString().toInt() + 1).toString()
            follow.text = "UnBlock"
            follow.setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryDark))
            follow.background = ContextCompat.getDrawable(requireContext(), R.drawable.horizontal_round_shape)
        }
        sheet?.setArrayList(list)
    }

    override fun onResume() {
        super.onResume()
        shimmer.startShimmer()
    }

    override fun onPause() {
        shimmer.startShimmer()
        super.onPause()
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }

}