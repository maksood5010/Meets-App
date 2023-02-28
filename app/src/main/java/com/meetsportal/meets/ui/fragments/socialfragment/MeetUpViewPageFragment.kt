package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.meetsportal.meets.databinding.FragmentMeetUpViewpagerBinding
import com.meetsportal.meets.ui.fragments.BaseFragment

class MeetUpViewPageFragment : BaseFragment() {

   // public val TAG = MeetUpViewPageFragment::class.java.simpleName
   // lateinit var viewpage: ViewPager
    lateinit var pagerAdapter : ViewPagerAdapter
    lateinit var pagerAdapter2 : ViewPagerAdapter2

    private var _binding: FragmentMeetUpViewpagerBinding? = null
    val binding get() = _binding!!

    companion object{
        val TAG = MeetUpViewPageFragment::class.java.simpleName!!
        val IS_IT_PLACE = "isItPlace"
        val MEET_TYPE = "meetType"



        fun getInstance(id:String?,isItPlace:Boolean):MeetUpViewPageFragment{
            return MeetUpViewPageFragment().apply {
                Log.i(TAG," trackplaceId:::: 8888 ${id}")
                arguments = Bundle().apply {
                    putBoolean(IS_IT_PLACE,isItPlace)
                    putString("id",id)
                }
            }
        }

        fun getInstanceForOpenMeetUp(meetType:MeetType):MeetUpViewPageFragment{
            return MeetUpViewPageFragment().apply {
                arguments = Bundle().apply {
                    putInt(MEET_TYPE,meetType.type)
                }
            }

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_viewpager, container, false)
        _binding = FragmentMeetUpViewpagerBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setupUI(binding.root)
        initView(view)
        setUp()
    }

    private fun initView(view: View) {
        //viewpage = view.findViewById(R.id.meet_up_viewpager)
    }

    private fun setUp() {

        Log.i(TAG," trackplaceId:::: 4444 ${arguments?.getString("id")}")
        Log.i(TAG," trackplaceId:::: type ${arguments?.getInt(MEET_TYPE)}")
        when(arguments?.getInt(MEET_TYPE)){
            MeetType.PRIVATE.type -> {
                binding.meetUpViewpager.offscreenPageLimit = 5
                pagerAdapter = ViewPagerAdapter(parentFragmentManager,arguments?.getString("id"),this)
                binding.meetUpViewpager.adapter = pagerAdapter
                binding.meetUpViewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    }

                    override fun onPageSelected(position: Int) {
                        //
                        Log.d(TAG, "onPageSelected:pagerAdapter.instantiateItem $position ")
                        if(position==1) {
                            (pagerAdapter.instantiateItem(binding.meetUpViewpager, 1) as MeetUpPlaceBottomFragment?)?.setScroll()
                        }
                    }

                    override fun onPageScrollStateChanged(state: Int) {

                    }

                })
            }
            MeetType.OPEN.type->{
                binding.meetUpViewpager.offscreenPageLimit = 3
                Log.i(TAG," checking::344:::  ${activity?.supportFragmentManager}")

                pagerAdapter2 = ViewPagerAdapter2(parentFragmentManager,this)
                binding.meetUpViewpager.adapter = pagerAdapter2
            }else->{
                binding.meetUpViewpager.offscreenPageLimit = 5
                pagerAdapter = ViewPagerAdapter(parentFragmentManager,arguments?.getString("id"),this)
                binding.meetUpViewpager.adapter = pagerAdapter
            binding.meetUpViewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    //
                    Log.d(TAG, "onPageSelected:pagerAdapter.instantiateItem $position ")
                    if(position==1) {
                        (pagerAdapter.instantiateItem(binding.meetUpViewpager, 1) as MeetUpPlaceBottomFragment?)?.setScroll()
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {

                }

            })
            }
        }

    }

    class ViewPagerAdapter(fm: FragmentManager,var id:String?,var fragment : Fragment) : FragmentStatePagerAdapter(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount()=5
        override fun getItem(position: Int): Fragment {

            var meetFriend = MeetUpFriendtBottomFragment.getInstance()
            var meetPlace = MeetUpPlaceBottomFragment.getInstance()
            //var selectedPlace =

            var  isItPlace = fragment.arguments?.getBoolean(IS_IT_PLACE)
            if(isItPlace != null && isItPlace == true){
                meetPlace = MeetUpPlaceBottomFragment.getInstance(id)
            }else if(isItPlace != null && isItPlace == false){
                meetFriend = MeetUpFriendtBottomFragment.getInstance(id)
            }

            Log.i("TAG"," trackplaceId:::: 2 ${id}")
            return when(position){
                0->meetFriend
                1-> meetPlace
                2 -> MeetUpSelectedPlaceBottomFragment()
                3 -> MeetUpRulesBottomFragment()
                4-> MeetUpDateNTimeBottomFragment()
                else-> meetFriend
            }
        }
    }

    class ViewPagerAdapter2(fm: FragmentManager?,var fragment : Fragment) : FragmentStatePagerAdapter(fm?:fragment.childFragmentManager, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount()=3
        override fun getItem(position: Int): Fragment {

            var meetPlace = OpenMeetPlaceBottomFragment.getInstance()


            return when(position){
                0-> OpenMeetPlaceBottomFragment()
                1 -> OpenMeetRuleFragment()
                2-> OpenMeetReviewFragment()
                else -> meetPlace
            }
        }
    }


    enum class MeetType(val type:Int){
        PRIVATE(1),
        OPEN(2)
    }


    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

    override fun hideNavBar(): Boolean {
        return true
    }


    override fun onBackPressed(): Boolean {
        val showProceed = showProceed {
            parentFragmentManager.popBackStack()
        }
        showProceed.setMessage("Discard","Are you sure you want to discard meetup?")
        return false
    }


}