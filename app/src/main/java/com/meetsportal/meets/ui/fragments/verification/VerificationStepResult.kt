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
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentVerificationStepOneResultBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.ui.dialog.LoaderDialog
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationStepResult : BaseVerificationFragment() {

    val TAG = VerificationStepResult::class.simpleName

    val userAccountViewModel: UserAccountViewModel by viewModels()

    val pagerFragment: VerificationPagerFragment? by lazy {
        activity?.supportFragmentManager?.findFragmentByTag(VerificationPagerFragment.TAG) as VerificationPagerFragment?
    }

    var loader: LoaderDialog? = null

    companion object {

        fun getInstance(verificationNumber: Int): VerificationStepResult {
            return VerificationStepResult().apply {
                arguments = bundleOf(VerificationPagerFragment.IMAGE_TYPE to verificationNumber)
            }
        }
    }

    private var _binding: FragmentVerificationStepOneResultBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentVerificationStepOneResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    //var baseFragment = VerificationCamera.getInstance(arguments?.getInt(VerificationPagerFragment.IMAGE_TYPE)?:0)
    //Navigation.addFragment(requireActivity(),baseFragment,VerificationCamera.TAG,R.id.homeFram,true,false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setUp()
        setObserver()

    }

    private fun initView() {
        Log.i(TAG, " dnckcdkc:::: ")
        userAccountViewModel.getPreVerificationData()
        loader = LoaderDialog(requireActivity())
        binding.next.setRoundedColorBackground(requireActivity(), R.color.primaryDark)
        binding.llRetake.onClick({
            Log.i(TAG, "  Retake:::")
            var baseFragment = VerificationCamera.getInstance(arguments?.getInt(VerificationPagerFragment.IMAGE_TYPE) ?: 0)
            Navigation.addFragment(requireActivity(), baseFragment, VerificationCamera.TAG, R.id.homeFram, true, false)
        })
        binding.tvLater.onClick({
            activity?.onBackPressed()
        })
        when(arguments?.getInt(VerificationPagerFragment.IMAGE_TYPE)) {
            0 -> {
                binding.ivDummy.setImageResource(R.drawable.close_eye)
                binding.next.text = "Next"
                binding.next.onClick({
                    pagerFragment?.binding?.verifyPager?.currentItem = 3
                })
            }

            1 -> {
                binding.ivDummy.setImageResource(R.drawable.head_tilt_to_shoulder)
                binding.next.text = "Submit for review"
                binding.next.onClick({
                    Log.i(TAG, " imageUri11:::: $image1Uri")
                    Log.i(TAG, " imageUri11:::: $image2Uri")
                    if(image1Uri != null && image2Uri != null) userAccountViewModel.verifyProfile(arrayOf(image1Uri, image2Uri))

                })
            }
        }

    }

    fun putImageResult(uri: Uri?) {
        binding.ivResult.setImageURI(uri)
    }

    fun setUp() {

    }

    private fun setObserver() {
        userAccountViewModel.observeVerifyProfile().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Loading -> {
                    if(it.loading) {
                        loader?.showDialog()
                    } else {
                        loader?.hideDialog()
                    }
                }

                is ResultHandler.Success -> {
                    pagerFragment?.binding?.verifyPager?.currentItem = 5
                }

                is ResultHandler.Failure -> {
                    loader?.hideDialog()
                    MyApplication.showToast(requireActivity(), "Oops.. Something went wrong.Please try again after Sometime..")
                    Log.e(TAG, " Fail in Verification ${it.throwable?.printStackTrace()}")
                }
            }
        })


        /**
         * this is observing preVerification data
         */
        userAccountViewModel.observePreVerifyChange().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    when(arguments?.getInt(VerificationPagerFragment.IMAGE_TYPE)) {
                        0 -> {
                            binding.ivDummy.loadImage(requireContext(), it.value?.definition?.pose_1)
                            binding.desc.text = it.value?.definition?.pose_description_1
                        }

                        1 -> {
                            binding.ivDummy.loadImage(requireContext(), it.value?.definition?.pose_2)
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

    }

    override fun image2UriCame(uri: Uri?) {

    }


    override fun onBackPageCome() {

    }
}