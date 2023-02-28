package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.profile.Definition
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground

class SelectedInterestAdapter(var myContext : Activity,var selected: ArrayList<Definition?>,val layoutType:Int = 0,var showCross: Boolean = true,var remove : ()->Unit={}) : RecyclerView.Adapter<SelectedInterestAdapter.RviewHolder>() {

    val TAG = SelectedInterestAdapter::class.simpleName
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder{
        return when(layoutType){
            0 -> RviewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.card_interest_item, parent, false),myContext,layoutType,showCross
            )
            1 -> RviewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.card_interest_item_two, parent, false),myContext,layoutType,showCross
            )
            else ->RviewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.card_interest_item, parent, false),myContext,layoutType,showCross
            )


        }
    }


    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        Log.i(TAG," interesttext:: ${selected.getOrNull(position)?.en}")
        holder.text.text = selected.getOrNull(position)?.en
        holder.cross.onClick({
            if(layoutType == 1){
                selected.removeAt(position)
                remove()
                notifyDataSetChanged()
            }

        })
    }

    override fun getItemCount(): Int {
        return selected.size
    }

    class RviewHolder(itemView: View, myContext: Activity, layoutType: Int, showCross: Boolean) :RecyclerView.ViewHolder(itemView){
        var root = itemView.findViewById<ConstraintLayout>(R.id.root).apply {
            if(layoutType == 0){
                setRoundedColorBackground(myContext,R.color.primaryDark)
            }
        }
        var text = itemView.findViewById<TextView>(R.id.tv_interest_item).apply {
            if(layoutType == 0){
                setTextColor(ContextCompat.getColor(myContext,R.color.white))
            }else if(layoutType == 1){
                setRoundedColorBackground(myContext,R.color.white,enbleDash = true,strokeHeight = 1f,Dashgap = 0f,stripSize = 10f,strokeColor = R.color.darkerGray)
            }

        }
        var cross = itemView.findViewById<ImageView>(R.id.cross).apply {
            if(showCross){
                visibility = View.VISIBLE
            }else{
                visibility = View.GONE
            }
            if(layoutType == 1){
                setRoundedColorBackground(myContext, R.color.darkerGray)
            }
        }
    }
}