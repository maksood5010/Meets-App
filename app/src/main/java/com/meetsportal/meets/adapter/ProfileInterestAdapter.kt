package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.profile.Definition
import com.meetsportal.meets.utils.setRoundedColorBackground

class ProfileInterestAdapter (var myContext: Activity, var interests: ArrayList<Definition?>?)
    : RecyclerView.Adapter<ProfileInterestAdapter.RviewHolder>(){

   // val differ: AsyncListDiffer<Definition?>
    val  differ: AsyncListDiffer<Definition?> = AsyncListDiffer<Definition?>(this, DIFF_CALLBACK)
    init {
        differ.submitList(interests)
    }

   // val differ: AsyncListDiffer<Definition?> = AsyncListDiffer<Definition?>(this, DIFF_CALLBACK)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.card_interest_item, parent, false),myContext
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        val data = differ.currentList.getOrNull(position)
        data?.let {
            holder.text.setText(it.en)
        }
        /*interests?.get(position)?.let {
            holder.text.setText(it.en)
        }*/

    }

    override fun getItemCount()=  differ.currentList.size


    fun setInterest(list :ArrayList<Definition?>){
        differ.submitList(list)
    }


    class RviewHolder(itemView: View, myContext: Activity) :RecyclerView.ViewHolder(itemView) {
        var text = itemView.findViewById<TextView>(R.id.tv_interest_item).apply{
            setRoundedColorBackground(myContext,R.color.transparent,enbleDash = true,strokeHeight = 1f,Dashgap = 0f,stripSize = 20f,strokeColor = R.color.primaryDark)
        }
    }

    companion object {

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Definition?>() {
            override fun areItemsTheSame(oldItem: Definition, newItem: Definition): Boolean {
                return oldItem.key.equals(newItem.key)
            }

            override fun areContentsTheSame(oldItem: Definition, newItem: Definition): Boolean {
                return oldItem.key.equals(newItem.key)
            }

        }


    }

}