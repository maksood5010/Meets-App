package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.InterestAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.profile.Definition
import com.meetsportal.meets.utils.onText

class InterestDialog(var activity: Activity, var listner: (ArrayList<Definition?>) -> Unit) {

    var dialog: Dialog? = null
    lateinit var recyclerView: RecyclerView
    lateinit var save: TextView
    lateinit var search: EditText
    lateinit var adapter: InterestAdapter
    fun showInterest(interest: List<Definition?>?, myInterest: List<Definition?>?) {
        MyApplication.smallVibrate()
        var selected: ArrayList<Definition?> = ArrayList()

        dialog = Dialog(activity)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //...set cancelable false so that it's never get hidden
        dialog?.setCancelable(false)
        //...that's the layout i told you will inflate later
        dialog?.setContentView(R.layout.dialog_interest)
        dialog?.findViewById<ImageView>(R.id.iv_cancel)?.setOnClickListener {
            hideInterest()
        }
        save = dialog?.findViewById(R.id.tv_save)!!
        search = dialog?.findViewById(R.id.et_search)!!



        recyclerView = dialog?.findViewById(R.id.rv_interest)!!


        var layoutManager = FlexboxLayoutManager(activity).apply {
            flexDirection = FlexDirection.ROW;
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }

        recyclerView.layoutManager = layoutManager
        adapter = InterestAdapter(activity, interest, selected, myInterest)
        recyclerView.adapter = adapter

        dialog?.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)

        search.onText { typed ->
            if(!typed.equals("")) {
                adapter.setItem(interest?.filter {
                    return@filter it?.en?.contains(typed, true)!!
                })
            } else {
                adapter.setItem(interest)
            }
        }

        save.setOnClickListener {
            listner(selected)
            dialog?.let {
                if(it.isShowing) it.dismiss()
            }
        }
        dialog?.show()

    }

    fun hideInterest() {
        dialog?.let {
            if(it.isShowing) it.dismiss()
        }
    }

}