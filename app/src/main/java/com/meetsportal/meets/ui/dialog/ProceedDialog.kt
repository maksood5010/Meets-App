package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.Window
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.DialogProceedBinding

class ProceedDialog(context: Activity, var click: (b: Boolean) -> Unit) : Dialog(context, R.style.BottomSheet) {

    private var _binding: DialogProceedBinding? = null
    private val binding get() = _binding!!

    init {
        _binding = DialogProceedBinding.inflate(LayoutInflater.from(context))
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(binding.root)
        binding.tvYes.setOnClickListener {
            click(true)
            dismiss()
        }
        binding.tvNo.setOnClickListener {
            click(false)
            dismiss()
        }
        setOnDismissListener {
            click(false)
        }
        show()
    }

    fun setMessage(head: String, msg: String) {
        binding.tvHeading.text = head
        binding.tvSubHeading.text = msg
    }

    fun setProceed(yes: String, no: String) {
        binding.tvYes.text = yes
        binding.tvNo.text = no
    }
}