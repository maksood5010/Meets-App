package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.SearchPeopleAdapter
import com.meetsportal.meets.adapter.SearchPlaceAdapter
import com.meetsportal.meets.adapter.SearchPostAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.LayoutRecyclerViewBinding
import com.meetsportal.meets.networking.places.SearchPlaceResponse
import com.meetsportal.meets.networking.post.SearchPostResponse
import com.meetsportal.meets.networking.profile.SearchPeopleResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils

class SearchPeopleRecyclerFragment(var index : Int): BaseFragment() {

     val TAG = SearchPeopleRecyclerFragment::class.simpleName

    lateinit var searchPeopleAdapter: SearchPeopleAdapter
    lateinit var searchPlaceAdapter: SearchPlaceAdapter
    lateinit var searchPostAdapter: SearchPostAdapter

    var peopleList : SearchPeopleResponse =SearchPeopleResponse()
    var postList : SearchPostResponse = SearchPostResponse()
    var placeList : SearchPlaceResponse = SearchPlaceResponse()

    private var _binding: LayoutRecyclerViewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        return inflater.inflate(R.layout.layout_recycler_view, container, false)
        _binding = LayoutRecyclerViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()
    }

    private fun initView(view: View) {
        //recycler = view.findViewById(R.id.recycler)
        //noData = view.findViewById(R.id.no_data_root)
        binding.noData.noDataImage.setImageResource(R.drawable.ic_no_search)
        binding.noData.noDataHeading.text = "No Results"
        binding.noData.noDataDesc.text = "We found nothing matching your search"

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        when(index){
            0-> searchPeopleAdapter = SearchPeopleAdapter(requireActivity(),peopleList) {
                Utils.hideSoftKeyBoard(requireContext(),binding.recycler)
                openProfile(it.sid,Constant.Source.SEARCH.sorce.plus(MyApplication.SID))

                /*if(it.sid?.equals(PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid) == true){
                    Navigation.addFragment(
                        requireActivity(),
                        ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
                    )
                }else{
                    var baseFragment : BaseFragment = OtherProfileFragment.getInstance(it.sid)
                   *//* baseFragment = Navigation.setFragmentData(baseFragment,
                        OtherProfileFragment.OTHER_USER_ID, it.sid)*//*
                    Navigation.addFragment(requireActivity(),baseFragment,
                        Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram,true,false)
                }*/
            }
            1-> searchPostAdapter = SearchPostAdapter(requireActivity(), postList) {}
            2-> searchPlaceAdapter = SearchPlaceAdapter(requireActivity(),placeList) {}
        }
    }

    private fun setUp() {
        when(index){
            0->binding.recycler.adapter =searchPeopleAdapter
            1->binding.recycler.adapter =searchPostAdapter
            2->binding.recycler.adapter =searchPlaceAdapter
        }
    }


    fun setSearchPeopleAdapter(list: SearchPeopleResponse){
        peopleList.clear()
        peopleList.addAll(list)
        if(peopleList.size <= 0){
            binding.noData.noDataRoot.visibility = View.VISIBLE
            //recycler.visibility =View.GONE
        }else{
            binding.noData.noDataRoot.visibility = View.GONE
            //recycler.visibility =View.VISIBLE
        }
        searchPeopleAdapter.notifyDataSetChanged()

    }

    fun setSearchPostAdapter(list: SearchPostResponse){
        Log.i("TAG "," setSearchPostAdapter::: ${list.size}")
        postList.clear()
        postList.addAll(list)
        if(postList.size <= 0){
            Log.i("TAG"," showNodata ${peopleList.size}")
            binding.noData.noDataRoot.visibility = View.VISIBLE
             binding.recycler.visibility =View.GONE
        }else{
            Log.i("TAG"," removeNodata ${peopleList.size}")
            binding.noData.noDataRoot.visibility = View.GONE
            binding.recycler.visibility =View.VISIBLE
        }
        searchPostAdapter.notifyDataSetChanged()


    }

    fun setSearchPlaceAdapter(list: SearchPlaceResponse){
        Log.i("TAG "," setPlaceAdapter::: ${list.size}")
        placeList.clear()
        placeList.addAll(list)
        if(placeList.size <= 0){
            Log.i("TAG"," showNodata ${placeList.size}")
            binding.noData.noDataRoot.visibility = View.VISIBLE
            binding.recycler.visibility =View.GONE
        }else{
            Log.i("TAG"," removeNodata ${placeList.size}")
            binding.noData.noDataRoot.visibility = View.GONE
            binding.recycler.visibility =View.VISIBLE
        }
        searchPlaceAdapter.notifyDataSetChanged()


    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }
}