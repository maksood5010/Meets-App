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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.post.Comment
import com.meetsportal.meets.ui.bottomsheet.ReportSheet
import com.meetsportal.meets.ui.fragments.socialfragment.DetailPostFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
//import com.shisheo.shisheo.networking.post.Comment
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.px
import com.meetsportal.meets.viewmodels.PostViewModel
import de.hdodenhof.circleimageview.CircleImageView

class CommentRepliesAdapter (var myContext: Activity, var isMyPost : Boolean, var fragment : DetailPostFragment, var postViewModel:PostViewModel, var  list: ArrayList<Comment>?, var parentPosition : Int?, var likelistener :(String) -> Unit, var deleteComment : (Comment?, Int?)->Unit):
    RecyclerView.Adapter<CommentRepliesAdapter.RviewHolder>() {

    private val TAG = CommentRepliesAdapter::class.java.simpleName
    //var list:ArrayList<CommentX>? = ArrayList()
    var isUserliked = false

    var myCommentOptions: BottomSheetOptions? = null
    var otherCommentOptions: BottomSheetOptions? = null

    var selectedComment : Comment? = null
    var selectedPosition : Int? = null

    /*init {

        this.list?.addAll(list!!)
    }*/

    init {
        myCommentOptions = BottomSheetOptions.getInstance("Delete", null, null, null,1)
        otherCommentOptions = BottomSheetOptions.getInstance("Report", null, null, null,1)
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
                        //notifyItemChanged(selectedPosition!!)
                        deleteComment(selectedComment,parentPosition)
                    }
                    BottomSheetOptions.CANCEL ->{
                        notifyItemChanged(selectedPosition!!)
                    }
                }
            }
        })
    }

    /*fun showMyCommentOptionFragment() {
        var fragment = myCommentOptions as BottomSheetDialogFragment?
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(this.fragment.childFragmentManager, fragment.tag)
    }*/

    fun showOption(sheet: BottomSheetDialogFragment?){
        if (sheet == null || sheet.isAdded) {
            return
        }
        sheet.show(this.fragment.childFragmentManager, sheet.tag)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_comments, parent, false)
    )

    override fun getItemCount(): Int {
        if(list != null) {
            Log.i("TAG","  countListNewsAdapter:: ${list?.size} ")
            return list?.size!!
        }
        else
            return 0
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        initiaLize(holder,position,list!![position])

    }

    fun updatelist(list: List<Comment>?){
        this.list?.clear()
        this.list?.addAll(list!!)
        this.notifyDataSetChanged()
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dp = itemView.findViewById<CircleImageView>(R.id.civ_image)
        var name = itemView.findViewById<TextView>(R.id.tv_username)
        var likeCount = itemView.findViewById<TextView>(R.id.tv_likecount)
        var viewReply = itemView.findViewById<TextView>(R.id.view_reply)
        var comment = itemView.findViewById<TextView>(R.id.tv_usercomment)
        var rvReplies = itemView.findViewById<RecyclerView>(R.id.rv_replies)
        var cretedAt = itemView.findViewById<TextView>(R.id.tv_created_at)
        var lLlike = itemView.findViewById<LinearLayout>(R.id.ll_like)
        var ivLike = itemView.findViewById<ImageView>(R.id.ic_heart)
        var verify = itemView.findViewById<ImageView>(R.id.iv_verify)
        var ll_comment = itemView.findViewById<LinearLayout>(R.id.ll_reply).apply {
            visibility = View.GONE
        }
        var tv_reply = itemView.findViewById<TextView>(R.id.tv_reply).apply {
            visibility = View.GONE
        }
        var commentCount = itemView.findViewById<TextView>(R.id.tv_reply)
        var card = itemView.findViewById<CardView>(R.id.cv_comment_card)


    }

    fun initiaLize(holder: RviewHolder, position: Int, comment: Comment){
        var a = holder.dp.layoutParams
        a.height = 40.px
        a.width = 40.px
        holder.dp.layoutParams = a
        Glide.with(myContext).load(comment.user.profile_image_url)
            .error(R.drawable.ic_person_placeholder)
            .placeholder(R.drawable.ic_default_person)
            .into(holder.dp)
        //holder.name.text = comment.user.first_name.plus(" ").plus(comment.user.last_name)
        holder.name.text = comment.user.username
        holder.likeCount.text = comment.likes_count.toString()
        holder.cretedAt.text = Utils.getCreatedAt(comment.createdAt)
        holder.viewReply.visibility = View.GONE
        holder.comment.text = comment.body
        holder.rvReplies.visibility = View.GONE
        if(comment.liked_by_user)
            holder.ivLike.setImageDrawable(
                ContextCompat.getDrawable(
                    myContext,
                    R.drawable.ic_heart
                )
            )else{
            holder.ivLike.setImageDrawable(
                ContextCompat.getDrawable(
                    myContext,
                    R.drawable.ic_small_heart
                ))
        }
        if(comment.user.verified_user == true){
            holder.verify.visibility = View.VISIBLE
        }else{
            holder.verify.visibility = View.GONE
        }
        holder.lLlike.setOnClickListener {
            likelistener(comment._id)
            togglelike(holder,comment)
        }
        isUserliked = comment.liked_by_user
        holder.card.setCardBackgroundColor(ContextCompat.getColor(myContext,R.color.page_bg))
        holder.card.setOnLongClickListener {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(myContext,R.color.bg_comment))
            selectedComment = comment
            selectedPosition = position
            //showMyCommentOptionFragment()
            //showOption(myCommentOptions)
            if(isMyPost)
                showOption(myCommentOptions)
            else{
                if(comment.user_id == MyApplication.SID)
                    showOption(myCommentOptions)
                else
                    showOption(otherCommentOptions)
            }

            false
            //deletecomment(comment,position)

        }
        //holder.commentCount.text = comment.commentCount.toString()

    }

    private fun togglelike(holder: RviewHolder, comment: Comment) {
        if(comment.liked_by_user){
            if(isUserliked) {
                holder.likeCount.text = (comment.likes_count - 1).toString()
                holder.ivLike.setImageDrawable(
                    ContextCompat.getDrawable(
                        myContext,
                        R.drawable.ic_small_heart
                    )
                )
                isUserliked = false
            }else{
                holder.likeCount.text = (comment.likes_count).toString()
                holder.ivLike.setImageDrawable(
                    ContextCompat.getDrawable(
                        myContext,
                        R.drawable.ic_heart
                    )
                )
                isUserliked = true
            }
        }
        else{
            if(isUserliked) {
                holder.likeCount.text = (comment.likes_count).toString()
                holder.ivLike.setImageDrawable(
                    ContextCompat.getDrawable(
                        myContext,
                        R.drawable.ic_small_heart
                    )
                )
                isUserliked = false
            }else{
                holder.likeCount.text = (comment.likes_count + 1).toString()
                holder.ivLike.setImageDrawable(
                    ContextCompat.getDrawable(
                        myContext,
                        R.drawable.ic_heart
                    )
                )
                isUserliked = true
            }
        }
    }



}