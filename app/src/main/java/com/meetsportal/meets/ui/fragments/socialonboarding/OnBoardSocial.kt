package com.meetsportal.meets.ui.fragments.socialonboarding

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.common.config.GservicesValue.value
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentOnboardSocialBinding
import com.meetsportal.meets.models.StateResource
import com.meetsportal.meets.models.Status
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.registration.OtpResponse
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.ui.dialog.LoaderDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.FireBaseViewModal
import com.meetsportal.meets.viewmodels.OnboardRegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OnBoardSocial : BaseFragment() {

    private var response: OtpResponse? = null
    private var email: String? = null
    private val TAG = OnBoardSocial::class.java.simpleName

    val onboardRegitrationViewModel: OnboardRegistrationViewModel by viewModels()
    val firebaseViewModel: FireBaseViewModal by viewModels()


    var loader : LoaderDialog? = null


    private var _binding: FragmentOnboardSocialBinding? = null
    val binding get() = _binding!!
    var canUseUserName : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOnboardSocialBinding.inflate(inflater, container, false)
        return binding.root
    }
    companion object{
        fun getInstance(value: OtpResponse?):OnBoardSocial{
            return OnBoardSocial().apply {
                arguments = Bundle().apply {
                    putParcelable("otp_res",value)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp(view)
        //findNavController().navigate(R.id.action_socialOnBoarding1_to_socialOnBoarding2)
    }

    private fun initView(view: View) {
        binding.etUsername.filters = arrayOf<InputFilter>(AllLowerInputFilter(),InputFilter.LengthFilter(20))
        loader = LoaderDialog(requireActivity())
    }
    private fun setUp(view: View) {
        response = arguments?.getParcelable<OtpResponse>("otp_res")
        email = response?.user?.email

        binding.etEmail.setText(email)
        binding.etUsername.count {
            onboardRegitrationViewModel.getIsUserNameExist(binding.etUsername.text.trim().toString().toLowerCase())
        }

        addListener(view)
        addObserver()


    }

    private fun addObserver() {

        onboardRegitrationViewModel.observeUserNameExist().observe(viewLifecycleOwner, Observer {
            when (it){
                is ResultHandler.Success -> {
                    Log.i(TAG," Success:: ${it.value}")
                    canUseUserName = !it.value?.exists!!
                    if(canUseUserName){
                        binding.tvUsernameDesc.setTextColor(Color.parseColor("#E8E8E8"))
                        binding.tvUsernameDesc.text = "Username must not have special characters"
                    }else{
                        binding.tvUsernameDesc.setTextColor(Color.parseColor("#ff0000"))
                        binding.tvUsernameDesc.text = "Username already exists"
                    }
                }
                is ResultHandler.Failure -> {
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }
            }
        })
        onboardRegitrationViewModel.observeSetUserNameResponse().observe(viewLifecycleOwner, Observer {
            loader?.hideDialog()
            when (it){
                is ResultHandler.Success -> {
                    Log.i(TAG," Success:: ${it.value}")
                    response?.user = it.value?.user
                    Log.d(TAG, "addObserver 1: ${response?.user?.username}")
                    Log.d(TAG, "addObserver 2: ${it.value?.user?.username}")
                    PreferencesManager.put(response, Constant.PREFRANCE_OTPRESPONSE)
                    firebaseViewModel.login(response?.auth?.firebase?.custom_token)
                }
                is ResultHandler.Failure -> {
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }
            }
        })

        onboardRegitrationViewModel.observeErrorResponse().observe(viewLifecycleOwner,Observer{
            loader?.hideDialog()
        })

        firebaseViewModel.observeLogin().observe(viewLifecycleOwner,Observer<StateResource<String?>>{
            it?.let {
                when(it.status){
                    Status.LOADING -> {
                        Log.i(TAG," Firebase Login start")
                        loader?.showDialog()
                    }
                    Status.SUCCESS ->{

                        //parent.showSnackBar("Signed in Successfull")
                        Log.i(TAG," Firebase Login Sucessfull")
                        loader?.hideDialog()
                        val intent = Intent(activity, MainActivity::class.java)
                        intent.putExtra(OnBoardOTP.IS_NEWUSER,true)
                        activity?.startActivity(intent)
                        activity?.finish()

                    }
                    Status.ERROR   -> {
                        Log.i(TAG," Firebase Login Error")
                        Log.e(TAG,"${it?.message}")
                        loader?.hideDialog()
                        // parent.showSnackBar("Check your email and password!!")
                    }
                }
            }
        })

    }

    private fun addListener(view: View) {
        binding.tvGo.onClick( {
            MyApplication.smallVibrate()
            if(validateField() && canUseUserName) {
                //username.text.toString().trim()
                PreferencesManager.put(response, Constant.PREFRANCE_OTPRESPONSE)
                loader?.showDialog()
                onboardRegitrationViewModel.setUserName(binding.etUsername.text.trim().toString().toLowerCase())
//                loader?.showDialog()
            }

        })

        binding.signIn.setOnClickListener {
            MyApplication.smallVibrate()
            activity?.onBackPressed()
        }

    }

    private fun validateField():Boolean{
        if(binding.etUsername.text.toString().trim().count() < 4) {
            binding.rootCo.showSnackBar("Username should be minimum 4 letter...")
            return false
        }
        if(binding.etUsername.text.toString().trim().getOrNull(0) in '0'..'9'){
            binding.rootCo.showSnackBar("Username cannot start with number…")
            return false
        }
        return true
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }
    class AllLowerInputFilter : InputFilter {

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {

            for (i in start until end) {
                if (source[i].isUpperCase()) {
                    val v = CharArray(end - start)
                    TextUtils.getChars(source, start, end, v, 0)
                    val s = String(v).toLowerCase()

                    return if (source is Spanned) {
                        val sp = SpannableString(s)
                        TextUtils.copySpansFrom(source, start, end, null, sp, 0)
                        sp
                    } else {
                        s
                    }
                }
            }

            return null // keep original
        }
    }
}