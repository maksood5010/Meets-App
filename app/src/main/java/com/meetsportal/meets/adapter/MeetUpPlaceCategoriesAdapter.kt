package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.setGradient

class MeetUpPlaceCategoriesAdapter (var myContext: Activity, var list: CategoryResponse, var categoryClick: (String?,String?, Boolean) -> Unit,var customplace:(Int,Triple<Int,String,Boolean>?)->Unit) : RecyclerView.Adapter<MeetUpPlaceCategoriesAdapter.RviewHolder>() {

    private val TAG = HomeCategoriesAdapter::class.java.simpleName

    var start3 : ArrayList<Triple<Int,String,Boolean>>  = arrayListOf(
        Triple(R.drawable.ic_trending,"Trending Place",true),
        Triple(R.drawable.ic_location,"Custom location",false),
        Triple(R.drawable.ic_bookmarked,"Saved Places",false),

    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_meetup_place_category, parent, false)

    )

    override fun getItemCount(): Int {
        return (list.definition?.size?:0).plus(3)
    }

    override fun onBindViewHolder(holder: RviewHolder, pos: Int) {
        var position=pos
        val selected = R.drawable.horizontal_round_shape
        val unSelected = ContextCompat.getColor(myContext, R.color.white)
//        if(position==1){
//            Utils.showToolTips(myContext,holder.root, rootCo, Tooltip.BOTTOM,"This is the total of your mined mints and mint cash. Click to view the leaderboard"){
//            }
//        }
        if(position ==0|| (position==list.definition?.size?.plus(1))||(position==list.definition?.size?.plus(2))){
            if(position==list.definition?.size?.plus(1)){
                position=1
            }else if(position==list.definition?.size?.plus(2)){
                position=2
            }
            holder.image.setImageResource(start3.getOrNull(position)?.first?:R.drawable.ic_trending)
            holder.image.setColorFilter(Color.argb(255, 0, 0, 0))
            holder.titleName.text = start3.getOrNull(position)?.second
            if(start3.get(position)?.third == true) {
                holder.root.setBackgroundResource(selected)
//                holder.titleName.setTextColor(black)
            } else {
                holder.root.setGradient(myContext, GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.parseColor("#32BFC9"), Color.parseColor("#06DFEF")), 30f)
//                holder.root.setBackgroundResource(R.drawable.round_border_primary_bg)
//                holder.titleName.setTextColor(white)
            }
            holder.root.setOnClickListener {
                list.definition?.map { it.isSelected = false }
                var temp = start3.map { Triple(it.first,it.second,false) }
                start3.clear()
                start3.addAll(temp)
                if(start3.getOrNull(position)?.third == false){
                    start3.set(position, Triple(start3.get(position).first,start3.get(position).second,true))
                    customplace(position,start3.getOrNull(position))
                }else{
                    start3.set(position, Triple(start3.get(position).first,start3.get(position).second,false))
                    customplace(position,start3.getOrNull(position))
                }
                if(position==0){
                    MyApplication.putTrackMP(Constant.AcTrendingPlaces,null)
                }else if(position==1){
                    MyApplication.putTrackMP(Constant.AcCustomPlaces,null)
                }else if(position==2){
                    MyApplication.putTrackMP(Constant.AcSavedPlaces,null)
                }
                notifyDataSetChanged()
            }
        }else{
            holder.image.setColorFilter(null)
            var data = list.definition?.getOrNull(position-1)
            Utils.stickOGSvg(myContext,data?.color_svg_url,holder.image)
            holder.titleName.text = data?.en
            //holder.titleName.setGravity(Gravity.CENTER_HORIZONTAL)
            if(data?.isSelected == true) {
                holder.root.setBackgroundResource(selected)
//                holder.titleName.setTextColor(black)
            } else {
                holder.root.setGradient(myContext, GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(Color.parseColor("#32BFC9"), Color.parseColor("#06DFEF")), 30f)
//                holder.titleName.setTextColor(white)
            }

            holder.root.setOnClickListener {
                if(data?.isSelected == false){
                var temp = start3.map { Triple(it.first,it.second,false) }
                start3.clear()
                start3.addAll(temp)
                if(data?.isSelected == false){
                    list.definition?.map { it.isSelected = false }
                    data?.isSelected = true
                    categoryClick(data?.en,data?.key,true)
                }else{
                    data?.isSelected = false
                    categoryClick(data?.en,data?.key,false)
                }
                    MyApplication.putTrackMP("clicked_on_${data.key}_category_button",null)
                    notifyDataSetChanged()
                }
            }
        }
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //var mainImage = itemView.findViewById<>(R.id.mainImage)
        var image = itemView.findViewById<ImageView>(R.id.icon)
        var titleName = itemView.findViewById<TextView>(R.id.place_name).apply {
            setGravity(Gravity.CENTER_HORIZONTAL)
        }
        var root  = itemView.findViewById<LinearLayout>(R.id.rootCo)
    }

}

