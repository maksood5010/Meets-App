package com.meetsportal.meets.ui.fragments.socialfragment.memeit

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R

class BottomSheetOptions : BottomSheetDialogFragment() {

    private val TAG = BottomSheetOptions::class.java.simpleName

    lateinit var tvbutton1: TextView
    lateinit var tvbutton2: TextView
    lateinit var tvbutton3: TextView
    lateinit var tvbutton4: TextView
    lateinit var tvbutton5: TextView
    lateinit var tvbutton6: TextView
    lateinit var tvbutton7: TextView
    lateinit var tvbutton8: TextView
    lateinit var tvbutton9: TextView
    lateinit var tvbutton10: TextView
    lateinit var cancel: TextView
    var listPosition = 0


    var textViewArray = ArrayList<TextView>()
    var hidingOption = arrayListOf<Int>()

    var bottomSheetListener: BottomSheetOptions.BottomSheetListener? = null

    companion object {


        val BUTTON1 = "button1"
        val BUTTON2 = "button2"
        val BUTTON3 = "button3"
        val BUTTON4 = "button4"
        val BUTTON5 = "button5"
        val BUTTON6 = "button6"
        val BUTTON7 = "button7"
        val BUTTON8 = "button8"
        val BUTTON9 = "button9"
        val BUTTON10 = "button10"

        val B1_NAME = "button1Name"
        val B2_NAME = "button2Name"
        val B3_NAME = "button3Name"
        val B4_NAME = "button4Name"
        val B5_NAME = "button5Name"
        val B6_NAME = "button6Name"
        val B7_NAME = "button7Name"
        val B8_NAME = "button8Name"
        val B9_NAME = "button9Name"
        val B10_NAME = "button10Name"
        val POSITION = "position"
        val CANCEL = "cancel"

        fun getInstance(
            nameB1: String? = null,
            nameB2: String? = null,
            nameB3: String? = null,
            nameB4: String? = null,
            positionForColor: Int?,
            nameB5: String? = null,
            nameB6: String? = null,
            nameB7: String? = null,
            nameB8: String? = null,
            nameB9: String? = null,
            nameB10: String? = null,
                       ) = BottomSheetOptions().apply {
            Log.i(TAG, " checkingOptionS::: init ")
            Log.i(TAG, " 1: ${nameB1}")
            Log.i(TAG, " 2: ${nameB2}")
            Log.i(TAG, " 3: ${nameB3}}")
            Log.i(TAG, " 4: ${nameB4}")
            Log.i(TAG, " 5: ${nameB5}")
            Log.i(TAG, " 6: ${nameB6}")
            Log.i(TAG, " 7: ${nameB7}")
            Log.i(TAG, " 8: ${nameB8}")
            Log.i(TAG, " 9: ${nameB9}")
            Log.i(TAG, " 10: ${nameB10}")

            arguments = bundleOf(B1_NAME to nameB1, B2_NAME to nameB2, B3_NAME to nameB3, B4_NAME to nameB4, B5_NAME to nameB5, B6_NAME to nameB6, B7_NAME to nameB7, B8_NAME to nameB8, B9_NAME to nameB9, B10_NAME to nameB10, POSITION to positionForColor)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_share_option_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)

    }

    override fun onCancel(dialog: DialogInterface) {
        bottomSheetListener?.bottomSheetClickedOption(CANCEL)
        super.onCancel(dialog)
    }

