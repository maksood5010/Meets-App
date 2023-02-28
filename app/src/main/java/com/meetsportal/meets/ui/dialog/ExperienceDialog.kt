package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.core.content.ContextCompat
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.DialogExperienceBinding
import com.meetsportal.meets.utils.customizeCountBG

class ExperienceDialog(val activity: Activity, count: Int, type: Int, val username: String? = null) : Dialog(activity, R.style.BottomSheet) {

    private var _binding: DialogExperienceBinding? = null
    private val binding get() = _binding!!

    init {
        _binding = DialogExperienceBinding.inflate(LayoutInflater.from(activity))
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(binding.root)
        setData(count, type)
    }

    fun setData(count: Int, type: Int) {
        if(type == 0) {
            showMeetUp(count)
        } else if(type == 1) {
            showPositive(count)
        }
    }

    private fun showPositive(count: Int) {
        val white = ContextCompat.getColor(activity, R.color.white)
        val black = ContextCompat.getColor(activity, R.color.blacktextColor)
        if(count > 0) {
            binding.rootView.customizeCountBG(activity, Color.parseColor("#3E78AE"), Color.parseColor("#4C8FCC"), Color.parseColor("#4FB8FF"))
            binding.ivIcon.setImageResource(R.drawable.thump01)
            binding.tvCount.setTextColor(white)
            binding.tvDesc.setTextColor(white)
            binding.tvCountDesc.setTextColor(white)
            binding.tvCount.text = "$count"
            binding.tvDesc.text = "Positive Experiences"
            if(username == null) {
                binding.tvCountDesc.text = "$count people have had such a pleasant experience with you."
                binding.tvDescription.text = "Mine mints when you give or receive a positive experience"
            } else {
                binding.tvCountDesc.text = "$count people have had such a pleasant experience with $username."
                binding.tvDescription.visibility = View.INVISIBLE
            }
        } else {
            binding.rootView.customizeCountBG(activity, Color.parseColor("#B8B8B8"), Color.parseColor("#D8D5D8"))
            binding.ivIcon.setImageResource(R.drawable.thumb_gray)
            binding.tvCount.setTextColor(black)
            binding.tvDesc.setTextColor(black)
            binding.tvCountDesc.setTextColor(black)
            binding.tvCount.text = "$count"
            binding.tvDesc.text = "Positive Experiences"
            if(username == null) {
                binding.tvCountDesc.text = "You have no Positive Experiences yet"
                binding.tvDescription.text = "Mine mints when you give or receive a positive experience"
            } else {
                binding.tvCountDesc.text = "$username has no Positive Experiences yet"
                binding.tvDescription.visibility = View.INVISIBLE
            }
        }
    }

    private fun showMeetUp(count: Int) {
        val white = ContextCompat.getColor(activity, R.color.white)
        val black = ContextCompat.getColor(activity, R.color.blacktextColor)
        if(count > 0) {
            binding.rootView.customizeCountBG(activity, Color.parseColor("#D34141"), Color.parseColor("#FE4755"), Color.parseColor("#F2A09C"))
            binding.ivIcon.setImageResource(R.drawable.table02)
            binding.tvCount.setTextColor(white)
            binding.tvDesc.setTextColor(white)
            binding.tvCountDesc.setTextColor(white)
            binding.tvCount.text = "$count"
            binding.tvDesc.text = "Meet ups"
            if(username == null) {
                binding.tvCountDesc.text = "You have attended a total of $count meet ups so far"
                binding.tvDescription.text = "Keep taking part in meets ups to increase your Meets Credibility"
            } else {
                binding.tvCountDesc.text = "$username has attended a total of $count meet ups so far"
                binding.tvDescription.visibility = View.INVISIBLE
            }
        } else {
            binding.rootView.customizeCountBG(activity, Color.parseColor("#B8B8B8"), Color.parseColor("#D8D5D8"))
            binding.ivIcon.setImageResource(R.drawable.table)
            binding.tvCount.setTextColor(black)
            binding.tvDesc.setTextColor(black)
            binding.tvCountDesc.setTextColor(black)
            binding.tvCount.text = "$count"
            binding.tvDesc.text = "Meet ups"
            if(username == null) {
                binding.tvCountDesc.text = "You have no meet ups yet"
                binding.tvDescription.text = "Keep taking part in meets ups to increase your Meets Credibility"
            } else {
                binding.tvCountDesc.text = "$username has no meet ups yet"
                binding.tvDescription.visibility = View.INVISIBLE
            }
        }
    }
}