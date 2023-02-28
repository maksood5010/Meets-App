package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.app.Activity
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
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.meetsportal.meets.viewmodels.PlacesViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@AndroidEntryPoint
class AddPlaceToMeetUp : BaseFragment() {


    val placeViewModel: PlacesViewModel by viewModels()
    val userViewModel: UserAccountViewModel by viewModels()
    val meetupViewModel: MeetUpViewModel by viewModels()

    var selectedPlace = mutableMapOf<String?, MeetsPlace?>()

    var selectedPlaceAdapter: SelectedPlaceInPlacePageAdapter? = null


    val categoryList = CategoryResponse();
    val categoryAdapter: MeetUpPlaceCategoriesAdapter by lazy {
        MeetUpPlaceCategoriesAdapter(requireActivity(), categoryList, { name, key, isSelected ->
            Log.i("TAG", " keyANdSelection::: $key -- $isSelected")
            binding.ivMap.visibility = View.GONE
            binding.tvAddPlaceText.visibility = View.GONE
            binding.tvSearchResult.text = name
            if(isSelected) {
                binding.rvSearch.adapter = categoryResultAdapter.withLoadStateFooter(TimelineFooterStateAdapter {
                    categoryResultAdapter.retry()
                })
                placeViewModel.getBestMeetPagingData(StringBuilder(), StringBuilder(), StringBuilder().append(key), null, Utils.getCountryCode(requireContext()))
            }
            //else
            //binding.llRecycler.visibility = View.GONE
        }, { position, triple ->
            if(position in 0..2) {
                binding.ivMap.visibility = View.GONE
                binding.tvAddPlaceText.visibility = View.GONE
                binding.tvSearchResult.text = triple?.second
                when(position) {
                    0 -> {
                        placeViewModel.getBestMeetUp(40, PreferencesManager.get<JsonObject>(Constant.PREFRENCE_LOCATION)
                                ?.get(Constant.LATITUDE)?.asDouble, PreferencesManager.get<JsonObject>(Constant.PREFRENCE_LOCATION)
                                ?.get(Constant.LONGITUDE)?.asDouble, true)
                        binding.rvSearch.adapter = trandingAdapter
                    }

                    1 -> {
                        binding.ivMap.visibility = View.VISIBLE
                        binding.tvAddPlaceText.visibility = View.VISIBLE
                        getAddress(null)
                        binding.rvSearch.adapter = customPlaceAdapter

                    }

                    2 -> {
                        placeViewModel.getSavedPlaceWOpaging()
                        binding.rvSearch.adapter = savedAdapter
                    }

                }

            }
        })
    }


    val trandingPlaceList = FetchPlacesResponse()
    lateinit var trandingAdapter: MeetUpPlaceTrandingAdapter

    val savedPlaceList = FetchPlacesResponse()
    lateinit var savedAdapter: MeetUpPlaceSavedAdapter

    lateinit var categoryResultAdapter: MeetUpPlaceCategoryResultAdapter

    lateinit var searchPlaceAdapter: MeetSearchPlaceAdapter
    var searchPlaceList = SearchPlaceResponse()
    lateinit var customPlaceAdapter: MeetAddressListAdapter<AddPlaceToMeetUp>


    private var _binding: FragmentMeetUpRestaurantBottomBinding? = null
    private val binding get() = _binding!!

    val customPlace = ArrayList<AddressModel>()

    var prePlace = ArrayList<String?>()
    var prePlaceElastic = ArrayList<String?>()
    var preCustomPlace = ArrayList<String?>()
    var compositeDisposable: CompositeDisposable = CompositeDisposable()


