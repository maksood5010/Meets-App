package com.meetsportal.meets.utils

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.ui.dialog.MeetUpInvitationAlert
import com.meetsportal.meets.ui.dialog.ProceedDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.*
import com.meetsportal.meets.viewmodels.MeetUpViewModel

class HandleNotification(var activity: AppCompatActivity,var meetUpViewModel: MeetUpViewModel) {

    val TAG = HandleNotification::class.simpleName
    var meetUpInvitation: MeetUpInvitationAlert? = null


    fun checkIntent() {
        Log.i(TAG, " HomeActIntent:::  ${activity?.intent?.getStringExtra(Constant.NOTIFICATION_TYPE)}")
        var notificationType = activity?.intent?.getStringExtra(Constant.NOTIFICATION_TYPE)
        notificationType?.let {
            when(it){
                NotificationServiceExtension.NotificationTypes.POST_LIKED.type ->{
                    var baseFragment: BaseFragment = DetailPostFragment()
                    Navigation.setFragmentData(
                        baseFragment,
                        "post_id",
                        activity?.intent?.getStringExtra(Constant.ENTITY_ID)
                    )
                    Navigation.addFragment(
                        activity!!,
                        baseFragment,
                        Constant.TAG_DETAIL_POST_FRAGMENT,
                        R.id.homeFram,
                        true,
                        false
                    )
                }
                NotificationServiceExtension.NotificationTypes.FOLLOW.type->{
                    var baseFragment: BaseFragment = OtherProfileFragment.getInstance(activity?.intent?.getStringExtra(Constant.ENTITY_ID),Constant.Source.NOTIFICATION.sorce)
                    Log.i(TAG, " checkSid = ${activity?.intent?.getStringExtra(Constant.ENTITY_ID)}")
                    /*baseFragment = Navigation.setFragmentData(
                        baseFragment,
                        OtherProfileFragment.OTHER_USER_ID,
                        activity?.intent?.getStringExtra(Constant.ENTITY_ID)
                    )*/
                    Navigation.addFragment(
                        activity!!, baseFragment,
                        Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
                    )
                }
                NotificationServiceExtension.NotificationTypes.POST_COMMENT.type->{
                    var baseFragment: BaseFragment = DetailPostFragment()
                    //var parentsIds = intent.getStringExtra(PARENT_ID)?.split(",")
                    Log.i(TAG, " parentsIds:: ${activity?.intent?.getStringExtra(Constant.PARENT_ID)}")
                    Navigation.setFragmentData(
                        baseFragment,
                        "post_id",
                        activity?.intent?.getStringExtra(Constant.PARENT_ID)
                    )
                    Navigation.setFragmentData(
                        baseFragment,
                        "comment_id",
                        activity?.intent?.getStringExtra(Constant.ENTITY_ID)
                    )
                    Navigation.addFragment(
                        activity!!,
                        baseFragment,
                        Constant.TAG_DETAIL_POST_FRAGMENT,
                        R.id.homeFram,
                        true,
                        false
                    )
                }
                NotificationServiceExtension.NotificationTypes.COMMENT_LIKED.type ,  NotificationServiceExtension.NotificationTypes.REPLY_COMMENT.type->{
                    var baseFragment: BaseFragment = DetailPostFragment()
                    var parentsIds = activity?.intent?.getStringExtra(Constant.PARENT_ID)?.split(",")
                    Log.i(TAG, " parentsIds:: ${parentsIds}")
                    if (parentsIds?.get(1).equals(parentsIds?.get(0))) {
                        Navigation.setFragmentData(baseFragment, "post_id", parentsIds?.get(0))
                        Navigation.setFragmentData(
                            baseFragment,
                            "comment_id",
                            activity?.intent?.getStringExtra(Constant.ENTITY_ID)
                        )
                        Navigation.addFragment(
                            activity!!,
                            baseFragment,
                            Constant.TAG_DETAIL_POST_FRAGMENT,
                            R.id.homeFram,
                            true,
                            false
                        )
                    } else {
                        Navigation.setFragmentData(baseFragment, "post_id", parentsIds?.get(0))
                        Navigation.setFragmentData(baseFragment, "comment_id", parentsIds?.get(1))
                        Navigation.setFragmentData(
                            baseFragment,
                            "reply_comment_id",
                            activity?.intent?.getStringExtra(Constant.ENTITY_ID)
                        )
                        Navigation.addFragment(
                            activity!!,
                            baseFragment,
                            Constant.TAG_DETAIL_POST_FRAGMENT,
                            R.id.homeFram,
                            true,
                            false
                        )
                    }
                }
                NotificationServiceExtension.NotificationTypes.MEETUP_INVITATION.type->{

                    var mergedId = activity?.intent?.getStringExtra(Constant.PARENT_ID)?.split(",")
                    var meetId = mergedId?.getOrNull(0)
                    var inviteeName = mergedId?.getOrNull(1)
                    var invitationId =  activity?.intent?.getStringExtra(Constant.ENTITY_ID)
                    meetUpInvitation = MeetUpInvitationAlert(activity!!){ join->
                        if(join.accept==true){
                            meetUpViewModel.acceptInvitaion(join)
                        }else {
                        val proceedDialog = ProceedDialog(activity) { b: Boolean ->
                            if(b) {
                                meetUpViewModel.acceptInvitaion(join)
                            }
                        }
                        proceedDialog.setMessage("Reject", "Are you sure you want to reject this invitation?")
                    }
                    }
//                    meetUpInvitation?.setOpenMeet(join.postId)
                    meetUpInvitation?.showDialog(invitationId,meetId,inviteeName)
                    meetUpViewModel.getMeetUpDetail(meetId)
                }
                NotificationServiceExtension.NotificationTypes.IN_APP_MESSAGE.type ->{
                    var baseFragment: BaseFragment = OnenOneChat.getInstance(activity?.intent?.getStringExtra(Constant.PARENT_ID))
                    Navigation.addFragment(activity,baseFragment,
                        Constant.TAG_ONE_N_ONE_CHAT,R.id.homeFram,true,false)
                }
                NotificationServiceExtension.NotificationTypes.POSITIVE_RES.type ->{
                    var meetId = activity?.intent?.getStringExtra(Constant.PARENT_ID)
                    var fragment = MeetAttendeeList.getInstance(meetId,Constant.EnumMeetPerson.ATTENDEE)
                    Navigation.addFragment(activity,fragment,
                        MeetAttendeeList.TAG,R.id.homeFram,true,false)
                }
                NotificationServiceExtension.NotificationTypes.MEET_CHAT.type ,
                NotificationServiceExtension.NotificationTypes.MEETUP_INVITATION.type,
                NotificationServiceExtension.NotificationTypes.MEET_TIME.type ,
                NotificationServiceExtension.NotificationTypes.MEET_ARRIVAL.type ->{
                    var meetId =  activity?.intent?.getStringExtra(Constant.PARENT_ID)
                    openMeetChat(meetId)
                }
//                else ->{
//                    var baseFragment: BaseFragment = NotificationFragment()
//                    Navigation.addFragment(
//                            activity,
//                            baseFragment, Constant.TAG_NOTIFICATION_FRAGMENT,
//                            R.id.homeFram,
//                            true,
//                            false
//                                          )
//                }
            }
        }
        activity?.intent?.replaceExtras(null)
    }

