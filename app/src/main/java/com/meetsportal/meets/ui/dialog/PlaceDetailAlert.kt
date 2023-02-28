package com.meetsportal.meets.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.KindOfPlaceAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.networking.meetup.Place
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.places.CuisineResponse
import com.meetsportal.meets.networking.places.FacilityResponse
import com.meetsportal.meets.networking.places.GetSinglePlaceResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Utils.Companion.loadImage

class PlaceDetailAlert(var activity: Activity, var places: List<Place>? = null, var customPlaces: List<AddressModel>? = null, var clicked: (String?) -> Unit) : Dialog(activity) {


    var placePosition: Int? = null

    var switcher: ViewFlipper? = null
    var image: ImageView? = null
    var rvCategory: RecyclerView? = null
    var rvCuisine: RecyclerView? = null
    var rvFacility: RecyclerView? = null
    var goToPlace: TextView? = null
    var votePlace: TextView? = null
    var address: TextView? = null
    var llShimmer: LinearLayout? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(true)
        setContentView(R.layout.custom_place_detail_layout)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        image = findViewById(R.id.image)
        rvCategory = findViewById(R.id.rvCategory)
        rvCuisine = findViewById(R.id.rv_cuisine)
        rvFacility = findViewById(R.id.rv_facility)
        goToPlace = findViewById(R.id.goToPlace)
        votePlace = findViewById(R.id.votePlace)
        address = findViewById(R.id.tv_address)
        switcher = findViewById(R.id.switcher)
        llShimmer = findViewById(R.id.llShimmer)
    }

    fun showDialog(position: Int?): Dialog? {
        //...set cancelable false so that it's never get hidden
        //...that's the layout i told you will inflate later
        placePosition = position
        switcher?.displayedChild = 2
        show()
        placePosition?.let {
            votePlace?.visibility = View.VISIBLE
        } ?: run {
            votePlace?.visibility = View.GONE
        }
        return this
    }

    fun populateView(value: GetSinglePlaceResponse?) {
        Log.i("TAG", "populateView: onSinglePlace  0")
        switcher?.displayedChild = 1
        var f = llShimmer?.layoutParams
        f?.height = 2
        llShimmer?.layoutParams = f
        image?.loadImage(activity, value?.main_image_url, showSimmer = false)
//        image?.let {
//            Utils.stickImage(activity,it,value?.main_image_url,null)
//        }

        Log.i("TAG", "populateView: onSinglePlace  1")
        var kindofPlace = PreferencesManager.get<CategoryResponse>(Constant.PREFRENCE_CATEGORY)?.definition?.filter { cat ->
            value?.kind_of_places?.forEach {
                if(cat.key.equals(it)) return@filter true
            }
            false
        }
        Log.i("TAG", "populateView: onSinglePlace  2")
        rvCategory?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rvCategory?.adapter = KindOfPlaceAdapter(activity, kindofPlace, 1)

        var cuisine = PreferencesManager.get<CuisineResponse>(Constant.PREFRENCE_CUISINE)?.definition?.filter { cuisine ->
            value?.cuisines?.forEach {
                if(cuisine.key.equals(it)) return@filter true
            }
            false
        }
        Log.i("TAG", "populateView: onSinglePlace  3")
        rvCuisine?.adapter = KindOfPlaceAdapter(activity, cuisine, 2)
        Log.i("TAG", "populateView: onSinglePlace  31 ${PreferencesManager.get<FacilityResponse>(Constant.PREFRENCE_FACILITY)?.definition}")

        var facility = PreferencesManager.get<FacilityResponse>(Constant.PREFRENCE_FACILITY)?.definition?.filter { facility ->
            value?.facilities?.forEach {
                if(facility.key.equals(it)) return@filter true
            }
            false
        }
        Log.i("TAG", "populateView: onSinglePlace  4")
        rvFacility?.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rvFacility?.adapter = KindOfPlaceAdapter(activity, facility, 3)
        address?.text = "".plus(value?.street?.en ?: "").plus(value?.city ?: "")
                .plus(value?.state ?: "").plus(value?.country ?: "")
        goToPlace?.onClick({
            hideDialog()
            var baseFragment: BaseFragment = RestaurantDetailFragment();
            Navigation.setFragmentData(baseFragment, "_id", value?._id)
            Navigation.addFragment(activity, baseFragment, RestaurantDetailFragment.TAG, R.id.homeFram, true, false)
        })
        Log.i("TAG", "populateView: onSinglePlace  5")
        votePlace?.onClick({
            placePosition?.let {
                if(places?.getOrNull(it)?.selected == true) {
                    MyApplication.showToast(activity, "Same vote!!!")
                    return@let
                }
                places?.map { it?.selected = false }
                customPlaces?.map { it.selected = false }
                places?.getOrNull(it)?.selected = true
                clicked(value?._id)
            }
            hideDialog()
        })
    }

    fun hideDialog() {
        let {
            if(it.isShowing) it.dismiss()
        }
    }

}