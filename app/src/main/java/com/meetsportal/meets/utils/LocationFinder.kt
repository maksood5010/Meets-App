package com.meetsportal.meets.utils

import android.Manifest
import android.app.Activity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices


class LocationFinder(var myActivity : Activity,locationCallback:LocationCallback? ,interval : Long = 20){

    var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null

    init {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(myActivity);
        locationRequest = LocationRequest.create();
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest?.interval = interval * 1000;
        this.locationCallback =locationCallback
        Utils.checkPermission(myActivity,Manifest.permission.ACCESS_FINE_LOCATION)
        mFusedLocationClient?.requestLocationUpdates(locationRequest,locationCallback,null)

    }

    fun setNewInterval(interval: Long){
        locationRequest = LocationRequest.create();
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest?.interval = interval * 1000;
        locationRequest?.fastestInterval = (interval-10)*1000
        Utils.checkPermission(myActivity,Manifest.permission.ACCESS_FINE_LOCATION)
        mFusedLocationClient?.requestLocationUpdates(locationRequest,locationCallback,null)

    }
}

