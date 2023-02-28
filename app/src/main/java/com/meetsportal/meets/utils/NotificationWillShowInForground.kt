package com.meetsportal.meets.utils

import android.util.Log
import com.onesignal.OSNotificationReceivedEvent
import com.onesignal.OneSignal

class NotificationWillShowInForground : OneSignal.OSNotificationWillShowInForegroundHandler {

    private val TAG = NotificationWillShowInForground::class.java.simpleName

    override fun notificationWillShowInForeground(notificationReceivedEvent: OSNotificationReceivedEvent?) {
        Log.i(TAG," checkingNotificationOrigin :: $TAG")
        var notification = notificationReceivedEvent?.notification
        var json = notification?.additionalData

        //ModelPreferencesManager.
        Log.i(TAG," MynotificationWillShowInForeground:: $json")

        notificationReceivedEvent?.complete(notification)
    }
}