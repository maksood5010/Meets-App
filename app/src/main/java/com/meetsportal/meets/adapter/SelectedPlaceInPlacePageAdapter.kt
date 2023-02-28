package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.models.MeetsPlace

class SelectedPlaceInPlacePageAdapter(
    var myContext: Activity,
    var selectedPlace: MutableMap<String?, MeetsPlace?>,
    var customPlaces: ArrayList<AddressModel>
) : RecyclerView.Adapter<SelectedPlaceInPlacePageAdapter.RviewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RviewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_pre_seleced_place, parent, false)

        )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        if(position < selectedPlace.size){
            holder.titleName.text = selectedPlace.get(selectedPlace.keys.elementAt(position))?.name?.en
        }else{
            customPlaces.getOrNull(position-(selectedPlace.size))?.let {
                holder.titleName.text = it.name
            }
        }
    }

    override fun getItemCount(): Int {
        return selectedPlace.size.plus(customPlaces.size)
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //var mainImage = itemView.findViewById<>(R.id.mainImage)

        var titleName = itemView.findViewById<TextView>(R.id.placename)
    }
}