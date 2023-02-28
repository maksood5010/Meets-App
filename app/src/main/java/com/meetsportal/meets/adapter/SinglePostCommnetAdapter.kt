package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.post.Comment
import com.meetsportal.meets.networking.post.SinglePostCommentsItem
import com.meetsportal.meets.ui.bottomsheet.ReportSheet
import com.meetsportal.meets.ui.fragments.socialfragment.DetailPostFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.PostViewModel
import de.hdodenhof.circleimageview.CircleImageView

class SinglePostCommnetAdapter(
    var myContext: Activity,
    var fragment : Fragment,
    var postViewModel : PostViewModel,
    var isItReply : Boolean,
    var likelistener: (String?) -> Unit,
    var porfileTapListener:(String?)->Unit,
    var replylistener : (SinglePostCommentsItem?,Int) -> Unit,
    var deletecomment:(SinglePostCommentsItem?,Int?,String?)->Unit,
    val viewReply:(SinglePostCommentsItem?,Int)->Unit
): PagingDataAdapter<SinglePostCommentsItem, SinglePostCommnetAdapter.RviewHolder>(COMPARATOR) {

    val TAG = SinglePostCommnetAdapter::class.simpleName

    var selectedComment : SinglePostCommentsItem? = null
    var selectedPosition : Int? = null

    var myCommentOptions: BottomSheetOptions? = null
    var otherCommentOptions: BottomSheetOptions? = null

    init {
        myCommentOptions = BottomSheetOptions.getInstance("Delete", null, null, null,null)
        otherCommentOptions = BottomSheetOptions.getInstance("Report", null, null, null,null)
        otherCommentOptions?.setBottomSheetLitener(object : BottomSheetOptions.BottomSheetListener{
            override fun bottomSheetClickedOption(buttonClicked: String) {
                when (buttonClicked) {
                    BottomSheetOptions.BUTTON1 -> {
                        /*Log.i(TAG," deleted:: ")
                        notifyItemChanged(selectedPosition!!)
                        deletecomment(selectedComment,selectedPosition,TYPE_COMMENT)*/
                        showOption(ReportSheet(postViewModel,myContext,selectedComment?._id,"comment"))
                        notifyItemChanged(selectedPosition!!)
                    }
                    BottomSheetOptions.CANCEL ->{
                        notifyItemChanged(selectedPosition!!)
                    }
                }
            }
        })
        myCommentOptions?.setBottomSheetLitener(object : BottomSheetOptions.BottomSheetListener{
            override fun bottomSheetClickedOption(buttonClicked: String) {
                when (buttonClicked) {
                    BottomSheetOptions.BUTTON1 -> {
                        Log.i(TAG," deleted:: ")
                        notifyItemChanged(selectedPosition!!)
                        deletecomment(selectedComment,selectedPosition, DetailPostFragment.TYPE_COMMENT)
                    }
                    BottomSheetOptions.CANCEL ->{
                        notifyItemChanged(selectedPosition!!)
                    }
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_comments, parent, false))

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        Log.i(TAG," onBinding::: ")
        val data = getItem(position)
        initiaLize(holder, position, data)
        addListener(holder, data,position)
        //addListener(holder, data)
    }

    fun initiaLize(holder: RviewHolder, position: Int, data: SinglePostCommentsItem?){
//        Glide.with(myContext).load(comment?.user?.profile_image_url)
//            .error(R.drawable.restro_img)
//            .placeholder(R.drawable.restro_img)
//            .into(holder.dp)
        Log.i(TAG, "initiaLize: checking 1 - ${data?.user_meta?.profile_image_url}")
        Log.i(TAG, "initiaLize: checking 2 - ${data?.user_meta?.username}")
        holder.dp.loadImage(myContext,data?.user_meta?.profile_image_url,R.drawable.ic_default_person,false)
        holder.ivBadge.setImageResource(Utils.getBadge(data?.user_meta?.badge).foreground)
        //holder.name.text = comment?.user?.first_name.plus(" ").plus( comment?.user?.last_name)
        holder.name.text = data?.user_meta?.username
        holder.likeCount.text = data?.stats?.likes.toString()
        Log.i("TAG"," commentLikeByUser:: ${data?.liked_by_user}")
      //  isUserliked = data?.liked_by_user
        if(data?.liked_by_user == true)
            holder.ivLike.setImageDrawable(
                ContextCompat.getDrawable(myContext, R.drawable.ic_heart)
            )
        else{
            holder.ivLike.setImageDrawable(ContextCompat.getDrawable(myContext, R.drawable.ic_small_heart))
        }
        if(data?.stats?.comments?.compareTo(0) == 1){
            holder.viewReply.visibility = View.VISIBLE
            holder.viewReply.text = "view ${data?.stats?.comments} more replies"
        }else{
            holder.viewReply.visibility = View.GONE
        }

        /*if(comment?.comments?.size?.compareTo(0) == 1){
            holder.viewReply.visibility = View.VISIBLE
        }else{
            holder.viewReply.visibility = View.GONE
        }*/
        holder.comment.text = data?.body
//        var layoutManager = LinearLayoutManager(holder.rvReplies.context,
//            LinearLayoutManager.VERTICAL,false)
        holder.cretedAt.text = Utils.getCreatedAt(data?.createdAt)
       // layoutManager.initialPrefetchItemCount = data?.stats..comments?.size!!
       // holder.rvReplies.layoutManager = layoutManager
        holder.lLlike.setOnClickListener {
            likelistener(data?._id)
            //togglelike(holder,data,position)
        }


        /*holder.repliesArray.size.let {
            holder.repliesArray.clear()

            if(comment.comments.size > it ){
                Log.i(TAG," repliesCount:: ${comment.comments.size}")
                holder.repliesArray.addAll(comment.comments.subList(0,it))
            }

        }*/
        if(data?.user_meta?.verified_user == true){
            holder.verify.visibility = View.VISIBLE
        }else{
            holder.verify.visibility = View.GONE
        }
        /*if(comment.makeExpanded == true && comment.repliesId != null){
            holder.repliesArray.clear()

            var replyPosition = comment.comments?.indexOfFirst { it._id.equals(comment.repliesId) }

            holder.repliesArray.addAll(comment.comments.subList(0,replyPosition+1))
            holder.rvReplies.visibility =View.VISIBLE
        }else{
            holder.rvReplies.visibility =View.GONE
        }*/

        /*holder.rvReplies.adapter = CommentRepliesAdapter(
            myContext,
            isMyPost,
            fragment,
            postViewModel,
            holder.repliesArray,position,{it->
                likelistener(it)
            },{ comment , position ->
                MyApplication.smallVibrate()
                deletecomment(comment,position, DetailPostFragment.TYPE_REPLY_COMMENT)
                //postViewModel.deleteComment(comment?._id)
            })*/
        /*if(comment.makeExpanded == true && comment.repliesId != null){

            var replyPosition = comment.comments?.indexOfFirst { it._id.equals(comment.repliesId) }
            Log.i(TAG,"SmoothScrolltoReplies::: $replyPosition")
            if(replyPosition.equals(-1)){
                Toast.makeText(myContext,"Comment is deleted...", Toast.LENGTH_LONG).show()
                myContext.onBackPressed()
                return
            }

            holder.rvReplies.smoothScrollToPosition(replyPosition)
            changeViewreplyText(comment,holder)
        }*/
        //holder.rvReplies.setRecycledViewPool(viewPool)

        Utils.onClick(holder.reply,1000){replylistener(data,position)}
        Utils.onClick(holder.dp,1000){porfileTapListener(data?.user_id)}
        Utils.onClick(holder.name,1000){porfileTapListener(data?.user_id)}
        holder.card.setCardBackgroundColor(ContextCompat.getColor(myContext,R.color.page_bg))
        holder.card.setOnLongClickListener {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(myContext,R.color.bg_comment))
            selectedComment = data
            selectedPosition = position
            if(data?.user_id == MyApplication.SID)
                showOption(myCommentOptions)
            else
                showOption(otherCommentOptions)
//            if(isMyPost)
//                showOption(myCommentOptions)
//            else{
//                if(comment.user_id == MyApplication.SID)
//                    showOption(myCommentOptions)
//                else
//                    showOption(otherCommentOptions)
//            }
            false
            //deletecomment(comment,position)
        }

        //holder.reply.setOnClickListener { replylistener(data,position) }
        //holder.dp.setOnClickListener { porfileTapListener(comment?.user_id) }
        //holder.name.setOnClickListener { porfileTapListener(comment?.user_id) }
    }

    /*private fun togglelike(holder: RviewHolder, data: SinglePostCommentsItem?, position: Int) {
        if(data?.liked_by_user== true){
            data?.liked_by_user = false
            list?.get(position)?.likes_count = list?.get(position)?.likes_count?.minus(1)!!

            *//*if(isUserliked == true) {
                *//**//*holder.likeCount.text = (comment.likes_count - 1).toString()
                holder.ivLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_small_heart))*//**//*
                isUserliked = false
                list?.get(position)?.liked_by_user = false
                list?.get(position)?.likes_count = list?.get(position)?.likes_count?.minus(1)!!
            }else{
                *//**//*holder.likeCount.text = (comment.likes_count).toString()
                holder.ivLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_heart))*//**//*
                isUserliked = true
                list?.get(position)?.liked_by_user = true
            }*//*
        }
        else{
            list?.get(position)?.liked_by_user = true
            list?.get(position)?.likes_count = list?.get(position)?.likes_count?.plus(1)!!
            *//*if(isUserliked == true) {
                holder.likeCount.text = (comment?.likes_count).toString()
                holder.ivLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_small_heart))
                isUserliked = false
            }else{
                holder.likeCount.text = (comment?.likes_count?.plus(1)).toString()
                holder.ivLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_heart))
                isUserliked = true
            }*//*
        }
        notifyItemChanged(position)
    }*/

    fun addListener(holder: RviewHolder, data: SinglePostCommentsItem?, position: Int) {
        holder.viewReply.setOnClickListener {
            //addCommentInReplies(holder,data)
            viewReply(data,position)
        }
    }



    fun showOption(sheet: BottomSheetDialogFragment?){
        try {
            if (sheet == null || sheet.isAdded) {
                return
            }
            sheet.show(this.fragment.childFragmentManager, sheet.tag)
        }catch (e:Exception){
            Log.e(TAG, "showOption: ${e.message}", )
        }
    }




    inner class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dp = itemView.findViewById<CircleImageView>(R.id.civ_image)
        var ivBadge = itemView.findViewById<ImageView>(R.id.ivBadge)
        var name = itemView.findViewById<TextView>(R.id.tv_username)
        var likeCount = itemView.findViewById<TextView>(R.id.tv_likecount)
        var viewReply = itemView.findViewById<TextView>(R.id.view_reply).apply {
            if(isItReply)
                visibility = View.GONE
        }
        var comment = itemView.findViewById<TextView>(R.id.tv_usercomment)
        var rvReplies = itemView.findViewById<RecyclerView>(R.id.rv_replies)
        var reply = itemView.findViewById<LinearLayout>(R.id.ll_reply).apply{
            if(isItReply)
                visibility = View.GONE
        }
        var cretedAt = itemView.findViewById<TextView>(R.id.tv_created_at)
        var lLlike = itemView.findViewById<LinearLayout>(R.id.ll_like)
        var ivLike = itemView.findViewById<ImageView>(R.id.ic_heart)
        var card = itemView.findViewById<CardView>(R.id.cv_comment_card)
        var verify = itemView.findViewById<ImageView>(R.id.iv_verify)


        var repliesArray = ArrayList<Comment>()
        var start = 0
        var end = 2
    }


    companion object{
        private val COMPARATOR = object : DiffUtil.ItemCallback<SinglePostCommentsItem>(){
            override fun areItemsTheSame(
                oldItem: SinglePostCommentsItem,
                newItem: SinglePostCommentsItem
            ): Boolean {
                return (oldItem._id == newItem._id)
            }

            override fun areContentsTheSame(
                oldItem: SinglePostCommentsItem,
                newItem: SinglePostCommentsItem
            ): Boolean {
                return oldItem._id ==  newItem._id && oldItem.stats.likes == newItem.stats.likes && oldItem.stats.comments == newItem.stats.comments
            }

        }
    }

}