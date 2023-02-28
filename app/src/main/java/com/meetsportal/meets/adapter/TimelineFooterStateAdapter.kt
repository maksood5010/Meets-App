package com.meetsportal.meets.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R

class TimelineFooterStateAdapter(private val retry: () -> Unit,): LoadStateAdapter<TimelineFooterStateAdapter.TimeLineFooterViewHolder>() {

    override fun onBindViewHolder(holder: TimeLineFooterViewHolder, loadState: LoadState) {
        populate(holder,loadState)
    }

    private fun populate(
        holder: TimeLineFooterViewHolder,
        loadState: LoadState
    ) {
        Log.i("TAG"," footerLoderChecking:: ${loadState}")
        if(loadState is LoadState.Error){
            //holder.errorMsg.text = "Something went wrong..."
        }
        if(loadState is LoadState.Loading){
            holder.progress.visibility = View.VISIBLE
            holder.btRetry.visibility = View.GONE
            holder.errorMsg.visibility = View.GONE
            holder.retryCard.visibility = View.GONE

        }else{
            holder.progress.visibility = View.GONE
            holder.btRetry.visibility = View.VISIBLE
            holder.errorMsg.visibility = View.VISIBLE
            holder.retryCard.visibility = View.VISIBLE
        }
        /*holder.progress.isVisible = loadState is LoadState.Loading
        holder.btRetry.isVisible = loadState !is LoadState.Loading
        holder.errorMsg.isVisible = loadState !is LoadState.Loading*/
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): TimeLineFooterViewHolder {
        return TimeLineFooterViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.footer_state, parent, false),retry
        )
    }

    class TimeLineFooterViewHolder(itemView : View,retry: () -> Unit) :RecyclerView.ViewHolder(itemView){
        var btRetry = itemView.findViewById<TextView>(R.id.bt_retry).apply {
            setOnClickListener {
                retry.invoke()
            }
        }
        var errorMsg = itemView.findViewById<TextView>(R.id.tv_errorMsg)
        var progress = itemView.findViewById<ProgressBar>(R.id.progress)
        var retryCard = itemView.findViewById<CardView>(R.id.retrycard)
        var root  = itemView.findViewById<LinearLayout>(R.id.rootCo)
    }
}