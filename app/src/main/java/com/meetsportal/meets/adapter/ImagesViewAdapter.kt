package com.meetsportal.meets.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.imageview.ShapeableImageView
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.onClick

class ImagesViewAdapter (var context: Context, var list: ArrayList<Uri>?,var click: (position:Int) -> Unit) :
    RecyclerView.Adapter<ImagesViewAdapter.RviewHolder>() {

    private val TAG = ImagesViewAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_images_layout, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        val image = list?.get(position)?:return
        Glide.with(context)
                .asBitmap().placeholder(R.drawable.ic_person_placeholder)
                .load(image).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.drawable.ic_person_placeholder)
                .into(holder.image)
//        holder.image.setImageURI(image)
        holder.image.onClick({
            click(position)
        })
    }



    /*fun setData(list:List<Contact>){
        this.list = list
        notifyDataSetChanged()
    }*/

    override fun getItemCount(): Int {
        return list?.size ?:0
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ShapeableImageView =itemView.findViewById(R.id.image)
    }
}