package com.meetsportal.meets.adapter

import android.app.Activity
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
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.ablanco.zoomy.Zoomy
import com.ddd.androidutils.DoubleClick
import com.ddd.androidutils.DoubleClickListener
import com.fenchtose.tooltip.Tooltip
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.post.FetchPostResponseItem
import com.meetsportal.meets.networking.post.OpenMeetUp
import com.meetsportal.meets.networking.profile.Badge
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.networking.profile.SuggestionResponse
import com.meetsportal.meets.ui.bottomsheet.ChooseTimeLineFeedSheet
import com.meetsportal.meets.ui.bottomsheet.ChooseTimeLineFeedSheet.Companion.GLOBAL_TIMELINE
import com.meetsportal.meets.ui.bottomsheet.ChooseTimeLineFeedSheet.Companion.OPEN_MEET
import com.meetsportal.meets.ui.bottomsheet.ReportSheet
import com.meetsportal.meets.ui.dialog.CustomPlaceDetailAlert
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.ui.fragments.socialfragment.*
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.AcCardtoDetailOpMeet
import com.meetsportal.meets.utils.Constant.AcCreatePostFrHome
import com.meetsportal.meets.utils.Constant.AcDisableCmnt
import com.meetsportal.meets.utils.Constant.AcDpOnTimeLine
import com.meetsportal.meets.utils.Constant.AcEditImagePost
import com.meetsportal.meets.utils.Constant.AcFeedChange
import com.meetsportal.meets.utils.Constant.AcJoinOpMTimeline
import com.meetsportal.meets.utils.Constant.AcPostCmtCount
import com.meetsportal.meets.utils.Constant.AcPostCmtToDetail
import com.meetsportal.meets.utils.Constant.AcPostImgToDetail
import com.meetsportal.meets.utils.Constant.AcPostInsight
import com.meetsportal.meets.utils.Constant.AcPostLikeCount
import com.meetsportal.meets.utils.Constant.AcSeeAllSuggestFrHome
import com.meetsportal.meets.utils.Constant.GradientTypeArray
import com.meetsportal.meets.utils.Constant.POST_TYPE
import com.meetsportal.meets.utils.Constant.TAG_CREATE_POST_FRAGMENT
import com.meetsportal.meets.utils.Constant.TAG_LIKE_FRAGMENT
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import com.meetsportal.meets.viewmodels.PostViewModel
import org.json.JSONObject
import java.util.*

class  TimeLineAdapter(var myContext: Activity, var timeLineFragment: TimeLineFragment, var type: Int?, var suggestionResponse: SuggestionResponse?, var postModel: PostViewModel, var meetUpViewModel: MeetUpViewModel, var likeListener: (String?) -> Unit, var followUnfollw: (String, Boolean) -> Unit, var deletePost: (String?) -> Unit,val openProfile :(String?,String?)->Unit) : PagingDataAdapter<FetchPostResponseItem, RecyclerView.ViewHolder>(COMPARATOR), ChooseTimeLineFeedSheet.BottomSheetListener {


    var viewPool = RecyclerView.RecycledViewPool()
    var gestureDetector: GestureDetector
    var customPlaceAlert : CustomPlaceDetailAlert? = null

    var chooseTimelineFeedSheeet: ChooseTimeLineFeedSheet? = null
    var myPostSheet = BottomSheetOptions.getInstance("Edit","Enable Comments","Disable Comments","Delete",4).apply {
        setBottomSheetLitener(object : BottomSheetOptions.BottomSheetListener{
            override fun bottomSheetClickedOption(buttonClicked: String) {
                when(buttonClicked){
                    BottomSheetOptions.BUTTON1 ->{
                        MyApplication.putTrackMP(AcEditImagePost, JSONObject(mapOf("postId" to selectedPost?._id)))
                        val baseFragment: BaseFragment = EditPostFragment()
                        Navigation.setFragmentData(baseFragment, "postId", selectedPost?._id)
                        Navigation.addFragment(myContext, baseFragment, Constant.TAG_EDIT_POST_FRAGMENT, R.id.homeFram, true, false)
                    }
                    BottomSheetOptions.BUTTON2 ->{
                        postModel.disableComment(selectedPost?._id, true)
                    }
                    BottomSheetOptions.BUTTON3 ->{
                        MyApplication.putTrackMP(AcDisableCmnt, JSONObject(mapOf("postId" to selectedPost?._id)))
                        postModel.disableComment(selectedPost?._id, false)
                    }
                    BottomSheetOptions.BUTTON4 ->{
                        MyApplication.putTrackMP(Constant.AcPostDelete, JSONObject(mapOf("postId" to selectedPost?._id)))
                        deletePost(selectedPost?._id)
                    }
                }
                //option(buttonClicked,getItem(cardPosition))
            }
        })
    }

    var otherPostOptions: BottomSheetOptions? = null

    val TAG = TimeLineAdapter::class.java.simpleName

    /*val HEADER = 100
    val DEFAULT = 101
    val CHECK_IN = 102
    val TEXT_POST = 103*/

    var headerHolder: HviewHolder? = null

    var selectedPost: FetchPostResponseItem? = null

