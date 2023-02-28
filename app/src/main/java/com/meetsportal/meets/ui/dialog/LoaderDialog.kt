package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.Window
import android.widget.TextView
import com.meetsportal.meets.R


class LoaderDialog(var activity: Activity):Dialog(activity) {

    var percent: TextView? = null
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setContentView(R.layout.custom_loading_layout)
        percent = findViewById(R.id.percent)
        percent?.visibility = View.GONE
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun showDialog(): Dialog? {
        show()
        return this
    }


    fun setPercent(progress: String) {
        percent?.text = progress
    }

    fun showPercent() {
        percent?.visibility = View.VISIBLE
    }


    fun hideDialog() {
        if(isShowing) dismiss()
        percent?.visibility = View.GONE
    }
}