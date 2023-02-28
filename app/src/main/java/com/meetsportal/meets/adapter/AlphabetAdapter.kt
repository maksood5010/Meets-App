package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.profile.Definition
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground

class AlphabetAdapter(var myContext: Activity,var selected :List<Definition?>,var fullInterest : ArrayList<Definition?> ,var clickAlphabet : (Int)->Unit) :  RecyclerView.Adapter<AlphabetAdapter.RviewHolder>() {

    var selectedCharList = selected.map{ it?.en?.getOrNull(0)?.toInt()}
    var allChatList = fullInterest.map{ it?.en?.getOrNull(0)?.toInt() }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = RviewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_alphabets, parent, false),myContext
        )

    override fun onBindViewHolder(holder: RviewHolder, position: Int){
       // var charList = selected.map{ it?.en?.getOrNull(0)?.toInt()}
        var number = 65 + position


        if(allChatList.contains(number)){
            holder.root.setRoundedColorBackground(myContext, Color.parseColor("#E2F5F7"),10f)
            holder.letter.setTextColor(Color.parseColor("#80ABAF"))
        }else{
            holder.root.setRoundedColorBackground(myContext, R.color.extraLightGray,10f)
            holder.letter.setTextColor(ContextCompat.getColor(myContext,R.color.darkerGray))
        }
        if(selectedCharList.contains(number)){
            holder.root.setRoundedColorBackground(myContext,R.color.primaryDark,10f)
            holder.letter.setTextColor(ContextCompat.getColor(myContext,R.color.white))
        }
        holder.letter.text = number.toChar().toString()
        if(allChatList.contains(number)){
            holder.root.onClick({
                clickAlphabet(number)
            },100)
        }
    }

    override fun getItemCount(): Int {
        return 26
    }

    class RviewHolder(itemView: View, myContext: Activity) : RecyclerView.ViewHolder(itemView) {
        val root = itemView.findViewById<ConstraintLayout>(R.id.rootCo).apply {
            setRoundedColorBackground(myContext, Color.parseColor("#E2F5F7"),10f)
        }
        var letter = itemView.findViewById<TextView>(R.id.letter)

    }

    fun updateItem(){
        selectedCharList = selected.map{ it?.en?.getOrNull(0)?.toInt()}
        notifyDataSetChanged()
    }

    fun putAllInterest(definition: List<Definition?>) {
        fullInterest.clear()
        fullInterest.addAll(definition)
        allChatList = fullInterest.map{ it?.en?.getOrNull(0)?.toInt() }

    }

}