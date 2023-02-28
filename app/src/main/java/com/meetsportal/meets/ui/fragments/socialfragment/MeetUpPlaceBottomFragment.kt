package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fenchtose.tooltip.Tooltip
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.*
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetUpRestaurantBottomBinding
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.models.MeetsPlace
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.places.FetchPlacesResponse
import com.meetsportal.meets.networking.places.SearchPlaceResponse
import com.meetsportal.meets.overridelayout.SpaceItemDecoration
import com.meetsportal.meets.ui.activities.HomeActivityNew
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment.MeetType
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.PlacesViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@AndroidEntryPoint
class MeetUpPlaceBottomFragment : BaseFragment() {

    val TAG = MeetUpPlaceBottomFragment::class.simpleName

    //var selectedPlace = mutableSetOf<String?>()
    var selectedPlace = mutableMapOf<String?,MeetsPlace?>()

    var meetPager : MeetUpViewPageFragment? = null
    val placeViewModel  : PlacesViewModel by viewModels()
    val userViewModel : UserAccountViewModel by viewModels()
    lateinit var customPlaceAdapter : MeetAddressListAdapter<MeetUpPlaceBottomFragment>

    //val PLACE_ID: String by lazy { Navigation.getFragmentData(this, "_id") }

    val categoryList = CategoryResponse()
    val categoryAdapter : MeetUpPlaceCategoriesAdapter by lazy { MeetUpPlaceCategoriesAdapter(requireActivity(),categoryList,{name, key,isSelected ->
        Log.i(TAG," keyANdSelection::: $key -- $isSelected")
        binding.ivMap.visibility = View.GONE
        binding.tvAddPlaceText.visibility = View.GONE
        binding.tvSearchResult.text = name
        if(isSelected) {
            binding.rvSearch.adapter = categoryResultAdapter.withLoadStateFooter(TimelineFooterStateAdapter{
                categoryResultAdapter.retry()
            })
            placeViewModel.getBestMeetPagingData(StringBuilder(), StringBuilder(), StringBuilder().append(key), null,Utils.getCountryCode(requireContext()))
        }
        //else
            //binding.llRecycler.visibility = View.GONE
    },{position,triple->
        if(position in 0..2){
            binding.ivMap.visibility = View.GONE
            binding.tvAddPlaceText.visibility = View.GONE
            binding.tvSearchResult.text = triple?.second
            when(position){
                0 -> {
                    placeViewModel.getBestMeetUp(40,
                            PreferencesManager.get<JsonObject>(Constant.PREFRENCE_LOCATION)?.get(Constant.LATITUDE)?.asDouble,
                            PreferencesManager.get<JsonObject>(Constant.PREFRENCE_LOCATION)?.get(Constant.LONGITUDE)?.asDouble,
                            true)
                    binding.rvSearch.adapter = trandingAdapter
                }
                1 ->{
                    binding.ivMap.visibility = View.VISIBLE
                    binding.tvAddPlaceText.visibility = View.VISIBLE
                    getAddress(null)
                    binding.rvSearch.adapter = customPlaceAdapter
                    Log.i(TAG," checkingCustomPlaceCount:: ${customPlaceAdapter.list.size}")
                    if(customPlaceAdapter.list.size > 0){
                        binding.noData.visibility = View.GONE
                    }

                }
                2 ->{
                    placeViewModel.getSavedPlaceWOpaging()
                    binding.rvSearch.adapter = savedAdapter
                }

            }

        }
    })
    }

    var selectedPlaceAdapter : SelectedPlaceInPlacePageAdapter? = null

    val trandingPlaceList = FetchPlacesResponse()
    lateinit var trandingAdapter: MeetUpPlaceTrandingAdapter

    val savedPlaceList = FetchPlacesResponse()
    lateinit var savedAdapter: MeetUpPlaceSavedAdapter

    lateinit var categoryResultAdapter : MeetUpPlaceCategoryResultAdapter

    lateinit var searchPlaceAdapter : MeetSearchPlaceAdapter
    var searchPlaceList = SearchPlaceResponse()