    fun openMeetChat(meetId: String?) {
        Log.i(TAG, "openMeetChat: $meetId")
        meetId?.let {
            Log.i(TAG, "openMeetChat: came1")
            var baseFragment = MeetUpVotingFragment.getInstance(meetId)
            Navigation.addFragment(activity, baseFragment, MeetUpVotingFragment.TAG, R.id.homeFram, true, true)
        }?:run{
            Log.i(TAG, "openMeetChat: came2")
            Toast.makeText(activity,"Meet not found",Toast.LENGTH_LONG).show()
        }
    }

    fun notificationObserver(){
        meetUpViewModel.observeMeetUpDetail().observe(activity, {
            Log.i(TAG," meetDetailCame::: ")
            when(it){
                is ResultHandler.Success ->{
                    Log.i(TAG," meetDetail::: ${it.value}")
                    meetUpInvitation?.showData(it.value)
                }
                is ResultHandler.Failure -> {
                    meetUpInvitation?.hideDialog()
                    MyApplication.showToast(activity,"Something went wrong!!!")
                }
            }
        })

        meetUpViewModel.observeMeetUpInvitaion().observe(activity,{
            when(it){
                is ResultHandler.Success -> {
                    Log.i(TAG," Invitation Accepted::: ${it.value}")
                    meetUpInvitation?.hideDialog()
                    if(it.value.accept == true) {
                        MyApplication.showToast(activity, "Invitation Accepted!!!")
                        if(it.value.isOpen == true) {
                            it.value.postId?.let {
                                var baseFragment: BaseFragment = DetailPostFragment()
                                Navigation.setFragmentData(baseFragment, "post_id", it)
                                Navigation.addFragment(activity, baseFragment, Constant.TAG_DETAIL_POST_FRAGMENT, R.id.homeFram, true, false)
                            }
                        } else {
                            it.value.meetUpId?.let {
                                var baseFragment: BaseFragment = MeetUpVotingFragment.getInstance(it)
                                Navigation.addFragment(activity, baseFragment, MeetUpVotingFragment.TAG, R.id.homeFram, true, true)
                            }
                        }
                    } else {
                        MyApplication.showToast(activity, "Invitation Declined!!!")
                    }
                    // ((activity as MainActivity).viewPageAdapter.instantiateItem((activity as MainActivity).viewPager,1) as MeetUpNew).getMeetUps()

                }
                is ResultHandler.Failure ->{
                    meetUpInvitation?.hideDialog()
                    MyApplication.showToast(activity,"Something went wrong...")
                }
            }
        })
    }



}