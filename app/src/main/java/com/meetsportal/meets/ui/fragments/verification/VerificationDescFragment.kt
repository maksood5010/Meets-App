package com.meetsportal.meets.ui.fragments.verification

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.FragmentVerificationDescriptionBinding
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.utils.*

class VerificationDescFragment : BaseVerificationFragment() {


    companion object {

        val DESC_NUMBER = "descNumber"
        val TAG = VerificationDescFragment::class.simpleName!!

        fun getInstance(descNumber: Int): VerificationDescFragment {
            return VerificationDescFragment().apply {
                arguments = bundleOf(DESC_NUMBER to descNumber)
            }
        }

    }

    val profileRes: ProfileGetResponse?

    init {
        profileRes = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
    }

    val parentFragment: VerificationPagerFragment? by lazy {
        activity?.supportFragmentManager?.findFragmentByTag(VerificationPagerFragment.TAG) as VerificationPagerFragment?
    }

    private var _binding: FragmentVerificationDescriptionBinding? = null

    //private var _binding: FragmentVerificationCodeBinding? = null
    private val binding get() = _binding!!
    val myProfile = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentVerificationDescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setUp()
        setObserver()

    }

    private fun initView() {
        binding.rlBorder.setRoundedColorBackground(requireActivity(), R.color.white, corner = 20f, enbleDash = true, strokeHeight = 1f, Dashgap = 0f, stripSize = 30f, strokeColor = R.color.black)
        Utils.stickImage(requireActivity(), binding.dp, profileRes?.cust_data?.profile_image_url, null)
        binding.tvVerify.setRoundedColorBackground(requireActivity(), R.color.primaryDark)
        Log.i(TAG, "initView:123::  ${myProfile?.cust_data?.last_verification_status}")
        when(arguments?.getInt(DESC_NUMBER)) {
            0 -> {
                when(myProfile?.cust_data?.last_verification_status) {
                    Constant.VerificationStat.NEVER.status      -> {
                        binding.tvHeading.text = "Verify your profile"
                        binding.tvVerify.onClick({
                            parentFragment?.binding?.verifyPager?.currentItem = 1
                        }, 1000)
                    }

                    Constant.VerificationStat.PROCESSING.status -> {

                        binding.tvHeading.text = "Verification Under review"
                        binding.headingIcon.setColorFilter(Color.parseColor("#93E2CA"), android.graphics.PorterDuff.Mode.MULTIPLY)
                        binding.headingIcon.setImageResource(R.drawable.ic_clock)
                        binding.desc1.text = "We are currently reviewing your selfies and Will get back to you as soon as the process is Complete "
                        binding.desc2.visibility = View.GONE
                        binding.tvVerify.text = "OK"
                        binding.tvLater.visibility = View.GONE
                        binding.tvVerify.onClick({
                            //parentFragment?.binding?.verifyPager?.currentItem = 6
                            activity?.onBackPressed()
                        }, 1000)
                    }

                    Constant.VerificationStat.VERIFIED.status   -> {
                        binding.tvHeading.text = "You are verified"
                        binding.descReward.text = "Congratulations  "
                        binding.desc1.text = "You are now a verified user of Meets. Your safety Is paramount to us"
                        binding.descReward.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
                        binding.tvLater.visibility = View.GONE
                        binding.tvVerify.text = "OK"
                        binding.tvVerify.onClick({
                            //parentFragment?.binding?.verifyPager?.currentItem = 6
                            activity?.onBackPressed()
                        }, 1000)
                    }

                    Constant.VerificationStat.FAILED.status     -> {
                        binding.tvHeading.text = "Verification failed"
                        binding.headingIcon.setColorFilter(Color.parseColor("#FF7272"), android.graphics.PorterDuff.Mode.MULTIPLY)
                        binding.headingIcon.setImageResource(R.drawable.ic_failed)
                        binding.desc1.text = "Please try the verification process again. "
                        binding.tvVerify.text = "Restart Verification"
                        binding.tvVerify.onClick({
                            parentFragment?.binding?.verifyPager?.currentItem = 1
                        }, 1000)
                    }
                }


                binding.tvLater.onClick({
                    activity?.onBackPressed()
                })
            }

            1 -> {
                binding.tvHeading.text = "Verification Under review"
                binding.headingIcon.setColorFilter(Color.parseColor("#93E2CA"), android.graphics.PorterDuff.Mode.MULTIPLY)
                binding.headingIcon.setImageResource(R.drawable.ic_clock)
                binding.desc1.text = "We are currently reviewing your selfies and Will get back to you as soon as the process is Complete "
                binding.desc2.visibility = View.GONE
                binding.tvVerify.text = "OK"
                binding.tvLater.visibility = View.GONE
                binding.tvVerify.onClick({
                    //parentFragment?.binding?.verifyPager?.currentItem = 6
                    activity?.onBackPressed()
                }, 1000)
            }

            2 -> {
                binding.tvHeading.text = "You are verified"
                binding.desc1.text = "ou are now a verified user of Meets. Your safety Is paramount to us"
                binding.desc2.text = "Verified profiles assures users of safety and gives them confidence to meetup with you.  Your activities get more interactions when you are verified"
                binding.tvVerify.text = "OK"
                binding.descReward.text = "Congratulations  "
                binding.descReward.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
                binding.descReward
                binding.ivCoin.visibility = View.GONE
                binding.tvLater.visibility = View.GONE
            }
        }


    }

    private fun setUp() {

    }

    private fun setObserver() {

    }

    override fun image1UriCame(uri: Uri?) {

    }

    override fun image2UriCame(uri: Uri?) {

    }

    override fun onBackPageCome() {

    }

}