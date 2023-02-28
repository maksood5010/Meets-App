package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
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
import com.meetsportal.meets.models.GetJoinRequestModel
import com.meetsportal.meets.models.GetJoinRequestModelItem
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground


class OpenMeetRequestAdapter(var myContext: Activity, var list: GetJoinRequestModel, val isRequest: Boolean = false, var approve: (String?, Boolean?) -> Unit, val openProfile :(String?)->Unit,val bind: (String?) -> Unit) : RecyclerView.Adapter<OpenMeetRequestAdapter.RviewHolder>() {

    val differ: AsyncListDiffer<GetJoinRequestModelItem> = AsyncListDiffer<GetJoinRequestModelItem>(this, DIFF_CALLBACK)

    val TAG = OpenMeetRequestAdapter::class.simpleName
    lateinit var type: Constant.RequestType


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder = RviewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.card_open_meet_request_item, parent, false), type, myContext)

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        val data = differ.currentList.getOrNull(position)
        //Utils.stickImage(myContext, holder.dp, data?.user_meta?.profile_image_url, null)
        holder.dp.loadImage(myContext,data?.user_meta?.profile_image_url,R.drawable.ic_default_person)
        holder.name.text = data?.user_meta?.username
        holder.username.text = data?.user_meta?.username
        if(data?.user_meta?.verified_user == true) holder.verify.visibility = View.VISIBLE
        else holder.verify.visibility = View.GONE
        holder.ivDpBadge.setImageResource(Utils.getBadge(data?.user_meta?.badge).foreground)

        holder.dp.onClick({
            openProfile(data?.user_id)
            //openProfile(holder, data)
        })
        holder.name.onClick({
            Utils.hideSoftKeyBoard(myContext, holder.root)
            openProfile(data?.user_id)
            //openProfile(holder, data)
        })

        holder.allow.onClick({
            Utils.hideSoftKeyBoard(myContext, holder.root)
            approve(data?._id, null)
        })
    }



    fun setRequestType(type: Constant.RequestType) {
        this.type = type
        Log.d(TAG, "setRequestType: type::: $type")
    }

    fun submitList(products: List<GetJoinRequestModelItem?>) {
        Log.i(TAG, "submitList: products.size is " + products.size)
        if(products.isEmpty()){
            bind("empty")
        }else{
            bind(null)
        }
        if(differ.currentList==products){
            notifyDataSetChanged()
        }
        differ.submitList(products)
    }

    /*fun openProfile(holder: RviewHolder, data: GetJoinRequestModelItem?) {
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


    class RviewHolder(itemView: View, var reuestType: Constant.RequestType, var myContext: Activity) : RecyclerView.ViewHolder(itemView) {

        var dp = itemView.findViewById<ImageView>(R.id.image)
        var name = itemView.findViewById<TextView>(R.id.name)
        var username = itemView.findViewById<TextView>(R.id.username)
        var allow = itemView.findViewById<TextView>(R.id.allow).apply {
            when(reuestType) {
                Constant.RequestType.PENDING  -> {
                    text = "Allow"
                }

                Constant.RequestType.ACCEPTED -> {
                    text = "Remove"
                    setRoundedColorBackground(myContext, R.color.gray1)
                }

                Constant.RequestType.ALL      -> {
                    text = "Follow"
                }
            }
        }
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var verify = itemView.findViewById<ImageView>(R.id.verify)
        var ivDpBadge = itemView.findViewById<ImageView>(R.id.ivDpBadge)
    }


    companion object {

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GetJoinRequestModelItem>() {
            override fun areItemsTheSame(oldItem: GetJoinRequestModelItem, newItem: GetJoinRequestModelItem): Boolean {
                return oldItem.user_id.equals(newItem.user_id)
            }

            override fun areContentsTheSame(oldItem: GetJoinRequestModelItem, newItem: GetJoinRequestModelItem): Boolean {
                return oldItem.user_id.equals(newItem.user_id)
            }

        }


    }
}