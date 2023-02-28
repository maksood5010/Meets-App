package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Window
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.meetup.MeetPerson

class MeetConfirmAlert(var activity: Activity?) {

    var dialog: Dialog? = null

    fun showDialog(participants: List<MeetPerson>): Dialog? {
        if(activity == null) return null
        dialog = Dialog(activity!!)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //...set cancelable false so that it's never get hidden
        dialog?.setCancelable(true)
        //...that's the layout i told you will inflate later
        dialog?.setContentView(R.layout.alert_meet_confirm)


        dialog?.findViewById<LottieAnimationView>(R.id.logo)?.apply {
            //setImageAssetsFolder("images/")
            setAnimation("meet_confirm.json")
            Log.i(MeetConfirmAlert::class.simpleName, " SpeedAnim:: $speed")
            playAnimation()
        }

        dialog?.findViewById<TextView>(R.id.tv_participant).apply {
            val map = participants.filter { it.sid != MyApplication.SID }.take(3)
                    .map { it.username }
            if(map.isNotEmpty()) {
                var text = "Your Meetup with ".plus(map.joinToString(","))
                        .plus("... Has been confirmed")
                var spannable = SpannableString(text)
                spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(activity!!, R.color.primaryDark)), text.lastIndexOf("with")
                        .plus(4), text.lastIndexOf("..."), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                spannable.setSpan(StyleSpan(Typeface.BOLD), text.lastIndexOf("with")
                        .plus(4), text.lastIndexOf("..."), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                this?.setText(spannable, TextView.BufferType.SPANNABLE)
            } else {
                this?.setText("Your Meetup has confirmed…")
            }
        }



        dialog?.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()
        return dialog
    }

    fun hideDialog() {
        dialog?.let {
            if(it.isShowing) it.dismiss()
        }
    }

}