package com.meetsportal.meets.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.cloudinary.android.MediaManager
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.PreferencesManager


class SplashScreen : AppCompatActivity() {

    private val SPLASH_TIME_OUT:Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_splash_screen)

        val config: MutableMap<String, String> = HashMap()
        config["cloud_name"] = "shisheo"
        MediaManager.init(this, config)

        var a = findViewById<MotionLayout>(R.id.motion_base)
        a.transitionToStart()


        Handler(Looper.getMainLooper()).postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity

            PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.let {
                startActivity(Intent(this, MainActivity::class.java))
            }?:run{
                startActivity(Intent(this, AuthenticationActivity::class.java))
            }

            // close this activity
            finish()
        }, SPLASH_TIME_OUT)

    }

    override fun onStart() {
        super.onStart()
        //findViewById<MotionLayout>(R.id.motion_base).transitionToEnd()
    }
}