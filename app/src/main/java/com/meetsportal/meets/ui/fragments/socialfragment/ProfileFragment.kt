package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.fenchtose.tooltip.Tooltip
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.PostAdapter
import com.meetsportal.meets.adapter.ProfileInterestAdapter
import com.meetsportal.meets.adapter.TimelineFooterStateAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.profile.FullInterestGetResponse
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.networking.profile.Spotlight
import com.meetsportal.meets.overridelayout.AppBarLayoutBehavior
import com.meetsportal.meets.overridelayout.SpaceItemDecoration
import com.meetsportal.meets.ui.bottomsheet.ChooseTimeLineFeedSheet.Companion.GLOBAL_TIMELINE
import com.meetsportal.meets.ui.dialog.ExperienceDialog
import com.meetsportal.meets.ui.dialog.LoaderDialog
import com.meetsportal.meets.ui.dialog.MeetsCredibilityStatusDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.ProfileEditPicPage.Companion.IMAGE_URL
import com.meetsportal.meets.ui.fragments.socialfragment.ProfileEditPicPage.Companion.SPOTLIGHT1
import com.meetsportal.meets.ui.fragments.socialfragment.ProfileEditPicPage.Companion.SPOTLIGHT2
import com.meetsportal.meets.ui.fragments.socialfragment.ProfileEditPicPage.Companion.SPOTLIGHT3
import com.meetsportal.meets.ui.fragments.socialfragment.ProfileEditPicPage.Companion.TYPE
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.ui.fragments.verification.VerificationPagerFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.AcDeletePost
import com.meetsportal.meets.utils.Constant.AcDisableCmnt
import com.meetsportal.meets.utils.Constant.CAMERA_REQUEST
import com.meetsportal.meets.utils.Constant.GALLERY_IMAGE_REQUEST
import com.meetsportal.meets.utils.Constant.PREFRENCE_PROFILE
import com.meetsportal.meets.utils.Constant.TAG_EDIT_PROFILE
import com.meetsportal.meets.utils.Constant.TAG_FOLLOW_FOLLOWING_FRAGMENT
import com.meetsportal.meets.utils.Constant.TAG_UPLOADVACCINE_FRAGMENT
import com.meetsportal.meets.utils.Constant.VwMyProfileView
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.PostViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_profile.*
import org.json.JSONObject
import java.io.InputStream


@AndroidEntryPoint
class ProfileFragment : BaseFragment(), View.OnClickListener, BottomSheetOptions.BottomSheetListener {

    private var isShown: Boolean = false
    private val TAG = ProfileFragment::class.java.simpleName!!

    lateinit var loader: LoaderDialog
    var image_uri: Uri? = null

    lateinit var profileSetting: ImageView
    lateinit var interestIcon: ImageView

    val userAccountViewModel: UserAccountViewModel by viewModels()
    val postViewModel: PostViewModel by viewModels()

    lateinit var profile_pic: ImageView
    lateinit var postCount: TextView
    lateinit var follower: TextView
    lateinit var following: TextView
    lateinit var bio: TextView
    lateinit var bioDesc: TextView
    lateinit var name: TextView
    lateinit var tvCreateMeet: TextView
    lateinit var tvCreatePost: TextView
    lateinit var tvProfileView: TextView
    lateinit var followFollowing: ConstraintLayout
    lateinit var verifyIcon: ImageView
    lateinit var dp: CircleImageView
    lateinit var poster: ImageView
    lateinit var camera: ImageView
    lateinit var rvPost: RecyclerView

    lateinit var rlMySafe: RelativeLayout
    lateinit var tvMySafe: TextView

    lateinit var tvVaccine: TextView
    lateinit var spotlight1: ImageView
    lateinit var spotlight2: ImageView
    lateinit var spotlight3: ImageView
    lateinit var noPostScreen: ConstraintLayout
    lateinit var dialog: BottomSheetDialog
    var mBottomSheetOptions: BottomSheetOptions? = null
    lateinit var interest: RecyclerView
    lateinit var tvinterest: TextView
    lateinit var tvCurrentBadge: TextView
    lateinit var tvCurrentLevel: TextView
    lateinit var tvMeetups: TextView
    lateinit var tvWorth: TextView
    lateinit var tvPositive: TextView
    lateinit var llPositive: LinearLayout
    lateinit var llMeetups: LinearLayout
    lateinit var ivPositive: ImageView
    lateinit var ivMeetup: ImageView
    lateinit var postAdapter: PostAdapter
    lateinit var tabLayout: TabLayout
    lateinit var shimmer: ShimmerFrameLayout
    lateinit var llBadge: LinearLayout
    lateinit var rootCo: CoordinatorLayout
    lateinit var ivBadge: ImageView
    lateinit var ivDpBadge: ImageView
    var profileInterestAdapter: ProfileInterestAdapter? = null
    var meetsCredibilityStatusDialog: MeetsCredibilityStatusDialog? = null
    var allInterestList = PreferencesManager.get<FullInterestGetResponse>(Constant.PREFRENCE_INTEREST)?.definition ?: ArrayList()


    //lateinit var gpPost: Group


