package com.meetsportal.meets.application

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cloudinary.android.MediaManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.meetsportal.meets.BuildConfig
import com.onesignal.OneSignal
import com.pusher.client.Pusher
import com.pusher.client.PusherOptions
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.MyNotificationOpenHandler
import com.meetsportal.meets.utils.NotificationInAppMessageLifecycleHandler
import com.meetsportal.meets.utils.NotificationWillShowInForground
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.HiltAndroidApp
import org.json.JSONObject


@HiltAndroidApp
class MyApplication : Application() {

    val TAG = MyApplication::class.simpleName


    init {
        appinstance = this
    }


    private var myApp:MyApplication? = null
    override fun onCreate() {
        super.onCreate()
        Log.i("MyApplication", "  hasCode " + this.hashCode())

        PreferencesManager.with(this)
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this)
        Log.i(TAG," MyApplicationOnCreate::: OneSignal initialized")
        OneSignal.setNotificationWillShowInForegroundHandler(NotificationWillShowInForground())
        Log.i(TAG," MyApplicationOnCreate::: setNotificationWillShowInForegroundHandler")
        OneSignal.setNotificationOpenedHandler(MyNotificationOpenHandler())
        Log.i(TAG," MyApplicationOnCreate::: setNotificationOpenedHandler")
        OneSignal.setInAppMessageLifecycleHandler(NotificationInAppMessageLifecycleHandler())
        Log.i(TAG," MyApplicationOnCreate::: setInAppMessageLifecycleHandler")
        OneSignal.setAppId("979299b5-b321-4511-9f17-29b680176c01")
        Firebase.database.setPersistenceEnabled(true)



        val options = PusherOptions()
        options.setCluster("ap2")

        pusher = Pusher("c7b4545fc798fc01257b", options)
        pusher.connect()

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val name = "Meets"
            val descriptionText = "Social App"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }*/

    }


    companion object{

        val CHANNEL_ID = "com.shisheo.meets"
        private var  showNotiIcon :  MutableLiveData<JSONObject?> = MutableLiveData()


        lateinit var pusher : Pusher
        var appinstance : MyApplication? = null
        private var vibrator : Vibrator? = null
        var SID : String? = null
        var gson : Gson? = null

        fun getContext(): Context? {
            return  appinstance?.applicationContext
        }

        fun getGsons():Gson?{
            if(gson == null){
                gson = Gson()
                return gson
            }else
                return gson
        }

        fun initClodinary(){
            val config: MutableMap<String, String> = HashMap()
            config["cloud_name"] = "shisheo"
            try {
                MediaManager.init(appinstance?.applicationContext!!, config)
            } catch(e: IllegalStateException) {
                Log.e("TAG", " MediaManager is already initialized ${e}")
            }
        }

        fun getMixPanel(): MixpanelAPI? {
            return MixpanelAPI.getInstance(getContext(), BuildConfig.MIXPANEL_TOKEN)
        }

        fun putTrackMP(eventName : String,jsonObject:JSONObject?){
            getMixPanel()?.track(eventName,jsonObject)
        }

        fun setOnSignalExternalId(id: String){
            OneSignal.setExternalUserId(id)
            OneSignal.disablePush(false)
        }

        @SuppressLint("MissingPermission")
        fun smallVibrate(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        10,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            else
                vibrator?.vibrate(10)
        }

        @SuppressLint("MissingPermission")
        fun mediumVibrate(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        50,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            else
                vibrator?.vibrate(50)
        }

        @SuppressLint("MissingPermission")
        fun bigVibrate(){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        200,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            else
                vibrator?.vibrate(50)
        }

        fun showToast(activity: Activity, msg: String,length : Int=Toast.LENGTH_SHORT){
            Toast.makeText(activity,msg,length).show()

            /*val inflater: LayoutInflater = activity.layoutInflater
            val layout: View = inflater.inflate(
                com.meetsportal.meets.R.layout.custom_toast, LinearLayout(activity)
            )
            val text: TextView = layout.findViewById(com.meetsportal.meets.R.id.text)
            text.text = msg

            var shape = GradientDrawable()
            shape.cornerRadii = floatArrayOf(50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f)
            shape.setColor(ContextCompat.getColor(activity, R.color.blacktextColor))
            text.background = shape

            val toast = Toast(appinstance)
            //toast.setGravity(Gravity.FILL_HORIZONTAL or Gravity.BOTTOM, 0, Utils.dpToPx(60f,activity.resources))
            //toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
            toast.duration = length
            toast.view = layout
            toast.show()*/
        }

        fun showNotiIcon(show: JSONObject?){
            showNotiIcon.postValue(show)
        }

        fun observeShowNoti():LiveData<JSONObject?>{
            return showNotiIcon
        }
    }

}