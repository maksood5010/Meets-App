package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.places.SearchPlaceResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils

class SearchPlaceAdapter(
    var myContext: Activity, var list: SearchPlaceResponse?, var clickListener: (Int) -> Unit
) : RecyclerView.Adapter<SearchPlaceAdapter.RviewHolder>() {

    private val TAG =  SearchPlaceAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_search_place_item, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var data = list?.get(position)
        Log.i(TAG," SearchPlaceAdapter::: $data ")
        Utils.stickImage(myContext,holder.image,data?.main_image_url,null)
        holder.place_name.text = data?.name_en
        if(data?.timings?.isNotEmpty() == true){
            holder.time.visibility = View.VISIBLE
            val opentime = data.timings?.get(0)?.opentime?:"N/A"
            val closetime = data.timings?.get(0)?.closetime?:"N/A"
            holder.time.text = "open: $opentime - $closetime"
        }
        else{
            holder.time.visibility = View.GONE
        }


        if(data?.kind_of_places?.size?.compareTo(0) == 1){
            var kindPlaces = StringBuilder()
            data.kind_of_places?.map { key->
                PreferencesManager.get<CategoryResponse>(Constant.PREFRENCE_CATEGORY)?.definition?.map {
                    if(it.key == key){
                        if (kindPlaces.isEmpty()){
                            kindPlaces.append(it.en)
                        }else{
                            kindPlaces.append(", ").append(it.en)
                        }
                    }
                }
            }
            holder.category.text = kindPlaces.toString()
        }
        holder.root.setOnClickListener {
            var baseFragment : BaseFragment = RestaurantDetailFragment();
            Navigation.setFragmentData(baseFragment,"_id",data?.elastic_id?:data?._idLocal)
            Navigation.setFragmentData(baseFragment,"isItElastic",data?.elastic_id?:"")
            Navigation.addFragment(myContext,baseFragment,
                RestaurantDetailFragment.TAG,R.id.homeFram,true,false)
        }
        data?.rating?.let {
            holder.llRate.visibility = View.VISIBLE
            holder.rate.text = it.toString().plus("/5.0")
        }?:run{
            holder.llRate.visibility = View.GONE
        }

        if(data?.offers_count == 0){
            holder.offer.visibility = View.GONE
        }
        else{
            holder.offer.visibility = View.VISIBLE
            holder.offer.text = data?.of_en?.get(0)
        }

    }

    override fun getItemCount(): Int {
        if(list == null)
            return 0
        else{
            Log.i(TAG," getItemCountplaces:: ${list?.size}")
            return list?.size!!
        }

    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var image = itemView.findViewById<ImageView>(R.id.place_image)
        var place_name = itemView.findViewById<TextView>(R.id.place_name)
        var time = itemView.findViewById<TextView>(R.id.time)
        var category = itemView.findViewById<TextView>(R.id.category)
        var rate = itemView.findViewById<TextView>(R.id.rate)
        var llRate = itemView.findViewById<LinearLayout>(R.id.llrate)
        var offer = itemView.findViewById<TextView>(R.id.offer)
    }

}