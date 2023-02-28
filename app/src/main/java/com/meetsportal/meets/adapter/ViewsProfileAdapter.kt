package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.fenchtose.tooltip.Tooltip
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.Utils

class ViewsProfileAdapter(var myContext: Activity, var counter:Int,val notiRoot : ViewGroup, var clicked : () ->Unit = {}) :
    RecyclerView.Adapter<ViewsProfileAdapter.RviewHolder>() {

    private val TAG = ViewsProfileAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_view_profile, parent, false),clicked
    )
    fun setCount(count:Int){
        this.counter=count
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        holder.rvPerson.adapter=NewVisitPeopleAdapter(myContext,4)
        holder.tvUsers.text="$counter new users visited your profile"
    }

    /*fun setData(list:List<Contact>){
        this.list = list
        notifyDataSetChanged()
    }*/

    override fun getItemCount(): Int {
        return if(counter>0) 1 else 0
    }

    inner class RviewHolder(itemView: View, clicked: ()->Unit) : RecyclerView.ViewHolder(itemView) {
        val rvPerson: RecyclerView =itemView.findViewById(R.id.rvPerson)
        val tvUsers: TextView =itemView.findViewById(R.id.tvUsers)
        val rootCo =itemView.findViewById<CardView>(R.id.rootCo)
        val tvView: TextView =itemView.findViewById<TextView>(R.id.tvView).apply {
            Utils.showToolTips(myContext,this,notiRoot, Tooltip.BOTTOM,"View people who visited your profile","tvView"){
            }
        }
        //val tvView = TextView
        var root = itemView.apply {
            setOnClickListener {
                clicked()
            }
        }
    }
}