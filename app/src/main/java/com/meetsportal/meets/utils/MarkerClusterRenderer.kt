package com.meetsportal.meets.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.socialfragment.MeetupOpenMemberMapFragment
import com.meetsportal.meets.utils.Utils.Companion.createClusterIcon


class MarkerClusterRenderer<T : ClusterItem?>(
    var myContext: Context?,
    googleMap: GoogleMap?,
    fragment:MeetupOpenMemberMapFragment,
    clusterManager: ClusterManager<T>?,
    var isItPlace : Boolean? = false,
) :
    DefaultClusterRenderer<T>(myContext, googleMap, clusterManager) {

    var clusterSize = 1
    var clusterBitmapDiscriptor :BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(createClusterIcon(myContext!!,null,0))

    val TAG  = MarkerClusterRenderer::class.simpleName

    override fun shouldRenderAsCluster(cluster: Cluster<T>): Boolean {
        Log.i("TAG", " shouldRenderAsCluster:: ${cluster.size}")
        if(isItPlace == false)
            return cluster.size >= 3
        else
            return false
        //return false
    }

    override fun onBeforeClusterItemRendered(
        item: T,
        markerOptions: MarkerOptions
    ) {
        Log.i("TAG", " onBeforeClusterItemRendered:: ${markerOptions.title}")
        val markerItem: MarkerClusterItem = item as MarkerClusterItem
        markerItem.gotIcon = false
        Log.i("TAG", " onBeforeClusterItemRendered:: ${item.title}")

        if(isItPlace ==  false){
            var view = Utils.initializeMapUserView(myContext!!, markerItem.image, false, markerItem.lastSeen, false)
            var k = view.findViewById<ImageView>(R.id.civ_image)
            Log.i("TAG"," checkingHW::::: ${k.height} ${k.width}")
            Log.i("TAG"," lastseen::  ${markerItem.lastSeen}")
            markerOptions.icon(
                BitmapDescriptorFactory.fromBitmap(Utils.createMapUserMarker(myContext!!,markerItem.image,false,markerItem.lastSeen))
            )
        }else{
            Log.i(TAG," settingMarkerForPlace::: ")
            createMapPlaceMarker(markerItem,markerOptions = markerOptions)
        }
    }



    override fun onClusterRendered(cluster: Cluster<T>, marker: Marker) {
        Log.i(TAG," onClusterRendered::: ${cluster.size}  ${clusterSize}")

        if(clusterSize != cluster.size){
            clusterSize = cluster.size
            clusterBitmapDiscriptor = BitmapDescriptorFactory.fromBitmap(createClusterIcon(myContext!!,null,cluster.size))
            marker.setIcon(clusterBitmapDiscriptor)
        }else{
            marker.setIcon(clusterBitmapDiscriptor)
        }

    }

    override fun onBeforeClusterRendered(cluster: Cluster<T>, markerOptions: MarkerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions)
        Log.i("TAG"," onBeforeClusterRendered:: ")

        if(cluster.items.contains((cluster.items.filter {
                Log.i("TAG"," onBeforeClusterRendered:: items  ${(it as MarkerClusterItem).seletced}")
                (it as  MarkerClusterItem).seletced}).elementAtOrElse(0){""})
        ) {
            Log.i("TAG"," onBeforeClusterRendered:: ripple removed")
        }
    }

    override fun onClusterItemRendered(clusterItem: T, marker: Marker) {
        //super.onClusterItemRendered(clusterItem, marker)
        Log.i("TAG"," onClusterItemRendered::: ")
        val markerItem: MarkerClusterItem = clusterItem as MarkerClusterItem
        if(marker.tag == null){
            marker.tag = markerItem.title
        }
        if(markerItem.gotIcon){
            return
        }
        markerItem.gotIcon = true

        if(isItPlace == false){
            var view = Utils.initializeMapUserView(myContext!!,markerItem.image,markerItem.seletced,markerItem.lastSeen,clusterItem.boosted)
            var k = view.findViewById<ImageView>(R.id.bg).apply {
                setImageResource(markerItem.getBadge().mapBg)
            }
            Log.i("TAG"," checkingHW::::: x${k.height} ${k.width}")
            var g = view.findViewById<ImageView>(R.id.civ_image).apply {
                Log.i("TAG"," checkingHW:: ${height} ${width}")
                setImageBitmap(markerItem.image)
            }
            marker.setIcon(
                BitmapDescriptorFactory.fromBitmap(Utils.getBitmapFromView(view,myContext!!,markerItem.image))

            )
        }else{
            createMapPlaceMarker(markerItem,marker)
        }
    }



    private fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorDrawableResourceId: Int
    ): BitmapDescriptor? {
        val background = ContextCompat.getDrawable(context, R.drawable.bg_circular_corcnor_filled2)
        background!!.setBounds(2, 2, background.intrinsicWidth, background.intrinsicHeight)
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        val image = ContextCompat.getDrawable(context, R.drawable.demo_post_image4)

        vectorDrawable!!.setBounds(
            40,
            20,
            vectorDrawable.intrinsicWidth + 40,
            vectorDrawable.intrinsicHeight + 20
        )

        image?.setBounds(
            40,
            20,
            vectorDrawable.intrinsicWidth + 40,
            vectorDrawable.intrinsicHeight + 20
        )

        val bitmap = Bitmap.createBitmap(
            200,
            200,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        vectorDrawable.draw(canvas)
        image?.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun setOnClusterItemClickListener(listener: ClusterManager.OnClusterItemClickListener<T>?) {
        super.setOnClusterItemClickListener(listener)
    }

    fun createMapPlaceMarker(markerItem: MarkerClusterItem, marker: Marker? = null,markerOptions: MarkerOptions? = null){
        Log.i(Utils.TAG," checkingnumberTime::: ${markerItem.seletced} ${markerItem.image}")
        Log.i(TAG," markerTag::: 1 ${marker?.tag}")
        var view =
            (myContext?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                R.layout.card_place_pin,
                null
            )
        var bg = view.findViewById<ImageView>(R.id.bg).apply {
            visibility = View.VISIBLE
            setImageBitmap(markerItem.image)
        }
        var time = view.findViewById<TextView>(R.id.time)
        time.text = markerItem.lastSeen
        Log.i(Utils.TAG," selected :: ${markerItem.seletced}")

        marker?.tag?.let {
            marker.setIcon(
                Utils.getBitMapfromLayout(view,myContext!!)
            )
        }


    }

}