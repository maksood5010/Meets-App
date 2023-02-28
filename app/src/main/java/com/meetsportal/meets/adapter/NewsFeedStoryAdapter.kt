package com.meetsportal.meets.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.Story

class NewsFeedStoryAdapter (var context: Context, var list: ArrayList<Story>?, var listener :(Int) -> Unit):
    RecyclerView.Adapter<NewsFeedStoryAdapter.RviewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_news_feed_story, parent, false)
    )

    override fun getItemCount(): Int {
        if(list != null)
            return list?.size!!
        else
            return 0
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        holder.image.setImageDrawable(context.resources.getDrawable(list?.get(position)?.image!!))
        holder.name.text = list?.get(position)?.name
        holder.followerCount.text = list?.get(position)?.followCount
    }

    fun updateList(list: List<Story>){
        this.list?.clear()
        this.list?.addAll(list)
        this.notifyDataSetChanged()
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image = itemView.findViewById<ImageView>(R.id.civ_image)
        var verifyTick = itemView.findViewById<ImageView>(R.id.iv_verify)
        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var followerCount = itemView.findViewById<TextView>(R.id.tv_follow_count)
        var follow = itemView.findViewById<TextView>(R.id.tv_follow)

    }
}