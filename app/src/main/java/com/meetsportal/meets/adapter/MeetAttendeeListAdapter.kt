package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.fenchtose.tooltip.Tooltip
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentLikedUserBinding
import com.meetsportal.meets.networking.meetup.MeetPerson
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.verifyIcon

class MeetAttendeeListAdapter (
    var myContext: Activity,
    var participant: ArrayList<MeetPerson?>?,
    var binding: FragmentLikedUserBinding,
    var type : Constant.EnumMeetPerson,
    var listener : (String?, Boolean)-> Unit,
    var positiveRes: (String?,String?)->Unit,
    var openProfile:(String?)->Unit
):
    RecyclerView.Adapter<MeetAttendeeListAdapter.RviewHolder>() {


    val TAG = MeetAttendeeListAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)  = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_meet_attendee,parent,false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        binding.npDataLayout.noDataRoot.visibility = View.GONE
        var data = participant?.getOrNull(position)
        Log.i(TAG," firstName:: ${data?.first_name} name:: ${data?.username}")
        if(!data?.first_name.isNullOrEmpty())
            holder.name.text = data?.first_name.plus(" ${data?.last_name}")
        else
            holder.name.text = data?.username
        holder.username.text = "@".plus("${data?.username}")
        holder.image.loadImage(myContext, data?.profile_image_url,R.drawable.ic_default_person,false)

        if(data?.rated_by_user == true){
            holder.positiveRes.setImageResource(R.drawable.ic_positive_res_fill)
        }else{
            holder.positiveRes.setImageResource(R.drawable.ic_positve_res_nofill)
        }
        data?.badge?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            holder.ivDpBadge.setImageResource(firstOrNull.foreground)
        }
        holder.name.verifyIcon(data?.verified_user)
        if(data?.followed_by_user == true){
            holder.followUnfollow.text = "Unfollow"
//            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext, R.color.primaryDark))
//            var bg = ContextCompat.getDrawable(myContext,
//                R.drawable.horizontal_round_shape) as GradientDrawable
//            holder.followUnfollow.setBackgroundDrawable(bg)
        }else{
            holder.followUnfollow.text = "Follow"
//            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext, R.color.white))
//            holder.followUnfollow.background = ContextCompat.getDrawable(myContext, R.drawable.round_border_primary_bg)
        }

        var myId = MyApplication.SID

        if(type == Constant.EnumMeetPerson.ATTENDEE){
            if(data?.sid.equals(myId)){
                holder.followUnfollow.visibility = View.GONE
                holder.positiveRes.visibility = View.GONE
            }else{
                holder.followUnfollow.visibility = View.VISIBLE
                holder.positiveRes.visibility = View.VISIBLE
                if(position==(itemCount-1)){
                    Utils.showToolTips(myContext,holder.positiveRes, binding.root, Tooltip.BOTTOM,"Give your friends a thumbs up to show you had a good experience during your Meet up","positiveRes"){
                    }
                }
            }
        }else{
            holder.positiveRes.visibility = View.GONE
            if(data?.sid.equals(myId)){
                holder.followUnfollow.visibility = View.GONE

            }else{
                holder.followUnfollow.visibility = View.VISIBLE
//                holder.positiveRes.visibility = View.VISIBLE
            }
        }

        addClickevent(holder,data)
    }

    fun setData(participant: List<MeetPerson?>?){
        participant?.let {
            this.participant?.clear()
            this.participant?.addAll(participant)
            notifyDataSetChanged()
        }?:run{
            Log.e(TAG," parameter Attendee is null")
        }

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
            openProfile(data?.sid)
        })
        holder.positiveRes.onClick({
            Log.i(TAG, "addClickevent: positiveRes 0")
            positiveRes(data?.sid,data?.username)
//            if(data?.rated_by_user == true){
//                Log.i(TAG, "addClickevent: positiveRes 1")
//            }
        })
    }

    /*fun openProfile(data: MeetPerson?) {
        var myId = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid
        if(data?.sid == myId){
            Navigation.addFragment(
                myContext,
                ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        }
        else{
            var baseFragment : BaseFragment = OtherProfileFragment.getInstance(data?.sid)
            *//*baseFragment = Navigation.setFragmentData(baseFragment,
                OtherProfileFragment.OTHER_USER_ID,
                data?.sid)*//*
            Navigation.addFragment(myContext,baseFragment,
                Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram,true,false)
        }
    }*/

    private fun toggleFollow(holder: RviewHolder, data: MeetPerson? ) {
        if(holder.followUnfollow.text.equals("Unfollow")){
            holder.followUnfollow.text = "Follow"
//            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext, R.color.white))
//            holder.followUnfollow.background = ContextCompat.getDrawable(myContext, R.drawable.round_border_primary_bg)
        }
        else if(holder.followUnfollow.text.equals("Follow")){
            holder.followUnfollow.text = "Unfollow"
//            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext, R.color.primaryDark))
//            var bg = ContextCompat.getDrawable(myContext,
//                R.drawable.horizontal_round_shape) as GradientDrawable
//            bg.setStroke(1, ContextCompat.getColor(myContext, R.color.primaryDark))
//            holder.followUnfollow.setBackgroundDrawable(bg)
        }
    }

    override fun getItemCount(): Int {
        participant?.let {
            return it.size
        }?:run{
            return 0
        }
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var name = itemView.findViewById<TextView>(R.id.name)
        var username = itemView.findViewById<TextView>(R.id.username)
        var followUnfollow = itemView.findViewById<TextView>(R.id.followUnfollow)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
        var positiveRes = itemView.findViewById<ImageView>(R.id.positiveRes)
        //var verifyIcon = itemView.findViewById<ImageView>(R.id.iv_verify)
        var image = itemView.findViewById<ImageView>(R.id.image)
    }
}