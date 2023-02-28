package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetupRulesBottomBinding
import com.meetsportal.meets.ui.dialog.VoteSelectAlert
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MeetUpRulesBottomFragment: BaseFragment(),DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    val TAG = MeetUpRulesBottomFragment::class.simpleName

    var meetPager:MeetUpViewPageFragment? = null
    var meetdate = Calendar.getInstance()
    var meetHour = meetdate.get(Calendar.HOUR_OF_DAY)
    var meetMinut = meetdate.get(Calendar.MINUTE)

    private var _binding: FragmentMeetupRulesBottomBinding? = null
    private val binding get() = _binding!!

    var joinTimeOption:String?="Until Meetup"
    var meetDurationOption:String? = "1 hour"
    var datePicked = false
    var timePicked = false

    var rule = Rules().apply {
        voteChnge = 1
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
        binding.tvBack.setRoundedColorBackground(requireActivity(),R.color.dark_transparent)
        meetPager = activity?.supportFragmentManager?.findFragmentByTag(MeetUpViewPageFragment.TAG) as MeetUpViewPageFragment?
        binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(), R.color.primaryDark)
        binding.etMeetName.count {
            binding.tvMeetname.setText(binding.etMeetName.text.toString().trim())
            rule.name = binding.etMeetName.text.toString().trim()
        }
        binding.etMeetName.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.page_bg)
        binding.llDateTime.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.page_bg)
        binding.rlDuration.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.page_bg)
        binding.rlVoteChange.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.page_bg)
        binding.rljoinTime.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.page_bg)
        binding.tvLine.setRoundedColorBackground(requireActivity(),enbleDash = true,strokeColor = R.color.black,Dashgap = 0f,stripSize = 20f,strokeHeight = 1f,color = R.color.transparent)
