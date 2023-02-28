package com.meetsportal.meets.ui.activities


import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.fenchtose.tooltip.Tooltip
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.*
import com.google.gson.JsonObject
import com.meetsportal.meets.BuildConfig
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.models.StateResource
import com.meetsportal.meets.models.Status
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.firebase.FireBaseUtils
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.networking.registration.OtpResponse
import com.meetsportal.meets.ui.dialog.AllowLocationAlert
import com.meetsportal.meets.ui.dialog.CreateMeetOptionAlert
import com.meetsportal.meets.ui.dialog.ForceUpdateAlert
import com.meetsportal.meets.ui.dialog.InterestDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.HomeFragment
import com.meetsportal.meets.ui.fragments.authfragment.MinedMintsCeleb
import com.meetsportal.meets.ui.fragments.socialfragment.*
import com.meetsportal.meets.ui.fragments.socialonboarding.OnBoardOTP
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.AcCreatMeetHome
import com.meetsportal.meets.utils.Constant.AcDmOnHome
import com.meetsportal.meets.utils.Constant.AcHomePageDp
import com.meetsportal.meets.utils.Constant.AcMapOnHome
import com.meetsportal.meets.utils.Constant.AcNotiOnHome
import com.meetsportal.meets.utils.Constant.LATITUDE
import com.meetsportal.meets.utils.Constant.LONGITUDE
import com.meetsportal.meets.utils.Constant.PREFERENCE_MSG_NOTI
import com.meetsportal.meets.utils.Constant.PREFERENCE_SHOW_NOTI
import com.meetsportal.meets.utils.Constant.PREFRENCE_CATEGORY
import com.meetsportal.meets.utils.Constant.PREFRENCE_CUISINE
import com.meetsportal.meets.utils.Constant.PREFRENCE_FACILITY
import com.meetsportal.meets.utils.Constant.PREFRENCE_LOCATION
import com.meetsportal.meets.utils.Constant.PREFRENCE_PROFILE
import com.meetsportal.meets.utils.Constant.TAG_CHAT_DASHBOARD_FRAGMENT
import com.meetsportal.meets.utils.Constant.TAG_NOTIFICATION_FRAGMENT
import com.meetsportal.meets.utils.Constant.TAG_PROFILE_FRAGMENT
import com.meetsportal.meets.utils.Constant.TAG_SEARCH_FRAGMENT
import com.meetsportal.meets.utils.Constant.VwMeetUpTab
import com.meetsportal.meets.utils.Constant.VwPlaceTab
import com.meetsportal.meets.utils.NotificationServiceExtension.NotificationTypes
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.meetsportal.meets.viewmodels.PlacesViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import kotlin.math.sqrt

@AndroidEntryPoint
class HomeActivityNew : AppCompatActivity() {

//    lateinit var rlMap: RelativeLayout
    val profileViewmodel: UserAccountViewModel by viewModels()
    val placeViewModel: PlacesViewModel by viewModels()
    val meetUpViewModel: MeetUpViewModel by viewModels()

    private val TAG = HomeActivityNew::class.java.simpleName
    lateinit var tablayout: TabLayout
    private lateinit var viewPager: ViewPager
    lateinit var appbar: AppBarLayout
    lateinit var createMeetUp: MotionLayout
    lateinit var createMeetUp2: LinearLayout
    lateinit var notifications: ImageView
    lateinit var rlNotifications: RelativeLayout
    lateinit var icMap: ImageView
    lateinit var location: TextView
    lateinit var dp: CircleImageView
    lateinit var ivDpBadge: ImageView
    lateinit var name: TextView
    lateinit var tvLocation: TextView
    lateinit var parentView: RelativeLayout
    lateinit var viewPageAdapter: ViewPagerAdapter
    lateinit var intrest: InterestDialog
    lateinit var floatButton: CardView
    lateinit var newNoti: ImageView
    lateinit var newMessageNoti: ImageView
    lateinit var myBooking: LinearLayout
    lateinit var mySavedPlaces: LinearLayout
    lateinit var cToastView: RelativeLayout
    lateinit var cToastText: TextView
    lateinit var snake: Snackbar
    var meetOption: CreateMeetOptionAlert? = null
    var locationFinder: LocationFinder? = null
    var locationCallback: LocationCallback? = null
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f


    // Listens for location broadcasts from ForegroundOnlyLocationService.
    var locationAlert: AllowLocationAlert? = null
    lateinit var gpsUtils: GpsUtils
    var gpsFunction: () -> Unit = {}
    var notificationHandler: HandleNotification? = null
    var isNewUser: Boolean = false