    var isSpotlight1Present = false
    var isSpotlight2Present = false
    var isSpotlight3Present = false
    var intentFrom: String? = null
    val FROM_CAMERA = "camera"
    val FROM_SPOTLIGHT1 = "spotlight1"
    val FROM_SPOTLIGHT2 = "spotlight2"
    val FROM_SPOTLIGHT3 = "spotlight3"


    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    lateinit var thisview: View
    fun getIsShown(id: String): Boolean {
        var count: Int = PreferencesManager.get<Int?>(id) ?: 0
        //showToast("Count:$count")
        count += 1
        if(count > 2) {
            return true
        } else {
            PreferencesManager.put<Int>(count, id)
            return false
        }
//        return false
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp(view)
        setFragmentListener()
//        appbar.addOnOffsetChangedListener(object :AppBarLayout.OnOffsetChangedListener{
//            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
//                //Log.i(TAG," offset::: $verticalOffset")
//            }
//        })
        //observerInfoChange()
    }

    fun changeCmtStatus(postId: String?, enable: Boolean) {
        if(enable) {

        } else {
            MyApplication.putTrackMP(AcDisableCmnt, JSONObject(mapOf("postId" to postId)))
        }
        postViewModel.disableComment(postId, enable)
    }


    private fun initView(view: View) {
        MyApplication.putTrackMP(VwMyProfileView, null)
        thisview = view
        profileSetting = view.findViewById(R.id.iv_profile_setting)
        interestIcon = view.findViewById(R.id.iv_interest)
        profile_pic = view.findViewById(R.id.app_image)
        postCount = view.findViewById(R.id.tv_post_count)
        follower = view.findViewById(R.id.tv_follower_count)
        following = view.findViewById(R.id.tv_following_count)
        bio = view.findViewById(R.id.tv_bio)
        bioDesc = view.findViewById(R.id.bio_desc)
        name = view.findViewById(R.id.tv_username)
        tvCreateMeet = view.findViewById(R.id.tvCreateMeet)
        tvCreatePost = view.findViewById(R.id.tvCreatePost)
        tvProfileView = view.findViewById(R.id.tvProfileView)
        dp = view.findViewById(R.id.civ_image)
        camera = view.findViewById(R.id.iv_camera)
        followFollowing = view.findViewById<ConstraintLayout>(R.id.cl_follow)
        rvPost = view.findViewById(R.id.rv_post)
        tvVaccine = view.findViewById(R.id.tv_vaccine_status)
        spotlight1 = view.findViewById(R.id.spotlight1)
        spotlight2 = view.findViewById(R.id.spotlight2)
        spotlight3 = view.findViewById(R.id.spotlight3)
        interest = view.findViewById(R.id.rv_interest)
        tvinterest = view.findViewById(R.id.tv_interest)
        tvCurrentLevel = view.findViewById(R.id.tvCurrentLevel)
        tvPositive = view.findViewById(R.id.tvPositive)
        tvMeetups = view.findViewById(R.id.tvMeetups)
        tvWorth = view.findViewById(R.id.tv_worth)
        llPositive = view.findViewById(R.id.llPositive)
        llMeetups = view.findViewById<LinearLayout>(R.id.llMeetups).apply {

        }
        rlMySafe = view.findViewById<RelativeLayout>(R.id.rlMySafe).apply {
//            setRoundedColorBackground(requireActivity(),color = R.color.page_bg,enbleDash = true,strokeColor = R.color.primaryDark,strokeHeight = 1f,Dashgap = 0f)
            setGradient(requireActivity(), GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#32BFC9"), Color.parseColor("#2E5CC5")), 30f)
            onClick({
                MyApplication.putTrackMP(Constant.AcMySafeIcon, null)
                Navigation.addFragment(requireActivity(), MySafeFragment(), Constant.TAG_MYSAFE_FRAGMENT, R.id.homeFram, true, needAnimation = false)
            })
        }
        ivMeetup = view.findViewById(R.id.ivMeetup)
        ivPositive = view.findViewById(R.id.ivPositive)
        tvCurrentBadge = view.findViewById(R.id.tvCurrentBadge)
        tabLayout = view.findViewById(R.id.tl_tabs)
        verifyIcon = view.findViewById<ImageView>(R.id.iv_verified)
        rootCo = view.findViewById<CoordinatorLayout>(R.id.rootCo)

//        Utils.showToolTips(requireActivity(), tvWorth, rootCo, Tooltip.BOTTOM, "This is the total of your mined mints and mint cash. Click to view the leaderboard", false) {}
//        Utils.showToolTips(requireActivity(), verifyIcon, rootCo, Tooltip.TOP, "Click to verify your profile and Get the verified blue tick", false) {}
//        Utils.showToolTips(requireActivity(), tvProfileView, rootCo, Tooltip.BOTTOM, "See how many people viewed Your profile ", false) {}

        noPostScreen = view.findViewById(R.id.no_post)
        val toolRoot = view.findViewById<ConstraintLayout>(R.id.toolRoot)
        val toolWorth = view.findViewById<ConstraintLayout>(R.id.toolWorth)
        val toolProfileView = view.findViewById<ConstraintLayout>(R.id.toolProfileView)
        val desc = toolWorth.findViewById<TextView>(R.id.desc)
        val desc2 = toolProfileView.findViewById<TextView>(R.id.desc)
        val ivToolTip = toolProfileView.findViewById<ImageView>(R.id.iv_ttp)
        ivToolTip.setColorFilter(ContextCompat.getColor(requireContext(), R.color.ttpend))
        val cs = ConstraintSet()
        cs.clone(toolProfileView)
        cs.setHorizontalBias(R.id.iv_ttp, 0.8.toFloat())
        cs.applyTo(toolProfileView)
        desc.text = "This is the total of your mined mints and mint cash. Click to view the leaderboard"
        desc2.text = "See how many people viewed Your profile"
        if(!getIsShown("toolRoot")){
            toolRoot.visibility = View.VISIBLE
            toolRoot.setOnClickListener {
                toolRoot.visibility=View.GONE
                toolWorth.visibility = View.VISIBLE
                toolWorth.setOnClickListener{
                    toolWorth.visibility = View.GONE
                    toolProfileView.visibility = View.VISIBLE
                    toolProfileView.setOnClickListener {
                        toolProfileView.visibility = View.GONE
                    }
                }
            }
        }else if(!getIsShown("toolWorth")){
            toolWorth.visibility = View.VISIBLE
            toolWorth.setOnClickListener{
                toolWorth.visibility = View.GONE
                toolProfileView.visibility = View.VISIBLE
                toolProfileView.setOnClickListener {
                    toolProfileView.visibility = View.GONE
                }
            }
        }else if(!getIsShown("toolProfileView")){
            toolProfileView.visibility = View.VISIBLE
            toolProfileView.setOnClickListener {
                toolProfileView.visibility = View.GONE
            }
        }
//        llReward = view.findViewById<LinearLayout>(R.id.ll_reward).apply {
//            setOnClickListener {
//                meetsCredibilityStatusDialog?.showDialog()
//            }
//        }

        llBadge = view.findViewById<LinearLayout>(R.id.llBadge)
        ivBadge = view.findViewById<ImageView>(R.id.ivBadge)
        ivDpBadge = view.findViewById<ImageView>(R.id.ivDpBadge)

        //shimmer = view.findViewById(R.id.shimmerLayout)
        //gpPost = view.findViewById(R.id.gp_post)
        dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        val inflate = layoutInflater.inflate(R.layout.bottomsheet_setting, null)
        dialog.setContentView(inflate)
        dialog.dismissWithAnimation = true
        val tvSetting: TextView = inflate.findViewById(R.id.tvSetting)
        val tvSaved: TextView = inflate.findViewById(R.id.tvSaved)
        // val tvMySafe: TextView = inflate.findViewById(R.id.tvMySafe)
        tvSetting.setOnClickListener(this)
        tvSaved.setOnClickListener(this)
        //tvMySafe.setOnClickListener(this)
        tvWorth.setGradient(requireActivity(), GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#FF7272"), Color.parseColor("#32BFC9")), 20f)
        spotlight1.setOnClickListener { spotlight1Clicked() }
        spotlight2.setOnClickListener { spotlight2Clicked() }
        spotlight3.setOnClickListener { spotlight3Clicked() }

        val params: CoordinatorLayout.LayoutParams = appbar.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = AppBarLayoutBehavior(requireContext(), null)
        appbar.requestLayout()
        tvCreateMeet.onClick({
            MyApplication.putTrackMP(Constant.AcMeetupProfile, null)
            MyApplication.smallVibrate()
            val baseMeetFragment = MeetUpViewPageFragment.getInstance(null, false)
            Navigation.addFragment(requireActivity(), baseMeetFragment, MeetUpViewPageFragment.TAG, R.id.homeFram, true, true)
        })
        tvCreatePost.onClick({
            MyApplication.putTrackMP(Constant.AcCreatePostProfile, null)
            MyApplication.smallVibrate()
            Navigation.addFragment(requireActivity(), CreatePost(), Constant.TAG_CREATE_POST_FRAGMENT, R.id.homeFram, stack = true, needAnimation = false)
        })
        tvProfileView.onClick({
            MyApplication.putTrackMP(Constant.AcViewedMeIcon, null)
            Navigation.addFragment(requireActivity(), ProfileViewFragment(), ProfileViewFragment.TAG, R.id.homeFram, true, false)
        })
        view.findViewById<RelativeLayout>(R.id.rl_back)
                .setOnClickListener { activity?.onBackPressed() }

        mBottomSheetOptions = BottomSheetOptions.getInstance("Camera", "Gallery", null, null, null)
        mBottomSheetOptions?.setBottomSheetLitener(this)

        var layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }

        interest.layoutManager = layoutManager

        loader = LoaderDialog(requireActivity())

    }

    private fun setUp(view: View) {

        var profileResponse = PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)
        profileSetting.visibility = View.VISIBLE
        profileInterestAdapter = ProfileInterestAdapter(requireActivity(), ArrayList())
        interest.adapter = profileInterestAdapter

        profileSetting.setOnClickListener(this)
        interestIcon.setOnClickListener(this)
        postAdapter = PostAdapter(requireActivity(), this, PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE), {
            postViewModel.toggleLike(it)
        }, {
            MyApplication.putTrackMP(AcDeletePost, JSONObject(mapOf("postId" to it)))
            val showProceed = showProceed { postViewModel.deletePost(it) }
            showProceed.setMessage("Delete", "Are you sure you want to delete this post?")
        })

