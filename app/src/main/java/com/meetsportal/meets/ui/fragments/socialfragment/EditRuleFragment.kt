package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetupRulesBottomBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.ui.dialog.VoteSelectAlert
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

@AndroidEntryPoint
class EditRuleFragment : BaseFragment() {

    var rule = MeetUpRulesBottomFragment.Rules()
    val meetUpViewModel: MeetUpViewModel by viewModels()
    var meetDurationOption: String? = "1 hour"
    var joinTimeOption: String = "Until meet up"

    val UNTIL_MEET = "Until meet up"
    val UNTIL_CONFIRM = "Until confirm"

    private var _binding: FragmentMeetupRulesBottomBinding? = null
    val binding get() = _binding!!

    companion object {

        val TAG = EditRuleFragment::class.simpleName!!
        val MEET_ID = "meetData"
        val FB_CHAT_ID = "fbChatId"
        val CALLING_TAG = "callingTag"
        fun getInstance(TAG: String?, meetId: String?, fbChatId: String?): EditRuleFragment {
            return EditRuleFragment().apply {
                arguments = Bundle().apply {
                    putString(MEET_ID, meetId)
                    putString(CALLING_TAG, TAG)
                    putString(FB_CHAT_ID, fbChatId)
                }
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_viewpager, container, false)
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
        binding.tvBack.setRoundedColorBackground(requireActivity(), R.color.dark_transparent)
        binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(), R.color.primaryDark)
        binding.addFriendOrNext.text = "Save"
        binding.etMeetName.count {
            binding.tvMeetname.setText(binding.etMeetName.text.toString().trim())
            rule.name = binding.etMeetName.text.toString().trim()
        }
        binding.etMeetName.setRoundedColorBackground(requireActivity(), enbleDash = true, strokeColor = R.color.black, Dashgap = 0f, stripSize = 20f, strokeHeight = 1f, color = R.color.page_bg)
        binding.llDateTime.setRoundedColorBackground(requireActivity(), enbleDash = true, strokeColor = R.color.black, Dashgap = 0f, stripSize = 20f, strokeHeight = 1f, color = R.color.gray1)
        binding.rlDuration.setRoundedColorBackground(requireActivity(), enbleDash = true, strokeColor = R.color.black, Dashgap = 0f, stripSize = 20f, strokeHeight = 1f, color = R.color.page_bg)
        binding.rlVoteChange.setRoundedColorBackground(requireActivity(), enbleDash = true, strokeColor = R.color.black, Dashgap = 0f, stripSize = 20f, strokeHeight = 1f, color = R.color.page_bg)
        binding.rljoinTime.setRoundedColorBackground(requireActivity(), enbleDash = true, strokeColor = R.color.black, Dashgap = 0f, stripSize = 20f, strokeHeight = 1f, color = R.color.page_bg)
        binding.tvLine.setRoundedColorBackground(requireActivity(), enbleDash = true, strokeColor = R.color.black, Dashgap = 0f, stripSize = 20f, strokeHeight = 1f, color = R.color.transparent)
        binding.tvMeetname.setOnClickListener {
            binding.etMeetName.requestFocus()
            val inputMethodManager = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            inputMethodManager?.showSoftInput(binding.etMeetName, InputMethodManager.SHOW_IMPLICIT)
        }

    }

    private fun setUp() {
        arguments?.getString(MEET_ID)?.let {
            meetUpViewModel.getMeetUpDetail(it)
        } ?: run {
            MyApplication.showToast(requireActivity(), "Something went wrong")
            return
        }
    }

