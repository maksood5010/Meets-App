package com.meetsportal.meets.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.places.FetchPlacesResponse
import com.meetsportal.meets.networking.places.FetchPlacesResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.utils.*
import java.util.*

class SavedPlacesListAdapter(
    var myContext: Activity, var list: FetchPlacesResponse?, var clickListener: (Boolean) -> Unit
) : RecyclerView.Adapter<SavedPlacesListAdapter.ViewHolder>() {

    var day: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    fun setListData(list: FetchPlacesResponse){
        this.list=list
        notifyDataSetChanged()
        clickListener(list.isEmpty())
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_saved_place, parent, false)
        )

    @SuppressLint("RecyclerView", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list?.get(position)
        Utils.stickImage(myContext, holder.image, item?.main_image_url, null)
        holder.tvName.text = item?.name?.en
        item?.timings?.get(day - 1)?.opentime?.let {
            holder.tvTime.text = "${item?.timings?.get(day - 1)?.opentime} - ${
                item?.timings?.get(day - 1)?.closetime
            }"
        } ?: run {
            holder.tvTime.visibility = View.GONE
        }
        if(item?.kind_of_places?.size?.compareTo(0) == 1){
            var kindPlaces = StringBuilder()
            item.kind_of_places?.map { key->
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
            holder.tvType.text = kindPlaces.toString()
        }else{
            holder.tvType.visibility=View.GONE
        }
        val address = getAddress(item)
        if (address!=null){
            holder.tvAddress.text=address
        }else{
            holder.tvAddress.visibility=View.GONE
        }
        if (item?.rating != null) {
            holder.tvRating.text= item.rating.toString().plus(" / 5.0")
        }else{
            holder.tvRating.visibility=View.GONE
        }
        holder.itemView.onClick({
            var baseFragment : BaseFragment = RestaurantDetailFragment();
            Navigation.setFragmentData(baseFragment,"_id",item?._id)
            Navigation.addFragment(myContext,baseFragment,
                RestaurantDetailFragment.TAG,R.id.homeFram,true,false)
        })
    }

    fun  getAddress(data: FetchPlacesResponseItem?):String?{
        val sbAddress = StringBuilder()
        data?.street?.en?.let { street -> sbAddress.append(street);sbAddress.append(", ") }
        data?.city?.let { sbAddress.append(it);sbAddress.append(", ") }
        data?.state?.let { sbAddress.append(it);sbAddress.append(", ") }
        data?.country?.let { sbAddress.append(it);sbAddress.append(", ") }
        return sbAddress.toString()
    }


    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.iv_bg_card)
        var tvName: TextView = itemView.findViewById(R.id.tv_name)
        var tvTime: TextView = itemView.findViewById(R.id.tvTime)
        var tvType: TextView = itemView.findViewById(R.id.tvType)
        var tvAddress: TextView = itemView.findViewById(R.id.tvAddress)
        var tvRating: TextView = itemView.findViewById(R.id.tvRating)
    }
}