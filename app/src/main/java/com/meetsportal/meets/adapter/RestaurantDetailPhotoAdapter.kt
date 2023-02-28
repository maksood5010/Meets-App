package com.meetsportal.meets.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.places.Gallery
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.onClick

class RestaurantDetailPhotoAdapter (var context: Context, var images: ArrayList<Gallery?>, var listener :(Int) -> Unit ) :
    RecyclerView.Adapter<RestaurantDetailPhotoAdapter.RviewHolder>() {

    private val TAG = RestaurantDetailPhotoAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_photo, parent, false)
    )


    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        images.getOrNull(position)?.imageUrl?.let {
            Utils.stickImage(context,holder.image,it,null)
        }
        holder.image.onClick({ listener(position) })
    }

    override fun getItemCount(): Int {
        Log.i(TAG," getCount:: ${images.size}")
        return if(images.isNotEmpty())
            images.size
        else
            0
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image :ImageView = itemView.findViewById(R.id.iv_image)
    }
}