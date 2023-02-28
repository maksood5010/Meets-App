package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.Utils

class EditPostImageAdapter(var context:Activity,
                           var list: List<String>?)
    : RecyclerView.Adapter<EditPostImageAdapter.RviewHolder>()  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder =
        RviewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.card_create_post, parent, false)
        )


    override fun getItemCount(): Int {
        return list?.size?:0
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
       Utils.stickImage(context,holder.image,list?.get(position),null)
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var Edit = itemView.findViewById<ImageView>(R.id.bt_edit).apply {
            visibility = View.GONE
        }
        var cancel = itemView.findViewById<ImageView>(R.id.cv_cancel).apply {
            visibility = View.GONE
        }
        var image = itemView.findViewById<ImageView>(R.id.iv_image)

    }


}