    private fun initView(view: View) {
        tvbutton1 = view.findViewById(R.id.tv_share)
        tvbutton2 = view.findViewById(R.id.tv_weekend)
        tvbutton3 = view.findViewById(R.id.tv_newsfeed)
        tvbutton4 = view.findViewById(R.id.tv_download)
        tvbutton5 = view.findViewById(R.id.option5)
        tvbutton6 = view.findViewById(R.id.option6)
        tvbutton7 = view.findViewById(R.id.option7)
        tvbutton8 = view.findViewById(R.id.option8)
        tvbutton9 = view.findViewById(R.id.option9)
        tvbutton10 = view.findViewById(R.id.option10)
        cancel = view.findViewById(R.id.tv_cancel)



        setTextorHide()
        addToArray()


        Log.i(TAG, " colorPosition ${requireArguments().getInt(POSITION)}")
        when(arguments?.getInt(POSITION)) {
            1  -> tvbutton1.setTextColor(Color.parseColor("#ff0000"))
            2  -> tvbutton2.setTextColor(Color.parseColor("#ff0000"))
            3  -> tvbutton3.setTextColor(Color.parseColor("#ff0000"))
            4  -> tvbutton4.setTextColor(Color.parseColor("#ff0000"))
            5  -> tvbutton5.setTextColor(Color.parseColor("#ff0000"))
            6  -> tvbutton6.setTextColor(Color.parseColor("#ff0000"))
            7  -> tvbutton7.setTextColor(Color.parseColor("#ff0000"))
            8  -> tvbutton8.setTextColor(Color.parseColor("#ff0000"))
            9  -> tvbutton9.setTextColor(Color.parseColor("#ff0000"))
            10 -> tvbutton10.setTextColor(Color.parseColor("#ff0000"))
        }

        tvbutton1.setOnClickListener {
            bottomSheetListener?.bottomSheetClickedOption(BUTTON1)
            dismiss()
        }
        tvbutton2.setOnClickListener {
            bottomSheetListener?.bottomSheetClickedOption(BUTTON2)
            dismiss()
        }
        tvbutton3.setOnClickListener {
            bottomSheetListener?.bottomSheetClickedOption(BUTTON3)
            dismiss()
        }

        tvbutton4.setOnClickListener {
            bottomSheetListener?.bottomSheetClickedOption(BUTTON4)
            dismiss()
        }

        tvbutton5.setOnClickListener {
            bottomSheetListener?.bottomSheetClickedOption(BUTTON5)
            dismiss()
        }

        tvbutton6.setOnClickListener {
            bottomSheetListener?.bottomSheetClickedOption(BUTTON6)
            dismiss()
        }

        tvbutton7.setOnClickListener {
            bottomSheetListener?.bottomSheetClickedOption(BUTTON7)
            dismiss()
        }

        tvbutton8.setOnClickListener {
            bottomSheetListener?.bottomSheetClickedOption(BUTTON8)
            dismiss()
        }

        tvbutton9.setOnClickListener {
            bottomSheetListener?.bottomSheetClickedOption(BUTTON9)
            dismiss()
        }

        tvbutton10.setOnClickListener {
            bottomSheetListener?.bottomSheetClickedOption(BUTTON10)
            dismiss()
        }



        cancel.setOnClickListener {
            bottomSheetListener?.bottomSheetClickedOption(CANCEL)
            dismiss()
        }
    }

    fun setTextorHide() {
        setText(tvbutton1, requireArguments().getString(B1_NAME))
        setText(tvbutton2, requireArguments().getString(B2_NAME))
        setText(tvbutton3, requireArguments().getString(B3_NAME))
        setText(tvbutton4, requireArguments().getString(B4_NAME))
        setText(tvbutton5, requireArguments().getString(B5_NAME))
        setText(tvbutton6, requireArguments().getString(B6_NAME))
        setText(tvbutton7, requireArguments().getString(B7_NAME))
        setText(tvbutton8, requireArguments().getString(B8_NAME))
        setText(tvbutton9, requireArguments().getString(B9_NAME))
        setText(tvbutton10, requireArguments().getString(B10_NAME))
    }

    fun addToArray() {
        textViewArray.clear()
        textViewArray.add(tvbutton1)
        textViewArray.add(tvbutton2)
        textViewArray.add(tvbutton3)
        textViewArray.add(tvbutton4)
        textViewArray.add(tvbutton5)
        textViewArray.add(tvbutton6)
        textViewArray.add(tvbutton7)
        textViewArray.add(tvbutton8)
        textViewArray.add(tvbutton9)
        textViewArray.add(tvbutton10)

        hidingOption.forEach {
            textViewArray.get(it).visibility = View.GONE
        }
        hidingOption.clear()
    }

    fun hideOption(ar: Array<Int>) {
        hidingOption.clear()
        hidingOption.addAll(ar)
    }

    fun setText(view: TextView, value: String?) {
        if(value != null) {
            view.text = value
        } else {
            view.visibility = View.GONE
        }
    }

    interface BottomSheetListener {

        fun bottomSheetClickedOption(buttonClicked: String)
    }

    fun setBottomSheetLitener(listener: BottomSheetListener) {
        bottomSheetListener = listener
    }

}