package com.meetsportal.meets.ui.fragments.socialonboarding

//import com.stfalcon.smsverifycatcher.SmsVerifyCatcher
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.models.StateResource
import com.meetsportal.meets.models.Status
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.registration.OtpRequest
import com.meetsportal.meets.networking.registration.OtpResponse
import com.meetsportal.meets.networking.registration.RegistrationRequest
import com.meetsportal.meets.ui.activities.HomeActivityNew
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.ui.dialog.LoaderDialog
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.REMAIN_TIME
import com.meetsportal.meets.viewmodels.FireBaseViewModal
import com.meetsportal.meets.viewmodels.OnboardRegistrationViewModel
import com.mukesh.OtpView
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern


@AndroidEntryPoint
class OnBoardOTP : BaseFragment()  {


    private val TAG = OnBoardOTP::class.java.simpleName

    private val disposable = CompositeDisposable()

    val onboardRegitrationViewModel: OnboardRegistrationViewModel by viewModels()
    val firebaseViewModel: FireBaseViewModal by viewModels()

    //lateinit var smsVerifyCatcher : SmsVerifyCatcher
    lateinit var parent : ConstraintLayout
    lateinit var otpView : OtpView
    lateinit var resend : TextView
    var resendSpannable : SpannableString? = null
    lateinit var tvTimer : TextView

    var loader: LoaderDialog? = null

    var makeResendActive : Boolean = false

    var time = REMAIN_TIME

    companion object{

        val CONTACT_NUMBER = "CONTACT_NUMBER"
        val USERNAME = "USERNAME"
        val IS_NEWUSER = "isNewUser"

        fun getInstance(contactNumber : String?,username:String?):OnBoardOTP{
            return OnBoardOTP().apply {
                arguments= Bundle().apply {
                    putString(CONTACT_NUMBER,contactNumber)
                    putString(USERNAME,username)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_otp, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp(view)
        //findNavController().navigate(R.id.action_socialOnBoarding1_to_socialOnBoarding2)
    }


    private fun initView(view: View) {
        otpView = view.findViewById(R.id.otpView)
        parent = view.findViewById(R.id.rootCo)
        tvTimer = view.findViewById(R.id.tv_timer)
        resend =view.findViewById(R.id.tv_resend)
        resendSpannable = SpannableString(resend.text)
        resendSpannable?.setSpan(StyleSpan(Typeface.BOLD), resend.text.indexOf("Resend"), resend.text.indexOf("Resend").plus(6), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        resendSpannable?.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.resend)),
            resend.text.indexOf("Resend"),
            resend.text.indexOf("Resend").plus(6),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        resend.setText(resendSpannable,TextView.BufferType.SPANNABLE)
        view.findViewById<TextView>(R.id.tv_contact).text = "Code sent to \n ${getContactText()}"
        loader = LoaderDialog(requireActivity())
    }
    private fun setUp(view: View) {


        disposable.add(Flowable.interval(1,TimeUnit.SECONDS)
            .onBackpressureDrop()
            /*.takeWhile{t ->
                Log.i(TAG," Takewhile:: $t")
                t < 60
            }*/
            .filter{t->
                if(time > 0)
                    Log.i(TAG," Filter:: $t")
                time > 0
            }
            .map{
                time -= 1
                time
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                //time - it
                if(time == 0){
                    resendSpannable?.setSpan(StyleSpan(Typeface.BOLD), resend.text.indexOf("Resend"), resend.text.indexOf("Resend").plus(6), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    resendSpannable?.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.white)),
                        resend.text.indexOf("Resend"),
                        resend.text.indexOf("Resend").plus(6),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    resend.setText(resendSpannable,TextView.BufferType.SPANNABLE)
                    makeResendActive = true
                }
                tvTimer.setText("${time} seconds remaining")

            },{
                Log.e(TAG, "setUp: OnBoard Interval Error handled", )
                FirebaseCrashlytics.getInstance().log("OnBoard Interval Error handled")
                FirebaseCrashlytics.getInstance().recordException(it)
            }))
        /*var r = "code sent is 7092 hcdh cd   ydg 60 minute."
        var code = parseCode(r)
        code?.let {
            t1.setText(code[0].toString())
            t2.setText(code[1].toString())
            t3.setText(code[2].toString())
            t4.setText(code[3].toString())
        }*/
        /*timer  = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.setText("${millisUntilFinished / 1000} seconds remaining")
            }

            override fun onFinish() {
                makeResendActive = true
            }
        }.start()*/


//        t1.requestFocus()
//        t1.count { if(it.toInt() == 0) else t2.requestFocus() }
//        t2.count { if(it.toInt() == 0) t1.requestFocus() else t3.requestFocus() }
//        t3.count { if(it.toInt() == 0) t2.requestFocus() else t4.requestFocus() }
//        t4.count { if(it.toInt() == 0) t3.requestFocus() else validate() }

        addListener(view)
        addObserver()
        addSmsReader()

    }

