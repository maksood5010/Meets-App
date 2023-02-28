package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.profile.Definition
import com.meetsportal.meets.networking.profile.FullInterestGetResponse
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.PreferencesManager

class OpenMeetListSelectCatAdapter(
    var myContext: Activity,
): RecyclerView.Adapter<OpenMeetListSelectCatAdapter.RviewHolder>()  {

    var InterestList = PreferencesManager.get<FullInterestGetResponse>(Constant.PREFRENCE_INTEREST)?.definition
    var list = ArrayList<Definition?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RviewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_pre_seleced_place, parent, false)

        )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
       var data =  list.getOrNull(position)
        Log.i("TAG"," checkingEn:: ${data}")
        holder.titleName.text = data?.en
    }



    override fun getItemCount() = list.size

    fun setData(selected: List<Definition?>) {
       /* Log.i("TAG"," De3finitionData coming:: 0  ${selected}")
        Log.i("TAG"," De3finitionData coming:: 1  ${selected.map { it?.key }}")
        Log.i("TAG"," De3finitionData coming:: 2  ${InterestList}")
        Log.i("TAG"," De3finitionData filter:: 0  ${InterestList?.filter { filter -> 
            Log.i(" "," checkingKey:: 0-- ${filter.key}")
            Log.i(" "," checkingKey:: 1-- ${selected.map { it?.key }.contains(filter.key)}")
            selected.map { it?.key }.contains(filter.key) 
        }}")*/
        list.clear()
        list.addAll(InterestList?.filter { filter -> selected.map { it?.key }.contains(filter?.key) }?:ArrayList<Definition>())
        Log.i("TAG"," De3finitionData coming:: 1  ${list}")
        notifyDataSetChanged()
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //var mainImage = itemView.findViewById<>(R.id.mainImage)
        var titleName = itemView.findViewById<TextView>(R.id.placename)
    }
}