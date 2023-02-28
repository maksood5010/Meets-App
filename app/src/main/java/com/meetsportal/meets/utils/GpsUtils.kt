package com.meetsportal.meets.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class GpsUtils(var myContext: Context, var gpsStatus : (Boolean,Int)-> Unit) {

    private val TAG = GpsUtils::class.java.simpleName

    private var mSettingsClient: SettingsClient? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var locationManager: LocationManager? = null
    private var locationRequest: LocationRequest? = null

    init {

        locationManager = myContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mSettingsClient = LocationServices.getSettingsClient(myContext)

        locationRequest = LocationRequest.create()
        locationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest?.setInterval((10 * 1000).toLong())
        locationRequest?.setFastestInterval((2 * 1000).toLong())
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)
        mLocationSettingsRequest = builder.build()

        //**************************
        builder.setAlwaysShow(true) //this is the key ingredient
        //**************************


    }

    fun turnGPSOn(requestCode:Int ) {
        Log.i(TAG," callingTurnGpsOn:: ")
        if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG," turnedOn:: 1 ")
            gpsStatus(true,requestCode)
        } else {
            mSettingsClient
                ?.checkLocationSettings(mLocationSettingsRequest)
                ?.addOnSuccessListener((myContext as Activity)) {
                    Log.i(TAG," turnedOn:: 2 ")//  GPS is already enable, callback GPS status through listener
                    gpsStatus(true,requestCode)
                }
                ?.addOnFailureListener(
                    (myContext as Activity)
                ) { e ->
                    Log.i(TAG," turnedOn:: 2 ${e}")
                    val statusCode = (e as ApiException).statusCode
                    Log.i(TAG," turnedOn:: 3 ${statusCode}")
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(myContext as Activity, requestCode)
                        } catch (sie: SendIntentException) {
                            Log.i(TAG," turnedOn:: 4 ${sie}")
                            Log.i(
                                ContentValues.TAG,
                                "PendingIntent unable to execute request."
                            )
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage = "Location settings are inadequate, and cannot be " +
                                    "fixed here. Fix in Settings."
                            Log.e(ContentValues.TAG, errorMessage)
                            Toast.makeText(myContext as Activity, errorMessage, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
        }
    }

}