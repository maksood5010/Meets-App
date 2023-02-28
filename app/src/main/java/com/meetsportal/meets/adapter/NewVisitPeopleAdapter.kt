package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import de.hdodenhof.circleimageview.CircleImageView

class NewVisitPeopleAdapter(var myContext: Activity,val count:Int): RecyclerView.Adapter<NewVisitPeopleAdapter.RviewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)  = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_meet_person_chat,parent,false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        holder.image.borderColor=ContextCompat.getColor(myContext,R.color.white)
        holder.image.circleBackgroundColor= Color.parseColor("#3AC9D5")
//        val lParams= ConstraintLayout.LayoutParams(
//                ConstraintLayout.LayoutParams.MATCH_PARENT,
//                ConstraintLayout.LayoutParams.MATCH_PARENT
//                                                  )
//        lParams.setMargins(Utils.dpToPx(10f,myContext.resources),0,0,0)
//        holder.image.layoutParams=lParams
    }

    override fun getItemCount(): Int {
        return count
    }

    inner class RviewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        var image = itemView.findViewById<CircleImageView>(R.id.image)
//                .apply {
//            setOnClickListener{
//                Log.d("TAG", "bindingAdapterPosition:$bindingAdapterPosition ")
//                onClick()
//            }
//        }
    }
}