    private fun addListener(view : View) {
        resend.onClick ({
            if(makeResendActive){
                time = REMAIN_TIME
                loader?.showDialog()
                if(Navigation.getFragmentData(this,"USERNAME") != "") {
                    onboardRegitrationViewModel.registerUser(
                        RegistrationRequest(
                            null, Navigation.getFragmentData(this, "CONTACT_NUMBER"),"register"
                        )
                    )
                }else{
                    onboardRegitrationViewModel.registerUser(
                        RegistrationRequest(
                            null, Navigation.getFragmentData(this, "CONTACT_NUMBER"),"login"
                        )
                    )
                }
                Toast.makeText(requireContext(),"OTP Sent!!",Toast.LENGTH_LONG).show()
                //timer?.start()
                makeResendActive = false
                resendSpannable?.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.resend)),
                    resend.text.indexOf("Resend"),
                    resend.text.indexOf("Resend").plus(6),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                resend.setText(resendSpannable,TextView.BufferType.SPANNABLE)

            }
        })
        otpView.setOtpCompletionListener {
            onboardRegitrationViewModel.verifyOtp(OtpRequest(getContactText(), it,Navigation.getFragmentData(this, "USERNAME")))
        }
    }

    private fun addObserver() {
        onboardRegitrationViewModel.observeVerifyOtpResponse().observe(
            viewLifecycleOwner,
            Observer {
                loader?.hideDialog()
                when(it){
                    is ResultHandler.Success ->{
                        Log.i(TAG, " verifyOtpResponse:: ${it.value}")
                        Log.i(TAG," OtpResponse:: ${it.value}")
                        PreferencesManager.put(it.value,Constant.PREFRANCE_OTPRESPONSE)
                        firebaseViewModel.login(it.value?.auth?.firebase?.custom_token)
                        //MyApplication.getMixPanel()?.alias(it.value?.user?.username)
                        //mixpanel.alias(it.value.user.username)
                        MyApplication.getMixPanel()?.identify(it.value?.user?.sid)
                        MyApplication.getMixPanel()?.people?.set(JSONObject(mapOf(
                            "name" to it.value?.user?.username,
                        )))
                    }
                    is ResultHandler.Failure ->{
                        parent.showSnackBar("Incorrect OTP, try again…")
                    }
                }
                /*Log.i(TAG, " verifyOtpResponse:: ${it.code()}")
                loader?.hideDialog()
                if (it.code() != 200) {
                    Log.i(TAG, "${it.body().toString()}  ${it.body()}")
                    parent.showSnackBar("Something went wrong::")
                } else {
                    Log.i(TAG," OtpResponse:: ${it.body()}")
                    PreferencesManager.put(it.body(),Constant.PREFRANCE_OTPRESPONSE)
                    firebaseViewModel.login(it.body()?.auth?.firebase?.custom_token)
                    //activity?.startActivity(Intent(activity, HomeActivityNew::class.java))
                }*/
            })

        onboardRegitrationViewModel.observeErrorResponse().observe(viewLifecycleOwner,Observer{
            Log.i(TAG," errorCame:: ")
            loader?.hideDialog()
            parent.showSnackBar(it!!)
        })

        onboardRegitrationViewModel.observerRegisterUserResponse().observe(viewLifecycleOwner,Observer{
            loader?.hideDialog()
            when(it){
                is ResultHandler.Success -> {
                    Log.i(TAG," otp is verified")
                }
                is ResultHandler.Failure -> {
                    parent.showSnackBar(it.message)
                }
            }

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
                        intent.putExtra(IS_NEWUSER,(PreferencesManager.get<OtpResponse>(Constant.PREFRANCE_OTPRESPONSE)?.auth?.first_time_login ?:false))
                        activity?.startActivity(intent)
                        activity?.finish()

                    }
                    Status.ERROR -> {
                        Log.i(TAG," Firebase Login Error")
                        Log.e(TAG,"${it?.message}")
                        loader?.hideDialog()
                       // parent.showSnackBar("Check your email and password!!")
                    }
                }
            }
        })
    }

    private fun addSmsReader() {
        /*smsVerifyCatcher = SmsVerifyCatcher(
            requireActivity()
        ) { message ->

            Log.i(TAG, " code:: ${message}")
            val code: String? = parseCode(message) //Parse verification code\
            *//*code?.let {
                t1.setText(code[0].toString())
                t2.setText(code[1].toString())
                t3.setText(code[2].toString())
                t4.setText(code[3].toString())
            }*//*
        }*/
    }

//    private fun validate() {
//        if(t1.text.count() != 1 && t2.text.count() != 1 && t3.text.count() != 1 && t4.text.count() != 1){
//            parent.showSnackBar("Please Enter Otp...")
//            return
//        }
//        onboardRegitrationViewModel.verifyOtp(OtpRequest(getContactText(), getVerificationCode(),Navigation.getFragmentData(this, "USERNAME")))
//    }

//    private fun getVerificationCode(): String? {
//        var otp = StringBuilder().apply {
//            append(t1.text.toString())
//            append(t2.text.toString())
//            append(t3.text.toString())
//            append(t4.text.toString())
//        }
//        return otp.toString()
//    }

    private fun getContactText():String{
        return "${Navigation.getFragmentData(this, "CONTACT_NUMBER")}"
    }

    private fun parseCode(message: String?): String? {
        val pattern: Pattern = Pattern.compile("(\\d{4})")
        val matcher: Matcher? = pattern.matcher(message)
        if(matcher?.find()!!){
            Log.i(TAG, " code:: ${matcher?.group(0)}")
            return  matcher?.group(0)
        }
        Log.i(TAG, " code:: not match")
        return  null
    }

    override fun onStart() {
        super.onStart()
        //smsVerifyCatcher.onStart()
    }

    override fun onStop() {
        super.onStop()
        //smsVerifyCatcher.onStop()
//        timer?.let {
//            timer?.cancel()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
//        timer?.let {
//            timer?.cancel()
//        }
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }

}