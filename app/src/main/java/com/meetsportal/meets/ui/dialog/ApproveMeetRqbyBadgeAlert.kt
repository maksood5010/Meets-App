package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.util.Log
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.AllowOpenMeetRqAdapter
import com.meetsportal.meets.models.MeetRqCountByBadge
import com.meetsportal.meets.networking.profile.Badge
import com.meetsportal.meets.utils.Constant.badgeList
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground

class ApproveMeetRqbyBadgeAlert(var list: (List<String>) -> Unit) {

    val TAG = ApproveMeetRqbyBadgeAlert::class.simpleName

    var dialog: Dialog? = null
    var badgelist: ArrayList<Badge> = ArrayList()
    var countByBadge: MeetRqCountByBadge? = null


    fun showDialog(activity: Activity): Dialog? {

        dialog = Dialog(activity, R.style.BottomSheetDialog)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //...set cancelable false so that it's never get hidden
        dialog?.setCancelable(false)
        //...that's the layout i told you will inflate later
        dialog?.setContentView(R.layout.custom_allow_meet_bybadge_alert_layout)

        badgelist.clear()
        badgelist.addAll(badgeList.map { it.apply { selected = false; count = 0 } })

        var allow = dialog?.findViewById<TextView>(R.id.Allow)?.apply {
            setRoundedColorBackground(activity, R.color.gray1)
        }

        dialog?.findViewById<RecyclerView>(R.id.badgeList)?.apply {
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            adapter = AllowOpenMeetRqAdapter(activity, badgelist, countByBadge) {
                Log.i(TAG, " selected:: ${badgelist.filter { it.selected == true }}")
                Log.i(TAG, " selectedCount::: ${
                    badgelist.filter { it.selected == true }.sumBy { it?.count ?: 0 }
                }")
                var total = badgelist.filter { it.selected == true }.sumBy { it?.count ?: 0 }
                if(total > 0) {
                    allow?.text = "Allow $total People"
                    allow?.setRoundedColorBackground(activity, R.color.primaryDark)
                    allow?.onClick({
                        var list = badgelist.filter { it.selected == true }.map { it.key }
                        list(list)
                        hideDialog()
                    })

                } else {
                    allow?.text = "Allow"
                    allow?.setRoundedColorBackground(activity, R.color.gray1)
                    allow?.onClick({})

                }
            }
        }



        dialog?.findViewById<ImageView>(R.id.cancel)?.apply {
            setOnClickListener {
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

    fun setCount(it: MeetRqCountByBadge?) {
        countByBadge = it
    }
}