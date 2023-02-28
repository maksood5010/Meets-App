package com.meetsportal.meets.utils

import android.content.Intent
import android.util.Log
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.ui.activities.HomeActivityNew
import com.meetsportal.meets.ui.activities.MainActivity
import com.onesignal.OSNotificationOpenedResult
import com.onesignal.OneSignal

class MyNotificationOpenHandler : OneSignal.OSNotificationOpenedHandler {

    private val TAG = MyNotificationOpenHandler::class.java.simpleName

    override fun notificationOpened(result: OSNotificationOpenedResult?) {
        var notification = result?.notification
        var json = notification?.additionalData

        Log.i(TAG," checkingNotificationOrigin :: $TAG")


        Log.i(TAG," notification_tapper:: $json ")

        Log.i("OSNotification", "data set with value: " + json);
        if (json != null) {
            // The following can be used to open an Activity of your choice.
            // Replace - getApplicationContext() - with any Android Context.
            // Replace - YOURACTIVITY.class with your activity to deep link
            var intent = Intent(MyApplication.getContext(), MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("data", json.toString());
            MyApplication.getContext()?.startActivity(intent);
        }
    }
}