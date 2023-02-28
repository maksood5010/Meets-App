package com.meetsportal.meets.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.ui.activities.HomeActivityNew
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.utils.Constant.ENTITY_ID
import com.meetsportal.meets.utils.Constant.NOTIFICATION_TYPE
import com.meetsportal.meets.utils.Constant.PARENT_ID
import com.meetsportal.meets.utils.Constant.PREFERENCE_MSG_NOTI
import com.meetsportal.meets.utils.Constant.PREFERENCE_SHOW_NOTI
import com.onesignal.OSNotificationReceivedEvent
import com.onesignal.OneSignal


class NotificationServiceExtension():OneSignal.OSRemoteNotificationReceivedHandler {
    private  var TAG = NotificationServiceExtension::class.java.simpleName

    override fun remoteNotificationReceived(
        context: Context?,
        notificationReceivedEvent: OSNotificationReceivedEvent?
    ) {
        Log.i(TAG," checkingNotificationOrigin :: 1")
        val notification = notificationReceivedEvent?.notification

        var json = notification?.additionalData
        Log.i(TAG," checkingNotificationOrigin :: $json")

        if(json?.get("type")?.equals(NotificationTypes.IN_APP_MESSAGE.type) == true){
            PreferencesManager.put<Boolean>(true,PREFERENCE_MSG_NOTI)
        }else{
            PreferencesManager.put<Boolean>(true,PREFERENCE_SHOW_NOTI)
        }


        MyApplication.showNotiIcon(json)
        val mutableNotification = notification?.mutableCopy()

        // Create an Intent for the activity you want to start
        val resultIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(NOTIFICATION_TYPE,json?.getString("type"))
            putExtra(ENTITY_ID,json?.getString(ENTITY_ID))
            putExtra(PARENT_ID,json?.getString(PARENT_ID))
        }
        // Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT )
            }
        }

        mutableNotification?.setExtender { builder: NotificationCompat.Builder ->
            builder.setColor(
                ContextCompat.getColor(context!!, R.color.primaryDark)
            ).setSmallIcon(
                R.drawable.ic_app_icon
            )
        }




        val manager = context?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?


        // creating the notification and its parameters.
        val builder = NotificationCompat.Builder(context!!, "Shisheo").apply {
            setSmallIcon(R.drawable.ic_app_icon)
            setContentTitle(notification?.title)
            setContentText(notification?.body)
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
            setContentIntent(resultPendingIntent)
            setAutoCancel(true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "1",
                "my_channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableLights(true)
            channel.lightColor = Color.green(1)
            channel.setShowBadge(true)
            manager?.createNotificationChannel(channel)
            builder.setChannelId("1")
        }
        val id = json?.getString(ENTITY_ID)?.toCharArray()?.sumOf { it.toInt() }?:1
        manager?.notify(id,builder.build())


        Log.i(TAG, " Mynotinoti:: Showing Notification")




        // If complete isn't call within a time period of 25 seconds, OneSignal internal logic will show the original notification
        // To omit displaying a notifiation, pass `null` to complete()
        notificationReceivedEvent?.complete(null);
    }


    enum class NotificationTypes(val type:String){
        POST_LIKED("post_liked"),
        FOLLOW("follow_user"),
        POST_COMMENT("post_comment"),
        COMMENT_LIKED("comment_liked"),
        REPLY_COMMENT("reply_comment"),
        MEETUP_INVITATION("meetup_invitation"),
        IN_APP_MESSAGE("in_app_message"),
        POSITIVE_RES("meetup_positive_experience_reminder"),
        MEET_CHAT("meetup_chat_message"),
        MEET_UPCOMING("meetup_upcoming_reminder"),
        MEET_TIME("meetup_confirm_arrival_reminder"),
        MEET_ARRIVAL("meetup_attendee_arrival")
    }


}