        rvPost.layoutManager = GridLayoutManager(requireContext(), 3)

        postAdapter.setViewtypes(PostAdapter.GRID)
        rvPost.adapter = postAdapter.withLoadStateFooter(TimelineFooterStateAdapter {
            postAdapter.retry()
        })

        tvVaccine.setOnClickListener { gotoUploadVaccine() }

        //-------------------postCountClick---------------------------
        postCount.setOnClickListener {
            appbar.setExpanded(false, true)
            MyApplication.putTrackMP(Constant.AcPostCount, null)
        }
        view.findViewById<TextView>(R.id.tv_post).apply {
            setOnClickListener {
                appbar.setExpanded(false, true)
                MyApplication.putTrackMP(Constant.AcPostCount, null)
            }
        }
        ///-------------------------------------------------------------

        //---------click Follower Count-----------------
        follower.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcFollowerCount, null)
            Log.i(TAG, " sidUsername:: ${profileResponse?.cust_data?.sid} ${profileResponse?.cust_data?.username}")
            var baseFragment: BaseFragment = FollowFollowerFragment()
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.SID, profileResponse?.cust_data?.sid)
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.USERNAME, profileResponse?.cust_data?.username)
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWING_COUNT, profileResponse?.social?.followings_count.toString())
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWER_COUNT, profileResponse?.social?.followers_count.toString())
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.TAB, "0")
            Navigation.addFragment(this.requireActivity(), baseFragment, TAG_FOLLOW_FOLLOWING_FRAGMENT, R.id.homeFram, true, false)
        }

        view.findViewById<TextView>(R.id.tv_follower).apply {
            setOnClickListener {
                Log.i(TAG, " sidUsername:: ${profileResponse?.cust_data?.sid} ${profileResponse?.cust_data?.username}")
                var baseFragment: BaseFragment = FollowFollowerFragment()
                Navigation.setFragmentData(baseFragment, FollowFollowerFragment.SID, profileResponse?.cust_data?.sid)
                Navigation.setFragmentData(baseFragment, FollowFollowerFragment.USERNAME, profileResponse?.cust_data?.username)
                Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWING_COUNT, profileResponse?.social?.followings_count.toString())
                Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWER_COUNT, profileResponse?.social?.followers_count.toString())
                Navigation.setFragmentData(baseFragment, FollowFollowerFragment.TAB, "0")
                Navigation.addFragment(requireActivity(), baseFragment, TAG_FOLLOW_FOLLOWING_FRAGMENT, R.id.homeFram, true, false)
            }
        }
        // -------------------------------------------------------------------------

        //---------click Following Count-----------------
        following.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcFollowingCount, null)
            Log.i(TAG, " sidUsername:: ${profileResponse?.cust_data?.sid} ${profileResponse?.cust_data?.username}")
            var baseFragment: BaseFragment = FollowFollowerFragment()
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.SID, profileResponse?.cust_data?.sid)
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.USERNAME, profileResponse?.cust_data?.username)
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWING_COUNT, profileResponse?.social?.followings_count.toString())
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWER_COUNT, profileResponse?.social?.followers_count.toString())
            Navigation.setFragmentData(baseFragment, FollowFollowerFragment.TAB, "1")
            Navigation.addFragment(requireActivity(), baseFragment, TAG_FOLLOW_FOLLOWING_FRAGMENT, R.id.homeFram, true, false)
        }

        view.findViewById<TextView>(R.id.tv_following).apply {
            setOnClickListener {
                Log.i(TAG, " sidUsername:: ${profileResponse?.cust_data?.sid} ${profileResponse?.cust_data?.username}")
                var baseFragment: BaseFragment = FollowFollowerFragment()
                Navigation.setFragmentData(baseFragment, FollowFollowerFragment.SID, profileResponse?.cust_data?.sid)
                Navigation.setFragmentData(baseFragment, FollowFollowerFragment.USERNAME, profileResponse?.cust_data?.username)
                Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWING_COUNT, profileResponse?.social?.followings_count.toString())
                Navigation.setFragmentData(baseFragment, FollowFollowerFragment.FOLLOWER_COUNT, profileResponse?.social?.followers_count.toString())
                Navigation.setFragmentData(baseFragment, FollowFollowerFragment.TAB, "1")
                Navigation.addFragment(requireActivity(), baseFragment, TAG_FOLLOW_FOLLOWING_FRAGMENT, R.id.homeFram, true, false)
            }
        }

        Utils.onClick(view.findViewById(R.id.iv_interest), 1000) {
            MyApplication.smallVibrate()
            var fragment = InterestFragment.getInstance(TAG, ArrayList(PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?.cust_data?.interests?.map { it?.key }))
            MyApplication.putTrackMP(Constant.AcEditInterest, null)
            Navigation.addFragment(requireActivity(), fragment, InterestFragment.TAG, R.id.homeFram, true, false)
        }

        // ------------------------------------------------------

        populateView()
        profileResponse?.let {
            if(it.social?.posts_count!! > 0) {
                postViewModel.fetchPost(it.social?.sid, GLOBAL_TIMELINE, false)
            }
        }
        addListener()
        addObserver()
        userAccountViewModel.getFullProfile()
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                Log.i(TAG, " tab:: ${tab?.text} ${tabLayout.selectedTabPosition}")
                //if(tab?.text?.equals("Places") == true){
                if(tabLayout.selectedTabPosition == 1) {
                    Log.i(TAG, "  Places:::: ")
                    postAdapter.setViewtypes("linear")
                    MyApplication.putTrackMP(Constant.AcProfileListTab, null)
                    rvPost.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    rvPost.adapter = postAdapter.withLoadStateFooter(TimelineFooterStateAdapter {
                        postAdapter.retry()
                    })
                } else if(tabLayout.selectedTabPosition == 0) {
                    Log.i(TAG, "  Posts:::: ")
                    //rvPost.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
                    MyApplication.putTrackMP(Constant.AcProfileGridTab, null)
                    postAdapter.setViewtypes(PostAdapter.GRID)
                    rvPost.layoutManager = GridLayoutManager(requireContext(), 3)
                    rvPost.adapter = postAdapter.withLoadStateFooter(TimelineFooterStateAdapter {
                        postAdapter.retry()
                    })
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        meetsCredibilityStatusDialog = MeetsCredibilityStatusDialog(requireActivity(), true) { s: String, i: Int ->
            userAccountViewModel.getBadgeRelation(profileResponse?.cust_data?.sid, "followers", s, i)
        }

        // rvPost.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->  }

        /*ivReward.setOnClickListener{
            meetsCredibilityStatusDialog?.showDialog()
        }*/

    }

    fun setFragmentListener() {
        setFragmentResultListener(TAG) { key, result ->
            // get the result from bundle
            val stringResult = result.getStringArrayList("returnInterestKey")
            Log.i(TAG, "setUpResultListener:::  $stringResult ${allInterestList}")
            val selected = allInterestList?.filter { stringResult?.contains(it?.key) == true }
            userAccountViewModel.updateInterest(ArrayList(selected))
        }
    }

    /**
     * rv in Nestedscrollview call all page at same timee
     *this method restring height of recyclerview
     */
    /*private fun setRecyclerViewHeight(view: View) {
        Log.i(TAG, " rootLayout ${view.findViewById<CoordinatorLayout>(R.id.root).height}")
        view.findViewById<CoordinatorLayout>(R.id.root).height
        var param = rvPost.layoutParams
        param.height = view.findViewById<CoordinatorLayout>(R.id.root).height
        //param.height = 1080
        rvPost.layoutParams = param

    }*/

    private fun spotlight3Clicked() {
        if(isSpotlight3Present) {
            Log.i(TAG, "SpotLight3 Present")
            Log.i(TAG, "SpotLight1 Present")
            var url = getSpotLightUrl(3)
            var baseFragment: BaseFragment = ProfileEditPicPage()
            baseFragment = Navigation.setFragmentData(baseFragment, TYPE, SPOTLIGHT3)
            baseFragment = Navigation.setFragmentData(baseFragment, IMAGE_URL, url)
            baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.PROFILE_TYPE, ProfileEditPicPage.OWN)
            Navigation.addFragment(requireActivity(), baseFragment, "ProfileEditPicPage", R.id.homeFram, true, false)

        } else {
            intentFrom = FROM_SPOTLIGHT3
            showBottomSheetDialogFragment()
        }
    }

    private fun spotlight2Clicked() {
        if(isSpotlight2Present) {
            Log.i(TAG, "SpotLight2 Present")
            Log.i(TAG, "SpotLight1 Present")
            var url = getSpotLightUrl(2)
            var baseFragment: BaseFragment = ProfileEditPicPage()
            baseFragment = Navigation.setFragmentData(baseFragment, TYPE, SPOTLIGHT2)
            baseFragment = Navigation.setFragmentData(baseFragment, IMAGE_URL, url)
            baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.PROFILE_TYPE, ProfileEditPicPage.OWN)
            Navigation.addFragment(requireActivity(), baseFragment, "ProfileEditPicPage", R.id.homeFram, true, false)

        } else {
            intentFrom = FROM_SPOTLIGHT2
            showBottomSheetDialogFragment()
        }
    }

    private fun spotlight1Clicked() {
        if(isSpotlight1Present) {
            Log.i(TAG, "SpotLight1 Present")
            var url = getSpotLightUrl(1)
            var baseFragment: BaseFragment = ProfileEditPicPage()
            baseFragment = Navigation.setFragmentData(baseFragment, TYPE, SPOTLIGHT1)
            baseFragment = Navigation.setFragmentData(baseFragment, IMAGE_URL, url)
            baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.PROFILE_TYPE, ProfileEditPicPage.OWN)
            Navigation.addFragment(requireActivity(), baseFragment, "ProfileEditPicPage", R.id.homeFram, true, false)

        } else {
            intentFrom = FROM_SPOTLIGHT1
            showBottomSheetDialogFragment()
        }
    }


    private fun gotoUploadVaccine() {
        Navigation.addFragment(requireActivity(), UploadVaccineFragment(), TAG_UPLOADVACCINE_FRAGMENT, R.id.homeFram, true, false)
    }

    private fun addListener() {
        camera.onClick({
            intentFrom = FROM_CAMERA
            showBottomSheetDialogFragment()
        })
        /*compositeDisposable.add(Utils.onClick(camera, 2000) {
            intentFrom = FROM_CAMERA
            showBottomSheetDialogFragment()
        })*/
        /*RxView.clicks(camera).throttleFirst(2, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer {
                intentFrom = FROM_CAMERA
                showBottomSheetDialogFragment()
            })*/
    }

    private fun addObserver() {
        userAccountViewModel.getBadgeRelation().observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.d(TAG, "addObserver: itmap: $it")
                meetsCredibilityStatusDialog?.setData(it)
            }
        })

        userAccountViewModel.observeFullProfileChange().observe(viewLifecycleOwner, Observer {

            when(it) {
                is ResultHandler.Success -> {
                    it.value?.let {
                        Log.i(TAG, "Response:::: $it")
                        PreferencesManager.put(it, PREFRENCE_PROFILE)
//                        (activity as MainActivity?)?.populateData()
                        populateView()
                    }
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })

        userAccountViewModel.observeSpotlightChange().observe(viewLifecycleOwner, Observer {
            it?.let {
                loader.hideDialog()
                Log.i(TAG, " urlCame:: ${it}")
                PreferencesManager.put(it, PREFRENCE_PROFILE)
                populateView()
                //postAdapter.notifyItemChanged(0)
            }
        })

        userAccountViewModel.observeInterestChange().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "Interest saved!!!")
                    Log.i(InterestFragment.TAG, " MuProfile:: ${it}")
                    //PreferencesManager.put(it.value, Constant.PREFRENCE_PROFILE)
                    userAccountViewModel.getFullProfile()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })

        postViewModel.observePostChange().observe(viewLifecycleOwner, Observer {
            postAdapter.submitData(lifecycle, it)

        })

        postViewModel.onCommentStatusUpdate().observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    postAdapter.refresh()
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

        postViewModel.observePostDelete().observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
            postAdapter.refresh()
        })

        userAccountViewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity, it)
        })

        postViewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity, it)
        })
    }

    fun populateView() {
        var it = PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)
