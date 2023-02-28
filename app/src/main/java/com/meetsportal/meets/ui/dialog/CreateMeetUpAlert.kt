package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.onClick

class CreateMeetUpAlert(var activity: Activity, var onYes: (GetMeetUpResponseItem?) -> Unit) {

    val TAG = CreateMeetUpAlert::class.simpleName

    var dialog: Dialog? = null
    var status: TextView? = null
    var cancel: ImageView? = null
    var button: TextView? = null
    var lotti: LottieAnimationView? = null

    lateinit var invitee: String

    fun showDialog(type: Constant.MeetType): Dialog? {
        dialog = Dialog(activity)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //...set cancelable false so that it's never get hidden
        dialog?.setCancelable(false)
        //...that's the layout i told you will inflate later
        dialog?.setContentView(R.layout.custom_create_meetup_alert_layout)
        dialog?.findViewById<TextView>(R.id.invitee)?.apply { text = invitee }
        status = dialog?.findViewById<TextView>(R.id.status)
        cancel = dialog?.findViewById<ImageView>(R.id.cancel)
        button = dialog?.findViewById<TextView>(R.id.seeMeetUp)
        lotti = dialog?.findViewById<LottieAnimationView>(R.id.logo)
        button?.background = Utils.getRoundedColorBackground(activity, R.color.gray4)

        when(type) {
            Constant.MeetType.OPEN -> {
                status?.text = "Creating Open meetup..."
                button?.text = "View Detail"
            }
        }

        lotti?.apply {
            setImageAssetsFolder("images/")
            setAnimation("meet_created.json")
        }
        dialog?.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()
        return dialog
    }


    fun setInvite(invitee: String) {
        this.invitee = invitee
    }

    fun changeStatus(isMeetupCreated: Boolean, meetResponse: GetMeetUpResponseItem? = null) {
        if(isMeetupCreated) {
            lotti?.playAnimation()
            button?.background = Utils.getRoundedColorBackground(activity, R.color.primaryDark)
            if(meetResponse?.type.equals("private")) status?.text = "Invitation Sent!"
            else status?.text = "Open Meet Created!"

            button?.onClick({
                onYes(meetResponse)
                hideDialog()
                //showMeetUps()
            })
            cancel?.visibility = View.VISIBLE
            cancel?.setOnClickListener {
                hideDialog()
                (activity as MainActivity?)?.popBackStack()
//                ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 1) as MeetUpNew).refreshThispage()
            }
        } else {
            button?.background = Utils.getRoundedColorBackground(activity, R.color.gray4)
            button?.text = "Retry"
            button?.setOnClickListener {
                Log.i(TAG, " retry");
                cancel?.visibility = View.INVISIBLE
                cancel?.setOnClickListener(null)
                hideDialog()
            }
            cancel?.visibility = View.VISIBLE
            cancel?.setOnClickListener { hideDialog() }
        }
    }

    fun hideDialog() {
        dialog?.let {
            if(it.isShowing) it.dismiss()
        }
    }

    /* fun showMeetUps(){
         ((activity as MainActivity).viewPageAdapter.instantiateItem((activity as MainActivity).viewPager,1) as MeetUpNew).getMeetUps()
         (activity as MainActivity).viewPager.setCurrentItem(1)
         hideDialog()
         activity.onBackPressed()
     }*/

}