package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment.MeetType
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.onClick

class CreateMeetOptionAlert(var activity: Activity, var crateMeetUp: (MeetType) -> Unit) : Dialog(activity) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //...set cancelable false so that it's never get hidden
        setCancelable(true)
        //...that's the layout i told you will inflate later
        setContentView(R.layout.create_meet_option_alert)
        getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun showDialog() {
        //dialog = Dialog(activity)


        findViewById<ImageView>(R.id.cancel)?.apply {
            setOnClickListener {
                hideDialog()
            }
        }

        findViewById<TextView>(R.id.tvPrivate)?.apply {
            background = Utils.getRoundedColorBackground(activity, R.color.primaryDark)
            onClick({
                MyApplication.putTrackMP(Constant.AcCreatePrivateMeet, null)
                crateMeetUp(MeetType.PRIVATE)
                hideDialog()
            })
        }

        findViewById<TextView>(R.id.tvOpen)?.apply {
            background = Utils.getRoundedColorBackground(activity, R.color.primaryDark)
            onClick({
                MyApplication.putTrackMP(Constant.AcCreatePrivateMeet, null)
                crateMeetUp(MeetType.OPEN)
                hideDialog()
            })
        }

        show()
    }

    fun hideDialog() {
        //dialog?.let {
        if(isShowing) dismiss()
        //}
    }
}