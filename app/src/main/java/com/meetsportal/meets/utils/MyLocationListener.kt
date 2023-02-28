package com.meetsportal.meets.utils

import android.location.Location
import android.location.LocationListener
import android.util.Log


class MyLocationListener(var location: (Location) -> Unit) : LocationListener {

    override fun onLocationChanged(location: Location) {
        Log.i("TAG"," getting location:::  ${location.latitude} ${location.longitude}")
        location(location)
    }

}