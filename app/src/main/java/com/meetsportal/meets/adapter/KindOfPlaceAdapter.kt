package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.places.Category
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.places.Cuisine
import com.meetsportal.meets.networking.places.Facility
import com.meetsportal.meets.utils.Constant.PREFRENCE_CATEGORY
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils

class KindOfPlaceAdapter<T>(
    var myContext: Activity,
    var list : List<T>?,
    var viewType : Int?

) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    var categories : List<Category>?

    init {
        categories = PreferencesManager.get<CategoryResponse>(PREFRENCE_CATEGORY)?.definition
    }

    companion object{
        var KIND_OF_PLACES = 1
        var CUISONE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //Log.i("TAG"," viewType::: $viewType")
        when(this.viewType){
            1 ->return KindOfPlacesHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.card_kind_of_places, parent, false)
            )
            2 -> return CuisineHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.card_detail_cuisine, parent, false)
            )
            3 -> return FacilityHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.card_detail_facility, parent, false)
            )

            else -> return KindOfPlacesHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.card_kind_of_places, parent, false)
            )
        }

    }

    override fun getItemCount(): Int {
        return list?.size?:0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(viewType){
            1 -> bindKindOfPlaces(holder as KindOfPlacesHolder,position)
            2-> bindCuisine(holder as CuisineHolder,position)
            3 -> bindFacility(holder as FacilityHolder, position)
        }

    }

    private fun bindFacility(holder: FacilityHolder, position: Int) {
        var facility = list?.get(position) as Facility?
        holder.facility.text = facility?.en
    }

    private fun bindCuisine(holder: CuisineHolder, position: Int) {
        var cuisine = list?.get(position) as Cuisine?
        holder.cuisine.text = cuisine?.en
    }

    private fun bindKindOfPlaces(holder: KindOfPlacesHolder, position: Int) {
        var category = list?.get(position) as Category?

        Utils.stickOGSvg(myContext,category?.color_svg_url,holder.icon)
        holder.text.text = category?.en
    }

    class KindOfPlacesHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        var icon = itemView.findViewById<ImageView>(R.id.icon)
        var text = itemView.findViewById<TextView>(R.id.tv_kind)
    }

    class CuisineHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        var cuisine = itemView.findViewById<TextView>(R.id.cuisine)
    }

    class FacilityHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        var facility = itemView.findViewById<TextView>(R.id.facility)
    }


}