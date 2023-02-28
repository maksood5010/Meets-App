package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.databinding.LayoutCoutryStatsBinding
import com.meetsportal.meets.models.RegionView
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.setRoundedColorBackground
import java.util.*

class ProfileViewStateAdapter(var myContext: Activity, var regionViews: List<RegionView>?) : RecyclerView.Adapter<ProfileViewStateAdapter.RviewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RviewHolder(
            //LayoutInflater.from(parent.context).inflate(R.layout.layout_coutry_stats, parent, false)
        LayoutCoutryStatsBinding.inflate(myContext.layoutInflater)
        )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        val data = regionViews?.getOrNull(position)
        holder.binding.tvTotal.text = "Total: ".plus(Utils.prettyCount(data?.stats?.total_views))
        val loc = Locale("", data?.region)
        holder.binding.tvCountry.text = loc.displayCountry
        holder.binding.tvCountCharge.text = (Utils.prettyCount(data?.stats?.boosted_views?:0)).toString()
        holder.binding.tvCountOrg.text = (Utils.prettyCount(data?.stats?.normal_views?:0)).toString()

    }

    override fun getItemCount(): Int {
        return  regionViews?.size?:0
    }

    inner class RviewHolder(itemView: LayoutCoutryStatsBinding) : RecyclerView.ViewHolder(itemView.root){
        var binding = itemView.apply {
            myRoot.setRoundedColorBackground(myContext, R.color.white)
        }
    }
}