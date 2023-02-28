package com.meetsportal.meets.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.places.FetchReviewResponseItem
import com.meetsportal.meets.utils.Utils

class AllReviewAdapter(var myContext: Context): PagingDataAdapter<FetchReviewResponseItem, AllReviewAdapter.RviewHolder>(COMPARATOR) {

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var reviewItem = getItem(position)
        Utils.stickImage(myContext,holder.image,reviewItem?.user_meta?.profile_image_url,null)
        holder.ivDpBadge.setImageResource(Utils.getBadge(reviewItem?.user_meta?.badge).foreground)
        holder.username.text = reviewItem?.user_meta?.username
        holder.reviewTime.text = Utils.getCreatedAt(reviewItem?.updatedAt)
        holder.rating.rating = reviewItem?.rating?.toFloat()?:0f
        holder.review.text = reviewItem?.body
        holder.ratingText.text = reviewItem?.rating.toString().plus(" of 5")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.review_item_layout,
            parent,
            false
        )
    )



    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image = itemView.findViewById<ImageView>(R.id.userImg)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
        var username = itemView.findViewById<TextView>(R.id.userName)
        var reviewTime = itemView.findViewById<TextView>(R.id.review_time)
        var rating = itemView.findViewById<RatingBar>(R.id.ratingDataRate)
        var ratingText = itemView.findViewById<TextView>(R.id.ratingData)
        var review = itemView.findViewById<TextView>(R.id.userReview)

    }



    companion object{
        private val COMPARATOR = object : DiffUtil.ItemCallback<FetchReviewResponseItem>(){
            override fun areItemsTheSame(
                oldItem: FetchReviewResponseItem,
                newItem: FetchReviewResponseItem
            ): Boolean {
                return (oldItem._id == newItem._id)
            }

            override fun areContentsTheSame(
                oldItem: FetchReviewResponseItem,
                newItem: FetchReviewResponseItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }


}