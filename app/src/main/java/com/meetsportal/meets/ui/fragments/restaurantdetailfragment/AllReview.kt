package com.meetsportal.meets.ui.fragments.restaurantdetailfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.AllReviewAdapter
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.viewmodels.PlacesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllReview : BaseFragment() {

    lateinit var rvReview :RecyclerView
    lateinit var adapter : AllReviewAdapter
    lateinit var count : TextView
    lateinit var heading : TextView
    val  placeViewModel: PlacesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_place_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUP(view)
        setObserver()

    }

    private fun initView(view: View) {
        view.findViewById<ImageView>(R.id.back).setOnClickListener {
            activity?.onBackPressed()
        }
        rvReview = view.findViewById(R.id.rvReview)
        adapter = AllReviewAdapter(requireContext())
        count = view.findViewById(R.id.count)
        heading = view.findViewById(R.id.heading)
    }

    private fun setUP(view: View) {
        rvReview.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        rvReview.adapter = adapter
        placeViewModel.getReviewPagingDataSource(Navigation.getFragmentData(this, "placeId"))
        placeViewModel.getReviewCount(Navigation.getFragmentData(this, "placeId"))
        heading.text = Navigation.getFragmentData(this, "placeName")
    }

    private fun setObserver() {
        placeViewModel.observeReviewData().observe(viewLifecycleOwner, Observer {
            adapter.submitData(lifecycle,it)
        })

        placeViewModel.observeReviewCount().observe(viewLifecycleOwner, Observer {
            count.text = it.toString().plus(" Reviews")
        })

    }

    override fun onBackPageCome(){

    }

}