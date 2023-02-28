package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.places.FetchPlacesResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import org.json.JSONObject
import java.util.*

class BestMeetInitCrousalAdapter(var context: Activity, var places: FetchPlacesResponse?) :
    RecyclerView.Adapter<BestMeetInitCrousalAdapter.RviewHolder>() {

    private val TAG = BestMeetInitCrousalAdapter::class.java.simpleName

    var myOptions: RequestOptions = RequestOptions()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_home_crousal, parent, false)
    )

    override fun getItemCount(): Int {
        if (places != null)
            return places?.size!!
        else
            return 0
    }

    override fun onBindViewHolder(holder: BestMeetInitCrousalAdapter.RviewHolder, position: Int) {
        places?.getOrNull(position)?.main_image_url.let {
            Utils.stickImage(context,holder.bg_card,it,null)
        }
        holder.root.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcPlacesBestJoints, JSONObject(mapOf("placeId" to places?.get(position)?._id)))
            var baseFragment : BaseFragment = RestaurantDetailFragment();
            Navigation.setFragmentData(baseFragment,"_id",places?.get(position)?._id)
            Navigation.addFragment(context,baseFragment,RestaurantDetailFragment.TAG,R.id.homeFram,true,false)

        }
        holder.name.text = places?.getOrNull(position)?.name?.en
        var day: Int= Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        places?.getOrNull(position)?.timings?.get(day-1)?.opentime?.let {
            holder.discount.text = "${places?.getOrNull(position)?.timings?.getOrNull(day-1)?.opentime} - ${
                places?.getOrNull(position)?.timings?.getOrNull(day-1)?.closetime
            }"
        }?:run{
            holder.discount.visibility = View.GONE
        }

        places?.getOrNull(position)?.rating?.let {
            holder.rate.visibility = View.VISIBLE
            holder.tvRate.visibility = View.VISIBLE
            holder.rate.rating = it
            holder.tvRate.text = it.toString().plus(" of 5")

        }?: run {
            holder.rate.visibility = View.GONE
            holder.tvRate.visibility = View.GONE
        }

        /*if(places?.get(position)?.is_meetable == true){
            holder.meetable.visibility = View.VISIBLE
        }else{
            holder.meetable.visibility = View.INVISIBLE
        }*/
        holder.meetable.visibility = View.INVISIBLE




    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var bg_card: ImageView = itemView.findViewById(R.id.iv_bg_card)
        var name: TextView = itemView.findViewById(R.id.tv_name)
        var discount: TextView = itemView.findViewById(R.id.tv_discount)
        var tvRate: TextView = itemView.findViewById(R.id.tv_rate)
        var rate: RatingBar = itemView.findViewById(R.id.rb_rate_start)
        var meetable: ImageView = itemView.findViewById(R.id.meetable)
        var root: ConstraintLayout = itemView.findViewById(R.id.cl_root)
    }

}