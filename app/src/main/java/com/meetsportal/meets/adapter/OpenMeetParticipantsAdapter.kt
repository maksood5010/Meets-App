package com.meetsportal.meets.adapter

import android.app.Activity
import android.text.format.DateUtils
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
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.meetup.MeetPerson
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.toDate


class OpenMeetParticipantsAdapter(var myContext: Activity,var isRequest:Boolean, var openProfile : (String?)->Unit,val bind: (String?) -> Unit) : RecyclerView.Adapter<OpenMeetParticipantsAdapter.RviewHolder>() {

    val differ: AsyncListDiffer<MeetPerson> = AsyncListDiffer<MeetPerson>(this, DIFF_CALLBACK)

    var attendees = ArrayList<MeetPerson>()
    var date: String? = null
    var alreadyHere:Boolean = false
    val TAG = OpenMeetParticipantsAdapter::class.simpleName


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder = RviewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.card_open_meet_request_item, parent, false), myContext)

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {

        val data = differ.currentList.getOrNull(position)
        val person = attendees.firstOrNull { it.sid == data?.sid }
        attendees.forEach{
            Log.d(TAG, "onBindViewHolder1: ${it.username}")
        }
        Log.d(TAG, "onBindViewHolder2: $person")
        if(person == null) { //waiting
            if(data?.sid == MyApplication.SID) {
                holder.allow.setBackgroundResource(R.drawable.round_border_primary_bg)
                holder.allow.setTextColor(ContextCompat.getColor(myContext, R.color.white))
                date?.toDate()?.time?.let {
                    if(DateUtils.isToday(it)) {
                        holder.allow.visibility = View.VISIBLE
                        holder.allow.text = "I'm Here"
                        holder.allow.onClick({
                            bind("iam_here")
                        })
                    } else {
                        holder.allow.visibility = View.GONE
                    }
                }?:run{
                    holder.allow.visibility = View.INVISIBLE
                }
            //                holder.wait.visibility = View.VISIBLE
            } else {
            //                holder.allow.setBackgroundResource(R.drawable.bg_gray_stroke_25)
            //                holder.allow.setTextColor(ContextCompat.getColor(myContext, R.color.gray4))
            //                holder.allow.text = "Waiting"
                if(isRequest){
                    holder.allow.visibility = View.VISIBLE
                    holder.allow.text = "Remove"
                    holder.allow.onClick({
                        bind(data?.sid)
                    })
                }else{
                    holder.allow.visibility = View.GONE
                }
            }
        } else {//reached
            holder.allow.visibility = View.VISIBLE
            holder.allow.setBackgroundResource(R.drawable.bg_gray_stroke_25)
            holder.allow.setTextColor(ContextCompat.getColor(myContext, R.color.gray4))
            holder.allow.text = "Arrived"
        }
        //Utils.stickImage(myContext, holder.dp, data?.profile_image_url, null)
        holder.dp.loadImage(myContext,data?.profile_image_url,R.drawable.ic_default_person)
        if(data?.first_name.isNullOrEmpty()) holder.name.text = data?.username
        else holder.name.text = data?.first_name?.plus(data?.last_name)
        holder.username.text = data?.username
        if(data?.verified_user == true) holder.verify.visibility = View.VISIBLE
        else holder.verify.visibility = View.GONE
        holder.ivDpBadge.setImageResource(Utils.getBadge(data?.badge).foreground)

        holder.dp.onClick({
            openProfile(data?.sid)
           // openProfile(holder, data)
        })
        holder.name.onClick({
            openProfile(data?.sid)
        })
    }

    /* fun setData(list : GetJoinRequestModel){
         this.list.clear()
         this.list.addAll(list)
         notifyDataSetChanged()
     }*/

    fun submitList(list: List<MeetPerson>?, attendees: List<MeetPerson>?, date: String?,alreadyHere:Boolean) {
        Log.i(TAG, "submitList: products.size is " + list?.size)
        this.date = date
        this.alreadyHere = alreadyHere
        if(attendees != null) {
            this.attendees.clear()
            this.attendees.addAll(attendees)
        }
        differ.submitList(list)
        if(list?.isEmpty() == true){
            bind("empty")
        }else{
            bind(null)
        }
    }
    fun submit(list: List<MeetPerson>?,attendees: List<MeetPerson>?) {
        if(list?.isEmpty() == true){
            bind("empty")
        }else{
            bind(null)
        }
        if(attendees != null) {
            this.attendees.clear()
            this.attendees.addAll(attendees)
        }
        if(differ.currentList==list){
            notifyDataSetChanged()
        }
        differ.submitList(list)
    }
    fun getDiffer(): MutableList<MeetPerson> {
        return differ.currentList
    }

    /*fun openProfile(holder: RviewHolder, data: MeetPerson?) {
        if(data?.sid?.equals(PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.sid) == true) {
            Navigation.addFragment(myContext, ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false)
        } else {
            var baseFragment: BaseFragment = OtherProfileFragment.getInstance(data?.sid)
           // baseFragment = Navigation.setFragmentData(baseFragment, OtherProfileFragment.OTHER_USER_ID, data?.sid)
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