package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.models.MeetsPlace
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment.MeetType
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment
import com.meetsportal.meets.utils.*

class MeetUpDateSelectedPlaceAdapter (var myContext: Activity, var map: MutableMap<String?, MeetsPlace?>?, var customPlace : ArrayList<AddressModel>, var meetType : MeetUpViewPageFragment.MeetType, var listener: (Int)->Unit, var click: () -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG = MeetUpDateSelectedPlaceAdapter::class.simpleName
    val Categories = PreferencesManager.get<CategoryResponse>(Constant.PREFRENCE_CATEGORY)?.definition

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) :RecyclerView.ViewHolder {
        if(viewType == APP_PLACE) {
            if (meetType == MeetType.OPEN) {
                return RviewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.card_open_meet_up_place_result, parent, false),
                    myContext
                )
            }
            return RviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_meet_up_place_result, parent, false),myContext
            )
        }
        else if(viewType == CUSTOM_PLACE){
            if (meetType == MeetType.OPEN) {
                return CustomPlaceViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.card_open_meet_up_custom_place_result, parent, false),myContext
                )
            }
            return return CustomPlaceViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_meet_up_custom_place_result, parent, false),myContext
            )
        }
        else
            return AddviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_meet_place_adder, parent, false)
            )
    }

    override fun getItemViewType(position: Int): Int {

        return when(position){
           // map?.size?.plus(customPlace.size) -> ADDER
            in 0..map?.size?.minus(1)!! -> APP_PLACE
            else -> CUSTOM_PLACE
        }
        /*map?.let {
            if(position == it.size) return ADDER
            return ITEM
        }*/
//        return ADDER
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
            is  AddviewHolder -> bindAdder(holder,position)
            is CustomPlaceViewHolder -> bindCustomPlace(holder,(position-(map?.size?:0)))

        }

    }

    fun bindItem(holder: RviewHolder, position: Int) {
        var key = map?.keys?.elementAt(position)
        var data = map?.get(key)
        Utils.stickImage(myContext,holder.image,data?.imageUrl,null)
        holder.name.text = data?.name?.en
        //holder.category.text = data?.primary_kind_place?.getOrNull(0)
        holder.category.text = Categories?.firstOrNull{ it.key == data?.primary_kind_place?.getOrNull(0)}?.en
        Log.i(TAG," data?.timing::: ${data?.timing}")
        data?.timing?.getOrNull(0)?.opentime?.let {
            holder.timing.visibility = View.VISIBLE
            holder.timing.text = "Open:".plus(data?.timing?.getOrNull(0)?.opentime).plus(" - ").plus(data?.timing?.getOrNull(0)?.closetime)
        }?:run{
            holder.timing.visibility =View.GONE
        }

        holder.address.text = data?.address
        holder.remove.onClick( {
            MyApplication.putTrackMP(Constant.AcCreateMeetupReviewPlaceRemove, null)
            if(meetType == MeetType.PRIVATE){
                map?.remove(key)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position,map?.size?.plus(customPlace.size)!!)
                listener(position)
            }else{
                click()
            }

        })

        holder.root.onClick( {
            MyApplication.putTrackMP(Constant.AcCreateMeetupReviewPlace, null)
            if(data?.isElastic == true){
                var baseFragment : BaseFragment = RestaurantDetailFragment();
                Navigation.setFragmentData(baseFragment,"_id",data.id)
                Navigation.setFragmentData(baseFragment,"isItElastic","yes")
                Navigation.addFragment(myContext,baseFragment,
                    RestaurantDetailFragment.TAG,R.id.homeFram,true,false)
            }else{
                var baseFragment : BaseFragment = RestaurantDetailFragment();
                Navigation.setFragmentData(baseFragment,"_id",data?.id)
                Navigation.addFragment(myContext,baseFragment,
                    RestaurantDetailFragment.TAG,R.id.homeFram,true,false)
            }
        })
    }

    fun bindAdder(holder: AddviewHolder, position: Int) {
        holder.root.setOnClickListener {
            click()
        }
    }

    fun bindCustomPlace(holder: CustomPlaceViewHolder, position: Int) {
        var data = customPlace.get(position)
        holder.name.text = data.name
        //holder.address.text = "CustomPlace\n".plus(data.address)
        holder.address.text = data.address
        holder.date.text = myContext.getString(R.string.saved_on, Utils.getCreatedAt(data.createdAt))
        holder.romove.setOnClickListener {
            Log.i(TAG," customPlaceSize::: ${position}")


            if(meetType == MeetType.PRIVATE){
                customPlace.removeAt(position)
                notifyItemRemoved(position.plus(map?.size?:0))
                notifyItemRangeChanged(position.plus(map?.size?:0),map?.size?.plus(customPlace.size)?.plus(1)!!)
                listener(position.plus(map?.size?:0))
            }else{
                click()
            }

        }

    }


    class RviewHolder(itemView: View,var  myContext: Activity) : RecyclerView.ViewHolder(itemView) {
        //var mainImage = itemView.findViewById<>(R.id.mainImage)
        var image = itemView.findViewById<ImageView>(R.id.image)
        var name = itemView.findViewById<TextView>(R.id.name)
        var category = itemView.findViewById<TextView>(R.id.category)
        var timing = itemView.findViewById<TextView>(R.id.timing)
        var address = itemView.findViewById<TextView>(R.id.address)
        var remove = itemView.findViewById<TextView>(R.id.romove)
        /*var card = itemView.findViewById<CardView>(R.id.card).apply {
            setRoundedColorBackground(myContext,enbleDash = true,strokeColor = R.color.gray1,stripSize = 20f,strokeHeight = 2f,Dashgap = 0f)
        }*/
        var root  = itemView.findViewById<ConstraintLayout>(R.id.rootCo).apply {
            //setRoundedColorBackground(myContext,enbleDash = true,strokeColor = R.color.gray1,stripSize = 20f,strokeHeight = 2f,Dashgap = 0f)
        }
    }

    class AddviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root  = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }

    class CustomPlaceViewHolder(itemView: View, myContext: Activity) : RecyclerView.ViewHolder(itemView) {
        var root  = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var name = itemView.findViewById<TextView>(R.id.name)
        var address = itemView.findViewById<TextView>(R.id.address)
        var date = itemView.findViewById<TextView>(R.id.date)
        var romove = itemView.findViewById<TextView>(R.id.romove)
        var bgImage = itemView.findViewById<ImageView>(R.id.image).apply {
            setImageResource(R.drawable.ic_custom_place_bg)
            /*this.setGradient(myContext,GradientDrawable.Orientation.TL_BR, intArrayOf(
                Color.parseColor("#FF7272"),
                Color.parseColor("#32BFC9"),
            ))*/
        }
    }

    companion object{
        val APP_PLACE = 0
        val ADDER = 1
        val CUSTOM_PLACE = 2

    }
}

