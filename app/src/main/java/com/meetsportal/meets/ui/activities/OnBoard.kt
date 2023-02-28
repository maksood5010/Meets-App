package com.meetsportal.meets.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.OnBoardingAdapter
import com.meetsportal.meets.utils.Constant.PREFRENCE_FIRST_TIME
import com.meetsportal.meets.utils.PreferencesManager


class OnBoard : AppCompatActivity() {

    lateinit var pager:ViewPager2
    lateinit var radioGroup : RadioGroup
    lateinit var skip : TextView
    lateinit var next : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboard)
        supportActionBar?.hide()

        initView()
        setUp()
    }

    private fun initView() {
        pager = findViewById(R.id.viewPager)
        radioGroup = findViewById(R.id.radioGroup)
        skip = findViewById<TextView>(R.id.skip).apply {
            setOnClickListener {
                openHomePage()
            }
        }
        next = findViewById(R.id.next)

    }

    private fun setUp() {
        pager.adapter = OnBoardingAdapter(
            this,
            resources.getStringArray(R.array.on_boarding_titles).size
        )
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                radioGroup.check(radioGroup.getChildAt(position).getId())
                when (position) {
                    4 -> {
                        next.text = "Get Started"
                        next.setOnClickListener { openHomePage() }
                    }
                    else -> {
                        next.text = "Next"
                        next.setOnClickListener { pager.currentItem = pager.currentItem + 1 }
                    }
                }
            }
        })
    }


    fun openHomePage(){
        PreferencesManager.put(true,PREFRENCE_FIRST_TIME)
        startActivity(Intent(this, AuthenticationActivity::class.java))
        finish()
    }
}
