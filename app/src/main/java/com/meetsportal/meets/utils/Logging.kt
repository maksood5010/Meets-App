package com.meetsportal.meets.utils

import android.util.Log

object Logging {

    fun i(tag: String, msg: String){
        for (i in 0..msg.length / 4000) {
            val start: Int = i * 4000
            var end: Int = (i + 1) * 4000
            end = if (end > msg.length) msg.length else end
            Log.i(tag, msg.substring(start, end))
        }
    }

}