package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.DMParentAdapter
import com.meetsportal.meets.application.MyApplication.Companion.SID
import com.meetsportal.meets.databinding.FragmentChatPagerBinding
import com.meetsportal.meets.models.DMParent
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.firebase.FireBaseUtils
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.FilterBottomSheet
import com.meetsportal.meets.utils.Constant.TAG_CHAT_DASHBOARD_FRAGMENT
import com.meetsportal.meets.viewmodels.FireBaseViewModal
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatPagerFragment : BaseFragment() {

    private var dialog: FilterBottomSheet? = null
    val TAG = ChatPagerFragment::class.java.simpleName

    val fireViewModel: FireBaseViewModal by viewModels()
    val profileViewModel: UserAccountViewModel by viewModels()

    private var _binding: FragmentChatPagerBinding? = null
    private val binding get() = _binding!!

    lateinit var adapter: DMParentAdapter
    var clickedDMParent: DMParent? = null


    companion object {

        val PAGE_POSITION = "pagePosition"

        fun getInstance(pageNumber: Int): ChatPagerFragment {
            return ChatPagerFragment().apply {
                arguments = Bundle().apply {
                    putInt(PAGE_POSITION, pageNumber)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_chat_pager,container,false)
        _binding = FragmentChatPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //setupUI(binding.root)>
        initView(view)
        observer()
    }

    private fun initView(view: View) {
        val pos = arguments?.getInt(PAGE_POSITION)
        Log.i(TAG, " pageNumber::: $pos")
        binding.noData.noDataImage.setImageResource(R.drawable.no_chat)
        binding.noData.noDataDesc.text = "To Search a user Type at least 3 characters to search"
        if(pos == 0) {
            binding.noData.noDataHeading.text = "No Messages"
        } else if(pos == 1) {
            binding.noData.noDataHeading.text = "No Unread Messages"
        } else if(pos == 2) {
            binding.noData.noDataHeading.text = "No Pinned Messages"
        }
        adapter = DMParentAdapter(requireActivity(), {
            clickedDMParent = it
            initDialog()
            dialog?.show()
        }) {
            if(it) {
                binding.noData.noDataRoot.visibility = View.VISIBLE
            } else {
                binding.noData.noDataRoot.visibility = View.GONE
            }
        }
        binding.recycler.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        binding.recycler.adapter = adapter
    }

    private fun setUp() {
        fireViewModel.getAllChat()
        FireBaseUtils.getDMParentChatListener {
            Log.i(TAG, " getAllChat::: 00000")
            fireViewModel.getAllChat()
        }
    }

    override fun onResume() {
        super.onResume()
        setUp()
    }

    fun refreshChat() {
        var chatDashBoard = activity?.supportFragmentManager?.findFragmentByTag(TAG_CHAT_DASHBOARD_FRAGMENT) as ChatDashboardFragment

        for(i in 0..2) {
            Log.i(TAG, " loop111::: $i")
            (chatDashBoard.viewPagerAdapter.instantiateItem(chatDashBoard.binding.pager, i) as ChatPagerFragment).fireViewModel.getAllChat()
        }
    }

    fun showOption(sheet: BottomSheetDialogFragment?) {
        // var fragment = sheet as BottomSheetDialogFragment?
        if(sheet == null || sheet.isAdded) {
            return
        }
        sheet.show(this.childFragmentManager, sheet.tag)
    }

    private fun observer() {

        profileViewModel.observeOnBlockUser().observe(viewLifecycleOwner, Observer {
            if(it) {
                showToast("")
            }
        })
        fireViewModel.observeDMParentChange().observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    if(arguments?.getInt(PAGE_POSITION) == 0) {
                        Log.i(TAG, " pager 1")
                        adapter.setItem(it.value)
                    } else if(arguments?.getInt(PAGE_POSITION) == 1) {
                        Log.i(TAG, " pager 2")
                        adapter.setItem(filterUnreadData(it.value))
                    } else if(arguments?.getInt(PAGE_POSITION) == 2) {
                        Log.i(TAG, " pager 3")
                        adapter.setItem(it.value.filter { it?.pinned == true })
                    }
                }

                is ResultHandler.Failure -> {
                    Log.e(TAG, " failed::: ${it.throwable?.printStackTrace()}")
                }
            }
        })

        fireViewModel.observeOnPinnedChange().observe(viewLifecycleOwner, Observer {
            it.forEach {
                Log.i(TAG, " PinnedModel:: $it")
            }
        })
    }

    fun filterUnreadData(value: List<DMParent?>): List<DMParent?> {
        return value.filter {
            if(it?.id?.indexOf(FirebaseAuth.getInstance().uid!!) == 0) {
                it.unread1?.let {
                    if(it > 0) return@filter true
                }
            } else {
                it?.unread2?.let {
                    if(it > 0) return@filter true
                }
            }
            false
        }
    }


    fun initDialog() {
        var youBlock = false
        var blockText = "Block"
        var pin = "Pin"
        if(clickedDMParent?.pinned == true) {
            pin = "Unpin"
        }
        clickedDMParent?.blocked?.let {
            if(it.isNotEmpty()) {
                it.forEach { blocked: String ->
                    if(SID?.let { it1 -> blocked.indexOf(it1) } == 0) {
                        youBlock = true
                        blockText = "Unblock"
                    }
                }
            }
        }
        dialog = FilterBottomSheet(requireActivity(), arrayListOf(blockText, pin, "Delete Chat")) {
            when(it) {
                0 -> {
                    val sid: String? = clickedDMParent?.sids?.find { it != SID }
                    if(youBlock) {
                        val showProceed = showProceed { profileViewModel.unBlockUser(sid) }
                        showProceed.setMessage("Unblock", "Are you sure you want to Unblock this user?")
                    } else {
                        val showProceed = showProceed { profileViewModel.blockUser(sid) }
                        showProceed.setMessage("Block", "Are you sure you want to block this user?")
                    }
                    dialog?.dismiss()
                }

                1 -> {
                    if(clickedDMParent?.pinned == false) fireViewModel.setPinned(clickedDMParent?.sids?.find { it != SID }) {
                        Log.i(TAG, " dataInserted::: ")
                        if(it) refreshChat()
                    }
                    else fireViewModel.setUnPinned(clickedDMParent?.sids?.find { it != SID }) {
                        Log.i(TAG, " dataInserted::: ")
                        if(it) refreshChat()
                    }
                    dialog?.dismiss()
                }

                2 -> {
                    clickedDMParent?.id?.let { s ->
                        val showProceed = showProceed {
                            fireViewModel.deleteChat(s) {
                                if(it) {
                                    showToast("Chat Deleted Successfully")
                                }
                            }
                        }
                        showProceed.setMessage("Delete chat", "Are you sure you want to delete chat with ${clickedDMParent?.profiles?.find { it?.sid != SID }?.username} ?")
                    }
                    dialog?.dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }

}