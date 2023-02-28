package com.meetsportal.meets.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.socialonboarding.OnBoardSignIn
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.viewmodels.OnboardRegistrationViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

import com.pusher.client.crypto.nacl.Salsa.core




@AndroidEntryPoint
class AuthenticationActivity : BaseActivity() {

    val TAG = AuthenticationActivity::class.java.simpleName

    val onboardRegitrationViewModel: OnboardRegistrationViewModel by viewModels()

    lateinit var go : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_social_onboarding)
        setContentView(R.layout.activity_onboard_new)

        supportActionBar?.hide()
        //Full Screen
        //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        /*window.decorView.apply {
            // Hide both the navigation bar and the status bar.
            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
            // a general rule, you should design your app to hide the status bar whenever you
            // hide the navigation bar.
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }*/
        initView()
        setUP()



    }

    private fun initView() {
        Navigation.removeReplaceFragment(this,OnBoardSignIn(),"OnBoardSignIn",R.id.onBoard_frame,false,false)

    }

    private fun setUP(){
       // Navigation.removeReplaceFragment(this,OnBoardFirstNew(),"OnBoardFirstNew",R.id.social_fram,false,false)
        /*val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, " onActivityResult::::::: ${requestCode} ${resultCode} ")
        //if(resultCode == RESULT_CANCELED)
        try {
            super.onActivityResult(requestCode, resultCode, data)
            Log.d("TAG", "Authentication getCurrentFragment: supportFragmentManager- ${supportFragmentManager.findFragmentById(R.id.onBoard_frame)?.javaClass}")
             supportFragmentManager.findFragmentById(R.id.onBoard_frame)?.onActivityResult(requestCode, resultCode, data)
            //getCurrentFragment()?.onActivityResult(requestCode, resultCode, data)
        } catch(e: RuntimeException) {

        }
    }



}
