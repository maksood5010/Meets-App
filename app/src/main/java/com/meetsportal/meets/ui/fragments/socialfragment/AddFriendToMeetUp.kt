package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.SelectedContactAdapted
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.database.entity.Contact
import com.meetsportal.meets.databinding.FragmentAddFriendToMeetupBinding
import com.meetsportal.meets.models.SelectedContactPeople
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.profile.SearchPeopleResponse
import com.meetsportal.meets.networking.profile.SearchPeopleResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant.VwInviteOpenMeetScreen
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.count
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable

@AndroidEntryPoint
class AddFriendToMeetUp : BaseFragment() {

    val TAG = AddFriendToMeetUp::class.simpleName

    lateinit var selectedContactAdapted: SelectedContactAdapted
    var selected: ArrayList<SelectedContactPeople> = ArrayList()
    var selectedContactList = ArrayList<Contact>()
    var selectedPeopleList = ArrayList<SearchPeopleResponseItem>()

    var serchComDisposabe = CompositeDisposable()


    lateinit var viewPageAdapter: ViewPagerAdapter


    val meetUpviewModel: MeetUpViewModel by viewModels()
    val userviewModel: UserAccountViewModel by viewModels()


    private var _binding: FragmentAddFriendToMeetupBinding? = null
    val binding get() = _binding!!

    companion object {

        val MEET_DATA = "meetData"

        fun getInstance(meetUpdata: GetMeetUpResponseItem?, TAG: String?): AddFriendToMeetUp {
            return AddFriendToMeetUp().apply {
                arguments = Bundle().apply {
                    putParcelable(MEET_DATA, meetUpdata)
                    putString("TAG", TAG)
                }
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_viewpager, container, false)
        _binding = FragmentAddFriendToMeetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setupUI(binding.root)>
        initView(view)
        setUp()
        addObserver()

    }

    private fun initView(view: View) {
        MyApplication.putTrackMP(VwInviteOpenMeetScreen, null)
        binding.include.tvBack.setRoundedColorBackground(requireActivity(),R.color.dark_transparent)
        binding.include.addFriendOrNext.text = "Add"
        viewPageAdapter = ViewPagerAdapter(childFragmentManager, arguments?.getString("sid"), arguments?.getParcelable<GetMeetUpResponseItem>(MEET_DATA))
        binding.include.tvNext.visibility = View.GONE
        binding.include.pager.adapter = viewPageAdapter
        binding.include.tablayout.setupWithViewPager(binding.include.pager)
        binding.include.rvSelectedContact.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        setAdapter()
        binding.include.addFriendOrNext.visibility = View.GONE
        binding.include.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(), R.color.primaryDark)
        binding.include.addFriendOrNext.onClick({
            arguments?.getParcelable<GetMeetUpResponseItem>(MEET_DATA)?.let {
                meetUpviewModel.inviteFriend(it._id, selected)
            }
        }, 3000)
    }

