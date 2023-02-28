package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.places.FetchPlacesResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment
import com.meetsportal.meets.utils.Constant.PREFRENCE_CATEGORY
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.onClick
import java.util.*


class AllPlacesAdapter(var myContext: Activity):
    PagingDataAdapter<FetchPlacesResponseItem, RecyclerView.ViewHolder>(COMPARATOR){

    private val TAG = AllPlacesAdapter::class.java.simpleName
    var day: Int= Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.i(TAG,"itemss: ${getItem(position)}")
        bindData(getItem(position),holder as RviewHolder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RviewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_all_places, parent, false))
    }


    private fun bindData(item: FetchPlacesResponseItem?, holder: RviewHolder) {
        holder.name.text = item?.name?.en
        Utils.stickImage(myContext,holder.image,item?.main_image_url,null)
        item?.timings?.getOrNull(day-1)?.opentime?.let {
            holder.timing.text = "${item?.timings?.get(day-1)?.opentime} - ${
                item?.timings?.get(day-1)?.closetime
            }"
        }?:run{
            holder.timing.text = "Timing not available..."
        }

        item?.rating?.let {
            holder.llRate.visibility = View.VISIBLE
            holder.rate.rating = it
            holder.ratetext.text = it.toString().plus(" of 5")
        }?:run{
            holder.llRate.visibility = View.INVISIBLE
        }

//        if(item?.is_bookable == true){
//            holder.bookTable.visibility = View.VISIBLE
//        }
        holder.root.onClick({
            var baseFragment : BaseFragment = RestaurantDetailFragment();
            Navigation.setFragmentData(baseFragment,"_id",item?._id)
            Navigation.addFragment(myContext,baseFragment,
                RestaurantDetailFragment.TAG,R.id.homeFram,true,false)
        } )
        RestaurantDetailFragment.TAG
        holder.meetUp.onClick({
            var baseFragment: BaseFragment = MeetUpViewPageFragment.getInstance(item?._id,true)
            Navigation.addFragment(
                    myContext,
                    baseFragment,
                MeetUpViewPageFragment.TAG,
                    R.id.homeFram,
                    true,
                    true
                                  )
        })

        if(item?.kind_of_places?.size?.compareTo(0) == 1){
            var kindPlaces = StringBuilder()
            item.kind_of_places?.map { key->
                PreferencesManager.get<CategoryResponse>(PREFRENCE_CATEGORY)?.definition?.map {
                    if(it.key == key){
                        if (kindPlaces.isEmpty()){
                            kindPlaces.append(it.en)
                        }else{
                            kindPlaces.append(", ").append(it.en)
                        }
                    }
                }
            }
            holder.type.text = kindPlaces.toString()
        }
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name = itemView.findViewById<TextView>(R.id.name)
        var timing = itemView.findViewById<TextView>(R.id.time)
        var type = itemView.findViewById<TextView>(R.id.type)
        var image = itemView.findViewById<ImageView>(R.id.image)
        var rate = itemView.findViewById<RatingBar>(R.id.rb_rate_start)
        var ratetext = itemView.findViewById<TextView>(R.id.rate_text)
        var llRate = itemView.findViewById<LinearLayout>(R.id.ll_rate)
        var bookTable = itemView.findViewById<TextView>(R.id.tv_book_table)
        var meetUp = itemView.findViewById<TextView>(R.id.tv_meet_up)
        var root = itemView.findViewById<CardView>(R.id.rootCo)
    }



    companion object{
        private val COMPARATOR = object : DiffUtil.ItemCallback<FetchPlacesResponseItem>(){
            override fun areItemsTheSame(
                oldItem: FetchPlacesResponseItem,
                newItem: FetchPlacesResponseItem
            ): Boolean {
                return (oldItem._id == newItem._id)
            }

            override fun areContentsTheSame(
                oldItem: FetchPlacesResponseItem,
                newItem: FetchPlacesResponseItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


}