    init {
        customPlaceAlert = CustomPlaceDetailAlert(myContext)
        chooseTimelineFeedSheeet = ChooseTimeLineFeedSheet()
        chooseTimelineFeedSheeet?.setBottomSheetLitener(this)
        otherPostOptions = BottomSheetOptions.getInstance("Report", null, null, null, 1)
        otherPostOptions?.setBottomSheetLitener(object : BottomSheetOptions.BottomSheetListener {
            override fun bottomSheetClickedOption(buttonClicked: String) {
                when(buttonClicked) {
                    BottomSheetOptions.BUTTON1 -> {
                        showOption(ReportSheet(postModel, myContext, selectedPost?._id, "post"))
                    }
                }
            }
        })


        gestureDetector = GestureDetector(myContext, MyGestureListener({
            if(it == MyGestureListener.Direction.up) {

            }
        }, {}))
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is HviewHolder) {
            Log.i("TAG", " checkHviewHolderPosition:: ${position}")
            headerHolder = holder
            Utils.showToolTips(myContext,holder.thinking,timeLineFragment.binding.rootCo, Tooltip.BOTTOM,"Create a text post or a post With images","thinking"){
            }
            Utils.showToolTips(myContext,holder.currentFeed,timeLineFragment.binding.rootCo, Tooltip.BOTTOM,"Choose what type of feed you Want to see","currentFeed"){
            }

        } else if(holder is RviewHolder) {
            Log.i("TAG", " checkRecyclerPosition:: ${position}")
            //headerHolder?.noDataScreen?.visibility = View.GONE
            val post = getItem(position)
            bindRecyclerData(holder, post)
            addRecyclerListener(holder, post)
            val doubleClick = false
        } else if(holder is CviewHolder) {
            Log.i("TAG", " checkCRecyclerPosition:: ${position}")
            val post = getItem(position)
            bindCheckinData(holder, post)
            addCheckinListener(holder, post)
        } else if(holder is TextPostHolder) {
            val post = getItem(position)
            bindTextPostData(holder, post)
            addTextPostListener(holder, post)
        } else if(holder is OpenMeetHolder) {
            val post = getItem(position)
            bindOpenMeet(holder, post)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when(viewType) {
            POST_TYPE.HEADER.label    -> return HviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.timeline_recycler_header, parent, false))
            POST_TYPE.DEFAULT.label   -> return RviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.card_news_feed, parent, false))
            POST_TYPE.CHECK_IN.label  -> return CviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.card_check_in, parent, false))
            POST_TYPE.TEXT_POST.label -> return TextPostHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.card_text_post, parent, false))
            POST_TYPE.OPEN_MEET.label -> return OpenMeetHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.card_open_meet_post, parent, false), myContext, viewPool)
            else                      -> return RviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.card_news_feed, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        var data = getItem(position)
        return when(position) {
            0    -> POST_TYPE.HEADER.label

            else -> {
                Log.i(TAG, " type:: ${data?.type} -- $position")
                when(data?.type) {
                    Constant.Post.DEFAULT.type   -> POST_TYPE.DEFAULT.label
                    Constant.Post.CHECK_IN.type  -> POST_TYPE.CHECK_IN.label
                    Constant.Post.TEXT_POST.type -> POST_TYPE.TEXT_POST.label
                    Constant.Post.OPEN_MEET.type -> POST_TYPE.OPEN_MEET.label
                    else                         -> POST_TYPE.DEFAULT.label
                }
            }
            //else ->  RECYCLER
        }
    }


    fun bindRecyclerData(holder: RviewHolder, post: FetchPostResponseItem?) {
        var myProfile = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
        if(post?.user_meta?.verified_user == true) holder.isVerified.visibility = View.VISIBLE
        else holder.isVerified.visibility = View.GONE
        addCommonLitener(holder, post)
        //Utils.stickImage(myContext, holder.dp, post?.user_meta?.profile_image_url, null)
        holder.dp.loadImage(myContext,post?.user_meta?.profile_image_url,R.drawable.ic_default_person)
        holder.dp.onClick({
            MyApplication.putTrackMP(AcDpOnTimeLine, JSONObject(mapOf("sid" to post?.user_meta?.sid)))
            openProfile(post?.user_meta?.sid,post?._id)
        })

        post?.user_meta?.badge?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            holder.ivDpBadge.setImageResource(firstOrNull.foreground)
        }
        holder.name.onClick( {
            openProfile(post?.user_meta?.sid,post?._id)
        })

        if(post?.media?.size?.compareTo(0) == 1) {
            Log.i(TAG, " sizeOfImage ")
            Utils.stickImage(myContext, holder.postImage, post.media?.getOrNull(0), null)

            val builder: Zoomy.Builder = Zoomy.Builder(myContext)
                .target(holder.postImage)
                .enableImmersiveMode(false)
                .animateZooming(false)
                .tapListener{
                    MyApplication.putTrackMP(AcPostImgToDetail, JSONObject(mapOf("postId" to post._id)))
                    openPostDetail(post)
                }
                .doubleTapListener {
                    likeDislikePost(holder, post)
                }
                .longPressListener {
                    selectedPost = post
                    if(post?.user_id?.equals(MyApplication.SID) == true) {
                        showMine(post?.comments_enabled)
                    } else {
                        showOther()
                    }
                }
            builder.register()

            holder.imageCout.text = if(post.media!!.size > 6) "6+ Photos" else "${post.media!!.size} Photos"
            if(post.media?.size?.compareTo(1) == 1) holder.multiImageIcon.visibility = View.VISIBLE
            else holder.multiImageIcon.visibility = View.GONE

        }
        var anim = holder.animLike.drawable
        if(anim is AnimatedVectorDrawableCompat) {

        }
        //holder.name.text = post?.user_meta?.first_name?.plus(post.user_meta!!.last_name)
        holder.name.text = post?.user_meta?.username

        holder.caption.setText(Utils.setMentionInCaption(myContext,post?.body?:"",post?.mentions,post?._id),TextView.BufferType.NORMAL)
        holder.caption.setMovementMethod(LinkMovementMethod.getInstance())
        holder.caption.onClick({
            MyApplication.putTrackMP(AcPostImgToDetail, JSONObject(mapOf("postId" to post?._id)))
            openPostDetail(post)
        })
        //holder.caption.text = post?.body
        //post?.createdAt?.toDate()?.compareTo(Date())


        var diffrence = (Date().time - post?.createdAt?.toDate()?.time!!) / 1000
        Log.i("TAG", " Diffrence ${diffrence}")
        holder.lastseenandLoc.text = Utils.getCreatedAt(post.createdAt)

    }

    fun bindOpenMeet(holder: OpenMeetHolder, post: FetchPostResponseItem?) {
        var myProfile = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
        if(post?.user_meta?.verified_user == true) holder.isVerified.visibility = View.VISIBLE
        else holder.isVerified.visibility = View.GONE

        addCommonLitener(holder, post)

        //Utils.stickImage(myContext, holder.dp, post?.user_meta?.profile_image_url, null)
        holder.dp.loadImage(myContext,post?.user_meta?.profile_image_url,R.drawable.ic_default_person)

        holder.name.text = post?.user_meta?.username
        holder.lastseenandLoc.text = Utils.getCreatedAt(post?.createdAt)

        holder.view.onClick({
            MyApplication.putTrackMP(Constant.AcOpMeetView, JSONObject(mapOf("postId" to post?.body_obj?.open_meetup?.meetup_id)))
            openPostDetail(post)
        })
        post?.body_obj?.open_meetup?.let { openMeet ->
            holder.monthOfYear.text = openMeet.date?.toDate()?.formatTo("MMM")
            holder.dayOfMonth.text = openMeet.date?.toDate()?.formatTo("dd")
            holder.dayOfWeak.text = openMeet.date?.toDate()?.formatTo("EEEE")
            holder.timeOfDay.text = openMeet.date?.toDate()?.formatTo("hh:mmaa")
            holder.meetName.text = openMeet.name

            holder.join.setTextColor(ContextCompat.getColor(myContext, R.color.white))
            //holder.join.setRoundedColorBackground(myContext,R.color.transparent)
            setMeetMultipurposeButton(openMeet,holder,post)


            if(openMeet.description?.isEmpty() == true) {
                holder.desc.text = "No description added"
            } else {
                holder.desc.text = openMeet.description
            }
            when(openMeet.chosen_place?.type) {
                Constant.PlaceType.MEET.label   -> {
                    holder.address.text = openMeet.places?.firstOrNull()?.name?.en
                    holder.address.onClick( {
                        MyApplication.putTrackMP(Constant.AcOpMeetLocation, JSONObject(mapOf("postId" to openMeet.meetup_id)))
                        var baseFragment: BaseFragment = RestaurantDetailFragment();
                        Navigation.setFragmentData(baseFragment, "_id", openMeet.places?.firstOrNull()?._id)
                        Navigation.addFragment(myContext, baseFragment, RestaurantDetailFragment.TAG, R.id.homeFram, true, false)
                    })
                }

                Constant.PlaceType.CUSTOM.label -> {
                    holder.address.setOnClickListener(null)
                    holder.address.text = openMeet.custom_places?.firstOrNull()?.name
                    holder.address.onClick( {
                        MyApplication.putTrackMP(Constant.AcOpMeetLocation, JSONObject(mapOf("postId" to openMeet.meetup_id)))
                        showCustomPlaceAlert(LatLng(openMeet.custom_places?.firstOrNull()?.getLongitude()?:0.0,openMeet.custom_places?.firstOrNull()?.getLatitude()?:0.0))
                    })

                }
            }
            if(openMeet.join_requests?.requests?.size?.compareTo(0) == 1) {
                holder.llNoRequest.visibility = View.GONE
                holder.llRequest.visibility = View.VISIBLE
                if(openMeet.join_requests?.requests?.size == 1) holder.countInterested.text = openMeet.join_requests?.requests?.size.toString()
                    .plus(" person\ninterested")
                else if(openMeet.join_requests?.requests?.size > 1) holder.countInterested.text = openMeet.join_requests?.requests?.size.toString()
                    .plus(" people\ninterested")

                holder.countInterested.onClick({
                    MyApplication.putTrackMP(Constant.AcOpMeetInterested, JSONObject(mapOf("postId" to openMeet.meetup_id)))
                    if(post.user_id.equals(MyApplication.SID)){
                        Log.i(TAG," itsMine::: true  0 ${openMeet.meetup_id}")
                        var fragment = RequestToJoinOpenFragment.getInstance(openMeet.meetup_id)
                        Navigation.addFragment(myContext,fragment,RequestToJoinOpenFragment.TAG,R.id.homeFram,true,false)
                    }
                    else{
                    Log.i(TAG," itsMine::: false  0 ${openMeet.meetup_id}")
                    var fragment = OpenMeetIntrestList.getInstance(openMeet.meetup_id)
                    Navigation.addFragment(myContext,fragment,"OpenMeetIntrestList",R.id.homeFram,true,false)
                }
                })
                holder.rvIntrested.adapter = OpenMeetJoinRqPeopleStackAdapter(myContext, openMeet){
                    holder.countInterested.callOnClick()
                }
            } else {
                holder.llNoRequest.visibility = View.VISIBLE
                holder.llRequest.visibility = View.GONE
                if(post?.user_id.equals(MyApplication.SID)) {
                    holder.tvFirst.text="Requested\n will displayed here"
                }
                holder.rvIntrested.adapter = OpenMeetJoinRqPeopleStackAdapter(myContext, openMeet){
                    holder.countInterested.callOnClick()
                }
            }
        }

        holder.rlIntract.onClick({
            MyApplication.putTrackMP(AcCardtoDetailOpMeet, JSONObject(mapOf("postId" to post?.body_obj?.open_meetup?.meetup_id)))
            var baseFragment: BaseFragment = DetailPostFragment()
            Navigation.setFragmentData(baseFragment, "post_id", post?._id)
            Navigation.addFragment(myContext, baseFragment, Constant.TAG_DETAIL_POST_FRAGMENT, R.id.homeFram, true, false)
        })


        holder.option.setOnClickListener {
            Log.i(TAG, " showMyPostOptionFragment:: ")
            selectedPost = post

            if(post?.user_id?.equals(MyApplication.SID) == true) {
                //showMyPostOptionFragment()
                showMine(post?.comments_enabled)
            } else {
                //showOtherPostOptionFragment()
//                showOption(otherPostOptions)
                showOther()
            }
        }

        holder.dp.onClick( {
            MyApplication.putTrackMP(AcDpOnTimeLine, JSONObject(mapOf("sid" to post?.user_meta?.sid)))
            openProfile(post?.user_meta?.sid,post?._id)
        })

        post?.user_meta?.badge?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            holder.ivDpBadge.setImageResource(firstOrNull.foreground)
        }
        holder.name.onClick( {
            openProfile(post?.user_meta?.sid,post?._id)
        })
    }

    fun setMeetMultipurposeButton(
        openMeet: OpenMeetUp,
        holder: OpenMeetHolder,
        post: FetchPostResponseItem
    ) {
        var currentUser = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
        Log.i(TAG, "setMeetMultipurposeButton: ${openMeet.max_join_time?.value}")
        if((openMeet.max_join_time?.value?.toDate()?.time?.compareTo(Date().time) == -1) || (Utils.getDayDiff(openMeet.date?.toDate(), Date())?.compareTo(0) == -1) ){
            holder.join.setRoundedColorBackground(myContext, R.color.gray1)
            holder.join.text = " closed "
            holder.join.onClick({})
            return
        }
        if(post.user_meta?.sid.equals(MyApplication.SID)) {
            Log.d(DetailPostFragment.TAG, "populateMeetPost: 1")
            if(openMeet.voting_closed == false) {
                Log.d(DetailPostFragment.TAG, "populateMeetPost: 3")
                holder.join.setRoundedColorBackground(myContext, R.color.gray1)
                holder.join.text = "Admin"
                holder.join.onClick({})
                holder.join.visibility=View.INVISIBLE
            } else {
                Log.d(DetailPostFragment.TAG, "populateMeetPost: 4")
                holder.join.setRoundedColorBackground(myContext, R.color.gray1)
    //                holder.join.text = "You closed this meetup"
                holder.join.text = "closed"
                holder.join.onClick({})
            }
        } else {
            Log.d(DetailPostFragment.TAG, "populateMeetPost: 5")
            if(openMeet.join_accepted_by_user == true){
                Log.d(DetailPostFragment.TAG, "populateMeetPost: 7")
                holder.join.setRoundedColorBackground(myContext, R.color.gray1)
                holder.join.setOnClickListener(null)
                holder.join.text = "Accepted"
            }
            if(openMeet.join_requested_by_user == true) {
                Log.d(DetailPostFragment.TAG, "populateMeetPost: 7")
                holder.join.setRoundedColorBackground(myContext, R.color.gray1)
                holder.join.setOnClickListener(null)
                holder.join.text = "Requested"
//                holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP,10f)
            } else {
                Log.d(DetailPostFragment.TAG, "populateMeetPost: 8")
                holder.join.setRoundedColorBackground(myContext, R.color.primaryDark)
                holder.join.onClick({
                    MyApplication.putTrackMP(AcJoinOpMTimeline, JSONObject(mapOf("meetId" to openMeet.meetup_id)))
                    if( (openMeet.max_join_time?.value?.toDate()?.time?.compareTo(Date().time) == -1)){
                        timeLineFragment.showToast("Meetup join time is passed!")
                        holder.join.setRoundedColorBackground(myContext, R.color.gray1)
                        holder.join.text = " closed "
                    }else {
                        val badge: Badge = Utils.getBadge(openMeet.min_badge)
                        val currentBadge: Badge = Utils.getBadge(currentUser?.social?.getbadge())
                        if(currentBadge.level>=badge.level){
                            meetUpViewModel.joinOpenMeet(openMeet.meetup_id)
                        }else{
                            timeLineFragment.showToast("${badge.name} Status and above are eligible for join this meetup")
                        }
                    }
                })
                holder.join.text = "Join"
            }
        }


        /*if(post.user_meta?.sid.equals(MyApplication.SID)) {
            if(openMeet.voting_closed == false) {
                holder.join.setRoundedColorBackground(myContext, R.color.gray1)
                holder.join.setOnClickListener(null)
                holder.join.visibility = View.INVISIBLE
                holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)

            } else {
                holder.join.setRoundedColorBackground(myContext, R.color.transparent)
                holder.join.setOnClickListener(null)
                holder.join.visibility = View.VISIBLE
                holder.join.text = "Closed"
                holder.join.setTextColor(ContextCompat.getColor(myContext, R.color.gray1))
                holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            }
        }
        else {
            holder.join.visibility = View.VISIBLE
            if(openMeet.join_requested_by_user == true) {
                holder.join.setRoundedColorBackground(myContext, R.color.gray1)
                holder.join.setOnClickListener(null)
                holder.join.text = "Requested"
                holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            } else {
                if(openMeet.voting_closed == false) {
                    holder.join.text = "Join"
                    holder.join.setRoundedColorBackground(myContext, R.color.primaryDark)
                    holder.join.setOnClickListener { meetUpViewModel.joinOpenMeet(openMeet.meetup_id) }
                    holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                } else {
                    holder.join.setRoundedColorBackground(myContext, R.color.gray1)
                    holder.join.setOnClickListener(null)
                    holder.join.visibility = View.VISIBLE
                    holder.join.text = "Closed"
                    holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                }
            }
        }*/
    }

    fun bindTextPostData(holder: TextPostHolder, post: FetchPostResponseItem?) {
        var myProfile = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
        if(post?.user_meta?.verified_user == true) holder.isVerified.visibility = View.VISIBLE
        else holder.isVerified.visibility = View.GONE

        addCommonLitener(holder, post)
        //Utils.stickImage(myContext, holder.dp, post?.user_meta?.profile_image_url, null)
        holder.dp.loadImage(myContext,post?.user_meta?.profile_image_url,R.drawable.ic_default_person)
        holder.dp.onClick( {
            MyApplication.putTrackMP(AcDpOnTimeLine, JSONObject(mapOf("sid" to post?.user_meta?.sid)))
            openProfile(post?.user_meta?.sid,post?._id)
        })

        post?.user_meta?.badge?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            holder.ivDpBadge.setImageResource(firstOrNull.foreground)
        }
        holder.name.onClick( {
            openProfile(post?.user_meta?.sid,post?._id)
        })


        var anim = holder.animLike.drawable
        if(anim is AnimatedVectorDrawableCompat) {

        }
        holder.name.text = post?.user_meta?.username

        holder.caption.setText(Utils.setMentionInCaption(myContext,post?.body,post?.mentions,post?._id),TextView.BufferType.SPANNABLE)
        holder.caption.setMovementMethod(LinkMovementMethod.getInstance())
        holder.caption.setFocusable(false);
        //holder.caption.text = post?.body


        var diffrence = (Date().time - post?.createdAt?.toDate()?.time!!) / 1000
        Log.i("TAG", " Diffrence ${diffrence}")
        holder.lastseenandLoc.text = "".plus(Utils.getCreatedAt(post.createdAt))

        post?.body_obj?.text_post?.let { textPost ->
            var gradient = GradientTypeArray.firstOrNull() { it.label.equals(textPost.gradient_type) }
            gradient?.let {
                //holder.textPostBg.background = Utils.gradientFromColor(it.gradient)
                //holder.caption.setTextColor(Color.parseColor(it.textColor))
            } ?: run {
                //holder.textPostBg.background = Utils.gradientFromColor(GradientTypeArray.first().gradient)
               // holder.caption.setTextColor(Color.parseColor("#ffffff"))
            }
        } ?: run {
            //holder.textPostBg.background = Utils.gradientFromColor(GradientTypeArray.first().gradient)
            //holder.caption.setTextColor(Color.parseColor("#ffffff"))
        }

    }


    fun addTextPostListener(holder: TextPostHolder, post: FetchPostResponseItem?) {
        if(post?.mentions?.isEmpty() == true){
            holder.caption.setOnClickListener(DoubleClick(object : DoubleClickListener {
                override fun onDoubleClickEvent(view: View?) {
                    Log.i(TAG, " textpost::: onDoubleClickEvent")
                    likeDislikePost(holder, post)
                }

                override fun onSingleClickEvent(view: View?) {
                    Log.i(TAG, " textpost::: onSingleClickEvent")
                    var baseFragment: BaseFragment = DetailPostFragment()
                    Navigation.setFragmentData(baseFragment, "post_id", post?._id)
                    Navigation.addFragment(myContext, baseFragment, Constant.TAG_DETAIL_POST_FRAGMENT, R.id.homeFram, true, false)
                }
            }))
        }else{
            holder.caption.setOnClickListener(null)
        }

        /*post?.body_obj?.text_post?.let { textPost ->
            var gradient =
                GradientTypeArray.firstOrNull() { it.label.equals(textPost.gradient_type) }
            gradient?.let {
                holder.caption.background =  it.gradient

            } ?: run {
                holder.caption.background =GradientTypeArray.first().gradient
            }
        }?:run{
            holder.caption.background =GradientTypeArray.first().gradient
        }*/
        holder.option.setOnClickListener {
            Log.i(TAG, " showMyPostOptionFragment:: ")
            selectedPost = post
            if(post?.user_id?.equals(MyApplication.SID) == true) {
//                showOption(myPostOptions)
                showMine(post?.comments_enabled)
            } else {
//                showOption(otherPostOptions)
                showOther()
            }
        }
        holder.caption.setOnLongClickListener {
            Log.i(TAG, " showMyPostOptionFragment:: ")
            selectedPost = post
            if(post?.user_id?.equals(MyApplication.SID) == true) {

//                showOption(myPostOptions)
                showMine(post?.comments_enabled)
            } else {

//                showOption(otherPostOptions)
                showOther()
            }
            true
        }

    }

    fun bindCheckinData(holder: CviewHolder, post: FetchPostResponseItem?) {
        var myProfile = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
        if(post?.user_meta?.verified_user == true) holder.isVerified.visibility = View.VISIBLE
        else holder.isVerified.visibility = View.GONE

        addCommonLitener(holder, post)
        //Utils.stickImage(myContext, holder.dp, post?.user_meta?.profile_image_url, null)
        holder.dp.loadImage(myContext,post?.user_meta?.profile_image_url,R.drawable.ic_default_person)
        holder.dp.onClick( {
            MyApplication.putTrackMP(AcDpOnTimeLine, JSONObject(mapOf("sid" to post?.user_meta?.sid)))
            openProfile(post?.user_meta?.sid,post?._id)
        })

        post?.user_meta?.badge?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            holder.ivDpBadge.setImageResource(firstOrNull.foreground)
        }
        holder.name.onClick( {
            openProfile(post?.user_meta?.sid,post?._id)
        })



        if(post?.media?.size?.compareTo(0) == 1) {
            Log.i(TAG, " sizeOfImage ")
            holder.imageCout.text = if(post.media!!.size > 6) "6+ Photos" else "${post.media!!.size} Photos"
        }
        var anim = holder.animLike.drawable
        if(anim is AnimatedVectorDrawableCompat) {

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
//                Navigation.addFragment(myContext, baseFragment, RestaurantDetailFragment.TAG, R.id.homeFram, true, false)
//            }
            checkIn.featured_image_url?.let {
                Utils.stickImage(myContext, holder.postImage, it, null)
            } ?: run {
                if(post?.media?.isNotEmpty() == true) {
                    Utils.stickImage(myContext, holder.postImage, post?.media?.get(0), null)
                }

            }
            var checkedText = "Checked in at ".plus(checkIn.name?.en)
            var spannable = SpannableString(checkedText)
            val clickableSpan: ClickableSpan = object : ClickableSpan() {
                override fun onClick(textView: View) {
                    Log.i(TAG, " clicked:::  ${checkIn.id}")
                    var baseFragment: BaseFragment = RestaurantDetailFragment();
                    Navigation.setFragmentData(baseFragment, "_id", checkIn.id)
                    Navigation.addFragment(myContext, baseFragment, RestaurantDetailFragment.TAG, R.id.homeFram, true, false)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }

            spannable.setSpan(clickableSpan, checkedText.indexOf("at")
                .plus(2), checkedText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(myContext, R.color.primaryDark)), checkedText.indexOf("at")
                .plus(2), checkedText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), checkedText.indexOf("at")
                .plus(2), checkedText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
            if(checkIn.timings?.isNotEmpty() == true) {
                holder.timing.visibility = View.VISIBLE
                holder.timing.text = "Open: ".plus(checkIn.timings?.get(0)?.opentime).plus(" - ")
                    .plus(checkIn.timings?.get(0)?.closetime)
            } else {
                holder.timing.visibility = View.GONE
            }
        }


    }

    fun addCheckinListener(holder: CviewHolder, post: FetchPostResponseItem?) {
        //holder.postImage.setOnClickListener{ openPostDetail(post) }
//        holder.postImage.setOnClickListener(DoubleClick(object : DoubleClickListener {
//            override fun onDoubleClickEvent(view: View?) {
////                likeDislikePost(holder, post)
//            }
//
//            override fun onSingleClickEvent(view: View?) {
//                openPostDetail(post)
//            }
//        }))
        holder.postImage.onClick({
            openPostDetail(post)
        })
        holder.option.setOnClickListener {
            Log.i(TAG, " showMyPostOptionFragment:: ")
            selectedPost = post
            if(post?.user_id?.equals(MyApplication.SID) == true) {
                //showMyPostOptionFragment()

//                showOption(myPostOptions)
                showMine(post?.comments_enabled)
            } else {
                //showOtherPostOptionFragment()

//                showOption(otherPostOptions)
                showOther()
            }
        }
        holder.postImage.setOnLongClickListener {
            Log.i(TAG, " showMyPostOptionFragment:: ")
            selectedPost = post
            if(post?.user_id?.equals(MyApplication.SID) == true) {
                //showMyPostOptionFragment()

//                showOption(myPostOptions)
                showMine(post?.comments_enabled)
            } else {
                //showOtherPostOptionFragment()

//                showOption(otherPostOptions)
                showOther()
            }
            true
        }

    }

    fun addRecyclerListener(holder: RecyclerView.ViewHolder, post: FetchPostResponseItem?) {
        var holder = holder as RviewHolder

        holder.option.setOnClickListener {
            Log.i(TAG, " showMyPostOptionFragment:: ")
            selectedPost = post
            if(post?.user_id?.equals(MyApplication.SID) == true) {
                //showMyPostOptionFragment()

//                showOption(myPostOptions)
                showMine(post?.comments_enabled)
            } else {
                //showOtherPostOptionFragment()

//                showOption(otherPostOptions)
                showOther()
            }
        }
        holder.postImage.setOnLongClickListener {
            Log.i(TAG, " showMyPostOptionFragment:: ")
            selectedPost = post
            if(post?.user_id?.equals(MyApplication.SID) == true) {
                //showMyPostOptionFragment()

//                showOption(myPostOptions)
                showMine(post?.comments_enabled)
            } else {
                //showOtherPostOptionFragment()
//                showOption(otherPostOptions)
                showOther()
            }
            true
        }

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
    fun addCommonLitener(holder: CommonOption, post: FetchPostResponseItem?) {
        var myId = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid
        Log.d(TAG, "addCommonLitener: terminate 0::${post?.boosted}")
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
        if(post?.user_meta?.sid.equals(myId)&&post?.type!=Constant.Post.CHECK_IN.type){
            holder.tvInsight.visibility=View.VISIBLE
            holder.tvInsight.onClick({
                MyApplication.putTrackMP(AcPostInsight, JSONObject(mapOf("postId" to post?._id)))
                openInsight(post)
            })
        }else{
            holder.tvInsight.visibility=View.GONE
        }
        holder.like.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcPostlikeButton, JSONObject(mapOf("postId" to post?._id)))
            likeDislikePost(holder, post)
        }
        if(post?.liked_by_user == true) {
            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_filled, 0, 0, 0)
            holder.isPostLiked = true
        } else {
            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_small_heart, 0, 0, 0)
            holder.isPostLiked = false
        }
        holder.likeCount.onClick({
            MyApplication.putTrackMP(AcPostLikeCount, JSONObject(mapOf("postId" to post?._id)))
            openLikerpage(post?._id)
        })
        holder.likeCount.text = post?.stats?.likes.toString().plus(" likes")
        holder.commentCount.onClick({
            MyApplication.putTrackMP(AcPostCmtCount, JSONObject(mapOf("postId" to post?._id)))
            openPostDetail(post)
        })
        holder.commentCount.text = post?.stats?.comments.toString().plus(" Comments")
        holder.comment.onClick({
            MyApplication.putTrackMP(AcPostCmtToDetail, JSONObject(mapOf("postId" to post?._id)))
            openPostDetail(post)
        })

    }


    private fun likeDislikePost(holder: CommonOption, post: FetchPostResponseItem?) {
        if(!holder.isPostLiked) {
            holder.isPostLiked = true
            if(post?.liked_by_user == true) {
                holder.likeCount.text = (post?.stats?.likes).toString().plus(" likes")
            } else {
                holder.likeCount.text = (post?.stats?.likes?.plus(1)).toString().plus(" likes")

            }
            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_filled, 0, 0, 0)
            holder.animLike.apply {
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
            holder.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_small_heart, 0, 0, 0)
            if(post?.liked_by_user == true) {
                holder.likeCount.text = (post.stats?.likes!! - 1).toString().plus(" likes")
            } else {
                holder.likeCount.text = (post?.stats?.likes).toString().plus(" likes")
            }
            holder.isPostLiked = false
        }
        likeListener(post?._id)
    }


    fun openPostDetail(post: FetchPostResponseItem?) {
/*
        Log.i(TAG," OpeningPostDetail ")
        var detailPostParcel = Utils.getDetailPostParcel(post)
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

        var baseFragment: BaseFragment = DetailPostFragment()
        Navigation.setFragmentData(baseFragment, "post_id", post?._id)
        Navigation.addFragment(myContext, baseFragment, Constant.TAG_DETAIL_POST_FRAGMENT, R.id.homeFram, true, false)

    }

    fun openLikerpage(postId: String?) {
        var baseFragment: BaseFragment = LikeFragment()
        Navigation.setFragmentData(baseFragment, LikeFragment.POST_ID, postId)
        Navigation.addFragment(myContext, baseFragment, TAG_LIKE_FRAGMENT, R.id.homeFram, true, false)
    }

    /*fun openProfile(post: FetchPostResponseItem?, myProfile: ProfileGetResponse?) {
        if(post?.user_meta?.sid == myProfile?.cust_data?.sid) {
            Navigation.addFragment(myContext, ProfileFragment(), TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false)
        } else {
            var baseFragment: BaseFragment = OtherProfileFragment.getInstance(post?.user_meta?.sid)
            //baseFragment = Navigation.setFragmentData(baseFragment, OtherProfileFragment.OTHER_USER_ID, post?.user_meta?.sid)
            Navigation.addFragment(myContext, baseFragment, Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false)
        }
    }*/

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), CommonOption {

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
        override var tvInsight: TextView=itemView.findViewById<TextView>(R.id.tvInsight).apply{
            visibility=View.GONE
        }
        override var tvBoostTime: TextView=itemView.findViewById<TextView>(R.id.tvBoostTime).apply{
            visibility=View.GONE
        }
        var shareCount = itemView.findViewById<TextView>(R.id.tv_shares)
        var memeCount = itemView.findViewById<TextView>(R.id.tv_memes)
        var imageCout = itemView.findViewById<TextView>(R.id.tv_imageCount)
        var multiImageIcon = itemView.findViewById<ImageView>(R.id.iv_imageCount)

        override var animLike = itemView.findViewById<ImageView>(R.id.anim_heart)

        var caption = itemView.findViewById<TextView>(R.id.tv_caption)
        override var comment = itemView.findViewById<TextView>(R.id.tv_comment).also {
            it.text = "Comment"
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

    inner class HviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var thinking = itemView.findViewById<RelativeLayout>(R.id.rv_thinking).apply {
            MyApplication.putTrackMP(AcCreatePostFrHome,null)
            setOnClickListener {
                Navigation.addFragment(myContext, CreatePost(), TAG_CREATE_POST_FRAGMENT, R.id.homeFram, stack = true, needAnimation = false)
            }
        }

        var currentFeed = itemView.findViewById<TextView>(R.id.tv_current_feed).apply {
            if(type == GLOBAL_TIMELINE) {
                text = "You are viewing Global feeds"
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_globle_feed, 0, 0, 0)
            } else {
                text = "You are viewing your Timeline feeds"
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_timeline, 0, 0, 0)
            }
            setOnClickListener {
                MyApplication.putTrackMP(AcFeedChange,null)
                timeLineFragment.showBottomSheetDialogFragment(chooseTimelineFeedSheeet)
            }
        }

        //var suggestionAdapter = SuggesionAdapter(context,suggestionResponse)
        var suggestionAdapter = SuggesionAdapter(myContext, suggestionResponse) { id, isFollow ->
            followUnfollw(id, isFollow)
        }
        var rvSuggestion = itemView.findViewById<RecyclerView>(R.id.rv_suggestion).apply {
            Log.i(TAG, "  bindingdone::: ")
            adapter = suggestionAdapter
            if(PreferencesManager.getBoolean(Constant.suggested_pref)) {
                visibility = View.VISIBLE
                itemView.findViewById<ImageView>(R.id.hideSuggested).animate().rotation(180f).start()
            } else {
                visibility = View.GONE
                itemView.findViewById<ImageView>(R.id.hideSuggested).animate().rotation(0f).start()
            }
        }
        val hideSuggested = itemView.findViewById<ImageView>(R.id.hideSuggested).apply {

            setOnClickListener {
                if(rvSuggestion.visibility == View.GONE) {
                    rvSuggestion.visibility = View.VISIBLE
                    PreferencesManager.putBoolean(true, Constant.suggested_pref)
                    itemView.findViewById<ImageView>(R.id.hideSuggested).animate().rotation(180f).start()
                } else {
                    rvSuggestion.visibility = View.GONE
                    PreferencesManager.putBoolean(false, Constant.suggested_pref)
                    itemView.findViewById<ImageView>(R.id.hideSuggested).animate().rotation(0f).start()
                }
            }
        }

        var seeAll = itemView.findViewById<TextView>(R.id.seeAll).apply {
            setOnClickListener {
                MyApplication.putTrackMP(AcSeeAllSuggestFrHome,null)
                var baseFragment: BaseFragment = SuggestionFragment()
                Navigation.addFragment(myContext, baseFragment, "SuggestionFragment", R.id.homeFram, true, false)
            }
        }

        /* var noDataScreen = itemView.findViewById<RelativeLayout>(R.id.rl_nodata).apply {
             try {
                 //Log.i(TAG," getItemCountNoadata::: ${getItem(1)}")
                 if(getItem(1) != null)
                     visibility = View.GONE
             }catch (e: IndexOutOfBoundsException){
                 visibility = View.VISIBLE
             }
         }*/
    }

    fun getPostIdFromIndex(position:Int): FetchPostResponseItem? {
        return try {
            getItem(position)
        }catch (e:Exception){
            Log.e(TAG," getting Post id in fail")
            e.printStackTrace()
            null
        }

    }

    fun showCustomPlaceAlert(latLng: LatLng) {
        var fragment = customPlaceAlert
        customPlaceAlert?.setLatLng(latLng)
        if(fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(timeLineFragment.childFragmentManager, fragment.tag)
    }

    class CviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), CommonOption {

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
        override var tvInsight: TextView=itemView.findViewById<TextView>(R.id.tvInsight).apply{
            visibility=View.GONE
        }
        override var tvBoostTime: TextView=itemView.findViewById<TextView>(R.id.tvBoostTime).apply{
            visibility=View.GONE
        }
        var shareCount = itemView.findViewById<TextView>(R.id.tv_shares)
        var imageCout = itemView.findViewById<TextView>(R.id.tv_imageCount)
        override var animLike = itemView.findViewById<ImageView>(R.id.anim_heart)

        var caption = itemView.findViewById<TextView>(R.id.tv_caption)
        override var comment = itemView.findViewById<TextView>(R.id.tv_comment).also {
            it.text = "Comment"
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

    class TextPostHolder(itemView: View) : RecyclerView.ViewHolder(itemView), CommonOption {

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
        override var tvInsight: TextView=itemView.findViewById<TextView>(R.id.tvInsight).apply{
            visibility=View.GONE
        }
        override var tvBoostTime: TextView=itemView.findViewById<TextView>(R.id.tvBoostTime).apply{
            visibility=View.GONE
        }
        var shareCount = itemView.findViewById<TextView>(R.id.tv_shares)
        override var animLike = itemView.findViewById<ImageView>(R.id.anim_heart)

        var caption = itemView.findViewById<TextView>(R.id.tv_caption)
        //var textPostBg = itemView.findViewById<View>(R.id.textPostBg)
        //var card = itemView.findViewById<CardView>(R.id.card)
        override var comment = itemView.findViewById<TextView>(R.id.tv_comment).also {
            it.text = "Comment"
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

    class OpenMeetHolder(itemView: View, var myContext: Activity, viewPool: RecyclerView.RecycledViewPool) : RecyclerView.ViewHolder(itemView), CommonOption {

        var dp = itemView.findViewById<ImageView>(R.id.civ_dp)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var lastseenandLoc = itemView.findViewById<TextView>(R.id.tv_loc_last_seen)
        var option = itemView.findViewById<TextView>(R.id.tv_options)
        var isVerified = itemView.findViewById<ImageView>(R.id.is_verified)
        var view = itemView.findViewById<TextView>(R.id.view).apply {
            setRoundedColorBackground(myContext, R.color.transparent, enbleDash = true, strokeColor = R.color.primaryDark, Dashgap = 0f, stripSize = 10f, strokeHeight = 1f)
        }
        override var likeCount = itemView.findViewById<TextView>(R.id.tv_likes)
        override var commentCount = itemView.findViewById<TextView>(R.id.tv_comments)
        override var tvInsight: TextView=itemView.findViewById<TextView>(R.id.tvInsight).apply{
            visibility=View.GONE
        }
        override var tvBoostTime: TextView=itemView.findViewById<TextView>(R.id.tvBoostTime).apply{
            visibility=View.GONE
        }
        override var animLike = itemView.findViewById<ImageView>(R.id.anim_heart)
        override var isPostLiked = false

        //var caption = itemView.findViewById<TextView>(R.id.tv_caption).apply{ text = ""}
        override var comment = itemView.findViewById<TextView>(R.id.tv_comment).also {
            it.text = "Comment"
        }
        override var like = itemView.findViewById<TextView>(R.id.tv_like).also {
            it.text = "Like"
        }
        var meetName = itemView.findViewById<TextView>(R.id.meetName)
        var address = itemView.findViewById<TextView>(R.id.address)
        var desc = itemView.findViewById<TextView>(R.id.desc)
        var monthOfYear = itemView.findViewById<TextView>(R.id.monthOfYear).apply {
            setGradient(myContext, GradientDrawable.Orientation.TL_BR, intArrayOf(ContextCompat.getColor(myContext, R.color.primaryDark), ContextCompat.getColor(myContext, R.color.gred_red), ContextCompat.getColor(myContext, R.color.gred_red)))
        }
        var dayOfMonth = itemView.findViewById<TextView>(R.id.dayOfMonth)
        var dayOfWeak = itemView.findViewById<TextView>(R.id.dayOfWeak)
        var timeOfDay = itemView.findViewById<TextView>(R.id.timeOfDay)
        var countInterested = itemView.findViewById<TextView>(R.id.countInterested)
        var join = itemView.findViewById<TextView>(R.id.join).apply {
            setRoundedColorBackground(myContext, R.color.primaryDark)
        }

        var parentCard = itemView.findViewById<LinearLayout>(R.id.parentCard).apply {
            setRoundedColorBackground(myContext, enbleDash = true, stripSize = 20f, Dashgap = 0f, strokeColor = R.color.gray1, corner = 20f, strokeHeight = 2f)
        }

        var rlIntract = itemView.findViewById<LinearLayout>(R.id.rl_intract).apply {
            setRoundedColorBackground(myContext, enbleDash = true, stripSize = 20f, Dashgap = 0f, strokeColor = R.color.gray1, corner = 20f, strokeHeight = 2f)
        }
        var rvIntrested = itemView.findViewById<RecyclerView>(R.id.rvIntrested).apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false)
            setRecycledViewPool(viewPool)
        }

        var llRequest = itemView.findViewById<LinearLayout>(R.id.llRequest)
        var llNoRequest = itemView.findViewById<LinearLayout>(R.id.llNoRequest)

        var tvFirst = itemView.findViewById<TextView>(R.id.tvFirst)

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


    fun showOption(sheet: BottomSheetDialogFragment?) {
        // var fragment = sheet as BottomSheetDialogFragment?
        if(sheet == null || sheet.isAdded) {
            return
        }
        sheet.show(this.timeLineFragment.childFragmentManager, sheet.tag)
    }

    fun showMine(alreadyEnabled: Boolean?) {
        Log.d(TAG, "showMine: disabled: ${selectedPost?.comments_enabled}, ${selectedPost?._id}, ${selectedPost?.type}")
        var cmtEnable="Enable Comment"
        if(selectedPost?.comments_enabled == true) {
            cmtEnable  ="Disable Comment"
        }
        //var myPostArray = arrayListOf("Edit", cmtEnable, "Delete")
        var invisibleOption = arrayListOf<Int>()
        if(selectedPost?.comments_enabled == true) invisibleOption.add(1) else invisibleOption.add(2)
         when(selectedPost?.type){ 
            Constant.Post.DEFAULT.type -> {}
             Constant.Post.OPEN_MEET.type -> { invisibleOption.add(0) ; invisibleOption.add(3)}
             Constant.Post.TEXT_POST.type -> { invisibleOption.add(0) }
             Constant.Post.CHECK_IN.type -> { invisibleOption.add(0) }
        }
        myPostSheet.hideOption(invisibleOption.toTypedArray())
        showOption(myPostSheet)

        /*val isCheckIn = selectedPost?.type != Constant.Post.DEFAULT.type
        Log.d(TAG, "showMine: isCheckIn $isCheckIn")
        if(isCheckIn){
            myPostArray = arrayListOf(cmtEnable, "Delete")
        }*/
        /*myPostSheet = FilterBottomSheet(myContext, myPostArray) {
            when(it) {
                0 -> {//edit or comment
                    if (!isCheckIn){
                        var baseFragment: BaseFragment = EditPostFragment()
                        Navigation.setFragmentData(baseFragment, "postId", selectedPost?._id)
                        Navigation.addFragment(myContext, baseFragment, Constant.TAG_EDIT_POST_FRAGMENT, R.id.homeFram, true, false)
                    }else{
                        val enabledDisabled = alreadyEnabled != true
                        Log.d(TAG, "showMine:comment2 $enabledDisabled")
                        postModel.disableComment(selectedPost?._id, enabledDisabled)
                    }
                }

                1 -> {//disable or enable comment or delete
                    if (!isCheckIn){
                        Log.d(TAG, "showMine:comment1 ${selectedPost?.comments_enabled}")
                        val enabledDisabled = alreadyEnabled != true
                        Log.d(TAG, "showMine:comment2 $enabledDisabled")
                        postModel.disableComment(selectedPost?._id, enabledDisabled)
                    }else{
                        deletePost(selectedPost?._id)
                    }
                }

                2 -> {//delete
                    deletePost(selectedPost?._id)
                }
            }
        }*/
       // myPostSheet?.show()
    }

    fun showOther() {
        otherPostOptions?.let{
            if(it.isAdded) {
                return
            }
            it.show(this.timeLineFragment.childFragmentManager, it.tag)
        }
    }

    override fun bottomSheetClickedOption(optionSelected: Int) {
        Log.i(TAG, " optionSelected ${optionSelected}")
        if(optionSelected != OPEN_MEET)
            timeLineFragment.changeTimeLine(optionSelected)
        else{
            Navigation.addFragment(myContext,OpenMeetListFragment(),OpenMeetListFragment.TAG,R.id.homeFram,true,false)
        }
    }


    companion object {

        private val COMPARATOR = object : DiffUtil.ItemCallback<FetchPostResponseItem>() {
            override fun areItemsTheSame(oldItem: FetchPostResponseItem, newItem: FetchPostResponseItem): Boolean {
                return (oldItem._id == newItem._id)
            }

            override fun areContentsTheSame(oldItem: FetchPostResponseItem, newItem: FetchPostResponseItem): Boolean {
                return oldItem == newItem
            }

        }
    }


}