package com.meetsportal.meets.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.places.Category
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import de.hdodenhof.circleimageview.CircleImageView

class MapCategotyAdapter(var myContext: Context, var list : List<Category>?,var clicked : (Category?)->Unit) :
    RecyclerView.Adapter<MapCategotyAdapter.RviewHolder>(){

    val TAG = MapCategotyAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)  =
        RviewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.card_map_filter, parent, false)
        )

    override fun onBindViewHolder(holder: MapCategotyAdapter.RviewHolder, position: Int) {
        holder.intrestName.text = list?.get(position)?.en
        Log.i(TAG," checkingsvg::: ${list?.get(position)?.key} ${list?.get(position)?.isSelected}")
        if(list?.get(position)?.isSelected == true){
            Utils.stickOGSvg(myContext,list?.get(position)?.color_svg_url,holder.icon)
            holder.backColor.setImageResource(R.drawable.bg_map_filter_selected)
        }else{
            Utils.stickOGSvg(myContext,list?.get(position)?.color_svg_url,holder.icon)
            holder.backColor.setImageResource(R.drawable.bg_map_filter_unselected)
        }
        holder.backColor.setOnClickListener {
            if(list?.get(position)?.isSelected == true){
                list?.get(position)?.isSelected = false
                Utils.stickOGSvg(myContext,list?.get(position)?.color_svg_url,holder.icon)
                holder.backColor.setImageResource(R.drawable.bg_map_filter_unselected)
            }else{
                list?.get(position)?.isSelected = true
                Utils.stickOGSvg(myContext,list?.get(position)?.color_svg_url,holder.icon)
                holder.backColor.setImageResource(R.drawable.bg_map_filter_selected)
            }
            clicked(list?.get(position))
        }
    }


    override fun getItemCount(): Int {
        return PreferencesManager.get<CategoryResponse>(Constant.PREFRENCE_CATEGORY)?.definition?.size ?: 0
    }


    inner class RviewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        var backColor = itemView.findViewById<CircleImageView>(R.id.backColor).apply {
            /*setImageResource(R.color.primaryDark)
            borderWidth = 5
            borderColor = ContextCompat.getColor(myContext,R.color.white)*/
        }
        var icon = itemView.findViewById<ImageView>(R.id.icon)
        var intrestName = itemView.findViewById<TextView>(R.id.tvInterest)
    }
}