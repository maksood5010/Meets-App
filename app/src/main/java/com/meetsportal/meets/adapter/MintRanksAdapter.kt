package com.meetsportal.meets.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R

class MintRanksAdapter(
    var context: Activity,
                      ) : RecyclerView.Adapter<MintRanksAdapter.RviewHolder>() {

    val TAG = MintRanksAdapter::class.simpleName
    val array = arrayOf<Boolean>(false, true, false, false, false, false, false, false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder {
        return RviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_top_leader, parent, false))
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
//        if(array[position]) {
//            holder.bottomBg.setRoundedColorBackground(context, corner = 0f, enbleDash = false, color = Color.parseColor("#42B05E"))
//        } else {
//            holder.bottomBg.setRoundedColorBackground(context, corner = 0f, enbleDash = false, color = Color.parseColor("#73D399"))
//        }
    }

    override fun getItemCount(): Int {
        return array.size
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}