    companion object {

        val TAG = AddPlaceToMeetUp::class.simpleName!!

        fun getInstance(meetId: String?, prePlace: ArrayList<String?>?, preCustomPlace: ArrayList<String?>, prePlaceElastic: ArrayList<String?>? = null): AddPlaceToMeetUp {
            return AddPlaceToMeetUp().apply {
                arguments = Bundle().apply {
                    putStringArrayList("prePlace", prePlace)
                    putStringArrayList("preCustomPlace", preCustomPlace)
                    putStringArrayList("prePlaceElastic", prePlaceElastic)
                    putString("meetId", meetId)
                }
            }
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    fun setScroll() {
        Log.d(TAG, "setScroll: onPageSelected: ")
        if(!categoryList.definition.isNullOrEmpty()) {
            compositeDisposable.add(Flowable.interval(Constant.interval, TimeUnit.MILLISECONDS)
                    .onBackpressureDrop().observeOn(AndroidSchedulers.mainThread()).subscribe({
                        if(binding.rvCategories.scrollState != RecyclerView.SCROLL_STATE_SETTLING) {
//                            val absSpeed: Int =
                            val d = abs(Constant.speed)
                            binding.rvCategories.smoothScrollBy(d, d) {
                                it
                            }
                        }
                    }, {
                        Log.e(TAG, "setUp: OnBoard Interval Error handled")
                        FirebaseCrashlytics.getInstance().log("OnBoard Interval Error handled")
                        FirebaseCrashlytics.getInstance().recordException(it)

                    }))
        }
        binding.rvCategories.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    compositeDisposable.clear()
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        prePlace.addAll(arguments?.getStringArrayList("prePlace") ?: ArrayList())
        prePlaceElastic.addAll(arguments?.getStringArrayList("prePlaceElastic") ?: ArrayList())
        preCustomPlace.addAll(arguments?.getStringArrayList("preCustomPlace") ?: ArrayList())
        binding.tvBack.setRoundedColorBackground(requireActivity(), R.color.dark_transparent)
        addSpanableAddress()
        binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(), R.color.gray1)
        binding.llRecycler.visibility = View.VISIBLE
        selectedPlaceAdapter = SelectedPlaceInPlacePageAdapter(requireActivity(), selectedPlace, customPlace)
        customPlaceAdapter = MeetAddressListAdapter(requireActivity(), this, customPlace, selectedPlace, MeetUpViewPageFragment.MeetType.PRIVATE, {

        }, prePlace, preCustomPlace)
        binding.rvCategories.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvSearch.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.selectedPlace.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.selectedPlace.addItemDecoration(SpaceItemDecoration(0, Utils.dpToPx(20f, resources)))
        binding.selectedPlace.adapter = selectedPlaceAdapter


        trandingAdapter = MeetUpPlaceTrandingAdapter(requireActivity(), trandingPlaceList, selectedPlace, customPlace, MeetUpViewPageFragment.MeetType.PRIVATE, {
            refreshedChecked()
        }, prePlace, preCustomPlace)
        savedAdapter = MeetUpPlaceSavedAdapter(requireActivity(), savedPlaceList, selectedPlace, customPlace, MeetUpViewPageFragment.MeetType.PRIVATE, {
            refreshedChecked()
        }, prePlace, preCustomPlace)

        categoryResultAdapter = MeetUpPlaceCategoryResultAdapter(requireActivity(), binding, selectedPlace, customPlace, MeetUpViewPageFragment.MeetType.PRIVATE, {
            refreshedChecked()
        }, prePlace, preCustomPlace)

        binding.rvSearch.adapter = trandingAdapter
        binding.rvCategories.adapter = categoryAdapter
        placeViewModel.getBestMeetUp(40, PreferencesManager.get<JsonObject>(Constant.PREFRENCE_LOCATION)
                ?.get(Constant.LATITUDE)?.asDouble, PreferencesManager.get<JsonObject>(Constant.PREFRENCE_LOCATION)
                ?.get(Constant.LONGITUDE)?.asDouble, true)

        searchPlaceAdapter = MeetSearchPlaceAdapter(requireActivity(), searchPlaceList, selectedPlace, customPlace, MeetUpViewPageFragment.MeetType.PRIVATE, {
            refreshedChecked()
        }, prePlaceElastic, preCustomPlace)

    }

    private fun setUp() {
        binding.ivMap.setOnClickListener { addAddress() }

        placeViewModel.getCategories()
        binding.tvNext.setOnClickListener {
            //todo: do add place in meetup

        }
        binding.tvBack.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.search.count {
            Log.i("TAG", " fatfat:: $it")
            if((it.toInt()) > 2) {
                placeViewModel.searchPlace(binding.search.text.toString().trim())
            } else {
                //binding.llRecycler.visibility = View.GONE
            }
        }
    }

    fun addSpanableAddress() {
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
        spannable.setSpan(clickableSpan, checkedText.indexOf("click here"), checkedText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.primaryDark)), checkedText.indexOf("click"), checkedText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvAddPlaceText.setText(spannable, TextView.BufferType.SPANNABLE)
        binding.tvAddPlaceText.setMovementMethod(LinkMovementMethod.getInstance())
        binding.tvAddPlaceText.onClick({
            addAddress()
        })

    }

    fun addAddress() {


        if(Utils.checkPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            (activity as MainActivity?)?.enableLocationStuff(654, 655) {
                toInviteMeetMapFragment()
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 655)
        }

    }

    fun toInviteMeetMapFragment() {
        (activity as MainActivity?)?.fetchLastLocation()
        val inviteMap: BaseFragment = InviteMeetMapFragment(this@AddPlaceToMeetUp)
//        val inviteMap:BaseFragment = InviteMeetMapFragment<MeetUpPlaceBottomFragment>()
        Navigation.addFragment(activity as Activity, inviteMap, inviteMap.javaClass.simpleName, R.id.homeFram, stack = true, needAnimation = false)
    }

    fun getAddress(addressFromLatLong: AddressModel?) {
        userViewModel.getAddresses(addressFromLatLong)
    }

