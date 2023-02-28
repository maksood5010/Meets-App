package com.meetsportal.meets.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R

class EmojiListAdapter(
    var myContext: Context,
    var list: ArrayList<String>?,
    var clickListener: (String) -> Unit
                      ) : RecyclerView.Adapter<EmojiListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.text_emoji, parent, false)
        )

    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val emoji = list?.get(position) ?: return
        holder.tvEmoji.text = emoji
        holder.tvEmoji.setOnClickListener {
            clickListener(emoji)
        }
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvEmoji: TextView = itemView.findViewById(R.id.tvEmoji)
    }
}