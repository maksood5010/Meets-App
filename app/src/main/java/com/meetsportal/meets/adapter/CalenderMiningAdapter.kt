package com.meetsportal.meets.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.setRoundedColorBackground
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

class CalenderMiningAdapter(var context: Activity,val startDate: Calendar,var listener : (startDate: Calendar)-> Unit)
    : RecyclerView.Adapter<CalenderMiningAdapter.RviewHolder>() {

    var weekFormat = SimpleDateFormat("EEE", Locale.UK)
    var dayFormat = SimpleDateFormat("dd", Locale.UK)

    val next = Calendar.getInstance()
    private var lastCheckedPos = 0
    val TAG = CalenderMiningAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RviewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calender, parent, false))

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        if(lastCheckedPos == position) {
            holder.root.setRoundedColorBackground(context, R.color.primary, enbleDash = false, corner = 50f)
        } else {
            holder.root.setRoundedColorBackground(context, R.color.white, enbleDash = false, corner = 50f)
        }
        next.time = startDate.time
        next.add(Calendar.DAY_OF_MONTH, position)
        val date = Date(next.timeInMillis)
        val week = weekFormat.format(date)
        val day: String = dayFormat.format(date)
        holder.tvDay.text = week
        holder.tvDate.text = day

        holder.itemView.setOnClickListener {
            val copyOfLastCheckedPosition: Int = lastCheckedPos
            lastCheckedPos = holder.bindingAdapterPosition
            notifyItemChanged(copyOfLastCheckedPosition)
            notifyItemChanged(lastCheckedPos)
            listener(next)
        }

    }

    override fun getItemCount(): Int {
        return 15
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val root = itemView.findViewById<LinearLayout>(R.id.rootCo)
        var tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        var tvDay = itemView.findViewById<TextView>(R.id.tvDay)
    }
}