    private fun setUp() {
        binding.include.tvBack.setOnClickListener {
            activity?.onBackPressed()
        }

        var text = arrayListOf<String>("Users", "Contacts")
        for(i in 0 until binding.include.tablayout.getTabCount()) {
            val tab: TabLayout.Tab = binding.include.tablayout.getTabAt(i)!!
            val v: View = LayoutInflater.from(requireContext()).inflate(R.layout.cuisine_text, null)
            v.findViewById<TextView>(R.id.tabtext).text = text.get(i)
            val img: ImageView = v.findViewById<ImageView>(R.id.tab_icon)
                    .apply { visibility = View.GONE }
            tab.customView = v
        }
        binding.include.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                var selectedTabText = tab?.customView?.findViewById<TextView>(R.id.tabtext)?.text
                if(selectedTabText?.equals("Contacts") == true) {
                    if(!Utils.checkPermission(requireContext(), Manifest.permission.READ_CONTACTS)) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.include.tablayout.selectTab(binding.include.tablayout.getTabAt(0))
                        }, 1)
                        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 1233)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.include.search.count {
            Log.i("TAG", " fatfat:: $it")
            (viewPageAdapter.instantiateItem(binding.include.pager, 1) as ContactListFragment?)?.searchContact(binding.include.search.text.toString()
                            .trim())

            if((it.toInt()) > 2) {
                serchComDisposabe.add(userviewModel.searchPeople(binding.include.search.text.toString()
                        .trim()))
            } else {
                serchComDisposabe.clear()
                (viewPageAdapter.instantiateItem(binding.include.pager, 0) as SearchUserMeetFragment?)?.setSearchPeopleAdapter(SearchPeopleResponse(), false)
            }
        }


    }

    /**
     * for contact make false for item who removed
     * and for people remove from selectedPeopleList
     */
    private fun setAdapter() {
        selectedContactAdapted = SelectedContactAdapted(requireContext(), selected, binding.include) {
            var item = selected.get(it)
            selected.removeAt(it)
            updateSelectCOntactFromDateTime(item)
        }
        binding.include.rvSelectedContact.adapter = selectedContactAdapted

    }

    fun updateSelectCOntactFromDateTime(item: SelectedContactPeople) {
        Log.i(TAG, " CheckingItsCall:: ")
        notifySelctedPeopleDataChaneged()
        if(item is SelectedContactPeople.SelectedContact) {
            var index = selectedContactList.indexOfFirst { it.id == item.id }
            selectedContactList.set(index, selectedContactList.get(index)
                    .apply { selected = false })
            (viewPageAdapter.instantiateItem(binding.include.pager, 1) as ContactListFragment?)?.adapter?.notifyDataSetChanged()
        } else if(item is SelectedContactPeople.SelectedPeople) {
            Log.i(TAG, " checkingselectedPeopleList --0--- $selectedPeopleList")
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                selectedPeopleList.removeIf { it.username == item.username }
            }
            Log.i(TAG, " checkingselectedPeopleList --1--- $selectedPeopleList")
            (viewPageAdapter.instantiateItem(binding.include.pager, 0) as SearchUserMeetFragment?)?.populateSelectedList(selectedPeopleList)

        }
        shouldshowAddFriend()
    }

    fun notifySelctedPeopleDataChaneged() {
        selectedContactAdapted.notifydataChanged() {}
    }

    fun poplulateSelectedContactList(list: List<Contact>) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selected.removeIf { it is SelectedContactPeople.SelectedContact }
        }
        list.forEach {
            selected.add(it.toSelectedPerson())
        }

        selected.sortBy { it.sortTimestamp }
        notifySelctedPeopleDataChaneged()

        //---------------------******----------------//
        selectedContactList.clear()
        selectedContactList.addAll(list)
        shouldshowAddFriend()
    }

    fun populateSelectedPeopleList(selectedPeople: ArrayList<SearchPeopleResponseItem>) {


        Log.i(TAG, " selectedPeopleselectedPeople:: $selectedPeople")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selected.removeIf { it is SelectedContactPeople.SelectedPeople }
        }
        selectedPeople.forEach {
            selected.add(it.toSelectedPeople())
        }

        selected.forEach {
            Log.i(TAG, " selected.forEach:: ${it.sortTimestamp}")
        }
        selected.sortBy { it.sortTimestamp }


        notifySelctedPeopleDataChaneged()


        //----------------------------88888888--------//
        //selectedPeopleList.clear()
        //selectedPeopleList.addAll(selectedPeople)
        selectedPeopleList = selectedPeople
        shouldshowAddFriend()
    }

    fun shouldshowAddFriend() {
        if(selected.isEmpty()) {
            binding.include.addFriendOrNext.visibility = View.GONE
        } else {
            binding.include.addFriendOrNext.visibility = View.VISIBLE
        }
    }


    fun addObserver() {
        meetUpviewModel.observerSearchedContact().observe(viewLifecycleOwner, {
            Log.i(TAG, " searchedContact::: ${it.size}")
            (viewPageAdapter.instantiateItem(binding.include.pager, 1) as ContactListFragment?)?.getSearchedResult(it)
            //adapter.setSearchedList(it)
        })
        userviewModel.observePeopleSearch().observe(viewLifecycleOwner, {
            //(viewPageAdapter.instantiateItem(viewPager,0) as SearchPeopleRecyclerFragment).setSearchPeopleAdapter(it)
            Log.i(TAG, " searchedPeople::: ${it}")
            when(it) {
                is ResultHandler.Success -> {
                    (viewPageAdapter.instantiateItem(binding.include.pager, 0) as SearchUserMeetFragment?)?.setSearchPeopleAdapter(it.value ?: SearchPeopleResponse(), true)
                }

                is ResultHandler.Failure -> {
                    Log.e(TAG, " people Search Failed::: ")
                }
            }

        })

        meetUpviewModel.observerInviteUser().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "Invitation Sent")
                    setFragmentResult(arguments?.getString("TAG") ?: "TAG", Bundle().apply {
                        putBoolean("refresh", true)
                    })
                    activity?.onBackPressed()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong. Please try again Later")
                }
            }
        })

    }


    class ViewPagerAdapter(fm: FragmentManager, var sid: String?, val meetUpdata: GetMeetUpResponseItem?) : FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount() = 2
        override fun getItem(position: Int): Fragment {
            //return ChatPagerFragment.getInstance(position)
            return when(position) {
                0    -> SearchUserMeetFragment.getInstance(sid, meetUpdata)
                1    -> ContactListFragment()
                else -> ChatPagerFragment.getInstance(position)
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