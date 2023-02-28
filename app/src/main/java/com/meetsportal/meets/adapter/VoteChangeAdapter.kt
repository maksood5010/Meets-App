package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R

class VoteChangeAdapter(val myContext: Activity, var icon: Int, var currentSelected:Int, val array: List<String>, var clicked: (Int) -> Unit) : RecyclerView.Adapter<VoteChangeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.card_badge_item, parent, false))


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = array.get(position)
        holder.ivTrophy.setImageResource(icon)
        holder.tvTitle.text = data
            if(currentSelected == position) {
                holder.ivCheck.setImageDrawable(ContextCompat.getDrawable(myContext, R.drawable.ic_checked))
            } else {
                holder.ivCheck.setImageDrawable(ContextCompat.getDrawable(myContext, R.drawable.ic_unchecked))
            }
        holder.itemView.setOnClickListener {

            currentSelected = position
            clicked(currentSelected)
            notifyDataSetChanged()
        }

    }


    override fun getItemCount(): Int {
        return array.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var ivTrophy = itemView.findViewById<ImageView>(R.id.ivTrophy)
        var ivCheck = itemView.findViewById<ImageView>(R.id.ivCheck)
        var tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        var root = itemView.findViewById<ConstraintLayout>(R.id.root)


    }
}