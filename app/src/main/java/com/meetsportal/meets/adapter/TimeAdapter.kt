package com.meetsportal.meets.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R

class TimeAdapter (val context: Context, var timesList: Array<String>,var listener: (Int,String) -> Unit) : RecyclerView.Adapter<TimeAdapter.MyViewHolder>() {

    var rowIndex = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_time, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return timesList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.tvTime.text = timesList[position]

        holder.itemView.setOnClickListener(View.OnClickListener { v ->
            rowIndex = position
            notifyDataSetChanged()
            listener(position,timesList.get(position).toString())

        })

        if (rowIndex == position) {
            holder.tvTime.setBackgroundResource(R.drawable.blue_button_background)
            holder.tvTime.setTextColor(Color.parseColor("#FFFFFF"))

        } else {
            holder.tvTime.setBackgroundResource(R.drawable.gray_button_background)
            //holder.tvTime.setTextColor(Color.parseColor("#32BFC9"))
            holder.tvTime.setTextColor(ContextCompat.getColor(context,R.color.primaryDark))

        }

    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTime = view.findViewById<TextView>(R.id.tv_time)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


}