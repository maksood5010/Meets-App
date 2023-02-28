package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.ImageSamplingViewPagerAdapter
import com.meetsportal.meets.overridelayout.CustomViewPager
import com.meetsportal.meets.ui.fragments.BaseFragment

class ImageSamplingFragment(var backPressed:(Boolean)->Unit): BaseFragment() {

    val TAG = ImageSamplingFragment::class.simpleName

    lateinit var pager : CustomViewPager
    lateinit var imageNumber : TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_sampling, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()


    }

    private fun initView(view: View) {
        pager = view.findViewById(R.id.pager)
        imageNumber = view.findViewById(R.id.image_number)
        var imageList = arguments?.getSerializable("media") as ArrayList<String>
        var position = arguments?.getInt("position")
        Log.i("TAG"," listlist:: $imageList")
        pager.adapter = ImageSamplingViewPagerAdapter(requireContext(),imageList){
            backPressed(true)
        }
        position?.let {  pager.currentItem = it }
    }

    private fun setUp() {
        pager.addOnPageChangeListener(object:ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                var imageList = arguments?.getSerializable("media") as ArrayList<String>
                imageNumber.text = position.plus(1).toString().plus("/").plus(imageList.size)
            }
            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

}