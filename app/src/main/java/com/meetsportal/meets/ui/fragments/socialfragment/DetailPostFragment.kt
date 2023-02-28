package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.fenchtose.tooltip.Tooltip
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.*
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentDetailedPost2Binding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.exception.ApiException
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.meetup.MeetStatus
import com.meetsportal.meets.networking.post.*
import com.meetsportal.meets.networking.profile.Badge
import com.meetsportal.meets.networking.profile.Definition
import com.meetsportal.meets.networking.profile.FullInterestGetResponse
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.overridelayout.AppBarLayoutBehavior
import com.meetsportal.meets.ui.activities.MainActivity
import com.meetsportal.meets.ui.dialog.CustomPlaceDetailAlert
import com.meetsportal.meets.ui.dialog.IAmHereAlert
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.NoContentFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.AcDeletePost
import com.meetsportal.meets.utils.Constant.AcDisableCmnt
import com.meetsportal.meets.utils.Constant.TAG_EDIT_POST_FRAGMENT
import com.meetsportal.meets.utils.Constant.VwImagePostViewed
import com.meetsportal.meets.utils.Constant.VwOPMeetPostViewed
import com.meetsportal.meets.utils.Constant.VwTextPostViewed
import com.meetsportal.meets.utils.Constant.badgeList
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.meetsportal.meets.viewmodels.PostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

import androidx.recyclerview.widget.SnapHelper

@AndroidEntryPoint
class DetailPostFragment : BaseFragment(), BottomSheetOptions.BottomSheetListener {

    private var isShown: Boolean = false

    //    private var iamHereID: String? = null
    var position = 0
    var isUserLiked: Boolean? = false

    //val viewModel : NewsFeedViewmodel by viewModels()
    val postViewModel: PostViewModel by viewModels()
    val meetUpViewModel: MeetUpViewModel by viewModels()
    var type = false
    lateinit var adapter: PostCommentAdapter
    lateinit var singlePostCommnetAdapter: SinglePostCommnetAdapter
    lateinit var adapterTag: OpenMeetupTagAdapter
    var currentUser: ProfileGetResponse? = null
    var isPostLiked = false

    var myPostOptions: BottomSheetOptions? = null
    var otherPostOptions: BottomSheetOptions? = null
    private var iAmHereAlert: IAmHereAlert? = null
    var locationFinder: LocationFinder? = null
    var customPlaceAlert: CustomPlaceDetailAlert? = null


    var isReplyingComment = false

    var POST_ID: String? = null
    var MeetId: String? = null
    lateinit var parentView: View
    var MODE: String? = null
    var COMMENT_ID: String? = null
    var COMMENT_POSITION = 0


    companion object {

        val TAG = DetailPostFragment::class.java.simpleName
        val TYPE_COMMENT = "commentType"
        val TYPE_REPLY_COMMENT = "replyComment"
    }

    private var _binding: FragmentDetailedPost2Binding? = null
    private val binding get() = _binding!!
    var replyFragment = SinglePostReplyFragment()


