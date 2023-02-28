package com.meetsportal.meets.adapter

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.profile.FollowerFollowingResponseItem
import com.meetsportal.meets.networking.profile.SearchPeopleResponseItem
import com.meetsportal.meets.ui.fragments.socialfragment.SearchUserMeetFragment
import com.meetsportal.meets.ui.fragments.socialfragment.SearchUserMeetFragment.Companion.MEET_DATA
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage

class MeetFollowingPagerAdapter (var myContext : Activity, var fragment: SearchUserMeetFragment, var selectedPeople : ArrayList<SearchPeopleResponseItem>, var bundle : Bundle?, var clickListener: (Int) -> Unit) : PagingDataAdapter<FollowerFollowingResponseItem, MeetFollowingPagerAdapter.RviewHolder>(COMPARATOR) {

    val TAG = MeetFollowingPagerAdapter::class.simpleName
    val meetUpData = bundle?.getParcelable<GetMeetUpResponseItem>(MEET_DATA)

    init {

        Log.i(TAG," checkMeetUp:: ${meetUpData}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder {
        return RviewHolder(
            //LayoutInflater.from(parent.context).inflate(R.layout.card_following, parent, false)
            LayoutInflater.from(parent.context).inflate(R.layout.card_meet_search_people_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var data = getItem(position)
        Log.i(TAG," onBindViewHolder:::: $position -- ${data?.sid}")
        if(selectedPeople.firstOrNull() { it.username == data?.user_meta?.username } != null){
            holder.ivCheck.setImageResource(R.drawable.ic_checked)
        }else{
            holder.ivCheck.setImageResource(R.drawable.ic_unchecked)
        }
        //Utils.stickImage(myContext,holder.dp,data?.user_meta?.profile_image_url,null)
        holder.dp.loadImage(myContext,data?.user_meta?.profile_image_url,R.drawable.ic_default_person)
        holder.badge.setImageResource(Utils.getBadge(data?.user_meta?.badge)?.foreground)

        holder.name.text = data?.user_meta?.username
        holder.username.text = data?.user_meta?.first_name?:data?.user_meta?.username
//        Utils.onClick(holder.dp,1000){
//            openProfile(holder,data)
//        }
//        Utils.onClick(holder.name,1000){
//            openProfile(holder,data)
//        }

        meetUpData?.invitees?.union(meetUpData?.participants?:ArrayList())?.firstOrNull{ it.username.equals(data?.user_meta?.username) }?.let {
            Log.i(TAG," alreadyAdded:: true ${data?.user_meta?.username}")
            holder.rlCheck.visibility = View.GONE
            Utils.onClick(holder.itemView,2){}
        }?:run{
            Log.i(TAG," alreadyAdded:: false ${data?.user_meta?.username}")
            holder.rlCheck.visibility = View.VISIBLE
            if(data?.sid != MyApplication.SID) {
                Utils.onClick(holder.itemView,2){
                    if(selectedPeople.filter { it.sid == data?.sid }.any()){
                        holder.ivCheck.setImageResource(R.drawable.ic_unchecked)
                        selectedPeople.remove(selectedPeople.filter{ it.sid == data?.sid }.firstOrNull())
                    }else{
                        holder.ivCheck.setImageResource(R.drawable.ic_checked)
                        data?.selectedTimeStamp = System.currentTimeMillis()
                        selectedPeople.add(data?.toSearchPeopleresponseItem()?: SearchPeopleResponseItem())
                    }
                    clickListener(position)
                }
            } else {
                holder.rlCheck.visibility=View.GONE
                Utils.onClick(holder.itemView,2){}
            }
        }

    }



//    fun openProfile(holder: MeetFollowingPagerAdapter.RviewHolder, data: FollowerFollowingResponseItem?) {
//        Utils.hideSoftKeyBoard(myContext,holder.root)
//        if(data?.sid?.equals(PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid) == true){
//            Navigation.addFragment(myContext, ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false)
//        }else{
//            var baseFragment : BaseFragment = OtherProfileFragment()
//            baseFragment = Navigation.setFragmentData(baseFragment,
//                OtherProfileFragment.OTHER_USER_ID, data?.sid)
//            Navigation.addFragment(myContext,baseFragment,
//                Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram,true,false)
//        }
//    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dp = itemView.findViewById<ImageView>(R.id.image)
        var name = itemView.findViewById<TextView>(R.id.name)
        var badge = itemView.findViewById<ImageView>(R.id.ivBadge)
        var username = itemView.findViewById<TextView>(R.id.username)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var rlCheck = itemView.findViewById<RelativeLayout>(R.id.rl_checkbox)
        var ivCheck = itemView.findViewById<ImageView>(R.id.iv_checkbox)
    }

    companion object{
        private val COMPARATOR = object : DiffUtil.ItemCallback<FollowerFollowingResponseItem>(){
            override fun areItemsTheSame(
                oldItem: FollowerFollowingResponseItem,
                newItem: FollowerFollowingResponseItem
            ): Boolean {
                return (oldItem._id == newItem._id)
            }

            override fun areContentsTheSame(
                oldItem: FollowerFollowingResponseItem,
                newItem: FollowerFollowingResponseItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }


}