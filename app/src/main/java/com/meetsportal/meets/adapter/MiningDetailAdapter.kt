package com.meetsportal.meets.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.places.RewardsResponseItem
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.setRoundedColorBackground
import java.util.*

class MiningDetailAdapter(
    var context: Activity,
    val list: ArrayList<RewardsResponseItem>,
                         ) : RecyclerView.Adapter<MiningDetailAdapter.RviewHolder>(){

    val TAG = MiningDetailAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_mining_detail, parent, false)
                                                                                  )

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        val item=list.get(position)
        holder.llBrought.setRoundedColorBackground(context,corner = 20f,enbleDash = false)
        holder.tvRewardTotal.setTextColor(Color.parseColor(item.meta?.color))
        holder.sivIcon.loadImage(context,item.meta?.icon_url,placeholder = R.drawable.bg_shimmer_curve,showSimmer = false)
        holder.tvTitle.text=item.meta?.name?.en
        holder.tvRewardTotal.text= Utils.prettyCount(item.total_mints)
        if(item.component_id=="mint_cash"){
            holder.tvBrought.text="Bought Mint"
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class RviewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        var llBrought = itemView.findViewById<LinearLayout>(R.id.ll_brought)
        var sivIcon = itemView.findViewById<ImageView>(R.id.sivIcon)
        var tvRewardTotal = itemView.findViewById<TextView>(R.id.tvRewardTotal)
        var tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        var tvDesc = itemView.findViewById<TextView>(R.id.tvDesc)
        var tvBrought = itemView.findViewById<TextView>(R.id.tvBrought)
    }
}