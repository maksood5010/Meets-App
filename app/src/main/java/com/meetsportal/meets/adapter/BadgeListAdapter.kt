package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.Constant.badgeList
import com.meetsportal.meets.utils.setRoundedColorBackground

class BadgeListAdapter(val myContext: Activity,var isItfilter : Boolean = false,var clicked:(Int)->Unit): RecyclerView.Adapter<BadgeListAdapter.ViewHolder>() {

    var myBadgelist = badgeList.map { it.apply { it.selected = false } }
    var selected = 0
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_badge_item, parent, false)
    )


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var  data  = badgeList.get(position)
        val wrapper = ContextThemeWrapper(myContext, data.style)
        val drawable: Drawable? = VectorDrawableCompat.create(myContext.resources, R.drawable.ic_trophy_dummy, wrapper.getTheme())
        holder.ivTrophy.setImageDrawable(drawable)
        holder.tvTitle.text = data.name.plus(" Status holder")
        if(isItfilter){
            if(selected <= position){
                holder.ivCheck.setImageDrawable(ContextCompat.getDrawable(myContext,R.drawable.ic_checked))
            }else{
                holder.ivCheck.setImageDrawable(ContextCompat.getDrawable(myContext,R.drawable.ic_unchecked))
            }
        }
        holder.root.setOnClickListener {
            selected = position
            clicked(selected)
            notifyDataSetChanged()
        }

    }



    override fun getItemCount(): Int {
        return badgeList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivTrophy = itemView.findViewById<ImageView>(R.id.ivTrophy).apply {
            setRoundedColorBackground(myContext,R.color.gray1,corner = 100f)
        }
        var ivCheck = itemView.findViewById<ImageView>(R.id.ivCheck)
        var tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        var root = itemView.findViewById<ConstraintLayout>(R.id.root)


    }
}