//        (activity as MainActivity?)?.populateData()
        postAdapter.notifyItemChanged(0)
        Log.i(TAG, " profileFragment:::Profile:  $it")
        it?.let { response ->
            //vaccine Status
            response.social?.getbadge()?.let { badge: String ->
                val firstOrNull = Utils.getBadge(badge)
                llBadge.setBackgroundResource(firstOrNull.background)
                ivBadge.setImageResource(firstOrNull.foreground)
                ivDpBadge.setImageResource(firstOrNull.foreground)
                tvCurrentBadge.text = firstOrNull.name
                tvCurrentLevel.text = "Level ${firstOrNull.level}"
            }
            response.social?.getmints()?.let {
                val prettyCount = Utils.prettyCount(it.toDouble())
                tvWorth.text = "Worth: $prettyCount mints"
            } ?: run {
                tvWorth.text = "Worth: 0 mints"
            }
            tvWorth.onClick({
                MyApplication.putTrackMP(Constant.AcWorthIcon, null)
                navigate(LeaderBoardFragment())
            })
            response.social?.meetup_attendance_count?.let {
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

            val positiveDialog = ExperienceDialog(requireActivity(), response.social?.meetup_positive_experience_count ?: 0, 1)
            llPositive.onClick({
                MyApplication.putTrackMP(Constant.AcProfilePositiveExp, null)
                positiveDialog.show()
            })
            val meetupDialog = ExperienceDialog(requireActivity(), response.social?.meetup_attendance_count ?: 0, 0)
            llMeetups.onClick({
                MyApplication.putTrackMP(Constant.AcProfileMeetCount, null)
                meetupDialog.show()
            })
            response.social?.meetup_positive_experience_count?.let {
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

            val d = meetsCredibilityStatusDialog?.initDialog(badge = Utils.getBadge(response.social?.getbadge()), mints = response.social?.getmints())
            llBadge.setOnClickListener {
                MyApplication.putTrackMP(Constant.AcMeetsCredibility, null)
                d?.show()
            }

            Log.d(TAG, "populateView:response.cust_data  ${response.cust_data}")
            if(response.cust_data?.document_stage_status?.missing_documents?.isNotEmpty() == true) {
                tvVaccine.text = "Upload your vaccination card"
                tvVaccine.setOnClickListener { gotoUploadVaccine() }
                tvVaccine.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circular_corcnor_filled2)
            } else if(response.cust_data?.document_stage_status?.pending_verifications?.isNotEmpty() == true) {
                tvVaccine.text = "Verification pending"
                //tvVaccine.setOnClickListener(null)
                tvVaccine.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verification_pend, 0)
                tvVaccine.setTextColor(ContextCompat.getColor(requireContext(), R.color.pending_document_text))
                tvVaccine.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circular_corner_pending)
            } else if(response.cust_data?.document_stage_status?.rejected_documents?.isNotEmpty() == true) {
                tvVaccine.text = "Vaccination card rejected"
                tvVaccine.setOnClickListener { gotoUploadVaccine() }
                tvVaccine.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circular_corner_rejected)
            } else {
                tvVaccine.text = "Vaccination card accepted"
                tvVaccine.setOnClickListener { gotoUploadVaccine() }
                tvVaccine.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_circular_corner_accepted)
            }



            Log.i(TAG, "Response:: ${response.toString()}")
            //name.text = response.cust_data.first_name ?: response.cust_data.username
            name.text = response.cust_data?.username
            verifyIcon.onClick({
                Log.i(TAG, "populateView: cliclje ${it.cust_data?.profile_image_url}")
                MyApplication.putTrackMP(Constant.AcVerifiedIcon, null)
                it.cust_data?.profile_image_url?.let {
                    /*if(response.cust_data?.verified_user == false)*/ Navigation.addFragment(requireActivity(), VerificationPagerFragment(), VerificationPagerFragment.TAG, R.id.homeFram, true, true)
                } ?: run {
                    MyApplication.showToast(requireActivity(), "First upload profile picture...")
                }
            }, 1000)
            if(response.cust_data?.verified_user == true)
                verifyIcon.setImageResource(R.drawable.ic_verified_tick)
            else
                verifyIcon.setImageResource(R.drawable.ic_unverified)
            //response.cust_data?.profile_image_url?.let { url ->
                //Log.i(TAG, " stickUrlOnGlide:: ${url}")
                //Utils.stickImage(requireContext(), dp, url, null)

            //}
            dp.loadImage(requireContext(),response.cust_data?.profile_image_url,R.drawable.ic_no_profile,profileHolder = R.drawable.ic_no_profile)
