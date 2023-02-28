package com.meetsportal.meets.utils

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.meetsportal.meets.networking.profile.Badge

class MarkerClusterItem (private val latLng: LatLng, private val title: String?, val image : Bitmap?, var seletced:Boolean = false, var lastSeen :String?, var gotIcon:Boolean = false,val badge:String? = null,val boosted:Boolean = false) : ClusterItem {
    override fun getPosition(): LatLng {
        return latLng
    }
    fun getBadge(): Badge {
        return Utils.getBadge(badge)
    }

    override fun getTitle(): String {
        return title!!
    }

    override fun getSnippet(): String {
        return ""
    }
}