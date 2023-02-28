package com.meetsportal.meets.ui.fragments.socialfragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.transition.TransitionSet.ORDERING_TOGETHER
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.OpenMeetListSelectCatAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.database.DiscoverMeetAdapter
import com.meetsportal.meets.databinding.FragmentOpenMeetListBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.profile.Definition
import com.meetsportal.meets.networking.profile.FullInterestGetResponse
import com.meetsportal.meets.overridelayout.SpaceItemDecoration
import com.meetsportal.meets.ui.dialog.BadgeSelectAlert
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import java.util.*
import kotlin.collections.ArrayList


@AndroidEntryPoint
class OpenMeetListFragment : BaseFragment() {

    val meetUpViewModel: MeetUpViewModel by viewModels()
    lateinit var adapter : DiscoverMeetAdapter
    lateinit var filterAdapter : OpenMeetListSelectCatAdapter

    var allInterestList = PreferencesManager.get<FullInterestGetResponse>(Constant.PREFRENCE_INTEREST)?.definition?:ArrayList()

    var days = Utils.getTimeOfAfterDay(100)
    var city : String? = null
    var minbadge : String?  =  null
    var selectedInterest: ArrayList<Definition?>? = ArrayList<Definition?>()

    companion object{
        val TAG = OpenMeetListFragment::class.simpleName!!
    }

