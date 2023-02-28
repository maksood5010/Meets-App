package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.fenchtose.tooltip.Tooltip
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.MapCategotyAdapter
import com.meetsportal.meets.adapter.MapInterestAdapter
import com.meetsportal.meets.adapter.ProfileInterestAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.CredibilityLayoutBinding
import com.meetsportal.meets.databinding.FragmentMeetUpOpenMapBinding
import com.meetsportal.meets.models.MapUserResponse
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.places.Category
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.places.FetchPlacesResponse
import com.meetsportal.meets.networking.profile.Definition
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.ui.dialog.ExperienceDialog
import com.meetsportal.meets.ui.dialog.MeetsCredibilityStatusDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.PREFRENCE_CATEGORY
import com.meetsportal.meets.utils.Constant.PREFRENCE_PROFILE
import com.meetsportal.meets.utils.Constant.VwMapViewed
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.PlacesViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class MeetupOpenMemberMapFragment : BaseFragment(), OnMapReadyCallback,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnCameraMoveCanceledListener, GoogleMap.OnCameraIdleListener,
    GoogleMap.OnCameraMoveStartedListener {

    lateinit var placeSwipe: GestureDetector
    lateinit var peopleSwipe: GestureDetector
    lateinit var placeDisposable: Disposable
    lateinit var peopleDisposable: Disposable

    //----------------BottomSheetUI------------------//
    lateinit var bsdp: ImageView
    //lateinit var bsverify: ImageView
    var isSpotlight1Present = false
    var isSpotlight2Present = false
    var isSpotlight3Present = false
    lateinit var mapFragment: SupportMapFragment
    var bsMapMarker: Marker? = null
    lateinit var mDetector: GestureDetector
    //----------------BottomSheetUI------------------//

    private lateinit var meetsCredibilityStatusDialog: MeetsCredibilityStatusDialog

    private val TAG = MeetupOpenMemberMapFragment::class.java.simpleName!!
    lateinit var renderer: MarkerClusterRenderer<MarkerClusterItem>
    var previorMarker: Marker? = null
    var previosItem: MarkerClusterItem? = null
    var gMap: GoogleMap? = null
    //lateinit var rvInterest: RecyclerView
    var mapInterestAdapter: MapInterestAdapter? = null
    var mapCategoryAdapter: MapCategotyAdapter? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    lateinit var bottomSheet: ConstraintLayout
    var clusterManager: ClusterManager<MarkerClusterItem>? = null


    val placeViewModel: PlacesViewModel by viewModels()
    val profileViewModel: UserAccountViewModel by viewModels()
    var locationFinder: LocationFinder? = null
    var myLocation: Location? = null
    var otherUserLocation: Location? = null
    var interestList = arrayListOf<Definition?>()
    var allInterestList = arrayListOf<Definition?>()
    var categoryList: List<Category>? = arrayListOf<Category>()
    var shouldFetchUser = false

    var subject = PublishSubject.create<MarkerClusterItem>()
    private val disposable = CompositeDisposable()



    var intrestPublish = PublishSubject.create<Int?>()

    private var _binding: FragmentMeetUpOpenMapBinding? = null
    private val binding get() = _binding!!
    private var _credility : CredibilityLayoutBinding? = null
    private val credility get() = _credility!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       // return inflater.inflate(R.layout.fragment_meet_up_open_map, container, false)
        Log.i(TAG," chencking::: 1 $inflater")
        Log.i(TAG," chencking::: 2 $container")

        _binding = FragmentMeetUpOpenMapBinding.inflate(inflater, container, false)
        _credility = CredibilityLayoutBinding.bind(binding.root)
        Log.i(TAG," chencking::: 4 $_binding")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MyApplication.putTrackMP(VwMapViewed,null)
        initView(view)
        setUp()
        setObserver(view)
        subscribeSubject()

    }


    private fun initView(view: View) {

        bottomSheet = view.findViewById(R.id.bottomSheet)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        binding.btSheet.tvVaccineStatus.visibility = View.GONE

        binding.btSheet.tvWorth.setGradient(requireActivity(), GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(
            Color.parseColor("#FF7272"), Color.parseColor("#32BFC9")),20f)
        view.findViewById<ImageView>(R.id.iv_profile_setting).apply { visibility = View.GONE }

        binding.btSheet.message.visibility = View.GONE

        mDetector = GestureDetector(requireContext(), MyGestureListener({
            if (it == MyGestureListener.Direction.up && renderer.isItPlace == false)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }, {}))
        placeSwipe = GestureDetector(requireContext(), MyGestureListener({
            Log.i(TAG, " checkingSwipeEvnet::: ")
            when (it) {
                MyGestureListener.Direction.left -> pleopleTabClicked()
            }
        }, { placeTabClicked() }))

        peopleSwipe = GestureDetector(requireContext(), MyGestureListener({
            Log.i(TAG, " checkingSwipeEvnet::: ")
            when (it) {
                //MyGestureListener.Direction.right -> placeTab.performClick()
                MyGestureListener.Direction.right -> placeTabClicked()
            }
        }, {
            pleopleTabClicked()
        }))
        binding.rvInterests.setOnTouchListener(Utils.getTouchlistener(mDetector))
        val mapOptions = GoogleMapOptions().apply {
            zoomControlsEnabled(false)
            zoomGesturesEnabled(false)
            ambientEnabled(false)
            rotateGesturesEnabled(false)
            tiltGesturesEnabled(false)
            scrollGesturesEnabled(false)
            compassEnabled(false)
            liteMode(true)
        }
        mapFragment = SupportMapFragment.newInstance(mapOptions)
        childFragmentManager.commit {
            add(R.id.map2, mapFragment)
        }

    }

    private fun setUp() {
        val MapFragment: SupportMapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        MapFragment.getMapAsync(this)
        PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?:return
        profileViewModel.getOtherProfile(PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?.cust_data?.sid)


        disposable.add(intrestPublish
            .debounce(500,TimeUnit.MILLISECONDS)
            .subscribe({ position ->
            profileViewModel.getMapUsers(LatLng(myLocation!!.latitude, myLocation!!.longitude), interestList.filter { it?.selected == true }
                .map { it?.key }.joinToString(","))
        },{

        }))
        binding.rvInterests.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        binding.btSheet.rvInterest.layoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW;
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }

        categoryList =
            PreferencesManager.get<CategoryResponse>(Constant.PREFRENCE_CATEGORY)?.definition

        mapCategoryAdapter = MapCategotyAdapter(requireActivity(), categoryList) {
            MyApplication.putTrackMP(Constant.AcMapCategories, JSONObject(mapOf("interestKey" to it?.key)))
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            Log.i(
                TAG,
                " primaryPlace:: ${
                    categoryList?.filter { it?.isSelected == true }?.map { it?.key }
                        ?.joinToString(",")
                }"
            )
            placeViewModel.getCloseByPlaceUp(
                20,
                gMap?.cameraPosition?.target?.latitude,
                gMap?.cameraPosition?.target?.longitude,
                null,
                "location,name,primary_kind_of_place,_id",
                1000,
                filter = categoryList?.filter { it?.isSelected == true }?.map { it?.key }
                    ?.joinToString(",")
            )
        }
    }

    private fun setObserver(view: View) {
        profileViewModel.observeFullInterestChange()
            .observe(viewLifecycleOwner, Observer { interestResponse ->

                when(interestResponse){
                    is ResultHandler.Success -> {
                        Log.i(TAG," interestResponse:: ${interestResponse}")
                        allInterestList.clear()
                        allInterestList.addAll(interestResponse?.value?.definition!!)
                        PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?.cust_data?.interests?.let { myInterest ->
                            interestList.apply {
                                clear()
                                addAll(allInterestList.filter { it?.svg_url!=null })
                                var list = interestResponse.value?.definition.filter { myInterest.contains(it?.key) }
                                removeAll(list)
                                list.forEach {
                                    add(0, it)
                                }
                            }
                            Log.i(TAG," interestResponse:: 2 ${interestList}")
                        }
                        Log.i(TAG, " MapInterest :: $interestList")
                        bottomSheetBehavior.halfExpandedRatio = 0.3f
                        bottomSheetBehavior.isDraggable = true
                        interestList.add(Definition("", "", "", "", ""))
                        mapInterestAdapter = MapInterestAdapter(requireActivity(), interestList) { position ->
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                if(position!=null){
                                    MyApplication.putTrackMP(Constant.AcMapInterest, JSONObject(mapOf("interestKey" to interestList.getOrNull(position)?.key)))
                                    interestList.getOrNull(position)?.selected?.let {
                                        interestList.getOrNull(position)?.selected = !it
                                    }?:run{
                                        interestList.getOrNull(position)?.selected = true
                                    }
                                    mapInterestAdapter?.notifyDataSetChanged()
                                    intrestPublish.onNext(position!!)
                                }else{
                                    val interest = InterestFragment.getInstance(TAG, ArrayList(interestList.filter { it?.selected == true }.map { it?.key }))
                                    Navigation.addFragment(requireActivity(),interest,InterestFragment.TAG,R.id.homeFram,true,false)
                                }
                        }
                        binding.rvInterests.adapter = mapInterestAdapter
                        setFragmentResultListener(TAG) {key, result->
                            // get the result from bundle
                            val stringResult = result.getStringArrayList("returnInterestKey")
                            Log.i(TAG, "setUpResultListener:::  $stringResult")
                            val selectedInterest = allInterestList?.filter { stringResult?.contains(it?.key) == true }.map { it.apply { it?.selected = true } }
                            val tempPicWithFalse = allInterestList.filter { it?.svg_url != null && stringResult?.contains(it?.key) == false }.map { it.apply { it?.selected = false } }

                            interestList.clear()
                            interestList.addAll(selectedInterest)
                            interestList.addAll(tempPicWithFalse)

//                            interestList
                            /*selectedInterest.map{
                                it?.apply {
                                    selected=true

                                }
                            }*/
                            //interestList.filter { stringResult?.contains(it?.key) == true }
                            //interestList.addAll(selectedInterest)
                            mapInterestAdapter?.notifyDataSetChanged()
                            profileViewModel.getMapUsers(LatLng(myLocation!!.latitude, myLocation!!.longitude), interestList.filter { it?.selected == true }
                                    .map { it?.key }.joinToString(","))

                        }
                        Log.i(TAG," showingGuidence::: ")
                        // Utils.showGuide(requireActivity(),rvInterest,"Matching Interests"
                        //,"Choose interests to see only people\nwho share same interests as you")

                        Utils.showToolTips(requireActivity(),binding.rvInterests,binding.tooltipView,Tooltip.TOP
                            ,"Choose interests to see only people\nWho share same interests as you","rvInterests"){
                            Utils.showToolTips(requireActivity(),binding.tab,binding.tooltipView,Tooltip.BOTTOM,
                                    "Switch between places and people in the map","tab"){
                                Utils.showToolTips(requireActivity(),binding.llsearch,binding.tooltipView,Tooltip.LEFT,
                                    "Search area for\npeople and places","llsearch"){
                                }
                            }
                        }
                    }
                    is ResultHandler.Failure -> {
                        Log.i(TAG," Something went wrong while fetching interest")
                    }
                }
            })

        profileViewModel.observeMapUsers().observe(viewLifecycleOwner, Observer {
            when(it){
                is ResultHandler.Success ->{
                    Log.i(TAG, "checkingMapUserResponse::: ${it?.value?.size}")
                    Utils.popUpVisibilityVisible(binding.count, 300)
                    Utils.fadeVisibilityGone(binding.llsearch, 100)
//            it.filter { it.hid }
                    if (it?.value?.size != 0)
                        binding.count.text = it?.value?.size.toString().plus(" people are available for Meetup ")
                    else
                        binding.count.text = "".plus(" No people is available for Meetup ")
                    binding.searchProgress.visibility = View.GONE
                    try {
                        gMap?.clear()
                    } catch (e: Exception) {
                        Log.i(TAG, " gMap?.clear()::: 1 ${e.printStackTrace()}")
                    }
                    clusterManager?.clearItems()
                    if(binding.peopleTab.background != null)
                        mapUserToMarker(it.value)
                }
                is ResultHandler.Failure ->{
                    Log.e(TAG," Get Map User APi failed")
                }
            }

        })

        placeViewModel.closeByPlace().observe(viewLifecycleOwner, Observer {
            Utils.popUpVisibilityVisible(binding.count, 300)
            Utils.fadeVisibilityGone(binding.llsearch, 100)
            if (it.size != 0)
                binding.count.text = "Found ".plus(it.size).plus(" places")
            else
                binding.count.text = "".plus(" No place is available for Meetup ")
            binding.searchProgress.visibility = View.GONE
            try {
                gMap?.clear()
            } catch (e: Exception) {
                Log.i(TAG, " gMap?.clear()::: 1 ${e.printStackTrace()}")
            }
            clusterManager?.clearItems()
            Log.i(TAG, " NearBy places::: 0 $it")
            Log.i(
                TAG,
                " NearBy places::: 1 ${
                    PreferencesManager.get<CategoryResponse>(PREFRENCE_CATEGORY)?.definition
                }"
            )
            var category =
                PreferencesManager.get<CategoryResponse>(PREFRENCE_CATEGORY)?.definition
            if(binding.placeTab.background != null)
                mapPlaceToMarker(it, category)
        })

        profileViewModel.observerFullOtherProfileChange().observe(viewLifecycleOwner, Observer {
            when(it){
                is ResultHandler.Success -> {
                    it.value?.let {
                        Log.i(TAG, "getOtherProfile: sid-- ${it?.cust_data?.sid}")
                        if(it?.social?.sid == MyApplication.SID){
                            binding.rlCreateMeetUp.setRoundedColorBackground(requireActivity(),R.color.extraLightGray)
                            binding.rlCreateMeetUp.setOnClickListener(null)
                            binding.btSheet.tvCreateMeet.setOnClickListener(null)
                            binding.btSheet.tvCreateMeet.setOnClickListener(null)
                        }
                        else{
                            binding.rlCreateMeetUp.setRoundedColorBackground(requireActivity(),R.color.primaryDark)
                            binding.rlCreateMeetUp.onClick({
                                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                                MyApplication.putTrackMP(Constant.AcMapProfileInviteMeet, JSONObject(mapOf("sid" to it?.cust_data?.sid)))
                                var baseFragment: BaseFragment = MeetUpViewPageFragment.getInstance(it?.social?.sid,false)
                                Navigation.addFragment(
                                    requireActivity(),
                                    baseFragment,
                                    MeetUpViewPageFragment.TAG,
                                    R.id.homeFram,
                                    true,
                                    true
                                )
                            })
                            binding.btSheet.tvCreateMeet.onClick({
                                MyApplication.putTrackMP(Constant.AcMapProfileMeetup, JSONObject(mapOf("sid" to it?.cust_data?.sid)))
                                var baseFragment: BaseFragment = MeetUpViewPageFragment.getInstance(it?.social?.sid,false)
                                MyApplication.smallVibrate()
                                Navigation.addFragment(
                                    requireActivity(),
                                    baseFragment,
                                    MeetUpViewPageFragment.TAG,
                                    R.id.homeFram,
                                    true,
                                    true)
                            })
                        }

                        if(it?.cust_data?.interests?.size == 0) {
                            binding.btSheet.rvInterest.visibility = View.GONE
                            binding.btSheet.tvInterest.visibility = View.GONE
                        }else{
                            binding.btSheet.rvInterest.visibility = View.VISIBLE
                            binding.btSheet.tvInterest.visibility = View.VISIBLE
                        }
                        it?.social?.getmints()?.let {
                            val prettyCount = Utils.prettyCount(it.toDouble())
                            binding.btSheet.tvWorth.text = "Worth: $prettyCount mints"
                        }

                        //Utils.stickImage(requireContext(), binding.btSheet.civImage, it?.cust_data?.profile_image_url, null)
                        binding.btSheet.civImage.loadImage(requireContext(),it?.cust_data?.profile_image_url,R.drawable.ic_default_person)
                        if (it?.cust_data?.verified_user == true) binding.btSheet.ivVerified.visibility =
                            View.VISIBLE else binding.btSheet.ivVerified.visibility = View.GONE


                        meetsCredibilityStatusDialog = MeetsCredibilityStatusDialog(requireActivity(), false) { s: String, i: Int ->
                        }
                        meetsCredibilityStatusDialog?.initDialog(badge = Utils.getBadge(it?.social?.badge), mints = it?.social?.getmints())
                        it?.social?.badge?.let { badge: String ->
                            val firstOrNull = Utils.getBadge(badge)
                            credility.llBadge.setBackgroundResource(firstOrNull.background)
                            credility.ivBadge.setImageResource(firstOrNull.foreground)
                            binding.btSheet.ivDpBadge.setImageResource(firstOrNull.foreground)
                            credility.tvCurrentBadge.text = firstOrNull.name
                            credility.tvCurrentLevel.text = "Level ${firstOrNull.level}"
                        }

                        credility.llBadge.onClick( {
                            MyApplication.putTrackMP(Constant.AcMapProfileBadge, JSONObject(mapOf("sid" to it?.cust_data?.sid)))
                            meetsCredibilityStatusDialog.show()
                        })
                        Log.i(TAG," checkingCount:: ${it?.social?.posts} -- ${it?.social?.followers} -- ${it?.social?.followings}")
                        binding.btSheet.tvUsername.text = it?.cust_data?.username
                        binding.btSheet.tvPostCount.text = it?.social?.posts_count.toString()
                        binding.btSheet.tvFollowCount.text = it?.social?.followers_count.toString()
                        binding.btSheet.tvFollowingCount.text = it?.social?.followings_count.toString()
//                        binding.btSheet.civImage.setOnClickListener { view -> openOtherProfile(it?.cust_data?.sid) }
                        binding.btSheet.civImage.onClick( {
                            MyApplication.putTrackMP(Constant.AcMapProfileImage, JSONObject(mapOf("sid" to it?.cust_data?.sid)))
                            openProfile(it?.cust_data?.sid,Constant.Source.MAP.sorce.plus(MyApplication.SID))
                        })
//                        binding.btSheet.tvUsername.setOnClickListener { view -> openOtherProfile(it?.cust_data?.sid) }
                        binding.btSheet.tvUsername.onClick( {
                            MyApplication.putTrackMP(Constant.AcMapProfileUserName, JSONObject(mapOf("sid" to it?.cust_data?.sid)))
                            openProfile(it?.cust_data?.sid,Constant.Source.MAP.sorce.plus(MyApplication.SID))
                        })
                        binding.btSheet.tvBio.text = it?.social?.bio
                        binding.btSheet.rvInterest.adapter =
                            ProfileInterestAdapter(requireActivity(), ArrayList(it?.cust_data?.interests))
                        binding.progress.progress = it?.social?.matching_interests?.toInt()?:0
                        binding.matching.text = it?.social?.matching_interests?.toInt().toString().plus("%")
                        if (it?.social?.followed_by_you == true) {
                            binding.btSheet.tvFollow.text = "Unfollow"
                            binding.btSheet.tvFollow.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.primaryDark
                                )
                            )
                            var bg = ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.horizontal_round_shape
                            ) as GradientDrawable
                            binding.btSheet.tvFollow.setBackgroundDrawable(bg)
                        } else {
                            binding.btSheet.tvFollow.text = "Follow"
                            binding.btSheet.tvFollow.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.white
                                )
                            )
                            binding.btSheet.tvFollow.background =
                                ContextCompat.getDrawable(requireContext(), R.drawable.round_border_primary_bg)
                        }

                        Utils.onClick(binding.btSheet.tvFollow, 500) {
                            MyApplication.putTrackMP(Constant.AcMapProfileFollow, JSONObject(mapOf("sid" to it?.cust_data?.sid)))
                            toggleFollow()
                        }
                        if (it?.cust_data?.sid.equals(MyApplication.SID)) {
                            binding.btSheet.message.visibility = View.GONE
                            binding.btSheet.tvFollow.visibility = View.GONE
                            binding.btSheet.tvCreateMeet.visibility = View.GONE

                            val positiveDialog = ExperienceDialog(requireActivity(), it?.social?.meetup_positive_experience_count ?: 0, 1, null)
                            credility.llPositive.onClick({
                                MyApplication.putTrackMP(Constant.AcMapProfilePositiveExp, JSONObject(mapOf("sid" to it?.cust_data?.sid)))
                                positiveDialog.show()
                            })
                            val meetupDialog = ExperienceDialog(requireActivity(), it?.social?.meetup_attendance_count ?: 0, 0, null)
                            credility.llMeetups.onClick({
                                MyApplication.putTrackMP(Constant.AcMapProfileMeetCount, JSONObject(mapOf("sid" to it?.cust_data?.sid)))
                                meetupDialog.show()
                            })
                        } else {
                            binding.btSheet.message.visibility = View.VISIBLE
                            binding.btSheet.tvFollow.visibility = View.VISIBLE
                            binding.btSheet.tvCreateMeet.visibility = View.VISIBLE
                            binding.btSheet.message.onClick( {
                                MyApplication.putTrackMP(Constant.AcMapProfileChat, JSONObject(mapOf("sid" to it?.cust_data?.sid)))
                                var baseFragment: BaseFragment = OnenOneChat.getInstance(it?.cust_data?.sid)
                                Navigation.addFragment(requireActivity(), baseFragment, Constant.TAG_ONE_N_ONE_CHAT, R.id.homeFram, true, false)
                            })
                            val positiveDialog = ExperienceDialog(requireActivity(), it?.social?.meetup_positive_experience_count ?: 0, 1, it?.cust_data?.username)
                            credility.llPositive.onClick({
                                positiveDialog.show()
                            })
                            val meetupDialog = ExperienceDialog(requireActivity(), it?.social?.meetup_attendance_count ?: 0, 0, it?.cust_data?.username)
                            credility.llMeetups.onClick({
                                meetupDialog.show()
                            })
                        }
                        it?.social?.meetup_positive_experience_count?.let {
                            if(it > 0) {
                                credility.llPositive.setBackgroundResource(R.drawable.bg_positive)
                                credility.ivPositive.setImageResource(R.drawable.thump01)
                            } else {
                                credility.llPositive.setBackgroundResource(R.drawable.bg_postive_gray)
                                credility.ivPositive.setImageResource(R.drawable.thumb_gray)
                            }
                            credility.tvPositive.text = "$it Positive Exp"
                        } ?: run {
                            credility.tvPositive.text = "0 Positive Exp"
                            credility.llPositive.setBackgroundResource(R.drawable.bg_postive_gray)
                            credility.ivPositive.setImageResource(R.drawable.thumb_gray)
                        }
                        it?.social?.meetup_attendance_count?.let {
                            if(it > 0) {
                                credility.llMeetups.setBackgroundResource(R.drawable.bg_meetups_color)
                                credility.ivMeetup.setImageResource(R.drawable.table02)
                            } else {
                                credility.llMeetups.setBackgroundResource(R.drawable.bg_postive_gray)
                                credility.ivMeetup.setImageResource(R.drawable.table)
                            }
                            credility.tvMeetups.text = "$it Meetups"
                        } ?: run {
                            credility.tvMeetups.text = "0 Meetups"
                            credility.llMeetups.setBackgroundResource(R.drawable.bg_postive_gray)
                            credility.ivMeetup.setImageResource(R.drawable.table)
                        }


                        otherUserLocation?.let { otherUserLocation ->
                            var distance = myLocation?.distanceTo(otherUserLocation)?.toInt()
                            var distanceString = if (distance?.compareTo(1000) == -1) distance.toString()
                                .plus(" metre") else distance?.div(1000).toString().plus(" km")

                            var distanceText = it?.cust_data?.username?.plus(" is just ").plus(distanceString)
                                .plus(" away from you")
                            var spannable = SpannableString(distanceText)
                            spannable.setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.primaryDark
                                    )
                                ),
                                distanceText.lastIndexOf("just").plus(4),
                                distanceText.lastIndexOf("away"),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                0,
                                it?.cust_data?.username?.length?:0,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.primaryDark
                                    )
                                ), 0, it?.cust_data?.username?.length?:0, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            spannable.setSpan(
                                StyleSpan(Typeface.BOLD),
                                0,
                                it?.cust_data?.username?.length?:0,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            binding.distanceDesc.setText(spannable, TextView.BufferType.SPANNABLE)

                        }

                        binding.btSheet.spotlight1.setImageDrawable(null)
                        binding.btSheet.spotlight2.setImageDrawable(null)
                        binding.btSheet.spotlight3.setImageDrawable(null)
                        Log.i(TAG, " bsSpotlight1Null:: ${binding.btSheet.spotlight1.drawable}")
