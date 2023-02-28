package com.meetsportal.meets.ui.fragments.restaurantdetailfragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.KindOfPlaceAdapter
import com.meetsportal.meets.adapter.RestaurantDetailPhotoAdapter
import com.meetsportal.meets.adapter.ReviewAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentRestaurantDetailBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.places.*
import com.meetsportal.meets.networking.places.Gallery
import com.meetsportal.meets.ui.activities.HomeActivityNew
import com.meetsportal.meets.ui.dialog.CheckInUserAlert
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.ImageSamplingFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpVotingFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.TAG_PLACE_ADD_REVIEW
import com.meetsportal.meets.viewmodels.PlacesViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RestaurantDetailFragment : BaseFragment(), OnMapReadyCallback {



    val placeViewModel: PlacesViewModel by viewModels()

    lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    var isShareble : Boolean? = true

    lateinit var gpsUtils: GpsUtils
    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null

    var checkInAlert: CheckInUserAlert? = null

    var lat: Double? = null
    var long: Double? = null

    var placeID : String? = null
    val isItElastic : Boolean by lazy { !Navigation.getFragmentData(this, "isItElastic").equals("") }

    companion object{
        val TAG = RestaurantDetailFragment::class.java.simpleName!!
    }

    private var _binding: FragmentRestaurantDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRestaurantDetailBinding.inflate(inflater, container, false)
        return binding.root
        //return inflater.inflate(R.layout.fragment_restaurant_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setUP()
        setObserver()
    }

    private fun initView() {
        binding.real.visibility = View.GONE
        gpsUtils = GpsUtils(requireActivity()) { it, statusCode ->
            Log.i(TAG, " gpsOnhoova::: $it")
            startGetLocation()
        }
        locationManager = getSystemService(requireContext(), LocationManager::class.java)
        //if(requireActivity().supportFragmentManager.findFragmentByTag())
//        binding.collapse.root.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
//            if(Math.abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
//                //  Collapsed
//            } else {
//                //Expanded
//            }
//        })

    }


    private fun setUP() {

        binding.detail.rvKindofPlaces.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.detail.rvFacility.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.detail.rvCuisine.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.detail.map.setOnClickListener {
            if (lat != null && long != null) {
                Log.i(TAG, " checking its comint or not ::: ")
                //MoveToMap()
            }
        }

        binding.llDirection.setOnClickListener {
            if (lat != null && long != null)
                moveToMap()
        }

        Log.i(TAG, " retaurantId::: ${Navigation.getFragmentData(this, "_id")}")

        placeViewModel.getPlace(Navigation.getFragmentData(this, "_id"),if(isItElastic) "elastic" else null)
//        placeViewModel.getPlace(Navigation.getFragmentData(this, "_id"),if(isItElastic) "elastic" else null)

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

        checkInAlert = CheckInUserAlert(requireActivity()) {it, isShareble->
            this.isShareble = isShareble
            gpsUtils.turnGPSOn(1000)
        }
        setReviewRecyclerView()

    }

    private fun setReviewRecyclerView() {
        binding.detail.recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        // rvReview.adapter = TempRecyclerViewAdapter(requireContext(), R.layout.review_item_layout)
    }

    private fun setCollapsingToolbarContent(value: GetSinglePlaceResponse?) {
        Log.i(TAG, "setCollapsingToolbarContent: 1")
        binding.collapse.tvTitle.text = value?.name?.en
        Log.i(TAG, "setCollapsingToolbarContent: 44")
        val sb = StringBuilder().apply {
            Log.i(TAG, "setCollapsingToolbarContent: 42")
            if(value?.timings?.isNotEmpty() == true){
                Log.i(TAG, "setCollapsingToolbarContent: 47")
                append(value?.timings?.get(0)?.opentime)
                append("-")
                append(value?.timings?.get(0)?.closetime)
                Log.i(TAG, "setCollapsingToolbarContent: 45")
            }
        }
        Log.i(TAG, "setCollapsingToolbarContent: 2")
        if(value?.timings?.isNotEmpty() == true) {
            value?.timings?.get(0)?.opentime?.let {
                binding.collapse.tvTiming.text = sb.toString()
            } ?: run {
                binding.collapse.tvTiming.text = ""
            }
        }

        value?.rating?.let { num ->
            binding.collapse.rbRating.rating = num.toFloat()
            Log.i(TAG, "  rating:: ${num}")
            binding.collapse.tvRate.text = num.toString().plus(" of 5")
        } ?: run {
            Log.i(TAG, " rateNull ")
            binding.collapse.rbRating.rating = 0f
            binding.collapse.tvRate.visibility = View.GONE
            binding.collapse.tvRate.text = ""
        }
        value?.featured_image_url?.let {
            Utils.stickImage(requireActivity(), binding.collapse.background, it, null)
        }?:run{
            Utils.stickImage(requireActivity(), binding.collapse.background, value?.main_image_url, null)
        }

        Log.i(TAG," valueOfBookmark:: ${value?.bookmarked}")
        when (value?.bookmarked) {
            true -> binding.collapse.ivBookmark.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_bookmarked
                )
            )
            false -> binding.collapse.ivBookmark.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_not_bookmarked
                )
            )
        }

        Utils.onClick(binding.collapse.ivBookmark, 500) {
            if (value?.bookmarked == true) {
                placeViewModel.deleteBookmark(placeID)
                value.bookmarked = false
                binding.collapse.ivBookmark.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_not_bookmarked
                    )
                )
            } else {
                placeViewModel.addBookmark(placeID)
                value?.bookmarked = true
                binding.collapse.ivBookmark.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_bookmarked
                    )
                )
            }

        }


        val sbAddress = StringBuilder()
        value?.street?.en?.let { street ->
            sbAddress.append(street)
            sbAddress.append(" ")
        }
        value?.city?.let {

            sbAddress.append(it)
            sbAddress.append(" ")
        }

        value?.state?.let {
            sbAddress.append(it)
            sbAddress.append(" ")
        }
        value?.country?.let {
            sbAddress.append(it)
            sbAddress.append(" ")
        }
        binding.detail.address.text = sbAddress.toString()
        Log.i(TAG, " somethingFInished:: 1 ")
        binding.collapse.llCreateMeetUp.onClick({
            Log.i(TAG," trackplaceId:::: 1 ${value?._id}")
            var baseFragment: BaseFragment = MeetUpViewPageFragment.getInstance(value?._id,true)
            Navigation.addFragment(
                requireActivity(),
                baseFragment,
                MeetUpViewPageFragment.TAG,
                R.id.homeFram,
                true,
                true
            )
        },1500)
        var createMeetUpFrag = activity?.supportFragmentManager?.findFragmentByTag(MeetUpViewPageFragment.TAG)
        var meetUpVotingFrag = activity?.supportFragmentManager?.findFragmentByTag(MeetUpVotingFragment.TAG)
        if(createMeetUpFrag != null || meetUpVotingFrag != null)     {
            binding.collapse.llCreateMeetUp.alpha = 0.0f
            binding.collapse.llCreateMeetUp.onClick({})
        }


        /*activity?.supportFragmentManager?.findFragmentByTag(MeetUpVotingFragment.TAG)?.let {
            binding.collapse.llCreateMeetUp.alpha = 0.0f
            binding.collapse.llCreateMeetUp.onClick({})
        }*/


        Log.i(TAG, "setCollapsingToolbarContent: 12")

    }

    private fun setOtherContent(value: GetSinglePlaceResponse?) {

        if (value?.unique_check_in_count == 0) {
            binding.detail.checkedCount.visibility = View.GONE
        } else if (value?.unique_check_in_count == 1) {
            binding.detail.checkedCount.text =
                value?.unique_check_in_count?.toString().plus(" person checked till date")
        } else {
            binding.detail.checkedCount.text =
                value?.unique_check_in_count?.toString().plus(" people checked till date")
        }

        binding.detail.tvSeeAllPhoto.setOnClickListener {
            openImageSlider(value, 0, value?.gallery)
        }


        Log.i(TAG, " settingOthercontent::: ")
        value?.description?.let { desc -> binding.detail.tvDescription.text = desc.en }
        binding.detail.tvDescription.setOnClickListener {
            if (binding.detail.tvDescription.maxLines == Integer.MAX_VALUE) {
                binding.detail.tvDescription.setMaxLines(2)
            } else {
                binding.detail.tvDescription.setMaxLines(Integer.MAX_VALUE)
            }
        }
        binding.llBookTable.visibility = View.GONE
        binding.llBookTable.setOnClickListener {
            Navigation.addFragment(
                requireActivity(),
                BookTableFragment(),
                "bookRestaurantTable",
                R.id.homeFram,
                true,
                false
            )
        }


        binding.llCheckIn.setOnClickListener {
            checkInAlert?.showDialog(value)
        }

        binding.detail.seeAllReview.onClick ({

            Log.i(TAG, " seeAllclicked::: ")
            var baseFragment: BaseFragment = AllReview()
            Navigation.setFragmentData(baseFragment, "placeId", value?._id)
            Navigation.setFragmentData(baseFragment, "placeName", value?.name?.en)
            Navigation.addFragment(
                requireActivity(),
                baseFragment,
                TAG_PLACE_ADD_REVIEW,
                R.id.homeFram,
                true,
                false
            )
        })

        binding.llAddReview.onClick( {
            var baseFragment: BaseFragment = AddReview()
            Navigation.setFragmentData(baseFragment, "placeName", value?.name?.en)
            Navigation.setFragmentData(baseFragment, "id", value?._id)
            Navigation.addFragment(
                requireActivity(),
                baseFragment,
                TAG_PLACE_ADD_REVIEW,
                R.id.homeFram,
                true,
                false
            )
        })

        value?.phone?.let { number ->
            if(number.isNotEmpty()){
                binding.detail.tvPhone.onClick( {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:".plus(number))
                    startActivity(intent)
                })
            }else{
                binding.detail.tvPhone.visibility = View.GONE
            }
        } ?: run {
            binding.detail.tvPhone.visibility = View.GONE
        }
        if(value?.email?.isBlank()==true) {
            binding.detail.tvEmail.visibility = View.GONE
        }
        value?.email?.let { emailId ->
            binding.detail.tvEmail.onClick( {
                Log.i(TAG, " emailCLICKED::: ")
                try{
//                    val intent = Intent(Intent.ACTION_SEND)
//                    intent.type = "plain/text"
//                    intent.data = Uri.parse("mailto:")
//                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailId))
//                    intent.putExtra(Intent.EXTRA_SUBJECT, "subject")
//                    intent.putExtra(Intent.EXTRA_TEXT, "mail body")
//                    startActivity(Intent.createChooser(intent, ""))
//                    startActivity(intent)
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:") // only email apps should handle this

                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailId))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Enquiry")
                    startActivity(intent)
                }catch(ex: ActivityNotFoundException){
                    showToast("No email app found")
                }
            })
        } ?: run {
            binding.detail.tvEmail.visibility = View.GONE
        }


        var kindofPlace =
            PreferencesManager.get<CategoryResponse>(Constant.PREFRENCE_CATEGORY)?.definition?.filter { cat ->
                value?.kind_of_places?.forEach {
                    if (cat.key.equals(it))
                        return@filter true
                }
                false
            }

        if (kindofPlace == null || kindofPlace.isEmpty())
            binding.detail.places.visibility = View.GONE
        else
            binding.detail.rvKindofPlaces.adapter = KindOfPlaceAdapter(requireActivity(), kindofPlace, 1)

        var cuisine =
            PreferencesManager.get<CuisineResponse>(Constant.PREFRENCE_CUISINE)?.definition?.filter { cuisine ->
                value?.cuisines?.forEach {
                    if (cuisine.key.equals(it))
                        return@filter true
                }
                false
            }


        if (cuisine == null || cuisine.isEmpty())
            binding.detail.cuisine.visibility = View.GONE
        else
            binding.detail.rvCuisine.adapter = KindOfPlaceAdapter(requireActivity(), cuisine, 2)

        var facility =
            PreferencesManager.get<FacilityResponse>(Constant.PREFRENCE_FACILITY)?.definition?.filter { facility ->
                value?.facilities?.forEach {
                    if (facility.key.equals(it))
                        return@filter true
                }
                false
            }

        if (facility == null || facility.isEmpty())
            binding.detail.facilities.visibility = View.GONE
        else
            binding.detail.rvFacility.adapter = KindOfPlaceAdapter(requireActivity(), facility, 3)


    }

    private fun setPhotoRecyclerView(value: GetSinglePlaceResponse?) {


        Log.i(TAG, " attachingImageToGallary:: ")
        binding.detail.rvPhoto.layoutManager = LinearLayoutManager(
            binding.detail.rvPhoto.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        if(value?.gallery?.size?.compareTo(0) == 0){
            binding.detail.rvPhoto.visibility = View.GONE
            binding.detail.tvSeeAllPhoto.visibility = View.GONE
            binding.detail.photo.visibility = View.GONE
        }else{
            binding.detail.rvPhoto.adapter = RestaurantDetailPhotoAdapter(
                requireActivity(),
                ArrayList(value?.gallery)
            ) {
                openImageSlider(value, it, value?.gallery)
            }
        }
        Log.i(TAG, " attachingImageToGallary:: 111 ")

        binding.detail.rvPhoto.adapter?.notifyDataSetChanged()

    }

    private fun setMenuRecyclerView(value: GetSinglePlaceResponse?) {
        binding.detail.menuSeeAll.onClick({
            if(!value?.menus.isNullOrEmpty()){
                openImageSlider(value, 0, value?.menus)
            }
        })

        binding.detail.rvMenu.layoutManager = LinearLayoutManager(
            binding.detail.rvPhoto.context,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.detail.rvMenu.adapter = RestaurantDetailPhotoAdapter(
            requireActivity(),
            ArrayList(value?.menus)
        ) {
            openImageSlider(value, it, value?.menus)
        }
        binding.detail.rvMenu.adapter?.notifyDataSetChanged()

    }

    fun openImageSlider(value: GetSinglePlaceResponse?, position: Int, list: List<Gallery>?) {
        Log.i(TAG, " MenuItemClicked:: ")
        var fragment: BaseFragment = ImageSamplingFragment() {
            if (it == true)
                activity?.onBackPressed()
        }
        var bundle: Bundle = Bundle()
        bundle.putSerializable("media", java.util.ArrayList<String>().apply {
            list?.map { add(it.imageUrl!!) }
        })
        bundle.putInt("position", position)
        fragment.arguments = bundle
        Navigation.addFragment(
            requireActivity(),
            fragment,
            "ImageSamplingFragment",
            R.id.homeFram,
            true,
            false,
            exitAnim = R.anim.slide_out_bottom
        )
    }

    fun setMapContent(value: GetSinglePlaceResponse?) {
        lat = value?.location?.coordinates?.get(1)
        long = value?.location?.coordinates?.get(0)
        if (lat != null && long != null) {
            Log.i(TAG," checking:::: 111")
            mapFragment.getMapAsync(::onMapReady)
            childFragmentManager.commit {
                Log.i(TAG," checking:::: 444 ${mapFragment.isAdded}")
                if(!mapFragment.isAdded)
                    add(R.id.map, mapFragment)
            }
            Log.i(TAG," checking:::: 222")
        } else {
            Log.i(TAG," checking:::: 333")
            binding.detail.map.visibility = View.GONE
            /*thisView.findViewById<FrameLayout>(R.id.map).apply {
                visibility = View.GONE
            }*/
        }
    }

    fun setReview(value: GetSinglePlaceResponse?) {
        if (value?.latest_reviews?.isNotEmpty() == true) {
            value?.latest_reviews?.let {
                binding.detail.recyclerView.adapter = ReviewAdapter(requireActivity(), PlaceReviewList().apply { addAll(it)}) {}
            }
        } else {
            binding.detail.reviewlay.visibility = View.GONE
        }
    }

    private fun setObserver() {
        placeViewModel.observeSinglePlace().observe(viewLifecycleOwner, Observer {
            when (it) {
                is ResultHandler.Success -> {
                    Log.i(TAG," SinglePlceResut::: ${it.value}")
                    placeID = it.value?._id
                    binding.real.visibility = View.VISIBLE
                    binding.shimmerLayout.visibility = View.GONE
                    Log.i(TAG, " getPlaceResponse:: ${it.value?._id}")
                    Log.i(TAG," checking::: 0")
                    setCollapsingToolbarContent(it.value)
                    Log.i(TAG," checking::: 1")
                    setOtherContent(it.value)
                    Log.i(TAG," checking::: 2")
                    setPhotoRecyclerView(it.value)
                    Log.i(TAG," checking::: 3")
                    if (it.value?.menus?.size == 0) {
                        Log.i(TAG," checking::: 4")
                        binding.detail.menuGroup.visibility = View.GONE
                    } else {
                        Log.i(TAG," checking::: 5")
                        setMenuRecyclerView(it.value)
                    }
                    Log.i(TAG," checking::: 6")
                    setReview(it.value)
                    setMapContent(it.value)
                    Log.i(TAG," checking::: 7")

                    Log.i(TAG," checking::: 8")

                }
            }
        })

        placeViewModel.observeCheckIn().observe(viewLifecycleOwner, Observer {
            when (it) {
                is ResultHandler.Success -> {
                    //Toast.makeText(requireContext(), "Successfully checkedIn", Toast.LENGTH_SHORT).show()
                    //Toast.makeText(requireContext(),"Now Let’s show the world where you checked in",Toast.LENGTH_SHORT).show()
                    //Toast.makeText(requireContext(),"Voila! Now go see the place on your timeline",Toast.LENGTH_SHORT).show()
                    if (isShareble==true){
                        MyApplication.showToast(requireActivity(), "Now Let’s show the world where you checked in")
                        MyApplication.showToast(requireActivity(), "Voila! Now go see the place on your timeline")
                    }else{
                        showToast("Voila! Check-in Successful!!")
                    }
                }
                is ResultHandler.Failure -> {
                    Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        placeViewModel.observeAddBookMarked().observe(viewLifecycleOwner, Observer {
            when (it) {
                is ResultHandler.Success -> {
                    Toast.makeText(requireContext(), "Bookmarked", Toast.LENGTH_SHORT).show()
                }
                is ResultHandler.Failure -> {
                    if (it.code.equals("errors.bookmark.alreadyexists")) {
                        Toast.makeText(requireContext(), "Already Bookmarked", Toast.LENGTH_SHORT)
                            .show()
                        binding.collapse.ivBookmark.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_bookmarked
                            )
                        )
                    }
                }
            }
        })

        placeViewModel.observeDeleteBookMarked().observe(viewLifecycleOwner, Observer {
            when (it) {
                is ResultHandler.Success -> {
                    Toast.makeText(requireContext(), "Bookmark removed", Toast.LENGTH_SHORT).show()
                }
                is ResultHandler.Failure -> {
                    if (it.code.equals("errors.generic.notfound")) {
                        Toast.makeText(
                            requireContext(),
                            "Already Bookmark removed",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.collapse.ivBookmark.setImageDrawable(
                            ContextCompat.getDrawable(
                                requireContext(),
                                R.drawable.ic_bookmarked
                            )
                        )
                    }
                }
            }
        })
    }

    //Map Ready
    override fun onMapReady(p0: GoogleMap?) {
        if (p0 != null) {
            var mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.google_style
            )
            p0.setMapStyle(mapStyleOptions)
            mMap = p0

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat!!, long!!), 10f))
            val position = LatLng(lat!!, long!!)
            mMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .icon(
                        Utils.bitmapDescriptorFromVector(
                            requireContext(),
                            R.drawable.ic_location_marker
                        )
                    )
            )
            /*mMap.setOnCircleClickListener {
                Log.i(TAG," setOnCircleClickListener:: ")
                MoveToMap()
            }*/
            mMap.setOnMapClickListener {
                moveToMap()
            }
            /*mMap.setOnMarkerClickListener {
                MoveToMap()
                true
            }*/
        }
    }

    private fun moveToMap() {
        Log.i(TAG, " shoeingRoot::  ")
        var packageName = "com.google.android.apps.maps"
        var query = "google.navigation:q=${lat},${long}"
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse(query))
        intent.setPackage(packageName)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, " onActivityResult:::::::  00 ${requestCode} ${resultCode} ")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == Constant.GPS_REQUEST) {
            startGetLocation()
        }
    }

    private fun startGetLocation() {
        locationListener = MyLocationListener() {
            placeViewModel.checkInPlace(placeID, it.latitude, it.longitude,isShareble)
            removeLocationListener()
        }
        if(Utils.checkPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            showToast("Let us Check you in to this place...")
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000L, 10f, locationListener!!
            )
        }
    }

    private fun removeLocationListener() {
        locationManager?.removeUpdates(locationListener!!)
        //locationManager = null
    }

    override fun onResume() {
        binding.shimmerLayout.startShimmer()
        super.onResume()
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onPause() {
        binding.shimmerLayout.stopShimmer()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onBackPageCome(){
        placeViewModel.getPlace(Navigation.getFragmentData(this, "_id"),if(isItElastic) "elastic" else null)
    }


}