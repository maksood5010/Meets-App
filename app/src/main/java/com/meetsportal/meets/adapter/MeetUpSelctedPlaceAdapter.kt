package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.models.MeetsPlace
import com.meetsportal.meets.networking.places.Category
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage

class MeetUpSelctedPlaceAdapter (var myContext: Activity, var map: MutableMap<String?, MeetsPlace?>?, var customPlace : ArrayList<AddressModel>, var listener: (Int)->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG = MeetUpSelctedPlaceAdapter::class.simpleName


    var categories : List<Category>?
    init {
        categories = PreferencesManager.get<CategoryResponse>(Constant.PREFRENCE_CATEGORY)?.definition
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : RecyclerView.ViewHolder {
        if(viewType == APP_PLACE)
            return RviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_trending_place, parent, false)
            )
        else if(viewType == CUSTOM_PLACE){
            return return CustomPlaceViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_meet_custom_address, parent, false)
            )
        }
        else
            return AddviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_trending_place, parent, false)
            )
    }

    override fun getItemViewType(position: Int): Int {

        return when(position){
            map?.size?.plus(customPlace.size) -> ADDER
            in 0..map?.size?.minus(1)!! -> APP_PLACE
            else -> CUSTOM_PLACE
        }
        /*map?.let {
            if(position == it.size) return ADDER
            return ITEM
        }*/
        return ADDER
    }

    override fun getItemCount(): Int {
        var size = map?.size?.plus(customPlace.size)

        /*size?.let{
            return it.plus(1)
        }*/


        return size?:0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position:  Int) {

        Log.i(TAG," onBindViewHolder:: $position -- ${map?.size?.plus(customPlace.size)}")
        when(holder){
            is RviewHolder -> bindItem(holder,position)
            //is  AddviewHolder -> bindAdder(holder,position)
            is CustomPlaceViewHolder -> bindCustomPlace(holder,(position-(map?.size?:0)))

        }

    }

    fun bindItem(holder: RviewHolder, position: Int) {
        var key = map?.keys?.elementAt(position)
        var data = map?.get(key)

        Utils.stickImage(myContext,holder.image,data?.imageUrl,null)
        data?.rating?.let {
            holder.tvRating.visibility = View.VISIBLE
            holder.ivStar.visibility = View.VISIBLE

            holder.tvRating.text = "".plus(it).plus("/5.0")
        }?:run{
            holder.tvRating.visibility = View.INVISIBLE
            holder.ivStar.visibility = View.INVISIBLE
        }
        if(data?.timing?.isNotEmpty() == true && data?.timing?.get(0)?.opentime != null){
            holder.timing.visibility = View.VISIBLE
            holder.timing.text = "Open: ".plus(data?.timing?.get(0)?.opentime).plus(" - ").plus(data?.timing?.get(0)?.closetime)
        }else{
            //holder.timing.visibility = View.INVISIBLE
            holder.timing.text = "Timing not available"
        }

        if(data?.address?.isNotEmpty() == true){
            holder.address.visibility = View.VISIBLE
            holder.address.text = data?.address
        }else{
            holder.address.visibility = View.INVISIBLE
        }
        holder.name.text = data?.name?.en
        categories?.let {
            categories?.firstOrNull() { it.key.equals(data?.primary_kind_place?.get(0)) == true }?.let {
                holder.tvCategory.text = it.en
                Utils.stickOGSvg(myContext,it.color_svg_url,holder.catIcon)
            }
        }


        holder.remove.setOnClickListener {
            map?.remove(key)
            notifyItemRemoved(position)

            listener(position)
        }

        holder.root.setOnClickListener {
            if(data?.isElastic == true){
                var baseFragment : BaseFragment = RestaurantDetailFragment();
                Navigation.setFragmentData(baseFragment,"_id",data.id)
                Navigation.setFragmentData(baseFragment,"isItElastic","yes")
                Navigation.addFragment(myContext,baseFragment,
                    RestaurantDetailFragment.TAG, R.id.homeFram,true,false)
            }else{
                var baseFragment : BaseFragment = RestaurantDetailFragment();
                Navigation.setFragmentData(baseFragment,"_id",data?.id)
                Navigation.addFragment(myContext,baseFragment,
                    RestaurantDetailFragment.TAG, R.id.homeFram,true,false)
            }
        }
    }

   /* fun bindAdder(holder: AddviewHolder, position: Int) {
        holder.root.setOnClickListener {
            click()
        }
    }*/


    fun bindCustomPlace(holder: CustomPlaceViewHolder, position: Int) {
        var data = customPlace.get(position)
        holder.tvPLaceDate.visibility = View.VISIBLE
        if (data.image_url.isNullOrEmpty()) {
            holder.image.visibility = View.GONE
            holder.cvTextView.visibility = View.VISIBLE
            val firstLetter: String = data.name?.substring(0, 1) ?: return
            holder.tvDrawableText.text = firstLetter
        } else {
            holder.image.visibility = View.VISIBLE
            holder.cvTextView.visibility = View.GONE
            holder.image.loadImage(myContext, data.image_url, R.drawable.ic_direction)
        }
        holder.tvPlaceName.text = data.name
        holder.tvPLaceAddress.text = data.address
        holder.tvPLaceDate.text = myContext.getString(R.string.saved_on, Utils.getCreatedAt(data.createdAt))



        holder.remove.setOnClickListener {
            Log.i(TAG," customPlaceSize::: ${position}")
            customPlace.removeAt(position)
            notifyItemRemoved(position.plus(map?.size?:0))
            notifyItemRangeChanged(position.plus(map?.size?:0),map?.size?.plus(customPlace.size)?.plus(1)!!)
            listener(position.plus(map?.size?:0))
        }

    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image = itemView.findViewById<ImageView>(R.id.iv_image)
        var catIcon = itemView.findViewById<ImageView>(R.id.cat_icon)
        var check = itemView.findViewById<ImageView>(R.id.checkbox)
        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var tvRating = itemView.findViewById<TextView>(R.id.tv_rate)
        var ivStar = itemView.findViewById<ImageView>(R.id.iv_star)
        var tvCategory = itemView.findViewById<TextView>(R.id.tv_categories)
        var timing = itemView.findViewById<TextView>(R.id.timing)
        var address = itemView.findViewById<TextView>(R.id.address)
        var remove = itemView.findViewById<ImageView>(R.id.remove).apply{
            visibility =View.VISIBLE
        }
        var rlCheck = itemView.findViewById<RelativeLayout>(R.id.rl_check).apply {
            visibility = View.GONE
        }
        var root  = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }

    class AddviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root  = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }

    class CustomPlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root : LinearLayout = itemView.findViewById(R.id.rootCo)
        var image: ImageView = itemView.findViewById(R.id.ivPlace)
        var cvTextView: CardView = itemView.findViewById(R.id.cvTextView)
        var tvDrawableText: TextView = itemView.findViewById(R.id.tvDrawableText)
        var rvCheckbox: ImageView = itemView.findViewById<ImageView>(R.id.rvCheckbox).apply{
            visibility = View.GONE
        }
        var remove = itemView.findViewById<ImageView>(R.id.remove).apply {
            visibility = View.VISIBLE
        }
        var tvPlaceName: TextView = itemView.findViewById(R.id.tvPlaceName)
        var tvPLaceAddress: TextView = itemView.findViewById(R.id.tvPLaceAddress)
        var tvPLaceDate: TextView = itemView.findViewById(R.id.tvPLaceDate)
    }

    companion object{
        val APP_PLACE = 0
        val ADDER = 1
        val CUSTOM_PLACE = 2

    }
}

