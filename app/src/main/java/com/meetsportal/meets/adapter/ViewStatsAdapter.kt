package com.meetsportal.meets.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.PricingModelItem
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setGradient

class ViewStatsAdapter(
    var context: Activity,val list:ArrayList<PricingModelItem>,
    var skuDetailss : (Int)->Unit
                      ) : RecyclerView.Adapter<ViewStatsAdapter.RviewHolder>() {


    val TAG = ViewStatsAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder {
        return RviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_boost_package, parent, false))
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        holder.bottomBg.setGradient(context, GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#FF660D"), Color.parseColor("#FEC200")), 1f)
        val item = list?.get(position)
        holder.itemView.onClick({
            skuDetailss(position)
        },500)
        holder.tvMint.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20f)
        holder.tvMint.text="${item.label}"
        if(item.discount==0){
            holder.tvPriceOffer.visibility=View.INVISIBLE
        }else{
            holder.tvPriceOffer.visibility=View.VISIBLE
            holder.tvPriceOffer.text="Save ${item.discount} %"
        }
        holder.tvPrice.text="${item.amount} mints"
        holder.tvCharges.text="Duration"

        if(position==1){
            holder.itemView.callOnClick()
        }
    }

    override fun getItemCount(): Int {
        return list?.size?:0
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var bottomBg = itemView.findViewById<LinearLayout>(R.id.bottomBg)
        var tvPrice = itemView.findViewById<TextView>(R.id.tvPrice)
        var tvPriceOffer = itemView.findViewById<TextView>(R.id.tvPriceOffer)
        var tvMint = itemView.findViewById<TextView>(R.id.tvMint)
        var tvCharges = itemView.findViewById<TextView>(R.id.tvCharges)
        var cvMintPackage = itemView.findViewById<CardView>(R.id.cvMintPackage)
    }
}
