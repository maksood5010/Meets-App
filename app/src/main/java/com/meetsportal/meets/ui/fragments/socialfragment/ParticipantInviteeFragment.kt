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
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.FragmentRequestToJoinOpenBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.meetup.MeetPerson
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.count
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ParticipantInviteeFragment: BaseFragment() {

    private val participants: ArrayList<MeetPerson> = ArrayList<MeetPerson>()
    private val invitees: ArrayList<MeetPerson> = ArrayList<MeetPerson>()
    private var _binding: FragmentRequestToJoinOpenBinding? = null
    val binding get() = _binding!!

    val meetUpViewModel: MeetUpViewModel by viewModels()

    lateinit var viewPagerAdapter : ViewPagerAdapter

    val MeetId : String? by lazy { arguments?.getString("meetId") }

    val page1: MeetParticipantList? by lazy {
        (viewPagerAdapter.instantiateItem(binding.pager, 0) as MeetParticipantList?)
    }

    val page2: MeetParticipantList? by lazy { (viewPagerAdapter.instantiateItem(binding.pager, 1) as MeetParticipantList?) }


    companion object{
        val TAG = ParticipantInviteeFragment::class.simpleName!!

        fun getInstance(meetId : String?):ParticipantInviteeFragment{
            return ParticipantInviteeFragment().apply {
                arguments = Bundle().apply {
                    putString("meetId",meetId)
                }
            }
        }
    }

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
        binding.option.visibility=View.GONE
        binding.name.text="Who is coming"

        binding.back.onClick({
            activity?.onBackPressed()
        })
//
//        binding.pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
//            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
//            override fun onPageSelected(position: Int) {
//                when(position){
//                    0 -> binding.option.visibility = View.GONE
//                    1 -> binding.option.visibility = View.VISIBLE
//                }
//            }
//            override fun onPageScrollStateChanged(state: Int) {}
//        })
        binding.search.setRoundedColorBackground(requireActivity(), R.color.gray1)
//        viewPagerAdapter = ViewPagerAdapter(childFragmentManager,arguments?.getString("meetId"))

//        binding.pager.adapter = viewPagerAdapter
//        binding.pager.offscreenPageLimit = 2
//        binding.tlTabs.setupWithViewPager(binding.pager)
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
         val meetId = arguments?.getString("meetId")
         meetUpViewModel.getMeetUpDetail(meetId)
    }

    private fun setObserver() {
        meetUpViewModel.observeMeetUpDetail().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                    // adapter?.setData(it.value?: GetJoinRequestModel())
                    Log.d("TAG", "addObserver: ")
                    it.value?.let { it1 ->

                        Log.e(TAG, "setUp: meetID::: ${it.value._id} --:: ${it.value.name}")
                        participants.clear()
                        participants.addAll(it.value?.participants ?: ArrayList())
                        invitees.clear()
                        invitees.addAll(it.value?.invitees ?: ArrayList())
                        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, participants,invitees,it.value._id)
                        binding.pager.adapter = viewPagerAdapter
                        binding.pager.offscreenPageLimit = 2
                        binding.tlTabs.setupWithViewPager(binding.pager)
                    }?:run{
                        showToast("Something went wrong!!!")
                    }
                }
                is ResultHandler.Failure ->{
                    showToast("Something went wrong!!!")
                }
            }
        })
    }

    class ViewPagerAdapter(fm: FragmentManager, val participant: ArrayList<MeetPerson>?, val invitee: ArrayList<MeetPerson>?,val _id: String) :
        FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount() = 2
        override fun getItem(position: Int): Fragment {
            return when(position){
                0 ->  MeetParticipantList.getInstance(invitee, _id)
                1 ->  MeetParticipantList.getInstance(participant, _id)
                else -> MeetParticipantList.getInstance(participant, _id)
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position) {
                0    -> "   Invitees  "
                1    -> "   Participants   "
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