package com.meetsportal.meets.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.utils.PlaceApi

class PlacesAutoCompleteAdapter(
    var myContext: Context,
    var list: MutableList<AddressModel>,
    var clickListener: (AddressModel) -> Unit
): RecyclerView.Adapter<PlacesAutoCompleteAdapter.ViewHolder>(), Filterable {
    val placeApi:PlaceApi= PlaceApi(myContext)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_places_api, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = list.get(position)?:return
//        Utils.stickImage(myContext,holder.image,address.img,null)
        holder.tvPlaceName.text=address.name
        holder.tvPLaceAddress.text=address.address
        holder.itemView.setOnClickListener{
            clickListener(address)
        }
    }

    override fun getItemCount(): Int {
        return list.size?:0
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image :ImageView= itemView.findViewById(R.id.ivPlace)
        var tvPlaceName:TextView = itemView.findViewById(R.id.tvPlaceName)
        var tvPLaceAddress:TextView = itemView.findViewById(R.id.tvPLaceAddress)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                Log.d("TAG", "performFiltering:constraint ${constraint != null}")
                if (constraint != null) {
                    val filteredList = placeApi.autoComplete(constraint.toString())
                    filterResults.values = filteredList
                    filterResults.count = filteredList.size
                    Log.d("TAG", "performFiltering:constraint ${filterResults.count}")
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
                if (filterResults != null && filterResults.count > 0) {
                    list=filterResults.values as MutableList<AddressModel>
                } else {
                    list= mutableListOf()
                }
                Log.d("TAG", "publishResults:constraint ${list?.size}")
                notifyDataSetChanged()
            }
        }
    }
}