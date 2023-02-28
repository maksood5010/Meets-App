package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.MeetUpDateSelectedPeopleAdapter
import com.meetsportal.meets.adapter.MeetUpDateSelectedPlaceAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetUpTimeBottomBinding
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.models.MeetsPlace
import com.meetsportal.meets.models.SelectedContactPeople
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.ui.dialog.AddNameAlert
import com.meetsportal.meets.ui.dialog.CreateMeetUpAlert
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment.MeetType
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MeetUpDateNTimeBottomFragment : BaseFragment(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener{

    val TAG = MeetUpDateNTimeBottomFragment::class.simpleName

    var selectedPlaceList : MutableMap<String?, MeetsPlace?> = mutableMapOf()
    lateinit var selectedPlaceAdapted : MeetUpDateSelectedPlaceAdapter

    var selectedPeopleList : ArrayList<SelectedContactPeople>? = null
    lateinit var selectedPeopleAdapted : MeetUpDateSelectedPeopleAdapter
    lateinit var addNameAlert : AddNameAlert
    var alert : CreateMeetUpAlert? = null

    var meetPager:MeetUpViewPageFragment? = null
    var isDateSelect = false
    var isTimeSelect = false
    var rules : MeetUpRulesBottomFragment.Rules? = null
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
        //setupUI(binding.root)>
        initView(view)
        setUp()
        setUpObserver()
    }



    private fun initView(view: View) {
        binding.invite.text = "Send Invitation"
        binding.back.setRoundedColorBackground(requireActivity(),R.color.dark_transparent)
        meetPager = activity?.supportFragmentManager?.findFragmentByTag(MeetUpViewPageFragment.TAG) as MeetUpViewPageFragment?
        addNameAlert = AddNameAlert(requireActivity()){
            meetName = it
            binding.tvAddName.text = it

        }
        alert = CreateMeetUpAlert(requireActivity()){
//            ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager,1) as MeetUpNew).getMeetUps()
            MyApplication.putTrackMP(Constant.AcCreateMeetupEnterChat, null)
//            (activity as MainActivity?)?.viewPager?.setCurrentItem(1)
            parentFragmentManager.popBackStack()
            it?._id?.let {
                var baseFragment :BaseFragment = MeetUpVotingFragment.getInstance(it)
                Navigation.addFragment(requireActivity(),baseFragment,MeetUpVotingFragment.TAG,R.id.homeFram,true,true)
            }?:run{

            }
        }
    }

    private fun setUp() {
        binding.invite.setRoundedColorBackground(requireActivity(),R.color.primaryDark,corner = 30f)
        binding.back.setOnClickListener {
            meetPager?.binding?.meetUpViewpager?.setCurrentItem(3,true)
        }
        /*binding.tvAddName.setOnClickListener {
            addNameAlert.showDialog(meetName)
        }*/
        binding.ivEditName.setOnClickListener {
            addNameAlert.showDialog(meetName)
        }
        binding.rvFriends.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        binding.rvPlaces.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        Utils.onClick(binding.llDatePicker,500){
            pickDate()
        }
        /*Utils.onClick(binding.llTimePicker,500){
            pickTime()
        }*/
        Utils.onClick(binding.invite,1000){
            Log.i(TAG," validating meetuprequest:: ")
            validate()?.let {
                MyApplication.showToast(requireActivity(),it)
            }?:run {
                Log.i(TAG,"send invitation $meetHour $meetMinut")
                MyApplication.putTrackMP(Constant.AcCreateMeetupInvitation, null)
                /*meetdate.set(Calendar.HOUR,meetHour)
                meetdate.set(Calendar.MINUTE,meetMinut)
                var date1 = meetdate.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
                var date2 = meetdate.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")*/
                //Log.i(TAG," chekinghhh:: $date1 --- $date2 --")
                //alert?.setInvite(selectedPeopleList?.take(2)?.joinToString(",").plus("..."))

                alert?.setInvite(selectedPeopleList?.take(2)?.map { it.contactName }?.joinToString(",").plus("..."))
                alert?.showDialog(Constant.MeetType.PRIVATE)
                meetUpviewModel.createMeetUp(selectedPlaceList,customPlace,selectedPeopleList,rules)
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
        selectedPeopleList?.let {
            if(it.isEmpty()) return "Please select some friend first"
        }?: run {
            return "Please select some friend first"
        }
        /*if(!isDateSelect || !isTimeSelect){
            return "please select date and time both"
        }*/
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
        selectedPlaceAdapted =  MeetUpDateSelectedPlaceAdapter(requireActivity(),selectedPlaceList,customPlace,
            MeetType.PRIVATE ,{
                (meetPager?.pagerAdapter?.instantiateItem(
                    meetPager?.binding?.meetUpViewpager!!,
                    1
                ) as MeetUpPlaceBottomFragment?)?.refreshedChecked(true)
            },{
            meetPager?.binding?.meetUpViewpager?.setCurrentItem(1,true)
        })
        binding.rvPlaces.adapter = selectedPlaceAdapted
    }

    fun initSelecyedPeopleDataSource(list : ArrayList<SelectedContactPeople>?){
        selectedPeopleList = list
        selectedPeopleAdapted = MeetUpDateSelectedPeopleAdapter(requireActivity(),selectedPeopleList,{
            (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,0) as MeetUpFriendtBottomFragment?)?.updateSelectCOntactFromDateTime(it!!)
            if(selectedPeopleList?.size == 0)
                meetPager?.binding?.meetUpViewpager?.setCurrentItem(0,true)
        },{
            meetPager?.binding?.meetUpViewpager?.setCurrentItem(0,true)
        })
        binding.rvFriends.adapter = selectedPeopleAdapted
    }

    fun notifyPlaceItemChanges(){
        selectedPlaceAdapted.notifyDataSetChanged()
    }

    fun notifyPeopleItemChange(){
        Log.i(TAG," notifyPeopleItemChange::: ")
        selectedPeopleAdapted.notifyDataSetChanged()
    }

    private fun pickDate() {
        val now: Calendar = Calendar.getInstance()
        val dpd = DatePickerDialog.newInstance(
            this,
            now.get(Calendar.YEAR),  // Initial year selection
            now.get(Calendar.MONTH),  // Initial month selection
            now.get(Calendar.DAY_OF_MONTH)
            // Inital day selection
        )
        dpd.minDate = Calendar.getInstance()
        dpd.setTitle("Choose MeetUp Date")
        dpd.accentColor = ContextCompat.getColor(requireContext(),R.color.primaryDark)
        dpd.setOkColor(ContextCompat.getColor(requireContext(),R.color.primaryDark))
        dpd.setCancelColor(ContextCompat.getColor(requireContext(),R.color.gray))

        dpd.show(parentFragmentManager, "Datepickerdialog");
    }

    private fun pickTime() {
        val now: Calendar = Calendar.getInstance()
        val tpd = TimePickerDialog.newInstance(this,
            now.get(Calendar.HOUR),
            now.get(Calendar.MINUTE),
            false)
        tpd.title = "Choose MeetUp Time"
        tpd.accentColor = ContextCompat.getColor(requireContext(),R.color.primaryDark)
        tpd.setOkColor(ContextCompat.getColor(requireContext(),R.color.primaryDark))
        tpd.setCancelColor(ContextCompat.getColor(requireContext(),R.color.gray))
        tpd.show(parentFragmentManager, "Datepickerdialog");
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        Log.i(TAG," $year $monthOfYear $dayOfMonth")
        var dateCalendar = Calendar.getInstance()
        dateCalendar.set(Calendar.YEAR, year)
        dateCalendar.set(Calendar.MONTH, monthOfYear)
        dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        dateCalendar.set(Calendar.HOUR, 0)
        dateCalendar.set(Calendar.MINUTE, 0)
        dateCalendar.set(Calendar.SECOND, 0)
        dateCalendar.add(Calendar.HOUR ,-12)

        meetdate = dateCalendar

        var sdfShow = SimpleDateFormat("E, dd MMM yyyy", Locale.UK)
        //var sdfShow = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.UK)

        binding.tvDatePickText.text = sdfShow.format(meetdate.getTime())
        Log.i(TAG," cheking date:: ${sdfShow.format(meetdate.getTime())} --- ${resources.configuration.locale} --- ${TimeZone.getDefault()}")

        isDateSelect = true
    }

    fun getDataFromRulePage(rule: MeetUpRulesBottomFragment.Rules){
        Log.i(TAG," rules::: $rule")
        rules = rule
        binding.tvAddName.text = rule.name
        binding.tvDate.text = rule.date?.toDate()?.formatTo("dd MMM yyyy")
        binding.tvTime.text = rule.date?.toDate()?.formatTo("hh:mm a")
        val dayDiff = Utils.getHourDiff(rule.duration?.toDate(),rule.date?.toDate())
        binding.durationCount.text = "$dayDiff hrs"
        if(rule.voteChnge?.compareTo(1000) == -1){
            binding.voteChangeCount.text = rule.voteChnge.toString()
        }else{
            binding.voteChangeCount.text= "Unlimited"
        }
        if(!rule.joinTime?.type.equals("until_confirm")){
            binding.joinTime.text = rule.joinTime?.value?.toDate()?.formatTo("dd MMM yyyy - hh:mm a")
        }else{
            binding.joinTime.text = "till Meetup Confirm"
        }
        if(!rule.desc.isNullOrEmpty()){
            binding.desc.visibility =View.VISIBLE
            binding.tvDesc.visibility =View.VISIBLE
            Log.i(TAG,"  rule.desc:: ${rule.desc}")
            binding.desc.text = rule.desc
        }else{
            binding.desc.visibility =View.GONE
            binding.tvDesc.visibility =View.GONE
        }
    }

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        //val time = "" + (hourOfDay%12).toString().plus(":").plus(minute.toString()).plus(if(hourOfDay>11)" pm" else " am")
        meetHour = hourOfDay
        meetMinut = minute
        Log.i(TAG," checkingHour:: $hourOfDay")
        val time = "" + String.format("%02d",hourOfDay%12).plus(":").plus(String.format("%02d",minute)).plus(if(hourOfDay>11)" pm" else " am")

        //binding.tvTimePickText.text = time
        isTimeSelect = true
    }

    private fun setUpObserver() {
        meetUpviewModel.observeCreateMeetUp().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success ->{
                    alert?.changeStatus(true,it.value)
                    Toast.makeText(requireActivity()," MeetUp created successfully!!!",Toast.LENGTH_LONG).show()
                }
                is ResultHandler.Failure -> {
                    Log.i(TAG," meetup not created")
                    alert?.changeStatus(false,null)
                    Toast.makeText(requireActivity()," ${it.message}",Toast.LENGTH_LONG).show()
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