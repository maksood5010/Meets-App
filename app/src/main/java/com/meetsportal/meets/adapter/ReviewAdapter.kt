package com.meetsportal.meets.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.places.PlaceReviewList
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage

class ReviewAdapter(
    var myContext: Context, var list: PlaceReviewList?, var clickListener: (Int) -> Unit
): RecyclerView.Adapter<ReviewAdapter.RviewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder =
        RviewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.review_item_layout, parent, false)
        )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var reviewItem = list?.get(position)
        //Utils.stickImage(myContext,holder.image,reviewItem?.user?.profile_image_url,null)
        holder.image.loadImage(myContext,reviewItem?.user?.profile_image_url,R.drawable.ic_default_person)
        holder.ivDpBadge.setImageResource(Utils.getBadge(reviewItem?.user?.badge).foreground)
        holder.username.text = reviewItem?.user?.username
        holder.reviewTime.text = Utils.getCreatedAt(reviewItem?.review?.updatedAt)
        holder.rating.rating = reviewItem?.review?.rating?.toFloat()?:0f
        holder.ratingText.text = reviewItem?.review?.rating.toString().plus(" of 5")
        holder.review.text = reviewItem?.review?.body

    }

    override fun getItemCount(): Int {
        return list?.size?:0
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image = itemView.findViewById<ImageView>(R.id.userImg)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
        var username = itemView.findViewById<TextView>(R.id.userName)
        var reviewTime = itemView.findViewById<TextView>(R.id.review_time)
        var rating = itemView.findViewById<RatingBar>(R.id.ratingDataRate)
        var ratingText = itemView.findViewById<TextView>(R.id.ratingData)
        var review = itemView.findViewById<TextView>(R.id.userReview)

    }


}