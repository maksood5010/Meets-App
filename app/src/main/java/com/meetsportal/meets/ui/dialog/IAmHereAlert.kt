package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.meetsportal.meets.R

class IAmHereAlert(var activity: Activity) {

    var dialog: Dialog? = null
    var title: TextView? = null
    var desc: TextView? = null
    var lotti: LottieAnimationView? = null

    fun showDialog(): Dialog? {
        dialog = Dialog(activity)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //...set cancelable false so that it's never get hidden
        dialog?.setCancelable(true)
        //...that's the layout i told you will inflate later
        dialog?.setContentView(R.layout.alert_iam_here_layout)

        title = dialog?.findViewById<TextView>(R.id.head)?.apply {
            text = "Fetching Location!!!"
        }
        desc = dialog?.findViewById<TextView>(R.id.desc)?.apply {
            text = "This will take just a few seconds."
        }

        lotti = dialog?.findViewById<LottieAnimationView>(R.id.logo)?.apply {
            setAnimation("fetching_location.json")
        }

        dialog?.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()
        return dialog
    }

    fun changeStatus(status: Int, distance: Int? = null) {
        var text = "Something went wrong"
        var desc = "Please try again later"
        when(status) {
            0    -> {
                text = "Validating Location"
                desc = "Please wait…"
                lotti?.setAnimation("fetching_location.json")
                lotti?.playAnimation()
            }

            1    -> {
                text = "Yes! you are here "
                desc = "Enjoy your Meetup"
                lotti?.setAnimation("imhere.json")
                lotti?.playAnimation()
            }

            2    -> {
                text = "You are too far away!"
                lotti?.setAnimation("faraway.json")
                distance?.let {
                    desc = "You are ${distance}meter away from meetup place!!"
                } ?: run {
                    desc = "You are too far away from meetup place!!"
                }
                lotti?.playAnimation()
            }

            else -> {
                text = "Something went wrong"
                desc = "Please try again later"
                lotti?.setImageResource(R.drawable.bg_too_far)
            }
        }
        this.desc?.text = desc
        title?.text = text
    }

    //    fun changeStatus(text: String, desc: String? = null, showAnim: Boolean? = null) {
//        title?.text = text
//        desc?.let {
//            this.desc?.text = it
//        }
//        if(showAnim == true) lotti?.playAnimation()
//    }
//
    fun hideDialog() {
        dialog?.let {
            if(it.isShowing) it.dismiss()
        }
    }
}