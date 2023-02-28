package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.location.*
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.model.PlacesSearchResult
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.NearByListAdapter
import com.meetsportal.meets.adapter.PlacesAutoCompleteAdapter
import com.meetsportal.meets.databinding.FragmentInviteMeetMapBinding
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.util.*


@AndroidEntryPoint()
class InviteMeetMapFragment(val incomingFragment: BaseFragment) : BaseFragment(), OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener {

    val TAG: String =javaClass.simpleName
    private var fromMapAddress: AddressModel? = null
    private var dialog: Dialog? = null
    var city: String? = null
    var country: String? = null
    var countryCode: String? = null

    //val incomingFragment : T?

    private var shouldUpdate: Boolean = true
    private var list: Array<PlacesSearchResult>? = null
    private lateinit var nearByListAdapter: NearByListAdapter
    private lateinit var placesAutoAdapter: PlacesAutoCompleteAdapter
    private var mMapView: View? = null
    private var addressFromLatLong: AddressModel? = null
    private var locationFinder: LocationFinder? = null
    private var current: LatLng? = null
    private var currentLocation: Location? = null
    private var gMap: GoogleMap? = null
    private var _binding: FragmentInviteMeetMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    lateinit var mapFragment: SupportMapFragment

    val userAccountViewModel: UserAccountViewModel by viewModels()

    //    lateinit var mDetector: GestureDetector

