package com.meetsportal.meets.adapter

//import com.shisheo.shisheo.networking.post.CommentX
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.post.Comment
import com.meetsportal.meets.ui.bottomsheet.ReportSheet
import com.meetsportal.meets.ui.fragments.socialfragment.DetailPostFragment
import com.meetsportal.meets.ui.fragments.socialfragment.DetailPostFragment.Companion.TYPE_COMMENT
import com.meetsportal.meets.ui.fragments.socialfragment.DetailPostFragment.Companion.TYPE_REPLY_COMMENT
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.viewmodels.PostViewModel
import de.hdodenhof.circleimageview.CircleImageView

class PostCommentAdapter(
    var myContext: Activity,
    var fragment : DetailPostFragment,
    list: ArrayList<Comment>?,
    var postViewModel : PostViewModel,
    var likelistener: (String?) -> Unit,
    var porfileTapListener:(String?)->Unit,
    var replylistener : (Comment?,Int) -> Unit,
    var deletecomment:(Comment?,Int?,String?)->Unit
): RecyclerView.Adapter<PostCommentAdapter.RviewHolder>() {

    var selectedComment : Comment? = null
    var selectedPosition : Int? = null

    private val TAG = PostCommentAdapter::class.java.simpleName

    var list:ArrayList<Comment>? = ArrayList()
    var viewPool = RecyclerView.RecycledViewPool()

    var isMyPost :Boolean = false

    var myCommentOptions: BottomSheetOptions? = null
    var otherCommentOptions: BottomSheetOptions? = null

    init {
        this.list?.addAll(list!!)
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
                        deletecomment(selectedComment,selectedPosition,TYPE_COMMENT)
                    }
                    BottomSheetOptions.CANCEL ->{
                        notifyItemChanged(selectedPosition!!)
                    }
                }
            }
        })
    }

    var isUserliked:Boolean? = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_comments, parent, false)
    )

    override fun getItemCount(): Int {
        if(list != null) {
            Log.i("TAG", " countListNewsAdapter:: ${list?.size} ")
            return list?.size!!
        }
        else
            return 0
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {

        initiaLize(holder, position, list!![position])
        //initiaLize(holder, position, getItem(position))
        addListener(holder, list!![position])
        //addListener(holder, getItem(position))

    }


    fun updatelist(list: List<Comment>?){
        this.list?.clear()
        this.list?.addAll(list!!)
        this.notifyDataSetChanged()
    }

    fun setPostowner(isMyPost : Boolean){
        this.isMyPost = isMyPost
    }

    fun addList(list: List<Comment>?){
        this.list?.addAll(list!!)
        this.notifyDataSetChanged()
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dp = itemView.findViewById<CircleImageView>(R.id.civ_image)
        var ivBadge = itemView.findViewById<ImageView>(R.id.ivBadge)
        var name = itemView.findViewById<TextView>(R.id.tv_username)
        var likeCount = itemView.findViewById<TextView>(R.id.tv_likecount)
        var viewReply = itemView.findViewById<TextView>(R.id.view_reply)
        var comment = itemView.findViewById<TextView>(R.id.tv_usercomment)
        var rvReplies = itemView.findViewById<RecyclerView>(R.id.rv_replies)
        var reply = itemView.findViewById<LinearLayout>(R.id.ll_reply)
        var cretedAt = itemView.findViewById<TextView>(R.id.tv_created_at)
        var lLlike = itemView.findViewById<LinearLayout>(R.id.ll_like)
        var ivLike = itemView.findViewById<ImageView>(R.id.ic_heart)
        var card = itemView.findViewById<CardView>(R.id.cv_comment_card)
        var verify = itemView.findViewById<ImageView>(R.id.iv_verify)


        var repliesArray = ArrayList<Comment>()
        var start = 0
        var end = 2
    }

    fun initiaLize(holder: RviewHolder, position: Int, comment: Comment?){
//        Glide.with(myContext).load(comment?.user?.profile_image_url)
//            .error(R.drawable.restro_img)
//            .placeholder(R.drawable.restro_img)
//            .into(holder.dp)
        Log.i(TAG, "initiaLize: checking 1 - ${comment?.user?.profile_image_url}")
        Log.i(TAG, "initiaLize: checking 2 - ${comment?.user?.username}")
        holder.dp.loadImage(myContext,comment?.user?.profile_image_url,R.drawable.ic_default_person)
        holder.ivBadge.setImageResource(Utils.getBadge(comment?.user?.badge).foreground)
        //holder.name.text = comment?.user?.first_name.plus(" ").plus( comment?.user?.last_name)
        holder.name.text = comment?.user?.username
        holder.likeCount.text = comment?.likes_count.toString()
        Log.i("TAG"," commentLikeByUser:: ${comment?.liked_by_user}")
        isUserliked = comment?.liked_by_user
        if(comment?.liked_by_user == true)
            holder.ivLike.setImageDrawable(ContextCompat.getDrawable(myContext, R.drawable.ic_heart)
        )else{
            holder.ivLike.setImageDrawable(ContextCompat.getDrawable(myContext, R.drawable.ic_small_heart))
        }
        holder.viewReply.text = "view ${comment?.comments?.count()} more replies"
        if(comment?.comments?.size?.compareTo(0) == 1){
            holder.viewReply.visibility = View.VISIBLE
        }else{
            holder.viewReply.visibility = View.GONE
        }
        holder.comment.text = comment?.body
        var layoutManager = LinearLayoutManager(holder.rvReplies.context,LinearLayoutManager.VERTICAL,false)
        holder.cretedAt.text = Utils.getCreatedAt(comment?.createdAt)
        layoutManager.initialPrefetchItemCount = comment?.comments?.size!!
        holder.rvReplies.layoutManager = layoutManager
        holder.lLlike.setOnClickListener {
            likelistener(comment?._id)
            togglelike(holder,comment,position)
        }
        holder.repliesArray.size.let {
            holder.repliesArray.clear()

                if(comment.comments.size > it ){
                    Log.i(TAG," repliesCount:: ${comment.comments.size}")
                    holder.repliesArray.addAll(comment.comments.subList(0,it))
                }

        }
        if(comment.user.verified_user == true){
            holder.verify.visibility = View.VISIBLE
        }else{
            holder.verify.visibility = View.GONE
        }
        if(comment.makeExpanded == true && comment.repliesId != null){
            holder.repliesArray.clear()

            var replyPosition = comment.comments?.indexOfFirst { it._id.equals(comment.repliesId) }

            holder.repliesArray.addAll(comment.comments.subList(0,replyPosition+1))
            holder.rvReplies.visibility =View.VISIBLE
        }else{
            holder.rvReplies.visibility =View.GONE
        }

        holder.rvReplies.adapter = CommentRepliesAdapter(
            myContext,
            isMyPost,
            fragment,
            postViewModel,
            holder.repliesArray,position,{it->
                likelistener(it)
            },{ comment , position ->
                MyApplication.smallVibrate()
                deletecomment(comment,position,TYPE_REPLY_COMMENT)
                //postViewModel.deleteComment(comment?._id)
            })
        if(comment.makeExpanded == true && comment.repliesId != null){

            var replyPosition = comment.comments?.indexOfFirst { it._id.equals(comment.repliesId) }
            Log.i(TAG,"SmoothScrolltoReplies::: $replyPosition")
            if(replyPosition.equals(-1)){
                Toast.makeText(myContext,"Comment is deleted...",Toast.LENGTH_LONG).show()
                myContext.onBackPressed()
                return
            }

            holder.rvReplies.smoothScrollToPosition(replyPosition)
            changeViewreplyText(comment,holder)
        }
        holder.rvReplies.setRecycledViewPool(viewPool)

        Utils.onClick(holder.reply,1000){replylistener(comment,position)}
        Utils.onClick(holder.dp,1000){porfileTapListener(comment?.user_id)}
        Utils.onClick(holder.name,1000){porfileTapListener(comment?.user_id)}
        holder.card.setCardBackgroundColor(ContextCompat.getColor(myContext,R.color.page_bg))
        holder.card.setOnLongClickListener {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(myContext,R.color.bg_comment))
            selectedComment = comment
            selectedPosition = position
            if(comment.user_id == MyApplication.SID)
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

        //holder.reply.setOnClickListener { replylistener(comment,position) }
        //holder.dp.setOnClickListener { porfileTapListener(comment?.user_id) }
        //holder.name.setOnClickListener { porfileTapListener(comment?.user_id) }
    }

    //fun showMyCommentOptionFragment() {
    /*fun showOption(option: BottomSheetDialogFragment) {
        //var fragment = myCommentOptions as BottomSheetDialogFragment?
        if (option == null || option.isAdded) {
            return
        }
        option.show(this.fragment.childFragmentManager, fragment.tag)
    }*/

    fun showOption(sheet: BottomSheetDialogFragment?){
        if (sheet == null || sheet.isAdded) {
            return
        }
        sheet.show(this.fragment.childFragmentManager, sheet.tag)
    }

    private fun addReplies(holder: RviewHolder, comment: Comment) {
        if(comment.start == null) comment.start = 0
        if(comment.end == null) comment.end = 2
        Log.i(TAG," addReplies::: size ${comment.comments.size} start:: ${comment.start} end:: ${comment.end}")
        if(comment.comments.size > comment.end!!)
            holder.repliesArray.addAll(comment.comments.subList(comment.start!!,comment.end!!))
        else{
            holder.repliesArray.addAll(comment.comments.subList(comment.start!!,comment.comments.size))
        }
        comment.start = comment.end
        comment.end = comment.end?.plus(2)
        holder.rvReplies.adapter?.notifyDataSetChanged()
    }

    /*fun showAllReplies(holder: RviewHolder, comment: Comment){
        holder.repliesArray.clear()
        holder.repliesArray.addAll(comment.comments)
        holder.rvReplies.adapter?.notifyDataSetChanged()
        holder.rvReplies.visibility = View.VISIBLE
        holder.viewReply.text = "Hide replies"
    }*/


    fun expandReplyCommentId(position:Int,replyCommentId: String){
        Log.i(TAG," tryingToExapand reply:: $position $replyCommentId")
        list?.get(position)?.makeExpanded = true
        list?.get(position)?.repliesId = replyCommentId
        notifyItemChanged(position)
        //notifyDataSetChanged()

    }

    private fun changeViewreplyText(comment: Comment, holder: RviewHolder) {
        if(holder.repliesArray.size == 0 ){
            holder.viewReply.text = "Show replies"
        }
        else if((comment.comments.size - holder.repliesArray.size) > 0) {
            holder.viewReply.text = "view ${comment.comments.size - holder.repliesArray.size} more replies"
        }
        else{
            holder.viewReply.text = "Hide replies"
        }
    }

    private fun togglelike(holder: RviewHolder, comment: Comment?, position: Int) {
        if(comment?.liked_by_user== true){
            list?.get(position)?.liked_by_user = false
            list?.get(position)?.likes_count = list?.get(position)?.likes_count?.minus(1)!!

            /*if(isUserliked == true) {
                *//*holder.likeCount.text = (comment.likes_count - 1).toString()
                holder.ivLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_small_heart))*//*
                isUserliked = false
                list?.get(position)?.liked_by_user = false
                list?.get(position)?.likes_count = list?.get(position)?.likes_count?.minus(1)!!
            }else{
                *//*holder.likeCount.text = (comment.likes_count).toString()
                holder.ivLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_heart))*//*
                isUserliked = true
                list?.get(position)?.liked_by_user = true
            }*/
        }
        else{
            list?.get(position)?.liked_by_user = true
            list?.get(position)?.likes_count = list?.get(position)?.likes_count?.plus(1)!!
            /*if(isUserliked == true) {
                holder.likeCount.text = (comment?.likes_count).toString()
                holder.ivLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_small_heart))
                isUserliked = false
            }else{
                holder.likeCount.text = (comment?.likes_count?.plus(1)).toString()
                holder.ivLike.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_heart))
                isUserliked = true
            }*/
        }
        notifyItemChanged(position)
    }

    private fun addListener(holder: RviewHolder, comment: Comment?) {
        holder.viewReply.setOnClickListener {
            addCommentInReplies(holder,comment)
        }
    }

    fun addCommentInReplies(holder: RviewHolder, comment: Comment?){
        if(holder.rvReplies.visibility == View.GONE){
            if((comment?.comments?.size?.minus(holder.repliesArray.size)!!) > 0) {
                holder.rvReplies.visibility = View.VISIBLE
                addReplies(holder, comment)
                changeViewreplyText(comment, holder)
            }else{
                holder.rvReplies.visibility = View.VISIBLE
                holder.viewReply.text = "Hide replies"
            }
        }else{
            if((comment?.comments?.size?.minus(holder.repliesArray.size)!!) > 0) {
                addReplies(holder, comment)
                changeViewreplyText(comment, holder)
            }else{
                holder.rvReplies.visibility = View.GONE
                holder.viewReply.text = "view ${comment?.comments?.count()} more replies"
            }
        }
    }

    fun addAllinReplies(holder: RviewHolder, comment: Comment?){}

    companion object{
        private val COMPARATOR = object : DiffUtil.ItemCallback<Comment>(){
            override fun areItemsTheSame(
                oldItem: Comment,
                newItem: Comment
            ): Boolean {
                return (oldItem._id == newItem._id)
            }

            override fun areContentsTheSame(
                oldItem: Comment,
                newItem: Comment
            ): Boolean {
                return oldItem == newItem
            }

        }
    }




}