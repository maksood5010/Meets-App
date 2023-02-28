package com.meetsportal.meets.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TempRecyclerViewAdapter (var context: Context,var layoutg: Int,var clicked : () ->Unit = {}) :
    RecyclerView.Adapter<TempRecyclerViewAdapter.RviewHolder>() {

    private val TAG = TempRecyclerViewAdapter::class.java.simpleName
    var previousletter = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(layoutg, parent, false),clicked
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {


    }

    /*fun setData(list:List<Contact>){
        this.list = list
        notifyDataSetChanged()
    }*/

    override fun getItemCount(): Int {
        return 15
    }

    class RviewHolder(itemView: View, clicked: ()->Unit) : RecyclerView.ViewHolder(itemView) {
        var root = itemView.apply {
            setOnClickListener {
                clicked()
            }
        }
    }
}