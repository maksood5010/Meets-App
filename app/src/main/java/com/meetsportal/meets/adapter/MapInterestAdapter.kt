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
import com.meetsportal.meets.networking.profile.Definition
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.iconTop
import com.meetsportal.meets.utils.onClick
import de.hdodenhof.circleimageview.CircleImageView

class MapInterestAdapter (var myContext: Context, var interests: List<Definition?>?,var clicked : (Int?)->Unit):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG = MapInterestAdapter::class.simpleName
    companion object{
        const val normal=0
        const val more=1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType== normal)
        return RviewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_map_filter,parent,false))
        else
        return MoreViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_map_filter,parent,false))
    }
    override fun getItemViewType(position: Int): Int {
        return when(position) {
            interests?.lastIndex -> more
            else                 -> normal
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        /*Utils.stickSvg(myContext,holder.icon,interests?.get(position)?.svg_url,
            ContextCompat.getColor(myContext,R.color.white))*/
        /*holder.intrestName.text = interests?.get(position)?.en
        Log.i(TAG," checkingsvg::: ${interests?.get(position)?.key} ${interests?.get(position)?.selected}")
        if(interests?.get(position)?.selected == true){
            Utils.stickSvg(myContext,holder.icon,interests?.get(position)?.svg_url,
                ContextCompat.getColor(myContext,R.color.primaryDark))
            var bg = ContextCompat.getDrawable(myContext,R.drawable.bg_circular_background_primary_selected) as GradientDrawable
            holder.backColor.setBackgroundDrawable(bg)
        }else{
            Utils.stickSvg(myContext,holder.icon,interests?.get(position)?.svg_url,
                ContextCompat.getColor(myContext,R.color.white))
            var bg = ContextCompat.getDrawable(myContext,R.drawable.bg_circular_background_primary) as GradientDrawable
            holder.backColor.setBackgroundDrawable(bg)
        }
        holder.icon.setOnClickListener {
            if(interests?.get(position)?.selected == true){
                interests?.get(position)?.selected = false
                Utils.stickSvg(myContext,holder.icon,interests?.get(position)?.svg_url,
                    ContextCompat.getColor(myContext,R.color.white))
                var bg = ContextCompat.getDrawable(myContext,R.drawable.bg_circular_background_primary) as GradientDrawable
                holder.backColor.setBackgroundDrawable(bg)
            }else{
                interests?.get(position)?.selected = true
                Utils.stickSvg(myContext,holder.icon,interests?.get(position)?.svg_url,
                    ContextCompat.getColor(myContext,R.color.primaryDark))
                var bg = ContextCompat.getDrawable(myContext,R.drawable.bg_circular_background_primary_selected) as GradientDrawable
                holder.backColor.setBackgroundDrawable(bg)
            }
            //interests?.get(position)?.selected  = interests?.get(position)?.selected == false
            clicked(interests?.get(position))
        }*/
        if(holder is RviewHolder){
            holder.intrestName.text = interests?.get(position)?.en
            Log.i(TAG, " checkingsvg::: ${interests?.get(position)?.key} ${interests?.get(position)?.selected}")
            if(interests?.get(position)?.selected == true) {
                Utils.stickOGSvg(myContext, interests?.get(position)?.color_svg_url, holder.icon)
                holder.backColor.setImageResource(R.drawable.bg_map_interest_filter_selected)
            } else {
                Log.i(TAG, " colorSvg: ${interests?.get(position)}")
                Log.i(TAG, " colorSvg: ${interests}")
                Utils.stickOGSvg(myContext, interests?.get(position)?.color_svg_url, holder.icon)
                holder.backColor.setImageResource(R.drawable.bg_map_interest_filter_unselected)
            }
            holder.backColor.onClick({
                if(interests?.get(position)?.selected == true) {
                    //interests?.get(position)?.selected = false
                    //Utils.stickOGSvg(myContext, interests?.get(position)?.color_svg_url, holder.icon)
                    //holder.backColor.setImageResource(R.drawable.bg_map_interest_filter_unselected)
                } else {
                    //interests?.get(position)?.selected = true
                    //Utils.stickOGSvg(myContext, interests?.get(position)?.color_svg_url, holder.icon)
                    //holder.backColor.setImageResource(R.drawable.bg_map_interest_filter_selected)
                }
                clicked(position)
            })
        }else if(holder is MoreViewHolder) {
            holder.backColor.setImageResource(R.drawable.bg_more_intrest)
            holder.intrestName.text = "More…"
            holder.intrestName.iconTop(R.drawable.ic_plus_sign)
            holder.icon.visibility = View.GONE
            holder.backColor.onClick({
                clicked(null)
            })
        }

    }

    override fun getItemCount(): Int {
        return interests?.size?:0
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
    inner class MoreViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        var backColor = itemView.findViewById<CircleImageView>(R.id.backColor).apply {
            /*setImageResource(R.color.primaryDark)
            borderWidth = 5
            borderColor = ContextCompat.getColor(myContext,R.color.white)*/
        }
        var icon = itemView.findViewById<ImageView>(R.id.icon)
        var intrestName = itemView.findViewById<TextView>(R.id.tvInterest)
    }

}