    private fun populateData(value: GetMeetUpResponseItem?) {
        binding.tvMeetname.setText(value?.name)
        binding.etMeetName.setText(value?.name)
        binding.selectedDate.text = value?.date?.toDate()?.formatTo("E, dd MMM yyyy")
        binding.selctedTime.text = value?.date?.toDate()?.formatTo("h:mm a")
        binding.etDesc.setText(value?.description)
        joinTimeOption = getJoinTime(value)
        binding.joinOption.text = joinTimeOption
        setJoinTimeAdapter(value)
        meetDurationOption = getDuration(value)
        binding.durationOption.text = meetDurationOption
        setDurationAdapter(value)
        setVoteChangeAdapter(value)


        binding.addFriendOrNext.onClick({
            validateAndEditRule()
        })
    }

    private fun getJoinTime(value: GetMeetUpResponseItem?): String {
        Log.i(TAG, " comparing:: myjoin--${value?.max_join_time?.value}--myMeetdate--${value?.date}--")
        if(value?.max_join_time?.type.equals("until_confirm")) {
            return UNTIL_CONFIRM
        } else if(value?.max_join_time?.type.equals("time")) {
            if(value?.max_join_time?.value?.equals(value?.date) == true) {
                Log.i(TAG, " came::::: 0")
                return UNTIL_MEET
            } else {
                Log.i(TAG, " came::::: 1")
                return "Same as Previous (".plus(value?.max_join_time?.value?.toDate()
                        ?.formatTo("MMM, dd hh:mm a")).plus(")")
            }
        } else {
            return ""
        }
    }

