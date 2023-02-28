package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.adapter.ProfileViewPersonListAdapter
import com.meetsportal.meets.adapter.TimelineFooterStateAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentProfileViewPersonListBinding
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject


@AndroidEntryPoint
class ProfileViewPersonList : BaseFragment() {


    val userviewModel: UserAccountViewModel by viewModels()
    var lisyAdapter : ProfileViewPersonListAdapter? = null

    private var _binding: FragmentProfileViewPersonListBinding? = null
    private val binding get() = _binding!!

    companion object{
        val TAG = ProfileViewPersonList::class.simpleName!!

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileViewPersonListBinding.inflate(inflater, container, false)
        return binding.root
        //return inflater.inflate(R.layout.fragment_meet_up_restaurant_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setupUI(binding.root)>
        initView(view)
        setUp()
        setObserver()
    }

    private fun initView(view: View) {
        binding.back.onClick({ activity?.onBackPressed() })
        val count = Navigation.getFragmentData(this, "count")
        binding.count.text="$count people viewed your profile "
        binding.rvPeople.setRoundedColorBackground(requireActivity(), corner = 20f)
        binding.rvPeople.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        lisyAdapter = ProfileViewPersonListAdapter(requireActivity(),{sid,isFollow ->
            MyApplication.putTrackMP(Constant.AcFollowUnFollowProfileView, JSONObject(mapOf("sid" to sid)))
            followuser(sid,isFollow)
        },{
            MyApplication.putTrackMP(Constant.AcDpOnProfileView, JSONObject(mapOf("sid" to it)))
            openProfile(it, Constant.Source.PROFILEINSIGHT.sorce.plus(MyApplication.SID))

        })
        binding.rvPeople.adapter = lisyAdapter?.withLoadStateFooter(TimelineFooterStateAdapter {
            lisyAdapter?.retry()
        })
    }




    private fun setUp() {
        userviewModel.getProfileViewPeoplwInsight()


    }

    private fun setObserver() {
        userviewModel.observeProfileViewChange().observe(viewLifecycleOwner,  {
            lisyAdapter?.submitData(lifecycle, it)
        })


    }

    private fun followuser(id: String?, forfollow: Boolean) {
        if(forfollow){
            userviewModel.followUser(id)
        }else{
            userviewModel.unfollowUser(id)
        }
    }

    override fun onBackPageCome() {

    }
}