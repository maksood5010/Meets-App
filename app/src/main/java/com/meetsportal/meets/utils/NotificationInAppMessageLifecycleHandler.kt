package com.meetsportal.meets.utils

import android.util.Log
import com.onesignal.OSInAppMessage
import com.onesignal.OSInAppMessageLifecycleHandler
import com.onesignal.OneSignal

class NotificationInAppMessageLifecycleHandler : OSInAppMessageLifecycleHandler() {

    val TAG = NotificationInAppMessageLifecycleHandler::class.simpleName!!
    override fun onWillDisplayInAppMessage(message: OSInAppMessage?) {
        OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "OSInAppMessageLifecycleHandler: onWillDisplay Message: " + message?.getMessageId())
        Log.i(TAG," OSInAppMessageLifecycleHandler:: onWillDisplayInAppMessage ${message?.messageId}")
    }

    override fun onDidDisplayInAppMessage(message: OSInAppMessage?) {
        OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "OSInAppMessageLifecycleHandler: onDidDisplay Message: " + message?.getMessageId())
        Log.i(TAG," OSInAppMessageLifecycleHandler:: onDidDisplayInAppMessage ${message?.messageId}")
    }

    override fun onWillDismissInAppMessage(message: OSInAppMessage?) {
        OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "OSInAppMessageLifecycleHandler: onWillDismiss Message: " + message?.getMessageId())
        Log.i(TAG," OSInAppMessageLifecycleHandler:: onWillDismissInAppMessage ${message?.messageId}")

    }

    override fun onDidDismissInAppMessage(message: OSInAppMessage?) {
        OneSignal.onesignalLog(OneSignal.LOG_LEVEL.VERBOSE, "OSInAppMessageLifecycleHandler: onDidDismiss Message: " + message?.getMessageId())
        Log.i(TAG," OSInAppMessageLifecycleHandler:: onDidDismissInAppMessage ${message?.messageId}")

    }
}