package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.FollowingPagerAdapter
import com.meetsportal.meets.adapter.TimelineFooterStateAdapter
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.count
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FollowingFragment(var sid: String?) : BaseFragment() {

    val TAG = FollowingFragment::class.simpleName

    val profileViewModel: UserAccountViewModel by viewModels()

    lateinit var search: EditText
    lateinit var recycler: RecyclerView
    lateinit var adapter: FollowingPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_following, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)
        setUp()
        setObserver()
        //viewModel.populatePickImage(UploadPost())
        //chooseImage()

    }

    private fun initView(view: View) {
        search = view.findViewById(R.id.et_search)
        recycler = view.findViewById(R.id.recycler)
    }

    private fun setUp() {

        recycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        adapter = FollowingPagerAdapter(requireActivity(), this, { sid, isFollow ->
            followuser(sid, isFollow)
        }, {
            openProfile(it, Constant.Source.PROFILE.sorce.plus(sid))
        })
        recycler.adapter = adapter.withLoadStateFooter(TimelineFooterStateAdapter {
            adapter.retry()
        })
        profileViewModel.getFollowFollowingDataSource(sid, "followings", null)

        search.count {
            Log.i("TAG", " fatfat:: $it")
            if((it.toInt()) > 2 && search.text.toString().trim().length > 2) {
                profileViewModel.getFollowFollowingDataSource(sid, "followings", search.text.toString()
                        .trim())
            } else {
                profileViewModel.getFollowFollowingDataSource(sid, "followings", null)

            }
        }
    }

    private fun followuser(id: String?, forfollow: Boolean) {
        if(forfollow) {
            profileViewModel.followUser(id)
        } else {
            profileViewModel.unfollowUser(id)
        }
    }

    private fun setObserver() {
        profileViewModel.observeFollowFollowingDataSource().observe(viewLifecycleOwner, Observer {
            adapter.submitData(lifecycle, it)
        })

        profileViewModel.observeFollowUnFollow().observe(viewLifecycleOwner, Observer {
            //adapter.refresh()
        })
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }
}