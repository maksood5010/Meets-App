package com.meetsportal.meets.ui.fragments.restaurantdetailfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.MyTableViewPagerAdapter
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Navigation

class MyTableBookingFragment :BaseFragment() {


    lateinit var pager : ViewPager2
    lateinit var tablayout : TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_table_booking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUP(view)
    }

    private fun setUP(view: View) {
        Navigation.removeFragment(requireContext(), "verifingCode")
        pager.adapter = createCardAdapter()
        TabLayoutMediator(tablayout,pager){ tab, position ->
            tab.text = when(position){
                0 -> "Confirmed"
                1 -> "Pending"
                2 -> "Cancelled"
                else -> "Confirmed"
            }
        }.attach()
    }

    private fun initView(view: View) {
        pager = view.findViewById(
            R.id.pager
        )

        tablayout = view.findViewById(R.id.tab_layout)
    }

    private fun createCardAdapter(): MyTableViewPagerAdapter? {
        return MyTableViewPagerAdapter(requireActivity())
    }


    override fun onBackPageCome(){

    }
}