    var meetOptions = BottomSheetOptions.getInstance("Close this  meetup", "Disable comments on this meetup", "Enable comments on this meetup", "Cancel this meetup", 4)
            .apply {
                setBottomSheetLitener(object : BottomSheetOptions.BottomSheetListener {
                    override fun bottomSheetClickedOption(buttonClicked: String) {
                        when(buttonClicked) {
                            BottomSheetOptions.BUTTON1 -> {
                                meetUpViewModel.closeOpenMeet(MeetId)
                            }

                            BottomSheetOptions.BUTTON2 -> {
                                MyApplication.putTrackMP(AcDisableCmnt, JSONObject(mapOf("postId" to POST_ID)))
                                postViewModel.disableComment(POST_ID, false)
                            }

                            BottomSheetOptions.BUTTON3 -> {
                                postViewModel.disableComment(POST_ID, true)
                            }

                            BottomSheetOptions.BUTTON4 -> {
                                meetUpViewModel.cancleMeetUp(MeetId)
                            }

                            BottomSheetOptions.CANCEL  -> {

                            }
                        }
                    }
                })
            }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return inflater.inflate(R.layout.fragment_detailed_post2,container,false)
        _binding = FragmentDetailedPost2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setUp()
        addObserver()
    }

    private fun initView(view: View) {

        customPlaceAlert = CustomPlaceDetailAlert(requireActivity())
        binding.tvPost.setOnTouchListener(null)
        parentView = view

        // binding.appbar.
        val params: CoordinatorLayout.LayoutParams = binding.appbar.layoutParams as CoordinatorLayout.LayoutParams
        params.behavior = AppBarLayoutBehavior(requireContext(), null)
        binding.appbar.requestLayout()


        val list: ArrayList<String> = arrayListOf("💜", "🔥", "😂", "🤣", "😍", "😘", "👏", "😰", "😫", "😅", "😲")
        binding.rvEmoji.adapter = EmojiListAdapter(requireContext(), list) {
            binding.etComment.append("$it")
        }

        binding.header.tvSuperCharge.setGradient(requireActivity(), GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.parseColor("#32BFC9"), Color.parseColor("#1D55A2")), 20f)


        binding.header.ppOpenMeet.tvSuperCharge1.setGradient(requireActivity(), GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.parseColor("#32BFC9"), Color.parseColor("#1D55A2")), 20f)

        myPostOptions = BottomSheetOptions.getInstance("Edit", "Delete", null, null, null)
        otherPostOptions = BottomSheetOptions.getInstance("Report", null, null, null, null)
        myPostOptions?.setBottomSheetLitener(object : BottomSheetOptions.BottomSheetListener {
            override fun bottomSheetClickedOption(buttonClicked: String) {
                when(buttonClicked) {
                    BottomSheetOptions.BUTTON1 -> {
                        MyApplication.putTrackMP(Constant.AcEditImagePost, JSONObject(mapOf("postId" to POST_ID)))
                        var baseFragment: BaseFragment = EditPostFragment()
                        Navigation.setFragmentData(baseFragment, "postId", POST_ID)
                        Navigation.addFragment(requireActivity(), baseFragment, TAG_EDIT_POST_FRAGMENT, R.id.homeFram, true, false)
                    }

                    BottomSheetOptions.BUTTON2 -> {
                        MyApplication.putTrackMP(AcDeletePost, JSONObject(mapOf("postId" to POST_ID)))
                        postViewModel.deletePost(POST_ID)
                        //deletePost(selectedPost?._id)
                    }

                    BottomSheetOptions.CANCEL  -> {

                    }
                }
            }
        })

    }

    fun setUp() {
        /*var post = arguments?.getParcelable<DetailPostParcel>("post")
        MODE = post?.mode*/
        MODE = arguments?.getString("mode")
        //Log.i("TAG"," serializationCheck:: ${post}   ")
        Log.i("TAG", " Mode:: ${MODE}   ")
        currentUser = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)

        adapter = getPostCommentAdapter()
        singlePostCommnetAdapter = getSinglePostAdapter()

        //binding.rvComments.adapter = adapter
        binding.rvComments.adapter = singlePostCommnetAdapter

        binding.header.tvLike.setOnClickListener {
            var baseFragment: BaseFragment = LikeFragment()
            Navigation.setFragmentData(baseFragment, LikeFragment.POST_ID, POST_ID)
            Navigation.addFragment(requireActivity(), baseFragment, Constant.TAG_LIKE_FRAGMENT, R.id.homeFram, true, false)
        }
        /* post?.let { post->

             POST_ID = post?._id
             postViewModel.fetchSinglePost(post._id,post?.mode)
             Log.i(TAG," checkingPostCommentId:: 1 ${post._id} -- ${arguments?.getString("comment_id")}")
             postViewModel.fetchCommnets(post._id,post?.mode,"post",arguments?.getString("comment_id"))
             arguments?.getString("reply_comment_id")?.let {
                 showReplied(null)
             }
         }?:run{*/
        var postID = Navigation.getFragmentData(this, "post_id")
        POST_ID = postID
        postViewModel.fetchSinglePost(postID, MODE)
        Log.i(TAG, " checkingPostCommentId:: 2 ${postID} -- ${arguments?.getString("comment_id")}")
        postViewModel.fetchCommnets(postID, MODE, "post", arguments?.getString("comment_id"))
        arguments?.getString("reply_comment_id")?.let {
            showReplied(null)
        }
        //}

        binding.rvComments.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false).apply { stackFromEnd = false }
        //Utils.stickImage(requireContext(),binding.postCommentDp,currentUser?.cust_data?.profile_image_url,null)
        binding.postCommentDp.loadImage(requireContext(), currentUser?.cust_data?.profile_image_url, R.drawable.ic_default_person)
        binding.etComment.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_SEND) {
                postComment()
                true
            }
            false
        }
        binding.replyingCancel.setOnClickListener {
            showIndicator(false, null)
        }

    }

    fun populateView(value: SinglePostResponse?) {
        //
        Log.d(TAG, "populateView: $value")
        binding.tvPost.setOnClickListener {
            postComment()
            //singlePostCommnetAdapter.refresh()
        }
        binding.real.visibility = View.VISIBLE
        if(value?.comments_enabled == false) binding.clCommentPost.visibility = View.GONE
        else binding.clCommentPost.visibility = View.VISIBLE

        Log.d(TAG, "populateView: 1")
        binding.shimmer.stopShimmer()
        binding.shimmer.visibility = View.GONE
        binding.header.ivHeart.setOnClickListener {
            likeDislikePost(value)
        }
        binding.header.postUserName.text = value?.user_meta?.username

        value?.user_meta?.badge?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            binding.header.ivDpBadge.setImageResource(firstOrNull.foreground)
        }
        binding.header.tvLike.text = value?.likes_count.toString().plus(" likes")
        //binding.header.postDescription.text = value?.body

        Log.d(TAG, "populateView: 2")
        binding.header.postDescription.setText(Utils.setMentionInCaption(requireActivity(), value?.body ?: "", value?.mentions, value?._id), TextView.BufferType.SPANNABLE)
        binding.header.postDescription.setMovementMethod(LinkMovementMethod.getInstance())

        binding.header.tvComment.text = (value?.stats?.comments).toString().plus(" comments")
        isUserLiked = value?.liked_by_user
        if(value?.liked_by_user == true) {
            binding.header.ivHeart.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_filled))
            isPostLiked = true
        } else {
            binding.header.ivHeart.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_small_heart))
            isPostLiked = false
        }
        Log.d(TAG, "populateView: 3 ${value?.type}")
        if(value?.user_meta?.sid == MyApplication.SID && value?.type != Constant.Post.CHECK_IN.type) {
            binding.header.tvSuperCharge.visibility = View.VISIBLE
            showTool(value?.type == Constant.Post.OPEN_MEET.type)
            binding.header.ppOpenMeet.tvSuperCharge1.visibility = View.VISIBLE
            binding.header.tvSuperCharge.onClick({
                val superChargeFragment = SuperChargeFragment()
                Navigation.setFragmentData(superChargeFragment, "post_id", value?._id)
                Navigation.addFragment(requireActivity(), superChargeFragment, "SuperChargeFragment", R.id.homeFram, true, null)
            })
            binding.header.ppOpenMeet.tvSuperCharge1.onClick({
                val superChargeFragment = SuperChargeFragment()
                Navigation.setFragmentData(superChargeFragment, "post_id", value?._id)
                Navigation.addFragment(requireActivity(), superChargeFragment, "SuperChargeFragment", R.id.homeFram, true, null)
            })
        } else {
            binding.header.tvSuperCharge.visibility = View.GONE
            binding.header.ppOpenMeet.tvSuperCharge1.visibility = View.GONE
        }
        Log.d(TAG, "populateView: 4")
        binding.header.postDp.setOnClickListener { view ->
            openProfile(value?.user_meta?.sid, Constant.Source.POST.sorce.plus(value?._id))

            /* if(value?.user_meta?.sid == MyApplication.SID){
                 Navigation.addFragment(
                     requireActivity(),
                     ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
                 )
             }
             else{
                 var baseFragment : BaseFragment = OtherProfileFragment.getInstance(value?.user_meta?.sid)
                 *//*baseFragment = Navigation.setFragmentData(baseFragment,
                    OtherProfileFragment.OTHER_USER_ID,
                    value?.user_meta?.sid)*//*
                Navigation.addFragment(requireActivity(),baseFragment,
                    Constant.OTHER_PROFILE_FRAGMENT,R.id.homeFram,true,false)
            }*/
        }
        binding.header.postUserName.setOnClickListener { view ->
            openProfile(value?.user_meta?.sid, Constant.Source.POST.sorce.plus(value?._id))
            /*if(value?.user_meta?.sid == MyApplication.SID){
                Navigation.addFragment(
                    requireActivity(),
                    ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
                )
            }
            else{
                var baseFragment : BaseFragment = OtherProfileFragment.getInstance(value?.user_meta?.sid)
               *//* baseFragment = Navigation.setFragmentData(baseFragment,
                    OtherProfileFragment.OTHER_USER_ID,
                    value?.user_meta?.sid)*//*
                Navigation.addFragment(requireActivity(),baseFragment,
                    Constant.OTHER_PROFILE_FRAGMENT,R.id.homeFram,true,false)
            }*/
        }
        binding.header.tvCreatedAt.text = Utils.getCreatedAt(value?.createdAt)
        //Utils.stickImage(requireContext(),binding.header.postDp,value?.user_meta?.profile_image_url,null)
        binding.header.postDp.loadImage(requireContext(), value?.user_meta?.profile_image_url, R.drawable.ic_default_person)
        if(value?.user_meta?.verified_user == true) {
            binding.header.ivVerify.visibility = View.VISIBLE
        }

        Log.d(TAG, "populateView: 5")
        /*if(value?.comments?.size?.compareTo(0) == 1) {
            Log.i(TAG,"no data VisibilityGone:: ")
            binding.noComment.visibility = View.GONE
            adapter.setPostowner(value?.user_meta?.sid == MyApplication.SID)
            adapter.updatelist(value?.comments?.reversed())
            adapter.notifyDataSetChanged()
        }else{
            binding.noComment.visibility = View.VISIBLE
            adapter.setPostowner(value?.user_meta?.sid == MyApplication.SID)
            adapter.updatelist(value?.comments?.reversed())
            adapter.notifyDataSetChanged()
        }*/


        Log.d(TAG, "populateView: 6")
        var commentid = Navigation.getFragmentData(this, "comment_id")
        Log.i(TAG, "comment_id::::::::: $commentid")

        /*if(!commentid.isNullOrEmpty()){
            value?.comments?.reversed()?.indexOfFirst { it._id.equals(commentid) }?.let{
                Log.i(TAG,"comment_id:: ${commentid}")
                Log.i(TAG," checkingScrollPostion:: $it ")
                binding.appbar.setExpanded(false,true)
                binding.rvComments.smoothScrollToPosition(it)
                var replyid = Navigation.getFragmentData(this,"reply_comment_id")
                if(!replyid.isNullOrEmpty()){
                    adapter.expandReplyCommentId(it,replyid)
                }

            }
        }*/

        Log.d(TAG, "populateView: 7")

        Log.i(TAG, " PostType:: ${value?.type}")
        when(value?.type) {
            Constant.Post.TEXT_POST.type -> {
                Log.d(TAG, "populateView: 8")
                binding.header.rvPostImage.visibility = View.GONE
                binding.header.ppOpenMeet.root.visibility = View.GONE
                binding.header.ppTextPost.root.visibility = View.VISIBLE
                binding.header.clPostDetail.visibility = View.VISIBLE
                binding.header.postDescription.visibility = View.GONE
                populateTextPost(value)
            }

            Constant.Post.DEFAULT.type   -> {
                Log.d(TAG, "populateView: 9")
                binding.header.rvPostImage.visibility = View.VISIBLE
                binding.header.ppOpenMeet.root.visibility = View.GONE
                binding.header.ppTextPost.root.visibility = View.GONE
                binding.header.clPostDetail.visibility = View.VISIBLE
                binding.header.postDescription.visibility = View.VISIBLE
                populateImagePost(value)
            }

            Constant.Post.OPEN_MEET.type -> {
                Log.d(TAG, "populateView: 10")
                binding.header.rvPostImage.visibility = View.GONE
                binding.header.ppOpenMeet.root.visibility = View.VISIBLE
                binding.header.ppTextPost.root.visibility = View.GONE
                binding.header.clPostDetail.visibility = View.GONE
                binding.header.postDescription.visibility = View.GONE
                populateMeetPost(value)
            }

            Constant.Post.CHECK_IN.type  -> {
                Log.d(TAG, "populateView: 11")
                binding.header.rvPostImage.visibility = View.VISIBLE
                binding.header.ppOpenMeet.root.visibility = View.GONE
                binding.header.ppTextPost.root.visibility = View.GONE
                binding.header.clPostDetail.visibility = View.VISIBLE
                binding.header.postDescription.visibility = View.VISIBLE
                populateImagePost(value)
                setupCheckIn(value)
            }

            else                         -> {
                Log.d(TAG, "populateView: 12")
                binding.header.rvPostImage.visibility = View.VISIBLE
                binding.header.ppOpenMeet.root.visibility = View.GONE
                binding.header.ppTextPost.root.visibility = View.GONE
                binding.header.clPostDetail.visibility = View.VISIBLE
                binding.header.postDescription.visibility = View.VISIBLE
                populateImagePost(value)
            }
        }
        Log.i(TAG, " commentSize:: ${value?.comments?.size}")

    }

    private fun showTool(b: Boolean) {
        var view = binding.header.tvSuperCharge
        if(b) {
            view = binding.header.ppOpenMeet.tvSuperCharge1
        }
        if(!isShown) {
            isShown = true
            Utils.showToolTips(requireActivity(), view, binding.root, Tooltip.BOTTOM, "Super charge post to get more views On your profile and posts", "tvSuperCharge") {}
        }
    }

    private fun setupCheckIn(value: SinglePostResponse) {
        binding.header.clCheckIn.visibility = View.VISIBLE
        value.body_obj?.check_in?.let { checkIn ->
            binding.header.tvTitle.text = checkIn.name?.en
            if(checkIn.timings?.isNotEmpty() == true && checkIn.timings?.getOrNull(0)?.opentime != null) {
                binding.header.tvTiming.text = "Open: ".plus(checkIn.timings?.get(0)?.opentime)
                        .plus(" - ").plus(checkIn.timings?.get(0)?.closetime)
            } else {
                binding.header.tvTiming.visibility = View.GONE
            }
            checkIn.rating?.let {
                binding.header.rbRating.rating = it
                binding.header.tvRate.text = "5 of ".plus(it)
            } ?: run {
                binding.header.rbRating.visibility = View.GONE
                binding.header.tvRate.visibility = View.GONE
            }
        }
    }

    fun populateImagePost(value: SinglePostResponse?) {
        MyApplication.putTrackMP(VwImagePostViewed, JSONObject(mapOf("postId" to value?._id)))


        binding.header.imageNumber.text = "1/".plus(value?.media?.size)
        var manager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        binding.header.rvPostImage.layoutManager = manager
        binding.header.rvPostImage.adapter = CardImageDetailPostAdapter(requireActivity(),value?.media)
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.header.rvPostImage)

        binding.header.rvPostImage.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                //super.onScrollStateChanged(recyclerView, newState)
                when(newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        binding.header.imageNumber.text = "".plus(manager.findFirstCompletelyVisibleItemPosition().plus(1)).plus("/")
                            .plus(value?.media?.size)

                        /*if(!arrayListOf(-1, 0).contains(manager.findFirstCompletelyVisibleItemPosition())) {
                            Log.i(TAG, " postPosition::  POSTID-- ${
                                timeLineAdapter.getPostIdFromIndex(timeLineLayoutmanager.findFirstCompletelyVisibleItemPosition())
                            }")
                            timeLineAdapter.getPostIdFromIndex(timeLineLayoutmanager.findFirstCompletelyVisibleItemPosition())
                                ?.let {
                                    postViewModel.ingestPost(it)
                                }
                        }*/
                    }
                }

            }
        })

        /*var imagelist = ArrayList<String>().also { list ->
            list.addAll(value?.media as Collection<String>)
            binding.header.vpPostImage.adapter = DetailPostImageViewPagerAdapter(binding.header.vpPostImage, requireActivity(), true, value, { garbage ->
                if(value.user_meta.sid.equals(MyApplication.SID) == true) showOption(myPostOptions) //showMyPostOptionFragment()
                else showOption(otherPostOptions)//showOtherPostOptionFragment()
                true
            }, {
                if(it == true) activity?.onBackPressed()
            })
        }
        binding.header.imageNumber.text = "1/".plus(value?.media?.size)
        binding.header.vpPostImage.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                binding.header.imageNumber.text = position.plus(1).toString().plus("/")
                        .plus(value?.media?.size)
            }

            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}

        })*/
    }

    fun populateTextPost(value: SinglePostResponse?) {
        //binding.header.ppTextPost.etTextpost.text = value?.body
        MyApplication.putTrackMP(VwTextPostViewed, JSONObject(mapOf("postId" to value?._id)))

        binding.header.ppTextPost.ivBack.onClick({ activity?.onBackPressed() })
        binding.header.ppTextPost.etTextpost.setText(Utils.setMentionInCaption(requireActivity(), value?.body, value?.mentions, value?._id), TextView.BufferType.SPANNABLE)
        binding.header.ppTextPost.etTextpost.setMovementMethod(LinkMovementMethod.getInstance())
        //binding.header.ppTextPost.etTextpost.setText("md bchjbch")

        value?.body_obj?.text_post?.let { textPost ->
            binding.header.ppTextPost.image.loadImage(requireActivity(), value.user.profile_image_url, R.drawable.ic_default_person, false)
            binding.header.ppTextPost.title.text = value.user_meta.username.plus("`s post")
            var gradient = Constant.GradientTypeArray.firstOrNull() { it.label.equals(textPost.gradient_type) }
            /*if(index != -1)
            binding.header.etTextpost.background = ContextCompat.getDrawable(requireActivity(),BodyObj.gradArray.get(index))*/
            gradient?.let {
                //binding.header.etTextpost.background = Utils.gradientFromColor(it.gradient)
                //binding.header.etTextpost.setTextColor(Color.parseColor(it.textColor))
            } ?: run {
                //binding.header.etTextpost.background = Utils.gradientFromColor(GradientTypeArray.first().gradient)
                //binding.header.etTextpost.setTextColor(Color.parseColor("#ffffff"))
            }
        } ?: run {
            //binding.header.etTextpost.background = Utils.gradientFromColor(GradientTypeArray.first().gradient)
            //binding.header.etTextpost.setTextColor(Color.parseColor("#ffffff"))
        }
        binding.header.postDescription.visibility = View.GONE
    }

    fun populateMeetPost(value: SinglePostResponse) {
        //binding.header.ppOpenMeet.
        MyApplication.putTrackMP(VwOPMeetPostViewed, JSONObject(mapOf("postId" to value?._id)))

        binding.header.ppOpenMeet.rlIntract.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.gray1))
        binding.header.ppOpenMeet.monthOfYear.setGradient(requireActivity(), GradientDrawable.Orientation.TL_BR, intArrayOf(ContextCompat.getColor(requireActivity(), R.color.primaryDark), ContextCompat.getColor(requireActivity(), R.color.gred_red), ContextCompat.getColor(requireActivity(), R.color.gred_red)))
        value?.body_obj?.open_meetup?.let {
            MeetId = it.meetup_id
            type = false;
            meetUpViewModel.getMeetUpDetail(it.meetup_id)
            binding.header.ppOpenMeet.ivBack.setOnClickListener { activity?.onBackPressed() }
            binding.header.ppOpenMeet.title.text = value.user_meta.username.plus("`s open meetup")
            binding.header.ppOpenMeet.meetName.text = it.name
            binding.header.ppOpenMeet.tvCreated.text = "Created on : ".plus(value.createdAt.toDate()
                    ?.formatTo("dd MMM yyyy"))
            binding.header.ppOpenMeet.monthOfYear.text = it.date?.toDate()?.formatTo("MMM")
            binding.header.ppOpenMeet.dayOfWeak.text = it.date?.toDate()?.formatTo("EEE")
            binding.header.ppOpenMeet.timeOfDay.text = it.date?.toDate()?.formatTo("hh:mmaa")
            binding.header.ppOpenMeet.dayOfMonth.text = it.date?.toDate()?.formatTo("dd")
            binding.header.ppOpenMeet.clockDate.text = it.date?.toDate()?.formatTo("EEEE, MMMM dd")
            binding.header.ppOpenMeet.clockTime.text = it.date?.toDate()?.formatTo("hh:mmaa")
            binding.header.ppOpenMeet.desc.text = it.date?.toDate()?.formatTo("hh:mmaa")
            binding.header.ppOpenMeet.image.loadImage(requireContext(), value.user.profile_image_url, R.drawable.ic_default_person)
            val badge = Utils.getBadge(it.min_badge)
            binding.header.ppOpenMeet.tvMinBadge.text = "${badge.name} Status +"

            val allInterest: List<Definition?> = PreferencesManager.get<FullInterestGetResponse>(Constant.PREFRENCE_INTEREST)?.definition ?: java.util.ArrayList()
            val tags = allInterest.filter { it1 -> it.interests?.contains(it1?.key) == true }
//            tags.addAll()
//            it.interests?.forEach { interest->
//                tags.add(allInterest.firstOrNull{ it?.key==interest })
//            }
            adapterTag = OpenMeetupTagAdapter(requireActivity(), tags)
            val layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
                alignItems = AlignItems.FLEX_START
            }
            binding.header.ppOpenMeet.rvTags.layoutManager = layoutManager
            binding.header.ppOpenMeet.rvTags.adapter = adapterTag
            it.date?.toDate()?.let { it1 ->
                it.duration?.toDate()?.let { d ->
                    if(d.equals(it1)) {
                        binding.header.ppOpenMeet.tvDuration.text = "Full Day"
                    } else {
                        val hours = Utils.getHourDiff(d, it1)
                        binding.header.ppOpenMeet.tvDuration.text = "${hours}hrs"
                    }
                } ?: run {
                    binding.header.ppOpenMeet.tvDuration.text = "N/A"
                }
                if(it.date.equals(it.max_join_time?.value)) {
                    binding.header.ppOpenMeet.tvJoinTime.text = "Until meetup"
                } else {
                    it.max_join_time?.value?.toDate()?.let { join ->
                        it.created_at?.toDate()?.let { c ->
                            val h = Utils.getHourDiff(join, Date())
                            val seconds = (it1.time - Date().time) / 1000
                            val minutes = seconds / 60
                            val days = h / 24
                            val hours = h % 24
                            if(days == 0L) {
                                if(hours == 0L) {
                                    binding.header.ppOpenMeet.tvJoinTime.text = "$minutes minutes"
                                } else {
                                    binding.header.ppOpenMeet.tvJoinTime.text = "${hours}hrs"
                                }
                            } else {
                                binding.header.ppOpenMeet.tvJoinTime.text = "${days}days ${hours}hrs"
                            }
                        }
                    }
                }
            }
            binding.header.ppOpenMeet.image.onClick({
                MyApplication.putTrackMP(Constant.AcOMDetailProfile, JSONObject(mapOf("sid" to value?.user_meta?.sid)))
                openProfile(value?.user_meta?.sid, Constant.Source.POST.sorce.plus(value?._id))

                /*if(value.user_id == MyApplication.SID){
                    Navigation.addFragment(requireActivity(), ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false)
                }else{
                    var baseFragment : BaseFragment = OtherProfileFragment.getInstance(value.user_id)
                    *//*baseFragment = Navigation.setFragmentData(baseFragment,
                        OtherProfileFragment.OTHER_USER_ID,
                        value.user_id)*//*
                    Navigation.addFragment(requireActivity(),baseFragment,
                        Constant.OTHER_PROFILE_FRAGMENT,R.id.homeFram,true,false)
                }*/
            }, 500)

            val progress: Int = 0
            try {
                val progress = Utils.getDayDiff(it.date?.toDate(), Date())?.times(100)
                        ?.div(Utils.getDayDiff(it.date?.toDate(), it.created_at?.toDate()) ?: 0L)
                        ?.toInt()
                if(progress?.compareTo(0) == 1) {
                    binding.header.ppOpenMeet.progress.progress = 100 - progress
                } else {
                    binding.header.ppOpenMeet.progress.progress = 100
                }

            } catch(e: Exception) {
                binding.header.ppOpenMeet.progress.progress = 0
            }
            Log.i(TAG, " progress::  $progress")

            if(DateUtils.isToday(it.date?.toDate()?.time ?: Date().time)) {
                Log.i(TAG, " checking::: 11")
                binding.header.ppOpenMeet.tvDaysPend.text = "Meetup happening"
                binding.header.ppOpenMeet.progress.progress = 100
            } else {
                Log.i(TAG, " checking::: 12")
                TimeUnit.DAYS.convert(it.date?.toDate()?.time?.minus(Date().time) ?: 0, TimeUnit.MILLISECONDS)
                        .let { days ->
                            binding.header.ppOpenMeet.tvDaysPend.text = if(days > 0L) {
                                Log.i(TAG, " checking::: 13")
                                days.toString().plus(" More days to start")
                            } else if(days == 0L) {
                                binding.header.ppOpenMeet.progress.progress = 100
                                TimeUnit.HOURS.convert(it.date?.toDate()?.time?.minus(Date().time) ?: 0, TimeUnit.MILLISECONDS)
                                        .toString().plus(" More hour to start")
                                // "Meetup happening"
                            } else {
                                Log.i(TAG, " checking::: 15")
                                "Meetup closed"
                            }
                        }
            }

            /*TimeUnit.DAYS.convert(it.date?.toDate()?.time?.minus(Date().time)?:0,TimeUnit.MILLISECONDS).let {
                binding.header.ppOpenMeet.tvDaysPend.text = if(it > 0L){
                    it.toString().plus(" More days to start")
                }else if(it == 0L){
                    "Meetup happning"
                }else{
                    "Meetup closed"
                }
            }*/
            //binding.header.ppOpenMeet.tvDaysPend.text = TimeUnit.DAYS.convert(it.date?.toDate()?.time?.minus(Date().time)?:0,TimeUnit.MILLISECONDS).toString().plus(" More days to start")
            if(!value.user_meta.sid.equals(MyApplication.SID)) {
                binding.header.ppOpenMeet.option.visibility = View.GONE
            }
            binding.header.ppOpenMeet.option.onClick({

                openOption(value, it)
            })


            when(it.chosen_place?.type) {
                Constant.PlaceType.MEET.label   -> {
                    MyApplication.putTrackMP(Constant.AcOMDetailLocation, JSONObject(mapOf("postId" to POST_ID)))
                    binding.header.ppOpenMeet.address.text = it.places?.firstOrNull()?.name?.en
                    binding.header.ppOpenMeet.address.onClick({
                        MyApplication.putTrackMP(Constant.AcOMDetailLocation, JSONObject(mapOf("postId" to POST_ID)))
                        var baseFragment: BaseFragment = RestaurantDetailFragment();
                        Navigation.setFragmentData(baseFragment, "_id", it.places?.firstOrNull()?._id)
                        Navigation.addFragment(requireActivity(), baseFragment, RestaurantDetailFragment.TAG, R.id.homeFram, true, false)
                    })
                }

                Constant.PlaceType.CUSTOM.label -> {
                    binding.header.ppOpenMeet.address.setOnClickListener(null)
                    binding.header.ppOpenMeet.address.text = it.custom_places?.firstOrNull()?.name
                    binding.header.ppOpenMeet.address.onClick({
                        MyApplication.putTrackMP(Constant.AcOMDetailLocation, JSONObject(mapOf("postId" to POST_ID)))
                        showCustomPlaceAlert(LatLng(it.custom_places?.firstOrNull()
                                ?.getLongitude() ?: 0.0, it.custom_places?.firstOrNull()
                                ?.getLatitude() ?: 0.0))
                    })
                }
            }

            var minBadge = badgeList.firstOrNull { badge -> badge.key == it.min_badge }
            val wrapper = ContextThemeWrapper(requireContext(), minBadge?.style ?: R.style.bronze)
            val drawable: Drawable? = VectorDrawableCompat.create(resources, R.drawable.ic_trophy_dummy, wrapper.getTheme())
            binding.header.ppOpenMeet.trophy.setImageDrawable(drawable)

            if(it.join_requests?.requests?.size?.compareTo(0) == 1) {
                binding.header.ppOpenMeet.llNoRequest.visibility = View.GONE
                binding.header.ppOpenMeet.llRequest.visibility = View.VISIBLE
                if(it.join_requests?.requests?.size == 1) binding.header.ppOpenMeet.countInterested.text = "1 person interested"
                else if(it.join_requests?.requests?.size > 1) binding.header.ppOpenMeet.countInterested.text = it.join_requests?.requests?.size.toString()
                        .plus(" people interested")
                //binding.header.ppOpenMeet.rvInterested.adapter = OpenMeetJoinRqPeopleStackAdapter(requireActivity(),it)
                binding.header.ppOpenMeet.countInterested.onClick({
                    if(value.user_id.equals(MyApplication.SID)) {
                        navigate(RequestToJoinOpenFragment.getInstance(it.meetup_id), anim = true)
                    } else {
                        val fragment = OpenMeetIntrestList.getInstance(it.meetup_id)
                        navigate(fragment, anim = true)
                    }
                })
            } else {
                binding.header.ppOpenMeet.llNoRequest.visibility = View.VISIBLE
                binding.header.ppOpenMeet.llRequest.visibility = View.GONE
                if(value.user_id.equals(MyApplication.SID)) {
                    binding.header.ppOpenMeet.tvFirst.text = "Invite people to Join meetup"
                }
                //binding.header.ppOpenMeet.rvIntrested.adapter = OpenMeetJoinRqPeopleStackAdapter(requireActivity(),it)
            }
            binding.header.ppOpenMeet.rvIntrested.adapter = OpenMeetJoinRqPeopleStackAdapter(requireActivity(), it) {
                if(value.user_id.equals(MyApplication.SID)) {
                    navigate(RequestToJoinOpenFragment.getInstance(it.meetup_id), anim = true)
                } else {
                    navigate(OpenMeetIntrestList.getInstance(it.meetup_id), anim = true)
                }
            }
            if(it.description?.isEmpty() == true) {
                binding.header.ppOpenMeet.desc.text = "No description added"
            } else {
                binding.header.ppOpenMeet.desc.text = it.description
            }
            Log.i(TAG, " dayPending:: ${Utils.getDayDiff(it.date?.toDate(), Date())}")
            setMeetMultipurposeButton(value, it)

        }
    }


    fun setMeetMultipurposeButton(value: SinglePostResponse, openMeetUp: OpenMeetUp) {
        if((openMeetUp.max_join_time?.value?.toDate()?.time?.compareTo(Date().time) == -1) || (Utils.getDayDiff(openMeetUp.date?.toDate(), Date())
                    ?.compareTo(0) == -1)) {
            binding.header.ppOpenMeet.join.setRoundedColorBackground(requireActivity(), R.color.gray1)
            binding.header.ppOpenMeet.join.text = " closed "
            binding.header.ppOpenMeet.join.onClick({})
            return
        }
        if(value.user_meta.sid.equals(MyApplication.SID)) {
            Log.d(TAG, "populateMeetPost: 1")
            if(openMeetUp.voting_closed == false) {
                Log.d(TAG, "populateMeetPost: 3")
                binding.header.ppOpenMeet.join.setRoundedColorBackground(requireActivity(), R.color.primaryDark)
                binding.header.ppOpenMeet.join.text = "Invite Friend"
                binding.header.ppOpenMeet.join.onClick({
                    type = true
                    meetUpViewModel.getMeetUpDetail(openMeetUp.meetup_id)
                })
            } else {
                Log.d(TAG, "populateMeetPost: 4")
                binding.header.ppOpenMeet.join.setRoundedColorBackground(requireActivity(), R.color.gray1)
//                binding.header.ppOpenMeet.join.text = "You closed this meetup"
                binding.header.ppOpenMeet.join.text = "You closed this meetup"
                binding.header.ppOpenMeet.join.onClick({})
            }
        } else {
            Log.d(TAG, "populateMeetPost: 5")
            if(openMeetUp.join_requested_by_user == true) {
                Log.d(TAG, "populateMeetPost: 7")
                binding.header.ppOpenMeet.join.setRoundedColorBackground(requireActivity(), R.color.gray1)
                binding.header.ppOpenMeet.join.setOnClickListener(null)
                binding.header.ppOpenMeet.join.text = "Requested"
            } else {
                Log.d(TAG, "populateMeetPost: 8")
                binding.header.ppOpenMeet.join.setRoundedColorBackground(requireActivity(), R.color.primaryDark)
                binding.header.ppOpenMeet.join.onClick({

                    if((openMeetUp.max_join_time?.value?.toDate()?.time?.compareTo(Date().time) == -1)) {
                        showToast("Meetup join time is passed!")
                        binding.header.ppOpenMeet.join.setRoundedColorBackground(requireActivity(), R.color.gray1)
                        binding.header.ppOpenMeet.join.text = " closed "
                    } else {
                        val badge: Badge = Utils.getBadge(openMeetUp.min_badge)
                        val currentBadge: Badge = Utils.getBadge(currentUser?.social?.getbadge())
                        if(currentBadge.level >= badge.level) {
                            meetUpViewModel.joinOpenMeet(openMeetUp.meetup_id)
                        } else {
                            showToast("${badge.name} Status and above are eligible for join this meetup")
                        }
                    }
                })
                binding.header.ppOpenMeet.join.text = "Join"
            }
        }
    }

    fun openOption(value: SinglePostResponse, openMeetUp: OpenMeetUp) {
        if(value.user_meta.sid.equals(MyApplication.SID)) {
            var invisibleOption = arrayListOf<Int>()
            if(Utils.getDayDiff(openMeetUp.date?.toDate(), Date())
                        ?.compareTo(0) == -1 || openMeetUp.voting_closed == true) {
                invisibleOption.add(0)
            }
            if(value.comments_enabled) {
                invisibleOption.add(2)
            } else {
                invisibleOption.add(1)
            }
            if(openMeetUp.status?.equals(MeetStatus.CANCELLED) == true) {
                invisibleOption.add(3)
            }
            meetOptions?.hideOption(invisibleOption.toTypedArray())
            showOption(meetOptions)
        }
    }
    override fun hideNavBar(): Boolean {
        return true
    }
    fun showOption(sheet: BottomSheetDialogFragment?) {
        if(sheet == null || sheet.isAdded) {
            return
        }
        sheet.show(childFragmentManager, sheet.tag)
    }

    /*fun showMyPostOptionFragment() {
        var fragment = myPostOptions as BottomSheetDialogFragment?
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(childFragmentManager, fragment.tag)
    }

    fun showOtherPostOptionFragment() {
        var fragment = otherPostOptions as BottomSheetDialogFragment?
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(childFragmentManager, fragment.tag)
    }*/

    private fun getPostCommentAdapter(): PostCommentAdapter {
        return PostCommentAdapter(requireActivity(), this, ArrayList<Comment>(), postViewModel, {
            postViewModel.toggleCommentLike(it)
        }, {
            openProfile(it, Constant.Source.POST.sorce.plus(POST_ID))
        }, { it, position ->
            // showIndicator(true,it)
            //COMMENT_ID = it?._id
            //COMMENT_POSITION = position
        }, { comment, position, type ->
            MyApplication.smallVibrate()
            if(type.equals(TYPE_COMMENT)) {
                postViewModel.deleteComment(comment?._id)
            } else {
                postViewModel.deleteCommentReply(comment?._id)
            }
        })
    }

    fun getSinglePostAdapter(): SinglePostCommnetAdapter {
        var adapter = SinglePostCommnetAdapter(requireActivity(), this, postViewModel, false, {
            postViewModel.toggleCommentLike(it)
        }, {
            openProfile(it, Constant.Source.POST.sorce.plus(POST_ID))
        }, { it, position ->
            showIndicator(true, it)
            COMMENT_ID = it?._id
            COMMENT_POSITION = position
        }, { comment, position, type ->
            MyApplication.smallVibrate()
            if(type.equals(TYPE_COMMENT)) {
                postViewModel.deleteComment(comment?._id)
            } else {
                postViewModel.deleteCommentReply(comment?._id)
            }
        }, { it, postition ->//View Reply
            COMMENT_ID = it?._id
            COMMENT_POSITION = position
            showIndicator(false, it)
            showReplied(it)
            //singlePostCommnetAdapter.refresh()
        })


        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.filter {
                (it.refresh is LoadState.NotLoading || it.refresh is LoadState.Error)
            }.collect {
                Log.i(TAG, "getSinglePostAdapterloadState::: $it")
                if(it.append is LoadState.NotLoading && it.append.endOfPaginationReached && adapter.itemCount < 1) {
                    Log.d(TAG, "setUp: NotificationPagingAdapter 2 :: ${adapter.itemCount}")
                    binding.noComment.visibility = View.VISIBLE
                    return@collect
                } else if(it.append is LoadState.NotLoading && adapter.itemCount >= 1) {
                    Log.d(TAG, "setUp: NotificationPagingAdapter 3 :: ${adapter.itemCount}")
                    binding.noComment.visibility = View.GONE
                    return@collect
                }
                if(it.refresh is LoadState.Error) {
                    Log.i(TAG, "getSinglePostAdapter: commentDeleted:::")
                    arguments?.getString("comment_id")?.let {
                        showToast("Comment deleted by user...")
                        postViewModel.fetchCommnets(POST_ID, MODE, "post")
                    }
                    return@collect
                }
            }
        }
        return adapter
    }


    private fun showIndicator(b: Boolean, comment: SinglePostCommentsItem?) {
        if(b) {
            childFragmentManager.popBackStack()
            binding.indicator.visibility = View.VISIBLE
            //upperline.visibility = View.VISIBLE
            isReplyingComment = true
            //replyingUser.text = comment?.user?.first_name
            binding.replyinguser.text = comment?.user_meta?.username
            binding.replyingComment.text = comment?.body
            //binding.etComment.hint = "replying to ".plus(comment?.user?.first_name)
            binding.etComment.hint = "replying to ".plus(comment?.user_meta?.username)
        } else {
            binding.indicator.visibility = View.GONE
            binding.etComment.hint = "Add a comment"
            //upperline.visibility = View.GONE
            isReplyingComment = false
        }

    }

    private fun postComment() {
        if(binding.etComment.text.toString().trim().isNotEmpty()) {
//            showToast("Posting")
            if(isReplyingComment) {
                Log.i(TAG, " COMMENT_POSITION:: 1 $COMMENT_POSITION ")
                postViewModel.replyComment(COMMENT_ID, binding.etComment.text.toString()
                        .trim(), COMMENT_POSITION)
            } else {
                if(childFragmentManager.backStackEntryCount == 0) {
                    postViewModel.publishComment(POST_ID, binding.etComment.text.toString()
                            .trim(), 0)
                } else {
                    postViewModel.replyComment(COMMENT_ID, binding.etComment.text.toString()
                            .trim(), COMMENT_POSITION)
                }
            }
        }
        showIndicator(false, null)
    }

    private fun likeDislikePost(post: SinglePostResponse?) {
        if(!isPostLiked) {
            isPostLiked = true
            if(isUserLiked == true) {
                binding.header.tvLike.text = (post?.likes_count).toString().plus(" likes")
            } else {
                binding.header.tvLike.text = (post?.likes_count?.plus(1)).toString().plus(" likes")

            }
            binding.header.ivHeart.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_filled))
            binding.header.animHeart.apply {
                this.alpha = 1.0f
                if(this.drawable is AnimatedVectorDrawableCompat) {
                    var avd = this.drawable as AnimatedVectorDrawableCompat
                    avd.start()
                } else if(this.drawable is AnimatedVectorDrawable) {
                    var avd = this.drawable as AnimatedVectorDrawable
                    avd.start()
                }
            }
        } else {
            binding.header.ivHeart.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_small_heart))

            if(isUserLiked == true) {
                binding.header.tvLike.text = (post?.likes_count!! - 1).toString().plus(" likes")
            } else {
                binding.header.tvLike.text = (post?.likes_count).toString().plus(" likes")
            }
            isPostLiked = false
        }
        postViewModel.toggleLike(post?._id)
    }


    override fun bottomSheetClickedOption(buttonClicked: String) {
        when(buttonClicked) {
            BottomSheetOptions.BUTTON1 -> {
                //edit Comment
            }

            BottomSheetOptions.BUTTON2 -> {
                //deleteCOmment

            }
        }
    }


    private fun addObserver() {

        meetUpViewModel.observeMarkAttandance().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    if(it.value?.get("marked")?.asBoolean == true) {
                        iAmHereAlert?.changeStatus(1)
                        binding.header.ppOpenMeet.join.isEnabled = false
                        binding.header.ppOpenMeet.join.setRoundedColorBackground(requireActivity(), R.color.gray1)
                        binding.header.ppOpenMeet.join.text = "You Are Here"
                    } else if(it.value?.get("marked")?.asBoolean == false) {
                        if(it.value.get("distance_to_place") != null) {
                            iAmHereAlert?.changeStatus(2, it.value.get("distance_to_place").asInt)
                        } else {
                            iAmHereAlert?.changeStatus(2)
                        }
                    } else {
                        iAmHereAlert?.changeStatus(3)

                    }
                }

                is ResultHandler.Failure -> {
                    iAmHereAlert?.hideDialog()
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })
        postViewModel.observeSinglePost().observe(viewLifecycleOwner, Observer { result ->
            when(result) {
                is ResultHandler.Success -> {
                    populateView(result.value)
                    POST_ID = result.value?._id
                    MODE = null
                }

                is ResultHandler.Failure -> {
                    Log.i(TAG, " APp fatla:: ")
                    if(result.throwable is ApiException && result.throwable.errorResponse?.errorCode == 404) {
                        Navigation.removeNAddFragment(requireActivity(), NoContentFragment(), "TAG_NO_CONTENT", R.id.homeFram, true, false)
                    } else {
                        Navigation.removeNAddFragment(requireActivity(), NoContentFragment(), "TAG_NO_CONTENT", R.id.homeFram, true, false)
                    }
                }
            }
        })

        postViewModel.observePostCommentsDataSource().observe(viewLifecycleOwner, {
            singlePostCommnetAdapter.submitData(lifecycle, it)
        })

        postViewModel.observerLikeDislike().observe(viewLifecycleOwner, {
            singlePostCommnetAdapter.refresh()
        })

        postViewModel.observePublishComment().observe(viewLifecycleOwner, Observer {
            binding.etComment.setText("")
            singlePostCommnetAdapter.refresh()
            childFragmentManager.setFragmentResult(SinglePostReplyFragment.TAG, bundleOf("refresh" to true))
            binding.appbar.setExpanded(false, true)
            Log.i(TAG, "  COMMENT_POSITION::  $it")
            binding.rvComments.smoothScrollToPosition(it)
//            ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 0) as TimeLineFragment).timeLineAdapter.refresh()
        })

        postViewModel.observePublishReplyComment().observe(viewLifecycleOwner, Observer {
            binding.etComment.setText("")
            singlePostCommnetAdapter.refresh()
            childFragmentManager.setFragmentResult(SinglePostReplyFragment.TAG, bundleOf("refresh" to true))
            binding.appbar.setExpanded(false, true)
            Log.i(TAG, "  COMMENT_POSITION::  $it")
            //binding.rvComments.smoothScrollToPosition(it)
//            ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager, 0) as TimeLineFragment).timeLineAdapter.refresh()
        })



        postViewModel.observeException().observe(viewLifecycleOwner, Observer {
            Utils.handleException(activity, it)
        })

        postViewModel.observeOnCommentDelete().observe(viewLifecycleOwner, Observer {
            //postViewModel.fetchSinglePost(POST_ID,MODE)
            singlePostCommnetAdapter.refresh()
            Toast.makeText(requireContext(), "Comment Deleted", Toast.LENGTH_SHORT).show()
//            ((activity as MainActivity?)?.viewPageAdapter?.instantiateItem((activity as MainActivity).viewPager,0) as TimeLineFragment).timeLineAdapter.refresh()
        })

        postViewModel.onCommentStatusUpdate().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    if(it.value == true) {
                        MyApplication.showToast(requireActivity(), "Comment enabled !!!")
                    } else {
                        MyApplication.showToast(requireActivity(), "Comment disabled !!!")
                    }
                    postViewModel.fetchSinglePost(POST_ID, MODE)
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })

        postViewModel.observePostDelete().observe(viewLifecycleOwner, Observer {
            var profile = activity?.supportFragmentManager?.findFragmentByTag(Constant.TAG_PROFILE_FRAGMENT) as ProfileFragment?
            profile?.postAdapter?.refresh()
//            (activity as MainActivity?)?.apply {
//                (this.viewPageAdapter.instantiateItem(this.viewPager,0) as TimeLineFragment).refreshTimeLine()
//            }
            popBackStack()
        })

        postViewModel.observerReportContent().observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    Toast.makeText(requireContext(), "Thank you for reporting, ${it.value?.get("content_type")?.asString} is under review", Toast.LENGTH_SHORT)
                            .show()
                }

                is ResultHandler.Failure -> {
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()

                }
            }
        })
        meetUpViewModel.observeJoinMeetUp().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "Meetup joined...")
                    postViewModel.fetchSinglePost(POST_ID, null)
                }

                is ResultHandler.Failure -> {
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
        meetUpViewModel.observeMeetUpDetail().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    if(type) {
                        val fragment = AddFriendToMeetUp.getInstance(it.value, TAG)
                        Navigation.addFragment(requireActivity(), fragment, AddFriendToMeetUp::class.simpleName!!, R.id.homeFram, true, true)
                        type = false
                    } else {
                        setParticipant(it.value)
                    }
                }

                is ResultHandler.Failure -> {
                    Log.e(TAG, " MeetDetailFail:: ${it.throwable?.printStackTrace()}")
                }
            }
        })
        meetUpViewModel.observeCloseOpenMeet().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    postViewModel.fetchSinglePost(POST_ID, MODE)
                    MyApplication.showToast(requireActivity(), "Open meetup closed!!!")
                }

                is ResultHandler.Failure -> {
                    Log.e(TAG, " Close open Meet Fail:: ${it.throwable?.printStackTrace()}")
                }
            }
        })

        meetUpViewModel.observeCancelMeetUp().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    MyApplication.showToast(requireActivity(), "MeetUp Cancelled!!")
                    activity?.onBackPressed()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong!!!")
                }
            }
        })
    }

    private fun setParticipant(value: GetMeetUpResponseItem?) {
        Log.d(TAG, "setParticipant: ")
        val get = value?.participants?.firstOrNull { it.sid == MyApplication.SID }
        val youHere = value?.attendees?.firstOrNull { it.sid == MyApplication.SID }
        if(get != null) {
            value.date?.toDate()?.time?.let {
                if(DateUtils.isToday(it)) {
                    binding.header.ppOpenMeet.join.setRoundedColorBackground(requireActivity(), R.color.primaryDark)
                    if(youHere == null) {
                        binding.header.ppOpenMeet.join.onClick({
                            if(Utils.checkPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                                (activity as MainActivity?)?.enableLocationStuff(654, 655) {
                                    MyApplication.putTrackMP(Constant.AcOMDetailIamHere, JSONObject(mapOf("meetId" to value?._id)))
                                    iamHere(value?._id)
                                }
                            } else {
//                                iamHereID = value?._id
                                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 655)
                            }
                        })
                        binding.header.ppOpenMeet.join.text = "I am Here"
                    } else {
                        binding.header.ppOpenMeet.join.setRoundedColorBackground(requireActivity(), R.color.gray1)
                        binding.header.ppOpenMeet.join.onClick({})
                        binding.header.ppOpenMeet.join.text = "You are Here"
                    }
                }
            }
        }
    }

    fun showCustomPlaceAlert(latLng: LatLng) {
        var fragment = customPlaceAlert
        customPlaceAlert?.setLatLng(latLng)
        if(fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(childFragmentManager, fragment.tag)
    }

    private fun iamHere(_id: String) {
        Log.i("TAG", " I am Here:: ")
        MyApplication.putTrackMP(Constant.AcOMDetailLocation, JSONObject(mapOf("postId" to _id)))
        iAmHereAlert = IAmHereAlert(requireActivity())
        iAmHereAlert?.showDialog()
        locationFinder = LocationFinder(requireActivity(), object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    it.locations.map {
                        it?.let {
                            Log.i("TAG", " Lat:: ${it.latitude} -- ${it.longitude}")
                            meetUpViewModel.markAttendance(_id, it)
                            iAmHereAlert?.changeStatus(0)
                            locationFinder?.mFusedLocationClient?.removeLocationUpdates(this)
                        }
                    }
                }
            }
        })
    }


    fun showReplied(singlePostCommentsItem: SinglePostCommentsItem?) {
        MyApplication.smallVibrate()
        val ft: FragmentTransaction = childFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_bottom, R.anim.slide_in_up, R.anim.slide_out_bottom)
        if(!replyFragment.isAdded) {
            ft.add(R.id.childFram, replyFragment, tag)
            ft.addToBackStack(null)
            ft.commit()
        }
        Log.i(TAG, " checkingdatasending!!! ")
        setReplyFragmentData(singlePostCommentsItem)

        //fragDetail(activity)
    }

    fun setReplyFragmentData(singlePostCommentsItem: SinglePostCommentsItem?) {
        childFragmentManager.setFragmentResult(SinglePostReplyFragment.TAG, Bundle().apply {
            //putParcelable("parentComment",singlePostCommentsItem)

            singlePostCommentsItem?.let {
                COMMENT_ID = it._id
                Log.i(TAG, "setReplyFragmentData: 0")
                putParcelable("parentComment", it)
                putString("comment_id", it._id)
                putString("post_id", it.post_id)
                putString("reply_comment_id", arguments?.getString("reply_comment_id"))
            } ?: run {
                COMMENT_ID = arguments?.getString("comment_id")
                Log.i(TAG, "setReplyFragmentData: 1")
                Log.i(TAG, " setReplyFragmentData:: ${arguments?.getString("reply_comment_id")}")
                putString("comment_id", arguments?.getString("comment_id"))
                putString("post_id", POST_ID)
                putString("reply_comment_id", arguments?.getString("reply_comment_id"))
            }

        })
    }

    override fun onBackPressed(): Boolean {
        if(childFragmentManager.backStackEntryCount == 0) return true
        else {
            childFragmentManager.popBackStack()
            return false
        }
    }

    override fun onResume() {
        binding.shimmer.startShimmer()
        super.onResume()
    }

    override fun onPause() {
        binding.shimmer.stopShimmer()
        super.onPause()
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, " onRequestPermissionsResult::: $requestCode  ")
        when(requestCode) {
            655 -> {
                var location = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if(grantResults.isNotEmpty() && location) {
                    (activity as MainActivity?)?.enableLocationStuff(654, 655) {
//                        iamHereID?.let {
//                            iamHere(it)
//                        }
                    }
                } else if(Build.VERSION.SDK_INT >= 23 && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    rationale()
                } else {
                }
            }


        }
    }
}