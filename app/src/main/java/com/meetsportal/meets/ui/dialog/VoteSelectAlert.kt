package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.VoteChangeAdapter
import com.meetsportal.meets.databinding.VoteSelectDialogBinding
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground


class VoteSelectAlert(val myContext: Activity, val title: String, val list: List<String>, var chosen: (Int, String) -> Unit) : Dialog(myContext, R.style.BottomSheetDialog) {


    private var _binding: VoteSelectDialogBinding? = null
    private val binding get() = _binding!!
    var currentSelected = 0

    init {

        Log.i("VoteSelectAlert", " checkingPosition:: $currentSelected")
        _binding = VoteSelectDialogBinding.inflate(LayoutInflater.from(myContext))
        setCanceledOnTouchOutside(false)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(binding.root)

        initView()
    }

    fun initView() {
        binding.tvTitle.text = title
        var icon = R.drawable.ic_exchange
        if(title.toLowerCase() == "join time") {
            icon = R.drawable.ic_jointime
        } else if(title.toLowerCase() == "duration") {
            icon = R.drawable.ic_clock2
        } else if(title.toLowerCase() == "vote change") {
            icon = R.drawable.ic_exchange
        }
        binding.rlDialog.setRoundedColorBackground(myContext)
        binding.ivCancel.setOnClickListener { if(isShowing) dismiss() }
        binding.tvDone.onClick({
            chosen(currentSelected, list[currentSelected])
            if(isShowing) dismiss()
        })
        var layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.VERTICAL, false)
        binding.rvBadges.layoutManager = layoutManager
//        val dividerItemDecoration = DividerItemDecoration(myContext, layoutManager.orientation)
//        binding.rvBadges.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(10f,myContext.resources),0))
//        binding.rvBadges.addItemDecoration(dividerItemDecoration)
        val voteChangeAdapter = VoteChangeAdapter(myContext, icon, currentSelected, list.toList()) {
            currentSelected = it
        }
        binding.rvBadges.adapter = voteChangeAdapter
    }

    fun showAlert() {
        show()
        binding.rvBadges.adapter?.notifyDataSetChanged()
    }

}
