package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.networking.meetup.Place
import com.meetsportal.meets.networking.places.Category
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils

class MeetInvitationPlaceAdapter(var myContext: Activity, var places: List<Place?>?,val customPlaces: List<AddressModel>?)
    : RecyclerView.Adapter<MeetInvitationPlaceAdapter.RviewHolder>(){

    var categories : List<Category>?
    init {
        categories = PreferencesManager.get<CategoryResponse>(Constant.PREFRENCE_CATEGORY)?.definition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_meet_place_item, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        if(!places.isNullOrEmpty()&& (position< (places?.size?:0))){
            places?.get(position)?.let { place ->
                holder.name.setText(place.name.en)
                categories?.let {
                    categories?.firstOrNull() { it.key.equals(place?.primary_kind_of_place?.get(0)) == true }
                            ?.let {
                                Utils.stickOGSvg(myContext, it.color_svg_url, holder.image)
                            } ?: run {
                        holder.image.setImageResource(R.drawable.ic_location)
                    }
                }
            }
        }else{
            customPlaces?.getOrNull(position-(places?.size?:0))?.let {
                holder.name.setText(it?.name)
                holder.image.setImageResource(R.drawable.ic_location)
            }
        }
    }

    override fun getItemCount(): Int {
        return (places?.size ?: 0) + (customPlaces?.size ?: 0)
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name = itemView.findViewById<TextView>(R.id.name).apply {
            paintFlags = getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG
        }
        var image = itemView.findViewById<ImageView>(R.id.image)
    }

}