    fun refreshedChecked(fromRemote: Boolean = false) {

        trandingAdapter.notifyDataSetChanged()
        savedAdapter.notifyDataSetChanged()
        searchPlaceAdapter.notifyDataSetChanged()
        categoryResultAdapter.notifyDataSetChanged()
        customPlaceAdapter.notifyDataSetChanged()
        selectedPlaceAdapter?.notifyDataSetChanged()
        categoryAdapter.notifyDataSetChanged()

        if(selectedPlace.size.plus(customPlace.size) > 0) {
            binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(), R.color.primaryDark)
            binding.addFriendOrNext.text = "Add ".plus(selectedPlace.size.plus(customPlace.size))
                    .plus(" Place")
            binding.addFriendOrNext.onClick({
                meetupViewModel.addPlace(arguments?.getString("meetId"), selectedPlace, customPlace)
            }, 1000)
        } else {
            binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(), R.color.gray1)
            binding.addFriendOrNext.text = "Add "
            binding.addFriendOrNext.setOnClickListener { null }

        }
    }


    private fun setObserver() {

        meetupViewModel.observeAddPlaceMeetUp().observe(viewLifecycleOwner, {
            Log.i(TAG, " came here:: response  ")
            when(it) {
                is ResultHandler.Success -> {
                    setFragmentResult(MeetUpVotingFragment.TAG, Bundle().apply {
                        putBoolean("refresh", true)
                    })
                    activity?.onBackPressed()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })

        placeViewModel.observeCategory().observe(viewLifecycleOwner, {
            categoryList.definition = it.definition
            categoryAdapter.notifyDataSetChanged()
            setScroll()
            /*Log.i(TAG," checkinfcategoryresponse::: ")
            (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,4) as MeetUpDateNTimeBottomFragment?)?.initSelectedPlaceDataSource(selectedPlace,customPlace)
            (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,2) as MeetUpSelectedPlaceBottomFragment?)?.initSelectedPlaceSource(selectedPlace,customPlace)
*/
        })

        placeViewModel.observeSinglePlace().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    Log.i("TAG", " observeSinglePlace::: ")
                    selectedPlace.put(it.value?.slug, it.value?.toMeetUpPlace())
                    refreshedChecked()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong...")
                }
            }
        })
        userViewModel.observeGetAddress().observe(viewLifecycleOwner, { result ->
            when(result) {
                is ResultHandler.Success -> {
                    result.value.first?.let {
                        Log.i("TAG", " checkingAddress::: ")
                        customPlaceAdapter.setListData(it, result.value.second)
                        refreshedChecked()
                    }

                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went Wrong")
                }
            }

        })

        placeViewModel.observerPlaces().observe(viewLifecycleOwner, {
            trandingPlaceList.clear()
            trandingPlaceList.addAll(it)
            trandingAdapter.notifyDataSetChanged()
            if(it.isNotEmpty()) binding.noData.visibility = View.GONE

        })
        placeViewModel.observerSavedPlace().observe(viewLifecycleOwner, {
            savedPlaceList.clear()
            savedPlaceList.addAll(it)
            savedAdapter.notifyDataSetChanged()
            if(it.isNotEmpty()) binding.noData.visibility = View.GONE
        })
        placeViewModel.observePlaceSearch().observe(viewLifecycleOwner, {
            binding.llRecycler.visibility = View.VISIBLE
            searchPlaceList.clear()
            searchPlaceList.addAll(it)
            binding.tvSearchResult.text = "Search Result"
            binding.rvSearch.adapter = searchPlaceAdapter
            searchPlaceAdapter.notifyDataSetChanged()
            if(binding.rvSearch.adapter?.itemCount?.compareTo(0) == 1) binding.noData.visibility = View.GONE
            else binding.noData.visibility = View.VISIBLE
            //binding.rvSearch.adapter =
        })

        placeViewModel.observePlacePagingDaraSource().observe(viewLifecycleOwner, {
            categoryResultAdapter.submitData(lifecycle, it)
            binding.llRecycler.visibility = View.VISIBLE
            binding.rvSearch.adapter = categoryResultAdapter
            Log.i("TAG", " countcount:: ${binding.rvSearch.adapter?.itemCount}")
            if(binding.rvSearch.adapter?.itemCount?.compareTo(1) == -1) binding.noData.visibility = View.GONE
            //Here Hiding and showing NoDATA layout implemented in adapter
        })


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i("TAG", " onRequestPermissionsResult::: $requestCode  ")
        when(requestCode) {
            655 -> {
                Log.i("TAG", " loging::: 1")
                var location = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if(grantResults.isNotEmpty() && location) {
                    toInviteMeetMapFragment()
                } else if(Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.i("TAG", " loging::: 5")
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

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome() {
        //TODO("Not yet implemented")
    }
}