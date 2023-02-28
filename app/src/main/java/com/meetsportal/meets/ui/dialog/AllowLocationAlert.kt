package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.utils.Constant

class AllowLocationAlert(var activity: Activity, var onYes: () -> Unit) {

    var dialog: Dialog? = null

    fun showDialog(): Dialog? {
        dialog = Dialog(activity)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //...set cancelable false so that it's never get hidden
        dialog?.setCancelable(true)
        //...that's the layout i told you will inflate later
        dialog?.setContentView(R.layout.custom_location_alert_layout)
        dialog?.findViewById<ImageView>(R.id.cancel)?.apply {
            setOnClickListener {
                hideDialog()
            }
        }
        dialog?.findViewById<TextView>(R.id.no)?.apply {
            setOnClickListener {
                MyApplication.putTrackMP(Constant.AcPlacesDenyLocation, null)
                hideDialog()
            }
        }

        dialog?.findViewById<TextView>(R.id.yes)?.apply {
            setOnClickListener {
                MyApplication.putTrackMP(Constant.AcPlacesAllowLocation, null)
                onYes()
                hideDialog()
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