//        val focus= View.OnFocusChangeListener { vi, b ->
//            if(b){
//                binding.addFriendOrNext.visibility=View.GONE
//            }else{
//                binding.addFriendOrNext.visibility=View.VISIBLE
//            }
//        }
//        binding.etDesc.onFocusChangeListener = focus
//        binding.etMeetName.onFocusChangeListener = focus
        binding.tvMeetname.setOnClickListener{
            binding.etMeetName.requestFocus()
            val inputMethodManager = getSystemService(requireContext(),
                InputMethodManager::class.java)
            inputMethodManager?.showSoftInput(binding.etMeetName, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.voteOption.text="1 Time"

        val stringArray: Array<String> = resources.getStringArray(R.array.vote_freq)
        val voteSelectAlert:VoteSelectAlert= VoteSelectAlert(requireActivity(),"Vote Change",stringArray.toList()){it,st->
            when(it){
                0-> {
                    rule.voteChnge = 1
                }
                1->{ rule.voteChnge = 3 }
                2 -> { rule.voteChnge = 5}
                3 -> { rule.voteChnge  = 1000 }
            }
            binding.voteOption.text=st
        }
        binding.rlVoteChange.onClick({
            voteSelectAlert.showAlert()
        })
        binding.durationOption.text=meetDurationOption

        val openArray: Array<String> = resources.getStringArray(R.array.open_meet_duration_options)
        val openDurationAlert:VoteSelectAlert= VoteSelectAlert(requireActivity(),"Duration",openArray.toList()){it,st->
            meetDurationOption=st
            binding.durationOption.text=st
        }
        binding.rlDuration.onClick({
            openDurationAlert.showAlert()
        })

        /*binding.joinOption.prompt = "Who can join"
        val adapter = ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.join_time_private_meetup, R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)*/
        //binding.joinOption.adapter = SpinnerArrayAdapter(requireActivity(),resources.getStringArray(R.array.join_time_private_meetup).toList())
        //binding.joinOption.adapter = SpinnerListAdapter(requireActivity(),resources.getStringArray(R.array.join_time_private_meetup))
        //spinner.setAdapter(adapter);

    }

    private fun validateData():String?{
        if(binding.etMeetName.text.toString().trim().isEmpty()){
            return "Meet name should not be empty!!!"
        }
        else if(binding.selectedDate.text.equals("Select Date") || binding.selctedTime.text.equals("Select Time")){
            return "Please select Date and Time!!!"
        }
        return null
    }

    private fun setUp() {

        binding.tvBack.setOnClickListener { meetPager?.binding?.meetUpViewpager?.setCurrentItem(2,true) }
        binding.rlPickDate.setOnClickListener{ pickDate() }
        binding.rlPickTime.setOnClickListener{ pickTime() }
        binding.addFriendOrNext.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcCreateMeetRuleAdd, null)
            validateData()?.let {
                MyApplication.showToast(requireActivity(),it)
            }?:run{
                meetdate.set(Calendar.HOUR_OF_DAY,meetHour)
                meetdate.set(Calendar.MINUTE,meetMinut)
                rule.date = meetdate.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
               /* when(rule.joinTime?.type){
                    //"untilMeetUp" -> rule.joinTime = Datatype("untilMeetUp",meetdate.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                    "until_confirm" -> { rule.joinTime = Datatype("until_confirm","until_confirm")
                    }
                    "time"-> {
                        var today = Calendar.getInstance().apply { time = Date() }
                        if(rule.joinTime?.value.equals("untilMeetUp")){
                            rule.joinTime = Datatype("time",meetdate.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                        }
                        else if(rule.joinTime?.value.equals("1")){
                            today.add(Calendar.DATE, 1)
                            rule.joinTime = Datatype("time",today.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                        }
                        else{
                            today.add(Calendar.DATE, 2)
                            rule.joinTime = Datatype("time",today.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                        }
                    }
                }*/
                var today = Calendar.getInstance().apply { time = Date() }
                when(joinTimeOption){
                    "Until Meetup"->{
                        rule.joinTime = Datatype("time",meetdate.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                    }
                    "Until confirm"->{
                        rule.joinTime = Datatype("until_confirm","until_confirm")
                    }
                    "24 hour"->{
                        today.add(Calendar.DATE, 1)
                        rule.joinTime = Datatype("time",today.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                    }
                    "2 Days"->{
                        today.add(Calendar.DATE, 2)
                        rule.joinTime = Datatype("time",today.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
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
                (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,4) as MeetUpDateNTimeBottomFragment?)?.getDataFromRulePage(rule)
                meetPager?.binding?.meetUpViewpager?.setCurrentItem(4,true)
            }
        }

//        binding.voteOption.setOnItemSelectedListener( object : AdapterView.OnItemSelectedListener{
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                Log.i(TAG," position:: ${position}")
//                when(position){
//                    0-> { rule.voteChnge = 1 }
//                    1->{ rule.voteChnge = 3 }
//                    2 -> { rule.voteChnge = 5}
//                    3 -> { rule.voteChnge  = 1000 }
//                }
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//            }
//        })

//        binding.joinOption.setOnItemSelectedListener( object : AdapterView.OnItemSelectedListener{
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//
//                joinTimeOption = position
//
//                Log.i(TAG," position:: ${position}")
//                /*when(position){
//                    0->{
//                        rule.joinTime = Datatype("time","untilMeetUp")
//                    }
//                    1->{
//                        rule.joinTime = Datatype("time","1")
//                    }
//                    2->{
//                        rule.joinTime = Datatype("time","2")
//                    }
//                    3->{
//                        rule.joinTime = Datatype("until_confirm","until_confirm")
//                    }
//
//                }*/
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//
//
//            }
//
//        })



    }

    private fun addObserver() {


    }
    private fun pickDate() {
        val dpd = DatePickerDialog.newInstance(
            this,
            meetdate.get(Calendar.YEAR),  // Initial year selection
                meetdate.get(Calendar.MONTH),  // Initial month selection
                meetdate.get(Calendar.DAY_OF_MONTH)
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
        if(!datePicked){
            showToast("Select a Date first")
            return
        }
        val tpd = TimePickerDialog.newInstance(this,
            meetHour,
            meetMinut,
            false)
        tpd.title = "Choose MeetUp Time"
        tpd.accentColor = ContextCompat.getColor(requireContext(),R.color.primaryDark)
        tpd.setOkColor(ContextCompat.getColor(requireContext(),R.color.primaryDark))
        tpd.setCancelColor(ContextCompat.getColor(requireContext(),R.color.gray))
        tpd.show(parentFragmentManager, "Datepickerdialog");
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
//        dateCalendar.add(Calendar.HOUR ,-12)

        meetdate = dateCalendar

        var sdfShow = SimpleDateFormat("E, dd MMM yyyy", Locale.UK)

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
        Log.i(TAG," checkingHour:: $hourOfDay :: $minute")
//        val time = "" + String.format("%02d",hourOfDay%12).plus(":").plus(String.format("%02d",minute)).plus(if(hourOfDay>11)" pm" else " am")

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
        //isTimeSelect = true
    }

    private fun setSpinner(hours: Long) {
        val stringArray = resources.getStringArray(R.array.join_time_private_meetup)
        val array:List<String>
        joinTimeOption="Until Meetup"
        if(hours<0){
            array=stringArray.take(0)
        }else if(hours<24){
            array=stringArray.take(2)
        }else if(hours<48){
            array=stringArray.take(3)
        }else{
            array=stringArray.toList()
        }
        val voteSelectAlert:VoteSelectAlert= VoteSelectAlert(requireActivity(),"Join Time",array){it,st->
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
        var duration : String?=null,
        var time : String? =null,
        var voteChnge : Int? = null,
        var joinTime : Datatype? = null,
    )

    data class Datatype(
        var type : String? = null,
        var value : String? = null
    )

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }


}