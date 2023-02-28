package com.meetsportal.meets.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.places.CuisineResponse

class CuisineFilterAdapter (var context: Context, var cuisineResponse: CuisineResponse?) :
    RecyclerView.Adapter<CuisineFilterAdapter.RviewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_place_filter, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        cuisineResponse?.definition?.get(position)?.let { category ->
            holder.optionText.text = category.en
            if(category.isSelected){
                holder.checkBox.setImageResource(R.drawable.ic_checked)
            }else{
                holder.checkBox.setImageResource(R.drawable.ic_filter_uncheck)
            }
            holder.option.setOnClickListener {
                if(category.isSelected){
                    cuisineResponse?.definition?.get(position)?.isSelected = false
                    holder.checkBox.setImageResource(R.drawable.ic_filter_uncheck)
                }else{
                    cuisineResponse?.definition?.get(position)?.isSelected = true
                    holder.checkBox.setImageResource(R.drawable.ic_checked)
                }
            }
        }

    }

    fun clearItem(){
        cuisineResponse?.definition?.map { it.isSelected = false }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        cuisineResponse?.definition?.let {
            return it.size
        }?:run {
            return 0
        }
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var option = itemView.findViewById<LinearLayout>(R.id.ll_option)
        var checkBox = itemView.findViewById<ImageView>(R.id.iv_checkbox)
        var optionText  = itemView.findViewById<TextView>(R.id.optionText)
    }

}