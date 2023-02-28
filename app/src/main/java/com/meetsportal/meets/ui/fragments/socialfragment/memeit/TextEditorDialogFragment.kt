package com.meetsportal.meets.ui.fragments.socialfragment.memeit

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.ColorPickerAdapter

class TextEditorDialogFragment : DialogFragment() {


    lateinit var mAddTextEditText: EditText
    lateinit var mAddTextDoneTextView: TextView
    private var mInputMethodManager: InputMethodManager? = null
    private var mColorCode: Int? = null
    private var mTextEditor: TextEditor? = null

    companion object {

        const val EXTRA_INPUT_TEXT = "extra_input_text"
        const val EXTRA_COLOR_CODE = "extra_color_code"
        val TAG = TextEditorDialogFragment::class.java.simpleName

        fun show(appCompatActivity: AppCompatActivity, inputText: String, @ColorInt colorCode: Int): TextEditorDialogFragment {
            val args = Bundle()
            args.putString(EXTRA_INPUT_TEXT, inputText)
            args.putInt(EXTRA_COLOR_CODE, colorCode)
            val fragment = TextEditorDialogFragment()
            fragment.arguments = args
            fragment.show(appCompatActivity.supportFragmentManager, TextEditorDialogFragment.TAG)
            return fragment
        }

        fun show(appCompatActivity: AppCompatActivity): TextEditorDialogFragment {
            return show(appCompatActivity, "", ContextCompat.getColor(appCompatActivity, R.color.white))
        }
    }

    interface TextEditor {

        fun onDone(inputText: String?, colorCode: Int)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        dialog?.let {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return return inflater.inflate(R.layout.add_text_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAddTextEditText = view.findViewById(R.id.add_text_edit_text);
        mAddTextDoneTextView = view.findViewById(R.id.add_text_done_tv)
        mInputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        //Setup the color picker for text color
        val addTextColorPickerRecyclerView: RecyclerView = view.findViewById(R.id.add_text_color_picker_recycler_view)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        addTextColorPickerRecyclerView.layoutManager = layoutManager
        //   addTextColorPickerRecyclerView.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(requireActivity())
        //This listener will change the text color when clicked on any color from picker
        //This listener will change the text color when clicked on any color from picker
        colorPickerAdapter.setOnColorPickerClickListener(object : ColorPickerAdapter.OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                mColorCode = colorCode
                mAddTextEditText.setTextColor(colorCode)
            }
        })
        addTextColorPickerRecyclerView.adapter = colorPickerAdapter
        mAddTextEditText.setText(arguments?.getString(EXTRA_INPUT_TEXT))
        mColorCode = arguments?.getInt(EXTRA_COLOR_CODE)
        mAddTextEditText.setTextColor(mColorCode!!)
        mInputMethodManager!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        //Make a callback on activity when user is done with text editing

        //Make a callback on activity when user is done with text editing
        mAddTextDoneTextView.setOnClickListener { view ->
            mInputMethodManager!!.hideSoftInputFromWindow(view.windowToken, 0)
            dismiss()
            val inputText = mAddTextEditText.text.toString()
            if(!TextUtils.isEmpty(inputText) && mTextEditor != null) {
                mTextEditor?.onDone(inputText, mColorCode!!)
            }
        }


    }

    fun setOnTextEditorListener(textEditor: TextEditor) {
        mTextEditor = textEditor
    }


}