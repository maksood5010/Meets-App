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
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MeetAttendeeList : BaseFragment() {

    private var userName: String? = ""
    private var enumMeetPerson: Constant.EnumMeetPerson = Constant.EnumMeetPerson.ATTENDEE
    private var _binding: FragmentLikedUserBinding? = null
    private val binding get() = _binding!!


    val profileViewmodel: UserAccountViewModel by viewModels()
    val meetUpviewModel: MeetUpViewModel by viewModels()

    var adapter: MeetAttendeeListAdapter? = null


    companion object {

        val TAG = MeetAttendeeList::class.simpleName!!
        val MEETID = "meetDetail"
        val TYPE = "type"

        fun getInstance(meetId: String?, type: Constant.EnumMeetPerson): MeetAttendeeList {
            return MeetAttendeeList().apply {
                arguments = Bundle().apply {
                    putString(MEETID, meetId)
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
        enumMeetPerson = arguments?.get(TYPE) as Constant.EnumMeetPerson
        when(enumMeetPerson) {
            Constant.EnumMeetPerson.ATTENDEE    -> {
                binding.npDataLayout.noDataHeading.text = "No one Attended Meetup"
                binding.npDataLayout.noDataDesc.text = "Seems Like No one Came to Meetup"
                binding.name.text = "Who Attended"
            }

            Constant.EnumMeetPerson.INVITEE     -> {
                binding.npDataLayout.noDataHeading.text = "No Invitee in this Meetup"
                binding.npDataLayout.noDataDesc.text = "Seems Like No one Invited to Meetup"
                binding.name.text = "Invitee"
            }

            Constant.EnumMeetPerson.PARTICIPANT -> {
                binding.npDataLayout.noDataHeading.text = "No Participants"
                binding.npDataLayout.noDataDesc.text = "Seems Like No one Joined the Meetup"
                binding.name.text = "Participant"
            }
        }
        binding.back.setOnClickListener { activity?.onBackPressed() }
        //Constant.EnumMeetPerson.
    }

    private fun setUp() {

        binding.recycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        meetUpviewModel.getMeetUpDetail(arguments?.getString(MEETID))

        adapter = MeetAttendeeListAdapter(requireActivity(), ArrayList(), binding, arguments?.get(TYPE) as Constant.EnumMeetPerson, { sid, isItFollow ->
            followuser(sid, isItFollow)
        }, { it, name ->
            userName = name
            meetUpviewModel.rateAttendee(arguments?.getString(MEETID), it)
        }, {
            openProfile(it, Constant.Source.MEETUPCHAT.sorce.plus(MEETID))
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
                    Log.e(TAG, " Rate attendee failed")
                }
            }
        })
        meetUpviewModel.observeMeetUpDetail().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    when(arguments?.get(TYPE) as Constant.EnumMeetPerson) {
                        Constant.EnumMeetPerson.ATTENDEE    -> adapter?.setData(it.value?.attendees)
                        Constant.EnumMeetPerson.INVITEE     -> adapter?.setData(it.value?.invitees)
                        Constant.EnumMeetPerson.PARTICIPANT -> adapter?.setData(it.value?.participants)
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
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
        meetUpviewModel.getMeetUpDetail(arguments?.getString(MEETID))
    }

    override fun hideNavBar(): Boolean {
        return true
    }


}