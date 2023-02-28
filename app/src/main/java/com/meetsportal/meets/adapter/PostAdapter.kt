package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.ddd.androidutils.DoubleClick
import com.ddd.androidutils.DoubleClickListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.post.FetchPostResponseItem
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.overridelayout.AutoResizeTextView
import com.meetsportal.meets.ui.dialog.CustomPlaceDetailAlert
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.ui.fragments.socialfragment.*
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.FilterBottomSheet
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.AcDeletePost
import com.meetsportal.meets.utils.Constant.GradientTypeArray
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import org.json.JSONObject
import java.util.*

class PostAdapter(
    var myContext: Activity, var fragment: ProfileFragment,
    var profile: ProfileGetResponse?,
    var likeListener: (String?) -> Unit,
    var deletePost: (String?) -> Unit
) : PagingDataAdapter<FetchPostResponseItem, RecyclerView.ViewHolder>(COMPARATOR) {

    val TAG = PostAdapter::class.java.simpleName

    /* val GRID = "grid"
     val LINEAR = "linear"*/
    var viewtype = LINEAR
    var customPlaceAlert : CustomPlaceDetailAlert? = null

    var myPostArray= arrayListOf("Edit", "Enable Comment", "Delete")
    var myCheckInArray= arrayListOf("Enable Comment", "Delete")
    var myPostSheet: FilterBottomSheet = FilterBottomSheet(myContext,myPostArray){
        when(it) {
            0 -> {
                MyApplication.putTrackMP(Constant.AcEditImagePost, JSONObject(mapOf("postId" to selectedPost?._id)))
                var baseFragment: BaseFragment = EditPostFragment()
                Navigation.setFragmentData(baseFragment, "postId", selectedPost?._id)
                Navigation.addFragment(myContext, baseFragment, Constant.TAG_EDIT_POST_FRAGMENT, R.id.homeFram, true, false)
            }
            1->{
                val enabledDisabled = selectedPost?.comments_enabled != true
                fragment.changeCmtStatus(selectedPost?._id,enabledDisabled)
            }
            2->{
                MyApplication.putTrackMP(AcDeletePost, JSONObject(mapOf("postId" to selectedPost?._id)))
                deletePost(selectedPost?._id)
            }
        }
    }
    val myCheckInSheet: FilterBottomSheet = FilterBottomSheet(myContext,myCheckInArray){
        when(it) {
            0->{
                Log.d(TAG, "showMine:comment1 ${selectedPost?.comments_enabled}")
                val enabledDisabled = selectedPost?.comments_enabled != true
                Log.d(TAG, "showMine:comment2 $enabledDisabled")
                fragment.changeCmtStatus(selectedPost?._id,enabledDisabled)
            }
            1->{
                deletePost(selectedPost?._id)
            }
        }
    }
    var selectedPost: FetchPostResponseItem? = null
    var myPostOptions: BottomSheetOptions? = null
    var viewPool = RecyclerView.RecycledViewPool()

    init {
        customPlaceAlert = CustomPlaceDetailAlert(myContext)
        myPostOptions = BottomSheetOptions.getInstance("Edit", "Delete", null, null, null)
        myPostOptions?.setBottomSheetLitener(object : BottomSheetOptions.BottomSheetListener {
            override fun bottomSheetClickedOption(buttonClicked: String) {
                when (buttonClicked) {
                    BottomSheetOptions.BUTTON1 -> {
                        MyApplication.putTrackMP(Constant.AcEditImagePost, JSONObject(mapOf("postId" to selectedPost?._id)))
                        var baseFragment: BaseFragment = EditPostFragment()
                        Navigation.setFragmentData(baseFragment, "postId", selectedPost?._id)
                        Navigation.addFragment(
                            myContext, baseFragment,
                            Constant.TAG_EDIT_POST_FRAGMENT, R.id.homeFram, true, false
                        )
                    }
                    BottomSheetOptions.BUTTON2 -> {
                        deletePost(selectedPost?._id)
                    }
                    BottomSheetOptions.CANCEL -> {

                    }
                }
            }
        })
    }

    fun setViewtypes(viewType: String) {
        Log.i(TAG, "  checking:: $viewType")
        this.viewtype = viewType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val post = getItem(position)

        fragment.noPostScreen.visibility = View.GONE
        when (holder) {
            is RLinearviewHolder -> {
                bindLinearRecyclerData(holder, post)
                addRecyclerListener(holder, post)
                addCommonLitener(holder, post)
            }
            is LinearTextPostHolder -> {
                val post = getItem(position)
                bindTextPostData(holder, post)
                addTextPostListener(holder, post)
                addCommonLitener(holder, post)
            }
            is OpenMeetHolder ->{
                val post = getItem(position)
                bindOpenMeet(holder,post)
            }
            is CviewHolder->{
                val post = getItem(position)
                bindCheckinData(holder,post)
            }

            is RGridviewHolder -> {
                bindGridRecyclerData(holder, post, position)
            }
            is GridTextHolder -> {
                bindGridTextPostData(holder, post, position)
            }
            is GridMeetHolder ->{
                bindGridMeetPostData(holder, post, position)
            }
        }
        Log.i(TAG, " crashing:: $position")

    }

    override fun getItemViewType(position: Int): Int {
        var data = getItem(position)
        if (viewtype.equals(LINEAR)) {
            return when (data?.type) {
                Constant.Post.DEFAULT.type -> LINEAR_IMAGE
                Constant.Post.CHECK_IN.type -> LINEAR_CHECKIN
                Constant.Post.TEXT_POST.type -> LINEAR_TEXT
                Constant.Post.OPEN_MEET.type -> LINEAR_MEET
                //Constant.Post.OPEN_MEET.type-> LINEAR_MEET
                else -> LINEAR_IMAGE
            }
        } else if (viewtype.equals(GRID)) {
            return when (data?.type) {
                Constant.Post.DEFAULT.type -> GRID_IMAGE
                Constant.Post.CHECK_IN.type -> GRID_IMAGE
                Constant.Post.TEXT_POST.type -> GRID_TEXT
                Constant.Post.OPEN_MEET.type -> GRID_MEET
                //Constant.Post.OPEN_MEET.type-> GRID_IMAGE
                else -> GRID_IMAGE
            }
        } else
            return GRID_IMAGE

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            LINEAR_IMAGE -> RLinearviewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.card_news_feed, parent, false)
            )
            LINEAR_TEXT -> LinearTextPostHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.card_text_post, parent, false)
            )
            LINEAR_MEET -> OpenMeetHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.card_open_meet_post,
                    parent,
                    false
                ),myContext,viewPool
            )
            LINEAR_CHECKIN -> CviewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.card_check_in,
                    parent,
                    false
                )
            )

            GRID_IMAGE -> RGridviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_grid_news_feed, parent, false)
            )
            GRID_TEXT -> GridTextHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_grid_text_post, parent, false)
            )
            GRID_MEET -> GridMeetHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_grid_meet_post, parent, false),myContext
            )

            else -> RGridviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_grid_news_feed, parent, false)
            )
        }

    }

    interface CommonOption {
        var like: TextView
        var comment: TextView
        var likeCount: TextView
        var animLike: ImageView
        var isPostLiked: Boolean
        var commentCount: TextView
        var tvInsight: TextView
        var tvBoostTime: TextView

    }

    class RLinearviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), CommonOption {

        var dp = itemView.findViewById<ImageView>(R.id.civ_dp)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)

        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var lastseenandLoc = itemView.findViewById<TextView>(R.id.tv_loc_last_seen)
        var option = itemView.findViewById<TextView>(R.id.tv_options)

        //var pager = itemView.findViewById<ViewPager>(R.id.iv_feed_pic)
        override var likeCount = itemView.findViewById<TextView>(R.id.tv_likes)
        override var isPostLiked = false
        override var commentCount = itemView.findViewById<TextView>(R.id.tv_comments)
        override var tvInsight: TextView=itemView.findViewById<TextView>(R.id.tvInsight)
        var shareCount = itemView.findViewById<TextView>(R.id.tv_shares)
        var memeCount = itemView.findViewById<TextView>(R.id.tv_memes)
        var imageCout = itemView.findViewById<TextView>(R.id.tv_imageCount)
        var multiImageIcon = itemView.findViewById<ImageView>(R.id.iv_imageCount)

        override var animLike = itemView.findViewById<ImageView>(R.id.anim_heart)

        var caption = itemView.findViewById<TextView>(R.id.tv_caption)
        override var comment = itemView.findViewById<TextView>(R.id.tv_comment).also {
            it.text = "Comment"
        }
        override var tvBoostTime: TextView=itemView.findViewById<TextView>(R.id.tvBoostTime).apply{
            visibility=View.GONE
        }
        override var like = itemView.findViewById<TextView>(R.id.tv_like).also {
            it.text = "Like"
        }
        var share = itemView.findViewById<TextView>(R.id.tv_share).also {
            it.text = "Share It"
        }
        var meme = itemView.findViewById<TextView>(R.id.tv_meme).also {
            it.text = "Meme It"
        }
        var postImage = itemView.findViewById<ImageView>(R.id.iv_post_image)


    }

    class LinearTextPostHolder(itemView: View) : RecyclerView.ViewHolder(itemView), CommonOption {
        var dp = itemView.findViewById<ImageView>(R.id.civ_dp)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)

        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var lastseenandLoc = itemView.findViewById<TextView>(R.id.tv_loc_last_seen)
        var option = itemView.findViewById<TextView>(R.id.tv_options)
        var isVerified = itemView.findViewById<ImageView>(R.id.is_verified)

        //var pager = itemView.findViewById<ViewPager>(R.id.iv_feed_pic)
        override var likeCount = itemView.findViewById<TextView>(R.id.tv_likes)
        override var isPostLiked = false
        override var commentCount = itemView.findViewById<TextView>(R.id.tv_comments)

        override var tvInsight: TextView=itemView.findViewById<TextView>(R.id.tvInsight)
        var shareCount = itemView.findViewById<TextView>(R.id.tv_shares)
        override var animLike = itemView.findViewById<ImageView>(R.id.anim_heart)

        var caption = itemView.findViewById<TextView>(R.id.tv_caption)
        override var comment = itemView.findViewById<TextView>(R.id.tv_comment).also {
            it.text = "Comment"
        }
        override var tvBoostTime: TextView=itemView.findViewById<TextView>(R.id.tvBoostTime).apply{
            visibility=View.GONE
        }
        override var like = itemView.findViewById<TextView>(R.id.tv_like).also {
            it.text = "Like"
        }
        var share = itemView.findViewById<TextView>(R.id.tv_share).also {
            it.text = "Share It"
        }
        var meme = itemView.findViewById<TextView>(R.id.tv_meme).also {
            it.text = "Meme It"
        }

    }

    class OpenMeetHolder(
        itemView: View,
        var myContext: Activity,
        viewPool: RecyclerView.RecycledViewPool
    ): RecyclerView.ViewHolder(itemView), CommonOption {
        var dp = itemView.findViewById<ImageView>(R.id.civ_dp)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var lastseenandLoc = itemView.findViewById<TextView>(R.id.tv_loc_last_seen)
        var option = itemView.findViewById<TextView>(R.id.tv_options)
        var isVerified = itemView.findViewById<ImageView>(R.id.is_verified)
        var view = itemView.findViewById<TextView>(R.id.view).apply {
            setRoundedColorBackground(myContext,R.color.transparent,enbleDash = true,
                strokeColor = R.color.primaryDark,Dashgap = 0f,stripSize = 10f,strokeHeight = 1f)
        }
        override var likeCount = itemView.findViewById<TextView>(R.id.tv_likes)
        override var commentCount = itemView.findViewById<TextView>(R.id.tv_comments)

        override var tvInsight: TextView=itemView.findViewById<TextView>(R.id.tvInsight)
        override var animLike = itemView.findViewById<ImageView>(R.id.anim_heart)
        override var isPostLiked = false
        //var caption = itemView.findViewById<TextView>(R.id.tv_caption).apply{ text = ""}
        override var comment = itemView.findViewById<TextView>(R.id.tv_comment).also {
            it.text = "Comment"
        }
        override var tvBoostTime: TextView=itemView.findViewById<TextView>(R.id.tvBoostTime).apply{
            visibility=View.GONE
        }
        override var like = itemView.findViewById<TextView>(R.id.tv_like).also {
            it.text = "Like"
        }
        var meetName = itemView.findViewById<TextView>(R.id.meetName)
        var address = itemView.findViewById<TextView>(R.id.address)
        var desc = itemView.findViewById<TextView>(R.id.desc)
        var monthOfYear = itemView.findViewById<TextView>(R.id.monthOfYear).apply {
            setGradient(myContext, GradientDrawable.Orientation.TL_BR, intArrayOf(
                ContextCompat.getColor(myContext,R.color.primaryDark),
                ContextCompat.getColor(myContext,R.color.gred_red),
                ContextCompat.getColor(myContext,R.color.gred_red)
            ))
        }
        var dayOfMonth = itemView.findViewById<TextView>(R.id.dayOfMonth)
        var dayOfWeak = itemView.findViewById<TextView>(R.id.dayOfWeak)
        var timeOfDay = itemView.findViewById<TextView>(R.id.timeOfDay)
        var countInterested = itemView.findViewById<TextView>(R.id.countInterested)
        var join = itemView.findViewById<TextView>(R.id.join).apply {
            setRoundedColorBackground(myContext,R.color.primaryDark)
        }

        var parentCard = itemView.findViewById<LinearLayout>(R.id.parentCard).apply{
            setRoundedColorBackground(myContext,enbleDash =true,stripSize = 20f,Dashgap = 0f,strokeColor = R.color.gray1,corner = 20f,strokeHeight = 2f,)
        }

        var rlIntract = itemView.findViewById<LinearLayout>(R.id.rl_intract).apply{
            setRoundedColorBackground(myContext,enbleDash =true,stripSize = 20f,Dashgap = 0f,strokeColor = R.color.gray1,corner = 20f,strokeHeight = 2f,)
        }
        var rvIntrested = itemView.findViewById<RecyclerView>(R.id.rvIntrested).apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL,false)
            setRecycledViewPool(viewPool)
        }

        var llRequest = itemView.findViewById<LinearLayout>(R.id.llRequest)
        var llNoRequest = itemView.findViewById<LinearLayout>(R.id.llNoRequest)
        var tvFirst = itemView.findViewById<TextView>(R.id.tvFirst)

    }

    class CviewHolder(itemView: View): RecyclerView.ViewHolder(itemView),CommonOption {
        var dp = itemView.findViewById<ImageView>(R.id.civ_dp)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var lastseenandLoc = itemView.findViewById<TextView>(R.id.tv_loc_last_seen)
        var option = itemView.findViewById<TextView>(R.id.tv_options)
        var isVerified = itemView.findViewById<ImageView>(R.id.is_verified)
        //var pager = itemView.findViewById<ViewPager>(R.id.iv_feed_pic)
        override var likeCount = itemView.findViewById<TextView>(R.id.tv_likes)
        override var isPostLiked = false
        override var commentCount = itemView.findViewById<TextView>(R.id.tv_comments)

        override var tvInsight: TextView=itemView.findViewById<TextView>(R.id.tvInsight)
        var shareCount = itemView.findViewById<TextView>(R.id.tv_shares)
        var imageCout = itemView.findViewById<TextView>(R.id.tv_imageCount)
        override var animLike = itemView.findViewById<ImageView>(R.id.anim_heart)

        var caption = itemView.findViewById<TextView>(R.id.tv_caption)
        override var comment = itemView.findViewById<TextView>(R.id.tv_comment).also {
            it.text = "Comment"
        }
        override var tvBoostTime: TextView=itemView.findViewById<TextView>(R.id.tvBoostTime).apply{
            visibility=View.GONE
        }
        override var like = itemView.findViewById<TextView>(R.id.tv_like).also {
            it.text = "Like"
        }
        var share = itemView.findViewById<TextView>(R.id.tv_share).also {
            it.text = "Share It"
        }
        var meme = itemView.findViewById<TextView>(R.id.tv_meme).also {
            it.text = "Meme It"
        }
        var postImage = itemView.findViewById<ImageView>(R.id.iv_post_image)
        var placeName = itemView.findViewById<TextView>(R.id.place_name)
        var timing = itemView.findViewById<TextView>(R.id.timing)
//        var checkOut = itemView.findViewById<TextView>(R.id.checkOut)
        var ratingTxt = itemView.findViewById<TextView>(R.id.rating_txt)
        var rating = itemView.findViewById<RatingBar>(R.id.rating)
    }

    class RGridviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageCout = itemView.findViewById<TextView>(R.id.tv_imageCount)
        var multiImageIcon = itemView.findViewById<ImageView>(R.id.iv_imageCount)
        var postImage = itemView.findViewById<ImageView>(R.id.iv_post_image)
    }

    class GridTextHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postText = itemView.findViewById<AutoResizeTextView>(R.id.tv_caption)
    }

    class GridMeetHolder(itemView: View,myContext: Activity): RecyclerView.ViewHolder(itemView) {
        val roundCorner = Utils.dpToPx(10f,myContext.resources).toFloat()
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo).apply {
            setBackgroundColor(ContextCompat.getColor(myContext,R.color.white))
        }
        var llBorder = itemView.findViewById<RelativeLayout>(R.id.llBorder).apply {
            setGradient(myContext,GradientDrawable.Orientation.TL_BR, intArrayOf(
                    ContextCompat.getColor(myContext,R.color.bg_gray),
                    ContextCompat.getColor(myContext,R.color.bg_gray),
                    ContextCompat.getColor(myContext,R.color.bg_gray),
            ))
        }
        var rlWhitebg = itemView.findViewById<RelativeLayout>(R.id.rlWhitebg).apply{

            setRoundedColorBackground(myContext,R.color.white,10f,enbleDash=true,strokeColor= R.color.extraLightGray,Dashgap=0f,strokeHeight=1f)
        }
        var dayOfMonth = itemView.findViewById<TextView>(R.id.dayOfMonth).apply {
        }
        var dayOfWeak = itemView.findViewById<TextView>(R.id.dayOfWeek)
        var time = itemView.findViewById<TextView>(R.id.Time)
        var meetName = itemView.findViewById<TextView>(R.id.meetName)
        var tvOpenMeet = itemView.findViewById<TextView>(R.id.tvOpenMeet).apply{
            var shape = Utils.getGradient(GradientDrawable.Orientation.BOTTOM_TOP,intArrayOf(
                Color.parseColor("#FF7272"),
                Color.parseColor("#7A76E6")))
            shape.cornerRadii = floatArrayOf(roundCorner, roundCorner, 2f, 2f, roundCorner, roundCorner, 2f, 2f)
            background = shape
        }


    }


    fun bindLinearRecyclerData(holder: RLinearviewHolder, post: FetchPostResponseItem?) {
//        Utils.stickImage(myContext, holder.dp, post?.user_meta?.profile_image_url, null)
        holder.dp.loadImage(myContext, post?.user_meta?.profile_image_url, R.drawable.ic_default_person)
        Log.d(TAG, "bindLinearRecyclerData:post?.user_meta?.badge #${post?.user_meta?.badge} ")
        post?.user_meta?.badge?.let { badge: String ->
            holder.ivDpBadge.setImageResource(Utils.getBadge(badge).foreground)
        }
        if (post?.media?.size?.compareTo(0) == 1) {
            Log.i(TAG, " mediaSize:: ${post?.media?.size} ")
            Utils.stickImage(myContext, holder.postImage, post.media!!.get(0), null)
            holder.imageCout.text =
                if (post.media!!.size > 6) "6+ Photos" else "${post.media!!.size} Photos"

            if (post.media?.size?.compareTo(1) == 1)
                holder.multiImageIcon.visibility = View.VISIBLE
            else
                holder.multiImageIcon.visibility = View.GONE
        }
        var anim = holder.animLike.drawable
        if (anim is AnimatedVectorDrawableCompat) {

        }
        //holder.name.text = post?.user_meta?.first_name?.plus(post.user_meta!!.last_name)
        holder.name.text = post?.user_meta?.username
        holder.caption.setText(Utils.setMentionInCaption(myContext,post?.body,post?.mentions,post?._id),TextView.BufferType.SPANNABLE)
        holder.caption.setMovementMethod(LinkMovementMethod.getInstance())
        //holder.caption.text = post?.body
        holder.commentCount.text = post?.stats?.comments.toString().plus(" Comments")
        holder.likeCount.text = post?.stats?.likes.toString().plus(" likes")
        post?.createdAt?.toDate()?.compareTo(Date())
        Log.i(TAG, "crashing::  ${post}")
        var diffrence = (Date().time - post?.createdAt?.toDate()?.time!!) / 1000
        Log.i("TAG", " Diffrence ${diffrence}")
        if (diffrence < 60) {
            holder.lastseenandLoc.text = "Just now"
        } else if (diffrence < (60 * 60)) {
            holder.lastseenandLoc.text = " ${diffrence / 60} min ago"
        } else if (diffrence < (60 * 60 * 24)) {
            holder.lastseenandLoc.text = " ${diffrence / 60 / 60} hour ago"
        } else {
            holder.lastseenandLoc.text = "${post?.createdAt?.toDate()!!.formatTo("dd MMM yyyy")}"
        }
        if (post.liked_by_user == true) {
            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_filled, 0, 0, 0)
            holder.isPostLiked = true
        } else {
            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_small_heart, 0, 0, 0)
            holder.isPostLiked = false
        }

    }

    fun bindTextPostData(holder: LinearTextPostHolder, post: FetchPostResponseItem?) {
        var myProfile = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
        if (post?.user_meta?.verified_user == true)
            holder.isVerified.visibility = View.VISIBLE
        else
            holder.isVerified.visibility = View.GONE
        Utils.stickImage(myContext, holder.dp, post?.user_meta?.profile_image_url, null)
        holder.dp.setOnClickListener {
            //openProfile(post, myProfile)
        }
        post?.user_meta?.badge?.let { badge: String ->
            holder.ivDpBadge.setImageResource(Utils.getBadge(badge).foreground)
        }
        holder.name.setOnClickListener {
            //openProfile(post, myProfile)
        }


        var anim = holder.animLike.drawable
        if (anim is AnimatedVectorDrawableCompat) {

        }
        holder.name.text = post?.user_meta?.username

        holder.caption.setText(Utils.setMentionInCaption(myContext,post?.body,post?.mentions,post?._id),TextView.BufferType.SPANNABLE)
        holder.caption.setMovementMethod(LinkMovementMethod.getInstance())
        holder.caption.setFocusable(false);

        //holder.caption.setText(spannable,TextView.BufferType.SPANNABLE)
        //holder.caption.text = post?.body

        //holder.caption.text = "Checked in at ".plus(post?.body_obj?.name?.en)
        holder.commentCount.text = post?.stats?.comments.toString().plus(" Comments")
        holder.likeCount.text = post?.stats?.likes.toString().plus(" likes")
        //post?.createdAt?.toDate()?.compareTo(Date())


        var diffrence = (Date().time - post?.createdAt?.toDate()?.time!!) / 1000
        Log.i("TAG", " Diffrence ${diffrence}")
        holder.lastseenandLoc.text = "".plus(Utils.getCreatedAt(post.createdAt))


        if (post.liked_by_user == true) {
            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_filled, 0, 0, 0)
            holder.isPostLiked = true
        } else {
            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_small_heart, 0, 0, 0)
            holder.isPostLiked = false
        }

    }

    fun bindOpenMeet(holder: OpenMeetHolder, post: FetchPostResponseItem?){
        var myProfile = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
        if(post?.user_meta?.verified_user == true)
            holder.isVerified.visibility = View.VISIBLE
        else
            holder.isVerified.visibility = View.GONE

        addCommonLitener(holder,post)

        Utils.stickImage(myContext, holder.dp, post?.user_meta?.profile_image_url, null)
        holder.name.text = post?.user_meta?.username
        holder.lastseenandLoc.text = Utils.getCreatedAt(post?.createdAt)

        holder.view.onClick({
            MyApplication.putTrackMP(Constant.AcProfileOMDetail, JSONObject(mapOf("postId" to post?._id)))
            openPostDetail(post)
        })
        post?.body_obj?.open_meetup?.let { openMeet ->
            holder.monthOfYear.text = openMeet.date?.toDate()?.formatTo("MMM")
            holder.dayOfMonth.text = openMeet.date?.toDate()?.formatTo("dd")
            holder.dayOfWeak.text = openMeet.date?.toDate()?.formatTo("EEEE")
            holder.timeOfDay.text = openMeet.date?.toDate()?.formatTo("hh:mmaa")
            holder.meetName.text = openMeet.name

            holder.join.setTextColor(ContextCompat.getColor(myContext,R.color.white))
            //holder.join.setRoundedColorBackground(myContext,R.color.transparent)
            if(post.user_meta?.sid.equals(MyApplication.SID)){
                if(openMeet.voting_closed == false){
                    holder.join.setRoundedColorBackground(myContext,R.color.gray1)
                    holder.join.setOnClickListener(null)
                    holder.join.visibility = View.INVISIBLE
                    holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f)

                }else{
                    holder.join.setRoundedColorBackground(myContext,R.color.transparent,)
                    holder.join.setOnClickListener(null)
                    holder.join.visibility = View.VISIBLE
                    holder.join.text = "Closed"
                    holder.join.setTextColor(ContextCompat.getColor(myContext,R.color.gray1))
                    holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f)
                }
            }
            if(openMeet.description?.isEmpty() == true){
                holder.desc.text = "No description added"
            }else{
                holder.desc.text = openMeet.description
            }
            when(openMeet.chosen_place?.type){
                Constant.PlaceType.MEET.label ->{
                    holder.address.text  = openMeet.places?.firstOrNull()?.name?.en
                    holder.address.setOnClickListener {
                        var baseFragment: BaseFragment = RestaurantDetailFragment();
                        Navigation.setFragmentData(baseFragment, "_id", openMeet.places?.firstOrNull()?._id)
                        Navigation.addFragment(
                            myContext, baseFragment,
                            RestaurantDetailFragment.TAG, R.id.homeFram, true, false
                        )
                    }
                }
                Constant.PlaceType.CUSTOM.label -> {
                    holder.address.setOnClickListener(null)
                    holder.address.text = openMeet.custom_places?.firstOrNull()?.name
                    holder.address.setOnClickListener {
                        showCustomPlaceAlert(LatLng(openMeet.custom_places?.firstOrNull()?.getLongitude()?:0.0,openMeet.custom_places?.firstOrNull()?.getLatitude()?:0.0))
                    }
                }
            }
            if(openMeet.join_requests?.requests?.size?.compareTo(0) == 1){
                holder.llNoRequest.visibility = View.GONE
                holder.llRequest.visibility = View.VISIBLE
                if(openMeet.join_requests?.requests?.size == 1)
                    holder.countInterested.text = openMeet.join_requests?.requests?.size.toString().plus(" person\ninterested")
                else if(openMeet.join_requests?.requests?.size > 1)
                    holder.countInterested.text = openMeet.join_requests?.requests?.size.toString().plus(" people\ninterested")
                holder.countInterested.onClick({
                    MyApplication.putTrackMP(Constant.AcProfileOMInterested, JSONObject(mapOf("postId" to post?._id)))
                    var fragment = RequestToJoinOpenFragment.getInstance(openMeet.meetup_id)
                    Navigation.addFragment(myContext,fragment,RequestToJoinOpenFragment.TAG,R.id.homeFram,true,false)
            }) 
                holder.rvIntrested.adapter = OpenMeetJoinRqPeopleStackAdapter(myContext,openMeet){
                    var fragment = RequestToJoinOpenFragment.getInstance(openMeet.meetup_id)
                    Navigation.addFragment(myContext,fragment,RequestToJoinOpenFragment.TAG,R.id.homeFram,true,false)
                }
            }else{
                holder.llNoRequest.visibility = View.VISIBLE
                holder.llRequest.visibility = View.GONE
                holder.tvFirst.text="Requested\n will displayed here"
                holder.rvIntrested.adapter = OpenMeetJoinRqPeopleStackAdapter(myContext,openMeet){
                    var fragment = RequestToJoinOpenFragment.getInstance(openMeet.meetup_id)
                    Navigation.addFragment(myContext,fragment,RequestToJoinOpenFragment.TAG,R.id.homeFram,true,false)
                }
            }
        }

        holder.rlIntract.onClick({
            MyApplication.putTrackMP(Constant.AcCardtoDetailOpMeet, JSONObject(mapOf("postId" to post?.body_obj?.open_meetup?.meetup_id)))
            var baseFragment: BaseFragment = DetailPostFragment()
            Navigation.setFragmentData(
                baseFragment,
                "post_id",
                post?._id
            )
            Navigation.addFragment(
                myContext,
                baseFragment,
                Constant.TAG_DETAIL_POST_FRAGMENT,
                R.id.homeFram,
                true,
                false
            )
        })


        holder.option.setOnClickListener {
            selectedPost = post
            showCheckOption()
        }

        post?.user_meta?.badge?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            holder.ivDpBadge.setImageResource(firstOrNull.foreground)
        }
    }

    fun bindCheckinData(holder: CviewHolder, post: FetchPostResponseItem?) {
        var myProfile = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
        if (post?.user_meta?.verified_user == true)
            holder.isVerified.visibility = View.VISIBLE
        else
            holder.isVerified.visibility = View.GONE

        addCommonLitener(holder, post)
        Utils.stickImage(myContext, holder.dp, post?.user_meta?.profile_image_url, null)
        holder.option.setOnClickListener {
            selectedPost = post
            showCheckOption()
        }

        post?.user_meta?.badge?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            holder.ivDpBadge.setImageResource(firstOrNull.foreground)
        }
        /*holder.name.setOnClickListener {
            openProfile(post, myProfile)
        }*/



        if (post?.media?.size?.compareTo(0) == 1) {
            Log.i(TAG, " sizeOfImage ")
            holder.imageCout.text =
                if (post.media!!.size > 6) "6+ Photos" else "${post.media!!.size} Photos"
        }
        var anim = holder.animLike.drawable
        if (anim is AnimatedVectorDrawableCompat) {

        }
        holder.name.text = post?.user_meta?.username


        //holder.caption.text = "Checked in at ".plus(post?.body_obj?.name?.en)
        //post?.createdAt?.toDate()?.compareTo(Date())


        var diffrence = (Date().time - post?.createdAt?.toDate()?.time!!) / 1000
        Log.i("TAG", " Diffrence ${diffrence}")
        holder.lastseenandLoc.text = Utils.getCreatedAt(post.createdAt)




        post?.body_obj?.check_in?.let { checkIn ->
//            holder.checkOut.setOnClickListener {
//                Log.i(TAG, " clicked:::  ${checkIn.id}")
//                var baseFragment: BaseFragment = RestaurantDetailFragment();
//                Navigation.setFragmentData(baseFragment, "_id", checkIn.id)
//                Navigation.addFragment(
//                    myContext, baseFragment,
//                    RestaurantDetailFragment.TAG, R.id.homeFram, true, false
//                )
//            }
            checkIn.featured_image_url?.let {
                Utils.stickImage(myContext, holder.postImage, it, null)
            } ?: run {
                if (post?.media?.isNotEmpty() == true) {
                    Utils.stickImage(myContext, holder.postImage, post?.media?.get(0), null)
                }

            }
            holder.postImage.onClick({
                openPostDetail(post)
            })
            var checkedText = "Checked in at ".plus(checkIn.name?.en)
            var spannable = SpannableString(checkedText)
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    Log.i(TAG, " clicked:::  ${checkIn.id}")
                    MyApplication.putTrackMP(Constant.AcProfileCheckInName, JSONObject(mapOf("postId" to post?._id)))
                    var baseFragment: BaseFragment = RestaurantDetailFragment();
                    Navigation.setFragmentData(baseFragment, "_id", checkIn.id)
                    Navigation.addFragment(
                        myContext, baseFragment,
                        RestaurantDetailFragment.TAG, R.id.homeFram, true, false
                    )
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }

            spannable.setSpan(
                clickableSpan, checkedText.indexOf("at").plus(2), checkedText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(myContext, R.color.primaryDark)),
                checkedText.indexOf("at").plus(2),
                checkedText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(Typeface.BOLD), checkedText.indexOf("at").plus(2), checkedText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            holder.caption.setText(spannable, TextView.BufferType.SPANNABLE)
            holder.caption.setMovementMethod(LinkMovementMethod.getInstance())
            checkIn.name?.let {
                holder.placeName.visibility = View.VISIBLE
                holder.placeName.text = it.en
            } ?: run {
                holder.placeName.visibility = View.GONE
            }
            checkIn.rating?.let {
                holder.rating.visibility = View.VISIBLE
                holder.ratingTxt.visibility = View.VISIBLE
                holder.rating.rating = it
                holder.ratingTxt.text = "5 of ".plus(it)
            } ?: run {
                holder.rating.visibility = View.GONE
                holder.ratingTxt.visibility = View.GONE
            }
            if (checkIn.timings?.isNotEmpty() == true) {
                holder.timing.visibility = View.VISIBLE
                holder.timing.text = "Open: ".plus(checkIn.timings?.get(0)?.opentime).plus(" - ")
                    .plus(checkIn.timings?.get(0)?.closetime)
            } else {
                holder.timing.visibility = View.GONE
            }
        }
    }

    private fun bindGridRecyclerData(
        holder: RGridviewHolder,
        post: FetchPostResponseItem?,
        position: Int
    ) {
        if (post?.media?.size?.compareTo(0) == 1) {
            Utils.stickImage(myContext, holder.postImage, post.media?.get(0), null)
            holder.imageCout.text =
                if (post.media!!.size > 6) "6+ Photos" else "${post.media!!.size} Photos"
            if (post.media?.size?.compareTo(1) == 1)
                holder.multiImageIcon.visibility = View.VISIBLE
            else
                holder.multiImageIcon.visibility = View.GONE

        }
        //holder.postImage.setOnClickListener{ openPostDetail(post) }
        holder.postImage.setOnClickListener {
            //fragment.tabLayout.
            fragment.tabLayout.selectTab(fragment.tabLayout.getTabAt(1))
            MyApplication.putTrackMP(Constant.AcProfileGridView, null)
            fragment.rvPost.scrollToPosition(position)
        }
    }

    private fun bindGridTextPostData(
        holder: GridTextHolder,
        post: FetchPostResponseItem?,
        position: Int
    ) {
        /*if(post?.media?.size?.compareTo(0) == 1) {
            Utils.stickImage(myContext, holder.postImage, post.media?.get(0), null)
            holder.imageCout.text = if(post.media!!.size > 6) "6+ Photos" else "${post.media!!.size} Photos"
            if(post.media?.size?.compareTo(1) ==1)
                holder.multiImageIcon.visibility = View.VISIBLE
            else
                holder.multiImageIcon.visibility = View.GONE

        }*/
        //holder.postImage.setOnClickListener{ openPostDetail(post) }
        holder.postText.text = post?.body
        post?.body_obj?.text_post?.let { textPost ->
            var gradient =
                GradientTypeArray.firstOrNull() { it.label.equals(textPost.gradient_type) }
            //holder.postText.background = ContextCompat.getDrawable(myContext,BodyObj.gradArray.get(index))
            gradient?.let {
                //holder.postText.background = Utils.gradientFromColor(it.gradient)
                //holder.postText.setTextColor(Color.parseColor(it.textColor))
            } ?: run {
                //holder.postText.background = Utils.gradientFromColor(GradientTypeArray.first().gradient)
                //holder.postText.setTextColor(Color.parseColor("#ffffff"))
            }
        } ?: run {
            //holder.postText.background = Utils.gradientFromColor(GradientTypeArray.first().gradient)
            //holder.postText.setTextColor(Color.parseColor("#ffffff"))
        }

        holder.postText.setOnClickListener {
            //fragment.tabLayout.
            fragment.tabLayout.selectTab(fragment.tabLayout.getTabAt(1))
            MyApplication.putTrackMP(Constant.AcProfileGridView, null)
            fragment.rvPost.scrollToPosition(position)
        }
    }

    fun bindGridMeetPostData(holder: GridMeetHolder, post: FetchPostResponseItem?, position: Int) {

        post?.body_obj?.open_meetup?.let { openMeet->
            //holder.monthOfYear.text = openMeet.date?.toDate()?.formatTo("M")
            holder.dayOfMonth.text = openMeet.date?.toDate()?.formatTo("dd")
            //holder.year.text = openMeet.date?.toDate()?.formatTo("yy")
            holder.dayOfWeak.text = openMeet.date?.toDate()?.formatTo("EEEE")
            holder.time.text = openMeet.date?.toDate()?.formatTo("hh:mm aa")
            holder.meetName.text = openMeet.name

        }


        holder.root.setOnClickListener {
            //fragment.tabLayout.
            fragment.tabLayout.selectTab(fragment.tabLayout.getTabAt(1))
            MyApplication.putTrackMP(Constant.AcProfileGridView, null)
            fragment.rvPost.scrollToPosition(position)
        }

    }


    fun addRecyclerListener(holder: RecyclerView.ViewHolder, post: FetchPostResponseItem?) {
        var holder = holder as RLinearviewHolder
        holder.postImage.setOnClickListener(DoubleClick(object : DoubleClickListener {
            override fun onDoubleClickEvent(view: View?) {
                likeDislikePost(holder, post)
            }

            override fun onSingleClickEvent(view: View?) {
                MyApplication.putTrackMP(Constant.AcPostImgToDetail, JSONObject(mapOf("postId" to post?._id)))
                openPostDetail(post)
            }
        }))
        holder.option.setOnClickListener {
            Log.i(TAG, " showMyPostOptionFragment:: ")
            selectedPost = post
            MyApplication.putTrackMP(Constant.AcProfileDotView, null)
            showMyPostOption()

        }
        holder.postImage.setOnLongClickListener {
            MyApplication.putTrackMP(Constant.AcProfilePostLongPress, null)
            Log.i(TAG, " showMyPostOptionFragment:: ")
            selectedPost = post
            showMyPostOption()
            true
        }
    }

    fun addTextPostListener(holder: LinearTextPostHolder, post: FetchPostResponseItem?) {

        if(post?.mentions?.isEmpty() == true) {
            holder.caption.setOnClickListener(DoubleClick(object : DoubleClickListener {
                override fun onDoubleClickEvent(view: View?) {
                    Log.i(TAG, " textpost::: onDoubleClickEvent")
                    likeDislikePost(holder, post)
                }

                override fun onSingleClickEvent(view: View?) {
                    Log.i(TAG, " textpost::: onSingleClickEvent")
                    var baseFragment: BaseFragment = DetailPostFragment()
                    Navigation.setFragmentData(
                        baseFragment,
                        "post_id",
                        post?._id
                    )
                    Navigation.addFragment(
                        myContext,
                        baseFragment,
                        Constant.TAG_DETAIL_POST_FRAGMENT,
                        R.id.homeFram,
                        true,
                        false
                    )
                    //openPostDetail(post)
                    //Utils.onClick(holder.caption,1000) { openPostDetail(post) }
                }
            }))
        }else{
            holder.caption.setOnClickListener(null)
        }

        post?.body_obj?.text_post?.let { textPost ->
            var gradient =
                GradientTypeArray.firstOrNull() { it.label.equals(textPost.gradient_type) }
            /*if(index != -1)
            holder.caption.background = ContextCompat.getDrawable(myContext, BodyObj.gradArray.get(index))*/
            gradient?.let {
                //holder.caption.background = Utils.gradientFromColor(gradient.gradient)
                //holder.caption.setTextColor(Color.parseColor(it.textColor))
            } ?: run {
                //holder.caption.background = Utils.gradientFromColor(GradientTypeArray.first().gradient)
                //holder.caption.setTextColor(Color.parseColor("#ffffff"))
            }
        } ?: run {
            //holder.caption.background = Utils.gradientFromColor(GradientTypeArray.first().gradient)
            //holder.caption.setTextColor(Color.parseColor("#ffffff"))
        }
        //BodyObj.gradArray

        holder.option.setOnClickListener {
            Log.i(TAG, " showMyPostOptionFragment:: ")
            selectedPost = post
            showCheckOption()
        }
        holder.caption.setOnLongClickListener {
            Log.i(TAG, " showMyPostOptionFragment:: ")
            selectedPost = post
            showCheckOption()
            if (post?.user_id?.equals(MyApplication.SID) == true) {
                //showOption(myPostOptions)
            } else {
                //showOption(otherPostOptions)
            }
            true
        }

    }

    fun addCommonLitener(holder: CommonOption, post: FetchPostResponseItem?) {
        var myId = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid
        if(post?.user_meta?.sid.equals(myId)&&post?.boosted == true) {
            post?.boost_meta?.let {
//                it.terminating_at?.let { it1 ->
                    holder.tvBoostTime.visibility=View.VISIBLE
//                    holder.tvBoostTime.text = Utils.timeDifference(it1.toDate())
//                    startTimer(it1,holder)
//                }
            }?:run{
                holder.tvBoostTime.visibility=View.GONE
            }
        }else{
            holder.tvBoostTime.visibility=View.GONE
        }
        if(post?.type!=Constant.Post.CHECK_IN.type) {
            holder.tvInsight.visibility = View.VISIBLE
            holder.tvInsight.onClick({
                MyApplication.putTrackMP(Constant.AcPostInsight, JSONObject(mapOf("postId" to post?._id)))
                openInsight(post)
            })
        }else{
            holder.tvInsight.visibility = View.GONE
            holder.tvInsight.onClick({
                MyApplication.putTrackMP(Constant.AcPostInsight, JSONObject(mapOf("postId" to post?._id)))
            })
        }
        holder.like.setOnClickListener{
            MyApplication.putTrackMP(Constant.AcPostlikeButton, JSONObject(mapOf("postId" to post?._id)))
            likeDislikePost(holder, post)
        }
        if(post?.liked_by_user == true){
            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_filled, 0, 0, 0)
            holder.isPostLiked = true
        }else{
            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_small_heart, 0, 0, 0)
            holder.isPostLiked = false
        }
        holder.commentCount.text = post?.stats?.comments.toString().plus(" Comments")
        holder.likeCount.text = post?.stats?.likes.toString().plus(" likes")
        holder.comment.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcPostCmtToDetail, JSONObject(mapOf("postId" to post?._id)))
            openPostDetail(post)
        }
        holder.like.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcPostlikeButton, JSONObject(mapOf("postId" to post?._id)))
            likeDislikePost(holder, post)
        }
        holder.likeCount.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcPostLikeCount, JSONObject(mapOf("postId" to post?._id)))
            openLikerpage(post?._id)
        }
        holder.commentCount.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcPostCmtCount, JSONObject(mapOf("postId" to post?._id)))
            openPostDetail(post)
        }
    }

    fun showCustomPlaceAlert(latLng: LatLng) {
        var fragment = customPlaceAlert
        customPlaceAlert?.setLatLng(latLng)
        if(fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(this.fragment.childFragmentManager, fragment.tag)
    }

    fun showMyPostOptionFragment() {
        var fragment = myPostOptions as BottomSheetDialogFragment?
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(this.fragment.childFragmentManager, fragment.tag)
    }
    fun showMyPostOption(){
        myPostArray= arrayListOf("Edit", "Enable Comment", "Delete")
        if(selectedPost?.comments_enabled == true){
            myPostArray= arrayListOf("Edit", "Disable Comment", "Delete")
        }
        myPostSheet.setArrayList(myPostArray)
        myPostSheet.show()
    }
    fun showCheckOption(){
        myCheckInArray= arrayListOf("Enable Comment", "Delete")
        if(selectedPost?.comments_enabled == true){
            myCheckInArray= arrayListOf("Disable Comment", "Delete")
        }
        myCheckInSheet.setArrayList(myCheckInArray)
        myCheckInSheet.show()
    }

    fun openLikerpage(postId: String?) {
        var baseFragment: BaseFragment = LikeFragment()
        Navigation.setFragmentData(baseFragment, LikeFragment.POST_ID, postId)
        Navigation.addFragment(
            myContext, baseFragment,
            Constant.TAG_LIKE_FRAGMENT, R.id.homeFram, true, false
        )
    }

    private fun likeDislikePost(holder: CommonOption, post: FetchPostResponseItem?) {
        if (!holder.isPostLiked) {
            holder.isPostLiked = true
            if (post?.liked_by_user == true) {
                holder.likeCount.text = (post?.stats?.likes).toString().plus(" likes")
            } else {
                holder.likeCount.text = (post?.stats?.likes?.plus(1)).toString().plus(" likes")

            }
            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_filled, 0, 0, 0)
            holder.animLike.apply {
                this.alpha = 1.0f
                if (this.drawable is AnimatedVectorDrawableCompat) {
                    var avd = this.drawable as AnimatedVectorDrawableCompat
                    avd.start()
                } else if (this.drawable is AnimatedVectorDrawable) {
                    var avd = this.drawable as AnimatedVectorDrawable
                    avd.start()
                }
            }
        } else {
            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_small_heart, 0, 0, 0)
            if (post?.liked_by_user == true) {
                holder.likeCount.text = (post.stats?.likes!! - 1).toString().plus(" likes")
            } else {
                holder.likeCount.text = (post?.stats?.likes).toString().plus(" likes")
            }
            holder.isPostLiked = false
        }
        likeListener(post?._id)
    }

    fun openPostDetail(post: FetchPostResponseItem?) {
        /*var detailPostParcel = Utils.getDetailPostParcel(post)
        val baseFragment: BaseFragment = DetailPostFragment()
        var bundle: Bundle = Bundle()
        bundle.putParcelable("post", detailPostParcel)
        baseFragment.arguments = bundle
        Navigation.addFragment(
            myContext, baseFragment,
            Constant.TAG_DETAIL_POST_FRAGMENT, R.id.homeFram,
            stack = true,
            needAnimation = false
        )*/
        val baseFragment: BaseFragment = DetailPostFragment()
        Navigation.setFragmentData(baseFragment, "post_id", post?._id)
        Navigation.addFragment(
            myContext,
            baseFragment,
            Constant.TAG_DETAIL_POST_FRAGMENT,
            R.id.homeFram,
            true,
            false
        )

    }


    fun openInsight(post: FetchPostResponseItem?) {
        val baseFragment: BaseFragment = PostInSightFragment()
        val bundle: Bundle = Bundle()
        Log.d(TAG, "openInsight:post?._id ${post?._id}")
        //bundle.putString("post", post?._id)
        baseFragment.arguments = bundleOf("post_id" to post?._id)
//        Navigation.setFragmentData(baseFragment,"post", post?._id)
        Navigation.addFragment(
            myContext, baseFragment,
                baseFragment.javaClass.simpleName, R.id.homeFram,
            stack = true,
            needAnimation = false
        )

    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<FetchPostResponseItem>() {
            override fun areItemsTheSame(
                oldItem: FetchPostResponseItem,
                newItem: FetchPostResponseItem
            ): Boolean {
                return (oldItem._id == newItem._id)
            }

            override fun areContentsTheSame(
                oldItem: FetchPostResponseItem,
                newItem: FetchPostResponseItem
            ): Boolean {
                return oldItem == newItem
            }

        }

        val GRID = "grid"
        val LINEAR = "linear"


        val LINEAR_TEXT = 1
        val LINEAR_IMAGE = 2

        val GRID_TEXT = 3
        val GRID_IMAGE = 4

        val LINEAR_MEET = 5
        val GRID_MEET = 6

        val LINEAR_CHECKIN = 7
        val GRID_CHECKIN = 8


    }


}