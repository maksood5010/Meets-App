package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.post.OpenMeetUp
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage

class OpenMeetJoinRqPeopleStackAdapter(var myContext: Activity,var  openMeet: OpenMeetUp?,var clicked : ()->Unit={}):
    RecyclerView.Adapter<OpenMeetJoinRqPeopleStackAdapter.RviewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)  = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_meet_person_chat,parent,false),clicked
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        //Utils.stickImage(myContext,holder.image,openMeet?.join_requests?.requests?.get(position)?.user_meta?.profile_image_url,null)
        holder.image.loadImage(myContext,openMeet?.join_requests?.requests?.get(position)?.user_meta?.profile_image_url,R.drawable.ic_default_person)
    }

    override fun getItemCount(): Int {
        /*participant?.let {
            return it.size
        }?:run{
            return 0
        }*/
        return openMeet?.join_requests?.requests?.take(3)?.size?:0
    }

    inner class RviewHolder(itemView: View, clicked: () -> Unit) : RecyclerView.ViewHolder(itemView) {
        var image = itemView.findViewById<ImageView>(R.id.image).apply {
            setOnClickListener{
                clicked()
               /* var fragment = MeetParticipantList.getInstance(meetData)
                Navigation.addFragment(myContext,fragment,
                    MeetParticipantList.TAG,
                    R.id.homeFram,true,false)*/
            }
        }
    }
}