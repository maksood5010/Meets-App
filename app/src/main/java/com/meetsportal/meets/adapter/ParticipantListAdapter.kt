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
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.meetup.MeetPerson
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick


class ParticipantListAdapter(var myContext: Activity, var list: List<MeetPerson>, var follow: (String?, Boolean?) -> Unit, val openProfile : (String?)->Unit, val bind: () -> Unit) : RecyclerView.Adapter<ParticipantListAdapter.RviewHolder>() {

    val differ: AsyncListDiffer<MeetPerson> = AsyncListDiffer<MeetPerson>(this, DIFF_CALLBACK)

    val TAG = ParticipantListAdapter::class.simpleName


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder = RviewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.card_like, parent, false), myContext)

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        bind()
        val data = differ.currentList.getOrNull(position)
        //Utils.stickImage(myContext, holder.dp, data?.profile_image_url, null)
        holder.dp.loadImage(myContext,data?.profile_image_url,R.drawable.ic_default_person)
        holder.name.text = data?.username
        holder.username.text = data?.username
        if(data?.verified_user == true) holder.verify.visibility = View.VISIBLE
        else holder.verify.visibility = View.GONE
        holder.ivDpBadge.setImageResource(Utils.getBadge(data?.badge).foreground)

        holder.dp.onClick({
            openProfile(data?.sid)
            //openProfile(holder, data)
        })
        holder.name.onClick({
            Utils.hideSoftKeyBoard(myContext, holder.root)
            openProfile(data?.sid)
            //openProfile(holder, data)
        })

        holder.followUnfollow.onClick({
            Utils.hideSoftKeyBoard(myContext, holder.root)
            follow(data?._id, null)
        })
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
        Utils.onClick(holder.followUnfollow,500){
            if(holder.followUnfollow.text.equals("Unfollow"))
                follow(data?.sid,false)
            else if(holder.followUnfollow.text.equals("Follow")){
                follow(data?.sid,true)
            }
            toggleFollow(holder)
        }
    }

    private fun toggleFollow(holder: RviewHolder) {
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

    fun submitList(products: List<MeetPerson?>) {
        Log.i(TAG, "submitList: products.size is " + products.size)
        differ.submitList(products)
    }

    /*fun openProfile(holder: RviewHolder, data: MeetPerson?) {
        Utils.hideSoftKeyBoard(myContext, holder.root)
        if(data?.user_id?.equals(PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid) == true) {
            Navigation.addFragment(myContext, ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false)
        } else {
            var baseFragment: BaseFragment = OtherProfileFragment.getInstance(data?.user_id)
            //baseFragment = Navigation.setFragmentData(baseFragment, OtherProfileFragment.OTHER_USER_ID, data?.user_id)
            Navigation.addFragment(myContext, baseFragment, Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false)
        }
    }*/

    override fun getItemCount(): Int {

        //return list.size
        return differ.currentList.size
    }


    class RviewHolder(itemView: View,var myContext: Activity) : RecyclerView.ViewHolder(itemView) {

        var dp = itemView.findViewById<ImageView>(R.id.image)
        var name = itemView.findViewById<TextView>(R.id.name)
        var username = itemView.findViewById<TextView>(R.id.username)
        var followUnfollow = itemView.findViewById<TextView>(R.id.followUnfollow)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var verify = itemView.findViewById<ImageView>(R.id.verify)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
    }


    companion object {

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MeetPerson>() {
            override fun areItemsTheSame(oldItem: MeetPerson, newItem: MeetPerson): Boolean {
                return oldItem.sid.equals(newItem.sid)
            }

            override fun areContentsTheSame(oldItem: MeetPerson, newItem: MeetPerson): Boolean {
                return oldItem.sid.equals(newItem.sid)
            }

        }
    }
}