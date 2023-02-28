package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.MeetRqCountByBadge
import com.meetsportal.meets.networking.profile.Badge

class AllowOpenMeetRqAdapter(
    var myContext: Activity, var list: MutableList<Badge>,var countByBadge: MeetRqCountByBadge?,var checked : ()->Unit
) : RecyclerView.Adapter<AllowOpenMeetRqAdapter.RviewHolder>() {



    val TAG = AllowOpenMeetRqAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_badge_type_chekbox, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var data  = list.get(position)
        holder.title.text = data.name.plus(" status member")
        countByBadge?.counts?.javaClass?.getDeclaredField(data.key)?.apply { isAccessible = true }?.get(countByBadge?.counts)?.apply {
            Log.i("TAG"," hbchjbdj::: ${this}")
            if(this is Int){
                holder.desc.text = this.toString().plus(" user you know is a ${data.name} status member")
                data.count = this
            }
        }

        if(data.selected == true){
            holder.check.setImageResource(R.drawable.ic_checked)
        }else{
            holder.check.setImageResource(R.drawable.color_border_white_bg_button_bg)
        }
        holder.check.setOnClickListener {
            if(data.selected == false){
                data.selected = true
                holder.check.setImageResource(R.drawable.ic_checked)
            }else{
                data.selected = false
                holder.check.setImageResource(R.drawable.color_border_white_bg_button_bg)
            }
            notifyItemChanged(position)
            checked()
        }

        /*var data = list.get(position)
        if(selectedPeople.firstOrNull() { it.username == data.username } != null){
            holder.ivCheck.setImageResource(R.drawable.ic_checked)
        }else{
            holder.ivCheck.setImageResource(R.drawable.ic_unchecked)
        }
        Utils.stickImage(myContext,holder.dp,data.profile_image_url,null)
        holder.name.text = data.username
        holder.username.text = data.first_name?:data.username
        Utils.onClick(holder.dp,1000){
            openProfile(holder,data)
        }
        Utils.onClick(holder.name,1000){
            openProfile(holder,data)
        }
        Utils.onClick(holder.rlCheck,2){
            if(selectedPeople.filter { it.sid == data.sid }.any()){
                holder.ivCheck.setImageResource(R.drawable.ic_unchecked)
                selectedPeople.remove(selectedPeople.filter { it.sid == data.sid }.firstOrNull())
            }else{
                holder.ivCheck.setImageResource(R.drawable.ic_checked)
                data.selectedTimeStamp = System.currentTimeMillis()
                selectedPeople.add(data)
            }
            clickListener(position)
        }

        meetUpData?.invitees?.firstOrNull{ it.username.equals(data?.username) }?.let {
            Log.i(TAG," alreadyAdded:: true ${data?.username}")
            holder.rlCheck.visibility = View.GONE
        }?:run{
            Log.i(TAG," alreadyAdded:: false ${data?.username}")
            holder.rlCheck.visibility = View.VISIBLE
        }*/

    }

    /*fun openProfile(holder: RviewHolder, data: SearchPeopleResponseItem) {
        //Utils.hideSoftKeyBoard(myContext,holder.root)
        if(data.sid?.equals(PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid) == true){
            Navigation.addFragment(myContext, ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false)
        }else{
            var baseFragment : BaseFragment = OtherProfileFragment.getInstance(data.sid)
            *//*baseFragment = Navigation.setFragmentData(baseFragment,
                OtherProfileFragment.OTHER_USER_ID, data.sid)*//*
            Navigation.addFragment(myContext,baseFragment,
                Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram,true,false)
        }
    }*/

    override fun getItemCount(): Int {
        return list.size
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title = itemView.findViewById<TextView>(R.id.title)
        var check = itemView.findViewById<ImageView>(R.id.check)
        var desc = itemView.findViewById<TextView>(R.id.desc)
        /*var dp = itemView.findViewById<ImageView>(R.id.image)
        var name = itemView.findViewById<TextView>(R.id.name)
        var username = itemView.findViewById<TextView>(R.id.username)
        var root = itemView.findViewById<ConstraintLayout>(R.id.root)
        var rlCheck = itemView.findViewById<RelativeLayout>(R.id.rl_checkbox)
        var ivCheck = itemView.findViewById<ImageView>(R.id.iv_checkbox)*/
    }

}