package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.gson.JsonObject
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.places.SearchPlaceResponse
import com.meetsportal.meets.networking.post.FetchPostResponse
import com.meetsportal.meets.networking.post.SearchPostResponse
import com.meetsportal.meets.networking.profile.SearchPeopleResponse
import com.meetsportal.meets.networking.profile.SuggestionResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.VwSearchPeopleViewed
import com.meetsportal.meets.utils.Constant.VwSearchPlaceViewed
import com.meetsportal.meets.utils.Constant.VwSearchPostViewed
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.count
import com.meetsportal.meets.viewmodels.PlacesViewModel
import com.meetsportal.meets.viewmodels.PostViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchFragment : BaseFragment(), View.OnClickListener {

    val TAG = SearchFragment::class.simpleName


    lateinit var ivClearText: ImageView
    lateinit var people: RelativeLayout
    lateinit var peopleIcon: ImageView
    lateinit var post: RelativeLayout
    lateinit var postIcon: ImageView
    lateinit var meetup: RelativeLayout
    lateinit var meetupIcon: ImageView
    lateinit var restaurant: RelativeLayout
    lateinit var restaurantIcon: ImageView

    lateinit var viewPager: ViewPager
    lateinit var viewPageAdapter: ViewPagerAdapter

    //lateinit var cancel :ImageView
    lateinit var search: EditText

    val userviewModel: UserAccountViewModel by viewModels()
    val postviewModel: PostViewModel by viewModels()
    val placeViewModel: PlacesViewModel by viewModels()
    val profileViewmodel: UserAccountViewModel by viewModels()

    companion object {

        val ARG_TAB = "othersProfile"

        fun getInstance(tab: Int): SearchFragment {
            var bundle = Bundle()
            bundle.putInt(ARG_TAB, tab)
            return SearchFragment().apply { arguments = bundle }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        MyApplication.putTrackMP(VwSearchPeopleViewed, null)
        initView(view)
        setUp()
        setObserver()
    }


    private fun initView(view: View) {

        search = view.findViewById(R.id.et_search)
        ivClearText = view.findViewById(R.id.iv_clear_edit)
        view.findViewById<ImageView>(R.id.iv_cancel).setOnClickListener(this)

        people = view.findViewById(R.id.rl_people)
        peopleIcon = view.findViewById(R.id.iv_people)
        post = view.findViewById(R.id.rl_post)
        postIcon = view.findViewById(R.id.iv_post)
        meetup = view.findViewById(R.id.rl_meetup)
        meetupIcon = view.findViewById(R.id.iv_meetup)
        restaurant = view.findViewById(R.id.rl_restaurant)
        restaurantIcon = view.findViewById(R.id.iv_restaurant)
        viewPager = view.findViewById<ViewPager>(R.id.vp_result).apply {
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    setColor(position)
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }

        people.setOnClickListener(this)
        post.setOnClickListener(this)
        meetup.setOnClickListener(this)
        restaurant.setOnClickListener(this)
    }

    private fun setUp() {
        viewPager.offscreenPageLimit = 2
        viewPageAdapter = ViewPagerAdapter(childFragmentManager)
        viewPager.adapter = viewPageAdapter
        placeViewModel.getBestMeetUp(30, PreferencesManager.get<JsonObject>(Constant.PREFRENCE_LOCATION)
                ?.get(Constant.LATITUDE)?.asDouble, PreferencesManager.get<JsonObject>(Constant.PREFRENCE_LOCATION)
                ?.get(Constant.LONGITUDE)?.asDouble, true)
        profileViewmodel.getSuggestion()
        postviewModel.fetchRecentPost()
        ivClearText.setOnClickListener {
            search.setText("")
        }
        search.count {
            Log.i("TAG", " fatfat:: $it")
            if((it.toInt()) > 0) {
                ivClearText.visibility = View.VISIBLE
            } else {
                ivClearText.visibility = View.GONE
                profileViewmodel.getSuggestion()
            }

            if((it.toInt()) > 2) {
                userviewModel.searchPeople(search.text.toString().trim())
                postviewModel.searchPost(search.text.toString().trim())
                placeViewModel.searchPlace(search.text.toString().trim())

            } else {
                //reset()
            }
        }

        arguments?.let {
            viewPager.setCurrentItem(it.getInt(ARG_TAB))
        }


    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.rl_people     -> {
                setColor(0);MyApplication.smallVibrate();viewPager.setCurrentItem(0)
            }

            R.id.rl_post       -> {
                setColor(1); MyApplication.smallVibrate();viewPager.setCurrentItem(1)
            }

            R.id.rl_meetup     -> {
                setColor(2); MyApplication.smallVibrate()
            }

            R.id.rl_restaurant -> {
                setColor(2);MyApplication.smallVibrate();viewPager.setCurrentItem(2)
            }

            R.id.iv_cancel     -> activity?.onBackPressed()

        }
    }




    private fun setColor(index: Int) {

        people.alpha = 0.19f
        peopleIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primaryDark), android.graphics.PorterDuff.Mode.MULTIPLY)
        post.alpha = 0.19f
        postIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primaryDark), android.graphics.PorterDuff.Mode.MULTIPLY)
        meetup.alpha = 0.19f
        restaurant.alpha = 0.19f
        restaurantIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primaryDark), android.graphics.PorterDuff.Mode.MULTIPLY)


        when(index) {
            0 -> {
                MyApplication.putTrackMP(VwSearchPeopleViewed, null)
                people.alpha = 1f
                peopleIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.clickedIcon), android.graphics.PorterDuff.Mode.MULTIPLY)
                search.hint = "Search People"
            }

            1 -> {
                MyApplication.putTrackMP(VwSearchPostViewed, null)
                post.alpha = 1f
                postIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.clickedIcon), android.graphics.PorterDuff.Mode.MULTIPLY)
                search.hint = "Search Post"

            }
            /*2 -> {
                meetup.alpha = 1f
                search.hint = "Search Meet Up"
            }*/
            2 -> {
                MyApplication.putTrackMP(VwSearchPlaceViewed, null)
                restaurant.alpha = 1f
                restaurantIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.clickedIcon), android.graphics.PorterDuff.Mode.MULTIPLY)
                search.hint = "Search Places"
            }

        }

    }

    private fun reset() {
        (viewPageAdapter.instantiateItem(viewPager, 0) as SearchPeopleRecyclerFragment).setSearchPeopleAdapter(SearchPeopleResponse())
    }

    private fun setObserver() {

        profileViewmodel.observeOnSuggestion().observe(viewLifecycleOwner, Observer {

            when(it) {
                is ResultHandler.Success -> {
                    it?.value?.let { it1 ->
                        val response: SearchPeopleResponse = SearchPeopleResponse()
                        it1.forEach { it2: SuggestionResponseItem ->
                            response.add(it2.toPeople())
                        }
                        (viewPageAdapter.instantiateItem(viewPager, 0) as SearchPeopleRecyclerFragment).setSearchPeopleAdapter(response)

                    }
                }
                is ResultHandler.Failure -> {

                }
            }})
        postviewModel.observePostRecent()
                .observe(viewLifecycleOwner, Observer { result: ResultHandler<FetchPostResponse?>? ->
                    val response: SearchPostResponse = SearchPostResponse()
                    if(result is ResultHandler.Success) {
                        result.value?.forEach {
                            response.add(it.toSearchPost())
                        }
                        (viewPageAdapter.instantiateItem(viewPager, 1) as SearchPeopleRecyclerFragment).setSearchPostAdapter(response)
                    }
                })
        placeViewModel.observerPlaces().observe(viewLifecycleOwner, {
            val response = SearchPlaceResponse()
            it.forEach { it1 ->
                response.add(it1.toSearchPlace())
            }
            (viewPageAdapter.instantiateItem(viewPager, 2) as SearchPeopleRecyclerFragment).setSearchPlaceAdapter(response)
        })
        userviewModel.observePeopleSearch().observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    (viewPageAdapter.instantiateItem(viewPager, 0) as SearchPeopleRecyclerFragment).setSearchPeopleAdapter(it.value ?: SearchPeopleResponse())
                }

                is ResultHandler.Failure -> {
                    Log.e(TAG, " people Search Failed::: ")
                }
            }
        })
        postviewModel.observePostSearch().observe(viewLifecycleOwner, Observer {
            (viewPageAdapter.instantiateItem(viewPager, 1) as SearchPeopleRecyclerFragment).setSearchPostAdapter(it)
        })

        placeViewModel.observePlaceSearch().observe(viewLifecycleOwner, Observer {
            (viewPageAdapter.instantiateItem(viewPager, 2) as SearchPeopleRecyclerFragment).setSearchPlaceAdapter(it)
        })

    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount() = 3

        override fun getItem(position: Int): Fragment {
            return when(position) {
                0    -> SearchPeopleRecyclerFragment(0)
                1    -> SearchPeopleRecyclerFragment(1)
                2    -> SearchPeopleRecyclerFragment(2)
                else -> TimeLineFragment()
            }
        }
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }
}