package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.adapter.MeetAttendeeListAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentLikedUserBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.meetsportal.meets.viewmodels.PostViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OpenMeetPersonList : BaseFragment() {

    private var _binding: FragmentLikedUserBinding? = null
    private val binding get() = _binding!!


    val profileViewmodel: UserAccountViewModel by viewModels()
    val meetUpviewModel: MeetUpViewModel by viewModels()
    val postViewModel: PostViewModel by viewModels()

    var adapter: MeetAttendeeListAdapter? = null
    private var userName: String? = ""


    companion object {

        val TAG = OpenMeetPersonList::class.simpleName!!
        val MEETDETAIL = "meetDetail"
        val TYPE = "type"

        fun getInstance(meetId: String?, type: Constant.EnumMeetPerson): OpenMeetPersonList {
            return OpenMeetPersonList().apply {
                arguments = Bundle().apply {
                    putString(MEETDETAIL, meetId)
                    putSerializable(TYPE, type)
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

        initView(view)
        setUp()
        setObserver()
    }

    private fun initView(view: View) {

        when(arguments?.get(TYPE) as Constant.EnumMeetPerson) {
            /*Constant.EnumMeetPerson.ATTENDEE -> binding.name.text = "Who Attended"
            Constant.EnumMeetPerson.INVITEE -> binding.name.text = "Invitee"
            Constant.EnumMeetPerson.PARTICIPANT -> binding.name.text = "Participant"*/
            Constant.EnumMeetPerson.INTERESTED -> binding.name.text = "Interested"
        }
        binding.back.setOnClickListener { activity?.onBackPressed() }
        //Constant.EnumMeetPerson.
    }

    private fun setUp() {

        binding.recycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        //meetUpviewModel.getMeetUpDetail(arguments?.getString(MEETDETAIL))
        postViewModel.fetchSinglePost(arguments?.getString(MEETDETAIL), null)

        adapter = MeetAttendeeListAdapter(requireActivity(), ArrayList(), binding, arguments?.get(TYPE) as Constant.EnumMeetPerson, { sid, isItFollow ->
            followuser(sid, isItFollow)
        }, { it, name ->
            userName = name
            meetUpviewModel.rateAttendee(arguments?.getString(MEETDETAIL), it)
        }, {
            openProfile(it)
        })
        binding.recycler.adapter = adapter
        //binding.recycler.adapter =

    }

    private fun followuser(id: String?, forfollow: Boolean) {
        if(forfollow) {
            profileViewmodel.followUser(id)
        } else {
            profileViewmodel.unfollowUser(id)
        }
    }

    private fun setObserver() {
        meetUpviewModel.observeRateAttendee().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    Log.i(TAG, " Attendee Rated:: ")
                    adapter?.setData(it.value)
                    MyApplication.showToast(requireActivity(), "You have given $userName thumbs up for Positive Experience")
                }

                is ResultHandler.Failure -> {
                    if(it.code == "errors.meetup.notattendees") {
                        showToast("You didn't attend the meetup for giving a Positive Experience")
                    } else {
                        showToast("Already given $userName thumb up for Positive Experience ")

                    }
                    it.throwable?.printStackTrace()
                    Log.e(MeetAttendeeList.TAG, " Rate attendee failed")
                }
            }
        })
        postViewModel.observeSinglePost().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    when(arguments?.get(TYPE) as Constant.EnumMeetPerson) {
                        /* Constant.EnumMeetPerson.ATTENDEE -> adapter?.setData(it.value?.attendees)
                         Constant.EnumMeetPerson.INVITEE -> adapter?.setData(it.value?.invitees)
                         Constant.EnumMeetPerson.PARTICIPANT -> adapter?.setData(it.value?.participants)*/
                    }
                }

                is ResultHandler.Failure -> {
                    it.throwable?.printStackTrace()
                    Log.e(TAG, " Rate attendee failed")
                }
            }

        })

    }


    override fun onBackPageCome() {
        //TODO("Not yet implemented")
    }
}