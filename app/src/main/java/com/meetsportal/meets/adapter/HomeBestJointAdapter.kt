package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import org.json.JSONObject

class HomeBestJointAdapter(
    var myContext: Activity,
    var places: FetchPlacesResponse?,
    var click: (Int) -> Unit
) : RecyclerView.Adapter<HomeBestJointAdapter.RviewHolder>() {

    private val TAG = HomeBestJointAdapter::class.java.simpleName

    var myOptions: RequestOptions = RequestOptions().override(400, 400)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_best_joints, parent, false)
    )

    override fun getItemCount(): Int {
        if (places != null)
            return places?.size!!
        else
            return 0
    }

    override fun onBindViewHolder(holder: HomeBestJointAdapter.RviewHolder, position: Int) {

        var data = places?.get(position)
        holder.itemView.setOnClickListener {
            click(position)
        }
        /*places[position].mainImageUrl?.let {
            if (it.contains("firebasestorage")) {
                Utils.stickImage(context,holder.image,it,myOptions)
            } else
                FirebaseStorage.getInstance()
                    .getReference(it).downloadUrl.addOnCompleteListener { it1 ->
                        if (it1.isSuccessful) {
                            Log.i(TAG, " restaurant :: ${it1.result}")
                            Utils.stickImage(context,holder.image,it1.result.toString(),myOptions)
                            places[position].mainImageUrl = it1.result.toString()
                        }
                    }
        }*/

        /*places?.get(position)?.main_image_url.let {
            Utils.stickImage(myContext,holder.image,it,null)
        }*/
        holder.image.loadImage(myContext,places?.get(position)?.main_image_url)
        holder.image.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcPlacesCloseBy, JSONObject(mapOf("placeId" to data?._id)))
            var baseFragment : BaseFragment = RestaurantDetailFragment();
            Navigation.setFragmentData(baseFragment,"_id",places?.get(position)?._id)
            Navigation.addFragment(myContext,baseFragment,
                RestaurantDetailFragment.TAG,R.id.homeFram,true,false)
        }

        //holder.rate.text = data.ratingAverage.toString()
        holder.rate.visibility = View.GONE

        holder.titleName.text = data?.name?.en!!
        data.timings?.get(0)?.opentime?.let {
            holder.time.text =
                "open: ${data.timings?.get(0)?.opentime} - ${data.timings?.get(0)?.closetime}"
        }?:run {
            holder.time.visibility = View.GONE
        }

        //holder.time =

    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //var mainImage = itemView.findViewById<>(R.id.mainImage)
        var image = itemView.findViewById<ImageView>(R.id.iv_bg_card)
        var titleName = itemView.findViewById<TextView>(R.id.tv_name)
        var rate = itemView.findViewById<TextView>(R.id.tv_rate)
        var time = itemView.findViewById<TextView>(R.id.tv_time)
        var tyope = itemView.findViewById<TextView>(R.id.tv_type)
    }

}

