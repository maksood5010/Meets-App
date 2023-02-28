package com.meetsportal.meets.ui.fragments.restaurantdetailfragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.CategoryFilterAdapter
import com.meetsportal.meets.adapter.CuisineFilterAdapter
import com.meetsportal.meets.adapter.FacilityFilterAdapter
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.places.CuisineResponse
import com.meetsportal.meets.networking.places.FacilityResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.PREFRENCE_CATEGORY
import com.meetsportal.meets.utils.Constant.PREFRENCE_CUISINE
import com.meetsportal.meets.utils.Constant.PREFRENCE_FACILITY
import com.meetsportal.meets.utils.PreferencesManager

class FilterFragment: BaseFragment() {

    private val TAG = FilterFragment::class.java.simpleName
    lateinit var cancel : ImageView
    lateinit var root : ConstraintLayout
    lateinit var clear : TextView
    lateinit var categoryText : TextView
    lateinit var facilityText : TextView
    lateinit var cuisineText : TextView
    lateinit var applyFilter: TextView

    lateinit var categoryRecycler : RecyclerView
    lateinit var categoryFilterAdapter : CategoryFilterAdapter
    var categoryResponse: CategoryResponse? = null

    lateinit var facilityRecycler : RecyclerView
    lateinit var facilityFilterAdapter: FacilityFilterAdapter
    var facilityResponse : FacilityResponse? = null

    lateinit var cusineRecycler : RecyclerView
    lateinit var cuisineFilterAdapter: CuisineFilterAdapter
    var cuisineResponse : CuisineResponse? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_place_filter,container,false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUP(view)
        setObserver()
    }

    private fun initView(view: View) {

        cancel = view.findViewById<ImageView>(R.id.cancel).apply {
            setOnClickListener { activity?.onBackPressed() }
        }
        clear = view.findViewById<TextView>(R.id.clear).apply {
            setOnClickListener {
                facilityFilterAdapter.clearItem()
                categoryFilterAdapter.clearItem()
                cuisineFilterAdapter.clearItem()
            }
        }
        root = view.findViewById(R.id.rootCo)
        categoryText = view.findViewById(R.id.sortByButton)
        facilityText = view.findViewById(R.id.facilityButton)
        cuisineText = view.findViewById(R.id.cusineButton)
        applyFilter = view.findViewById(R.id.apply)

        categoryRecycler = view.findViewById(R.id.categoryRecycler)
        categoryRecycler.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        facilityRecycler = view.findViewById(R.id.facilitesRecycler)
        facilityRecycler.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        cusineRecycler = view.findViewById(R.id.cusineRecyclerView)
        cusineRecycler.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)



    }

    private fun setUP(view: View) {

        root.setOnClickListener{activity?.onBackPressed()}

        setCategoryAdapter()
        setFacilityAdapter()
        setCuisineAdapter()
        setUpListener()

    }

    private fun setUpListener() {
        var facilities = ""
        var cuisines = ""
        var categories = ""
        var placeListFragment = activity?.supportFragmentManager?.findFragmentByTag(Constant.TAG_PLACE_LIST_FRAGMENT) as PlacesListFragment


        applyFilter.setOnClickListener {

            saveFilter()
            facilityResponse?.definition?.map {
                if(it.isSelected){
                    facilities = facilities.plus(it.key).plus(",")
                }
            }

            cuisineResponse?.definition?.map {
                if(it.isSelected){
                    cuisines = cuisines.plus(it.key).plus(",")
                }
            }
            categoryResponse?.definition?.map {
                if(it.isSelected){
                    categories = categories.plus(it.key).plus(",")
                }
            }

            if(facilities.length > 1) {
                facilities = facilities.substring(0, (facilities.length - 1))
            }
            if(cuisines.length >  1) {
                cuisines = cuisines.substring(0, (cuisines.length - 1))
            }
            if(categories.length > 1) {
                categories = categories.substring(0, (categories.length - 1))
            }


            Log.i(TAG," facilities:: ${facilities}")
            placeListFragment.applyFilter(facilities,cuisines,categories)
            activity?.onBackPressed()


        }

        categoryText.setOnClickListener {
            categoryRecycler.visibility = View.VISIBLE
            facilityRecycler.visibility = View.GONE
            cusineRecycler.visibility = View.GONE
            categoryText.setBackgroundColor(Color.parseColor("#ffffff"))
            facilityText.setBackgroundColor(Color.parseColor("#d3d3d3"))
            cuisineText.setBackgroundColor(Color.parseColor("#d3d3d3"))
        }

        facilityText.setOnClickListener {
            categoryRecycler.visibility = View.GONE
            facilityRecycler.visibility = View.VISIBLE
            cusineRecycler.visibility = View.GONE
            categoryText.setBackgroundColor(Color.parseColor("#d3d3d3"))
            facilityText.setBackgroundColor(Color.parseColor("#ffffff"))
            cuisineText.setBackgroundColor(Color.parseColor("#d3d3d3"))
        }

        cuisineText.setOnClickListener {
            categoryRecycler.visibility = View.GONE
            facilityRecycler.visibility = View.GONE
            cusineRecycler.visibility = View.VISIBLE
            categoryText.setBackgroundColor(Color.parseColor("#d3d3d3"))
            facilityText.setBackgroundColor(Color.parseColor("#d3d3d3"))
            cuisineText.setBackgroundColor(Color.parseColor("#ffffff"))
        }


    }

    private fun saveFilter() {
        PreferencesManager.put(facilityResponse, PREFRENCE_FACILITY)
        PreferencesManager.put(categoryResponse, PREFRENCE_CATEGORY)
        PreferencesManager.put(cuisineResponse, PREFRENCE_CUISINE)
    }

    private fun setCuisineAdapter() {
        cuisineResponse = PreferencesManager.get<CuisineResponse>(PREFRENCE_CUISINE)
        cuisineFilterAdapter = CuisineFilterAdapter(requireContext(),cuisineResponse)
        cusineRecycler.adapter = cuisineFilterAdapter
    }

    private fun setFacilityAdapter() {
        facilityResponse = PreferencesManager.get<FacilityResponse>(PREFRENCE_FACILITY)
        Log.i(TAG," facilityResponse:: ${facilityResponse}")
        facilityFilterAdapter = FacilityFilterAdapter(requireContext(),facilityResponse)
        facilityRecycler.adapter =  facilityFilterAdapter
    }

    private fun setCategoryAdapter() {
        categoryResponse = PreferencesManager.get<CategoryResponse>(PREFRENCE_CATEGORY)
        categoryFilterAdapter = CategoryFilterAdapter(requireContext(),categoryResponse)
        categoryRecycler.adapter = categoryFilterAdapter
    }

    private fun setObserver() {

    }

    override fun onBackPageCome(){

    }
}