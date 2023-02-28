package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.profile.Definition
import com.meetsportal.meets.utils.setRoundedColorBackground

class OpenMeetupTagAdapter(var myContext: Activity, var interests: List<Definition?>?)
    : RecyclerView.Adapter<OpenMeetupTagAdapter.RviewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.card_interest_item, parent, false),myContext
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        interests?.get(position)?.let {
            holder.text.text = it.en
        }

    }

    override fun getItemCount()= interests?.size ?:0


    class RviewHolder(itemView: View, myContext: Activity) :RecyclerView.ViewHolder(itemView) {
        var text = itemView.findViewById<TextView>(R.id.tv_interest_item).apply{
            setRoundedColorBackground(myContext,R.color.white,enbleDash = true,strokeHeight = 1f,Dashgap = 0f,stripSize = 20f,strokeColor = R.color.mining_e)
        }
    }

}