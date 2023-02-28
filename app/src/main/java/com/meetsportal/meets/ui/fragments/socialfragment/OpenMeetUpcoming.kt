package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentOpenMeetupUpcomingBinding
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.FilterBottomSheet
import com.meetsportal.meets.utils.Constant.VwOpenMeetList
import com.meetsportal.meets.utils.Utils

class OpenMeetUpcoming : BaseFragment() {

    lateinit var viewPageAdapter: ViewPagerAdapter

    private var _binding: FragmentOpenMeetupUpcomingBinding? = null
    private val binding get() = _binding!!

    companion object {

        val TAG = OpenMeetUpcoming::class.simpleName!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentOpenMeetupUpcomingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setUp()
    }

    private fun initView() {
        MyApplication.putTrackMP(VwOpenMeetList, null)
        val bottomSheet = FilterBottomSheet(requireActivity(), arrayListOf("All", "Closed", "Attended", "Cancelled")) {
            Log.d("TAG", "initView:FilterBottomSheet ::$it ")
            val meetUpListFragment = viewPageAdapter.instantiateItem(binding.pager, 0) as OpenMeetUpListFragment
            meetUpListFragment.filter = it
            meetUpListFragment.getOpenMeet()
            val meetUpListFragment2 = viewPageAdapter.instantiateItem(binding.pager, 1) as OpenMeetUpListFragment
            meetUpListFragment2.filter = it
            meetUpListFragment2.getOpenMeet()
        }
        binding.ivFilter.setOnClickListener {
            bottomSheet.show()
        }

        binding.llTab.background = Utils.getRoundedColorBackground(requireActivity(), R.color.upcomingmeetuptab)
        binding.tvMyMeetUp.background = Utils.getRoundedColorBackground(requireActivity(), R.color.primaryDark)
        viewPageAdapter = ViewPagerAdapter(childFragmentManager)
        binding.pager.adapter = viewPageAdapter
        binding.tvMyMeetUp.setOnClickListener {
            binding.pager.setCurrentItem(0, true)
            binding.tvMyMeetUp.background = Utils.getRoundedColorBackground(requireActivity(), R.color.primaryDark)
            binding.tvInvites.background = null
        }
        binding.tvInvites.setOnClickListener {
            binding.pager.setCurrentItem(1, true)
            binding.tvMyMeetUp.background = null
            binding.tvInvites.background = Utils.getRoundedColorBackground(requireActivity(), R.color.primaryDark)
        }
        binding.pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.i("TAG", " selectedPage:: $position ")
                if(position == 0) {
                    binding.tvMyMeetUp.background = Utils.getRoundedColorBackground(requireActivity(), R.color.primaryDark)
                    binding.tvInvites.background = null

                } else if(position == 1) {
                    binding.tvMyMeetUp.background = null
                    binding.tvInvites.background = Utils.getRoundedColorBackground(requireActivity(), R.color.primaryDark)
                }
            }
        })
        binding.cancel.setOnClickListener {
            activity?.onBackPressed()
        }

    }

    private fun setUp() {


    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount() = 2
        override fun getItem(position: Int): Fragment {
            //return ChatPagerFragment.getInstance(position)
            return when(position) {
                0    -> OpenMeetUpListFragment.getInstance(0)
                1    -> OpenMeetUpListFragment.getInstance(1)
                else -> OpenMeetUpListFragment.getInstance(1)
            }
        }
    }

    override fun onBackPageCome() {
        (viewPageAdapter.instantiateItem(binding.pager, 0) as OpenMeetUpListFragment?)?.refresh()
        (viewPageAdapter.instantiateItem(binding.pager, 1) as OpenMeetUpListFragment?)?.refresh()
    }
}