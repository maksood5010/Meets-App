package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.Window
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.MeetInvitationPlaceAdapter
import com.meetsportal.meets.adapter.MeetPeopleAdapter
import com.meetsportal.meets.databinding.DialogMeetupDetailBinding
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.meetup.MeetJoinRequest
import com.meetsportal.meets.ui.fragments.socialfragment.MeetAttendeeList
import com.meetsportal.meets.utils.*
import java.util.*


/**TODO Remove
 * isJoin
 * Param :-  isJoin(accept:Boolean, invitationId : String, meetUpId: String?)
 */
class MeetUpInvitationAlert(var activity: Activity, var isJoin: (MeetJoinRequest) -> Unit) {

    private var _binding: DialogMeetupDetailBinding? = null
    private val binding get() = _binding!!
    val TAG = CreateMeetUpAlert::class.simpleName


    var dialog: Dialog? = null


    lateinit var invitee: String
    var isOpen: Boolean = false
    var postId: String? = null
    var invitaionId: String? = null
    var meetUpId: String? = null
    var inviteeName: String? = null

    init {

        dialog = Dialog(activity, R.style.BottomSheetDialog)
        _binding = DialogMeetupDetailBinding.inflate(LayoutInflater.from(activity))
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setContentView(binding.root)
        //...set cancelable false so that it's never get hidden

        dialog?.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.setCancelable(true)
    }

    fun showDialog(invitationId: String?, meetUpId: String?, inviteeName: String?): Dialog? {
        this.invitaionId = invitationId
        this.meetUpId = meetUpId
        this.inviteeName = inviteeName
        binding.switcher?.displayedChild = 2
        binding.tvPlaces.isVisible = true
        binding.llPlaces.isVisible = true
        binding.llBefore.isVisible = true
        binding.tvNo.isVisible = true
        binding.llBefore.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Constant.Safety))
            activity.startActivity(browserIntent)
        }
        binding.tvJoin.isVisible = true
        dialog?.show()
        return dialog
    }

    fun setOpenMeet(post_id: String?) {
        isOpen = true
        postId = post_id
        binding.tvVoteChange.isVisible = false
        binding.tvTitle.text = "Open Meetup details"
        binding.tvPlaces.text = "Place of Meet"
        binding.status.text = "Place of Meet"
    }

    fun showData(data: GetMeetUpResponseItem?) {
        binding.switcher?.displayedChild = 1
        binding.name.text = data?.name
        if(!isOpen) {
            binding.tvTitle.text = "Invitation to join meetup"
        } else {
            binding.tvTitle.text = "Open Meetup details"
        }
        val max = if(data?.max_vote_changes?.compareTo(100) == 1) "Unlimited" else "${data?.max_vote_changes}"
        val votes = SpannableStringBuilder("Vote change: $max")
        votes.setSpan(StyleSpan(Typeface.BOLD), 0, votes.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvVoteChange.text = votes
        var temp: String? = ""
        when {
            data?.max_join_time?.type.equals(Constant.JoinTimeType.TIME.type)         -> {
                temp = data?.max_join_time?.value?.toDate()?.formatTo("MMM, dd hh:mm a")
            }

            data?.max_join_time?.type.equals(Constant.JoinTimeType.TILL_CONFIRM.type) -> {
                temp = "Confirm"
            }

            data?.max_join_time?.type.equals(Constant.JoinTimeType.TILL_MEETUP.type)  -> {
                temp = "Meetup"
            }
        }
        val time = SpannableStringBuilder("Join till: $temp")
        time.setSpan(StyleSpan(Typeface.BOLD), 0, time.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvJoinTime.text = time
        temp = ""
        data?.date?.toDate()?.let { it1 ->
            data.duration?.toDate()?.let { d ->
                if(d.equals(it1)) {
                    temp = "Full Day"
                } else {
                    val hours = getHourDiff(d, it1)
                    temp = "${hours}hrs"
                }
            } ?: run {
                temp = "N/A"
            }
        }
        time.clear()
        time.append("Duration: $temp")
        time.setSpan(StyleSpan(Typeface.BOLD), 0, time.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvDuration.text = time

        binding.status?.text = (inviteeName ?: data?.user_meta?.username)?.plus(", has invited you to join a Meetup. Would You like to join now?")
        binding.date?.text = data?.date?.toDate()?.formatTo("dd MMM yyyy")
        binding.time?.text = data?.date?.toDate()?.formatTo("hh:mm a")
        binding.rvPlaces?.layoutManager = FlexboxLayoutManager(activity).apply {
            flexDirection = FlexDirection.ROW;
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }
        binding.rvPlaces?.adapter = MeetInvitationPlaceAdapter(activity, data?.places, data?.custom_places)
        binding.rvInvitee?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvInvitee?.adapter = MeetPeopleAdapter(activity, data?.invitees?.take(4), data) {
            dialog?.dismiss()
            var fragment = MeetAttendeeList.getInstance(data?._id, Constant.EnumMeetPerson.INVITEE)
            Navigation.addFragment(activity, fragment, MeetAttendeeList.TAG, R.id.homeFram, true, false)
        }
        binding.tvPeoples.text = "${data?.invitees?.size}+ People"
        Utils.onClick(binding.tvJoin, 3000) {
            isJoin(MeetJoinRequest(accept = true, invitationId = invitaionId, meetUpId = meetUpId, isOpen = isOpen, postId = postId))
        }
        if(data?.description.isNullOrEmpty()) {
            binding.llDesc.isVisible = false
            binding.tvDesc.isVisible = false
        } else {
            binding.tvDescription.text = data?.description
        }
        binding.cancel.setOnClickListener {
            dialog?.dismiss()
        }
        binding.tvNo?.setOnClickListener { isJoin(MeetJoinRequest(accept = false, invitationId = invitaionId, meetUpId = meetUpId, isOpen = isOpen, postId = postId)) }

    }

    private fun getHourDiff(start: Date, end: Date): Long {
        val seconds = (start.time - end.time) / 1000
        val minutes = seconds / 60
        var hours = minutes / 60
        return hours
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