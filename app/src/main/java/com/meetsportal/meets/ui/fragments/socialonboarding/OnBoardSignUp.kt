package com.meetsportal.meets.ui.fragments.socialonboarding

import android.app.Activity
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.hbb20.CountryCodePicker
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.models.StateResource
import com.meetsportal.meets.models.Status
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.registration.RegistrationRequest
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.ui.dialog.LoaderDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.TermsFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.FireBaseViewModal
import com.meetsportal.meets.viewmodels.OnboardRegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OnBoardSignUp : BaseFragment() {

    private val TAG = OnBoardSignUp::class.java.simpleName

    val onboardRegitrationViewModel: OnboardRegistrationViewModel by viewModels()
    val firebaseViewModel: FireBaseViewModal by viewModels()

    lateinit var countryCodePicker: CountryCodePicker
    lateinit var  username : EditText
    lateinit var contactNumber : EditText
    lateinit var parent : View
    lateinit var userNameExistDesc :TextView
    lateinit var signInButton : ImageView
    lateinit var fbLogin : ImageView
    lateinit var callbackManager : CallbackManager

    private var email: String? = null
    private var idToken: String? = null

    private val RC_SIGN_IN = 9001
    var loader : LoaderDialog? = null


    var canUseUserName : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_signup,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp(view)
        //findNavController().navigate(R.id.action_socialOnBoarding1_to_socialOnBoarding2)
    }

    private fun initView(view: View) {
        countryCodePicker = view.findViewById(R.id.country_code_picker)
        username  = view.findViewById(R.id.et_username)
        fbLogin = view.findViewById(R.id.fbLogin)
        username.filters = arrayOf<InputFilter>(AllLowerInputFilter(),InputFilter.LengthFilter(20))
//        {
//            override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence {
//                return source.toString().toLowerCase()
//            }
//        })
        contactNumber = view.findViewById(R.id.et_mobile)
        parent = view.findViewById(R.id.rootCo)
        userNameExistDesc = view.findViewById(R.id.tv_username_desc)
        loader = LoaderDialog(requireActivity())
        signInButton = view.findViewById(R.id.signInButton)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        signInButton.onClick({
            signIn()
        })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode,resultCode,data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                idToken = account.idToken
                email = account.email
                loader?.showDialog()
                onboardRegitrationViewModel.verifyGoogle(idToken, Utils.getCountryCode(requireActivity()))
//                showToast("email::${account.email}")
//                firebaseAuthWithGoogle(account.idToken!!)
            } catch(e: ApiException) {
                // Google Sign In failed, update UI appropriately
                if (e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED){
                    showToast("Cancelled")
                }else{
                    showToast("Something went wrong...")
                }
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }
    var googleSignInClient: GoogleSignInClient? = null
    private fun signIn() {
        googleSignInClient?.signOut()
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    private fun setUp(view: View) {
        countryCodePicker.registerCarrierNumberEditText(contactNumber)
        callbackManager = CallbackManager.Factory.create();

//        username.filters = arrayOf<InputFilter>(object : InputFilter.AllCaps() {
//            override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): String {
//                return source.toString().toLowerCase()
//            }
//        })
        username.count {
            onboardRegitrationViewModel.getIsUserNameExist(username.text.trim().toString().toLowerCase())
        }

        arguments?.let {
            countryCodePicker.fullNumber = it.getString("phoneNumber")
        }

        fbLogin.onClick({
            fbLogin()
        })
        addListener(view)
        addObserver()


    }

    private fun fbLogin(){
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired

        Log.i(TAG, "logInWithReadPermissions: 0  $isLoggedIn")
        if(isLoggedIn){
            LoginManager.getInstance().logOut()
        }
        //LoginManager.getInstance().loginBehavior = LoginBehavior.NATIVE_ONLY
        LoginManager.getInstance()
            .logInWithReadPermissions(requireActivity(), listOf("public_profile", "email"))

        Log.i(TAG, "logInWithReadPermissions: 1 ${callbackManager.hashCode()}")


        LoginManager.getInstance()
            .registerCallback(callbackManager,object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.i(TAG, "logInWithReadPermissions: onSuccess -- ${result?.accessToken.token}")
                    onboardRegitrationViewModel.verifyFacebook(result.accessToken.token, Utils.getCountryCode(requireActivity()))
                    loader?.showDialog()
                }

                override fun onCancel() {
                    Log.i(TAG, "logInWithReadPermissions: onCancel")
                    activity!!.toast("cancelled")
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, "logInWithReadPermissions: onError -- ${error?.message} ${error?.localizedMessage}")
                    FirebaseCrashlytics.getInstance().log("Firebase Login error happened!!!!!")
                    FirebaseCrashlytics.getInstance().log(error.toString())
                    FirebaseCrashlytics.getInstance().recordException(RuntimeException())
                    Log.e("ERROR", error.toString())
                }
            })
        Log.i(TAG, "logInWithReadPermissions: 2")
    }

    private fun addObserver() {

        onboardRegitrationViewModel.observeVerifyTokenResponse().observe(viewLifecycleOwner, Observer {

            when(it){
                is ResultHandler.Success ->{
                    if(it.value?.user?.username.isNullOrEmpty()){
                        loader?.hideDialog()
                        Navigation.addFragment(requireActivity(),OnBoardSocial.getInstance(it?.value),"OnBoardSocial",R.id.onBoard_frame,true,false)
                    }else{
                        PreferencesManager.put(it.value,Constant.PREFRANCE_OTPRESPONSE)
                        firebaseViewModel.login(it.value?.auth?.firebase?.custom_token)
                    }
//                    var fragment = OnBoardOTP.getInstance("+${countryCodePicker.fullNumber}",username.text.toString().trim().toLowerCase())
//                    //baseFragment = Navigation.setFragmentData(baseFragment,"CONTACT_NUMBER","+${countryCodePicker.fullNumber}")
//                    //baseFragment = Navigation.setFragmentData(baseFragment,"USERNAME","${username.text.toString().trim()}")
//                    Navigation.addFragment(requireActivity(),fragment,"OnBoardOTP",R.id.onBoard_frame,true,false)
                }
                is ResultHandler.Failure ->{
                    loader?.hideDialog()
                    Toast.makeText(requireContext(),it.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
        onboardRegitrationViewModel.observerRegisterUserResponse().observe(viewLifecycleOwner, Observer {
            /*Toast.makeText(requireActivity(),"OTP is:: ${it.message}",Toast.LENGTH_LONG).show()
            loader?.hideDialog()
            var baseFragment:BaseFragment = OnBoardOTP()
            baseFragment = Navigation.setFragmentData(baseFragment,"CONTACT_NUMBER","+${countryCodePicker.fullNumber}")
            baseFragment = Navigation.setFragmentData(baseFragment,"USERNAME","${username.text.toString().trim()}")
            Navigation.addFragment(requireActivity(),baseFragment,"OnBoardOTP",R.id.onBoard_frame,true,false)*/
            loader?.hideDialog()
            when(it){
                is ResultHandler.Success ->{
                    var fragment = OnBoardOTP.getInstance("+${countryCodePicker.fullNumber}",username.text.toString().trim().toLowerCase())
                    //baseFragment = Navigation.setFragmentData(baseFragment,"CONTACT_NUMBER","+${countryCodePicker.fullNumber}")
                    //baseFragment = Navigation.setFragmentData(baseFragment,"USERNAME","${username.text.toString().trim()}")
                    Navigation.addFragment(requireActivity(),fragment,"OnBoardOTP",R.id.onBoard_frame,true,false)
                }
                is ResultHandler.Failure ->{
                    Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                }
            }
        })

        onboardRegitrationViewModel.observeUserNameExist().observe(viewLifecycleOwner, Observer {
            //Log.i(TAG," checkingUsernameRes:: ${it.code()}  ${it.body()?.exists}")
            /*if(it.code() == 200){
                canUseUserName = !it.body()?.exists!!
                if(canUseUserName){
                    userNameExistDesc.setTextColor(Color.parseColor("#E8E8E8"))
                    userNameExistDesc.text = "Username must not have special characters"
                }else{
                    userNameExistDesc.setTextColor(Color.parseColor("#ff0000"))
                    userNameExistDesc.text = "Username already exists"
                }
            }
*/
            when (it){
                is ResultHandler.Success -> {
                    Log.i(TAG," Success:: ${it.value}")
                    canUseUserName = !it.value?.exists!!
                    if(canUseUserName){
                        userNameExistDesc.setTextColor(Color.parseColor("#E8E8E8"))
                        userNameExistDesc.text = "Username must not have special characters"
                    }else{
                        userNameExistDesc.setTextColor(Color.parseColor("#ff0000"))
                        userNameExistDesc.text = "Username already exists"
                    }
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
            loader?.hideDialog()
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
                        intent.putExtra(OnBoardOTP.IS_NEWUSER,false)
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
        view.findViewById<TextView>(R.id.tv_go).onClick( {
            MyApplication.smallVibrate()
            if(validateField() && canUseUserName) {
                //username.text.toString().trim()
                onboardRegitrationViewModel.registerUser(RegistrationRequest(null, "+${countryCodePicker.fullNumber}","register"))
                loader?.showDialog()
            }

        })

        view.findViewById<TextView>(R.id.sign_in).setOnClickListener {
            MyApplication.smallVibrate()
            activity?.onBackPressed()
        }

        view.findViewById<TextView>(R.id.tvTerms).setOnClickListener {
            val terms: BaseFragment = TermsFragment()
            Navigation.setFragmentData(terms, "url", Constant.TermsUrl)
            Navigation.setFragmentData(terms, "title", getString(R.string.terms))
            Navigation.addFragment(activity as Activity, terms, "TermsFragment", R.id.onBoard_frame, stack = true, needAnimation = false)
        }

    }

    private fun validateField():Boolean{
        if(username.text.toString().trim().count() < 4) {
            parent.showSnackBar("Username should be minimum 4 letter...")
            return false
        }
        Log.i(TAG,"phone number ${countryCodePicker.fullNumber}")
        if(!countryCodePicker.isValidFullNumber) {
            parent.showSnackBar("Please fill correct Phone Number")
            return false
        }
        if(username.text.toString().trim().getOrNull(0) in '0'..'9'){
            parent.showSnackBar("Username cannot start with number…")
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