//            dp.setImageResource(R.drawable.ic_no_profile)
            //response?.social?.wallpaper_url?.let {
            profile_pic.loadImage(requireContext(), response?.social?.wallpaper_url, R.drawable.ic_person_placeholder, showSimmer = true)
            //}
            dp.onClick({
                MyApplication.putTrackMP(Constant.AcProfileImage, null)
                Navigation.addFragment(requireActivity(), EditProfile(), TAG_EDIT_PROFILE, R.id.homeFram, stack = true, needAnimation = false)
            })
            profile_pic.onClick({
                MyApplication.putTrackMP(Constant.AcCoverImage, null)
                var baseFragment: BaseFragment = ProfileEditPicPage()
                baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.TYPE, ProfileEditPicPage.COVER_PIC)
                baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.IMAGE_URL, response?.social?.wallpaper_url)
                baseFragment = Navigation.setFragmentData(baseFragment, ProfileEditPicPage.PROFILE_TYPE, ProfileEditPicPage.OWN)
                Navigation.addFragment(requireActivity(), baseFragment, "ProfileEditPicPage", R.id.homeFram, true, false)
            })
            response.social?.bio?.let {
                bio.text = response.social?.bio
                bioDesc.visibility = View.GONE
            } ?: run {
                bio.text = "Your Bio tells people about who you are, what you do.  You  are free to express yourself any how you want. Click the icon to edit your Bio"
            }
            follower.text = Utils.prettyCount(response.social?.followers_count)
            following.text = Utils.prettyCount(response.social?.followings_count)
            postCount.text = Utils.prettyCount(response.social?.posts_count)

            //interests
            Log.i(TAG, " totalInterest:: ${response.cust_data?.interests}")
            if(response.cust_data?.interests?.size?.compareTo(0) == 1) {
                interest.visibility = View.VISIBLE
                tvinterest.visibility = View.GONE
            } else {
                interest.visibility = View.GONE
                tvinterest.visibility = View.VISIBLE
            }

            interest.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(2f, resources), Utils.dpToPx(1f, resources)))
            profileInterestAdapter?.setInterest(ArrayList(response.cust_data?.interests))
            //interest.adapter = ProfileInterestAdapter(requireActivity(), ArrayList(response.cust_data?.interests))

            //spotLight
            isSpotlight1Present = false
            isSpotlight2Present = false
            isSpotlight3Present = false
            spotlight1.setImageDrawable(null)
            spotlight2.setImageDrawable(null)
            spotlight3.setImageDrawable(null)
            Log.i(TAG, " spotlight:: ${response.social?.spotlights}")

            if(response.social?.spotlights_count?.compareTo(0) == 1) {

            }
            for(item in response.social?.spotlights ?: ArrayList<Spotlight>()) {
                item?.one?.let {
                    isSpotlight1Present = true
                    Utils.stickImage(requireContext(), spotlight1, it, null)
                }
                item?.two?.let {
                    isSpotlight2Present = true
                    Utils.stickImage(requireContext(), spotlight2, it, null)
                }
                item?.three?.let {
                    isSpotlight3Present = true
                    Utils.stickImage(requireContext(), spotlight3, it, null)
                }
                Log.i(TAG, " ${item.toString()}")
            }

            Log.i(TAG, " postCount:: ${response.social?.posts_count}")
            if(response.social?.posts_count == 0) {

            } else {

            }
        }
    }

    private fun getSpotLightUrl(i: Int): String? {
        PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?.social?.spotlights?.let {
            for(item in it) {
                when(i) {
                    1 -> item?.one?.let { return@getSpotLightUrl it }
                    2 -> item?.two?.let { return@getSpotLightUrl it }
                    3 -> item?.three?.let { return@getSpotLightUrl it }
                }
            }
        }
        return null
    }

    override fun onClick(v: View?) {

        when(v?.id) {
            R.id.iv_profile_setting -> {
                MyApplication.putTrackMP(Constant.AcMoreIcon, null)
                dialog.show()
            }

            R.id.tvSetting          -> {
                dialog.dismiss()
                Navigation.addFragment(requireActivity(), EditProfile(), TAG_EDIT_PROFILE, R.id.homeFram, stack = true, needAnimation = false)
            }

            R.id.tvSaved            -> {
                dialog.dismiss()
                Navigation.addFragment(requireActivity(), SavedPlaceListFragment(), "SavedPlaceFragment", R.id.homeFram, true, needAnimation = false)
            }

            /*R.id.tvMySafe           -> {
                dialog.dismiss()
                Navigation.addFragment(requireActivity(), MySafeFragment(), Constant.TAG_MYSAFE_FRAGMENT, R.id.homeFram, true, needAnimation = false)
            }*/
            /*R.id.iv_interest -> {
                Log.i("TAG", " Clicked:: 234")
                MyApplication.smallVibrate()
                (activity as MainActivity).profileViewmodel.getFullGenericList("interests")
            }*/
        }
    }

    fun showBottomSheetDialogFragment() {
        var fragment = mBottomSheetOptions as BottomSheetDialogFragment?
        if(fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(childFragmentManager, fragment.tag)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun bottomSheetClickedOption(buttonClicked: String) {
        when(buttonClicked) {
            BottomSheetOptions.BUTTON1 -> {
                Log.i(TAG, "Camera choosed")

                if(Utils.checkReadWritePermission(requireContext())) {
                    getImageFromCamera()
                } else {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), 1001)
                }
            }

            BottomSheetOptions.BUTTON2 -> {
                Log.i(TAG, "Gallery choosed")
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                startActivityForResult(Intent.createChooser(photoPickerIntent, "select Camera"), GALLERY_IMAGE_REQUEST)

            }
        }
    }

    private fun getImageFromCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        image_uri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        Log.i(TAG, "image_uri:: ${image_uri?.path}  ${image_uri}")
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, CAMERA_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, " ActivityResult:::: ${resultCode} $data")
        if(requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            loader.showDialog()
            loader.showPercent()

            // Log.i(TAG,"image_uri::  2 ${image_uri?.path}  ${image_uri}")
            Utils.uriToBitmap(image_uri, requireContext())?.let { bitmap ->
                intentFrom?.let {
                    var spotligh = ArrayList<Spotlight?>(PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?.social?.spotlights)
                    Log.i(TAG, " checkIndex:: $it")
                    when(it) {
                        FROM_CAMERA     -> userAccountViewModel.updateProfilePicture(bitmap)
                        FROM_SPOTLIGHT1 -> userAccountViewModel.updateSpotLightPicture(bitmap, spotligh, 1)
                        FROM_SPOTLIGHT2 -> userAccountViewModel.updateSpotLightPicture(bitmap, spotligh, 2)
                        FROM_SPOTLIGHT3 -> userAccountViewModel.updateSpotLightPicture(bitmap, spotligh, 3)
                    }
                }

            } ?: run {
                loader.hideDialog()
            }
            image_uri = null


        } else if(requestCode == GALLERY_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                val imageUri: Uri? = data?.data
                val imageStream: InputStream? = requireActivity().contentResolver.openInputStream(imageUri!!)
                val selectedImage: Bitmap = BitmapFactory.decodeStream(imageStream)
                loader.showDialog()
                loader.showPercent()
                intentFrom?.let {
                    Log.i(TAG, " spotligh:: 000 ${PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?.social?.spotlights}")
                    var spotligh = ArrayList<Spotlight?>(PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?.social?.spotlights)
                    Log.i(TAG, " spotligh:: ${spotligh}")
                    Log.i(TAG, " checkgallaryIndex:: $it")
                    when(it) {
                        FROM_CAMERA     -> userAccountViewModel.updateProfilePicture(selectedImage)
                        FROM_SPOTLIGHT1 -> userAccountViewModel.updateSpotLightPicture(selectedImage, spotligh, 1)
                        FROM_SPOTLIGHT2 -> userAccountViewModel.updateSpotLightPicture(selectedImage, spotligh, 2)
                        FROM_SPOTLIGHT3 -> userAccountViewModel.updateSpotLightPicture(selectedImage, spotligh, 3)
                    }
                }
            } catch(e: Exception) {
                loader.hideDialog()
                Log.e(TAG, "Something went wrong to pick image ${e}")
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when(requestCode) {
            1001 -> {
                var read = grantResults[0] == PackageManager.PERMISSION_GRANTED
                var write = grantResults[1] == PackageManager.PERMISSION_GRANTED
                var camera = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if(grantResults.isNotEmpty() && read && write && camera) {
                    //selectImage()
                    getImageFromCamera()
                } else if(Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) && !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    rationale()
                } else {
                    /*requestPermissions(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                        ), 1001
                    )*/
                }
            }


        }
    }

    override fun onResume() {
        super.onResume()
        //shimmer.startShimmer()
    }

    override fun onPause() {
        //shimmer.stopShimmer()
        super.onPause()
    }

    override fun onBackPageCome() {
        populateView()
    }


}