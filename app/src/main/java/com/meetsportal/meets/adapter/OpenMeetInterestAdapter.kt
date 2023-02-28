package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.GetJoinRequestModelItem
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick


class OpenMeetInterestAdapter(var myContext: Activity, val bind: (Boolean) -> Unit, var openProfile: (String?) -> Unit) : RecyclerView.Adapter<OpenMeetInterestAdapter.RviewHolder>() {

    val differ: AsyncListDiffer<GetJoinRequestModelItem> = AsyncListDiffer<GetJoinRequestModelItem>(this, DIFF_CALLBACK)


    val TAG = OpenMeetInterestAdapter::class.simpleName


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder = RviewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.card_open_meet_request_item, parent, false), myContext)

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        bind(false)

        val data = differ.currentList.getOrNull(position)
//        holder.wait.visibility = View.GONE
        holder.allow.visibility = View.GONE
       // Utils.stickImage(myContext, holder.dp, data?.user_meta?.profile_image_url, null)
        holder.dp.loadImage(myContext,data?.user_meta?.profile_image_url,R.drawable.ic_default_person)
        if(data?.user_meta?.first_name.isNullOrEmpty()) holder.name.text = data?.user_meta?.username
        else holder.name.text = data?.user_meta?.first_name?.plus(data?.user_meta?.last_name)
        holder.username.text = data?.user_meta?.username
        if(data?.user_meta?.verified_user == true) holder.verify.visibility = View.VISIBLE
        else holder.verify.visibility = View.GONE
        holder.ivDpBadge.setImageResource(Utils.getBadge(data?.user_meta?.badge).foreground)

        holder.dp.onClick({
            this.openProfile(data?.user_meta?.sid)
            //openProfile(holder, data)
        })
        holder.name.onClick({
            this.openProfile(data?.user_meta?.sid)
            //openProfile(holder, data)
        })
    }

    /* fun setData(list : GetJoinRequestModel){
         this.list.clear()
         this.list.addAll(list)
         notifyDataSetChanged()
     }*/

    fun submitList(list: ArrayList<GetJoinRequestModelItem>?) {
        differ.submitList(list)
    }

    /*fun openProfile(holder: RviewHolder, data: GetJoinRequestModelItem?) {
        if(data?.user_meta?.sid?.equals(PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid) == true) {
            Navigation.addFragment(myContext, ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false)
        } else {
            var baseFragment: BaseFragment = OtherProfileFragment.getInstance(data?.user_meta?.sid)
            //baseFragment = Navigation.setFragmentData(baseFragment, OtherProfileFragment.OTHER_USER_ID, data?.user_meta?.sid)
            Navigation.addFragment(myContext, baseFragment, Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false)
        }
    }*/

    override fun getItemCount(): Int {

        //return list.size
        return differ.currentList.size
    }


    class RviewHolder(itemView: View, var myContext: Activity) : RecyclerView.ViewHolder(itemView) {

        var dp = itemView.findViewById<ImageView>(R.id.image)
        var name = itemView.findViewById<TextView>(R.id.name)
        var username = itemView.findViewById<TextView>(R.id.username)
        var allow = itemView.findViewById<TextView>(R.id.allow)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var verify = itemView.findViewById<ImageView>(R.id.verify)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
    }


    companion object {

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GetJoinRequestModelItem>() {
            override fun areItemsTheSame(oldItem: GetJoinRequestModelItem, newItem: GetJoinRequestModelItem): Boolean {
                return oldItem.user_meta?.sid.equals(newItem.user_meta?.sid)
            }

            override fun areContentsTheSame(oldItem: GetJoinRequestModelItem, newItem: GetJoinRequestModelItem): Boolean {
                return oldItem.user_meta?.sid.equals(newItem.user_meta?.sid)
            }

        }


    }
}