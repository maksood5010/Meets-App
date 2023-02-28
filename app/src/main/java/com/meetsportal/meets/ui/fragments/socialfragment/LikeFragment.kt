package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.LikeAdapter
import com.meetsportal.meets.adapter.TimelineFooterStateAdapter
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.viewmodels.PostViewModel
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LikeFragment : BaseFragment() {

    val TAG = LikeFragment::class.simpleName

    val postViewModel: PostViewModel by viewModels()
    val profileViewmodel: UserAccountViewModel by viewModels()

    lateinit var recycler: RecyclerView
    lateinit var likeAdapter: LikeAdapter
    lateinit var noData: View

    companion object {

        val POST_ID = "postID"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_liked_user, container, false)
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

        recycler = view.findViewById(R.id.recycler)
        noData = view.findViewById(R.id.npDataLayout)
        view.findViewById<ImageView>(R.id.back).setOnClickListener { activity?.onBackPressed() }
    }

    private fun setUp() {
        recycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        likeAdapter = LikeAdapter(requireActivity(), this, { sid, isFollow ->
            followuser(sid, isFollow)
        }, {
            openProfile(it, Constant.Source.POST.sorce.plus(Navigation.getFragmentData(this, POST_ID)))
        })
        recycler.adapter = likeAdapter.withLoadStateFooter(TimelineFooterStateAdapter {
            likeAdapter.retry()
        })


        var postID = Navigation.getFragmentData(this, POST_ID)
        postViewModel.getLikePageDataSource(postID)

    }

    private fun followuser(id: String?, forfollow: Boolean) {
        if(forfollow) {
            profileViewmodel.followUser(id)
        } else {
            profileViewmodel.unfollowUser(id)
        }
    }

    private fun setObserver() {

        postViewModel.observePostLikeDataSource().observe(viewLifecycleOwner, Observer {
            likeAdapter.submitData(lifecycle, it)
        })
        profileViewmodel.observeFollowUnFollow().observe(viewLifecycleOwner, Observer {
            likeAdapter.refresh()

        })
        profileViewmodel.observeUnblockUser().observe(viewLifecycleOwner, {
            showToast("you have Unblocked")
            likeAdapter.refresh()
        })
    }

    override fun onBackPageCome() {
        likeAdapter.refresh()
    }
}