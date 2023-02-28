package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateUtils
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.MeetPeopleAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.DialogMeetupDetailBinding
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.ui.fragments.socialfragment.MeetAttendeeList
import com.meetsportal.meets.utils.*
import java.util.*


/**
 * Param :-  isJoin(accept:Boolean, invitationId : String, meetUpId: String?)
 */
class MeetUpDetailDialog(var activity: Activity, var isJoin: (Boolean, String?, String?) -> Unit) {

    private var _binding: DialogMeetupDetailBinding? = null
    private val binding get() = _binding!!
    val TAG = CreateMeetUpAlert::class.simpleName


    var dialog: Dialog? = null


    lateinit var invitee: String

    init {

        dialog = Dialog(activity, R.style.BottomSheetDialog)
        _binding = DialogMeetupDetailBinding.inflate(LayoutInflater.from(activity))
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(binding.root)
        //...set cancelable false so that it's never get hidden

        dialog?.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.setCancelable(true)
    }

    fun showDialog(data: GetMeetUpResponseItem?): Dialog? {
        //...that's the layout i told you will inflate later
        //dialog?.setContentView(R.layout.dialog_meetup_detail)
        Log.d(TAG, "showDialog: ")

        binding.tvPlaces.isVisible = false
        binding.llPlaces.isVisible = false
        binding.llBefore.isVisible = false
        binding.tvNo.isVisible = false
        binding.tvJoin.isVisible = false
        // showData(data)
        dialog?.show()
        return dialog
    }

    fun showData(data: GetMeetUpResponseItem?) {
        binding.switcher?.displayedChild = 1
        binding.tvJoin.isVisible = false
        binding.tvNo.isVisible = false
        binding.tvPlaces.isVisible = false
        binding.llPlaces.isVisible = false
        binding.name?.text = data?.name
        binding.status?.text = "This meetup was created by ${if(data?.user_meta?.sid == MyApplication.SID) "you" else data?.user_meta?.username},"
        binding.date?.text = data?.date?.toDate()?.formatTo("dd MMM yyyy")
        binding.time?.text = data?.date?.toDate()?.formatTo("hh:mm a")
        data?.date?.toDate()?.let { it1 ->
            data.duration?.toDate()?.let { d ->
                if(d.equals(it1)) {
                    binding.tvDuration.text = "Duration: Full Day"
                } else {
                    val hours = Utils.getHourDiff(d, it1)
                    binding.tvDuration.text = "Duration: ${hours}hrs"
                }
            } ?: run {
                binding.tvDuration.text = "N/A"
            }
        }
        var temp1: String? = ""
        val time1 = SpannableStringBuilder("Join till: $temp1")
        data?.date?.toDate()?.let { it1 ->
            data.duration?.toDate()?.let { d ->
                if(d.equals(it1)) {
                    temp1 = "Full Day"
                } else {
                    val hours = Utils.getHourDiff(d, it1)
                    temp1 = "${hours}hrs"
                }
            } ?: run {
                temp1 = "N/A"
            }
        }
        time1.clear()
        time1.append("Duration: $temp1")
        time1.setSpan(StyleSpan(Typeface.BOLD), 0, time1.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvDuration.text = time1
//       binding.rvPlaces?.layoutManager = FlexboxLayoutManager(activity).apply {
//           flexDirection = FlexDirection.ROW;
//           justifyContent = JustifyContent.FLEX_START
//           alignItems = AlignItems.FLEX_START
//       }
//       binding.rvPlaces?.adapter = MeetInvitationPlaceAdapter(activity,data?.places)
        binding.rvInvitee?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        var take = data?.participants?.take(4)
        var type = Constant.EnumMeetPerson.PARTICIPANT
        var title = "Also Invited"
        if(DateUtils.isToday(data?.date?.toDate()?.time ?: 0L)) {
            //today
//            type = Constant.EnumMeetPerson.PARTICIPANT
//            take = data?.participants?.take(4)
//            title = "Participants"
            type = Constant.EnumMeetPerson.ATTENDEE
            take = data?.attendees?.take(4)
            title = "Attended by"
        } else if(data?.date?.toDate()?.before(Date()) == true) {
            //Previous
            type = Constant.EnumMeetPerson.ATTENDEE
            take = data?.attendees?.take(4)
            title = "Attended by"
        } else {
            //upcoming
            type = Constant.EnumMeetPerson.INVITEE
            take = data?.invitees?.take(4)
            title = "Also Invited"
        }
        binding.tvHeading.text = title
        binding.rvInvitee?.adapter = MeetPeopleAdapter(activity, take, data) {
            dialog?.dismiss()
            var fragment = MeetAttendeeList.getInstance(data?._id, type)
            Navigation.addFragment(activity, fragment, MeetAttendeeList.TAG, R.id.homeFram, true, false)
        }
        binding.tvPeoples.text = "${take?.size}+ People"

        binding.cancel.setOnClickListener {
            dialog?.dismiss()
        }
        val votes = SpannableStringBuilder("Vote change: ${if(data?.max_vote_changes?.compareTo(100) == 1) "Unlimited" else data?.max_vote_changes}")
        votes.setSpan(StyleSpan(Typeface.BOLD), 0, votes.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvVoteChange.text = votes
        var temp: String? = ""
        if(data?.max_join_time?.type.equals(Constant.JoinTimeType.TIME.type)) {
            temp = data?.max_join_time?.value?.toDate()?.formatTo("MMM, dd hh:mm a")
        } else if(data?.max_join_time?.type.equals(Constant.JoinTimeType.TILL_CONFIRM.type)) {
            temp = "Confirm"
        } else if(data?.max_join_time?.type.equals(Constant.JoinTimeType.TILL_MEETUP.type)) {
            temp = "Meetup"
        }
        val time = SpannableStringBuilder("Join till: $temp")
        time.setSpan(StyleSpan(Typeface.BOLD), 0, time.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvJoinTime.text = time
        if(data?.description.isNullOrEmpty()) {
            binding.llDesc.isVisible = false
            binding.tvDesc.isVisible = false
        } else {
            binding.tvDescription.text = data?.description
        }
//       Utils.onClick(tvJoin!!,1000){
//           isJoin(true,invitaionId,meetUpId)
//       }
//       tvNo?.setOnClickListener {  isJoin(false,invitaionId,meetUpId) }

    }


    fun hideDialog() {
        dialog?.let {
            if(it.isShowing) it.dismiss()
        }
    }

    fun showMeetUps() {
//        ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 1) as MeetUpNew).getMeetUps()
//        (activity as MainActivity?)?.viewPager?.setCurrentItem(1)
        hideDialog()
        activity.onBackPressed()
    }

}