package com.meetsportal.meets.ui.bottomsheet

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.JsonObject
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.socialfragment.ReportOtherFragment
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.viewmodels.PostViewModel

class ReportSheet(var viewModel:PostViewModel, var myContext: Activity?, var id:String?, var contentType:String?): BottomSheetDialogFragment() {

    lateinit var spam :TextView
    lateinit var nudity :TextView
    lateinit var notlike : TextView
    lateinit var violence : TextView
    lateinit var other : TextView

    var body = JsonObject()

    init {
        body.addProperty("content_id",id)
        body.addProperty("content_type",contentType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_report_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()

    }

    private fun initView(view: View) {

        spam = view.findViewById(R.id.tv_spam)
        nudity = view.findViewById(R.id.tv_nudity)
        notlike = view.findViewById(R.id.tv_not_liked)
        violence = view.findViewById(R.id.tv_violence)
        other = view.findViewById(R.id.tv_other)

    }

    private fun setUp() {

        spam.setOnClickListener {
            body.addProperty("reason",spam.text.toString())
            viewModel.reportContent(body)
            dismiss()
        }
        nudity.setOnClickListener {
            body.addProperty("reason",nudity.text.toString())
            viewModel.reportContent(body)
            dismiss()
        }

        notlike.setOnClickListener {
            body.addProperty("reason",notlike.text.toString())
            viewModel.reportContent(body)
            dismiss()
        }

        violence.setOnClickListener {
            body.addProperty("reason",violence.text.toString())
            viewModel.reportContent(body)
            dismiss()
        }

        other.setOnClickListener {
            myContext?.let {
                Navigation.addFragment(it,ReportOtherFragment.getInstance(contentType,id),"ReportOtherFragment",R.id.homeFram,true,false)
            }
            dismiss()
        }
    }
}