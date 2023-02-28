package com.meetsportal.meets.adapter

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.meetsportal.meets.ui.fragments.socialonboarding.SocialOnBoarding1Fragment

class OnBoardingAdapter (activity: AppCompatActivity, private val itemsCount: Int) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return itemsCount
    }

    override fun createFragment(position: Int): Fragment {

        Log.i("TAG"," fragnumber:: $position")

        return SocialOnBoarding1Fragment.getInstance(position)

    }

}