//                        for (item in it?.social?.spotlights!!) {
//                            item?.one?.let {
//                                Log.i(TAG, " SPotlight::  1")
//                                isSpotlight1Present = true
//                                Utils.stickImage(requireContext(), binding.btSheet.spotlight1, it, null)
//                            }
//                            item?.two?.let {
//                                Log.i(TAG, " SPotlight::  2")
//                                isSpotlight2Present = true
//                                Utils.stickImage(requireContext(), binding.btSheet.spotlight2, it, null)
//                            }
//                            item?.three?.let {
//                                Log.i(TAG, " SPotlight::  3")
//                                isSpotlight3Present = true
//                                Utils.stickImage(requireContext(), binding.btSheet.spotlight3, it, null)
//                            }
//                            Log.i("TAG", " ${item.toString()}")
//                        }
                        binding.btSheet.spotlight1.drawable?.let { draw ->
                            binding.btSheet.spotlight1.setOnClickListener { view ->
                                var url = OtherProfileFragment.getSpotLightUrl(1, it)
                                var baseFragment: BaseFragment = ProfileEditPicPage()
                                baseFragment = Navigation.setFragmentData(
                                    baseFragment,
                                    ProfileEditPicPage.TYPE,
                                    ProfileEditPicPage.SPOTLIGHT3
                                )
                                baseFragment =
                                    Navigation.setFragmentData(baseFragment, ProfileEditPicPage.IMAGE_URL, url)
                                baseFragment = Navigation.setFragmentData(
                                    baseFragment,
                                    ProfileEditPicPage.PROFILE_TYPE,
                                    ProfileEditPicPage.OTHER
                                )
                                Navigation.addFragment(
                                    requireActivity(),
                                    baseFragment,
                                    "ProfileEditPicPage",
                                    R.id.homeFram,
                                    true,
                                    false
                                )
                            }

                        } ?: run {
                            binding.btSheet.spotlight1.setOnClickListener(null)
                        }

                        binding.btSheet.spotlight2.drawable?.let { draw ->
                            binding.btSheet.spotlight2.setOnClickListener { view ->
                                var url = OtherProfileFragment.getSpotLightUrl(2, it)
                                var baseFragment: BaseFragment = ProfileEditPicPage()
                                baseFragment = Navigation.setFragmentData(
                                    baseFragment,
                                    ProfileEditPicPage.TYPE,
                                    ProfileEditPicPage.SPOTLIGHT3
                                )
                                baseFragment =
                                    Navigation.setFragmentData(baseFragment, ProfileEditPicPage.IMAGE_URL, url)
                                baseFragment = Navigation.setFragmentData(
                                    baseFragment,
                                    ProfileEditPicPage.PROFILE_TYPE,
                                    ProfileEditPicPage.OTHER
                                )
                                Navigation.addFragment(
                                    requireActivity(),
                                    baseFragment,
                                    "ProfileEditPicPage",
                                    R.id.homeFram,
                                    true,
                                    false
                                )
                            }

                        } ?: run {
                            binding.btSheet.spotlight2.setOnClickListener(null)
                        }

                        binding.btSheet.spotlight3.drawable?.let { draw ->
                            binding.btSheet.spotlight3.setOnClickListener { view ->
                                var url = OtherProfileFragment.getSpotLightUrl(3, it)
                                var baseFragment: BaseFragment = ProfileEditPicPage()
                                baseFragment = Navigation.setFragmentData(
                                    baseFragment,
                                    ProfileEditPicPage.TYPE,
                                    ProfileEditPicPage.SPOTLIGHT3
                                )
                                baseFragment =
                                    Navigation.setFragmentData(baseFragment, ProfileEditPicPage.IMAGE_URL, url)
                                baseFragment = Navigation.setFragmentData(
                                    baseFragment,
                                    ProfileEditPicPage.PROFILE_TYPE,
                                    ProfileEditPicPage.OTHER
                                )
                                Navigation.addFragment(
                                    requireActivity(),
                                    baseFragment,
                                    "ProfileEditPicPage",
                                    R.id.homeFram,
                                    true,
                                    false
                                )
                            }


                        } ?: run {
                            binding.btSheet.spotlight3.setOnClickListener(null)
                        }
                        otherUserLocation?.let { loc ->
                            binding.address.text = Geocoder(requireContext(), Locale.UK).getFromLocation(
                                loc.latitude,
                                loc.longitude,
                                1
                            ).getOrNull(0)?.featureName
                        }

                    }
                }
                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(),"Something went wrong!!")
                }
            }


        })
    }
    fun refreshImage(){
        binding.rvInterests.adapter?.notifyDataSetChanged()
    }

    fun mapUserToMarker(mapUserResponse: MapUserResponse?) {
        var flowable = Flowable.create<MarkerClusterItem?>({ emitter ->
            mapUserResponse?.forEach {
                Log.i(TAG, " mapUserToMarker::: 0")
                Log.d(TAG, "mapUserToMarker: ${it}")
                try {
                    Log.i(TAG, " mapUserToMarker::: 2 ${it?.profile_image_url}")
                    var image : Bitmap? = null
                    it?.profile_image_url?.let {profile_image_url ->
                        val url = URL(profile_image_url)
                        Log.i(TAG, " mapUserToMarker::: 3")
                        image =
                            BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    }?:run{
                        image = BitmapFactory.decodeResource(context?.getResources(),
                            R.drawable.ic_default_person);
                    }
                    /*val url = URL(it.profile_image_url?:getString(R.string.default_img_url))
                    Log.i(TAG, " mapUserToMarker::: 3")
                    val image =
                        BitmapFactory.decodeStream(url.openConnection().getInputStream())*/
                    Log.i(TAG, " mapUserToMarker::: 6  ")
                    Log.i(TAG, " mapUserToMarker::: 4  $image")
                    Log.i(TAG, " mapUserToMarker::: 5  ${it}")
                    var marker = MarkerClusterItem(
                        LatLng(it?.location?.coordinates?.getOrNull(1)?:0.0, it?.location?.coordinates?.getOrNull(0)?:0.0),
                        it?.sid,
                        image,
                        lastSeen = Utils.getLastSeen(it?.location_captured_at),
                            badge = it?.badge,
                            boosted=it?.boosted?:false
                    )
                    emitter.onNext(marker)
                    if (mapUserResponse.last()?.equals(it) == true) {
                        Log.i(TAG, " complete:: ")
                        //subject.onComplete()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, " errorComws:: ${e.printStackTrace()}")
                }
            }
        }, BackpressureStrategy.BUFFER)
        peopleDisposable = flowable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                subject.onNext(it)
            }, {
                it.printStackTrace()
                Log.e(TAG," check above stackStrace for crash mapUserToMarker ")
                //subject.onError(it)
            })
        disposable.add(peopleDisposable)


    }

    fun mapPlaceToMarker(mapUserResponse: FetchPlacesResponse?, categories: List<Category>?) {
        //disposable.add(
        Log.i(TAG," mapPlaceToMarker::: ")
        var flowable = Flowable.create<MarkerClusterItem>({
            val markers = mapUserResponse?.map { response ->
                Log.i(TAG, " NearBy places::: ${response.primary_kind_of_place}")
                var category =
                    categories?.filter { it.key == response.primary_kind_of_place?.get(0) }

                try {
                    val url = URL(
                        if (category?.size?.compareTo(0) == 1) category.get(0).color_png_url else PreferencesManager.get<CategoryResponse>(
                            PREFRENCE_CATEGORY
                        )?.definition?.get(0)?.color_png_url
                    )
                    Log.i(
                        TAG,
                        " mapPlaceToMarker::: 1 ${
                            if (category?.size?.compareTo(0) == 1) category.get(0).color_png_url else "not availble"
                        }"
                    )
                    val image : Bitmap? =
                        BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    Log.i(TAG, " mapPlaceToMarker::: ${response._id} ${image?.byteCount}")
                    var marker = MarkerClusterItem(
                        LatLng(
                            response.location?.coordinates?.get(1)!!,
                            response.location?.coordinates!!.get(0)
                        ),
                        response._id,
                        image,
                        lastSeen = response.name?.en,
                    )
                    it.onNext(marker)
                    //
                    if (mapUserResponse.last().equals(response)) {
                        //subject.onComplete()
                    }
                } catch (e: IOException) {
                    println(e.printStackTrace())
                    //it.onError(e)

                }
            }
        }, BackpressureStrategy.BUFFER)

        placeDisposable = flowable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                subject.onNext(it)
            }, {
                Log.e(TAG," check above stackStrace for crash mapPlaceToMarker ${it.printStackTrace()}")
                //subject.onError(it)
            })

        disposable.add(placeDisposable)

    }

    fun subscribeSubject() {
        disposable.add(
            subject.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Log.i(TAG, " observingCorretly:: ")
                    clusterManager?.addItem(it)
                    clusterManager?.cluster()
                }, {
                    Log.i(TAG, " errorInPublisher:::  ${it.printStackTrace()}")
                })
        )
    }

    /*private fun openOtherProfile(sid: String?) {
        sid?.let {
            if (sid == SID) {
                Navigation.addFragment(
                    requireActivity(),
                    ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
                )
            } else {
                var baseFragment: BaseFragment = OtherProfileFragment.getInstance(sid)
                *//*baseFragment = Navigation.setFragmentData(
                    baseFragment,
                    OtherProfileFragment.OTHER_USER_ID,
                    sid
                )*//*
                Navigation.addFragment(
                    requireActivity(), baseFragment,
                    Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
                )
            }
        }
    }*/

    private fun toggleFollow() {
        if (binding.btSheet.tvFollow.text.equals("Unfollow")) {
            binding.btSheet.tvFollow.text = "Follow"
            binding.btSheet.tvFollow.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.btSheet.tvFollow.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.round_border_primary_bg)
        } else if (binding.btSheet.tvFollow.text.equals("Follow")) {
            binding.btSheet.tvFollow.text = "Unfollow"
            binding.btSheet.tvFollow.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primaryDark
                )
            )
            var bg = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.horizontal_round_shape
            ) as GradientDrawable
            bg.setStroke(1, ContextCompat.getColor(requireContext(), R.color.primaryDark))
            binding.btSheet.tvFollow.setBackgroundDrawable(bg)
        }
    }

    override fun onMapReady(gMap: GoogleMap?) {
        Log.i(TAG, " onMapReadyonMapReady:: 1")
        this.gMap = gMap
        Log.i(TAG, " onMapReadyonMapReady:: 2")
        var mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
            requireContext(),
            R.raw.google_style
        )
        Log.i(TAG, " onMapReadyonMapReady:: 3")
        gMap?.setMapStyle(mapStyleOptions)
        Log.i(TAG, " onMapReadyonMapReady:: 4")

        clusterManager = ClusterManager<MarkerClusterItem>(activity, gMap)
        fetchCurrentLocation()

        binding.search.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcMapSearchArea, null)
            searcClicked()
        }

        renderer =
            MarkerClusterRenderer<MarkerClusterItem>(requireActivity(), gMap, this, clusterManager)
        clusterManager?.renderer = renderer
        clusterManager?.setAnimation(true)

        Log.i(TAG, " yaha Pahucha:: ")
        //binding.placeTab.setOnTouchListener(Utils.getTouchlistener(placeSwipe))
        //binding.peopleTab.setOnTouchListener(Utils.getTouchlistener(peopleSwipe))
        binding.peopleTab.onClick({ pleopleTabClicked()})
        binding.placeTab.onClick({ placeTabClicked()  })

        clusterManager?.setOnClusterClickListener {
            Log.i(TAG, " setOnClusterClickListener::: ")
            val cameraPosition =
                CameraPosition.Builder().target(it.position).tilt(90f).zoom(50f).bearing(0f)
                    .build()
            this.gMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            //this.gMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position,21f))
            true
        }

        clusterManager?.setOnClusterItemClickListener {
            Log.i(TAG, " Item:Clicked::: ")
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            if (renderer.isItPlace == false) {
                MyApplication.putTrackMP(Constant.AcMapPeopleCluster, JSONObject(mapOf("sid" to it.title)))
                if(it.title == null)
                    return@setOnClusterItemClickListener true
                profileViewModel.getOtherProfile(it.title)
                otherUserLocation = Location("otherUserLocartion").apply {
                    latitude = it.position.latitude
                    longitude = it.position.longitude
                }
                mapFragment.getMapAsync(OnMapReadyCallback { map ->
                    var mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.google_style
                    )
                    map?.setMapStyle(mapStyleOptions)
                    val cameraPosition =
                        CameraPosition.Builder()
                            .target(LatLng(it.position.latitude, it.position.longitude)).tilt(60f)
                            .zoom(18f)
                            .bearing(0f).build()
                    //map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.position.latitude, it.position.longitude), 14f))
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                    bsMapMarker?.let { it.remove() }
                    bsMapMarker = map.addMarker(
                        MarkerOptions()
                            .position(it.position)
                            .icon(
                                Utils.bitmapDescriptorFromVector(
                                    requireContext(),
                                    R.drawable.ic_location_marker
                                )
                            )
                    )
                })
                //updateSelectedMarker(previorMarker, previosItem, it, gMap)
            }


            val cameraPosition =
                CameraPosition.Builder().target(it.position).tilt(60f).zoom(19f).bearing(0f)
                    .build()

            gMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        if (renderer.isItPlace == false)
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                        else {
                            MyApplication.putTrackMP(Constant.AcMapPlaceCluster, JSONObject(mapOf("placeId" to it.title)))
                            var baseFragment: BaseFragment = RestaurantDetailFragment();
                            Navigation.setFragmentData(baseFragment, "_id", it.title)
                            Navigation.addFragment(
                                requireActivity(), baseFragment,
                                RestaurantDetailFragment.TAG, R.id.homeFram, true, false
                            )
                        }
                    }

                    override fun onCancel() {
                        Log.i(TAG, " onCancel:: ${(it as MarkerClusterItem).image}")
                    }

                });
            true
        }
        gMap?.setOnCameraIdleListener(this)
        //gMap?.setOnMarkerClickListener(this)
        clusterManager?.cluster()

    }


    fun fetchCurrentLocation(){
        if(!Utils.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) return
        Log.i(TAG, " listenerAdded:: ")
        gMap?.isMyLocationEnabled = true
        gMap?.setOnCameraMoveListener(this)
        gMap?.setOnCameraMoveCanceledListener(this)
        gMap?.setOnCameraIdleListener(this)
        gMap?.setOnCameraMoveStartedListener(this)

        locationFinder = LocationFinder(requireActivity(), object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    it.locations.map {
                        it?.let {
                            myLocation = it
                            val cameraPosition =
                                CameraPosition.Builder().target(LatLng(it.latitude, it.longitude))
                                    .tilt(90f)
                                    .zoom(15f).bearing(0f).build()

                            gMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                            //gMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude,it.longitude), 15f))
                            profileViewModel.getMapUsers(LatLng(it.latitude, it.longitude), null)
                            profileViewModel.getFullGenericList("interests")
                            locationFinder?.mFusedLocationClient?.removeLocationUpdates(this)
                        }
                    }
                }
            }
        })
    }


    //---------------------clickedEvent------------//
    fun searcClicked() {
        binding.searchProgress.visibility = View.VISIBLE
        if (renderer.isItPlace == false) {
            profileViewModel.getMapUsers(
                LatLng(
                    gMap?.cameraPosition?.target?.latitude!!,
                    gMap?.cameraPosition?.target?.longitude!!
                ),
                interestList.filter { it?.selected == true }.map { it?.key }.joinToString(",")
            )
        } else {
            Log.i(TAG, " placesCalled:: ")
            placeViewModel.getCloseByPlaceUp(
                20,
                gMap?.cameraPosition?.target?.latitude,
                gMap?.cameraPosition?.target?.longitude,
                null,
                "location,name,primary_kind_of_place,_id",
                1000,
                filter = categoryList?.filter { it?.isSelected == true }?.map { it?.key }
                    ?.joinToString(",")
            )
        }
    }

    fun placeTabClicked() {
        MyApplication.putTrackMP(Constant.AcMapPlacesTab, null)
        //binding.count.visibility = View.GONE
        if(this::peopleDisposable.isInitialized && !peopleDisposable.isDisposed)
            peopleDisposable.dispose()
        profileViewModel.disposeMapUser()
        clusterManager?.clearItems()
        gMap?.clear()
        clusterManager?.cluster()
        Log.i(TAG, " clicked:: placeTabClicked  ")
        Utils.popUpVisibilityGone(binding.count, 300)
        binding.placeTab.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_map_tab_new)
        binding.peopleTab.background = null
        renderer.isItPlace = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        try {
            gMap?.clear()
        } catch (e: Exception) {
            Log.i(TAG, " gMap?.clear()::: 1 ${e.printStackTrace()}")
        }
        placeViewModel.getCloseByPlaceUp(20,
            gMap?.cameraPosition?.target?.latitude,
            gMap?.cameraPosition?.target?.longitude,
            null,
            "location,name,primary_kind_of_place,_id",
            1000,
            filter = categoryList?.filter { it?.isSelected == true }?.map { it?.key }
                ?.joinToString { "," })
        binding.rvInterests.adapter = mapCategoryAdapter
    }

    fun pleopleTabClicked() {
        MyApplication.putTrackMP(Constant.AcMapPeopleTab, null)
        Log.i(TAG, " clicked:: peopleTabClicked  ")
        placeViewModel.disposeNearByPlace()
        if(this::placeDisposable.isInitialized && !placeDisposable.isDisposed)
            placeDisposable.dispose()

        clusterManager?.clearItems()
        gMap?.clear()
        clusterManager?.cluster()
        Utils.popUpVisibilityGone(binding.count, 300)
        binding.peopleTab.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_map_tab_new)
        binding.placeTab.background = null
        renderer.isItPlace = false
        try {
            gMap?.clear()
        } catch (e: Exception) {
            Log.i(TAG, " gMap?.clear()::: 1 ${e.printStackTrace()}")
        }
        profileViewModel.getMapUsers(
            LatLng(
                gMap?.cameraPosition?.target?.latitude!!,
                gMap?.cameraPosition?.target?.longitude!!
            ), interestList.filter { it?.selected == true }.map { it?.key }.joinToString(",")
        )
        binding.rvInterests.adapter = mapInterestAdapter
    }

    //---------------------clickedEvent Closed------------//

    private fun updateSelectedMarker(
        marker: Marker?,
        previousItem: MarkerClusterItem?,
        currentItem: MarkerClusterItem?,
        gMap: GoogleMap?
    ) {
        Log.i(TAG, " previous:: ${previousItem?.title}  currentItem:: ${currentItem?.title}")
        try {
            previousItem?.seletced = false
            previousItem?.let {
                previorMarker?.let {
                    it.setIcon(
                        BitmapDescriptorFactory.fromBitmap(
                            Utils.createMapUserMarker(
                                requireActivity(),
                                previousItem?.image,
                                false,
                                timestamp = previousItem.lastSeen
                            )
                        )
                    )
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.i(TAG, " IllegalArgumentException")
        }
        previorMarker = renderer.getMarker(currentItem)
        previosItem = currentItem
        currentItem?.seletced = true
        previorMarker?.let { marker ->
            marker.setIcon(
                BitmapDescriptorFactory.fromBitmap(
                    Utils.createMapUserMarker(
                        requireActivity(),
                        currentItem?.image,
                        true,
                        timestamp = currentItem?.lastSeen
                    )
                )
            )

            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    marker.setIcon(
                        BitmapDescriptorFactory.fromBitmap(
                            Utils.createMapUserMarker(
                                requireActivity(),
                                currentItem?.image,
                                true,
                                timestamp = currentItem?.lastSeen
                            )
                        )

                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 100)
        }
    }

    override fun onCameraMove() {
        Log.i(TAG, " onCameraMove:: ")
        Utils.fadeVisibilityVisible(binding.llsearch, 100)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        clusterManager?.cluster()
    }

    override fun onCameraMoveCanceled() {
        Log.i(TAG, " onCameraMoveCanceled:: ")
    }

    override fun onCameraMoveStarted(reason: Int) {
        Log.i(TAG, " onCameraMoveStarted:: $reason")
        if (reason == REASON_GESTURE)
            shouldFetchUser = true
        else
            shouldFetchUser = false
    }

    override fun onCameraIdle() {
        Log.i(TAG, " onCameraIdle::  $shouldFetchUser")

    }


    override fun onMyLocationClick(p0: Location) {
        Log.i(TAG, " onMyLocationClick:: $p0")
    }

    override fun onMyLocationButtonClick(): Boolean {
        Log.i(TAG, " onMyLocationButtonClick:: ")
        return false
    }

    override fun onStop() {
        super.onStop()
    }


    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }

    override fun onBackPressed(): Boolean {
        if(bottomSheetBehavior.state != BottomSheetBehavior.STATE_COLLAPSED){
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            return false
        }
        return true
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }


}