    /////               --gettingLastLocation--            ///////


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, " onCreate:: ")
        setContentView(R.layout.activity_home_new)
        supportActionBar?.hide()

        initiateRegisterShake()
        initview()
        var a = PreferencesManager.get<OtpResponse>(Constant.PREFRANCE_OTPRESPONSE)
        a?.user?.sid?.let {
            MyApplication.setOnSignalExternalId(it)
            if(intent?.getBooleanExtra(OnBoardOTP.IS_NEWUSER, false) == true) {
                isNewUser = true
                Navigation.addFragment(this, MinedMintsCeleb(), MinedMintsCeleb.TAG, R.id.homeFram, true, true)
            }
        } ?: run {
            profileViewmodel.signOut()
            startActivity(Intent(this, AuthenticationActivity::class.java))
            finish()
        }


        //Log.i(TAG," HomeActivityUser:: $a")
        setUp()


    }


    private fun initiateRegisterShake() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        Objects.requireNonNull(sensorManager)!!
                .registerListener(sensorListener, sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration
            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta
            //Log.i(TAG," acceleration:: ${acceleration}")
            if(acceleration > 9.5) {
                Log.i(TAG, " Shake working:: ${acceleration}")
                //hitEmergencyApi()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    private fun hitEmergencyApi() {
        MyApplication.bigVibrate()
        profileViewmodel.sendEmergency()
    }

    private fun initview() {

        /*//if(intent?.getBooleanExtra(OnBoardOTP.IS_NEWUSER,false) == true){
            Navigation.addFragment(this,
                MinedMintsCeleb(),
                MinedMintsCeleb.TAG,R.id.homeFram,true,true)
        //}*/
        FireBaseUtils.init()
        //showUpdateEmailSnake()

        notificationHandler = HandleNotification(this, meetUpViewModel)
        notificationHandler?.notificationObserver()
        rlNotifications = findViewById(R.id.rl_notification)
        Utils.onClick(rlNotifications, 1000) {
            MyApplication.putTrackMP(AcNotiOnHome, null)
            newNoti.visibility = View.INVISIBLE
            PreferencesManager.put<Boolean>(false, PREFERENCE_SHOW_NOTI)
            var baseFragment: BaseFragment = NotificationFragment()
            //var baseFragment: BaseFragment = NonUiFragment.getInstance("HeadlessFragment")
            Navigation.addFragment(this@HomeActivityNew, baseFragment, TAG_NOTIFICATION_FRAGMENT, R.id.homeFram, true, false)

            /*Navigation.addFragment(
                this@HomeActivityNew,
                ProfileViewFragment(),
                TAG_NOTIFICATION_FRAGMENT,
                R.id.homeFram,
                true,
                false
            )*/

        }
//        rlMap = findViewById<RelativeLayout>(R.id.rl_map)
//        rlMap.onClick({
//            MyApplication.putTrackMP(AcMapOnHome, null)
//            enableLocationStuff(2000, 1242) {
//                fetchLastLocation()
//                Navigation.addFragment(this, MeetupOpenMemberMapFragment(), Constant.TAG_OPEN_FOR_MEETUP_MAP, R.id.homeFram, true, false)
//            }
//        }, 2000)
        findViewById<ImageView>(R.id.iv_chat).onClick({
            MyApplication.putTrackMP(AcDmOnHome, null)
            newMessageNoti.visibility = View.GONE
            PreferencesManager.put<Boolean>(false, Constant.PREFERENCE_MSG_NOTI)
            MyApplication.smallVibrate()
            Navigation.addFragment(this, ChatDashboardFragment(), TAG_CHAT_DASHBOARD_FRAGMENT, R.id.homeFram, stack = true, needAnimation = false)
        })

        meetOption = CreateMeetOptionAlert(this) {
            MyApplication.smallVibrate()
            var baseFragment = MeetUpViewPageFragment.getInstanceForOpenMeetUp(it)
            Navigation.addFragment(this, baseFragment, MeetUpViewPageFragment.TAG, R.id.homeFram, true, true)
        }
        tablayout = findViewById(R.id.tl_tabs)
        viewPager = findViewById(R.id.home_viewPager)
        appbar = findViewById(R.id.appbar)
        createMeetUp = findViewById<MotionLayout>(R.id.ll_create_meet_up)
        createMeetUp2 = findViewById<LinearLayout>(R.id.ll_create_meet_up2)
        dp = findViewById(R.id.civ_image)
        ivDpBadge = findViewById(R.id.ivDpBadge)
        floatButton = findViewById(R.id.floatbutton)
        myBooking = findViewById(R.id.ll_my_booking)
        mySavedPlaces = findViewById(R.id.ll_saved_places)
        name = findViewById(R.id.tv_greet)
        location = findViewById(R.id.tv_quote)
        parentView = findViewById(R.id.rootCo)
        cToastView = findViewById<RelativeLayout>(R.id.rl_toast).apply {
            background = Utils.getToastBacGround(this@HomeActivityNew)
        }
        cToastText = findViewById(R.id.text_toast)
        tvLocation = findViewById(R.id.tv_location)
        tvLocation.text = "cdhvch"
        tvLocation.visibility = View.VISIBLE
        newNoti = findViewById(R.id.new_noti)
        newMessageNoti = findViewById(R.id.newMessage)
        gpsUtils = GpsUtils(this) { it, status ->
            if(it) {
                Log.i(TAG, " responseGpscame::  ")
                when(status) {
                    1000 -> fetchLastLocation()

                    2000 -> {
                        fetchLastLocation()
                        Navigation.addFragment(this, MeetupOpenMemberMapFragment(), Constant.TAG_OPEN_FOR_MEETUP_MAP, R.id.homeFram, true, false)
                    }
                }
            }
        }

        locationAlert = AllowLocationAlert(this) {
            if(Utils.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //startFetchLocation()
                gpsUtils.turnGPSOn(1000)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1232)
            }

        }
        /*findViewById<ImageView>(R.id.iv_chat).setOnClickListener {
            *//*val enabled = sharedPreferences.getBoolean(
               KEY_FOREGROUND_ENABLED, false)

            if (enabled) {
                foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
            } else {
                // TODO: Step 1.0, Review Permissions: Checks and requests if needed.
                if (foregroundPermissionApproved()) {
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                }
            }*//*
        }*/
        findViewById<ImageView>(R.id.iv_search).onClick({
            MyApplication.smallVibrate()
            Navigation.addFragment(this, SearchFragment.getInstance(0), TAG_SEARCH_FRAGMENT, R.id.homeFram, stack = true, needAnimation = false)
        })
        dp.onClick({
            MyApplication.smallVibrate()
            MyApplication.putTrackMP(AcHomePageDp, null)
            openProfile()
        })
        name.onClick({
            MyApplication.smallVibrate()
            Log.i(TAG, " ")
            openProfile()
        })
        intrest = InterestDialog(this) {
            Log.i(TAG, " INTEREST:: ${it} ")
            profileViewmodel.updateInterest(it)
        }

        if(Utils.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) fetchLastLocation()
//        Utils.showToolTips(this, rlMap, parentView, Tooltip.BOTTOM, "View people who are available for Meetup on the map", "rlMap") {}

//        Utils.showTooltip(this,rlMap,parentView,Tooltip.TOP,"Matching Interests"
//                ,"Choose interests to see only people\nWho share same interests as you"){
//        }
    }

    private fun showUpdateEmailSnake() {
        var email = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.email ?: ""
        Log.d(TAG, "showUpdateEmailSnak: $email")
        if(!email.trim().equals("")) return
        val snackbar = Snackbar.make(findViewById(R.id.rootCoord), "hd", Snackbar.LENGTH_LONG)
                .setDuration(10000).setAction("OK") {
                    var baseFragment = UpdateMailFragment.getInstance("email")
                    Navigation.addFragment(this, baseFragment, UpdateMailFragment.TAG, R.id.homeFram, true, false)
                }
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.primaryDark))
        val sbView = snackbar.view
        val textView = sbView.findViewById<View>(R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        val s = "Please update your email\nThis will help you recover your account"
        val ss1 = SpannableString(s)
        ss1.setSpan(RelativeSizeSpan(1.2f), 0, s.indexOf("This"), 0) // set size
        textView.setText(ss1)
        snackbar.show()
    }

    fun enableLocationStuff(gpsCode: Int, requestCode: Int, gpsFunction: () -> Unit) {
        this.gpsFunction = gpsFunction
        this.gpsUtils = GpsUtils(this) { it, status ->
            if(it) {
                Log.i(TAG, " responseGpscame::  $status $gpsCode")
                when(status) {
                    gpsCode -> {
                        Log.i(TAG, " cameHere But not working")
                        this.gpsFunction()
                    }
                }
            }
        }
        if(Utils.checkPermission(this@HomeActivityNew, Manifest.permission.ACCESS_FINE_LOCATION)) {
            this.gpsUtils.turnGPSOn(gpsCode)
        } else {
            ActivityCompat.requestPermissions(this@HomeActivityNew, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
        }
    }

    fun fetchLastLocation() {
        locationFinder?.let {
            locationCallback?.let { callback ->
                it.mFusedLocationClient?.removeLocationUpdates(callback)
            }
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.i(TAG, " LocationCame::: ")
                locationResult?.let {
                    it.locations.map {
                        it?.let {
                            locationFinder?.setNewInterval(60)
                            //locationFinder?.mFusedLocationClient?.removeLocationUpdates(this)
                            initLocationRelatedView(it, true)
                        }
                    }
                }
            }
        }
        locationFinder = LocationFinder(this, locationCallback, 10)
    }

    /*private fun startFetchLocation(){
        gpsUtils.turnGPSOn(1000)

        *//*if (foregroundPermissionApproved()) {
            foregroundOnlyLocationService?.subscribeToLocationUpdates()
                ?: Log.d(TAG, "Service Not Bound")
        }*//*
    }*/

    // TODO: Step 1.0, Review Permissions: Method checks if permissions approved.
    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun addClickEvent() {
        compositeDisposable.add(Utils.onClick(createMeetUp, 1000) {
            MyApplication.putTrackMP(AcCreatMeetHome, null)
            meetOption?.showDialog()
        })

        compositeDisposable.add(Utils.onClick(createMeetUp2, 1000) {
            MyApplication.putTrackMP(AcCreatMeetHome, null)
            meetOption?.showDialog()
        })

        Utils.onClick(myBooking, 500) {
            // Navigation.addFragment(this,SavedPlaceFragment(),"SavedPlaceFragment",R.id.homeFram,true,false)
        }
        Utils.onClick(mySavedPlaces, 500) {
            MyApplication.putTrackMP(Constant.AcPlacesSavedFloating, null)
            Navigation.addFragment(this, SavedPlaceListFragment(), "SavedPlaceFragment", R.id.homeFram, true, false)
        }
    }


    private fun openProfile() {
        Navigation.addFragment(this, ProfileFragment(), TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false)
    }

    private fun setUp() {
        //setUpTabView()
        viewPager.offscreenPageLimit = 2
        viewPageAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = viewPageAdapter
        tablayout.setupWithViewPager(viewPager)

        if(PreferencesManager.get<Boolean>(PREFERENCE_SHOW_NOTI) == true) newNoti.visibility = View.VISIBLE

        if(PreferencesManager.get<Boolean>(PREFERENCE_MSG_NOTI) == true) newMessageNoti.visibility = View.VISIBLE

        profileViewmodel.getRandomQuote()
        addClickEvent()
        addListener()
        setUpTabView()
        addObserver()

    }

    private fun addListener() {
        profileViewmodel.getFullProfile()
        placeViewModel.getCategories()
        placeViewModel.getFacilities()
        placeViewModel.getCuisine()
        profileViewmodel.getMinVersion()
        PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?.let {
            populateData()
        }
    }

    private fun addObserver() {
        profileViewmodel.observeFullProfileChange().observe(this, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.let {
                        MyApplication.SID = it.cust_data?.sid
                        Log.i(TAG, " custdataInterest::MyApplication.SID ${MyApplication.SID}")
                        PreferencesManager.put(it, Constant.PREFRENCE_PROFILE)
                        profileViewmodel.getFullGenericList("interests", it.cust_data?.interests?.isEmpty() == true)
                        showUpdateEmailSnake()
                        populateData()
                    }
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(this, "Something went wrong!!!")
                }
            }

        })

        profileViewmodel.observeFullInterestChange().observe(this, Observer { interestRes ->
            Log.i(TAG, " fullinterest:: ${interestRes}")
            when(interestRes) {
                is ResultHandler.Success -> {
                    PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?.let {
                        if(!isNewUser) {
                            intrest.showInterest(interestRes.value?.definition, it.cust_data?.interests)
                        }
                    }
                }

                is ResultHandler.Failure -> {
                    Log.i(TAG, " Something went wrong while fetching interest")
                }
            }


        })

        profileViewmodel.observeInterestChange().observe(this, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    PreferencesManager.put(it.value, PREFRENCE_PROFILE)
                    Log.i(TAG, " cheeccking:::::: ")
                    var profile = supportFragmentManager.findFragmentByTag(TAG_PROFILE_FRAGMENT) as ProfileFragment?
                    profile?.populateView()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(this, "Something went wrong!!!")
                }
            }
        })

        profileViewmodel.observeEmergencyApi().observe(this, Observer<StateResource<String>> {
            it?.let {
                when(it.status) {
                    Status.LOADING -> {
                        //TODO(" add loader ")
                    }

                    Status.SUCCESS -> {
                        it.message
                        //parentView.showSnackBar("${it.message}")
                    }

                    Status.ERROR   -> {
                        //parentView.showSnackBar(it.message!!)
                    }
                }
            }
        })
        profileViewmodel.observeMinVersion().observe(this, {
            it?.let {
                when(it) {
                    is ResultHandler.Success -> {
                        val minVersion: Int = it.value?.get("version")?.asInt ?: 1
                        if(minVersion > BuildConfig.VERSION_CODE) {
                            val d = ForceUpdateAlert(this)
                            d.show()
                        }
                    }

                    is ResultHandler.Failure -> {
                    }
                }
            }
        })

        profileViewmodel.observeQuoteChange().observe(this, Observer {
            Log.i(TAG, " quote:::: $it")
            //motto.text = it
            //startFetchLocation()

        })

        placeViewModel.observeCategory().observe(this, Observer {
            PreferencesManager.put(it, PREFRENCE_CATEGORY)
        })

        placeViewModel.observeCuisine().observe(this, Observer {
            PreferencesManager.put(it, PREFRENCE_CUISINE)
        })
        placeViewModel.observeFacility().observe(this, Observer {
            Log.i(TAG, " facilityResponse::  1 ${it} ")
            PreferencesManager.put(it, PREFRENCE_FACILITY)
        })

        /*profileViewmodel.observeException().observe(this, Observer {
            Utils.handleException(this, it)
        })

        placeViewModel.observeException().observe(this, Observer {
            Utils.handleException(this, it)
        })*/

        MyApplication.observeShowNoti().observe(this, Observer {
            if(it?.get("type")?.equals(NotificationTypes.IN_APP_MESSAGE.type) == true) {
                newMessageNoti.visibility = View.VISIBLE
            } else {
                newNoti.visibility = View.VISIBLE
            }

        })
    }

    fun populateData() {
        var profile = PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)
        //profile?.cust_data?.profile_image_url?.let {
            //Utils.stickImage(this, dp, it, null)
            dp.loadImage(this,profile?.cust_data?.profile_image_url,R.drawable.ic_default_person)

        //}
        profile?.social?.getbadge()?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            ivDpBadge.setImageResource(firstOrNull.foreground)
        }
        tvLocation.text = "@${profile?.cust_data?.username}"
        if(profile?.cust_data?.first_name.isNullOrEmpty()) {
            name.text = profile?.cust_data?.username
        } else {
            name.text = profile?.cust_data?.first_name.plus(" ").plus(profile?.cust_data?.last_name)
        }

        notificationHandler?.checkIntent()
