package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.meetup.MeetPerson
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Utils.Companion.loadImage

class MeetPeopleListAdapter(var myContext: Activity, var participant: ArrayList<MeetPerson?>?,var bindData : ()->Unit,var listener : (String?,Boolean)-> Unit,var openProfile:(String?)->Unit,var newpage: ()->Unit = {}):
    RecyclerView.Adapter<MeetPeopleListAdapter.RviewHolder>() {

    var followVisibility = true

    val TAG = MeetPeopleListAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)  = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_like,parent,false)
    )
//    fun setParticipants(participant:List<MeetPerson?>?){
//        this.participant=participant
//        notifyDataSetChanged()
//    }

    fun submitList(data: List<MeetPerson?>) {
        participant?.clear()
        participant?.addAll(data)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        //binding.npDataLayout.noDataRoot.visibility = View.GONE
        bindData()
        var data = participant?.getOrNull(position)
        Log.i(TAG," firstName:: ${data?.first_name} name:: ${data?.username}")
        if(!data?.first_name.isNullOrEmpty())
            holder.name.text = data?.first_name.plus(" ${data?.last_name}")
        else
            holder.name.text = data?.username
        holder.username.text = "@".plus("${data?.username}")
        //Utils.stickImage(myContext,holder.image,data?.profile_image_url,null)
        holder.image.loadImage(myContext,data?.profile_image_url,R.drawable.ic_default_person)
        /*if(data?.verified_user == true){
            //holder.verifyIcon.visibility = View.VISIBLE
        }else{
            //holder.verifyIcon.visibility = View.GONE
        }*/
        holder.name.verifyIcon(data?.verified_user)
        if(data?.followed_by_user == true){
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
        if(data?.sid.equals(myId)){
            holder.followUnfollow.visibility = View.GONE
        }else{
            if(followVisibility)
                holder.followUnfollow.visibility = View.VISIBLE
            else
                holder.followUnfollow.visibility = View.GONE
        }

        addClickevent(holder,data)

    }

    private fun addClickevent(holder: RviewHolder, data: MeetPerson?) {
            Utils.onClick(holder.followUnfollow,500){
                if(holder.followUnfollow.text.equals("Unfollow"))
                    listener(data?.sid,false)
                else if(holder.followUnfollow.text.equals("Follow")){
                    listener(data?.sid,true)
                }
                toggleFollow(holder,data)
            }

        holder.root.onClick( {
            bindData()
            newpage()
            openProfile(data?.sid)
        })
    }

    fun hideFollowunFollw(){
        followVisibility = false
    }

    /*fun openProfile(data: MeetPerson?) {
        bindData()
        var myId = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid
        if(data?.sid == myId){
            newpage()
            Navigation.addFragment(
                myContext,
                ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        }
        else{
            newpage()
            var baseFragment : BaseFragment = OtherProfileFragment.getInstance(data?.sid)
            *//*baseFragment = Navigation.setFragmentData(baseFragment,
                OtherProfileFragment.OTHER_USER_ID,
                data?.sid)*//*
            Navigation.addFragment(myContext,baseFragment,
                Constant.OTHER_PROFILE_FRAGMENT,R.id.homeFram,true,false)
        }
    }*/

    private fun toggleFollow(holder: RviewHolder, data: MeetPerson? ) {
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
        participant?.let {
            return it.size
        }?:run{
            return 0
        }
    }

    inner class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var name = itemView.findViewById<TextView>(R.id.name)
        var username = itemView.findViewById<TextView>(R.id.username)
        var followUnfollow = itemView.findViewById<TextView>(R.id.followUnfollow).apply {


        }
        //var verifyIcon = itemView.findViewById<ImageView>(R.id.iv_verify)
        var image = itemView.findViewById<ImageView>(R.id.image)
    }
}