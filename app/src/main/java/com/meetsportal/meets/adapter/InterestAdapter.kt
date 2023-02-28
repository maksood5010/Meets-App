package com.meetsportal.meets.adapter

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.profile.Definition


class InterestAdapter(
    var context: Activity,
    interest: List<Definition?>?,
    var selected: ArrayList<Definition?>,
    var myInterest: List<Definition?>?,
    var selectionChange : ()->Unit = {},
) : RecyclerView.Adapter<InterestAdapter.RviewHolder>(){


    val differ: AsyncListDiffer<Definition?> = AsyncListDiffer<Definition?>(this, DIFF_CALLBACK)


    val TAG = InterestAdapter::class.simpleName

    var adapterInterest :ArrayList<Definition?> = ArrayList()

    init {
        adapterInterest.addAll(interest as Collection<Definition?>)
        myInterest?.forEach { myint ->
            selected.add(interest.filter { it?.key == myint?.key }.getOrNull(0))
        }
        differ.submitList(adapterInterest)
    }
    

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_interest_item, parent, false)
    )

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        val data = differ.currentList.getOrNull(position)
        data?.let { adapterInterest ->
            holder.text.setText(adapterInterest.en.plus(" "))
            deselectItem(holder)
            selected.map {
                if(it?.en.plus(" ").toString().equals(holder.text.text.toString())){
                    selectItem(holder)
                    return@map
                }
            }
        }

        holder.text.setOnClickListener{
            Log.i(TAG, " clicked:: ${data}")
            if(selected.map{it?.key}.contains(data?.key)){
                selected.removeIf { it?.key == data?.key }
                deselectItem(holder)
            }else{
                if(selected.size > 9){
                    MyApplication.showToast(context,"Can select only 10 interest")
                    return@setOnClickListener
                }
                selected.add(data)
                selectItem(holder)
            }
            selectionChange()
        }
    }

    fun selectItem(holder: RviewHolder){
        holder.text.setBackgroundResource(R.drawable.round_border_primary_bg)
        holder.text.setTextColor(ContextCompat.getColor(context, R.color.white))
    }

    fun deselectItem(holder: RviewHolder){
        holder.text.setBackgroundResource(R.drawable.horizontal_round_shape)
        holder.text.setTextColor(ContextCompat.getColor(context, R.color.black))
    }

    override fun getItemCount(): Int {
        /*adapterInterest?.let {
            return it.size
        }
       return 0*/
        return differ.currentList.size
    }
    
    fun setItem(interest: List<Definition?>?){
//        adapterInterest.clear()
//        adapterInterest.addAll(interest as Collection<Definition?>)
        differ.submitList(interest)
        //notifyDataSetChanged()
    }

    class RviewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        var text = itemView.findViewById<TextView>(R.id.tv_interest_item)
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