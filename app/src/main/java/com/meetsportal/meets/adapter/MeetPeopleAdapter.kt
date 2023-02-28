package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.meetup.MeetPerson
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.onClick
import de.hdodenhof.circleimageview.CircleImageView

class MeetPeopleAdapter(var myContext: Activity, var participant: List<MeetPerson?>?, var meetData : GetMeetUpResponseItem?,var onClick: ()->Unit = {}): RecyclerView.Adapter<MeetPeopleAdapter.RviewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)  = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_meet_person_chat,parent,false),onClick
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        Utils.stickImage(myContext,holder.image,participant?.get(position)?.profile_image_url,null)
        holder.itemView.onClick({
            onClick()
        })
    }

    override fun getItemCount(): Int {
        participant?.let {
            return it.size
        }?:run{
            return 0
        }
    }

    inner class RviewHolder(itemView: View,onClick: () -> Unit) :RecyclerView.ViewHolder(itemView) {
        var image = itemView.findViewById<CircleImageView>(R.id.image)
//                .apply {
//            setOnClickListener{
//                Log.d("TAG", "bindingAdapterPosition:$bindingAdapterPosition ")
//                onClick()
//            }
//        }
    }
}