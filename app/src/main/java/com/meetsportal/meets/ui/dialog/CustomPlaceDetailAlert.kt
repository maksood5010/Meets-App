package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commit
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.DialogCustomPlaceDetailBinding
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.networking.meetup.Place
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.onClick

class CustomPlaceDetailAlert(var activity: Activity, var places: List<Place>? = null, var customPlaces: List<AddressModel>? = null, var customPlaceVote: (String?) -> Unit = {}) : DialogFragment(), OnMapReadyCallback {

    lateinit var mapFragment: SupportMapFragment
    var latLang: LatLng? = null

    var customPlacePostion: Int? = null

    override fun getTheme(): Int {
        return R.style.BottomSheetDialog
    }

    private var _binding: DialogCustomPlaceDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogCustomPlaceDetailBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //...set cancelable false so that it's never get hidden
        dialog?.setCancelable(true)
        dialog?.window?.setGravity(Gravity.CENTER)
        //...that's the layout i told you will inflate later
        return binding.root
    }

    /* init {
        _binding = DialogCustomPlaceDetailBinding.inflate(LayoutInflater.from(activity))
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(binding.root)
        getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
        setMap()
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setMap()
        addListener()
    }

    private fun initView() {
        customPlacePostion?.let {
            binding.votePlace.visibility = View.VISIBLE
        } ?: run {
            binding.votePlace.visibility = View.GONE
        }
        binding.goToPlace.onClick({
            latLang?.let {
                moveToMap()
            }
        })
    }

    private fun addListener() {
        binding.votePlace.onClick({
            customPlacePostion?.let {
                if(customPlaces?.getOrNull(it)?.selected == true) {
                    MyApplication.showToast(activity, "Same vote!!!")
                    dismiss()
                    return@onClick
                }
                places?.map { it?.selected = false }
                customPlaces?.map { it.selected = false }
                customPlaces?.getOrNull(it)?.selected = true
                customPlaceVote(customPlaces?.getOrNull(it)?._id)
                dismiss()
            }
        })
    }

    private fun setMap() {
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
        mapFragment = SupportMapFragment.newInstance(mapOptions)
        mapFragment.getMapAsync(::onMapReady)
        childFragmentManager.commit {
            Log.i("TAG", " checking:::: 444 ${mapFragment.isAdded}")
            if(!mapFragment.isAdded) add(R.id.map, mapFragment)
        }

    }

    fun setLatLng(ltlng: LatLng, position: Int? = null) {
        latLang = ltlng
        customPlacePostion = position
        // mapFragment.getMapAsync(::onMapReady)
    }

    override fun onMapReady(gMap: GoogleMap?) {
        if(latLang == null || gMap == null) return
        Log.d("TAG", "onMapReady:current $latLang ")
        mapSetting(gMap)

    }

    private fun mapSetting(gMap: GoogleMap) {
        var mapStyleOptions = MapStyleOptions.loadRawResourceStyle(activity, R.raw.google_style)
        Log.i("TAG", "mapSetting: checking location::: $latLang")
        gMap.setMapStyle(mapStyleOptions)
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLang, 15f))


        gMap.addMarker(MarkerOptions().position(latLang ?: LatLng(0.9, 0.9))
                .icon(Utils.bitmapDescriptorFromVector(activity, R.drawable.ic_location_marker)))
        gMap.setOnMapClickListener {
            moveToMap()
        }
        val bottomMargin = Utils.dpToPx(60f, activity?.resources)
        gMap.setPadding(0, 0, 0, bottomMargin)
    }

    private fun moveToMap() {
        Log.i("TAG", " shoeingRoot::  ")
        var packageName = "com.google.android.apps.maps"
        var query = "google.navigation:q=${latLang?.latitude},${latLang?.longitude}"
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(query))
        intent.setPackage(packageName)
        activity.startActivity(intent)
    }


}