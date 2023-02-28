package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.text.InputFilter
import android.util.Log
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.utils.setRoundedColorBackground


class AddNameAlert(var activity: Activity, var name: (meetUpName: String) -> Unit) {


    val TAG = AddNameAlert::class.simpleName
    private val blockCharacterSet = "~#^|$%&*!@(){}:;<>?~`-_+=/'"
    private val filter = InputFilter { source, start, end, dest, dstart, dend ->
        if(source != null && blockCharacterSet.contains("" + source)) {
            ""
        } else null
    }

    var dialog: Dialog? = null
    var etName: EditText? = null

    fun showDialog(meetName: String?): Dialog? {
        dialog = Dialog(activity)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //...set cancelable false so that it's never get hidden
        dialog?.setCancelable(true)
        //...that's the layout i told you will inflate later
        dialog?.setContentView(R.layout.alert_add_name_layout)

        etName = dialog?.findViewById<EditText>(R.id.etName)
        etName?.setRoundedColorBackground(activity, enbleDash = true, Dashgap = 0f, strokeHeight = 1f, strokeColor = R.color.black)
        etName?.setText(meetName)
        //etName?.filters = InputFilter { filter  }
        etName?.filters = arrayOf<InputFilter>(filter)


        dialog?.findViewById<ImageView>(R.id.cancel)?.apply {
            setOnClickListener {
                hideDialog()
            }
        }

        dialog?.findViewById<TextView>(R.id.tvOk)?.setOnClickListener {
            if(validate()) {
                name(etName?.text?.trim().toString())
                hideDialog()
            } else {
                MyApplication.showToast(activity, "Name should not be empty!!!")
            }

        }

        dialog?.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.show()
        return dialog
    }

    private fun validate(): Boolean {
        Log.i(TAG, " checkingtext::${etName?.text?.trim().toString()}")
        return etName?.text?.trim().toString().isNotEmpty()
    }

    fun hideDialog() {
        dialog?.let {
            if(it.isShowing) it.dismiss()
        }
    }


}