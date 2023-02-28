package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.FragmentShowMapBinding
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Utils

class ShowMapFragment : BaseFragment(), OnMapReadyCallback {

    private var mMapView: View? = null
    private var _binding: FragmentShowMapBinding? = null
    private val binding get() = _binding!!
    lateinit var mapFragment: SupportMapFragment
    private var current: LatLng? = null

    companion object {

        fun getInstance(lat: Double?, lon: Double?): ShowMapFragment {
            return ShowMapFragment().apply {
                arguments = Bundle().apply {
                    putDouble("lat", lat ?: 0.0)
                    putDouble("lon", lon ?: 0.0)
                }
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentShowMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val lat = arguments?.getDouble("lat") ?: 0.0
        val lon = arguments?.getDouble("lon") ?: 0.0
        current = LatLng(lon, lat)
        binding.ivBack.setOnClickListener {
            activity?.onBackPressed()
        }

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mMapView = mapFragment.view
        mapFragment.getMapAsync(this)
    }

    override fun onBackPageCome() {

    }

    override fun onMapReady(gMap: GoogleMap?) {
        if(current == null || gMap == null) return
        Log.d("TAG", "onMapReady:current $current ")
//        Toast.makeText(requireContext(), "onMapReady", Toast.LENGTH_SHORT).show()
        mapUI(gMap)
        gMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15f))
        val marker = MarkerOptions()
        marker.position(LatLng(current?.latitude!!, current?.longitude!!))
        gMap.addMarker(marker)
    }

    private fun mapUI(gMap: GoogleMap) {
        if(Utils.checkPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            gMap.isMyLocationEnabled = true
        }
        var mapStyleOptions = MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.google_style)
        gMap.setMapStyle(mapStyleOptions)
        gMap.uiSettings.isCompassEnabled = false
    }
}