package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.OnlineStatusAdapter
import com.meetsportal.meets.adapter.SearchPeopleChatAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentChatDashboardBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.firebase.FireBaseUtils
import com.meetsportal.meets.networking.profile.SearchPeopleResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant.VwChatDashViewed
import com.meetsportal.meets.utils.count
import com.meetsportal.meets.viewmodels.FireBaseViewModal
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatDashboardFragment : BaseFragment(){

    val TAG = ChatDashboardFragment::class.java.simpleName

    val  fireViewModel: FireBaseViewModal by viewModels()
    val userviewModel : UserAccountViewModel by viewModels()

    lateinit var viewPagerAdapter : ViewPagerAdapter
    lateinit var adapter : OnlineStatusAdapter
    lateinit var searchPeopleAdapter : SearchPeopleChatAdapter
    var statusListener : ListenerRegistration? = null

    private var _binding: FragmentChatDashboardBinding? = null
    val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return inflater.inflate(R.layout.fragment_chat_dashboard,container,false)
        _binding = FragmentChatDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setupUI(binding.root)>
        MyApplication.putTrackMP(VwChatDashViewed,null)
        initView(view)
        setUp()
//        setUpTabView()
        setObserver()
    }

    private fun initView(view: View) {
        binding.ivBack.setOnClickListener { activity?.onBackPressed() }
        adapter = OnlineStatusAdapter(requireActivity())
        fireViewModel.getAllChat(10)
        binding.recentPeople.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        binding.recentPeople.adapter = adapter
        viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        binding.rvSearch.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        searchPeopleAdapter = SearchPeopleChatAdapter(requireActivity()){
            if(it){
                binding.noDataResult.noDataRoot.visibility=View.VISIBLE
            }else{
                binding.noDataResult.noDataRoot.visibility=View.GONE
            }
        }
        binding.rvSearch.adapter = searchPeopleAdapter

    }

    fun setUp(){

        binding.etSearch.count {
            Log.i("TAG"," fatfat:: $it")
            if((it.toInt()) >  2){
                Log.i(TAG," startsearching:: ")
                binding.rvSearch.visibility = View.VISIBLE
                userviewModel.searchPeople(binding.etSearch.text.toString().trim())
            }else{
                binding.rvSearch.visibility = View.GONE
                binding.noDataResult.noDataRoot.visibility=View.GONE
            }
        }
        binding.pager.adapter = viewPagerAdapter
        binding.pager.offscreenPageLimit = 2
        binding.pager.setBackgroundResource( R.drawable.bg_meet_place_map)
        binding.tlTabs.setupWithViewPager(binding.pager)
    }

    private fun setUpTabView() {
        var text = arrayListOf<String>("  All", "Unread","Pinned" )
        for (i in 0 until binding.tlTabs.getTabCount()) {
            val tab: TabLayout.Tab = binding.tlTabs.getTabAt(i)!!
            val v: View = LayoutInflater.from(requireContext()).inflate(R.layout.cuisine_text, null)
            //v.findViewById<TextView>(R.id.tabtext).text = text.get(i)
            v.findViewById<TextView>(R.id.tabtext).apply { this.text = text.get(i); this.setTextColor(ContextCompat.getColor(requireContext(),R.color.primaryDark)) }
            val img: ImageView = v.findViewById<ImageView>(R.id.tab_icon).apply { visibility = View.GONE }
            tab.customView = v
        }
    }

    private fun setObserver() {
        fireViewModel.observeDMParentChange().observe(viewLifecycleOwner, Observer {
            when(it){
                is ResultHandler.Success ->{
                    var list = it.value.map { it?.participants?.find { !it.equals(FirebaseAuth.getInstance().uid) } }
                    Log.i(TAG," OnlinepeopleList:::: ${list.size}")
                    fireViewModel.getOnlineStatus(list)
                    statusListener = FireBaseUtils.getOnlineStatusListener(list){
                        fireViewModel.getOnlineStatus(list)
                    }
                }
                is ResultHandler.Failure -> {
                    Log.e(TAG, "getting error to get AllChat ${it.throwable?.printStackTrace()}")
                }
            }
        })

        fireViewModel.observeOnlineStatus().observe(viewLifecycleOwner, Observer {
            when(it){
                is ResultHandler.Success ->{
                    adapter.setItem( it.value)
                }
                is ResultHandler.Failure -> {
                    Log.e(TAG, "getting error to get AllChat ${it.throwable?.printStackTrace()}")
                }
            }
        })
        userviewModel.observePeopleSearch().observe(viewLifecycleOwner, Observer {
            when(it){
                is ResultHandler.Success -> {
                    Log.i(TAG," startsearching:: datacame")
                    setSearchPeopleAdapter(it.value?:SearchPeopleResponse())
                }
                is ResultHandler.Failure ->{
                    Log.e(TAG," people Search Failed::: ")
                }
            }

        })
    }

    fun setSearchPeopleAdapter(list: SearchPeopleResponse){
        searchPeopleAdapter.setData(list)

    }


    class ViewPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount() = 3
        override fun getItem(position: Int): Fragment {
            return ChatPagerFragment.getInstance(position)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when(position) {
                0    -> "All"
                1    -> "Unread"
                2    -> "Pin"
                else -> ""
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        statusListener?.remove()
    }

    override fun onBackPageCome(){
        Log.i(TAG," clasCameOnTop:: ${this::class.simpleName}")
    }
}