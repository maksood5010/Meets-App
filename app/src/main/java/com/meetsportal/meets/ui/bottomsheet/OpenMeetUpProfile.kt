package com.meetsportal.meets.ui.bottomsheet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.Utils.Companion.createCustomMarker

class OpenMeetUpProfile (var ctx: Activity) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_meetup_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()

    }

    private fun initView(view: View) {


    }

    private fun setUp() {

        //Map Setting
        val mapOptions = GoogleMapOptions().apply {
            zoomControlsEnabled(false)
            zoomGesturesEnabled(false)
            ambientEnabled(false)
            rotateGesturesEnabled(false)
            tiltGesturesEnabled(false)
            scrollGesturesEnabled(false)
            compassEnabled(false)
            liteMode(true)
        }
        var mapFragment = SupportMapFragment.newInstance(mapOptions)

        mapFragment.getMapAsync(::onMapReady)
        childFragmentManager.commit {
            add(R.id.map, mapFragment)
        }


    }

    private fun onMapReady(googleMap: GoogleMap?) {

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(25.197525, 55.274288), 10f))
        val position = LatLng(25.197525, 55.274288)
        googleMap?.addMarker(
            MarkerOptions()
            .position(position)
            .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(requireContext(), R.drawable.meetup_pin_layer, "")))
        )
        googleMap?.setOnMarkerClickListener {
            MoveToMap()
            true
        }
    }

    //Move to Map with Route
    private fun MoveToMap() {
        var packageName = "com.google.android.apps.maps"
        var query = "google.navigation:q=25.197525,55.274288"
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(query))
        intent.setPackage(packageName)
        startActivity(intent)
    }
}