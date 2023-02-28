package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.MeetUpSelctedPlaceAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetupSelectedPlaceBottomBinding
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.models.MeetsPlace
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.setRoundedColorBackground

class MeetUpSelectedPlaceBottomFragment :BaseFragment(){

    val TAG = MeetUpSelectedPlaceBottomFragment::class.simpleName

    lateinit var customPlace : ArrayList<AddressModel>
    lateinit var selectedPlaceList : MutableMap<String?, MeetsPlace?>
    lateinit var selectedPlaceAdapted : MeetUpSelctedPlaceAdapter
    var meetPager:MeetUpViewPageFragment? = null

    private var _binding: FragmentMeetupSelectedPlaceBottomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_friend_bottom, container, false)
        _binding = FragmentMeetupSelectedPlaceBottomBinding.inflate(inflater, container, false)
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
        binding.tvBack.setRoundedColorBackground(requireActivity(),R.color.dark_transparent)
        binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(), R.color.gray1,)
        meetPager = activity?.supportFragmentManager?.findFragmentByTag(MeetUpViewPageFragment.TAG) as MeetUpViewPageFragment?

    }

    private fun setUp() {
        binding.tvBack.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcCreateMeetSelectedPlaceBack, null)
            meetPager?.binding?.meetUpViewpager?.setCurrentItem(1,true) }
        binding.rvPlaces.layoutManager = LinearLayoutManager(requireActivity(),LinearLayoutManager.VERTICAL,false)
    }

    private fun addObserver() {


    }

    fun initSelectedPlaceSource(
        selectedPlace: MutableMap<String?, MeetsPlace?>,
        customPlace: ArrayList<AddressModel>
    ){
        Log.i(TAG," dataCame::: 123432")
        this.customPlace = customPlace
        selectedPlaceList = selectedPlace
        if(selectedPlaceList.size.plus(this.customPlace.size) > 0){
            binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(),R.color.primaryDark)
            binding.addFriendOrNext.text = "Add ".plus(selectedPlaceList.size.plus(customPlace.size)).plus(" Place")
            binding.addFriendOrNext.setOnClickListener { meetPager?.binding?.meetUpViewpager?.setCurrentItem(3,true) }
        }
        selectedPlaceAdapted =  MeetUpSelctedPlaceAdapter(requireActivity(),selectedPlaceList,customPlace) {
            (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,1) as MeetUpPlaceBottomFragment?)?.refreshedChecked(true)
            (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,4) as MeetUpDateNTimeBottomFragment?)?.notifyPlaceItemChanges()
            checkPlaceCount()
        }
        binding.rvPlaces.adapter = selectedPlaceAdapted
    }

    fun checkPlaceCount(){
        if(selectedPlaceList.size.plus(this.customPlace.size) > 0){
            binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(),R.color.primaryDark)
            binding.addFriendOrNext.text = "Add ".plus(selectedPlaceList.size.plus(customPlace.size)).plus(" Place")
            binding.addFriendOrNext.setOnClickListener {
                MyApplication.putTrackMP(Constant.AcCreateMeetSelectedPlace, null)
                meetPager?.binding?.meetUpViewpager?.setCurrentItem(3,true)
            }
        }
        else{
            binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(),R.color.gray1,)
            binding.addFriendOrNext.text = "Add "
            binding.addFriendOrNext.setOnClickListener(null)
            meetPager?.binding?.meetUpViewpager?.setCurrentItem(1,true)
        }

    }

    fun notifySelectedPlaceItemChanges(){
        selectedPlaceAdapted.notifyDataSetChanged()
        checkPlaceCount()
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }
}