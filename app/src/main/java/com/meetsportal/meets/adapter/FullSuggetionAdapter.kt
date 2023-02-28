package com.meetsportal.meets.adapter

import android.app.Activity
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
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.profile.SuggestionResponse
import com.meetsportal.meets.networking.profile.SuggestionResponseItem
import com.meetsportal.meets.utils.Constant.AcDpSuggested
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.verifyIcon
import org.json.JSONObject

class FullSuggetionAdapter(
    var myContext: Activity,
    var suggestionResponse: SuggestionResponse?,
    var listener: (String?, Boolean) -> Unit,
    var openProfile:(String?)->Unit
) :
    RecyclerView.Adapter<FullSuggetionAdapter.RviewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RviewHolder {
        return RviewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_like, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return suggestionResponse?.size ?: 0
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var data = suggestionResponse?.get(position)
        if (!data?.first_name.isNullOrEmpty())
            holder.name.text = data?.first_name.plus(data?.last_name)
        else
            holder.name.text = data?.username
        holder.username.text = data?.username
        holder.name.verifyIcon(data?.verified_user)
        data?.social?.getbadge()?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            holder.ivDpBadge.setImageResource(firstOrNull.foreground)
        }
//        if(data?.verified_user == true)
//            holder.verifyIcon.visibility = View.VISIBLE
//        else
//            holder.verifyIcon.visibility = View.GONE
//        Utils.stickImage(myContext,holder.image,data?.profile_image_url,null)
        holder.image.loadImage(myContext,data?.profile_image_url,R.drawable.ic_default_person)
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
        addClickevent(holder,data)
    }

    private fun addClickevent(holder: RviewHolder, data: SuggestionResponseItem?) {
            Utils.onClick(holder.followUnfollow,500){
                if(holder.followUnfollow.text.equals("Unfollow"))
                    listener(data?.sid,false)
                else if(holder.followUnfollow.text.equals("Follow")){
                    listener(data?.sid,true)
                }
                toggleFollow(holder,data)
            }

        holder.root.onClick({
            MyApplication.putTrackMP(AcDpSuggested, JSONObject(mapOf("sid" to data?.sid)))
            openProfile(data?.sid)
        })
    }

    private fun toggleFollow(holder: RviewHolder, data: SuggestionResponseItem?) {
        if(holder.followUnfollow.text.equals("Unfollow")){
            holder.followUnfollow.text = "Follow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext,R.color.white))
            holder.followUnfollow.background = ContextCompat.getDrawable(myContext,R.drawable.round_border_primary_bg)
        }
        else if(holder.followUnfollow.text.equals("Follow")){
            holder.followUnfollow.text = "Unfollow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext,R.color.primaryDark))
            var bg = ContextCompat.getDrawable(myContext,R.drawable.horizontal_round_shape) as GradientDrawable
            bg.setStroke(1,ContextCompat.getColor(myContext,R.color.primaryDark))
            holder.followUnfollow.setBackgroundDrawable(bg)
        }
    }

    /*fun openProfile(data: SuggestionResponseItem?) {
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
                Constant.OTHER_PROFILE_FRAGMENT,R.id.homeFram,true,false)
        }
    }*/



    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var name = itemView.findViewById<TextView>(R.id.name)
        var username = itemView.findViewById<TextView>(R.id.username)
        var followUnfollow = itemView.findViewById<TextView>(R.id.followUnfollow)
        var image = itemView.findViewById<ImageView>(R.id.image)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
    }
}