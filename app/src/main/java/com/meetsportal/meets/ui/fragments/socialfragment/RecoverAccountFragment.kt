package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.RecoverAccountFragmentBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant.REMAIN_TIME
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.viewmodels.OnboardRegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class RecoverAccountFragment : BaseFragment() {

    val onboardRegistrationViewModel: OnboardRegistrationViewModel by viewModels()
    private var makeResendActive: Boolean = false
    private var email: String? = null
    private var newNumber: String? = null
    private var oldNUmber: String? = null
    private var emailOtp: String? = null
    private var phoneOtp: String? = null
//    val userViewModel: UserAccountViewModel by viewModels()

    private var _binding: RecoverAccountFragmentBinding? = null
    private val binding get() = _binding!!
    private val disposable = CompositeDisposable()

    var time = 0
    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = RecoverAccountFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.viewFlipper.displayedChild=1
        initView()
        setObserver()
        setup()

    }

    private fun initView() {
    }

    private fun setup() {
        binding.oldCCP.registerCarrierNumberEditText(binding.etOldPhone)
        binding.newCCP.registerCarrierNumberEditText(binding.etNewPhone)
        binding.ivBack.onClick({
            activity?.onBackPressed()
        })
        binding.tvSubmit.onClick({
            if(validateFrom()) {
                oldNUmber = binding.oldCCP.fullNumberWithPlus
                newNumber = binding.newCCP.fullNumberWithPlus
                email = binding.etEmail.text.toString()
                onboardRegistrationViewModel.recoverAccountOtp(oldNUmber, newNumber, email)
            }
        })
        binding.tvResend.onClick({
            if(makeResendActive) {
                makeResendActive = false
                time = REMAIN_TIME
                binding.emailOtp.text = null
                binding.phoneOtp.text = null
                phoneOtp = null
                emailOtp = null
                onboardRegistrationViewModel.recoverAccountOtp(oldNUmber, newNumber, email)
            }
        })
        disposable.add(Flowable.interval(1, TimeUnit.SECONDS)
            .onBackpressureDrop()
            .filter { t ->
            time > 0
        }.map {
            time -= 1
            time
        }.observeOn(AndroidSchedulers.mainThread()).subscribe( {
            //time - it
            if(time == 0) {
                makeResendActive = true
                binding.tvResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            } else {
                binding.tvResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.resend))
            }
            binding.tvTimer.text = "$time seconds remaining"

        },{
                    Log.e(AddPlaceToMeetUp.TAG, "setUp: OnBoard Interval Error handled")
                    FirebaseCrashlytics.getInstance().log("OnBoard Interval Error handled")
                    FirebaseCrashlytics.getInstance().recordException(it)

                }))
    }

    private fun validateFrom(): Boolean {
        if(binding.etOldPhone.text.toString().isEmpty()) {
            showToast("Enter the registered phone number")
        } else if(binding.etNewPhone.text.toString().isEmpty()) {
            showToast("Enter the new phone number")
        } else if(binding.etEmail.text.toString().isEmpty()) {
            showToast("Enter the recovery email")
        } else if(!binding.oldCCP.isValidFullNumber) {
            showToast("Old phone number is not valid")
        } else if(!binding.newCCP.isValidFullNumber) {
            showToast("New phone number is not valid")
        } else if(!Utils.isValidEmail(binding.etEmail.text)) {
            showToast("Please enter a valid email")
        } else {
            return true
        }
        return false
    }

    private fun setObserver() {
        onboardRegistrationViewModel.observeRecoverResponse().observe(viewLifecycleOwner, { it1 ->
            when(it1) {
                is ResultHandler.Success -> {
                    onSendOtp()
                }

                is ResultHandler.Failure -> {
                    showToast(it1?.message ?: "Something went wrong")
                }
            }
        })
        onboardRegistrationViewModel.observeRecoverVerifyResponse().observe(viewLifecycleOwner, { it1 ->
            when(it1) {
                is ResultHandler.Success -> {
                    onVerifyCompleted()
                }

                is ResultHandler.Failure -> {
                    showToast(it1?.message ?: "Something went wrong")
                }
            }
        })
//        userViewModel.observeOnChangeCred().observe(viewLifecycleOwner, {
//            when(it) {
//                is ResultHandler.Success -> {
//                    showToast("Changed Successfully")
//                    val bundle = Bundle().apply {
//                        putBoolean("result", true)
//                    }
//                    setFragmentResult("update", bundle)
//                    activity?.onBackPressed()
//                }
//
//                is ResultHandler.Failure -> {
//                    showToast(it?.message ?: "Something went wrong")
//                }
//            }
//        })
    }

    private fun onVerifyCompleted() {
        showToast("Number Changed successfully, now you can login with new number", Toast.LENGTH_LONG)
        activity?.onBackPressed()
    }

    private fun onSendOtp() {
        showToast("OTP sent to new phone and recovery email")
        makeResendActive = false
        time = REMAIN_TIME
        binding.viewFlipper.displayedChild = 1
        binding.tvSendMail.text = "code sent to \n$email"
        binding.tvSendPhone.text = "code sent to \n$newNumber"
        binding.emailOtp.addTextChangedListener {
            if(it == null) {
                emailOtp = null
            } else {
                emailOtp = it.toString()
            }
        }
        binding.phoneOtp.addTextChangedListener {
            if(it == null) {
                phoneOtp = null
            } else {
                phoneOtp = it.toString()
            }
        }
        binding.emailOtp.setOtpCompletionListener {
            if(phoneOtp?.length == 4) {
                onCompletedOtp(it, phoneOtp, email, newNumber)
            }
        }
        binding.phoneOtp.setOtpCompletionListener {
            if(emailOtp?.length == 4) {
                onCompletedOtp(emailOtp, it, email, newNumber)
            }
        }
    }

    private fun onCompletedOtp(emailOtp: String?, phoneOtp: String?, email: String?, newNumber: String?) {
//        showToast("emailOtp:$emailOtp,phoneOtp:$phoneOtp")
        onboardRegistrationViewModel.recoverVerifyOtp(oldNUmber, newNumber, email, emailOtp?.toIntOrNull(), phoneOtp?.toIntOrNull())

    }

    override fun onBackPageCome() {
    }
}