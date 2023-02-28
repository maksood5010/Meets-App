package com.meetsportal.meets.ui.bottomsheet

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpFriendtBottomFragment

class CreateMeetUpBottomSheeet(var ctx: Activity) : BottomSheetDialogFragment(){

    lateinit var viewpager : ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_create_meetup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()

    }


    override fun onResume() {
        super.onResume()
    }

    private fun setUp() {
        //viewpager.adapter = MeetUpPAgerAdapter(activity?.supportFragmentManager!!)
        viewpager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.i("TAG", " selectedPage:: $position ")
                // if (position == 0) {
//                    var a = pager.layoutParams
//                    a.height = b.get(position)!!
//                    pager.layoutParams = a
                //changePagerHight(b.get(position)!!)

                // }
            }

        })

    }

    private fun initView(view: View) {
        viewpager = view.findViewById(R.id.meet_up_viewpager)
    }

    class MeetUpPAgerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount()=3

        override fun getItem(position: Int): Fragment {
            return when(position){
                0-> MeetUpFriendtBottomFragment()
                1-> MeetUpFriendtBottomFragment()
                2-> MeetUpFriendtBottomFragment()
                else-> MeetUpFriendtBottomFragment()
            }
        }
    }

}