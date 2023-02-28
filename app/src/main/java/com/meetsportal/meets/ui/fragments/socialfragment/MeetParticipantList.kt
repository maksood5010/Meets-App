package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.adapter.MeetPeopleListAdapter
import com.meetsportal.meets.databinding.FragmentLikedUserBinding
import com.meetsportal.meets.networking.meetup.MeetPerson
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeetParticipantList : BaseFragment() {

    private var _binding: FragmentLikedUserBinding? = null
    private val binding get() = _binding!!
    val allList: ArrayList<MeetPerson?> = ArrayList()


    val profileViewmodel: UserAccountViewModel by viewModels()

    var adapter: MeetPeopleListAdapter? = null


    companion object {

        val TAG = MeetParticipantList::class.simpleName!!
        val MEETDETAIL = "meetDetail"

        fun getInstance(meet: ArrayList<MeetPerson>?, _id: String?): MeetParticipantList {
            return MeetParticipantList().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(MEETDETAIL, meet)
                    putString("meetId", _id)
                }
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLikedUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData(arguments?.getParcelableArrayList<MeetPerson>(MEETDETAIL))
    }

    private fun initData(data: ArrayList<MeetPerson?>?) {
        this.allList.clear()
        this.allList.addAll(data ?: ArrayList())
        binding.npDataLayout.noDataHeading.text = "No Results"
        binding.npDataLayout.noDataDesc.text = "We found nothing matching your search"
//        binding.back.setOnClickListener { activity?.onBackPressed() }
        binding.back.visibility = View.GONE
        binding.name.visibility = View.GONE
//        binding.name.text = "Who is coming"
        binding.recycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = MeetPeopleListAdapter(requireActivity(), data, {
            binding.npDataLayout.noDataRoot.visibility = View.GONE
        }, { sid, isItFollow ->
            followuser(sid, isItFollow)
        }, {
            openProfile(it, Constant.Source.MEETUPCHAT.sorce.plus(arguments?.getString("meetId")))
        })
        binding.recycler.adapter = adapter
        if(allList.isEmpty()) {
            binding.npDataLayout.noDataRoot.visibility = View.VISIBLE
        } else {
            binding.npDataLayout.noDataRoot.visibility = View.GONE
        }
    }

    private fun followuser(id: String?, forfollow: Boolean) {
        if(forfollow) {
            profileViewmodel.followUser(id)
        } else {
            profileViewmodel.unfollowUser(id)
        }
    }
    /*override fun addObserver() {
        super.addObserver()
    }*/

//    override fun populateView(value: GetMeetUpResponseItem?) {
//        Log.d(TAG, "populateView: GetMeetUpResponseItem")
//        adapter?.setParticipants(value?.participants)
//    }

    override fun onBackPageCome() {
    }

    fun searchRequest(search: String?) {
        if(!search.isNullOrEmpty()) {
            var searchList = allList?.filter { it?.username?.contains(search, true) == true }
            adapter?.submitList(searchList)
            if(searchList.isEmpty()) {
                binding.npDataLayout.noDataRoot.visibility = View.VISIBLE
            } else {
                binding.npDataLayout.noDataRoot.visibility = View.GONE
            }
        } else {
            adapter?.submitList(allList)
            if(allList.isEmpty()) {
                binding.npDataLayout.noDataRoot.visibility = View.VISIBLE
            } else {
                binding.npDataLayout.noDataRoot.visibility = View.GONE
            }
        }
    }

    override fun hideNavBar(): Boolean {
        return true
    }

}