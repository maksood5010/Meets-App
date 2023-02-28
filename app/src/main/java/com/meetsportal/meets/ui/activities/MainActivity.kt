package com.meetsportal.meets.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.material.tabs.TabLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import com.meetsportal.meets.BuildConfig
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.ActivityMainBinding
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
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.meetsportal.meets.viewmodels.PlacesViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    val TAG = this.javaClass.simpleName
    private var isNewUser: Boolean = false
    val profileViewmodel: UserAccountViewModel by viewModels()
    val placeViewModel: PlacesViewModel by viewModels()
    val meetUpViewModel: MeetUpViewModel by viewModels()

    private var _binding: ActivityMainBinding? = null
    val binding get() = _binding!!
    lateinit var viewPageAdapter: ViewPagerAdapter
    var locationText: String? = null
    var locationAlert: AllowLocationAlert? = null
    var viewInitializedLocation : Location? = null


    var notificationHandler: HandleNotification? = null
    var meetOption: CreateMeetOptionAlert? = null
    lateinit var intrest: InterestDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        Log.d(TAG, "onCreate: calling set content 2")
        supportActionBar?.hide()
        val a = PreferencesManager.get<OtpResponse>(Constant.PREFRANCE_OTPRESPONSE)
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
        initView()
        setUpTabView()
        clickEvents()
        setUp()

        Log.d(TAG, "onCreate: calling set content 3")
        FireBaseUtils.init()

    }

    private fun setUp() {
        addObserver()
//        binding.bottomNavigation.selectedItemId = R.id.action_feed
        profileViewmodel.getFullProfile()
        profileViewmodel.getMinVersion()
        placeViewModel.getCategories()
        placeViewModel.getFacilities()
        placeViewModel.getCuisine()

    }

    private fun addObserver() {
        profileViewmodel.observeFullProfileChange().observe(this, {
            when(it) {
                is ResultHandler.Success -> {
                    it.value?.let {
                        MyApplication.SID = it.cust_data?.sid
                        Log.i(TAG, " custdataInterest::MyApplication.SID ${MyApplication.SID}")
                        PreferencesManager.put(it, Constant.PREFRENCE_PROFILE)
                        profileViewmodel.getFullGenericList("interests", it.cust_data?.interests?.isEmpty() == true)
                    }
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(this, "Something went wrong!!!")
                }
            }

        })
        profileViewmodel.observeFullInterestChange().observe(this, { interestRes ->
            Log.i(TAG, " fullinterest:: ${interestRes}")
            when(interestRes) {
                is ResultHandler.Success -> {
                    PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.let {
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

        profileViewmodel.observeInterestChange().observe(this, {
            when(it) {
                is ResultHandler.Success -> {
                    PreferencesManager.put(it.value, Constant.PREFRENCE_PROFILE)
                    Log.i(TAG, " cheeccking:::::: ")
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(this, "Something went wrong!!!")
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

        placeViewModel.observeCategory().observe(this, {
            PreferencesManager.put(it, Constant.PREFRENCE_CATEGORY)
        })

        placeViewModel.observeCuisine().observe(this, {
            PreferencesManager.put(it, Constant.PREFRENCE_CUISINE)
        })
        placeViewModel.observeFacility().observe(this, {
            Log.i(TAG, " facilityResponse::  1 ${it} ")
            PreferencesManager.put(it, Constant.PREFRENCE_FACILITY)
        })
    }

    var gpsFunction: () -> Unit = {}
    lateinit var gpsUtils: GpsUtils
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
        if(Utils.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            this.gpsUtils.turnGPSOn(gpsCode)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
        }
    }

    var locationFinder: LocationFinder? = null
    var locationCallback: LocationCallback? = null
    fun fetchLastLocation() {
        Log.i(TAG, "fetchLastLocation: 76e")
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

    fun initLocationRelatedView(location: Location?, isFromGps: Boolean) {
        if(location != null) {
            //placeViewModel.setLocation(LatLng(location.latitude,location.longitude))
            Log.i(TAG, "Foreground location: ${location.toText()}")
            PreferencesManager.put(JsonObject().apply {
                addProperty(Constant.LATITUDE, location.latitude)
                addProperty(Constant.LONGITUDE, location.longitude)
            }, Constant.PREFRENCE_LOCATION)

            val homeFragment = (viewPageAdapter.instantiateItem(binding.homeViewPager, 2) as HomeFragment?)
            if(homeFragment?.isHomeSetUp == false) {
                Log.i(TAG, " settingUp homeFragment ")
                Handler(Looper.getMainLooper()).postDelayed({
                    viewInitializedLocation = location
                    (viewPageAdapter.instantiateItem(binding.homeViewPager, 2) as HomeFragment).setUp(location)
                    (viewPageAdapter.instantiateItem(binding.homeViewPager, 3) as MeetupOpenMemberMapFragment?)?.fetchCurrentLocation()
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
                    locationText = addresses.getOrNull(0)?.getAddressLine(0)
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
    private fun clickEvents() {
//        binding.bottomNavigation.setOnNavigationItemSelectedListener {
//            when(it.itemId) {
//                R.id.action_feed     -> {
//                    replaceFragment(TimeLineFragment(), false)
//                }
//
//                R.id.action_meetup   -> {
//                    replaceFragment(MeetUpNew(),false)
//                }
//
//                R.id.action_places   -> {
//                    replaceFragment(HomeFragment(),false)
//
//                }
//
//                R.id.action_discover -> {
//                    replaceFragment(MeetupOpenMemberMapFragment(),false)
//                }
//
//                R.id.action_profile  -> {
//                    replaceFragment(ProfileFragment(),false)
//                }
//            }
//            return@setOnNavigationItemSelectedListener true
//        }
    }

    fun replaceFragment(toFragment: Fragment, stack: Boolean) {
        var ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.homeFram, toFragment, toFragment.javaClass.simpleName)
        if(stack) {
            ft.addToBackStack(toFragment.javaClass.simpleName)
        }
        ft.commit()
        //fragDetail(activity)
    }
    private fun setUpTabView() {
        val images = arrayListOf<Int>(R.drawable.ic_home, R.drawable.ic_meet_tab, R.drawable.ic_places_tab_new,R.drawable.ic_discover,R.drawable.ic_menu)
        for(i in 0 until binding.tlTabs.tabCount) {
            val tab: TabLayout.Tab = binding.tlTabs.getTabAt(i)!!
//            val v: View = LayoutInflater.from(this).inflate(R.layout.home_tab, null)
//            v.findViewById<TextView>(R.id.tabtext).text = text.getOrNull(i)
//            val img: ImageView = v.findViewById(R.id.tabIcon) as ImageView
//            if(i != 1) img.setColorFilter(Color.argb(255, 77, 77, 77))
//            img.setImageResource(images.get(i))
//            tab.customView = v
            tab.setIcon(images.get(i))

        }
//        binding.tlTabs.visibility=View.GONE
    }

    private fun initView() {
        viewPageAdapter = ViewPagerAdapter(supportFragmentManager)
        binding.homeViewPager.offscreenPageLimit = 4
        binding.homeViewPager.adapter = viewPageAdapter
        binding.tlTabs.setupWithViewPager(binding.homeViewPager)
        notificationHandler = HandleNotification(this, meetUpViewModel)
        notificationHandler?.notificationObserver()
        notificationHandler?.checkIntent()

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

        binding.homeViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                try {
                    when(position){
                        0->{
                            Handler(Looper.getMainLooper()).postDelayed({
                                (viewPageAdapter.instantiateItem(binding.homeViewPager, 0) as TimeLineFragment?)?.refreshImage()
                            }, 1)
                        }
                        2->{
                            Handler(Looper.getMainLooper()).postDelayed({
                                (viewPageAdapter.instantiateItem(binding.homeViewPager, 2) as HomeFragment?)?.refreshImage()
                            }, 1)
                        }
                        3->{
                            Handler(Looper.getMainLooper()).postDelayed({
                                (viewPageAdapter.instantiateItem(binding.homeViewPager, 3) as MeetupOpenMemberMapFragment?)?.refreshImage()
                            }, 1)
                        }
                        4->{
                            Handler(Looper.getMainLooper()).postDelayed({
                                (viewPageAdapter.instantiateItem(binding.homeViewPager, 4) as ProfileFragment?)?.populateView()
                            }, 1)
                        }
                    }
                } catch(e: Exception) {
                    FirebaseCrashlytics.getInstance().log("crashing in viewpager")
                    FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(e)))
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })

        binding.tlTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                popBackStackAll()
                when(tab?.position){
                    2->{
                        MyApplication.putTrackMP(Constant.AcPlacesTab, null)
                        if(!Utils.isGpsOn(this@MainActivity) || !Utils.checkPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                binding.tlTabs.selectTab(binding.tlTabs.getTabAt(0))
                            }, 1)
                            locationAlert?.showDialog()
                        } else {
                            if(viewInitializedLocation == null){
                                fetchLastLocation()
                            }
                            MyApplication.putTrackMP(Constant.VwPlaceTab, null)
                        }
                    }
                    3->{
                        if(!Utils.isGpsOn(this@MainActivity) || !Utils.checkPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            locationAlert?.showDialog()
                        } else {
                            if(viewInitializedLocation == null){
                                fetchLastLocation()
                            }
                            MyApplication.putTrackMP(Constant.VwPlaceTab, null)
                        }
                    }
                }

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                Log.d(TAG, "onTabReselected: ${tab?.position}")
                if(supportFragmentManager.backStackEntryCount==0){
                when(tab?.position){
                    0->{
                        (viewPageAdapter.instantiateItem(binding.homeViewPager, 0) as TimeLineFragment).scrollUpPage()
                    }
                }
                }else{
                    popBackStackAll()
                }
            }

        })

        meetOption = CreateMeetOptionAlert(this) {
            MyApplication.smallVibrate()
            var baseFragment = MeetUpViewPageFragment.getInstanceForOpenMeetUp(it)
            Navigation.addFragment(this, baseFragment, MeetUpViewPageFragment.TAG, R.id.homeFram, true, true)
        }


        intrest = InterestDialog(this) {
            Log.i(TAG, " INTEREST:: ${it} ")
            profileViewmodel.updateInterest(it)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            Log.d(TAG, "initView: backStackEntryCount ${supportFragmentManager.backStackEntryCount}")
            (getCurrentFragment() as BaseFragment?)?.hideBottom()
            if(supportFragmentManager.backStackEntryCount==0){
                binding?.tlTabs?.visibility= View.VISIBLE
            }
        }
        if(Utils.checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) fetchLastLocation()
    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount() = 4

        private val tabTitles = arrayOf("Home", "Meetup", "Places","Discover","Profile")
        override fun getPageTitle(position: Int): CharSequence? {
            return tabTitles[position]
        }
        override fun getItem(position: Int): Fragment {
            return when(position) {
                0    -> TimeLineFragment()
                1    -> MeetUpNew()
                2    -> HomeFragment()
                3    -> MeetupOpenMemberMapFragment()
//                4    -> ProfileFragment()
                else -> TimeLineFragment()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            1232 -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //startFetchLocation()
                    Log.i(TAG, "onRequestPermissionsResult: gpuUtils")
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
            Log.d(TAG, "onBackPressed:name 1 ${getCurrentFragment()?.javaClass}")
            Log.d(TAG, "onBackPressed:name 2 ${baseFragment2?.hideNavBar()}")
            if(baseFragment2?.hideNavBar()==true){
                binding?.tlTabs?.visibility = View.GONE
            }else{
                binding?.tlTabs?.visibility = View.VISIBLE
            }
            if(supportFragmentManager.backStackEntryCount == 0){
                refreshViewPager()
            }
        }

    }

    private fun refreshViewPager() {
        if(supportFragmentManager.backStackEntryCount == 0){
            (viewPageAdapter.instantiateItem(binding.homeViewPager,0) as TimeLineFragment).refreshTimeLine()
            (viewPageAdapter?.instantiateItem(binding.homeViewPager, 1) as MeetUpNew).refreshThispage()
        }
    }
    fun popBackStack(){
        Log.d(TAG, "popBackStack: in main")
        supportFragmentManager.popBackStack()
        refreshViewPager()
    }
    fun popBackStackAll(){
        try {
            supportFragmentManager.apply {
                for (i in fragments.indices) {
                    if(i in 0..5)
                        continue
                    beginTransaction().remove(fragments.get(i)).commitAllowingStateLoss()
                }
                popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log("popBackStackAll crashed")
            FirebaseCrashlytics.getInstance().recordException(Throwable(e))
        }
        refreshViewPager()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, " onActivityResult::::::: ${requestCode} ${resultCode} ")
        try {
            super.onActivityResult(requestCode, resultCode, data)
            getCurrentFragment()?.onActivityResult(requestCode, resultCode, data)
        } catch(e: RuntimeException) {

        }
    }

    val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
    }


}