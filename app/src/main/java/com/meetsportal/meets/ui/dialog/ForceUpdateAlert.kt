package com.meetsportal.meets.ui.dialog

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import com.meetsportal.meets.BuildConfig
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.ForceUpdateAlertBinding
import com.meetsportal.meets.utils.onClick


class ForceUpdateAlert(var mContext: Context) : Dialog(mContext, R.style.TransparentDilaog) {

    private var _binding: ForceUpdateAlertBinding? = null
    private val binding get() = _binding!!
    private val TAG = ForceUpdateAlert::class.java.simpleName

    init {
        _binding = ForceUpdateAlertBinding.inflate(LayoutInflater.from(mContext))
        setCanceledOnTouchOutside(false)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setContentView(binding.root)
        initView()
    }

    fun initView() {
        binding.update.onClick({
            try {
                mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")))
            } catch(e: ActivityNotFoundException) {
                mContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")));
            } catch(e: Exception) {
                Toast.makeText(mContext, "No Apps to Perform this action", Toast.LENGTH_SHORT)
                        .show()
            }
        })
    }

    fun hideDialog() {
        if(this.isShowing) this.dismiss()
    }
}