package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.paging.LoadState
import com.meetsportal.meets.adapter.AllPlacesAdapter
import com.meetsportal.meets.adapter.TimelineFooterStateAdapter
import com.meetsportal.meets.databinding.FragmentAllSavedPlacesBinding
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.viewmodels.PlacesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SavedPlacesAllFragment : BaseFragment(){
    val TAG= javaClass.simpleName
    private var _binding: FragmentAllSavedPlacesBinding? = null
    private val binding get() = _binding!!
    val  placeViewModel: PlacesViewModel by viewModels()
    lateinit var placeAdapter : AllPlacesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
                             ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAllSavedPlacesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        placeAdapter = AllPlacesAdapter(requireActivity())
        setUp()
        placeViewModel.getSavedplacesPagingData()
    }

    private fun setUp() {
        binding.recyclePlace.adapter = placeAdapter.withLoadStateFooter(TimelineFooterStateAdapter {
            placeAdapter.retry()
        })
        placeAdapter.addLoadStateListener {loadState ->

            if(loadState.refresh is LoadState.Error){
                binding.retryCard.retry.visibility = View.VISIBLE
            }

            Log.i(TAG," loadStateloadState:::: $loadState")
            if(loadState.source.refresh is LoadState.Loading){
                Log.i(TAG," shimmer::: 1")
                binding.shimmer.visibility = View.VISIBLE
                binding.noData.noDataRoot.isVisible = false
                binding.recyclePlace.isVisible = false

            }
            else if (loadState.source.refresh is LoadState.NotLoading &&
                loadState.append is LoadState.NotLoading &&
                loadState.prepend is LoadState.NotLoading ) {
                binding.shimmer.visibility = View.GONE
                if(placeAdapter.itemCount < 1 ){
                    Log.i(TAG," shimmer::: 3")
                    binding.recyclePlace.isVisible = false
                    binding.noData.noDataRoot.isVisible = true
                }else{
                    Log.i(TAG," shimmer::: 4")
                    binding.recyclePlace.isVisible = true
                    binding.noData.noDataRoot?.isVisible = false
                }
            }
        }
    }

    private fun setObserver() {
        placeViewModel.observePlacePagingDaraSource().observe(viewLifecycleOwner, Observer {
            Log.i("TAG"," dataSubmitted:: ")
            placeAdapter.submitData(lifecycle,it)
        })
    }
    override fun onBackPageCome() {

    }
}