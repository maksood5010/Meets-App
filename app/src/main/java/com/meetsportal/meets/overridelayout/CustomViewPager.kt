package com.meetsportal.meets.overridelayout

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class CustomViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet?=null) :
    ViewPager(context,attrs) {


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return try {
            super.onInterceptTouchEvent(ev)
        }catch (e: IllegalArgumentException){
            false
        }
    }

}