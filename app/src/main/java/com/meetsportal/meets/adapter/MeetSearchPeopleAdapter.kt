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
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.profile.SearchPeopleResponse
import com.meetsportal.meets.networking.profile.SearchPeopleResponseItem
import com.meetsportal.meets.ui.fragments.socialfragment.SearchUserMeetFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.onClick
import org.json.JSONObject

class MeetSearchPeopleAdapter (
    var myContext: Activity, var selectedPeople : ArrayList<SearchPeopleResponseItem>, var list: SearchPeopleResponse, var bundle : Bundle?, var clickListener: (Int) -> Unit
) : RecyclerView.Adapter<MeetSearchPeopleAdapter.RviewHolder>() {


    val TAG = MeetSearchPeopleAdapter::class.simpleName
    val meetUpData = bundle?.getParcelable<GetMeetUpResponseItem>(SearchUserMeetFragment.MEET_DATA)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_meet_search_people_item, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var data = list.get(position)
        if(selectedPeople.firstOrNull() { it.username == data.username } != null){
            holder.ivCheck.setImageResource(R.drawable.ic_checked)
            MyApplication.putTrackMP(Constant.AcSelectUsername, JSONObject(mapOf("sid" to data.sid)))
        }else{
            holder.ivCheck.setImageResource(R.drawable.ic_unchecked)
        }
        val firstOrNull = Utils.getBadge(data.badge)
        Log.i(TAG, "onBindViewHolder: $firstOrNull")
        holder.badge.setImageResource(firstOrNull.foreground)
        Utils.stickImage(myContext,holder.dp,data.profile_image_url,null)
        holder.name.text = data.username
        holder.username.text = data.first_name?:data.username
//        Utils.onClick(holder.dp,1000){
//            openProfile(holder,data)
//        }
//        Utils.onClick(holder.name,1000){
//            openProfile(holder,data)
//        }


        meetUpData?.invitees?.union(meetUpData?.participants?:ArrayList())?.firstOrNull{ it.username.equals(data?.username) }?.let {
            Log.i(TAG," alreadyAdded:: true ${data?.username}")
            holder.rlCheck.visibility = View.GONE
            holder.itemView.onClick({
                Log.i(TAG," alreadyAdded:: true ${data?.username}")
            })
        }?:run{

            holder.rlCheck.visibility = View.VISIBLE
            if(data?.sid!= MyApplication.SID){
                holder.itemView.onClick({
                    Log.i(TAG," alreadyAdded:: false ${data?.username}")
                    if(selectedPeople.filter { it.sid == data.sid }.any()){
                        holder.ivCheck.setImageResource(R.drawable.ic_unchecked)
                        selectedPeople.remove(selectedPeople.filter { it.sid == data.sid }.firstOrNull())
                    }else{
                        holder.ivCheck.setImageResource(R.drawable.ic_checked)
                        data.selectedTimeStamp = System.currentTimeMillis()
                        selectedPeople.add(data)
                    }
                    clickListener(position)
                })
            }else{
                holder.ivCheck.visibility=View.GONE
                holder.rlCheck.visibility=View.GONE
            }
        }

    }

//    fun openProfile(holder: RviewHolder, data: SearchPeopleResponseItem) {
//        Utils.hideSoftKeyBoard(myContext,holder.root)
//        if(data.sid?.equals(PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid) == true){
//            Navigation.addFragment(myContext, ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false)
//        }else{
//            var baseFragment : BaseFragment = OtherProfileFragment()
//            baseFragment = Navigation.setFragmentData(baseFragment,
//                OtherProfileFragment.OTHER_USER_ID, data.sid)
//            Navigation.addFragment(myContext,baseFragment,
//                Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram,true,false)
//        }
//    }

    override fun getItemCount(): Int {
        return list.size
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dp = itemView.findViewById<ImageView>(R.id.image)
        var name = itemView.findViewById<TextView>(R.id.name)
        var username = itemView.findViewById<TextView>(R.id.username)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var rlCheck = itemView.findViewById<RelativeLayout>(R.id.rl_checkbox)
        var badge = itemView.findViewById<ImageView>(R.id.ivBadge)
        var ivCheck = itemView.findViewById<ImageView>(R.id.iv_checkbox)
    }

}