//        PreferencesManager.get<OtpResponse>(Constant.PREFRANCE_OTPRESPONSE).auth.first_time_login==true


        //checkIntent()

    }

    /* fun checkIntent() {
         Log.i(TAG, " HomeActIntent:::  ${intent.getStringExtra(NOTIFICATION_TYPE)}")
         var notificationType = intent.getStringExtra(NOTIFICATION_TYPE)
         notificationType?.let {
             when(it){
                 NotificationTypes.POST_LIKED.type ->{
                     var baseFragment: BaseFragment = DetailPostFragment()
                     Navigation.setFragmentData(
                         baseFragment,
                         "post_id",
                         intent.getStringExtra(ENTITY_ID)
                     )
                     Navigation.addFragment(
                         this,
                         baseFragment,
                         TAG_DETAIL_POST_FRAGMENT,
                         R.id.homeFram,
                         true,
                         false
                     )
                 }
                 NotificationTypes.FOLLOW.type->{
                     var baseFragment: BaseFragment = OtherProfileFragment.getInstance(intent.getStringExtra(ENTITY_ID))
                     Log.i(TAG, " checkSid = ${intent.getStringExtra(ENTITY_ID)}")
                     *//*baseFragment = Navigation.setFragmentData(
                        baseFragment,
                        OtherProfileFragment.OTHER_USER_ID,
                        intent.getStringExtra(ENTITY_ID)
                    )*//*
                    Navigation.addFragment(
                        this, baseFragment,
                        Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
                    )
                }
                NotificationTypes.POST_COMMENT.type->{
                    var baseFragment: BaseFragment = DetailPostFragment()
                    //var parentsIds = intent.getStringExtra(PARENT_ID)?.split(",")
                    Log.i(TAG, " parentsIds:: ${intent.getStringExtra(PARENT_ID)}")
                    Navigation.setFragmentData(
                        baseFragment,
                        "post_id",
                        intent.getStringExtra(PARENT_ID)
                    )
                    Navigation.setFragmentData(
                        baseFragment,
                        "comment_id",
                        intent.getStringExtra(ENTITY_ID)
                    )
                    Navigation.addFragment(
                        this,
                        baseFragment,
                        TAG_DETAIL_POST_FRAGMENT,
                        R.id.homeFram,
                        true,
                        false
                    )
                }
                NotificationTypes.COMMENT_LIKED.type  ->{
                    var baseFragment: BaseFragment = DetailPostFragment()
                    var parentsIds = intent.getStringExtra(PARENT_ID)?.split(",")
                    Log.i(TAG, " parentsIds:: ${parentsIds}")
                    if (parentsIds?.get(1).equals("null")) {
                        Navigation.setFragmentData(baseFragment, "post_id", parentsIds?.getOrNull(0))
                        Navigation.setFragmentData(
                            baseFragment,
                            "comment_id",
                            intent.getStringExtra(ENTITY_ID)
                        )
                        Navigation.addFragment(
                            this,
                            baseFragment,
                            TAG_DETAIL_POST_FRAGMENT,
                            R.id.homeFram,
                            true,
                            false
                        )
                    } else {
                        Navigation.setFragmentData(baseFragment, "post_id", parentsIds?.getOrNull(0))
                        Navigation.setFragmentData(baseFragment, "comment_id", parentsIds?.getOrNull(1))
                        Navigation.setFragmentData(
                            baseFragment,
                            "reply_comment_id",
                            intent.getStringExtra(ENTITY_ID)
                        )
                        Navigation.addFragment(
                            this,
                            baseFragment,
                            TAG_DETAIL_POST_FRAGMENT,
                            R.id.homeFram,
                            true,
                            false
                        )
                    }
                }
                NotificationTypes.REPLY_COMMENT.type ->{
                    var baseFragment: BaseFragment = DetailPostFragment()
                    var parentsIds = intent.getStringExtra(PARENT_ID)?.split(",")
                    Log.i(TAG, " parentsIds:: ${parentsIds}")
                    if (parentsIds?.getOrNull(1).equals("null")) {
                        Navigation.setFragmentData(baseFragment, "post_id", parentsIds?.getOrNull(0))
                        Navigation.setFragmentData(
                            baseFragment,
                            "comment_id",
                            intent.getStringExtra(ENTITY_ID)
                        )
                        Navigation.addFragment(
                            this,
                            baseFragment,
                            TAG_DETAIL_POST_FRAGMENT,
                            R.id.homeFram,
                            true,
                            false
                        )
                    } else {
                        Navigation.setFragmentData(baseFragment, "post_id", parentsIds?.getOrNull(0))
                        Navigation.setFragmentData(baseFragment, "comment_id", parentsIds?.getOrNull(1))
                        Navigation.setFragmentData(
                            baseFragment,
                            "reply_comment_id",
                            intent.getStringExtra(ENTITY_ID)
                        )
                        Navigation.addFragment(
                            this,
                            baseFragment,
                            TAG_DETAIL_POST_FRAGMENT,
                            R.id.homeFram,
                            true,
                            false
                        )
                    }
                }
            }
            *//*if (it.equals(POST_LIKED_TYPE)) {

            } else if (it.equals(FOLLOW_TYPE)) {

            } else if (it.equals(POST_COMMENT)) {

            } else if (it.equals(COMMENT_LIKED) || it.equals(REPLY_COMMENT)) {

            } else if(it.equals(MEETUP_INVITATION)){

            }*//*

        }

        intent.replaceExtras(null)

    }*/


    private fun setUpTabView() {
        var text = arrayListOf<String>("Feed", "Meet Ups", "Places")
        var images = arrayListOf<Int>(R.drawable.ic_post_tab, R.drawable.ic_meet_up, R.drawable.ic_places_tab)
        for(i in 0 until tablayout.getTabCount()) {
            val tab: TabLayout.Tab = tablayout.getTabAt(i)!!
            val v: View = LayoutInflater.from(this).inflate(R.layout.cuisine_text, null)
            v.findViewById<TextView>(R.id.tabtext).text = text.getOrNull(i)
            val img: ImageView = v.findViewById(R.id.tab_icon) as ImageView
            if(i != 1) img.setColorFilter(Color.argb(255, 77, 77, 77))
            img.setImageResource(images.get(i))
            tab.customView = v
        }

        tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(R.id.tabtext)
                        ?.setTextColor(ContextCompat.getColor(this@HomeActivityNew, R.color.primaryDark))
                //if(!(tab?.customView?.findViewById<TextView>(R.id.tabtext)?.text?.equals("Meet Ups")!!))
                tab?.customView?.findViewById<ImageView>(R.id.tab_icon)
                        ?.setColorFilter(ContextCompat.getColor(this@HomeActivityNew, R.color.primaryDark))
                Log.i(TAG, " checkingTAAB:: ${tab?.customView?.findViewById<TextView>(R.id.tabtext)?.text}")
                var selectedTabText = tab?.customView?.findViewById<TextView>(R.id.tabtext)?.text
                if(selectedTabText?.equals("Places") == true) {
                    MyApplication.putTrackMP(Constant.AcPlacesTab, null)
                    floatButton.visibility = View.VISIBLE
                    if(!Utils.isGpsOn(this@HomeActivityNew) || !Utils.checkPermission(this@HomeActivityNew, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            tablayout.selectTab(tablayout.getTabAt(1))
                        }, 1)

                        locationAlert?.showDialog()
                    } else {
                        MyApplication.putTrackMP(VwPlaceTab, null)
                    }
                } else if(selectedTabText?.equals("Meet Ups") == true) {
                    MyApplication.putTrackMP(VwMeetUpTab, null)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                if(tab?.customView?.findViewById<TextView>(R.id.tabtext)?.text?.equals("Places") == true) floatButton.visibility = View.GONE
                tab?.customView?.findViewById<TextView>(R.id.tabtext)
                        ?.setTextColor(ContextCompat.getColor(this@HomeActivityNew, R.color.blacktextColor))
                tab?.customView?.findViewById<ImageView>(R.id.tab_icon)
                        ?.setColorFilter(ContextCompat.getColor(this@HomeActivityNew, R.color.blacktextColor))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                var selectedTabText = tab?.customView?.findViewById<TextView>(R.id.tabtext)?.text
                Log.i(TAG, " tabReselected:: $selectedTabText")
                if(selectedTabText?.equals("Feed") == true) {
                    Log.i(TAG, " Tabreselected:: ")
                    (viewPageAdapter.instantiateItem(viewPager, 0) as TimeLineFragment).scrollUpPage()
                }
            }
        })

    }

    fun showMyToast(text: String) {
        cToastView.visibility = View.VISIBLE
        cToastText.text = text
    }

    fun changeToastText(text: String) {
        cToastText.text = text
    }

    fun hideMyToast() {
        cToastView.visibility = View.GONE
    }

    fun hideKeyBoard() {
        cToastView.visibility = View.GONE
    }


    class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount() = 3

        override fun getItem(position: Int): Fragment {
            return when(position) {
                0    -> TimeLineFragment()
                1    -> MeetUpNew()
                2    -> HomeFragment()
                else -> TimeLineFragment()
            }
        }
    }

    override fun onDestroy() {
        Log.i(TAG, " lifecycle::: onDestroy:: ")
        super.onDestroy()
        Log.i(TAG, " onDestroy:: ")
        compositeDisposable.clear()
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, " lifecycle::: onStart:: ")

    }

    override fun onResume() {
        Log.i(TAG, " lifecycle::: onResume:: ")
        sensorManager?.registerListener(sensorListener, sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        super.onResume()
        Log.i(TAG, " onResume:: ")
        notificationHandler?.checkIntent()

        FireBaseUtils.setOnline()

    }

    override fun onPause() {
        Log.i(TAG, " lifecycle::: onPause:: ")
        sensorManager!!.unregisterListener(sensorListener)
        FireBaseUtils.setOffline()
        super.onPause()
    }

    override fun onStop() {
        Log.i(TAG, " lifecycle::: onStop:: ")
        super.onStop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            1232 -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //startFetchLocation()
                    gpsUtils.turnGPSOn(1000)
                } else {

                    if(Build.VERSION.SDK_INT > 23 && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.i(TAG, " checkingratinale:: ${shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)}")
                        rationale()
                    }
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied for location", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            1242 -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //startFetchLocation()
                    gpsUtils.turnGPSOn(2000)
                } else {

                    if(Build.VERSION.SDK_INT > 23 && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Log.i(TAG, " checkingratinale:: ${shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)}")
                        rationale()
                    }
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied for location", Toast.LENGTH_SHORT)
                            .show();
                }
            }

        }
    }

    private fun rationale() {
        val builder: AlertDialog.Builder
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
        } else {
            builder = AlertDialog.Builder(this)
        }
        builder.setTitle(getString(R.string.mandatory_permission))
                .setMessage(getString(R.string.app_setting))
                .setPositiveButton("Proceed") { dialog, which ->
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", this.packageName, null)
                    intent.data = uri
                    startActivityForResult(intent, 1)
                }.setCancelable(true).setIcon(android.R.drawable.ic_dialog_alert).show()
    }


    fun initLocationRelatedView(location: Location?, isFromGps: Boolean) {
        if(location != null) {
            //placeViewModel.setLocation(LatLng(location.latitude,location.longitude))
            Log.i(TAG, "Foreground location: ${location.toText()}")
            PreferencesManager.put(JsonObject().apply {
                addProperty(LATITUDE, location.latitude)
                addProperty(LONGITUDE, location.longitude)
            }, PREFRENCE_LOCATION)

            val homeFragment = (viewPageAdapter.instantiateItem(viewPager, 2) as HomeFragment?)
            if(homeFragment?.isHomeSetUp == false) {
                Log.i(TAG, " settingUp homeFragment ")
                Handler(Looper.getMainLooper()).postDelayed({
                    (viewPageAdapter.instantiateItem(viewPager, 2) as HomeFragment).setUp(location)
                }, 100)
            }
            var addresses: List<Address?>? = null
            try {
                addresses = Geocoder(this, Locale.UK).getFromLocation(location.latitude, location.longitude, 1)
                if(addresses.isNotEmpty()) {
                    Log.i(TAG, "${addresses.getOrNull(0)?.featureName}--${
                        addresses.getOrNull(0)?.getAddressLine(0)
                    }----${addresses.getOrNull(0)?.adminArea};;;${addresses.getOrNull(0)?.locality} ")
                    var currentaddress: String? = addresses.getOrNull(0)?.featureName ?: addresses.getOrNull(0)
                            ?.getAddressLine(0)
                    Log.i(TAG, " currentaddress:: ${currentaddress}")
                    /*tvLocation.text = addresses.get(0).getAddressLine(0)
                        .substring(0, addresses.get(0).getAddressLine(0).lastIndexOf("-"))*/
                    this.location.text = addresses.getOrNull(0)?.getAddressLine(0)
                    //tvLocation.text = addresses.getOrNull(0)?.getAddressLine(0)
                }

            } catch(e: Exception) {
                FirebaseCrashlytics.getInstance().log("crash comes in getting location...")
                FirebaseCrashlytics.getInstance().recordException(RuntimeException(e))
                Log.e(TAG, " IOException::: getting Address from GeoCoder ")
            }

            if(isFromGps) profileViewmodel.sendLocation(location)
        }
    }

    fun getCurrentFragment(): Fragment? {
        Log.d("TAG", "getCurrentFragment: supportFragmentManager- ${supportFragmentManager.findFragmentById(R.id.homeFram)?.javaClass}")
        return supportFragmentManager.findFragmentById(R.id.homeFram)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, " onActivityResult::::::: ${requestCode} ${resultCode} ")
        try {
            super.onActivityResult(requestCode, resultCode, data)
            getCurrentFragment()?.onActivityResult(requestCode, resultCode, data)
        } catch(e: RuntimeException) {

        }
