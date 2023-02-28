package com.meetsportal.meets.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.ProfileView
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Utils.Companion.loadImage

class Person4Adapter(var myContext: Context, val list:List<ProfileView?>,var listener : (String?,Boolean)-> Unit,var openProfile : (String?)->Unit) :
    RecyclerView.Adapter<Person4Adapter.RviewHolder>() {

    private val TAG = Person4Adapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_like, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        val data = list[position]
        if(!data?.user_meta?.first_name.isNullOrEmpty())
            holder.name.text = data?.user_meta?.first_name.plus(" ${data?.user_meta?.last_name}")
        else
            holder.name.text = data?.user_meta?.username

        holder.username.text = "@".plus("${data?.user_meta?.username}")

        //Utils.stickImage(myContext,holder.image,data?.user_meta?.profile_image_url,null)
        holder.image.loadImage(myContext,data?.user_meta?.profile_image_url,R.drawable.ic_default_person)
        holder.ivDpBadge.setImageResource(Utils.getBadge(data?.user_meta?.badge).foreground)
        holder.name.verifyIcon(data?.user_meta?.verified_user)
//        if(data?.user_meta?.verified_user == true){
//            holder.verifyIcon.visibility = View.VISIBLE
//        }else{
//            holder.verifyIcon.visibility = View.GONE
//        }
        if(data?.followed_by_you == true){
            holder.followUnfollow.text = "Unfollow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext,R.color.primaryDark))
            var bg = ContextCompat.getDrawable(myContext,R.drawable.horizontal_round_shape) as GradientDrawable
            holder.followUnfollow.setBackgroundDrawable(bg)
        }else{
            holder.followUnfollow.text = "Follow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext,R.color.white))
            holder.followUnfollow.background = ContextCompat.getDrawable(myContext,R.drawable.round_border_primary_bg)
        }

        var myId = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid
        if(data?.user_meta?.sid.equals(myId)){
            holder.followUnfollow.visibility = View.GONE
        }else{
            holder.followUnfollow.visibility = View.VISIBLE
        }

        addClickevent(holder,data)

    }


    private fun addClickevent(holder: RviewHolder, data: ProfileView?, ) {
            Utils.onClick(holder.followUnfollow,500){
                if(holder.followUnfollow.text.equals("Unfollow"))
                    listener(data?.user_meta?.sid,false)
                else if(holder.followUnfollow.text.equals("Follow")){
                    listener(data?.user_meta?.sid,true)
                }
                toggleFollow(holder,data)
        }

        holder.root.onClick( {
            openProfile(data?.user_meta?.sid)
        })
    }

    private fun toggleFollow(holder: RviewHolder, data: ProfileView?) {
        if(holder.followUnfollow.text.equals("Unfollow")){
            holder.followUnfollow.text = "Follow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext,R.color.white))
            holder.followUnfollow.background = ContextCompat.getDrawable(myContext,R.drawable.round_border_primary_bg)
        }
        else if(holder.followUnfollow.text.equals("Follow")){
            holder.followUnfollow.text = "Unfollow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext,R.color.primaryDark))
            var bg = ContextCompat.getDrawable(myContext,R.drawable.horizontal_round_shape) as GradientDrawable
//            bg.setStroke(1,ContextCompat.getColor(myContext,R.color.primaryDark))
            holder.followUnfollow.setBackgroundDrawable(bg)
        }
    }
    override fun getItemCount(): Int {
        return list.size
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var name = itemView.findViewById<TextView>(R.id.name)
        var username = itemView.findViewById<TextView>(R.id.username)
        var followUnfollow = itemView.findViewById<TextView>(R.id.followUnfollow)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
        var image = itemView.findViewById<ImageView>(R.id.image)
    }
}