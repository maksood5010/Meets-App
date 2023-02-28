package com.meetsportal.meets.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.maps.model.PlacesSearchResult
import com.meetsportal.meets.R
import com.meetsportal.meets.models.AddressModel

class NearByListAdapter(
    var myContext: Context,
    var list: Array<PlacesSearchResult>?,
    var clickListener: (AddressModel) -> Unit
) : RecyclerView.Adapter<NearByListAdapter.ViewHolder>() {

    var lastPosition: Int = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_near_by, parent, false)
        )

    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = list?.get(position) ?: return
        val addressModel = AddressModel()
        addressModel._id = address.placeId
        addressModel.image_url = address.icon.toString()
        addressModel.name = address.name
        addressModel.address = address.vicinity
        addressModel.setLocation(address.geometry.location.lat,address.geometry.location.lng)
//        addressModel.lat = address.geometry.location.lat
//        addressModel.lon = address.geometry.location.lng
//        Utils.stickImage(myContext,holder.image,address.icon.toString(),null)

        //Used for to change the place holder
        Glide.with(myContext)
            .asBitmap().placeholder(R.drawable.ic_pin_location)
            .load(address.icon.toString()).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .error(R.drawable.ic_pin_location)
            .into(holder.image)

        holder.tvPlaceName.text = address.name
        holder.tvPLaceAddress.text = address.vicinity
        holder.itemView.setOnClickListener {
            lastPosition = position
            notifyDataSetChanged()
            clickListener(addressModel)
        }
        if (lastPosition == position) {
            holder.rvCheckbox.setImageResource(R.drawable.ic_round_check)
        } else {
            holder.rvCheckbox.setImageResource(R.drawable.bg_black_stroke_25dp)
        }
    }

    override fun getItemCount(): Int {

        return list?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.ivPlace)
        var rvCheckbox: ImageView = itemView.findViewById(R.id.rvCheckbox)
        var tvPlaceName: TextView = itemView.findViewById(R.id.tvPlaceName)
        var tvPLaceAddress: TextView = itemView.findViewById(R.id.tvPLaceAddress)
    }
}