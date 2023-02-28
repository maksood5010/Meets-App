package com.meetsportal.meets.ui.fragments.verification

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.FragmentVerificationStepOneBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationStep : BaseVerificationFragment() {

    val TAG = VerificationStep::class.simpleName
    val profileViewmodel: UserAccountViewModel by viewModels()

    companion object {

        fun getInstance(verificationNumber: Int): VerificationStep {
            return VerificationStep().apply {
                arguments = bundleOf(VerificationPagerFragment.IMAGE_TYPE to verificationNumber)
            }
        }
    }

    val parentFragment: VerificationPagerFragment? by lazy {
        activity?.supportFragmentManager?.findFragmentByTag(VerificationPagerFragment.TAG) as VerificationPagerFragment?
    }

    private var _binding: FragmentVerificationStepOneBinding? = null

    //private var _binding: FragmentVerificationCodeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentVerificationStepOneBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setUp()
        setObserver()

    }

    private fun initView() {
        profileViewmodel.getPreVerificationData()
        binding.takeSelfie.setRoundedColorBackground(requireActivity(), R.color.primaryDark)
        binding.takeSelfie.onClick({
            var baseFragment = VerificationCamera.getInstance(arguments?.getInt(VerificationPagerFragment.IMAGE_TYPE) ?: 0)
            Navigation.addFragment(requireActivity(), baseFragment, VerificationCamera.TAG, R.id.homeFram, true, false)
        }, 1000)

        binding.tvLater.onClick({
            activity?.onBackPressed()
        })
        when(arguments?.getInt(VerificationPagerFragment.IMAGE_TYPE)) {
            0 -> {
                binding.tvStepOne.text = "Step One"
                binding.desc.text = "Take a selfie with both eye close just like in the sample picture above"
                //inding.ivImages.setImageResource(R.drawable.close_eye)
            }

            1 -> {
                binding.tvStepOne.text = "Step Two"
                binding.desc.text = "Take a selfie with your head tilted to your shoulder just like in the sample picture above"
                //binding.ivImages.setImageResource(R.drawable.head_tilt_to_shoulder)
            }
        }


    }

    private fun setUp() {


    }

    private fun setObserver() {
        /**
         * this is observing preVerification data
         */
        profileViewmodel.observePreVerifyChange().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    when(arguments?.getInt(VerificationPagerFragment.IMAGE_TYPE)) {
                        0 -> {
                            binding.ivImages.loadImage(requireContext(), it.value?.definition?.pose_1)
                            binding.desc.text = it.value?.definition?.pose_description_1
                        }

                        1 -> {
                            binding.ivImages.loadImage(requireContext(), it.value?.definition?.pose_2)
                            binding.desc.text = it.value?.definition?.pose_description_2
                        }
                    }
                }

                is ResultHandler.Failure -> {

                }
            }
        })
    }

    override fun image1UriCame(uri: Uri?) {
        Log.i(TAG, " image1UriCame:: $uri ")

    }

    override fun image2UriCame(uri: Uri?) {

        Log.i(TAG, " image1UriCame:: $uri ")

    }


    override fun onBackPageCome() {

    }
}