//        (supportFragmentManager.findFragmentByTag(RestaurantDetailFragment.TAG) as RestaurantDetailFragment?)?.let {
//            it.onActivityResult(requestCode, resultCode, data)
//        } ?: run {
//            if (resultCode == RESULT_OK) {
//                when (requestCode) {
//                    1000 -> fetchLastLocation()
//                    2000 -> Navigation.addFragment(
//                        this,
//                        MeetupOpenMemberMapFragment(),
//                        Constant.TAG_OPEN_FOR_MEETUP_MAP, R.id.homeFram, true, false
//                    )
//                    654 -> gpsFunction()
//
//                }
//
//            }
//        }
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        val backStackEntryCount = supportFragmentManager.backStackEntryCount
        if(backStackEntryCount == 0) {
            if(doubleBackToExitPressedOnce) {
                finishAffinity()
                return
            }
            doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Click back again to exit", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        } else {
            var baseFragment2: BaseFragment? = getCurrentFragment() as BaseFragment?
            if(baseFragment2?.onBackPressed() == true) super.onBackPressed()
            baseFragment2 = getCurrentFragment() as BaseFragment?
            baseFragment2?.onBackPageCome()
            if(supportFragmentManager.backStackEntryCount == 0) {
                (viewPageAdapter?.instantiateItem(viewPager, 1) as MeetUpNew).refreshThispage()
            }
        }


        //supportFragmentManager?.findFragmentByTag(MeetUpViewPageFragment.TAG) as baseFragment2.javaClass?


    }

}