    private var _binding: FragmentOpenMeetListBinding? = null
    private val binding get() = _binding!!
    var badgeAlert : BadgeSelectAlert? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentOpenMeetListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        setUp()
        addObserver()

    }

    private fun initView() {

       // city = Utils.getCountryCode(requireContext())
        Log.i(TAG," countryCode::: ${Utils.getCountryCode(requireContext())}")
        Log.i(TAG," countryCode::: ${Utils.getTimeOfAfterDay(0)}")
        Log.i(TAG," countryCode::: ${Utils.getTimeOfAfterDay(3)}")
        badgeAlert = BadgeSelectAlert(requireActivity()) {
            val wrapper = ContextThemeWrapper(requireContext(), Constant.badgeList.getOrNull(it)?.style?:R.style.bronze)
            val drawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.ic_trophy_dummy, wrapper.getTheme())
            binding.filterTrophy.setImageDrawable(drawable)
            if (it == 0){
                binding.filterBadge.text = "Bronze and above"
                minbadge = null
            }else{
                minbadge = Constant.badgeList.subList (it, Constant.badgeList.size).map { it.key }.joinToString(",")
                binding.filterBadge.text = Constant.badgeList.getOrNull(it)?.name.plus(" and above")
            }
            applyFilter()
            Log.i(TAG, " minbadge:::: ${minbadge}")
        }
        setFragmentResultListener(TAG) {key, result->
            // get the result from bundle
            val stringResult = result.getStringArrayList("returnInterestKey")
            Log.i(TAG, "setUpResultListener:::  $stringResult ${allInterestList}")
            val selected = allInterestList?.filter { stringResult?.contains(it?.key) == true }
            filterAdapter.setData(selected)
            selectedInterest?.clear()
            selectedInterest?.addAll(selected)

            Log.i(TAG," checking selectedInterest:: ${selectedInterest}")
            applyFilter()
        }
        binding.rlFilter.setRoundedColorBackground(requireActivity(), R.color.white,corner = 20f)
        binding.rlLocFilter.setRoundedColorBackground(requireActivity(), R.color.white,corner = 20f)
        binding.cardFilter.setRoundedColorBackground(requireActivity(),R.color.gray1,corner = 20f,enbleDash = true,Dashgap = 0f,strokeColor = R.color.white)
        binding.llContent.setRoundedColorBackground(requireActivity(),R.color.gray1,corner = 20f,enbleDash = true,Dashgap = 0f,strokeColor = R.color.white)
        binding.back.onClick({ activity?.onBackPressed()} )
        binding.rlLocFilter.onClick({ Navigation.addFragment(requireActivity(),CountryFilter(),CountryFilter.TAG,R.id.homeFram,true,false) })
        binding.countryName.text = Locale("",Utils.getCountryCode(requireContext())).getDisplayCountry()
        adapter = DiscoverMeetAdapter(requireActivity(),meetUpViewModel)
        binding.recycler.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.recycler.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(20f,resources),0))
        binding.recycler.adapter = adapter
        binding.filterRecycler.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        binding.filterRecycler.addItemDecoration(SpaceItemDecoration(0,Utils.dpToPx(20f,resources)))
        filterAdapter = OpenMeetListSelectCatAdapter(requireActivity())
        binding.filterRecycler.adapter = filterAdapter
        clearFilterIndicator()

        binding.rlFilter.setOnClickListener {
            chengeFilterVisibility()
        }

    }

    private fun clearFilterIndicator(){
        filterAdapter.setData(ArrayList())
        binding.filterBadge.text = "Bronze and above"
        val wrapper = ContextThemeWrapper(requireContext(), Constant.badgeList.getOrNull(0)?.style?:R.style.bronze)
        val drawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.ic_trophy_dummy, wrapper.getTheme())
        binding.filterTrophy.setImageDrawable(drawable)
    }

    private fun setUp() {

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.filter {
                Log.i(TAG," checking data = ${it.refresh}")
                it.refresh is LoadState.NotLoading
            }.collect {
                Log.i(TAG," distinctUntilChangedBy::: 1-- $it")
                if(it.append is LoadState.NotLoading && it.append.endOfPaginationReached && adapter.itemCount < 1) {
                    Log.i(TAG," distinctUntilChangedBy::: 2-- data not available")
                    binding.rlNoData.visibility = View.VISIBLE
                } else if(it.append is LoadState.NotLoading && it.append.endOfPaginationReached && adapter.itemCount >= 1) {
                    Log.i(TAG," distinctUntilChangedBy::: 2-- data available")
                    binding.rlNoData.visibility = View.GONE
                }
                if(adapter.itemCount > 0){
                    binding.rlNoData.visibility = View.GONE
                }
                Log.i(TAG, " distinctUntilChangedBy:::  3 --  ${adapter.itemCount}")
            }
        }

        setDayInFilter(100)
        binding.today.onClick({
            setSelected(0)
            setDayInFilter(1)
            chengeFilterVisibility()
        })
        binding.week.onClick({
            setSelected(1)
            setDayInFilter(7)
            chengeFilterVisibility()
        })
        binding.month.onClick({
            setSelected(2)
            setDayInFilter(31)
            chengeFilterVisibility()
        })
        binding.badge.onClick({
            /*CreateMeetOptionAlert(requireActivity(),{}).apply {
                showDialog()
            }*/
            badgeAlert?.show()
            chengeFilterVisibility()
         //   BadgeSelectAlert(requireActivity()).show()
        })
        binding.clear.onClick({
            setSelected(3)
            //setDayInFilter(100)
            clearFiler()
            chengeFilterVisibility()

        })
        binding.category.onClick({
            var interest = InterestFragment.getInstance(TAG, ArrayList(selectedInterest?.map { it?.key }))
            Navigation.addFragment(requireActivity(),interest,InterestFragment.TAG,R.id.homeFram,true,false)
            chengeFilterVisibility()
        })
    }

    fun clearFiler(){
        clearFilterIndicator()
        setDayInFilter(100)
        selectedInterest?.clear()
        minbadge = null
        applyFilter()

    }

    fun chengeFilterVisibility(){
        if (binding.llContent.getVisibility() == View.VISIBLE) {
            TransitionManager.beginDelayedTransition(
                binding.cardFilter, getTransition())
            binding.llContent.visibility = View.GONE
            // arrow.setImageResource(R.drawable.ccp_ic_arrow_drop_down)
        } else {
            TransitionManager.beginDelayedTransition(
                binding.cardFilter, getTransition())
            binding.llContent.visibility = View.VISIBLE
        }
    }

    fun getTransition(): TransitionSet {
        var k = TransitionSet()
        k.setOrdering(ORDERING_TOGETHER)
        return k.addTransition(Fade(Fade.OUT))
            .addTransition(ChangeBounds())
            .addTransition(Fade(Fade.IN))
            .setDuration(100L)
    }

    fun setSelected(option :Int){
        binding.today.background = null
        binding.week.background = null
        binding.month.background = null
        binding.clear.background = null
        when(option){
            0 ->  {
                binding.filter.text = "Today";binding.today.setBackgroundColor(ContextCompat.getColor(requireContext(),R.color.white))}
            1 -> {
                binding.filter.text = "Week"
                binding.week.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            2 -> {
                binding.filter.text = "Month"
                binding.month.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            3 -> {
                binding.filter.text = "Filter"
            }
        }
    }

    fun setDayInFilter(days:Int){
        this.days = Utils.getTimeOfAfterDay(days)
        applyFilter()
    }

    private fun addObserver(){
        meetUpViewModel.observeDiscoverMeetPageDataSource().observe(viewLifecycleOwner,{
            adapter.submitData(lifecycle,it)
        })
        meetUpViewModel.observeJoinMeetUp().observe(viewLifecycleOwner,{
            when(it){
                is ResultHandler.Success ->{
                    MyApplication.showToast(requireActivity(),"Meetup joined...")
                    adapter.refresh()
//                    postViewModel.fetchSinglePost(POST_ID,null)
                }
                is ResultHandler.Failure ->{
                    Toast.makeText(requireContext(),"${it.message}", Toast.LENGTH_SHORT).show()
                    adapter.refresh()
                }
            }
        })
    }

    fun setCityFilter(city : String){
        binding.countryName.text  = city
        this.city = city
        applyFilter()


    }

    fun applyFilter(){
        meetUpViewModel.discoverOpenMeet(
            Utils.getCountryCode(requireContext()),
            city,
            days,
            if(selectedInterest?.map{ it?.key }?.isNotEmpty() == true){
                selectedInterest?.map{ it?.key }?.joinToString(",")
            }else{
                null
            },
            minbadge
        )
    }




    override fun onBackPageCome() {

    }
}