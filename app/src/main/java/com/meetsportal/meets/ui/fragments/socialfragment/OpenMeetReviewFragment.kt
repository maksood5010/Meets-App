package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.MeetUpDateSelectedPeopleAdapter
import com.meetsportal.meets.adapter.MeetUpDateSelectedPlaceAdapter
import com.meetsportal.meets.adapter.SelectedInterestAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetUpTimeBottomBinding
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.models.MeetsPlace
import com.meetsportal.meets.models.SelectedContactPeople
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.overridelayout.SpaceItemDecoration
import com.meetsportal.meets.ui.dialog.AddNameAlert
import com.meetsportal.meets.ui.dialog.CreateMeetUpAlert
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment.MeetType
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class OpenMeetReviewFragment:BaseFragment() {

    val TAG = MeetUpDateNTimeBottomFragment::class.simpleName

    var selectedPlaceList : MutableMap<String?, MeetsPlace?> = mutableMapOf<String?, MeetsPlace?>()
    lateinit var selectedPlaceAdapted : MeetUpDateSelectedPlaceAdapter

    var selectedPeopleList : ArrayList<SelectedContactPeople>? = null
    lateinit var selectedPeopleAdapted : MeetUpDateSelectedPeopleAdapter
    lateinit var addNameAlert : AddNameAlert
    var alert : CreateMeetUpAlert? = null

    var meetPager:MeetUpViewPageFragment? = null
    var isDateSelect = false
    var isTimeSelect = false
    var rules : OpenMeetRuleFragment.Rules? = null
    var meetdate = Calendar.getInstance()
    var meetHour = 0
    var meetMinut = 0

    lateinit var customPlace : ArrayList<AddressModel>

    var meetName: String? = null

    val meetUpviewModel: MeetUpViewModel by viewModels()
    private var _binding: FragmentMeetUpTimeBottomBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMeetUpTimeBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setUp()
        setUpObserver()
    }



    private fun initView(view: View) {
        binding.tvChosenRestaurant.text = "Chosen Place"
        binding.llFriend.visibility = View.GONE
        binding.llCategory.visibility = View.VISIBLE
        binding.rvCategory.layoutManager = FlexboxLayoutManager(requireActivity()).apply {
            flexDirection = FlexDirection.ROW;
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        binding.voteChange.text = "Who can join :"
        binding.back.setRoundedColorBackground(requireActivity(), R.color.dark_transparent)
        binding.back.setRoundedColorBackground(requireActivity(), R.color.dark_transparent)
        meetPager = activity?.supportFragmentManager?.findFragmentByTag(MeetUpViewPageFragment.TAG) as MeetUpViewPageFragment?
        addNameAlert = AddNameAlert(requireActivity()){
            meetName = it
            binding.tvAddName.text = it

        }

        alert = CreateMeetUpAlert(requireActivity()){
            Log.i(TAG," Confirm Clicked")
//            ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager,0) as TimeLineFragment).refreshTimeLine()
//            (activity as MainActivity?)?.viewPager?.setCurrentItem(0)
//            parentFragmentManager.popBackStack()
            getMain()?.popBackStack()
            var baseFragment: BaseFragment = DetailPostFragment()
            Navigation.setFragmentData(
                baseFragment,
                "post_id",
                it?.post_id
            )
            Navigation.addFragment(
                requireActivity(),
                baseFragment,
                Constant.TAG_DETAIL_POST_FRAGMENT,
                R.id.homeFram,
                true,
                false
            )
        }
    }

    private fun setUp() {
        binding.invite.setRoundedColorBackground(requireActivity(), R.color.primaryDark,corner = 30f)
        binding.back.setOnClickListener {
            meetPager?.binding?.meetUpViewpager?.setCurrentItem(1,true)
        }

        binding.ivEditName.setOnClickListener {
            addNameAlert.showDialog(meetName)
        }
        binding.rvFriends.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL,false)
        binding.rvPlaces.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.HORIZONTAL,false)
        Utils.onClick(binding.invite,1000){
            Log.i(TAG," validating meetuprequest:: ")
            validate()?.let {
                MyApplication.showToast(requireActivity(),it)
            }?:run {
                Log.i(TAG,"send invitation $meetHour $meetMinut")
                //alert?.setInvite(selectedPeopleList?.take(2)?.map { it.contactName }?.joinToString(",").plus("..."))
                alert?.setInvite("")
                alert?.showDialog(Constant.MeetType.OPEN)
                meetUpviewModel.createOpenMeet(selectedPlaceList,customPlace,selectedPeopleList,rules)
            }
        }
    }

    private fun validate():String?{
        if(rules?.name == null || rules?.name?.trim()?.isEmpty() ==  true){
            return "Please Add Meet Name"
        }
        else if(selectedPlaceList.isEmpty() && customPlace.isEmpty()){
            return "Please select place first"
        }
        else if(rules?.categories?.isNullOrEmpty() == true){
            return "please select category"
        }
        if(rules?.date.isNullOrEmpty() ){
            return "please select date and time both"
        }
        Log.i(TAG," sendNull")
        return  null
    }

    fun initSelectedPlaceDataSource(
        selectedPlace: MutableMap<String?, MeetsPlace?>,
        customPlace: ArrayList<AddressModel>
    ){
        this.customPlace = customPlace
        selectedPlaceList = selectedPlace
        selectedPlaceAdapted =  MeetUpDateSelectedPlaceAdapter(requireActivity(),selectedPlaceList,customPlace, MeetType.OPEN,{
            (meetPager?.pagerAdapter2?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,0) as OpenMeetPlaceBottomFragment?)?.refreshedChecked(true)
        },{
            meetPager?.binding?.meetUpViewpager?.setCurrentItem(0,true)
        })
        binding.rvPlaces.adapter = selectedPlaceAdapted
    }

   /* fun initSelecyedPeopleDataSource(list : ArrayList<SelectedContactPeople>?){
        selectedPeopleList = list
        selectedPeopleAdapted = MeetUpDateSelectedPeopleAdapter(requireActivity(),selectedPeopleList,{
            (meetPager?.pagerAdapter2?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,0) as MeetUpFriendtBottomFragment?)?.updateSelectCOntactFromDateTime(it!!)
        },{
            meetPager?.binding?.meetUpViewpager?.setCurrentItem(0,true)
        })
        binding.rvFriends.adapter = selectedPeopleAdapted
    }*/

    fun notifyPlaceItemChanges(){
        selectedPlaceAdapted.notifyDataSetChanged()
    }

    fun notifyPeopleItemChange(){
        Log.i(TAG," notifyPeopleItemChange::: ")
        selectedPeopleAdapted.notifyDataSetChanged()
    }



    fun getDataFromRulePage(rule: OpenMeetRuleFragment.Rules){
        Log.i(TAG," rules::: $rule")
        rules = rule
        binding.rvCategory.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(2f, resources), Utils.dpToPx(10f, resources)))
        binding.rvCategory.adapter = SelectedInterestAdapter(requireActivity(),rule.categories?:ArrayList(),1,showCross = false)
        binding.tvAddName.text = rule.name
        binding.tvDate.text = rule.date?.toDate()?.formatTo("dd MMM yyyy")
        binding.tvTime.text = rule.date?.toDate()?.formatTo("hh:mm a")
        val dayDiff = Utils.getHourDiff(rule.duration?.toDate(),rule.date?.toDate())
        binding.durationCount.text = "$dayDiff hrs"
        binding.voteChangeCount.text = Utils.getBadge(rule.min_badge).name.toString().plus(" status and above")
        if(!rule.joinTime?.typeJoin?.type.equals("untilConfirm")){
            binding.joinTime.text = rule.joinTime?.value?.toDate()?.formatTo("dd MMM yyyy - hh:mm a")
        }else{
            binding.joinTime.text = "till Meetup Confirm"
        }
        if(!rule.desc.isNullOrEmpty()){
            binding.desc.visibility = View.VISIBLE
            binding.tvDesc.visibility = View.VISIBLE
            Log.i(TAG,"  rule.desc:: ${rule.desc}")
            binding.desc.text = rule.desc
        }else{
            binding.desc.visibility = View.GONE
            binding.tvDesc.visibility = View.GONE
        }

    }


    private fun setUpObserver() {
        meetUpviewModel.observeOpenCreateMeet().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success ->{
                    alert?.changeStatus(true,it.value)
                    Toast.makeText(requireActivity()," MeetUp created successfully!!!", Toast.LENGTH_LONG).show()
                }
                is ResultHandler.Failure -> {
                    Log.i(TAG," meetup not created")
                    alert?.changeStatus(false,null)
                    Toast.makeText(requireActivity()," ${it.message}", Toast.LENGTH_LONG).show()
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