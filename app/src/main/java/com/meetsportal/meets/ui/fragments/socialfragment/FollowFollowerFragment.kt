package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.viewmodels.UserAccountViewModel

class FollowFollowerFragment :BaseFragment(){

    private val TAG = FollowFollowerFragment::class.java.simpleName

    val  profileViewModel: UserAccountViewModel by viewModels()

    lateinit var viewPager: ViewPager
    lateinit var viewPageAdapter: ViewPagerAdapter
    lateinit var tablayout: TabLayout
    lateinit var name :TextView

    var sid : String? = null
    var username : String? = null
    var followerCount : Int? = null
    var followingCount : Int? = null
    var tabPosition = 0

    companion object{
        val SID = "sid"
        val USERNAME = "username"
        val FOLLOWER_COUNT = "followerCount"
        val FOLLOWING_COUNT = "followingCount"
        val TAB = "tabPosition"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_follower_following, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()
        setObserver()

    }

    private fun initView(view: View) {
        name = view.findViewById(R.id.name)
        view.findViewById<ImageView>(R.id.back).setOnClickListener { activity?.onBackPressed() }

        viewPager = view.findViewById(R.id.viewpager)

        tablayout = view.findViewById(R.id.tablayout)


        sid = Navigation.getFragmentData(this, SID)
        username = Navigation.getFragmentData(this, USERNAME)
        followerCount = Navigation.getFragmentData(this, FOLLOWER_COUNT).toInt()
        followingCount = Navigation.getFragmentData(this, FOLLOWING_COUNT).toInt()
        tabPosition = Navigation.getFragmentData(this, TAB).toInt()


    }

    private fun setUp() {
        Log.i(TAG," username:::: $username ")
        username?.let { name.text = it }?:run { name.text = "Hello User" }

        viewPageAdapter = ViewPagerAdapter(childFragmentManager,sid)
        viewPager.adapter = viewPageAdapter
        tablayout.setupWithViewPager(viewPager)
        tablayout.getTabAt(0)?.text = followerCount.toString().plus(" Followers")
        tablayout.getTabAt(1)?.text = followingCount.toString().plus(" Followings")
        tablayout.selectTab(tablayout.getTabAt(tabPosition))


    }

    private fun setObserver() {
    }

    class ViewPagerAdapter(fm: FragmentManager,var sid:String?) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val tabTitles = arrayOf("0 Followers", "0 Following")

        override fun getCount() = 2

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> FollowerFragment(sid)
                1 -> FollowingFragment(sid)
                else -> FollowingFragment(sid)
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return tabTitles.get(position)
        }
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }
}