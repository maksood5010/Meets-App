package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.places.LeaderboardResponseItem
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick

class LeaderboardAdapter(var myContext: Activity,val list: ArrayList<LeaderboardResponseItem>,var openProfile : (String?)->Unit) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_leader, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item= list[position]
        val badge = Utils.getBadge(item.badge)
        holder.ivDp.loadImage(myContext, item.user_meta?.profile_image_url, R.drawable.ic_default_person,showSimmer = false)
        holder.ivDpBadge.setImageResource(badge.foreground)
        holder.tvBadgeStatus.text= badge.name
        holder.tvUserName.text= item.user_meta?.username
        holder.tvMints.text= Utils.prettyCount(item.mints)
        holder.tvRank.text=item.rank

        holder.ivDp.onClick({
            if(item.sid!=MyApplication.SID){
                openProfile(item.sid)

//                var baseFragment: BaseFragment = OtherProfileFragment.getInstance(item.sid)
//                Navigation.addFragment(myContext, baseFragment, Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false)
            }
        })


    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivDp:ImageView=itemView.findViewById(R.id.ivDp)
        val ivDpBadge:ImageView=itemView.findViewById(R.id.ivDpBadge)
        val tvUserName:TextView=itemView.findViewById(R.id.tvUserName)
        val tvBadgeStatus:TextView=itemView.findViewById(R.id.tvBadgeStatus)
        val tvMints:TextView=itemView.findViewById(R.id.tvMints)
        val tvRank:TextView=itemView.findViewById(R.id.tvRank)


    }
}