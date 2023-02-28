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
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.profile.FollowerFollowingResponseItem
import com.meetsportal.meets.ui.dialog.BlockUserAlert
import com.meetsportal.meets.ui.fragments.socialfragment.FollowerFragment
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.verifyIcon

class FollowerPagerAdapter (var context : Activity, var fragment: FollowerFragment, var listener : (String?, Boolean)-> Unit,var blockListner:(String?)->Unit,var openProfile:(String?)->Unit
) : PagingDataAdapter<FollowerFollowingResponseItem, FollowerPagerAdapter.RviewHolder>(COMPARATOR) {


    var blockAlert : BlockUserAlert? = null
    init {
        blockAlert = BlockUserAlert(context,{
            blockListner(it)
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder {
        return RviewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.card_follower, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var data = getItem(position)
//        Utils.stickImage(context,holder.image,data?.user_meta?.profile_image_url,null)
        holder.image.loadImage(context,data?.user_meta?.profile_image_url,R.drawable.ic_default_person)
        holder.ivDpBadge.setImageResource(Utils.getBadge(data?.user_meta?.badge).foreground)
        if(data?.sid.equals(MyApplication.SID)){
            holder.followUnfollow.visibility = View.GONE
            holder.block.visibility = View.GONE
        }else {
            holder.followUnfollow.visibility = View.VISIBLE
            holder.block.visibility = View.VISIBLE
        }
        if(data?.user_meta?.first_name.isNullOrEmpty()) holder.name.text = data?.user_meta?.username
        else holder.name.text = data?.user_meta?.first_name.plus(" ${data?.user_meta?.last_name}")

        holder.username.text = "@".plus("${data?.user_meta?.username}")
        holder.name.verifyIcon(data?.user_meta?.verified_user)
//        if(data?.user_meta?.verified_user == true)
//        {
//            holder.verifyIcon.visibility = View.VISIBLE
//        }
//        else{
//            holder.verifyIcon.visibility = View.GONE
//        }
        if(data?.followed_by_user == true){
            holder.followUnfollow.text = "Unfollow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(context,R.color.primaryDark))
            var bg = ContextCompat.getDrawable(context,R.drawable.horizontal_round_shape) as GradientDrawable
            holder.followUnfollow.setBackgroundDrawable(bg)
        }else{
            holder.followUnfollow.text = "Follow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(context,R.color.white))
            holder.followUnfollow.background = ContextCompat.getDrawable(context,R.drawable.round_border_primary_bg)
        }

        addClickevent(holder,data,position)

    }

    private fun addClickevent(
        holder: RviewHolder,
        data: FollowerFollowingResponseItem?,
        position: Int
    ) {
            Utils.onClick(holder.followUnfollow,500){
                if(holder.followUnfollow.text.equals("Unfollow"))
                    listener(data?.user_meta?.sid,false)
                else if(holder.followUnfollow.text.equals("Follow")){
                    listener(data?.user_meta?.sid,true)
                }
                toggleFollow(holder,data)
        }

        holder.block.setOnClickListener {
            blockAlert?.showDialog(data)
            //blockListner(data?.sid)
            //notifyItemRemoved(position)
        }

        holder.root.onClick( {
            openProfile(data?.user_meta?.sid)
            Utils.hideSoftKeyBoard(context,holder.root)
        })

    }

    /*fun openProfile(data: FollowerFollowingResponseItem?) {
        var myId = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid
        if(data?.user_meta?.sid == myId){
            Navigation.addFragment(
                context,
                ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        }
        else{
            var baseFragment : BaseFragment = OtherProfileFragment.getInstance(data?.user_meta?.sid)
            *//*baseFragment = Navigation.setFragmentData(baseFragment,
                OtherProfileFragment.OTHER_USER_ID,
                data?.user_meta?.sid)*//*
            Navigation.addFragment(context,baseFragment,
                Constant.OTHER_PROFILE_FRAGMENT,R.id.homeFram,true,false)
        }
    }*/

    private fun toggleFollow(holder: RviewHolder, data: FollowerFollowingResponseItem?) {
        if(holder.followUnfollow.text.equals("Unfollow")){
            holder.followUnfollow.text = "Follow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(context,R.color.white))
            holder.followUnfollow.background = ContextCompat.getDrawable(context,R.drawable.round_border_primary_bg)
        }
        else if(holder.followUnfollow.text.equals("Follow")){
            holder.followUnfollow.text = "Unfollow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(context,R.color.primaryDark))
            var bg = ContextCompat.getDrawable(context,R.drawable.horizontal_round_shape) as GradientDrawable
            bg.setStroke(1,ContextCompat.getColor(context,R.color.primaryDark))
            holder.followUnfollow.setBackgroundDrawable(bg)
        }
    }






    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var name = itemView.findViewById<TextView>(R.id.name)
        var username = itemView.findViewById<TextView>(R.id.username)
        var followUnfollow = itemView.findViewById<TextView>(R.id.followUnfollow)
        var block = itemView.findViewById<TextView>(R.id.block)
        var image = itemView.findViewById<ImageView>(R.id.image)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
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