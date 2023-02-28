package com.meetsportal.meets.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.onClick
import de.hdodenhof.circleimageview.CircleImageView

class ImageFilterOptionAdapter(var context: Context, var list: ArrayList<Bitmap>?, var listener :(Int) -> Unit):
    RecyclerView.Adapter<ImageFilterOptionAdapter.RviewHolder>() {

    var clicked = 0
    val filterName: Array<String> = context.resources.getStringArray(R.array.filterNames)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    )= RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_image_filter, parent, false)
    )

    override fun getItemCount(): Int {
        if(list != null) {
            return list?.size!!
        }
        else
            return 0
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        Glide.with(context)
                .asBitmap().placeholder(R.drawable.ic_person_placeholder)
                .load(list!![position]).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.drawable.ic_person_placeholder)
                .into(holder.image)
//        holder.image.setImageBitmap(list!![position])
        if(clicked == position){
            holder.ring.visibility = View.VISIBLE
        }else{
            holder.ring.visibility = View.INVISIBLE
        }
        holder.image.onClick( {
            clicked = position
            notifyDataSetChanged()
            listener(position)
        })
        holder.tvName.text = filterName.getOrNull(position)?:"Filter"

    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName = itemView.findViewById<TextView>(R.id.tvName)
        var ring = itemView.findViewById<LinearLayout>(R.id.ll_ring)
        var image = itemView.findViewById<CircleImageView>(R.id.civ_image)

    }
}