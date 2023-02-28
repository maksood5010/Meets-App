package com.meetsportal.meets.ui.fragments.socialfragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.SinglePostCommnetAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentPostRepyBinding
import com.meetsportal.meets.networking.post.SinglePostCommentsItem
import com.meetsportal.meets.overridelayout.SpaceItemDecoration
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.viewmodels.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter

@AndroidEntryPoint
class SinglePostReplyFragment :BaseFragment() {

    val postViewModel: PostViewModel by viewModels()

    private var _binding: FragmentPostRepyBinding? = null
    private val binding get() = _binding!!

    var POST_ID :String? = null
    lateinit var adapter : SinglePostCommnetAdapter
    var parentCommentSubject : PublishSubject<SinglePostCommentsItem?> = PublishSubject.create()

    companion object {
        val TAG = SinglePostReplyFragment::class.simpleName!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPostRepyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setUp()
        addObserver()
    }

    private fun initView(view: View) {
//        binding.parentComment.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray1))
        binding.parentComment.tvUsercomment.maxLines = 3
//        binding.parentComment.cvCommentCard.setCardBackgroundColor(ContextCompat.getColor(requireContext(),R.color.extraLightGray))
        adapter = getSinglePostAdapter()
        binding.rvReplies.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false).apply { stackFromEnd = false }
        binding.rvReplies.addItemDecoration(SpaceItemDecoration(Utils.dpToPx(10f,requireContext().resources),0))
        binding.rvReplies.adapter = adapter
        binding.cancel.onClick({
            parentFragmentManager.popBackStack()
        })
        binding.parentComment.tvUsercomment.setOnClickListener {
            if (binding.parentComment.tvUsercomment.maxLines == Integer.MAX_VALUE) {
                binding.parentComment.tvUsercomment.maxLines = 2
            } else {
                binding.parentComment.tvUsercomment.maxLines = Integer.MAX_VALUE
            }
        }
    }

    private fun setUp() {
        setFragmentResultListener(TAG) {key, result->
            var commentID = result.getString("comment_id")
            Log.i(TAG, "setUp: checkingreply_commeID:: ${result?.getString("reply_comment_id")}")
            Log.i(TAG, "setUp: checkingcommeID:: ${result?.getString("comment_id")}")

            commentID?.let {
                Log.i(TAG, "setUp: checkingcommeID:: 1")
                POST_ID = result.getString("post_id")
                postViewModel.fetchCommnets(it,null,"comment",result?.getString("reply_comment_id"),parentCommentSubject)
            }
            Log.i(TAG, "setUp: checkingcommeID:: 3")
            val parentComment = result.getParcelable<SinglePostCommentsItem?>("parentComment")
            parentComment?.let{
                POST_ID = it.post_id
                populateData(it)
                //postViewModel.fetchCommnets(it._id,null,"comment")
            }
            val refresh = result.getBoolean("refresh",false)
            if(refresh){
                adapter.refresh()
                binding.rvReplies.scrollToPosition(0)

            }
        }

        parentCommentSubject
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it?.let {
                    populateData(it)
                }
            },{

            })
    }

    fun populateData(parentComment: SinglePostCommentsItem) {
        binding.parentComment.tvUsercomment.text = parentComment.body
        binding.parentComment.civImage.loadImage(requireContext(),parentComment.user_meta.profile_image_url,R.drawable.ic_default_person)
        binding.parentComment.tvUsername.text = parentComment.user_meta.username
        binding.parentComment.tvCreatedAt.text = Utils.getCreatedAt(parentComment.createdAt)
        binding.parentComment.tvLikecount.text = Utils.prettyCount(parentComment.stats.likes)
        binding.parentComment.viewReply.visibility = View.GONE
        binding.parentComment.tvReply.text = Utils.prettyCount(parentComment.stats.comments)
        binding.parentComment.civImage.loadImage(requireContext(),parentComment?.user_meta?.profile_image_url,R.drawable.ic_default_person,false)
        if(parentComment?.user_meta?.verified_user == true){
            binding.parentComment.ivVerify.visibility = View.VISIBLE
        }else{
            binding.parentComment.ivVerify.visibility = View.GONE
        }
        binding.parentComment.ivBadge.setImageResource(Utils.getBadge(parentComment.user_meta.badge).foreground)
    }

    fun getSinglePostAdapter(): SinglePostCommnetAdapter {
        var adapter = SinglePostCommnetAdapter(requireActivity(), this,postViewModel,true,{
            Log.i(TAG," comment li")
            postViewModel.toggleCommentLike(it)
        },{
            openProfile(it, Constant.Source.POST.sorce.plus(POST_ID))
        },{ it,position ->
            /*showIndicator(true,it)
            COMMENT_ID = it?._id
            COMMENT_POSITION = position*/
        },{ comment , position ,type->
            MyApplication.smallVibrate()
            postViewModel.deleteCommentReply(comment?._id)
           /* MyApplication.smallVibrate()
            if(type.equals(DetailPostFragment.TYPE_COMMENT)) {
                postViewModel.deleteComment(comment?._id)
            }else{
                postViewModel.deleteCommentReply(comment?._id)
            }*/
        },{it,position->//View Reply
            //showReplied(it)
        })
        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.filter{
                (it.refresh is LoadState.NotLoading ||it.refresh is LoadState.Error)
            }.collect {
                if(it.refresh is LoadState.Error){
                    activity?.onBackPressed()
                    return@collect
                }
            }
        }

        return adapter

    }

    private fun addObserver() {
        postViewModel.observePostCommentsDataSource().observe(viewLifecycleOwner,{
            adapter.submitData(lifecycle,it)
        })
        //commentLike
        postViewModel.observerLikeDislike().observe(viewLifecycleOwner,{
            adapter.refresh()
        })
        postViewModel.observeOnCommentDelete().observe(viewLifecycleOwner,  {
            //postViewModel.fetchSinglePost(POST_ID,MODE)
            adapter.refresh()
            //Toast.makeText(requireContext(),"Comment Deleted", Toast.LENGTH_SHORT).show()
//            ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager,0) as TimeLineFragment).timeLineAdapter.refresh()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onBackPageCome() {
       ////TODO("Not yet implemented")
    }
}