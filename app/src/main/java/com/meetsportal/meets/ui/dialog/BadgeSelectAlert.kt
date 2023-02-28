package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.Window
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.BadgeListAdapter
import com.meetsportal.meets.databinding.BadgeSelectDialogBinding
import com.meetsportal.meets.overridelayout.SpaceItemDecoration
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground


class BadgeSelectAlert(val myContext: Activity, var chosen: (Int) -> Unit) : Dialog(myContext, R.style.BottomSheetDialog) {


    private var _binding: BadgeSelectDialogBinding? = null
    private val binding get() = _binding!!
    var currentSelected = 0

    init {
        _binding = BadgeSelectDialogBinding.inflate(LayoutInflater.from(myContext))
        setCanceledOnTouchOutside(false)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.rlDialog.setRoundedColorBackground(myContext)
        binding.ivCancel.setOnClickListener { if(isShowing) dismiss() }
        binding.tvDone.onClick({
            chosen(currentSelected)
            if(isShowing) dismiss()
        })
        var layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
        binding.rvBadges.layoutManager = layoutManager
        val dividerItemDecoration = DividerItemDecoration(myContext, layoutManager.orientation)
        binding.rvBadges.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(10f, myContext.resources), 0))
        binding.rvBadges.addItemDecoration(dividerItemDecoration)
        binding.rvBadges.adapter = BadgeListAdapter(myContext, true) {
            currentSelected = it
        }
    }

    fun showAlert() {
        show()
        binding.rvBadges.adapter?.notifyDataSetChanged()
    }

}