    private fun setJoinTimeAdapter(value: GetMeetUpResponseItem?) {
        var meetdate = Calendar.getInstance()
        meetdate.time = value?.date?.toDate()
        val milliDiff = meetdate.time.time - Date().time
        val hours = TimeUnit.HOURS.convert(milliDiff, TimeUnit.MILLISECONDS)

        val stringArray = resources.getStringArray(R.array.join_time_private_meetup)
        val array: ArrayList<String>
        if(hours < 0) {
            array = ArrayList(stringArray.take(0))
        } else if(hours < 24) {
            array = ArrayList(stringArray.take(2))
        } else if(hours < 48) {
            array = ArrayList(stringArray.take(3))
        } else {
            array = ArrayList(stringArray.toList())
        }
        var position = 0
        var temp = joinTimeOption
        when(joinTimeOption) {
            UNTIL_MEET    -> position = 0
            UNTIL_CONFIRM -> position = 1

            else          -> {
                array.add(joinTimeOption)
                position = array.size.minus(1)
            }
        }

        binding.joinOption.text = joinTimeOption


        Log.i(TAG, " currentPosition:: 0 $position")
        val voteSelectAlert = VoteSelectAlert(requireActivity(), "Join Time", array) { it, st ->
            if(it != position) {
                joinTimeOption = st
                binding.joinOption.text = st
                var today = Calendar.getInstance().apply { time = Date() }
                when(joinTimeOption) {
                    "Until meet up" -> {
                        rule.joinTime = MeetUpRulesBottomFragment.Datatype("time", meetdate.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                    }

                    "Until confirm" -> {
                        rule.joinTime = MeetUpRulesBottomFragment.Datatype("until_confirm", "until_confirm")
                    }

                    "24 hour"       -> {
                        today.add(Calendar.DATE, 1)
                        rule.joinTime = MeetUpRulesBottomFragment.Datatype("time", today.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                    }

                    "2 Days"        -> {
                        today.add(Calendar.DATE, 2)
                        rule.joinTime = MeetUpRulesBottomFragment.Datatype("time", today.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC")))
                    }
                }
            } else {
                joinTimeOption = temp
                binding.joinOption.text = temp
                rule.joinTime = null
            }
        }.apply {
            currentSelected = position
            initView()
        }



        binding.rljoinTime.onClick({
            voteSelectAlert.showAlert()
        })

    }

    private fun getDuration(value: GetMeetUpResponseItem?): String {
        var hour = value?.duration?.toDate()?.time?.minus(value.date.toDate()?.time ?: 0L)
                ?.div(1000)?.div(60)?.div(60)
        Log.i(TAG, " checkingHour::: ${hour}")
        return hour.toString().plus(" hour")
    }

    private fun setDurationAdapter(value: GetMeetUpResponseItem?) {
        val openArray: Array<String> = resources.getStringArray(R.array.open_meet_duration_options)
        val openDurationAlert: VoteSelectAlert = VoteSelectAlert(requireActivity(), "Duration", openArray.toList()) { it, st ->
            meetDurationOption = st
            binding.durationOption.text = st
            if(meetDurationOption?.contains("hour") == true) {
                val meetDuration = Calendar.getInstance().apply { time = value?.date?.toDate() }
                when(meetDurationOption) {
                    "1 hour"  -> {
                        meetDuration.add(Calendar.HOUR_OF_DAY, 1)
                        rule.duration = meetDuration.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
                    }

                    "3 hour"  -> {
                        meetDuration.add(Calendar.HOUR_OF_DAY, 3)
                        rule.duration = meetDuration.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
                    }

                    "7 hour"  -> {
                        meetDuration.add(Calendar.HOUR_OF_DAY, 7)
                        rule.duration = meetDuration.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
                    }

                    "24 hour" -> {
                        meetDuration.add(Calendar.HOUR_OF_DAY, 24)
                        rule.duration = meetDuration.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))
                    }
                }
            } else {
                rule.duration = null
            }
        }.apply {
            openArray.indexOfFirst { it.equals(meetDurationOption) }
            currentSelected = openArray.indexOfFirst { it.equals(meetDurationOption) }
            initView()
        }
        binding.rlDuration.onClick({
            openDurationAlert.showAlert()
        })
    }

    private fun setVoteChangeAdapter(value: GetMeetUpResponseItem?) {
        binding.voteOption.text = "No changes"
        val stringArray: Array<String> = resources.getStringArray(R.array.vote_freq)
        var tempPosition = stringArray.size
        var finalList = ArrayList(stringArray.toList()).apply { add("No changes") }
        val voteSelectAlert: VoteSelectAlert = VoteSelectAlert(requireActivity(), "Vote Change", finalList) { it, st ->
            if(it == tempPosition) {
                rule.voteChnge = null
            } else {
                when(it) {
                    0 -> {
                        rule.voteChnge = 1
                    }

                    1 -> {
                        rule.voteChnge = 3
                    }

                    2 -> {
                        rule.voteChnge = 5
                    }

                    3 -> {
                        rule.voteChnge = 1000
                    }
                }
            }

            binding.voteOption.text = st
        }.apply {
            currentSelected = tempPosition
            initView()
        }
        binding.rlVoteChange.onClick({
            voteSelectAlert.showAlert()
        })
    }

    private fun validateAndEditRule() {
        validateData()?.let {
            MyApplication.showToast(requireActivity(), it)
        } ?: run {
            rule.desc = binding.etDesc.text.toString().trim()
            rule.name = binding.etMeetName.text.toString().trim()
            meetUpViewModel.updateMeetUp(arguments?.getString(MEET_ID), arguments?.getString(FB_CHAT_ID), rule)
        }
    }

    private fun validateData(): String? {
        if(binding.etMeetName.text.toString().trim().isEmpty()) {
            return "Meet name should not be empty!!!"
        }
        return null
    }


    private fun addObserver() {
        meetUpViewModel.observeMeetUpDetail().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    populateData(it.value)
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong..")
                }
            }
        })
        meetUpViewModel.observeUpdateMeetUp().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    Log.i(TAG, " updateMeetUp::: SuccessFull")
                    MyApplication.showToast(requireActivity(), "Rule updated")
                    setFragmentResult(arguments?.getString(CALLING_TAG) ?: "", Bundle().apply {
                        putBoolean("refresh", true)
                    })
                    parentFragmentManager.popBackStack()
                }

                is ResultHandler.Failure -> {
                    Log.i(TAG, " updateMeetUp::: Failuers")
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome() {
        ////TODO("Not yet implemented")
    }
}