    /*fun <T>initialize(`object`: T){
        fun <T> put(`object`: T, key: String) {
            Log.i(ModelPreferencesManager.TAG," key::  $key ")
            //Convert object to JSON String.
            val jsonString = GsonBuilder().create().toJson(`object`)
            //Save that String in SharedPreferences
            ModelPreferencesManager.preferences.edit().putString(key, jsonString).apply()
        }
    }*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onResume()
    }

    override fun onStop() {
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onStop()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentInviteMeetMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utils.popUpAnim(binding.imageView3, requireContext())
        initViews()
        setObserver()
        clickViews()
        getLocation()
        setUpPlacesAPI()
    }

    private fun setObserver() {
        userAccountViewModel.observeSavedAddress().observe(viewLifecycleOwner, {
            Log.d(TAG, "setObserver:working ")
            if(dialog != null) {
                dialog?.dismiss()
            }
            when(incomingFragment) {
                is MeetUpPlaceBottomFragment   -> incomingFragment?.getAddress(addressFromLatLong)
                is SavedPlaceListFragment      -> incomingFragment?.getAddress()
                is OpenMeetPlaceBottomFragment -> incomingFragment?.getAddress(addressFromLatLong)
                is AddPlaceToMeetUp -> incomingFragment?.getAddress(addressFromLatLong)
            }
            requireActivity().onBackPressed()
        })
    }

    private fun clickViews() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        binding.inCurrent.setOnClickListener {
            binding.ivCheckbox.setImageResource(R.drawable.ic_round_check)
            if(this::nearByListAdapter.isInitialized && nearByListAdapter.lastPosition != -1) {
                nearByListAdapter.lastPosition = -1
                nearByListAdapter.notifyDataSetChanged()
            } else {
                setBottomState(true, full = true)
            }
            addressFromLatLong = fromMapAddress
        }
//        binding.bottomSheet.setOnTouchListener(Utils.getTouchlistener(mDetector))
        binding.tvUseLoc.setOnClickListener {
            if(this::nearByListAdapter.isInitialized) {
                if(nearByListAdapter.lastPosition == -1) {
                    showDialog(dialog!!)
                } else {
                    callSaveAddress()
                }
            } else {
                showDialog(dialog!!)
            }
        }

        binding.ivClearEdit.setOnClickListener {
            binding.etSearch.setText("")
        }
        binding.etSearch.onText {
            setBottomState(false)
            if(it.isEmpty()) {
                binding.ivClearEdit.visibility = View.GONE
            } else {
                binding.ivClearEdit.visibility = View.VISIBLE
            }
//            if (it.length > 1)
            placesAutoAdapter.filter.filter(it)
        }

    }

    fun showDialog(dialog: Dialog): Dialog {
        val etName: EditText = dialog.findViewById(R.id.etName)
        Log.d(TAG, "addressFromLatLong set: 7: ${addressFromLatLong?.name}")
        etName.setText(addressFromLatLong?.name)
        dialog.findViewById<ImageView>(R.id.cancel)?.apply {
            setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.findViewById<TextView>(R.id.tvAdd)?.apply {
            setOnClickListener {
                if(!etName.text.toString().isEmpty()) {
                    addressFromLatLong?.name = etName.text.toString().trim()
                    callSaveAddress()
                } else {
                    showToast("Please enter a name")
                }
            }
        }
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
        return dialog
    }

    private fun callSaveAddress() {
        Log.d(TAG, "setObserver :addressFromLatLong!=null ${addressFromLatLong?.country} ")
        if(addressFromLatLong != null) {
            if(addressFromLatLong?.image_url != null) {
                addressFromLatLong?.country = country
                addressFromLatLong?.country_code = countryCode
                addressFromLatLong?.city = city
            }
            userAccountViewModel.saveAddress(addressFromLatLong!!)
        }
    }


    private fun setBottomState(collapse: Boolean, full: Boolean = false) {
        Log.d(TAG, "setBottomState:shouldUpdate $collapse, $full")
        if(collapse) {
            if(!full) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpPlacesAPI() {
        placesAutoAdapter = PlacesAutoCompleteAdapter(requireContext(), mutableListOf()) {
            addressFromLatLong = getLatLong(it) ?: return@PlacesAutoCompleteAdapter
            Log.d(TAG, "addressFromLatLong set: 2: ${addressFromLatLong?.name}")
            addressFromLatLong?.let { add: AddressModel ->
                gMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(add.getLatitude(), add.getLongitude()), 15f))
            }
            binding.etSearch.setText("")
            binding.etSearch.clearFocus()
            hideKeyboard(binding.etSearch)
            fromMapAddress = addressFromLatLong
//            setText()
//            shouldUpdate = false
//            setBottomState(true)
        }
        binding.rvAutoComplete.adapter = placesAutoAdapter
    }

    private fun getLatLong(model: AddressModel): AddressModel? {
        val geocoder = Geocoder(requireContext())
        val addresses: List<Address>?

        return try {
            addresses = geocoder.getFromLocationName(model._id, 1)
            if(addresses != null && !addresses.isEmpty()) {
                val address = addresses[0]
                val addressLine = address.getAddressLine(0)
                model.address = addressLine
                model.setLocation(address.latitude,address.longitude)
//                model.lat = address.latitude
//                model.lon = address.longitude
                model.country_code = address.countryCode
//                val name = address.featureName
//                val street = address.thoroughfare
//                val locality = address.subLocality
//                if (name != null) {
//                    model.name = name
//                } else if (street != null) {
//                    model.name = street
//                } else if (locality != null) {
//                    model.name = locality
//                }
                model
            } else {
                showToast("Sorry, Service not Available Try again later")
                null
            }
        } catch(e: Exception) {
            e.printStackTrace()
            showToast("Sorry, Service not Available")
            null
        }
    }

    private fun initViews() {
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mMapView = mapFragment.view
        mapFragment.getMapAsync(this)


        dialog = Dialog(requireContext())
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setContentView(R.layout.custom_name_layout)

//        val mapOptions = GoogleMapOptions().apply {
//            zoomControlsEnabled(false)
//            zoomGesturesEnabled(false)
//            ambientEnabled(false)
//            rotateGesturesEnabled(false)
//            tiltGesturesEnabled(false)
//            scrollGesturesEnabled(false)
//            compassEnabled(false)
//            liteMode(true)
//        }
//        mapFragment = SupportMapFragment.newInstance(mapOptions)
//        childFragmentManager.commit {
//            add(R.id.map2, mapFragment)
//        }
//        mMapView = mapFragment.view
    }

    override fun onMapReady(gMap: GoogleMap?) {
        if(current == null || gMap == null) return
        this.gMap = gMap
        locationButtonRule()
        mapUI(gMap)
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f))
        gMap.setOnCameraMoveStartedListener(this)
//        addressFromLatLong = Utils.getAddressFromLatLong(requireContext(), current!!.latitude, current!!.longitude)
//        if(addressFromLatLong==null){
//
//        }
        DoAsync({
            addressFromLatLong = getAddressFromLatLong(current!!.latitude, current!!.longitude)
            Log.d(TAG, "addressFromLatLong set: 3: ${addressFromLatLong?.name}")
        }, {
            fromMapAddress = addressFromLatLong
            Log.d(TAG, "addressFromLatLong set: 4: ${addressFromLatLong?.name}")
            setText()
        })
        gMap.setOnCameraIdleListener {
            val lat = gMap.cameraPosition.target.latitude
            val lng = gMap.cameraPosition.target.longitude
//            addressFromLatLong = Utils.getAddressFromLatLong(requireContext(), lat, lng)
            DoAsync(
                    {
                        addressFromLatLong = getAddressFromLatLong(lat, lng)
                        Log.d(TAG, "addressFromLatLong set: 5: ${addressFromLatLong?.name}")
                    },
                    {
                        fromMapAddress = addressFromLatLong
                        if(addressFromLatLong != null) {
                            setText()
                            shouldUpdate = true
                        }
                    },
                   )
            DoAsync({
                Log.i(TAG, "PlacesSearchResult: -1 ")
                val near = NearBySearch()
                near.setValues(getString(R.string.GOOGLE_API_KEY), com.google.maps.model.LatLng(lat, lng), "en")
                val placesSearchResults: Array<PlacesSearchResult>? = near.run().results
                Log.i(TAG, "PlacesSearchResult: 1 $placesSearchResults")
                list = placesSearchResults

            }, {
                Log.i(TAG, "PlacesSearchResult: 0 $list")
                nearByListAdapter = NearByListAdapter(requireContext(), list) {
                    binding.ivCheckbox.setImageResource(R.drawable.bg_black_stroke_25dp)
                    addressFromLatLong = it
                    Log.d(TAG, "addressFromLatLong set: 6: ${addressFromLatLong?.name}")
                }
                binding.rvNearBy.adapter = nearByListAdapter
                binding.inCurrent.callOnClick()
                binding.shimmer.stopShimmer()
                binding.shimmer.visibility = View.GONE
            })
        }
    }

    private fun mapUI(gMap: GoogleMap) {
        Utils.checkPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        gMap.isMyLocationEnabled = true
        var mapStyleOptions = MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.google_style)
        gMap.setMapStyle(mapStyleOptions)
        gMap.uiSettings.isCompassEnabled = false
    }

    private fun locationButtonRule() {
        val locationButton = (mMapView!!.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(Integer.parseInt("2"))
        val rlp = locationButton.layoutParams as RelativeLayout.LayoutParams
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
        rlp.setMargins(200, 150, 200, 200)
    }

    private fun setText() {
        if(fromMapAddress != null) {
            if(shouldUpdate) {
                binding.tvCurrentName.text = fromMapAddress?.name
                binding.tvCurrentAddress.text = fromMapAddress?.address
            }
        }
    }

    private fun getLocation() {


        locationFinder = LocationFinder(requireActivity(), object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    it.locations.map {
                        it?.let {
                            currentLocation = it
                            if(currentLocation != null) {
                                current = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                            }
                            mapFragment.getMapAsync(this@InviteMeetMapFragment)
                            Log.d(TAG, "onLocationResult: ${current != null} , ${currentLocation != null}")
//                            val cameraPosition =
//                                CameraPosition.Builder().target(LatLng(it.latitude, it.longitude))
//                                    .tilt(90f)
//                                    .zoom(15f).bearing(0f).build()
//
//                            gMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                            locationFinder?.mFusedLocationClient?.removeLocationUpdates(this)
                        }
                    }
                }
            }
        })

        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val bestProvider = locationManager.getBestProvider(Criteria(), true)
        if(Utils.checkPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return
            currentLocation = locationManager.getLastKnownLocation(bestProvider!!) ?: return
            if(currentLocation != null) {
                current = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
            }
        }
    }

    class DoAsync(val handler: () -> Unit, val onPost: () -> Unit = { }) : AsyncTask<Void, Void, Void>() { init {
        execute()
    }

        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }

        override fun onPostExecute(result: Void?) {
            onPost()
        }

    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onCameraMoveStarted(p0: Int) {
        setBottomState(false)
    }

    override fun onBackPageCome() {

    }

    fun getAddressFromLatLong(latitude: Double?, longitude: Double?): AddressModel? {
        val geocoder = Geocoder(requireActivity(), Locale.UK)
        var addresses: List<Address>? = null
        val model = AddressModel()
        try {
            addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1)
        } catch(e: IOException) {
            Log.d(TAG, "getAddressFromLatLong: " + e.localizedMessage)
            Toast.makeText(requireActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show()
        }
        return if(addresses != null && !addresses.isEmpty()) {
            val address: Address = addresses[0]
            val addressLine: String = address.getAddressLine(0)
            Log.d(TAG, "getAddressFromLatLong: addressLine : $addressLine")
            val adminArea: String? = address.adminArea
            val locality: String? = address.subLocality
            val street: String? = address.thoroughfare
            model.address = addressLine
            model.setLocation(latitude,longitude)
//            model.lat = latitude
//            model.lon = longitude
            val name = address.featureName
            if(name != null) {
                model.name = name
            } else if(street != null) {
                model.name = street
            } else if(locality != null) {
                model.name = locality
            }
            if(adminArea != null) {
                city = adminArea
            } else if(locality != null) {
                city = locality
            }
            Log.d(TAG, "getAddressFromLatLong: city $city")
            country = address.countryName
            countryCode = address.countryCode
            model.city = city
            model.country = country
            model.country_code = countryCode
            model
        } else {
            null
        }
    }

}
