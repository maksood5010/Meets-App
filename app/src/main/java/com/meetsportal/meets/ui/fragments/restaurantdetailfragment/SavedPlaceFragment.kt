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
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.PlacesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_places_list.*


@AndroidEntryPoint
class SavedPlaceFragment : BaseFragment() {

    val TAG = SavedPlaceFragment::class.java.simpleName

    lateinit var dp : ImageView
    lateinit var location : TextView
    lateinit var recyclerList : RecyclerView
    lateinit var placeAdapter : AllPlacesAdapter
    lateinit var shimmer : ShimmerFrameLayout
    lateinit var retryCard : ConstraintLayout
    lateinit var retryButton : TextView

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

        dp = view.findViewById(R.id.dp)
        location = view.findViewById(R.id.location)
        recyclerList = view.findViewById(R.id.recycle_place)
        shimmer = view.findViewById(R.id.shimmer)
        retryCard = view.findViewById(R.id.retry)
        retryButton = view.findViewById<TextView>(R.id.bt_retry).apply {
            setOnClickListener {
                retryCard.visibility = View.GONE
                placeAdapter.refresh()
            }
        }
        placeAdapter = AllPlacesAdapter(requireActivity())

    }

    private fun setUP(view: View) {
        view.findViewById<TextView>(R.id.et_search).apply { visibility = View.GONE }
        view.findViewById<ImageView>(R.id.filter).apply { visibility = View.GONE }
        view.findViewById<ImageView>(R.id.back).setOnClickListener { activity?.onBackPressed() }
        PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.profile_image_url .let {
            //Utils.stickImage(requireActivity(),dp,it,null)
            dp.loadImage(requireActivity(),it,R.drawable.ic_default_person)

        }
        location.text = "Saved Places"
        recyclerList.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL,false)
        recyclerList.adapter = placeAdapter.withLoadStateFooter(TimelineFooterStateAdapter{
            placeAdapter.retry()
        })

        placeAdapter.addLoadStateListener {loadState ->
            /*if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && placeAdapter.itemCount < 1) {
                recyclerList?.isVisible = false
                no_data?.isVisible = true
            } else {
                recyclerList?.isVisible = true
                no_data?.isVisible = false
            }*/


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
        placeViewModel.getSavedplacesPagingData()


    }

    private fun setObserver() {
        placeViewModel.observePlacePagingDaraSource().observe(viewLifecycleOwner, Observer {
            Log.i("TAG"," dataSubmitted:: ")
            placeAdapter.submitData(lifecycle,it)
        })
    }

    override fun onStart() {
        super.onStart()
        shimmer.startShimmer()
    }

    override fun onBackPageCome(){

    }
}