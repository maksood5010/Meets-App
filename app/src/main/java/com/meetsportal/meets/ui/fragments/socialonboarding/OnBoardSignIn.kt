package com.meetsportal.meets.ui.fragments.socialonboarding

import android.content.Intent
import android.os.Bundle
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
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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
import com.meetsportal.meets.ui.fragments.socialfragment.RecoverAccountFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.FireBaseViewModal
import com.meetsportal.meets.viewmodels.OnboardRegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.firebase.crashlytics.FirebaseCrashlytics


@AndroidEntryPoint
class OnBoardSignIn : BaseFragment() {

    private var email: String? = null
    private var idToken: String? = null
    val onboardRegitrationViewModel: OnboardRegistrationViewModel by viewModels()
    val firebaseViewModel: FireBaseViewModal by viewModels()
    val TAG = OnBoardSignIn::class.java.simpleName

    lateinit var countryCodePicker: CountryCodePicker
    lateinit var contactNumber : EditText
    lateinit var parent : View
    lateinit var tvRecover : TextView
    lateinit var tvTerms : TextView
    lateinit var signInButton : ImageView
    lateinit var fbLogin : ImageView
    lateinit var callbackManager : CallbackManager
    var loader :LoaderDialog? = null

    private val RC_SIGN_IN = 9001
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_signin,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp(view)
        //findNavController().navigate(R.id.action_socialOnBoarding1_to_socialOnBoarding2)
    }

    private fun initView(view: View) {
        countryCodePicker = view.findViewById(R.id.country_code_picker)
        contactNumber = view.findViewById(R.id.et_mobile)
        parent = view.findViewById(R.id.rootCo)
        tvRecover = view.findViewById(R.id.tvRecover)
        signInButton = view.findViewById(R.id.signInButton)
        fbLogin = view.findViewById(R.id.fbLogin)
        loader = LoaderDialog(requireActivity())

    }
    var googleSignInClient: GoogleSignInClient? = null
    private fun setUp(view: View) {
        Log.i(TAG, "logInWithReadPermissions: ---1")

        callbackManager = CallbackManager.Factory.create();
        Log.i(TAG, "logInWithReadPermissions: ---2")
        countryCodePicker.registerCarrierNumberEditText(contactNumber)
        //fbLogin.setPermissions("email", "public_profile")
        tvRecover.onClick({
            Navigation.addFragment(requireActivity(),RecoverAccountFragment(), "RecoverAccountFragment",R.id.onBoard_frame,true,false)
        })
        Log.d(TAG, "setUp: default_web_client_id: ${getString(R.string.default_web_client_id)} ")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()


        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        signInButton.onClick({
            signIn()
        })
        fbLogin.onClick({
            fbLogin()
        })
        /*fbLogin.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                Log.d(TAG, "facebook:onSuccess:${loginResult.accessToken}")
                //handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })*/
        addListener(view)
        addObserver()
    }
    private fun signIn() {
        googleSignInClient?.signOut()
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun fbLogin(){
        Utils.printHashKey(requireContext())
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
            .registerCallback(callbackManager,object : FacebookCallback<LoginResult>{
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
                    //error?.printStackTrace()
                    FirebaseCrashlytics.getInstance().log("Firebase Login error happened!!!!!")
                    FirebaseCrashlytics.getInstance().log(error.toString())
                    FirebaseCrashlytics.getInstance().recordException(RuntimeException())
                    Log.e("ERROR", error.toString())
                }
            })
        Log.i(TAG, "logInWithReadPermissions: 2")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "logInWithReadPermissions: onActivityResult")

        try{
            callbackManager.onActivityResult(requestCode,resultCode,data)
        }catch (e:Exception){
            Log.e(TAG, "onActivityResult: ${e.message}", )
            e.printStackTrace()
        }

        Log.i(TAG, "logInWithReadPermissions: onActivityResult 2")
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
            } catch (e: ApiException) {
                if (e.getStatusCode() == GoogleSignInStatusCodes.SIGN_IN_CANCELLED){
                    showToast("Cancelled")
                }else{
                    showToast("Something went wrong...")
                }
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
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
        onboardRegitrationViewModel.observerRegisterUserResponse().observe(viewLifecycleOwner, {
            when(it){
                is ResultHandler.Success ->{
                    Log.i(TAG," checkResponse:: ${it.value}")
                    Log.i(TAG," Go To OTP Screen")
                    loader?.hideDialog()
                    var baseFragment:BaseFragment = OnBoardOTP()
                    baseFragment = Navigation.setFragmentData(baseFragment,"CONTACT_NUMBER","+${countryCodePicker.fullNumber}")
                    baseFragment = Navigation.setFragmentData(baseFragment,"USERNAME",null)
                    Navigation.addFragment(requireActivity(),baseFragment,"OnBoardOTP",R.id.onBoard_frame,true,false)
                }
                is ResultHandler.Failure ->{
                    Log.i(TAG," checkingobserverRegisterUserResponseFail::: $it ")
                    loader?.hideDialog()
                    if(it.code.equals("errors.user.usernamerequired")){
                        parent.showSnackBar("Enter a registered number or Sign up")
                    }else{
                        parent.showSnackBar(it.message)
                    }

                }
            }
                /*Log.i(TAG," checkResponse:: ${it}")

                Log.i(TAG," Go To OTP Screen")
                loader?.hideDialog()
                var baseFragment:BaseFragment = OnBoardOTP()
                baseFragment = Navigation.setFragmentData(baseFragment,"CONTACT_NUMBER","+${countryCodePicker.fullNumber}")
                baseFragment = Navigation.setFragmentData(baseFragment,"USERNAME",null)
                Navigation.addFragment(requireActivity(),baseFragment,"OnBoardOTP",R.id.onBoard_frame,true,false)*/
        })

        onboardRegitrationViewModel.observeErrorResponse().observe(viewLifecycleOwner,Observer{
            Log.i(TAG," errorobserveErrorResponse::: $it ")
            loader?.hideDialog()
            parent.showSnackBar(it)
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
            Log.i(TAG," vibration:: ")
            MyApplication.smallVibrate()
            if(validateField()) {
                loader?.showDialog()
                onboardRegitrationViewModel.registerUser(RegistrationRequest(null, "+${countryCodePicker.fullNumber}","login"))
            }
        })

        view.findViewById<TextView>(R.id.sign_up).setOnClickListener {
            MyApplication.smallVibrate()
            var baseFragment = OnBoardSignUp()
            var bundle =Bundle().apply {
                putString("phoneNumber",countryCodePicker.fullNumber)
            }
            baseFragment.arguments = bundle
            Navigation.addFragment(requireActivity(),baseFragment,"OnBoardSignUp",R.id.onBoard_frame,true,false)
        }
    }

    private fun validateField(): Boolean {
        if(!countryCodePicker.isValidFullNumber) {
            parent.showSnackBar("Please fill correct Phone Number")
            return false
        }
        return true
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

}