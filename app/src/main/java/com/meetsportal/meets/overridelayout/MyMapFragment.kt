package com.meetsportal.meets.overridelayout

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker

class MyMapFragment : SupportMapFragment(), GoogleMap.OnMarkerClickListener{

    override fun onMarkerClick(p0: Marker?): Boolean {
        Log.i("TAG"," onMarkerClick2::  ")
        return false
    }

}