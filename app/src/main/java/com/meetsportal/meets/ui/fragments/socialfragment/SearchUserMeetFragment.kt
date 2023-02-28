package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.adapter.MeetFollowingPagerAdapter
import com.meetsportal.meets.adapter.MeetSearchPeopleAdapter
import com.meetsportal.meets.adapter.TimelineFooterStateAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentSearchUserMeetBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.profile.SearchPeopleResponse
import com.meetsportal.meets.networking.profile.SearchPeopleResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchUserMeetFragment:BaseFragment() {

    val TAG = SearchUserMeetFragment::class.simpleName

    var peopleList : SearchPeopleResponse = SearchPeopleResponse()
    lateinit var searchPeopleAdapter: MeetSearchPeopleAdapter
    //lateinit var noData : ConstraintLayout
    val userviewModel: UserAccountViewModel by viewModels()

    var createMeetPager:MeetUpViewPageFragment? = null
    var addFriendMeetpager :AddFriendToMeetUp? = null

    var selectedPeople = ArrayList<SearchPeopleResponseItem>()
    lateinit var adapter : MeetFollowingPagerAdapter
    private var _binding: FragmentSearchUserMeetBinding? = null
    private val binding get() = _binding!!

    companion object{

        val MEET_DATA = "meetData"

        fun getInstance(sid:String?, meetUpdata: GetMeetUpResponseItem? = null ):SearchUserMeetFragment{
            return SearchUserMeetFragment().apply {
                arguments = Bundle().apply {
                    putString("sid",sid)
                    meetUpdata?.let {
                        putParcelable(MEET_DATA,meetUpdata)
                    }
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_friend_bottom, container, false)
        _binding = FragmentSearchUserMeetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setupUI(binding.root)>
        initView(view)
        setUp()
        addObserver()
    }

    private fun initView(view: View) {
        arguments?.getString("sid")?.let {
            userviewModel.getOtherProfile(it)
        }
      // noData = view.findViewById(R.id.no_data_root)
        binding.noDataInclude.noDataRoot.visibility = View.GONE
        createMeetPager = activity?.supportFragmentManager?.findFragmentByTag(MeetUpViewPageFragment.TAG) as MeetUpViewPageFragment?
        addFriendMeetpager = activity?.supportFragmentManager?.findFragmentByTag(AddFriendToMeetUp::class.simpleName) as AddFriendToMeetUp?

        adapter = MeetFollowingPagerAdapter(requireActivity(),this,selectedPeople, arguments){
            (createMeetPager?.pagerAdapter?.instantiateItem(createMeetPager?.binding?.meetUpViewpager!!,0) as MeetUpFriendtBottomFragment?)?.populateSelectedPeopleList(selectedPeople)
            addFriendMeetpager?.populateSelectedPeopleList(selectedPeople)
        }
        searchPeopleAdapter = MeetSearchPeopleAdapter(requireActivity(),selectedPeople,peopleList,arguments) {
            (createMeetPager?.pagerAdapter?.instantiateItem(createMeetPager?.binding?.meetUpViewpager!!,0) as MeetUpFriendtBottomFragment?)?.populateSelectedPeopleList(selectedPeople)
            addFriendMeetpager?.populateSelectedPeopleList(selectedPeople)
        }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.recycler.adapter =searchPeopleAdapter

    }

    private fun setUp() {
        userviewModel.getFollowFollowingDataSource(MyApplication.SID,"followings",null)
        binding.recycler.adapter = adapter.withLoadStateFooter(TimelineFooterStateAdapter{
            adapter.retry()
        })

    }

    fun populateSelectedList(selectedPeopleList: ArrayList<SearchPeopleResponseItem>) {


        //selectedPeople.clear()
        //selectedPeople.addAll(selectedPeopleList)
        searchPeopleAdapter.notifyDataSetChanged()
        adapter.notifyDataSetChanged()

    }

    fun setSearchPeopleAdapter(list: SearchPeopleResponse,isFromSearch : Boolean = true){
        peopleList.clear()
        peopleList.addAll(list)
        if(peopleList.size <= 0){
            if(isFromSearch){
                binding.recycler.adapter =  searchPeopleAdapter
                binding.noDataInclude.noDataRoot.visibility = View.VISIBLE
            }else{
                binding.recycler.adapter = adapter
                binding.noDataInclude.noDataRoot.visibility = View.GONE
            }
        }else{
            binding.recycler.adapter =  searchPeopleAdapter
            binding.noDataInclude.noDataRoot.visibility = View.GONE
        }
        searchPeopleAdapter.notifyDataSetChanged()
    }



    private fun addObserver() {
        userviewModel.observeFollowFollowingDataSource().observe(viewLifecycleOwner, {
            adapter.submitData(lifecycle,it)

        })

        userviewModel.observerFullOtherProfileChange().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success -> {
                    var selectedpersone = it.value?.apply {
                        selected = true
                        selectedTimeStamp = System.currentTimeMillis()
                    }?.toSearchPeopleresponseItem()
                    selectedpersone?.let {
                        selectedPeople.add(selectedpersone)
                        (createMeetPager?.pagerAdapter?.instantiateItem(createMeetPager?.binding?.meetUpViewpager!!,0) as MeetUpFriendtBottomFragment?)?.populateSelectedPeopleList(selectedPeople)
                        addFriendMeetpager?.populateSelectedPeopleList(selectedPeople)
                    }
                }
                is ResultHandler.Failure ->{
                    MyApplication.showToast(requireActivity(),"Something went wrong!!!")
                }
            }

        })

    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }
}