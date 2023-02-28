package com.meetsportal.meets.ui.fragments.restaurantdetailfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.SlidingImage_Adapter
import com.meetsportal.meets.ui.activities.RestaurantDetail
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Navigation

class SlidingImageFragment : BaseFragment() {

    lateinit var pager : ViewPager



    var restauranDetainActivity : RestaurantDetail? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sliding_image,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUP(view)
    }

    private fun setUP(view: View) {
        restauranDetainActivity?.restaurant?.let {
            var type = Navigation.getFragmentData(this,"IMAGE_TYPE")
            if(type.equals("gallery"))
                pager.adapter = SlidingImage_Adapter(requireContext(), ArrayList(it.gallery))
            else
                pager.adapter = SlidingImage_Adapter(requireContext(), ArrayList(it.gallery))
            pager.setCurrentItem(Navigation.getFragmentData(this,"position").toInt(),true)
        }
    }

    private fun initView(view: View) {

        pager = view.findViewById(R.id.pager)
        restauranDetainActivity = (activity as RestaurantDetail)

    }

    override fun onBackPageCome(){

    }

}