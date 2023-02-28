package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.UpdateEmailFragmentBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.REMAIN_TIME
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class UpdateMailFragment : BaseFragment() {

    private var channel: String = "email"
    private var makeResendActive: Boolean = false
    private var email: String? = null
    private var phone: String? = null
    val userViewModel: UserAccountViewModel by viewModels()

    private var _binding: UpdateEmailFragmentBinding? = null
    private val binding get() = _binding!!
    private val disposable = CompositeDisposable()

    var time = 0
    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = UpdateEmailFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.viewFlipper.displayedChild=1
        initView()
        setObserver()
        setup()

    }

    companion object {

        val TAG = UpdateMailFragment::class.java.simpleName!!
        fun getInstance(channel: String?): UpdateMailFragment {
            return UpdateMailFragment().apply {
                arguments = Bundle().apply {
                    putString("channel", channel)
                }
            }
        }
    }


    private fun initView() {
        channel = arguments?.getString("channel") ?: "email"
        email = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.email ?: ""
        phone = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.phone_number ?: ""
        if(channel == "email") {
            binding.tvToolbarText.text = "Update Email"
            binding.tvEmail.text = "Email"
            binding.etEmail.hint = "type your email…"
            binding.etEmail.setText(email)
            binding.ivChannel.setImageResource(R.drawable.ic_mail)
            binding.countryCodePicker.visibility = View.GONE
        } else {
            binding.tvToolbarText.text = "Update phone number"
            binding.ivChannel.setImageResource(R.drawable.ic_phone)
            binding.tvEmail.text = "Phone"
            binding.etEmail.hint = "type your phone number…"
//            binding.etEmail.setText(phone)
            binding.countryCodePicker.visibility = View.VISIBLE
            binding.countryCodePicker.registerCarrierNumberEditText(binding.etEmail)
            binding.countryCodePicker.fullNumber = phone
        }
    }

    private fun setup() {
        binding.tvSubmit.isEnabled = false
        binding.tvSubmit.setBackgroundResource(R.drawable.gray_bg_button)
        binding.etEmail.onText {
            if(channel == "email") {
                if(email == it) {
                    binding.tvSubmit.setBackgroundResource(R.drawable.gray_bg_button)
                    binding.tvSubmit.isEnabled = false
                } else {
                    binding.tvSubmit.setBackgroundResource(R.drawable.bg_button)
                    binding.tvSubmit.isEnabled = true
                }
            } else {
                if(binding.countryCodePicker.fullNumber == it) {
                    binding.tvSubmit.setBackgroundResource(R.drawable.gray_bg_button)
                    binding.tvSubmit.isEnabled = false
                } else {
                    binding.tvSubmit.setBackgroundResource(R.drawable.bg_button)
                    binding.tvSubmit.isEnabled = true
                }
            }
        }
        binding.ivBack.onClick({
            activity?.onBackPressed()
        })
        binding.tvSubmit.onClick({
            if(channel == "email") {
                email = binding.etEmail.text.toString()
                if(email == "") {
                    showToast("Please Enter an Email address")
                } else if(!Utils.isValidEmail(binding.etEmail.text, requireContext())) {
                    showToast("Please enter valid Email address!!")
                } else {
                    userViewModel.generateOtp(channel, email, null)
                }
            } else {
                phone = binding.countryCodePicker.fullNumberWithPlus.toString()
                if(phone == "") {
                    showToast("Please Enter a Phone Number")
                } else if(!binding.countryCodePicker.isValidFullNumber) {
                    showToast("Please enter valid Phone Number!!")
                } else {
                    userViewModel.generateOtp(channel, null, phone)
                }
            }
        })
        binding.tvResend.onClick({
            if(makeResendActive) {
                if(channel == "email") {
                    userViewModel.generateOtp(channel, email, null)
                } else {
                    userViewModel.generateOtp(channel, null, phone)
                }
                makeResendActive = false
                time = REMAIN_TIME
            }
        })
        disposable.add(Flowable.interval(1, TimeUnit.SECONDS).onBackpressureDrop()
                /*.takeWhile{t ->
                    Log.i(TAG," Takewhile:: $t")
                    t < 60
                }*/.filter { t ->
                    time > 0
                }.map {
                    time -= 1
                    time
                }.observeOn(AndroidSchedulers.mainThread()).subscribe({
                    //time - it
                    if(time == 0) {
                        makeResendActive = true
                        binding.tvResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                    } else {
                        binding.tvResend.setTextColor(ContextCompat.getColor(requireContext(), R.color.resend))
                    }
                    binding.tvTimer.text = "$time seconds remaining"

                }, {
                    Log.e(AddPlaceToMeetUp.TAG, "setUp: OnBoard Interval Error handled")
                    FirebaseCrashlytics.getInstance().log("OnBoard Interval Error handled")
                    FirebaseCrashlytics.getInstance().recordException(it)

                }))
    }

    private fun setObserver() {
        userViewModel.observeOnSendOtp().observe(viewLifecycleOwner, { it1 ->
            when(it1) {
                is ResultHandler.Success -> {
                    if(channel == "email") {
                        binding.tvContact.text = "code sent to \n$email"
                        binding.tvQuickOne.text = "Verify Email"
                    } else {
                        binding.tvContact.text = "code sent to \n$phone"
                        binding.tvQuickOne.text = "Verify Phone"
                    }
                    makeResendActive = false
                    time = REMAIN_TIME
                    binding.viewFlipper.displayedChild = 1
                    binding.otpView.setOtpCompletionListener {
                        userViewModel.changeCredential(channel, email, phone, it)
                    }
                }

                is ResultHandler.Failure -> {
                    showToast(it1?.message ?: "Something went wrong")
                }
            }
        })
        userViewModel.observeOnChangeCred().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    showToast("Changed Successfully")
                    val bundle = Bundle().apply {
                        putBoolean("result", true)
                    }
                    setFragmentResult("update", bundle)
                    activity?.onBackPressed()
                }

                is ResultHandler.Failure -> {
                    showToast(it?.message ?: "Something went wrong")
//                    if(it.code=="errors.user.phone.alreadyexists"){
//                        if(channel=="email"){
//                            showToast("This Email is already in-use by another user")
//                        }else{
//                            showToast("This Phone number is already in-use by another user")
//                        }
//                    }
                }
            }
        })
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome() {
    }
}