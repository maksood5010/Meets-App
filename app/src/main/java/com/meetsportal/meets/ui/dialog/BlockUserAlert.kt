package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.profile.FollowerFollowingResponseItem

class BlockUserAlert(var activity: Activity, var onYes: (sid: String?) -> Unit) {

    var dialog: Dialog? = null

    fun showDialog(item: FollowerFollowingResponseItem?): Dialog? {
        dialog = Dialog(activity)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //...set cancelable false so that it's never get hidden
        dialog?.setCancelable(false)
        //...that's the layout i told you will inflate later
        dialog?.setContentView(R.layout.cutom_block_alert_layout)

        dialog?.findViewById<TextView>(R.id.head)?.apply {
            text = "Block ".plus(item?.user_meta?.username)
        }

        dialog?.findViewById<TextView>(R.id.username)?.apply {
            text = item?.user_meta?.username.plus("?")
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
                onYes(item?.sid)
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