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
import com.meetsportal.meets.networking.post.PostLikerResponseItem
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.ui.fragments.socialfragment.LikeFragment
import com.meetsportal.meets.utils.Constant.PREFRENCE_PROFILE
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.verifyIcon

class LikeAdapter (var context : Activity, var fragment: LikeFragment,var listener : (String?,Boolean)-> Unit,var openProfile : (String?)->Unit
) : PagingDataAdapter<PostLikerResponseItem, LikeAdapter.RviewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder {
        return RviewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.card_like, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        fragment.noData.visibility = View.GONE
        var data = getItem(position)
        if(!data?.user_meta?.first_name.isNullOrEmpty())
            holder.name.text = data?.user_meta?.first_name.plus(" ${data?.user_meta?.last_name}")
        else
            holder.name.text = data?.user_meta?.username

        holder.username.text = "@".plus("${data?.user_meta?.username}")

       // Utils.stickImage(context,holder.image,data?.user_meta?.profile_image_url,null)
        holder.image.loadImage(context,data?.user_meta?.profile_image_url,R.drawable.ic_default_person,false)
        holder.ivDpBadge.setImageResource(Utils.getBadge(data?.user_meta?.badge).foreground)
        holder.name.verifyIcon(data?.user_meta?.verified_user)
//        if(data?.user_meta?.verified_user == true){
//            holder.verifyIcon.visibility = View.VISIBLE
//        }else{
//            holder.verifyIcon.visibility = View.GONE
//        }
        setFolowed(holder,data)
        /*if(data?.followed_by_user == true){
            holder.followUnfollow.text = "Unfollow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(context,R.color.primaryDark))
            var bg = ContextCompat.getDrawable(context,R.drawable.horizontal_round_shape) as GradientDrawable
            holder.followUnfollow.setBackgroundDrawable(bg)
        }else{
            holder.followUnfollow.text = "Follow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(context,R.color.white))
            holder.followUnfollow.background = ContextCompat.getDrawable(context,R.drawable.round_border_primary_bg)
        }*/

        var myId = PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?.cust_data?.sid
        if(data?.user_meta?.sid.equals(myId)){
            holder.followUnfollow.visibility = View.GONE
        }else{
            holder.followUnfollow.visibility = View.VISIBLE
        }

        addClickevent(holder,data)

    }

    //fun setFolowed(following: Boolean?, isBlocked: Boolean?) {
    fun setFolowed(holder: RviewHolder, data: PostLikerResponseItem?) {
        if(data?.blocked_by_you == false) {
            if(data.followed_by_user == true) {
                holder.followUnfollow.text = "Unfollow"
                holder.followUnfollow.setTextColor(ContextCompat.getColor(context,R.color.primaryDark))
                var bg = ContextCompat.getDrawable(context,R.drawable.horizontal_round_shape) as GradientDrawable
                holder.followUnfollow.setBackgroundDrawable(bg)
            } else {
                holder.followUnfollow.text = "Follow"
                holder.followUnfollow.setTextColor(ContextCompat.getColor(context,R.color.white))
                holder.followUnfollow.background = ContextCompat.getDrawable(context,R.drawable.round_border_primary_bg)
            }
        } else {

            holder.followUnfollow.text = "UnBlock"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(context, R.color.primaryDark))
            holder.followUnfollow.background = ContextCompat.getDrawable(context, R.drawable.horizontal_round_shape)
        }
    }

    private fun addClickevent(holder: RviewHolder, data: PostLikerResponseItem?) {
        /*Utils.onClick(holder.followUnfollow,500){
            if(holder.followUnfollow.text.equals("Unfollow"))
                listener(data?.user_meta?.sid,false)
            else if(holder.followUnfollow.text.equals("Follow")){
                listener(data?.user_meta?.sid,true)
            }
            toggleFollow(holder,data)
        }
*/
       Utils.onClick(holder.followUnfollow, 1000) {
            if(holder.followUnfollow.text.equals("Unfollow"))
                fragment.profileViewmodel.unfollowUser(data?.user_meta?.sid)
            else if(holder.followUnfollow.text.equals("Follow")) {
                //if(data?.user_meta.social.blocked == false) {
                fragment.profileViewmodel.followUser(data?.user_id)
                /*} else {
                    Toast.makeText(requireContext(), "You cannot follow...", Toast.LENGTH_SHORT)
                        .show()
                }*/
            } else if(holder.followUnfollow.text.equals("UnBlock")){
                val showProceed = fragment.showProceed { fragment.profileViewmodel.unBlockUser(data?.user_id) }
                showProceed.setMessage("Unblock", "Are you sure you want to Unblock this user?")
            }
        }

        holder.root.onClick( {
            openProfile(data?.user_meta?.sid)
        })
    }

    /*fun openProfile(data: PostLikerResponseItem?) {
        var myId = PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?.cust_data?.sid
        if(data?.user_meta?.sid == myId){
            Navigation.addFragment(
                context,
                ProfileFragment(), TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
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

    private fun toggleFollow(holder: RviewHolder, data: PostLikerResponseItem? ) {
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
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
        var image = itemView.findViewById<ImageView>(R.id.image)
    }

    companion object{
        private val COMPARATOR = object : DiffUtil.ItemCallback<PostLikerResponseItem>(){
            override fun areItemsTheSame(
                oldItem: PostLikerResponseItem,
                newItem: PostLikerResponseItem
            ): Boolean {
                return (oldItem._id == newItem._id)
            }

            override fun areContentsTheSame(
                oldItem: PostLikerResponseItem,
                newItem: PostLikerResponseItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

}
