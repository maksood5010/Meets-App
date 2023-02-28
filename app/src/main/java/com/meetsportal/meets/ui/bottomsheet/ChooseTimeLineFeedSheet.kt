package com.meetsportal.meets.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.utils.Constant.AcGlobalFeed
import com.meetsportal.meets.utils.Constant.AcOpenMeetFeed
import com.meetsportal.meets.utils.Constant.AcTimeLineFeed

class ChooseTimeLineFeedSheet: BottomSheetDialogFragment() {

    var bottomSheetListener: ChooseTimeLineFeedSheet.BottomSheetListener? = null

    companion object{
        val GLOBAL_TIMELINE = 0
        val MY_TIMELINE = 1
        val OPEN_MEET = 2
    }


    lateinit var global :LinearLayout
    lateinit var timeline :LinearLayout
    lateinit var openMeet :LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_choose_timeline_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)

    }

    private fun initView(view: View) {
        global = view.findViewById<LinearLayout>(R.id.ll_global).apply {
            setOnClickListener{
                MyApplication.putTrackMP(AcGlobalFeed,null)
                bottomSheetListener?.bottomSheetClickedOption( GLOBAL_TIMELINE)
                dismiss()
            }
        }
        timeline = view.findViewById<LinearLayout>(R.id.ll_timeline).apply {
            setOnClickListener{
                MyApplication.putTrackMP(AcTimeLineFeed,null)
                bottomSheetListener?.bottomSheetClickedOption(MY_TIMELINE)
                dismiss()
            }
        }
        openMeet = view.findViewById<LinearLayout>(R.id.ll_openMeet).apply {
            setOnClickListener{
                MyApplication.putTrackMP(AcOpenMeetFeed,null)
                bottomSheetListener?.bottomSheetClickedOption(OPEN_MEET)
                dismiss()
            }
        }


    }

    interface BottomSheetListener {
        fun bottomSheetClickedOption(optionSelected : Int)
    }

    fun setBottomSheetLitener(listener: BottomSheetListener){
        bottomSheetListener = listener
    }
}