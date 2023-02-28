package com.meetsportal.meets.ui.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.Observer
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.meetsportal.meets.BuildConfig
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.networking.registration.OtpResponse
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.PREFRENCE_FIRST_TIME
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.viewmodels.FireBaseViewModal
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NewSplashScreen : AppCompatActivity() {

    val TAG = NewSplashScreen::class.java.simpleName

    val firebaseViewModel: FireBaseViewModal by viewModels()
    lateinit var appName: TextView

    private val SPLASH_TIME_OUT: Long = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_splash_screen)
        AppEventsLogger.activateApp(application!!);

        //Utils.printHashKey(this)
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
//                    val deepLink: Uri?
//                    if(pendingDynamicLinkData != null && pendingDynamicLinkData.link != null) {
//                        deepLink = pendingDynamicLinkData.link
//                        val profileId = deepLink!!.getQueryParameter("id")
//                        val type = deepLink!!.getQueryParameter("type")
//                        Toast.makeText(this, "full deeplink: $deepLink ", Toast.LENGTH_LONG).show()
//                    }
                }.addOnFailureListener(this) { e ->
                    Log.e("TAG", "getDynamicLink:onFailure", e)
                }
        appName = findViewById<TextView>(R.id.title).apply {
            var version = ""
            try {
                val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                version = pInfo.versionName
            } catch(e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            if(BuildConfig.DEBUG){
                text = getString(R.string.shisheomeets).plus(" $version")
            }

        }

        observe()
        supportActionBar?.hide()

        MyApplication.initClodinary()

        var a = findViewById<MotionLayout>(R.id.motion_base)

        a.transitionToStart()

        PreferencesManager.get<ProfileGetResponse>(Constant.PREFRANCE_OTPRESPONSE)?.let {
            Log.i(TAG, " otpResponseSplash::: ")
            if(FirebaseAuth.getInstance().currentUser == null) {
                Log.i(TAG, " startedLogin:: in Firebase")
                firebaseViewModel.login(
                        PreferencesManager.get<OtpResponse>(Constant.PREFRANCE_OTPRESPONSE)?.auth?.firebase?.custom_token,
                                       )
            }
        } ?: run {
            Log.i(TAG, " OtpRsponseNotSaved::: ")
        }

        Handler(Looper.getMainLooper()).postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity
            //throw Exception("Test Exceeption")
            PreferencesManager.get<OtpResponse>(Constant.PREFRANCE_OTPRESPONSE)?.let {
                //startActivity(Intent(this, OnBoard::class.java))
                // Log.i(TAG," splashScreen COming::: ")
                /*firebaseViewModel.login(ModelPreferencesManager.get<OtpResponse>(Constant.PREFRANCE_OTPRESPONSE)?.auth?.firebase?.email,
                    ModelPreferencesManager.get<OtpResponse>(Constant.PREFRANCE_OTPRESPONSE)?.auth?.firebase?.password)*/

                if(it.user?.username.isNullOrEmpty()){
                    startActivity(Intent(this, AuthenticationActivity::class.java))

                }else{
                    startActivity(Intent(this, MainActivity::class.java))
                }
                Log.d(TAG, "onCreate: calling MainActivity")
                //startActivity(Intent(this, SocialOnboarding::class.java))
            } ?: run {
                PreferencesManager.get<Boolean>(PREFRENCE_FIRST_TIME)?.let {
                    startActivity(Intent(this, AuthenticationActivity::class.java))
                } ?: run {
                    startActivity(Intent(this, OnBoard::class.java))
                }
                //startActivity(Intent(this, AuthenticationActivity::class.java))
            }
            // close this activity
            //finish()
            finishAffinity()
        }, SPLASH_TIME_OUT)
    }

    private fun observe() {
        firebaseViewModel.observeLogin().observe(this, Observer {
            Log.i(TAG, " checkingresponse::: $it")
        })
    }





}