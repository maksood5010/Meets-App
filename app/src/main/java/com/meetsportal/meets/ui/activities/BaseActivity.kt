package com.meetsportal.meets.ui.activities

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.meetsportal.meets.R


abstract class BaseActivity : AppCompatActivity() {


    fun getCurrentFragment(): Fragment? {
        Log.d("TAG", "getCurrentFragment: supportFragmentManager- ${supportFragmentManager.findFragmentById(R.id.homeFram)?.javaClass}")
        return supportFragmentManager.findFragmentById(R.id.homeFram)
    }
}