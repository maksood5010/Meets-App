package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.profile.SearchPeopleResponse
import com.meetsportal.meets.networking.profile.SearchPeopleResponseItem
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage

class SearchPeopleAdapter(
    var myContext: Activity, var list: SearchPeopleResponse, var clickListener: (SearchPeopleResponseItem) -> Unit
) : RecyclerView.Adapter<SearchPeopleAdapter.RviewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_search_people_item, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var data = list.get(position)
        //Utils.stickImage(myContext,holder.dp,data.profile_image_url,null,false)
        holder.dp.loadImage(myContext,data.profile_image_url,R.drawable.ic_default_person)
        holder.ivDpBadge.setImageResource(Utils.getBadge(data.badge).foreground)
        if (!data?.first_name.isNullOrEmpty())
            holder.name.text = data?.first_name.plus(" ${data?.last_name}")
        else
            holder.name.text = data?.username
        //holder.name.text = data.username
        holder.username.text = "@".plus(data.username)
        Utils.onClick(holder.root,1000){
            clickListener(data)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItem(data : SearchPeopleResponse){
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dp = itemView.findViewById<ImageView>(R.id.image)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
        var name = itemView.findViewById<TextView>(R.id.name)
        var username = itemView.findViewById<TextView>(R.id.username)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }

}