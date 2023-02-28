package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetUpUpcomingBinding
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.FilterBottomSheet
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.VwPreviousMeetList
import com.meetsportal.meets.utils.Constant.VwUpComingMeetList
import com.meetsportal.meets.utils.Utils

class MeetUpUpComing : BaseFragment() {

    private var type: Int = 0
    val TAG = MeetUpUpComing::class.simpleName

    lateinit var viewPageAdapter: ViewPagerAdapter

    private var _binding: FragmentMeetUpUpcomingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_upcoming, container, false)

        _binding = FragmentMeetUpUpcomingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            type = it.getInt(MeetUpListFragment.TYPE)
        }

        initView()
        setUp()
    }

    private fun initView() {
        if(type == 1) {
            binding.tvUpcoming.text = "Previous Meetup"
            MyApplication.putTrackMP(VwUpComingMeetList, null)
        } else {
            binding.tvUpcoming.text = "Up Coming Meetup"
            MyApplication.putTrackMP(VwPreviousMeetList, null)
        }

        val bottomSheet = FilterBottomSheet(requireActivity()) {
            Log.d("TAG", "initView:FilterBottomSheet ::$it ")
            val meetUpListFragment = viewPageAdapter.instantiateItem(binding.pager, 0) as MeetUpListFragment
            meetUpListFragment.filter = it
            meetUpListFragment.getMeetUpData()
            val meetUpListFragment2 = viewPageAdapter.instantiateItem(binding.pager, 1) as MeetUpListFragment
            meetUpListFragment2.filter = it
            meetUpListFragment2.getMeetUpData()
        }
        binding.ivFilter.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcMeetupListFilter, null)
            bottomSheet.show()
        }
        binding.llTab.background = Utils.getRoundedColorBackground(requireActivity(), R.color.upcomingmeetuptab)
        binding.tvMyMeetUp.background = Utils.getRoundedColorBackground(requireActivity(), R.color.primaryDark)
        viewPageAdapter = ViewPagerAdapter(childFragmentManager, type)
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

    }

    private fun setUp() {
        binding.cancel.setOnClickListener {
            activity?.onBackPressed()
        }

    }


    class ViewPagerAdapter(fm: FragmentManager, val type: Int) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount() = 2
        override fun getItem(position: Int): Fragment {
            //return ChatPagerFragment.getInstance(position)
            return when(position) {
                0    -> MeetUpListFragment().apply {
                    arguments = bundleOf(MeetUpListFragment.PAGE to 0, MeetUpListFragment.TYPE to type)
                }
                1    -> MeetUpListFragment().apply { arguments = bundleOf(MeetUpListFragment.PAGE to 1, MeetUpListFragment.TYPE to type) }
                else -> MeetUpListFragment().apply { arguments = bundleOf(MeetUpListFragment.PAGE to 1, MeetUpListFragment.TYPE to type) }
            }
        }
    }

    override fun onBackPageCome() {
        Log.i(TAG, " classCameOnTop:: ${this::class.simpleName}")
        (viewPageAdapter.instantiateItem(binding.pager, 0) as MeetUpListFragment).adapter.refresh()
        (viewPageAdapter.instantiateItem(binding.pager, 1) as MeetUpListFragment).adapter.refresh()

    }

}