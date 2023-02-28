package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.post.SearchPostResponse
import com.meetsportal.meets.networking.post.SearchPostResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.DetailPostFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage

class SearchPostAdapter(
    var myContext: Activity, var list: SearchPostResponse, var clickListener: (Int) -> Unit
) : RecyclerView.Adapter<SearchPostAdapter.RviewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_search_post_item, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var data = list.get(position)
        if(data.media?.size?.compareTo(0) == 1){
        //Utils.stickImage(myContext,holder.dp,data.media?.get(0)?:data.user_meta?.profile_image_url,null)
            holder.dp.loadImage(myContext,data.media?.getOrNull(0)?:data.user_meta?.profile_image_url,R.drawable.ic_default_person)
        }
        holder.time.text = Utils.getCreatedAt(data.createdAt)
        if(data.user_meta?.first_name.isNullOrEmpty()){
            holder.name.text = data.user_meta?.username
        }else{
            holder.name.text = data.user_meta?.first_name
        }
        holder.caption.text  = data.body
        holder.root.setOnClickListener{
            openPostDetail(data)

        }

    }

    fun openPostDetail(post: SearchPostResponseItem) {

        /*var detailPostParcel = DetailPostParcel(
            _id = post.elastic_id,
            media = post.media,
            stats  = Stats(likes = post.likes_count,comments = post.comments_count),
            body = post.body,
            user_meta = post.user_meta,
            createdAt = post.createdAt,
            mode = "elastic"

        )
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
        Navigation.setFragmentData(baseFragment, "post_id", post?.elastic_id)
        Navigation.setFragmentData(baseFragment, "mode", "elastic")
        Navigation.addFragment(
            myContext,
            baseFragment,
            Constant.TAG_DETAIL_POST_FRAGMENT,
            R.id.homeFram,
            true,
            false
        )

    }

    override fun getItemCount(): Int {
        Log.i("TAG"," getItemCountpost:: ${list.size}")
        return list.size
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dp = itemView.findViewById<ImageView>(R.id.iv_post_image)
        var name = itemView.findViewById<TextView>(R.id.name)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var time = itemView.findViewById<TextView>(R.id.time)
        var caption = itemView.findViewById<TextView>(R.id.caption)
    }

}