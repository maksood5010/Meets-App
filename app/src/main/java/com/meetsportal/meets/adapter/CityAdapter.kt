package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.onClick
import org.json.JSONArray

class CityAdapter(var context : Activity,var array: JSONArray,var click:(String)->Unit) : RecyclerView.Adapter<CityAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_city, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.city.text = array.getString(position)
        holder.city.onClick({
            click(array.getString(position))
        })
    }

    override fun getItemCount(): Int {
        return array.length()
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var city = itemView.findViewById<TextView>(R.id.city)

    }

}