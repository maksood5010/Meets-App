package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.content.pm.PackageManager
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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.SelectedContactAdapted
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.database.entity.Contact
import com.meetsportal.meets.databinding.FragmentMeetUpFriendBottomBinding
import com.meetsportal.meets.models.SelectedContactPeople
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.profile.SearchPeopleResponse
import com.meetsportal.meets.networking.profile.SearchPeopleResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable

@AndroidEntryPoint
class MeetUpFriendtBottomFragment : BaseFragment() {

    private val TAG = MeetUpFriendtBottomFragment::class.java.simpleName

    lateinit var selectedContactAdapted: SelectedContactAdapted
    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var selectedContactList = ArrayList<Contact>()
    var selectedPeopleList = ArrayList<SearchPeopleResponseItem>()
    var selected: ArrayList<SelectedContactPeople> = ArrayList()

    lateinit var viewPageAdapter: ViewPagerAdapter
    var serchComDisposabe = CompositeDisposable()



    val meetUpviewModel: MeetUpViewModel by viewModels()
    val userviewModel: UserAccountViewModel by viewModels()
    var meetPager:MeetUpViewPageFragment? = null

    private var _binding: FragmentMeetUpFriendBottomBinding? = null
    private val binding get() = _binding!!

    companion object{

        fun getInstance():MeetUpFriendtBottomFragment{
            return MeetUpFriendtBottomFragment()
        }

        fun getInstance(id: String?):MeetUpFriendtBottomFragment{
            return MeetUpFriendtBottomFragment().apply {
                arguments = Bundle().apply {
                    putString("sid",id)
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return inflater.inflate(R.layout.fragment_meet_up_friend_bottom, container, false)
        _binding = FragmentMeetUpFriendBottomBinding.inflate(inflater, container, false)
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

        binding.tvBack.setRoundedColorBackground(requireActivity(),R.color.dark_transparent)
        binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(),R.color.gray1,)
        meetPager = activity?.supportFragmentManager?.findFragmentByTag(MeetUpViewPageFragment.TAG) as MeetUpViewPageFragment?
        viewPageAdapter = ViewPagerAdapter(childFragmentManager,arguments?.getString("sid"))
        binding.pager.adapter = viewPageAdapter
        binding.tablayout.setupWithViewPager(binding.pager)

        binding.rvSelectedContact.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        setAdapter()
        //list = ArrayList<Contact>(Utils.getContactList(requireContext()).sortedWith(compareBy({it.name})))

    }

    private fun setUp() {
        binding.tvNext.setOnClickListener {
            meetPager?.binding?.meetUpViewpager?.setCurrentItem(1,true)
        }
        binding.tvBack.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcNextFriendBack,null)
            activity?.onBackPressed()
        }
        binding.search.onClick({
            MyApplication.putTrackMP(Constant.AcSearchMeetUser,null)
        })

        var text = arrayListOf<String>("Users", "Contacts")
        for (i in 0 until binding.tablayout.getTabCount()) {
            val tab: TabLayout.Tab = binding.tablayout.getTabAt(i)!!
            val v: View = LayoutInflater.from(requireContext()).inflate(R.layout.cuisine_text, null)
            v.findViewById<TextView>(R.id.tabtext).text = text.get(i)
            val img: ImageView =
                v.findViewById<ImageView>(R.id.tab_icon).apply { visibility = View.GONE }
            tab.customView = v
        }
        binding.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                var selectedTabText = tab?.customView?.findViewById<TextView>(R.id.tabtext)?.text
                if (selectedTabText?.equals("Contacts") == true) {
                    if (!Utils.checkPermission(
                            requireContext(),
                            Manifest.permission.READ_CONTACTS
                        )
                    ) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.tablayout.selectTab(binding.tablayout.getTabAt(0))
                        }, 1)
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_CONTACTS),
                            1233
                        )
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        binding.search.count {
            Log.i("TAG", " fatfat:: $it")
            (viewPageAdapter.instantiateItem(binding.pager, 1) as ContactListFragment?)
                    ?.searchContact(binding.search.text.toString().trim())
            if ((it.toInt()) > 2) {
                serchComDisposabe.add(userviewModel.searchPeople(binding.search.text.toString().trim()))
            } else {
                serchComDisposabe.clear()
                (viewPageAdapter.instantiateItem(
                    binding.pager,
                    0
                ) as SearchUserMeetFragment?)?.setSearchPeopleAdapter(SearchPeopleResponse(),false)
            }
        }
    }

    /**
     * for contact make false for item who removed
     * and for people remove from selectedPeopleList
     */
    private fun setAdapter() {
        selectedContactAdapted = SelectedContactAdapted(requireContext(), selected,binding) {
            var item = selected.get(it)
            selected.removeAt(it)
            updateSelectCOntactFromDateTime(item)
            (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,4) as MeetUpDateNTimeBottomFragment?)?.notifyPeopleItemChange()
        }
        binding.rvSelectedContact.adapter = selectedContactAdapted

    }

    fun updateSelectCOntactFromDateTime(item: SelectedContactPeople) {

        notifySelctedPeopleDataChaneged()
       // selectedContactAdapted.itemCount
        if (item is SelectedContactPeople.SelectedContact) {
            var index = selectedContactList.indexOfFirst { it.id == item.id }
            selectedContactList.set(index, selectedContactList.get(index).apply { selected = false })
            (viewPageAdapter.instantiateItem(binding.pager, 1) as ContactListFragment?)?.adapter?.notifyDataSetChanged()
        } else if (item is SelectedContactPeople.SelectedPeople) {
            Log.i(TAG," checkingselectedPeopleList --0--- $selectedPeopleList")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                selectedPeopleList.removeIf { it.username == item.username }
            }
            Log.i(TAG," checkingselectedPeopleList --1--- $selectedPeopleList")
            (viewPageAdapter.instantiateItem(binding.pager, 0) as SearchUserMeetFragment?)?.populateSelectedList(selectedPeopleList)
        }
    }

    fun notifySelctedPeopleDataChaneged(){
        selectedContactAdapted.notifydataChanged(){
            if(it>0){
                binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(),R.color.primaryDark)
                binding.addFriendOrNext.onClick( {
                    MyApplication.putTrackMP(Constant.AcNextFriends,null)
                    meetPager?.binding?.meetUpViewpager?.setCurrentItem(1,true) })
            }
            else{
                binding.addFriendOrNext.background = Utils.getRoundedColorBackground(requireActivity(),R.color.gray1,)
                binding.addFriendOrNext.onClick( {  })


            }
        }
    }

    fun poplulateSelectedContactList(list: List<Contact>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selected.removeIf { it is SelectedContactPeople.SelectedContact }
        }
        list.forEach {
            selected.add(it.toSelectedPerson())
        }

        selected.sortBy { it.sortTimestamp }
        notifySelctedPeopleDataChaneged()
        (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,4) as MeetUpDateNTimeBottomFragment?)?.notifyPeopleItemChange()


        //---------------------******----------------//
        selectedContactList.clear()
        selectedContactList.addAll(list)
    }

    fun populateSelectedPeopleList(selectedPeople: ArrayList<SearchPeopleResponseItem>) {

        Log.i(TAG," selectedPeopleselectedPeople:: $selectedPeople")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selected.removeIf { it is SelectedContactPeople.SelectedPeople }
        }
        selectedPeople.forEach {
            selected.add(it.toSelectedPeople())
        }

       selected.forEach{
           Log.i(TAG," selected.forEach:: ${it.sortTimestamp}")
       }
        selected.sortBy { it.sortTimestamp }


        notifySelctedPeopleDataChaneged()
        (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,4) as MeetUpDateNTimeBottomFragment?)?.notifyPeopleItemChange()


        //----------------------------88888888--------//
        //selectedPeopleList.clear()
        //selectedPeopleList.addAll(selectedPeople)
        selectedPeopleList = selectedPeople
    }

    fun addObserver() {
        meetUpviewModel.observerSearchedContact().observe(viewLifecycleOwner, {
            Log.i(TAG, " searchedContact::: ${it.size}")
            (viewPageAdapter.instantiateItem(
                binding.pager,
                1
            ) as ContactListFragment?)?.getSearchedResult(it)
            //adapter.setSearchedList(it)
        })
        userviewModel.observePeopleSearch().observe(viewLifecycleOwner, {
            //(viewPageAdapter.instantiateItem(viewPager,0) as SearchPeopleRecyclerFragment).setSearchPeopleAdapter(it)
            when(it){
                is ResultHandler.Success -> {
                    (viewPageAdapter.instantiateItem(
                        binding.pager,
                        0
                    ) as SearchUserMeetFragment?)?.setSearchPeopleAdapter(it.value?:SearchPeopleResponse(),true)
                }
                is ResultHandler.Failure ->{
                    Log.e(TAG," people Search Failed::: ")
                }
            }

        })

    }

    class ViewPagerAdapter(fm: FragmentManager,var sid:String?) :
        FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount() = 2
        override fun getItem(position: Int): Fragment {
            //return ChatPagerFragment.getInstance(position)
            return when (position) {
                0 -> SearchUserMeetFragment.getInstance(sid)
                1 -> ContactListFragment()
                else -> ChatPagerFragment.getInstance(position)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, " onRequestPermissionsResultContact:: ")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1233 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //((activity as MainActivity).viewPageAdapter.instantiateItem((activity as MainActivity).viewPager,0) as TimeLineFragment).updateSuggestion()
                    (viewPageAdapter.instantiateItem(
                        binding.pager,
                        1
                    ) as ContactListFragment).getCon()
                    //var profile = activity?.supportFragmentManager?.findFragmentByTag(Constant.TAG_PROFILE_FRAGMENT) as ProfileFragment
                } else {
                    if (Build.VERSION.SDK_INT > 23 && !shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                        Log.i(
                            TAG,
                            " checkingratinale:: ${shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)}"
                        )
                        rationale()
                    }
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(
                        requireContext(),
                        "Permission denied for contact",
                        Toast.LENGTH_SHORT
                    )
                        .show();
                }
            }
        }
    }

    /*private fun rationale() {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = AlertDialog.Builder(
                requireContext(),
                android.R.style.Theme_Material_Light_Dialog_Alert
            )
        } else {
            builder = AlertDialog.Builder(requireContext())
        }
        builder.setTitle(getString(R.string.mandatory_permission))
            .setMessage("Permission Require to show contact list..")
            .setPositiveButton("Proceed") { dialog, which ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", activity?.packageName, null)
                intent.data = uri
                startActivityForResult(intent, 1)
            }
            .setCancelable(true)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }*/

    override fun onResume() {
        (meetPager?.pagerAdapter?.instantiateItem(meetPager?.binding?.meetUpViewpager!!,4) as MeetUpDateNTimeBottomFragment?)?.initSelecyedPeopleDataSource(selected)
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, " onPause:: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        Log.i(TAG, " onDestroy:: ")

    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }


}