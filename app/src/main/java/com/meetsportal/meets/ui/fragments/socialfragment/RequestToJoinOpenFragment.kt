package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.FragmentRequestToJoinOpenBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.ui.dialog.ApproveMeetRqbyBadgeAlert
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.FilterBottomSheet
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.badgeList
import com.meetsportal.meets.utils.count
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestToJoinOpenFragment: BaseFragment() {

    private var _binding: FragmentRequestToJoinOpenBinding? = null
    val binding get() = _binding!!

    val meetUpViewModel: MeetUpViewModel by viewModels()

    lateinit var viewPagerAdapter : ViewPagerAdapter

   var option : FilterBottomSheet? = null
    val MeetId : String? by lazy { arguments?.getString("meetId") }

    companion object{
        val TAG = RequestToJoinOpenFragment::class.simpleName!!

        fun getInstance(meetId : String?):RequestToJoinOpenFragment{
            return RequestToJoinOpenFragment().apply {
                arguments = Bundle().apply {
                    putString("meetId",meetId)
                }
            }
        }
    }

    val approveByBadgeAlert  : ApproveMeetRqbyBadgeAlert by lazy { ApproveMeetRqbyBadgeAlert{
        meetUpViewModel.opMeetRqApprove(MeetId,null,it)
    }}

    val page1 : FragmentOpenMeetRequestList? by lazy { (viewPagerAdapter.instantiateItem(
        binding.pager,
        0
    ) as FragmentOpenMeetRequestList?) }

    val page2 : FragmentOpenMeetRequestList? by lazy { (viewPagerAdapter.instantiateItem(
        binding.pager,
        1
    ) as FragmentOpenMeetRequestList?) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRequestToJoinOpenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()
        setObserver()
    }

    private fun initView(view: View) {
        option = FilterBottomSheet(requireActivity(), arrayListOf("Allow All","Allow by status")){
            Log.i(TAG," position:: ${it}")
            when(it){
                0 -> {
                    meetUpViewModel.opMeetRqApprove(MeetId,null,badgeList.map { it.key })
                }
                1->{
                    approveByBadgeAlert.showDialog(requireActivity())
                }
            }
        }
        binding.back.onClick({
            activity?.onBackPressed()
        })

        binding.pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                when(position){
                    0 -> binding.option.visibility = View.VISIBLE
                    1 -> binding.option.visibility = View.GONE
                }
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })
        binding.search.setRoundedColorBackground(requireActivity(), R.color.gray1)
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager,arguments?.getString("meetId"))
        binding.option.onClick({option?.show()},500)
        binding.pager.adapter = viewPagerAdapter
        binding.pager.offscreenPageLimit = 2
        binding.tlTabs.setupWithViewPager(binding.pager)
        binding.search.count{
            if ((it.toInt()) > 0) {
                page1?.searchRequest(binding.search.text.toString().trim())
                page2?.searchRequest(binding.search.text.toString().trim())
            } else {
                page1?.searchRequest(null)
                page2?.searchRequest(null)
            }
        }

    }
     fun setUp() {
        meetUpViewModel.countRequestbyBadge(MeetId)
    }

    private fun setObserver() {
        meetUpViewModel.obsrveMeetRqCountByBadge().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                    approveByBadgeAlert.setCount(it.value)
                }
                is ResultHandler.Failure -> {

                }
            }
        })
        meetUpViewModel.observerSingleMeetApproval().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                    setUp()
                    (viewPagerAdapter.instantiateItem(
                        binding.pager,
                        0
                    ) as FragmentOpenMeetRequestList?)?.setUp()
                    (viewPagerAdapter.instantiateItem(
                        binding.pager,
                        1
                    ) as FragmentOpenMeetRequestList?)?.setUp()
                }
                is ResultHandler.Failure -> {

                }
            }
        })
    }

    class ViewPagerAdapter(fm: FragmentManager,val meetId: String?) :
        FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount() = 2
        override fun getItem(position: Int): Fragment {
            return when(position){
                0 ->  FragmentOpenMeetRequestList.getInstance(meetId,Constant.RequestType.PENDING)
                1 ->  FragmentOpenMeetRequestList.getInstance(meetId,Constant.RequestType.ACCEPTED)
                else -> FragmentOpenMeetRequestList.getInstance(meetId,Constant.RequestType.ACCEPTED)
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position) {
                0    -> "   Requested  "
                1    -> "   Accepted   "
                else -> ""
            }
        }
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

    override fun hideNavBar(): Boolean {
        return true
    }

}