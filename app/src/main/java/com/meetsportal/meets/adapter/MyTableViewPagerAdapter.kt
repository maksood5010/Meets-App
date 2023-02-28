package com.meetsportal.meets.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.TableBookCardFragment

class MyTableViewPagerAdapter(fragmentActivity:FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
       return TableBookCardFragment.newInstance(position)!!
    }
}