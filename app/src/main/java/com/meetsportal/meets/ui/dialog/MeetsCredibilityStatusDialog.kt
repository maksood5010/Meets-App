package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.MeetsCredibilityStatusAdapter
import com.meetsportal.meets.networking.profile.Badge
import com.meetsportal.meets.networking.profile.FollowerFollowingResponse


class MeetsCredibilityStatusDialog(var activity: Activity, val isMyProfile: Boolean, var onApiCall: (name: String, index: Int) -> Unit) {


    var dialog: Dialog? = null
    lateinit var rvMeetsCredibilityStatus: RecyclerView
    lateinit var adapterMeetsCredibilityStatus: MeetsCredibilityStatusAdapter

    fun initDialog(badge: Badge, mints: String? = null): Dialog? {
        dialog = Dialog(activity, R.style.BottomSheetDialog)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //...set cancelable false so that it's never get hidden
//        dialog?.setCancelable(false)12000 1000
        //...that's the layout i told you will inflate later
        dialog?.setContentView(R.layout.meetup_credibility_status_dialog)

        rvMeetsCredibilityStatus = dialog?.findViewById(R.id.rv_meets_credibility_status)!!
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvMeetsCredibilityStatus.layoutManager = layoutManager
        // rvMeetsCredibilityStatus.setHasFixedSize(true)
        adapterMeetsCredibilityStatus = MeetsCredibilityStatusAdapter(activity, badge, mints, isMyProfile) { s, i, b ->
            if(b) {
                dialog?.dismiss()
            } else {
                onApiCall(s, i)
            }
        }
        if(isMyProfile) {
            val level = badge?.level ?: 1
            rvMeetsCredibilityStatus.scrollToPosition(level - 1)
        }
        val helper: SnapHelper = PagerSnapHelper()
        helper.attachToRecyclerView(rvMeetsCredibilityStatus)

        rvMeetsCredibilityStatus.adapter = adapterMeetsCredibilityStatus
        dialog?.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.getWindow()
                ?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
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
                hideDialog()
            }
        }

        dialog?.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
//        dialog?.show()
        return dialog
    }

    fun show() {
        dialog?.show()
    }

    fun hideDialog() {
        dialog?.let {
            if(it.isShowing) it.dismiss()
        }
    }

    public fun setData(map: Map<Int, FollowerFollowingResponse>) {
        adapterMeetsCredibilityStatus.setData(map)
    }


}