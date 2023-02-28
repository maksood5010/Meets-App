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
import com.meetsportal.meets.databinding.FragmentOpenmeetIntrestListBinding
import com.meetsportal.meets.networking.meetup.MeetPerson
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OpenMeetIntrestList : BaseFragment() {

    val meetUpViewModel: MeetUpViewModel by viewModels()
    val profileViewmodel: UserAccountViewModel by viewModels()

    var participants = ArrayList<MeetPerson>()
    var attendees = ArrayList<MeetPerson>()

    private var _binding: FragmentOpenmeetIntrestListBinding? = null
    private val binding get() = _binding!!

    lateinit var viewPagerAdapter: ViewPagerAdapter


    companion object {

        var MEETID: String? = null
        fun getInstance(meetId: String?): OpenMeetIntrestList {
            Log.d("OpenMeetIntrestList", "getInstance: meetId $meetId")
            return OpenMeetIntrestList().apply {
                if(meetId != null) {
                    MEETID = meetId
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentOpenmeetIntrestListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setUp()
        addObserver()

    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount() = 2
        override fun getItem(position: Int): Fragment {
            return when(position) {
                0    -> FragmentOMInterestList.getInstance(MEETID, Constant.RequestType.PENDING)
                1    -> FragmentOMInterestList.getInstance(MEETID, Constant.RequestType.ACCEPTED)
                else -> FragmentOMInterestList.getInstance(MEETID, Constant.RequestType.PENDING)
            }
        }

        private val tabTitles = arrayOf("   Requested  ", "    Accepted    ")
        override fun getPageTitle(position: Int): CharSequence? {
            return tabTitles[position]
        }
    }

    private fun initView() {

        binding.name.text = "Participants"

        viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        binding.pager.adapter = viewPagerAdapter
        binding.tlTabs.setupWithViewPager(binding.pager)

        binding.shimmer.stopShimmer()
        binding.shimmer.visibility = View.GONE
//        binding.rvSuggestion.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
//        adapter = OpenMeetInterestAdapter(requireActivity()){
//            if(!it){
//                binding.shimmer.stopShimmer()
//                binding.shimmer.visibility = View.GONE
//            }else{
//                if (Utils.checkPermission(
//                            requireActivity(),
//                            Manifest.permission.ACCESS_FINE_LOCATION
//                                         )
//                ) {
//                    (activity as MainActivity?)?.enableLocationStuff(654,655){
//                        iamHere()
//                    }
//                } else {
//                    requestPermissions(
//                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                            655
//                                      )
//                }
//            }
//        }
//        binding.rvSuggestion.adapter = adapter
//        binding.search.setRoundedColorBackground(requireActivity(), R.color.gray1)
        binding.back.onClick({ activity?.onBackPressed() })
//        binding.search.count{
//            if ((it.toInt()) > 0) {
//                searchRequest(binding.search.text.toString().trim())
//            } else {
//                searchRequest(null)
//            }
//        }

    }


    private fun setUp() {
//        adapter?.setRequestType(Constant.RequestType.ALL)

    }
//
//    fun searchRequest(search : String?){
//        if(!search.isNullOrEmpty()){
//            var searchList = joinRequestList.filter { it.user_meta.username?.contains(search) == true }
//            adapter?.submitList(searchList)
//        }else{
//            adapter?.submitList(joinRequestList)
//        }
//    }

//    private fun followuser(id: String?, forfollow: Boolean?) {
//        if(forfollow == true){
//            profileViewmodel.followUser(id)
//        }else{
//            profileViewmodel.unfollowUser(id)
//        }
//    }

    private fun addObserver() {
//        meetUpViewModel.observeMeetUpDetail().observe(viewLifecycleOwner,{
//            when(it){
//                is ResultHandler.Success -> {
//                    // adapter?.setData(it.value?: GetJoinRequestModel())
//                    Log.d("TAG", "addObserver: ")
//                    it.value?.let { it1 ->
//                        participants.clear()
//                        participants.addAll(it.value?.participants ?: ArrayList())
//                        attendees.clear()
//                        attendees.addAll(it.value?.attendees ?: ArrayList())
//                        binding.name.text = "${participants.size} Participants"
//                        binding.shimmer.stopShimmer()
//                        binding.shimmer.visibility = View.GONE
//                    }?:run{
//                        binding.shimmer.stopShimmer()
//                        binding.shimmer.visibility = View.GONE
//                        showToast("Something went wrong!!!")
//                    }
//                }
//                is ResultHandler.Failure ->{
//                    binding.shimmer.stopShimmer()
//                    binding.shimmer.visibility = View.GONE
//                    showToast("Something went wrong!!!")
//                    Log.e("TAG","  OpenMeetJoinRequest Failed :: ")
//                }
//            }
//        })
    }

    override fun onStart() {
        super.onStart()
        binding.shimmer.startShimmer()
    }

    override fun onBackPageCome() {

    }
}