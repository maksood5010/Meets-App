package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.util.Log
import android.view.Window
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.places.GetSinglePlaceResponse

class CheckInUserAlert(var activity: Activity, var onYes: (sid: String?, isShareble: Boolean?) -> Unit) {

    var dialog: Dialog? = null

    fun showDialog(value: GetSinglePlaceResponse?): Dialog? {
        dialog = Dialog(activity)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //...set cancelable false so that it's never get hidden
        dialog?.setCancelable(false)
        //...that's the layout i told you will inflate later
        dialog?.setContentView(R.layout.cutom_check_in_alert_layout)

        dialog?.findViewById<TextView>(R.id.tv_placename)?.apply {
            text = value?.name?.en
        }
        dialog?.findViewById<LottieAnimationView>(R.id.logo)?.apply {
            setImageAssetsFolder("images/")
            setAnimation("checkinanim2.json")
            playAnimation()

        }

        dialog?.findViewById<ImageView>(R.id.cancel)?.apply {
            setOnClickListener {
                hideDialog()
            }
        }

        dialog?.findViewById<TextView>(R.id.no)?.apply {
            setOnClickListener {
                hideDialog()
            }
        }

        dialog?.findViewById<TextView>(R.id.yes)?.apply {
            setOnClickListener {
                dialog?.findViewById<CheckBox>(R.id.share).apply {
                    Log.i("TAG", " checking ChackBox ${this?.isChecked}")
                    onYes(null, this?.isChecked)
                    hideDialog()
                }
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