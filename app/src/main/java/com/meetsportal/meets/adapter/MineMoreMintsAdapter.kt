package com.meetsportal.meets.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.socialfragment.MySafeFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.setRoundedColorBackground
import de.hdodenhof.circleimageview.CircleImageView

class MineMoreMintsAdapter(
    var context: Activity,
                          ) : RecyclerView.Adapter<MineMoreMintsAdapter.RviewHolder>(){

    val TAG = MineMoreMintsAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.card_mine_more_mint, parent, false)
                                                                                  )

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RviewHolder, position: Int) {

        holder.itemView.setOnClickListener {
            Navigation.addFragment(context, MySafeFragment(), Constant.TAG_MYSAFE_FRAGMENT, R.id.homeFram, true, false)
        }

        val corner=10f
        val corner5=5f

        if(position == 1) {
            holder.ivCircleBG.setRoundedColorBackground(context, Color.parseColor("#FBB03B"), enbleDash = false, corner = corner)
            holder.tvMintHeading.setRoundedColorBackground(context, Color.parseColor("#FBB03B"), enbleDash = false, corner = corner5)
            holder.tvMintNumber.setText("50")
            holder.tvMintHeading.setText("Attend Meetup")
            holder.tvMintSubHeading.setText("Use the “I am here” button to indicate that you have attended a meetup to mine 50mints. ")
        } else if(position == 2){
            holder.ivCircleBG.setRoundedColorBackground(context, Color.parseColor("#ED1C24"), enbleDash = false, corner = corner)
            holder.tvMintHeading.setRoundedColorBackground(context, Color.parseColor("#ED1C24"), enbleDash = false, corner = corner5)
            holder.tvMintNumber.setText("30")
            holder.tvMintHeading.setText("Get a Meets Positive Experience review")
            holder.tvMintSubHeading.setText("After a meetup, your friends can give you a review letting you know they had good experience with you. Mine 30 mints when you receive a MPE.")
        }  else if(position == 3){
            holder.ivCircleBG.setRoundedColorBackground(context, Color.parseColor("#FBB03B"), enbleDash = false, corner = corner)
            holder.tvMintHeading.setRoundedColorBackground(context, Color.parseColor("#FBB03B"), enbleDash = false, corner = corner5)
            holder.tvMintNumber.setText("20")
            holder.tvMintHeading.setText("Give a Meets Positive Experience review")
            holder.tvMintSubHeading.setText("After a meetup, your can give your  friends a review letting them know you had good experience with them. Mine 20 mints when you give a MPE.")
        } else if(position == 4){
            holder.ivCircleBG.setRoundedColorBackground(context, Color.parseColor("#9E005D"), enbleDash = false, corner = corner)
            holder.tvMintHeading.setRoundedColorBackground(context, Color.parseColor("#9E005D"), enbleDash = false, corner = corner5)
            holder.tvMintNumber.setText("15")
            holder.tvMintHeading.setText("Check-in to a place and share on timeline")
            holder.tvMintSubHeading.setText("Use the check-in feature and share the place on your timeline. Mine 15mints when you check in and share the place on your timeline.")
        } else if(position == 5){
            holder.ivCircleBG.setRoundedColorBackground(context, Color.parseColor("#662D91"), enbleDash = false, corner = corner)
            holder.tvMintHeading.setRoundedColorBackground(context, Color.parseColor("#662D91"), enbleDash = false, corner = corner5)
            holder.tvMintNumber.setText("10")
            holder.tvMintHeading.setText("Check -in to a place without sharing")
            holder.tvMintSubHeading.setText("Use the check-in feature without sharing the place on your timeline. Mine 10mints when you check in and share the place on your timeline.")
        } else if(position == 6){
            holder.ivCircleBG.setRoundedColorBackground(context, Color.parseColor("#ED1E79"), enbleDash = false, corner = corner)
            holder.tvMintHeading.setRoundedColorBackground(context, Color.parseColor("#ED1E79"), enbleDash = false, corner = corner5)
            holder.tvMintNumber.setText("5")
            holder.tvMintHeading.setText("Write a review")
            holder.tvMintSubHeading.setText("Write a review for a place you have been before. Mine 5 mints when you write a review.")
        } else if(position == 7){
            holder.ivCircleBG.setRoundedColorBackground(context, Color.parseColor("#22B573"), enbleDash = false, corner = corner)
            holder.tvMintHeading.setRoundedColorBackground(context, Color.parseColor("#22B573"), enbleDash = false, corner = corner5)
            holder.tvMintNumber.setText("1")
            holder.tvMintHeading.setText("Open the App daily")
            holder.tvMintSubHeading.setText("Open the Meets App 3 consecutive days")
        } else if(position == 8){
            holder.ivCircleBG.setRoundedColorBackground(context, Color.parseColor("#9E005D"), enbleDash = false, corner = corner)
            holder.tvMintHeading.setRoundedColorBackground(context, Color.parseColor("#9E005D"), enbleDash = false, corner = corner5)
            holder.tvMintNumber.setText("0.5")
            holder.tvMintHeading.setText("Like")
            holder.tvMintSubHeading.setText("When a user gets a like on a post.")
        } else if(position == 9){
            holder.ivCircleBG.setRoundedColorBackground(context, Color.parseColor("#F15A24"), enbleDash = false, corner = corner)
            holder.tvMintHeading.setRoundedColorBackground(context, Color.parseColor("#F15A24"), enbleDash = false, corner = corner5)
            holder.tvMintNumber.setText("0.1")
            holder.tvMintHeading.setText("Like")
            holder.tvMintSubHeading.setText("When a user likes a unique post.")
        } else if(position == 0){
            holder.ivCircleBG.setRoundedColorBackground(context, Color.parseColor("#D4145A"), enbleDash = false, corner = corner)
            holder.tvMintHeading.setRoundedColorBackground(context, Color.parseColor("#D4145A"), enbleDash = false, corner = corner5)
            holder.tvMintNumber.setText("55")
            holder.tvMintHeading.setText("Verify your profile")
            holder.tvMintSubHeading.setText("When a user completes the profile verification successfully.")
            holder.civTopLeft.visibility = View.GONE
            holder.civTopRight.visibility = View.GONE
        }

    }

    override fun getItemCount(): Int {
        return 10
    }

    class RviewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        var ivCircleBG = itemView.findViewById<ImageView>(R.id.iv_circle_bg)
        var ivDottedLine = itemView.findViewById<ImageView>(R.id.iv_dotted_line)
        var civTopLeft = itemView.findViewById<CircleImageView>(R.id.civ_top_left)
        var civTopRight = itemView.findViewById<CircleImageView>(R.id.civ_top_right)
        var civBottomLeft = itemView.findViewById<CircleImageView>(R.id.civ_bottom_left)
        var civBottomRight = itemView.findViewById<CircleImageView>(R.id.civ_bottom_right)
        var tvMintNumber = itemView.findViewById<TextView>(R.id.tv_mint_number)
        var tvMints = itemView.findViewById<TextView>(R.id.tv_mints)
        var tvMintHeading = itemView.findViewById<TextView>(R.id.tv_mint_heading)
        var tvMintSubHeading = itemView.findViewById<TextView>(R.id.tv_mint_sub_heading)
    }
}