    private var _binding: FragmentMeetUpRestaurantBottomBinding? = null
    private val binding get() = _binding!!
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    val customPlace = ArrayList<AddressModel>()


    companion object{
        fun getInstance():MeetUpPlaceBottomFragment{
            return MeetUpPlaceBottomFragment()
        }

        fun getInstance(id : String?):MeetUpPlaceBottomFragment{
            return MeetUpPlaceBottomFragment().apply {
                arguments= Bundle().apply {
                    putString("id",id)
                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMeetUpRestaurantBottomBinding.inflate(inflater, container, false)
        return binding.root
        //return inflater.inflate(R.layout.fragment_meet_up_restaurant_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setupUI(binding.root)>
        initView(view)
        setUp()
        setObserver()
    }
    
    private fun initView(view: View) {

        binding.tvBack.setRoundedColorBackground(requireActivity(),R.color.dark_transparent)
        addSpanableAddress()

        binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(),R.color.gray1,)
        binding.llRecycler.visibility = View.VISIBLE
        selectedPlaceAdapter = SelectedPlaceInPlacePageAdapter(requireActivity(),selectedPlace,customPlace)
        meetPager = activity?.supportFragmentManager?.findFragmentByTag(MeetUpViewPageFragment.TAG) as MeetUpViewPageFragment?
        customPlaceAdapter = MeetAddressListAdapter(requireActivity(),this,customPlace,selectedPlace,MeetType.PRIVATE,{

        })
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        binding.rvSearch.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.selectedPlace.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL,false)
        binding.selectedPlace.addItemDecoration(SpaceItemDecoration(0,Utils.dpToPx(20f,resources)))
        binding.selectedPlace.adapter = selectedPlaceAdapter


        trandingAdapter = MeetUpPlaceTrandingAdapter(requireActivity(),trandingPlaceList,selectedPlace,customPlace,
            MeetType.PRIVATE,{
            refreshedChecked()
        })
        savedAdapter = MeetUpPlaceSavedAdapter(requireActivity(),savedPlaceList,selectedPlace,customPlace,MeetType.PRIVATE,{
            refreshedChecked()
        })

        categoryResultAdapter = MeetUpPlaceCategoryResultAdapter(requireActivity(),binding,selectedPlace,customPlace,MeetType.PRIVATE,{
            refreshedChecked()
        })

        binding.rvSearch.adapter = trandingAdapter
        binding.rvCategories.adapter = categoryAdapter
        Utils.showToolTips(requireActivity(),binding.rvCategories, binding.root, Tooltip.BOTTOM,"View places by categories","rvCategoriesPrivate"){
        }
        placeViewModel.getBestMeetUp(40,
            PreferencesManager.get<JsonObject>(Constant.PREFRENCE_LOCATION)?.get(Constant.LATITUDE)?.asDouble,
            PreferencesManager.get<JsonObject>(Constant.PREFRENCE_LOCATION)?.get(Constant.LONGITUDE)?.asDouble,
            true)

        searchPlaceAdapter = MeetSearchPlaceAdapter(requireActivity(),searchPlaceList,selectedPlace,customPlace,MeetType.PRIVATE,{
            refreshedChecked()
        })


    }

    private fun setUp() {
        arguments?.getString("id")?.let {
            Log.i("TAG"," trackplaceId:::: 3 ${it}")
            placeViewModel.getPlace(it,null)
        }
        binding.ivMap.setOnClickListener { addAddress() }

        placeViewModel.getCategories()
        binding.tvNext.setOnClickListener {
            meetPager?.binding?.meetUpViewpager?.setCurrentItem(2,true)
        }
        binding.tvBack.setOnClickListener {
            meetPager?.binding?.meetUpViewpager?.setCurrentItem(0,true)
        }
        binding.search.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcPlaceSearchBar,null)
        }
        binding.search.count {
            Log.i("TAG", " fatfat:: $it")
            if ((it.toInt()) > 2) {
                placeViewModel.searchPlace(binding.search.text.toString().trim())
            } else {
                //binding.llRecycler.visibility = View.GONE
            }
        }
    }


    fun addSpanableAddress(){
        var checkedText = "Use the map icon to add custom places or click here to add"
        var spannable = SpannableString(checkedText)
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {

            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        spannable.setSpan(clickableSpan,checkedText.indexOf("click here"),checkedText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan( ForegroundColorSpan(ContextCompat.getColor(requireContext(),R.color.primaryDark)),checkedText.indexOf("click"),checkedText.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvAddPlaceText.setText(spannable, TextView.BufferType.SPANNABLE)
        binding.tvAddPlaceText.setMovementMethod(LinkMovementMethod.getInstance())
        binding.tvAddPlaceText.onClick({
            addAddress()
        })

    }

    fun addAddress(){
        /*if (Utils.checkPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            //startFetchLocation()
            Log.i(TAG," responseGpscame::  4  ")
            gpsUtils.turnGPSOn(2001)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                2001
            );
        }*/

        if (Utils.checkPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            (activity as MainActivity?)?.enableLocationStuff(654, 655){
                toInviteMeetMapFragment()
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                655
            )
        }

    }

    fun getAddress(addressFromLatLong: AddressModel?) {
        userViewModel.getAddresses(addressFromLatLong)
    }

    fun refreshedChecked(fromRemote: Boolean = false){

        trandingAdapter.notifyDataSetChanged()
        savedAdapter.notifyDataSetChanged()
        searchPlaceAdapter.notifyDataSetChanged()
        categoryResultAdapter.notifyDataSetChanged()
        customPlaceAdapter.notifyDataSetChanged()
        categoryAdapter.notifyDataSetChanged()
        Log.i(TAG," selectedPlace::: $selectedPlace")
        selectedPlaceAdapter?.notifyDataSetChanged()

        if(selectedPlace.size.plus(customPlace.size) > 0){
            binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(),R.color.primaryDark)
            binding.addFriendOrNext.text = "Add ".plus(selectedPlace.size.plus(customPlace.size)).plus(" Place")
            binding.addFriendOrNext.onClick( {
                MyApplication.putTrackMP(Constant.AcCreateMeetAddPlace, null)
                meetPager?.binding?.meetUpViewpager?.setCurrentItem(2,true)
            })
        }
        else{
            binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(),R.color.gray1,)
            binding.addFriendOrNext.text = "Add "
            binding.addFriendOrNext.setOnClickListener { null }

        }

        if(!fromRemote)
            (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,4) as MeetUpDateNTimeBottomFragment?)?.notifyPlaceItemChanges()
            (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,2) as MeetUpSelectedPlaceBottomFragment?)?.notifySelectedPlaceItemChanges()

    }

    fun setScroll() {
        Log.d(TAG, "setScroll: onPageSelected: ")
        if(!categoryList.definition.isNullOrEmpty()) {
            compositeDisposable.add(Flowable.interval(Constant.interval, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                if(binding.rvCategories.scrollState != RecyclerView.SCROLL_STATE_SETTLING) {
//                            val absSpeed: Int =
                    val d = abs(Constant.speed)
                    binding.rvCategories.smoothScrollBy(d, d) {
                        it
                    }
                }
            },{
                        Log.e(AddPlaceToMeetUp.TAG, "setUp: OnBoard Interval Error handled")
                        FirebaseCrashlytics.getInstance().log("OnBoard Interval Error handled")
                        FirebaseCrashlytics.getInstance().recordException(it)

            }))
        }
        binding.rvCategories.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState== RecyclerView.SCROLL_STATE_DRAGGING){
                    MyApplication.putTrackMP(Constant.AcSlideCategories,null)
                    compositeDisposable.clear()
                }
            }
        })
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }
    private fun setObserver() {
        placeViewModel.observeCategory().observe(viewLifecycleOwner,{
            categoryList.definition = it.definition
            categoryAdapter.notifyDataSetChanged()
            Log.i(TAG," checkinfcategoryresponse::: ")
            (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,4) as MeetUpDateNTimeBottomFragment?)?.initSelectedPlaceDataSource(selectedPlace,customPlace)
            (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,2) as MeetUpSelectedPlaceBottomFragment?)?.initSelectedPlaceSource(selectedPlace,customPlace)

        })

        placeViewModel.observeSinglePlace().observe(viewLifecycleOwner,Observer{
            when(it){
                is ResultHandler.Success -> {
                    Log.i(TAG," observeSinglePlace::: ")
                    selectedPlace.put(it.value?.slug,it.value?.toMeetUpPlace())
                    refreshedChecked()
                }
                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(),"Something went wrong...")
                }
            }
        })
        userViewModel.observeGetAddress().observe(viewLifecycleOwner,{ result->
            when(result){
                is ResultHandler.Success -> {
                    result.value.first?.let {
                        Log.i(TAG," checkingAddress::: ")
                        customPlaceAdapter.setListData(it,result.value.second)
                        refreshedChecked()
                        if(it.isNotEmpty())
                            binding.noData.visibility = View.GONE
                    }

                }
                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(),"Something went Wrong")
                }
            }

        })

        placeViewModel.observerPlaces().observe(viewLifecycleOwner,{
//            binding.rvSearch.adapter = trandingAdapter
            trandingPlaceList.clear()
            trandingPlaceList.addAll(it)
            trandingAdapter.notifyDataSetChanged()
            Log.i(TAG, "setObserver: checkingCount:: ${it.size}")
            if(it.isNotEmpty())
                binding.noData.visibility = View.GONE

        })
        placeViewModel.observerSavedPlace().observe(viewLifecycleOwner,{
            savedPlaceList.clear()
            savedPlaceList.addAll(it)
            savedAdapter.notifyDataSetChanged()
            if(it.isNotEmpty())
                binding.noData.visibility = View.GONE
        })
        placeViewModel.observePlaceSearch().observe(viewLifecycleOwner, Observer {
            binding.llRecycler.visibility = View.VISIBLE
            searchPlaceList.clear()
            searchPlaceList.addAll(it)
            binding.tvSearchResult.text = "Search Result"
            binding.rvSearch.adapter = searchPlaceAdapter
            searchPlaceAdapter.notifyDataSetChanged()
            if(binding.rvSearch.adapter?.itemCount?.compareTo(0) ==  1)
                binding.noData.visibility = View.GONE
            else
                binding.noData.visibility = View.VISIBLE
            //binding.rvSearch.adapter =
        })

        placeViewModel.observePlacePagingDaraSource().observe(viewLifecycleOwner, Observer {
            categoryResultAdapter.submitData(lifecycle,it)
            binding.llRecycler.visibility = View.VISIBLE
            binding.rvSearch.adapter = categoryResultAdapter
            Log.i(TAG," countcount:: ${binding.rvSearch.adapter?.itemCount}")
            if(binding.rvSearch.adapter?.itemCount?.compareTo(1) == -1 )
                binding.noData.visibility = View.GONE
            //Here Hiding and showing NoDATA layout implemented in adapter
        })

    }

    fun toInviteMeetMapFragment(){
        (activity as MainActivity?)?.fetchLastLocation()
        val inviteMap:BaseFragment = InviteMeetMapFragment( this@MeetUpPlaceBottomFragment)
//        val inviteMap:BaseFragment = InviteMeetMapFragment<MeetUpPlaceBottomFragment>()
        Navigation.addFragment(
            activity as Activity,
            inviteMap,
            inviteMap.javaClass.simpleName,
            R.id.homeFram,
            stack = true, needAnimation = false
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG," onRequestPermissionsResult::: $requestCode  ")
        when (requestCode) {
            655 -> {
                Log.i(TAG," loging::: 1")
                var location = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if (grantResults.isNotEmpty() && location) {
                    toInviteMeetMapFragment()
                } else if (Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    Log.i(TAG," loging::: 5")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG," responseGpscame::  3  ${requestCode}")
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

}