package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.widget.LinearLayout
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.android.billingclient.api.SkuDetails
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.MintPackageAdapter
import com.meetsportal.meets.databinding.BuyMintDialogBinding
import com.meetsportal.meets.overridelayout.CenterZoomLinearLayoutManager
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setGradient
import com.meetsportal.meets.utils.setRoundedColorBackground


class BuyMintsDialog(var myContext: Activity, val isSuperCharge: Boolean, var buySku: (SkuDetails?) -> Unit) : Dialog(myContext, R.style.BottomSheetDialog) {


    private var _binding: BuyMintDialogBinding? = null
    private val binding get() = _binding!!


    var layoutManager: CenterZoomLinearLayoutManager? = null
    var mintPackageAdapter: MintPackageAdapter? = null
    var skuList: ArrayList<SkuDetails>? = ArrayList<SkuDetails>()

    init {
        _binding = BuyMintDialogBinding.inflate(LayoutInflater.from(myContext))
        setCanceledOnTouchOutside(false)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(binding.root)

        initView()
    }

    fun initView() {
        if(isSuperCharge) {
            binding.parentCard.setGradient(myContext, GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#32BFC9"), Color.parseColor("#1DA271")), 30f)
        } else {
            binding.parentCard.setGradient(myContext, GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#3298C9"), Color.parseColor("#1DA271")), 30f)
        }
        binding.tvBuy.setGradient(myContext, GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(Color.parseColor("#17FDFD"), Color.parseColor("#186F69")), 30f)
//        binding.image.setGradient(myContext, GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
//            Color.parseColor("#4DBD6B"),
//            Color.parseColor("#0095FFC0")),0f)
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.tvBuy.onClick({
            Log.i("TAG", " MiddleItemPosition:::  ${layoutManager?.getPosition()}")
            layoutManager?.getPosition()?.let {
                buySku(skuList?.getOrNull(it))
            }
        })
        layoutManager = CenterZoomLinearLayoutManager(myContext) { child, position, selected ->
            if(selected) {
                child.findViewById<LinearLayout>(R.id.bottomBg).apply {
                    setRoundedColorBackground(myContext, corner = 0f, enbleDash = false, color = Color.parseColor("#42B05E"))
                }
            } else {
                child.findViewById<LinearLayout>(R.id.bottomBg).apply {
                    setRoundedColorBackground(myContext, corner = 0f, enbleDash = false, color = Color.parseColor("#73D399"))
                }
            }
        }
        mintPackageAdapter = MintPackageAdapter(myContext, skuList) {
            binding.rvMintPackage.smoothScrollToPosition(it)
            //binding.rvMintPackage.get

        }
        binding.rvMintPackage.layoutManager = layoutManager
        binding.rvMintPackage.adapter = mintPackageAdapter
        val helper: SnapHelper = PagerSnapHelper()
        helper.attachToRecyclerView(binding.rvMintPackage)
        binding.rvMintPackage.scrollToPosition(1)

    }

    fun setDataInAdapter(value: List<SkuDetails>?) {
        Log.i("TAG", " checkingAllProduct:: $value")
        mintPackageAdapter?.setData(value)
    }

    fun hideDialog() {
        if(this.isShowing) this.dismiss()
    }

}