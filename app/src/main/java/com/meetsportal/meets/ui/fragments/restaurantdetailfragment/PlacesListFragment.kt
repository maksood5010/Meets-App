package com.meetsportal.meets.ui.fragments.restaurantdetailfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.AllPlacesAdapter
import com.meetsportal.meets.adapter.TimelineFooterStateAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.places.CuisineResponse
import com.meetsportal.meets.networking.places.FacilityResponse
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.ProfileFragment
import com.meetsportal.meets.ui.fragments.socialfragment.SearchFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.PREFRENCE_CATEGORY
import com.meetsportal.meets.utils.Constant.PREFRENCE_CUISINE
import com.meetsportal.meets.utils.Constant.PREFRENCE_FACILITY
import com.meetsportal.meets.utils.Constant.VwBestPlaceList
import com.meetsportal.meets.utils.Constant.VwCategoryPlaceList
import com.meetsportal.meets.utils.Constant.VwSeeAllPlaceList
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.PlacesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_places_list.*
import kotlinx.android.synthetic.main.generic_retry_card.*

@AndroidEntryPoint
class PlacesListFragment :BaseFragment(){

    val TAG = PlacesListFragment::class.java.simpleName

    lateinit var search :TextView
    lateinit var filter:ImageView
    lateinit var dp : ImageView
    lateinit var shimmer : ShimmerFrameLayout
    lateinit var location : TextView
    lateinit var recyclerList : RecyclerView
    lateinit var retryCard : ConstraintLayout
    lateinit var retryButton : TextView
    lateinit var placeAdapter : AllPlacesAdapter

    var facilities = StringBuilder()
    var cuisines = StringBuilder()
    var categories = StringBuilder()

    val  placeViewModel: PlacesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_places_list,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUP(view)
        setObserver()
    }

    private fun initView(view: View) {

        when(arguments?.getBoolean("isBestPlaces")){
            true -> MyApplication.putTrackMP(VwBestPlaceList,null)
            false -> MyApplication.putTrackMP(VwSeeAllPlaceList,null)
            null -> MyApplication.putTrackMP(VwCategoryPlaceList,null)
        }

        search = view.findViewById<TextView>(R.id.et_search).apply {
            setOnClickListener {
                MyApplication.smallVibrate()
                Navigation.addFragment(
                    requireActivity(), SearchFragment.getInstance(2), Constant.TAG_SEARCH_FRAGMENT, R.id.homeFram,
                    stack = true,
                    needAnimation = false
                )
            }

        }
        shimmer = view.findViewById(R.id.shimmer)
        retryCard = view.findViewById(R.id.retry)
        retryButton = view.findViewById<TextView>(R.id.bt_retry).apply {
            setOnClickListener {
                retryCard.visibility = View.GONE
                placeAdapter.refresh()
            }
        }
        filter = view.findViewById<ImageView>(R.id.filter).apply {
            setOnClickListener {
                var baseFragment : BaseFragment = FilterFragment()
                Navigation.addFragment(requireActivity(),baseFragment,Constant.TAG_PLACE_FILTER_FRAGMENT,R.id.homeFram,true,true)
            }
        }
        dp = view.findViewById(R.id.dp)
        location = view.findViewById(R.id.location)
        recyclerList = view.findViewById(R.id.recycle_place)
        placeAdapter = AllPlacesAdapter(requireActivity())
    }

    private fun setUP(view: View) {
        view.findViewById<ImageView>(R.id.back).setOnClickListener { activity?.onBackPressed() }
        PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.profile_image_url .let {
            //Utils.stickImage(requireActivity(),dp,it,null)
            dp.loadImage(requireActivity(),it,R.drawable.ic_default_person)
        }
        dp.setOnClickListener {
            Navigation.addFragment(
                requireActivity(),
                ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        }
        location.text = getMain()?.locationText
        recyclerList.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        recyclerList.adapter = placeAdapter.withLoadStateFooter(TimelineFooterStateAdapter{
            placeAdapter.retry()
        })
        no_data?.isVisible = false
        placeAdapter.addLoadStateListener {loadState ->
            if(loadState.refresh is LoadState.Error){
                retryCard.visibility = View.VISIBLE
            }
            Log.i(TAG," loadStateloadState:::: $loadState")
            if(loadState.source.refresh is LoadState.Loading){
                Log.i(TAG," shimmer::: 1")
                shimmer.visibility = View.VISIBLE
                shimmer.startShimmer()
                no_data?.isVisible = false
                recyclerList?.isVisible = false

            }
            else if (loadState.source.refresh is LoadState.NotLoading &&
                loadState.append is LoadState.NotLoading &&
                loadState.prepend is LoadState.NotLoading ) {

                Log.i(TAG," shimmer::: 2 ${placeAdapter.itemCount}")
                shimmer.stopShimmer()
                shimmer.visibility = View.GONE
                if(placeAdapter.itemCount < 1 ){
                    Log.i(TAG," shimmer::: 3")
                    recyclerList?.isVisible = false
                    no_data?.isVisible = true
                }else{
                    Log.i(TAG," shimmer::: 4")
                    recyclerList?.isVisible = true
                    no_data?.isVisible = false
                }
            }
        }

        var categoryResponse = PreferencesManager.get<CategoryResponse>(PREFRENCE_CATEGORY)
        categoryResponse?.definition?.map {
            if(it.isSelected){
                //categories = categories.plus(it.key).plus(",")
                categories = categories.append(it.key)
                Log.i("TAG"," categories::: $categories")
                return@map
            }
        }


        placeViewModel.getBestMeetPagingData(facilities,cuisines,categories,this.arguments?.getBoolean("isBestPlaces"),Utils.getCountryCode(requireContext()))
        //categories.clear()
    }

    private fun setObserver() {
        placeViewModel.observePlacePagingDaraSource().observe(viewLifecycleOwner, Observer {
            placeAdapter.submitData(lifecycle,it)
        })

        placeViewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity,it)
        })
    }

    fun applyFilter(facilities: String,cuisines:String,categories:String){
        this.facilities.clear()
        this.facilities.append(facilities)
        this.cuisines.clear()
        this.cuisines.append(cuisines)
        this.categories.clear()
        this.categories.append(categories)
        placeAdapter.refresh()

    }

    override fun onDestroyView() {
        var facility = PreferencesManager.get<FacilityResponse>(PREFRENCE_FACILITY)
        facility?.definition = facility?.definition?.map { it.isSelected = false ; it }

        var cuisine = PreferencesManager.get<CuisineResponse>(PREFRENCE_CUISINE)
        cuisine?.definition = cuisine?.definition?.map { it.isSelected = false ; it }

        var category = PreferencesManager.get<CategoryResponse>(PREFRENCE_CATEGORY)
        category?.definition = category?.definition?.map { it.isSelected = false ; it }


        PreferencesManager.put(facility, PREFRENCE_FACILITY)
        PreferencesManager.put(cuisine, PREFRENCE_CUISINE)
        PreferencesManager.put(category, PREFRENCE_CATEGORY)

        super.onDestroyView()

    }

    override fun onStart() {
        super.onStart()
        shimmer.startShimmer()
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

}