package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils

class TextPostBGAdapter (var myContext: Activity,var gradArray: Array<Constant.TextGrad>, var position : (Int)->Unit) :
    RecyclerView.Adapter<TextPostBGAdapter.RviewHolder>() {

    private val TAG = TextPostBGAdapter::class.java.simpleName
    /*val gradArray : Array<Int> = arrayOf(
        R.drawable.grad1,
        R.drawable.grad2,
        R.drawable.grad3,
        R.drawable.grad4,
        R.drawable.grad5,
        R.drawable.grad6,
        R.drawable.grad7,
        R.drawable.grad8,
        R.drawable.grad9,
    )*/


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_post_grad, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        holder.cardBg.setImageDrawable(Utils.gradientFromColor(gradArray.get(position).gradient))
        holder.root.setOnClickListener {
            position(position)
        }
    }


    override fun getItemCount(): Int {
        //return myContext.resources.getIntArray(R.array.text_post_bg).size
        return gradArray.size
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cardBg = itemView.findViewById<ImageView>(R.id.ivCardGradient)
        var root = itemView.findViewById<CardView>(R.id.rootCo)
    }
}