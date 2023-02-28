package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.SelectedInterestAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetupRulesBottomBinding
import com.meetsportal.meets.networking.profile.Definition
import com.meetsportal.meets.networking.profile.FullInterestGetResponse
import com.meetsportal.meets.ui.dialog.BadgeSelectAlert
import com.meetsportal.meets.ui.dialog.VoteSelectAlert
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.JoinTimeType
import com.meetsportal.meets.utils.Constant.badgeList
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class OpenMeetRuleFragment:BaseFragment(),DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    val TAG = OpenMeetRuleFragment::class.simpleName!!

    var selected: ArrayList<Definition?> = ArrayList()
    var meetPager:MeetUpViewPageFragment? = null
    var meetdate = Calendar.getInstance()
    var meetHour = meetdate.get(Calendar.HOUR_OF_DAY)
    var meetMinut = meetdate.get(Calendar.MINUTE)
    var datePicked = false
    var timePicked = false

    var allInterest = PreferencesManager.get<FullInterestGetResponse>(Constant.PREFRENCE_INTEREST)?.definition?:ArrayList()
    var badgeAlert : BadgeSelectAlert? = null
    var minBadge : String? = null
    private var _binding: FragmentMeetupRulesBottomBinding? = null
    private val binding get() = _binding!!
    var joinTimeOption :String?= "Until Meetup"
    var meetDurationOption:String? = "1 hour"

    var rule = Rules().apply {
        min_badge = badgeList.getOrNull(0)?.key
    };


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_friend_bottom, container, false)
        _binding = FragmentMeetupRulesBottomBinding.inflate(inflater, container, false)
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
        binding.tvMinBadge.visibility = View.VISIBLE
        binding.voteOption.visibility = View.GONE
        binding.tvMinBadge.text = "Select min level to request!!"
        binding.pageDes.text = "Define your meetup rules here. For example, select the meetup duration and join time"
        badgeAlert = BadgeSelectAlert(requireActivity()) {
            badgeList.getOrNull(it)?.let {
                minBadge = it.key
                binding.tvMinBadge.visibility = View.VISIBLE
                binding.tvMinBadge.text = it.name.plus( " status and above")
                rule.min_badge = it.key?:"bronze"
            }?:run{
                rule.min_badge = "bronze"
            }
        }

        binding.rlParntCategory.visibility = View.VISIBLE
        binding.tvBack.setRoundedColorBackground(requireActivity(), R.color.dark_transparent)
        binding.tvVoteChange.text = "Who can join :"
        binding.tvVoteCngDesc.text = "You can decide the people who can request to join"
        meetPager = activity?.supportFragmentManager?.findFragmentByTag(MeetUpViewPageFragment.TAG) as MeetUpViewPageFragment?
        binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(), R.color.primaryDark)

        binding.etMeetName.count {
            binding.tvMeetname.setText(binding.etMeetName.text.toString().trim())
            rule.name = binding.etMeetName.text.toString().trim()
        }
        binding.etMeetName.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.page_bg)
        binding.llDateTime.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.page_bg)
        binding.rlVoteChange.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.page_bg)
        binding.rljoinTime.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.page_bg)
        binding.rlDuration.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.page_bg)
        binding.rlCategory.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.page_bg)
        binding.tvLine.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.transparent)
        binding.rvCategory.layoutManager = FlexboxLayoutManager(requireActivity()).apply {
            flexDirection = FlexDirection.ROW;
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        binding.rvCategory.adapter = SelectedInterestAdapter(requireActivity(),selected,1,showCross = true){
            if(selected.isEmpty()){
                binding.tvClick.visibility=View.VISIBLE
            }else{
                binding.tvClick.visibility=View.GONE
            }
        }
        binding.addCategory.onClick({
            var interest = InterestFragment.getInstance(TAG, ArrayList(selected.map { it?.key }))
            Navigation.addFragment(requireActivity(),interest,InterestFragment.TAG,R.id.homeFram,true,false)
        })
        binding.rlCategory.onClick({
            var interest = InterestFragment.getInstance(TAG, ArrayList(selected.map { it?.key }))
            Navigation.addFragment(requireActivity(),interest,InterestFragment.TAG,R.id.homeFram,true,false)
        })
        binding.tvMeetname.setOnClickListener{
            binding.etMeetName.requestFocus()
            val inputMethodManager = ContextCompat.getSystemService(
                requireContext(),
                InputMethodManager::class.java
            )
            inputMethodManager?.showSoftInput(binding.etMeetName, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.voteOption.visibility = View.GONE
        binding.rlVoteChange.setOnClickListener {
            badgeAlert?.showAlert()
        }
       // binding.voteOption.setOnTouchListener(null)
        /*binding.voteOption.adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            badgeList.map { it.name.plus( " status and above") }.toTypedArray()
        ).apply {
            setDropDownViewResource(
                android.R.layout
                    .simple_spinner_dropdown_item);

        }*/

//        binding.joinOption.adapter = ArrayAdapter<String>(
//            requireContext(),
//            android.R.layout.simple_spinner_item,
//            resources.getStringArray(R.array.join_time_open_meetup)
//        ).apply {
//            setDropDownViewResource(
//                android.R.layout
//                    .simple_spinner_dropdown_item);
//        }

        binding.durationOption.text = meetDurationOption

        val openArray: Array<String> = resources.getStringArray(R.array.open_meet_duration_options)
        val openDurationAlert:VoteSelectAlert= VoteSelectAlert(requireActivity(),"Duration",openArray.toList()){it,st->
            meetDurationOption=st
            binding.durationOption.text=st
        }
        binding.rlDuration.onClick({
            openDurationAlert.showAlert()
        })

        setFragmentResultListener(TAG) {key, result->
            // get the result from bundle
            val stringResult = result.getStringArrayList("returnInterestKey")
            Log.i(TAG, "setUpResultListener:::  $stringResult")
            if(allInterest.isNullOrEmpty()){
                allInterest = PreferencesManager.get<FullInterestGetResponse>(Constant.PREFRENCE_INTEREST)?.definition?:ArrayList()
            }
            selected.clear()
            selected.addAll(allInterest?.filter { stringResult?.contains(it?.key)== true })
            if(selected.isEmpty()){
                binding.tvClick.visibility=View.VISIBLE
            }else{
                binding.tvClick.visibility=View.GONE
            }
            Log.i(TAG, "setUpResultListener:::  $selected")
            binding.rvCategory.adapter?.notifyDataSetChanged()
           // Toast.makeText(requireContext(), "$key: $stringResult", Toast.LENGTH_SHORT).show()

        }




    }

    private fun validateData():String?{
        if(binding.etMeetName.text.toString().trim().isEmpty()){
            return "Meet name should not be empty!!!"
        }
        else if(binding.selectedDate.text.equals("Select Date") || binding.selctedTime.text.equals("Select Time")){
            return "Please select Date and Time!!!"
        }
        else if(selected.isEmpty()){
            return "please select categories"
        }
        return null
    }

    private fun setUp() {

        binding.tvBack.setOnClickListener { meetPager?.binding?.meetUpViewpager?.setCurrentItem(0,true) }
        binding.rlPickDate.setOnClickListener{ pickDate() }
        binding.rlPickTime.setOnClickListener{ pickTime() }
        binding.addFriendOrNext.setOnClickListener {
            validateData()?.let {
                MyApplication.showToast(requireActivity(),it)
            }?:run{
                meetdate.set(Calendar.HOUR_OF_DAY,meetHour)
                meetdate.set(Calendar.MINUTE,meetMinut)

                Log.d(TAG, "onDateSet:meetdate 3 ${SimpleDateFormat("dd MMM hh:mm a", Locale.UK).format(meetdate.getTime())}")
                rule.date = meetdate.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
                var today = Calendar.getInstance().apply { time = Date() }
                when(joinTimeOption){
                    "Until Meetup"->{
                        rule.joinTime = Datatype(JoinTimeType.TIME,meetdate.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                    }
                    "24 hour"->{
                        today.add(Calendar.DATE, 1)
                        rule.joinTime = Datatype(JoinTimeType.TIME,today.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                    }
                    "5 Days"->{
                        today.add(Calendar.DATE, 5)
                        rule.joinTime = Datatype(JoinTimeType.TIME,today.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                    }
                    "10 Days"->{
                        today.add(Calendar.DATE, 10)
                        rule.joinTime = Datatype(JoinTimeType.TIME,today.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                    }
                    "15 Days"->{
                        today.add(Calendar.DATE, 15)
                        rule.joinTime = Datatype(JoinTimeType.TIME,today.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                    }
                }
                val meetDuration = Calendar.getInstance().apply { time = meetdate.time }
                when(meetDurationOption){
                    "1 hour"->{
                        meetDuration.add(Calendar.HOUR_OF_DAY,1)
                        rule.duration = meetDuration.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
                    }
                    "3 hour"->{
                        meetDuration.add(Calendar.HOUR_OF_DAY,3)
                        rule.duration = meetDuration.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
                    }
                    "7 hour"->{
                        meetDuration.add(Calendar.HOUR_OF_DAY,7)
                        rule.duration = meetDuration.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
                    }
                    "24 hour"->{
                        meetDuration.add(Calendar.HOUR_OF_DAY,24)
//                        meetDuration.add(Calendar.DATE,1)
//                        meetDuration.set(Calendar.HOUR_OF_DAY, 0);
//                        meetDuration.set(Calendar.MINUTE, 0);
//                        meetDuration.set(Calendar.SECOND, 0);
//                        meetDuration.set(Calendar.MILLISECOND, 0);
                        rule.duration = meetDuration.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
                    }
                }
                rule.desc = binding.etDesc.text.toString().trim()
                rule.categories = selected
                Log.d(TAG, "initView:rule::: $rule ")
                (meetPager?.pagerAdapter2?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,2) as OpenMeetReviewFragment?)?.getDataFromRulePage(rule)
                meetPager?.binding?.meetUpViewpager?.setCurrentItem(2,true)
            }
        }


        /*binding.voteOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.i(TAG," position:: ${position} ::min badge ${badgeList.getOrNull(position)?.name}")
                rule.min_badge = badgeList.getOrNull(position)?.key?:"bronze"
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }*/

//        binding.joinOption.setOnItemSelectedListener( object : AdapterView.OnItemSelectedListener{
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                Log.i(TAG," position:: ${position}")
//                joinTimeOption = position
//                /*when(position){
//                    0->{
//                        rule.joinTime = Datatype(JoinTimeType.TILL_MEETUP,"untilMeetUp")
//                    }
//                    1->{
//                        rule.joinTime = Datatype(JoinTimeType.TIME,"1")
//                    }
//                    2->{
//                        rule.joinTime = Datatype(JoinTimeType.TIME,"5")
//                    }
//                    3->{
//                        rule.joinTime = Datatype(JoinTimeType.TIME,"10")
//                    }
//                    4->{
//                        rule.joinTime = Datatype(JoinTimeType.TIME,"15")
//                    }
//                }*/
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//            }
//
//        })
    }

    private fun addObserver() {


    }
    private fun pickDate() {
//        val now: Calendar = Calendar.getInstance()
        val dpd = DatePickerDialog.newInstance(
            this,
            meetdate.get(Calendar.YEAR),  // Initial year selection
                meetdate.get(Calendar.MONTH),  // Initial month selection
                meetdate.get(Calendar.DAY_OF_MONTH)
            // Inital day selection
        )
        dpd.minDate = Calendar.getInstance()
        dpd.setTitle("Choose MeetUp Date")
        dpd.accentColor = ContextCompat.getColor(requireContext(), R.color.primaryDark)
        dpd.setOkColor(ContextCompat.getColor(requireContext(), R.color.primaryDark))
        dpd.setCancelColor(ContextCompat.getColor(requireContext(), R.color.gray))

        dpd.show(parentFragmentManager, "Datepickerdialog");
    }

    private fun pickTime() {
        if(!datePicked){
            showToast("Select a Date first")
            return
        }
        val tpd = TimePickerDialog.newInstance(this,
                meetHour,
                meetMinut,
            false)
        tpd.title = "Choose MeetUp Time"
        tpd.accentColor = ContextCompat.getColor(requireContext(), R.color.primaryDark)
        tpd.setOkColor(ContextCompat.getColor(requireContext(), R.color.primaryDark))
        tpd.setCancelColor(ContextCompat.getColor(requireContext(), R.color.gray))
        tpd.show(parentFragmentManager, "Datepickerdialog")
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        Log.i(TAG," $year $monthOfYear $dayOfMonth")
        val dateCalendar = Calendar.getInstance()
        dateCalendar.set(Calendar.YEAR, year)
        dateCalendar.set(Calendar.MONTH, monthOfYear)
        dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        dateCalendar.set(Calendar.HOUR_OF_DAY, 0)
        dateCalendar.set(Calendar.MINUTE, 0)
        dateCalendar.set(Calendar.SECOND, 0)
        if(timePicked){
            dateCalendar.set(Calendar.HOUR_OF_DAY, meetHour)
            dateCalendar.set(Calendar.MINUTE, meetMinut)
            val milliDiff = dateCalendar.time.time - Date().time
            val hours = TimeUnit.HOURS.convert(milliDiff, TimeUnit.MILLISECONDS)
            if(milliDiff<0){
                showToast("Selected Time is invalid")
                binding.selctedTime.text= "Select Time"
                timePicked=false
            }
            setSpinner(hours)
        }
        datePicked=true

        meetdate = dateCalendar

        var sdfShow = SimpleDateFormat("E, dd MMM yyyy", Locale.UK)
        Log.d(TAG, "onDateSet:meetdate 1 ${SimpleDateFormat("dd MMM hh:mm a", Locale.UK).format(meetdate.getTime())}")
        binding.selectedDate.text = sdfShow.format(dateCalendar.getTime())
        rule.date = sdfShow.format(dateCalendar.getTime())
        //Log.i(TAG," cheking date:: ${sdfShow.format(meetdate.getTime())} --- ${resources.configuration.locale} --- ${TimeZone.getDefault()}")

        //isDateSelect = true
    }

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        val dateCalendar=meetdate
        dateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        dateCalendar.set(Calendar.MINUTE, minute)
        dateCalendar.set(Calendar.SECOND, 0)
        val milliDiff = dateCalendar.time.time - Date().time
        val hours = TimeUnit.HOURS.convert(milliDiff, TimeUnit.MILLISECONDS)
        setSpinner(hours)
        if(milliDiff<0){
            showToast("Selected Time is Past, please select a valid time")
            return
        }
        timePicked=true
        meetHour = hourOfDay
        meetMinut = minute
        var hour=meetHour

        var timeSet = ""
        if(hour > 12) {
            hour -= 12
            timeSet = "PM"
        } else if(hour == 0) {
            hour += 12
            timeSet = "AM"
        } else if(hour == 12) {
            timeSet = "PM"
        } else {
            timeSet = "AM"
        }

        var min: String? = ""
        if(meetMinut < 10) min = "0$meetMinut" else min = java.lang.String.valueOf(meetMinut)

        val mTime: String = StringBuilder().append(hour).append(':').append(min).append(" ")
                .append(timeSet).toString()
        rule.time = mTime
        binding.selctedTime.text = mTime
//        Log.i(TAG," checkingHour:: $hourOfDay")
//        val time = "" + String.format("%02d",hourOfDay%12).plus(":").plus(String.format("%02d",minute)).plus(if(hourOfDay>11)" pm" else " am")
//        binding.selctedTime.text = time
//        rule.time = time
        //isTimeSelect = true
    }

    private fun setSpinner(hours: Long) {
        val stringArray = resources.getStringArray(R.array.join_time_open_meetup)
        val array:List<String>
        joinTimeOption="Until Meetup"
        if(hours<0){
            array=stringArray.take(0)
        }else if(hours<24){
            array=stringArray.take(1)
        }else if(hours<120){
            array=stringArray.take(2)
        }else if(hours<240){
            array=stringArray.take(3)
        }else{
            array=stringArray.toList()
        }
        val voteSelectAlert: VoteSelectAlert = VoteSelectAlert(requireActivity(),"Join Time",array){ it, st->
            joinTimeOption = st
            binding.joinOption.text=st
        }
        binding.rljoinTime.onClick({
            voteSelectAlert.showAlert()
        })
        binding.joinOption.text=joinTimeOption
    }

    data class Rules(
        var name : String? =  null,
        var date : String? = null,
        var desc : String? =null,
        var time : String? =null,
        var duration : String?=null,
        var min_badge : String? = null,
        var categories : ArrayList<Definition?>? = null,
        var joinTime : Datatype? = null,
    )

    data class Datatype(
        var typeJoin : JoinTimeType? = null,
        var value : String? = null
    )

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

}