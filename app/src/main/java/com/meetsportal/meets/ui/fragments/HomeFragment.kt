package com.meetsportal.meets.ui.fragments

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.BestMeetInitCrousalAdapter
import com.meetsportal.meets.adapter.HomeBestJointAdapter
import com.meetsportal.meets.adapter.HomeCategoriesAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.places.Category
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.places.FetchPlacesResponse
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.PlacesListFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.ui.fragments.socialfragment.ChatDashboardFragment
import com.meetsportal.meets.ui.fragments.socialfragment.NotificationFragment
import com.meetsportal.meets.ui.fragments.socialfragment.SavedPlaceListFragment
import com.meetsportal.meets.ui.fragments.socialfragment.SearchFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.PlacesViewModel
import com.synnapps.carouselview.CarouselView
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private val TAG = HomeFragment::class.java.simpleName
    val  placeViewModel: PlacesViewModel by viewModels()

    var carousalAdapter : BestMeetInitCrousalAdapter? = null
    var categoryAdapter : HomeCategoriesAdapter? = null
    var nearByRestaurantAdapter : HomeBestJointAdapter? = null

    var bestMeetUpList  = FetchPlacesResponse()
    var categoryList : CategoryResponse = CategoryResponse()
    var nearByPlacesList = FetchPlacesResponse()

    lateinit var carousalRV: RecyclerView
    lateinit var categoryRV:RecyclerView
    lateinit var closeByRestaurantRV:RecyclerView
    lateinit var carousal:CarouselView
    lateinit var shimmer :ShimmerFrameLayout
    lateinit var real :ConstraintLayout
    lateinit var seeAllMeetUp : TextView
    lateinit var closebyCount : TextView
    lateinit var seeAllcloseBy : TextView
    var isHomeSetUp = false



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        //setUp()
        clickToolBar(view)

        addObserver()
    }

    fun clickToolBar(view: View){
        view.findViewById<ImageView>(R.id.iv_chat).setImageResource(R.drawable.ic_bookmark)
        val noti=view.findViewById<ImageView>(R.id.notification)
        noti.setImageResource(R.drawable.ic_search)
        noti.setColorFilter(ContextCompat.getColor(requireContext(), R.color.black))
        view.findViewById<RelativeLayout>(R.id.rl_chat).onClick({
            MyApplication.putTrackMP(Constant.AcPlacesSavedFloating, null)
            Navigation.addFragment(requireActivity(), SavedPlaceListFragment(), "SavedPlaceFragment", R.id.homeFram, true, false)
        })
        view.findViewById<RelativeLayout>(R.id.rl_notification).onClick({
//            PreferencesManager.put<Boolean>(false, Constant.PREFERENCE_SHOW_NOTI)
//            var baseFragment: BaseFragment = NotificationFragment()
//            //var baseFragment: BaseFragment = NonUiFragment.getInstance("HeadlessFragment")
//            Navigation.addFragment(requireActivity(), baseFragment, Constant.TAG_NOTIFICATION_FRAGMENT, R.id.homeFram, true, false)
            MyApplication.smallVibrate()
            Navigation.addFragment(requireActivity(), SearchFragment.getInstance(2), Constant.TAG_SEARCH_FRAGMENT, R.id.homeFram, stack = true, needAnimation = false)

        })
    }

    private fun initView(view: View) {

        carousalRV = view.findViewById(R.id.rv_crousal);
        categoryRV = view.findViewById(R.id.rv_categories)
        closeByRestaurantRV = view.findViewById(R.id.rv_close_restaurant)
        carousal = view.findViewById(R.id.carousal)
        shimmer = view.findViewById(R.id.shimmer)
        real = view.findViewById(R.id.real)
        closebyCount = view.findViewById(R.id.tv_restaurant_close_by)
        seeAllMeetUp = view.findViewById<TextView>(R.id.seeAll_meetup).apply {
            setOnClickListener {
                Log.i(TAG,"  clicking::: ")
                MyApplication.putTrackMP(Constant.AcPlacesBestJointsSeeAll, null)
                var baseFragment:BaseFragment = PlacesListFragment()
                baseFragment.arguments = Bundle().apply { putBoolean("isBestPlaces",true)}
                Navigation.addFragment(requireActivity(),baseFragment,Constant.TAG_PLACE_LIST_FRAGMENT,R.id.homeFram,true,false)
            }
        }

        seeAllcloseBy = view.findViewById<TextView>(R.id.seeAll_closeby).apply {
            setOnClickListener {
                Log.i(TAG,"  clicking::: ")
                MyApplication.putTrackMP(Constant.AcPlacesCloseBySeeAll,null)
                var baseFragment:BaseFragment = PlacesListFragment()
                baseFragment.arguments = Bundle().apply { putBoolean("isBestPlaces",false)}
                Navigation.addFragment(requireActivity(),baseFragment,Constant.TAG_PLACE_LIST_FRAGMENT,R.id.homeFram,true,false)
            }
        }



    }

    fun setUp(location: Location) {
        if(this::shimmer.isInitialized)
            isHomeSetUp = true
        else
            return
        shimmer.stopShimmer()
        shimmer.visibility = View.GONE
        real.visibility = View.VISIBLE


        setUpMeetUpDataInAdapter(bestMeetUpList)
        setUpCategoryDataInAdapter(categoryList)
        setUpNearByRestaurantDataInAdapter(nearByPlacesList)

        /*restaurantDashboardViewModel.getAllOfferCard()?.observe(viewLifecycleOwner, Observer { t ->
            offerArrayList.clear()
            offerArrayList.addAll(t)
            carousalAdapter?.notifyDataSetChanged()
        })*/
        placeViewModel.getBestMeetUp(10,location.latitude,location.longitude,true)
        placeViewModel.getNearByPlaceCount(location.latitude,location.longitude)
        placeViewModel.getCloseByPlaceUp(10,location.latitude,location.longitude,null)
        placeViewModel.getCategories()
        placeViewModel.getOffers()



        /*placeViewModel.getAllCategoryCard()?.observe(viewLifecycleOwner, Observer {
            categoryList.clear()
            categoryList.addAll(it)
            Log.i(TAG," categorydata:: came ${it.size}")
            categoryAdapter?.notifyDataSetChanged()
        })*/

    }

    private fun addObserver() {
       /* placeViewModel.getInitRestaurantInfo().observe(viewLifecycleOwner, Observer {
            it?.let {
                offerArrayList.clear()
                offerArrayList.addAll(it)
                carousalAdapter?.notifyDataSetChanged()
                nearByRestaurantAdapter?.notifyDataSetChanged()
            }
        })*/

        /*placeViewModel.getAllCategoryCard()?.observe(viewLifecycleOwner, Observer {
            categoryList.clear()
            categoryList.addAll(it)
            Log.i(TAG," categorydata:: came ${it.size}")
            categoryAdapter?.notifyDataSetChanged()
        })*/

        placeViewModel.observeCategory().observe(viewLifecycleOwner, Observer {
            categoryList.definition = it.definition
            Log.i(TAG," categorydata:: came ${it.definition?.size}")
            categoryAdapter?.notifyDataSetChanged()
        })
        placeViewModel.observeOnOffers().observe(viewLifecycleOwner, Observer { response: CategoryResponse? ->
//            showToast("${response?.definition?.size}")
            Log.d(TAG, "addObserver: response?.definition${response?.definition?.size},${response?.definition?.isEmpty()}")
            response?.definition?.let { list: List<Category> ->
                if(list.isEmpty()){
                    carousal.visibility=View.GONE
                    return@let
                }
                carousal.setViewListener {
                    Log.d(TAG, "addObserver:image_url ${list?.getOrNull(it)?.image_url}")
                    var customView = layoutInflater.inflate(R.layout.card_carousal, null)
                    var image = customView.findViewById<ImageView>(R.id.iv_bg_card)
                    image.loadImage(requireActivity(), list?.getOrNull(it)?.image_url)
                    image.onClick({
                        MyApplication.putTrackMP(Constant.AcPlacesCarousal, JSONObject(mapOf("placeId" to list?.getOrNull(it)?.place_id)))
                        val baseFragment : BaseFragment = RestaurantDetailFragment();
                        Navigation.setFragmentData(baseFragment,"_id",list?.getOrNull(it)?.place_id)
                        Navigation.addFragment(requireActivity(),baseFragment,
                                RestaurantDetailFragment.TAG,R.id.homeFram,true,false)
                    })
                    customView
                }
                carousal.pageCount = list.size
            }?: run {
                carousal.visibility=View.GONE
            }
        })

        placeViewModel.observerPlaces().observe(viewLifecycleOwner, Observer {
            bestMeetUpList.clear()
            bestMeetUpList.addAll(it)
            Log.i(TAG," lengthOfplaces:: ${it.size}")
            Logging.i(TAG," $it")
            carousalAdapter?.notifyDataSetChanged()
            //nearByRestaurantAdapter?.notifyDataSetChanged()
        })

        placeViewModel.closeByPlace().observe(viewLifecycleOwner, Observer {
            nearByPlacesList.clear()
            nearByPlacesList.addAll(it)
            Logging.i(TAG," lengthOfNearByplaces:: ${it}")
            closeByRestaurantRV.adapter?.notifyDataSetChanged()
        })

        placeViewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity,it)
        })

        placeViewModel.observeNearPlaceCount().observe(viewLifecycleOwner, Observer {
            closebyCount.text = it.toString().plus(" Places close by")
        })
    }



    fun refreshImage(){
        placeViewModel.getOffers()
        closeByRestaurantRV.adapter?.notifyDataSetChanged()
        categoryRV.adapter?.notifyDataSetChanged()
        carousalRV.adapter?.notifyDataSetChanged()
//        carousal.containerViewPager.adapter?.notifyDataSetChanged()
    }

    /**
     * addRestaurantDataInAdapter for set data in adapteer
     * @param list used to fill data in recyclerView adapter
     */
    private fun setUpMeetUpDataInAdapter(list: FetchPlacesResponse?){
        carousalAdapter = BestMeetInitCrousalAdapter(requireActivity(),list);
        carousalRV.adapter = carousalAdapter;
        carousalAdapter?.notifyDataSetChanged()
    }

    /**
     * addRestaurantDataInAdapter for set data in adapteer
     * @param list used to fill data in recyclerView adapter
     */
    private fun setUpCategoryDataInAdapter(list: CategoryResponse){
        categoryAdapter = HomeCategoriesAdapter(requireActivity(),list){
            Log.i(TAG," HomeCategoriesAdapter:: clickecd ")
        }
        categoryRV.adapter = categoryAdapter;
        categoryAdapter?.notifyDataSetChanged()
    }

    private fun setUpNearByRestaurantDataInAdapter(list: FetchPlacesResponse?){
        nearByRestaurantAdapter = HomeBestJointAdapter(requireActivity(),list){
            Log.i(TAG," HomeCategoriesAdapter:: clickecd ")
        };
        closeByRestaurantRV.adapter = nearByRestaurantAdapter;
        categoryAdapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG," shimmerSTart:: ")
        shimmer.startShimmer()
    }

    override fun onPause() {
        shimmer.stopShimmer()
        super.onPause()
    }
    override fun onBackPageCome(){
       /* (activity as MainActivity?)?.viewPager?.let {
            ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem(it,1) as MeetUpNew?)?.refreshThispage()
            ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem(it,1) as MeetUpNew?)?.refreshThispage()
        }*/

        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }
}