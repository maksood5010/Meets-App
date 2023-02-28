package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.CityAdapter
import com.meetsportal.meets.databinding.FragmentCountryFilterBinding
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground
import org.json.JSONObject
import java.util.*

class CountryFilter : BaseFragment() {


    private var _binding: FragmentCountryFilterBinding? = null
    private val binding get() = _binding!!

    companion object {

        val TAG = CountryFilter::class.simpleName!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_create_post, container, false)

        _binding = FragmentCountryFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()

    }

    private fun initView(view: View) {
        binding.back.onClick({ activity?.onBackPressed() })
        binding.search.setRoundedColorBackground(requireActivity(), R.color.gray1)
        binding.rlCurrentLoc.setRoundedColorBackground(requireActivity(), R.color.primaryDark, corner = 10f)
        binding.rlCurrentLoc.onClick({
            activity?.onBackPressed()
        })
        binding.rvOtherCity.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

    }

    private fun setUp() {
        var json = JSONObject(Utils.getJsonDataFromAsset(requireContext(), "countrycity.json"))
        var locale = Locale("", Utils.getCountryCode(requireContext()))
        Log.i(TAG, " checkingCities:: 3 ${locale.getDisplayCountry()}")
        var array = json.getJSONArray(locale.getDisplayCountry())
        Log.i(TAG, " checkingCities:: $array ")
        binding.rvOtherCity.adapter = CityAdapter(requireActivity(), array) {
            var fragment = activity?.supportFragmentManager?.findFragmentByTag(OpenMeetListFragment.TAG) as OpenMeetListFragment?
            fragment?.setCityFilter(it)
            activity?.onBackPressed()
        }
    }


    override fun onBackPageCome() {
    }
}