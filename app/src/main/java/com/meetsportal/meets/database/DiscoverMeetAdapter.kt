package com.meetsportal.meets.database

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.OpenMeetJoinRqPeopleStackAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.profile.Badge
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.ui.fragments.socialfragment.DetailPostFragment
import com.meetsportal.meets.ui.fragments.socialfragment.OpenMeetIntrestList
import com.meetsportal.meets.ui.fragments.socialfragment.RequestToJoinOpenFragment
import com.meetsportal.meets.ui.fragments.socialfragment.ShowMapFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.viewmodels.MeetUpViewModel
import org.json.JSONObject

class DiscoverMeetAdapter(
    val myContext: Activity,
    val meetUpViewModel: MeetUpViewModel
) : PagingDataAdapter<GetMeetUpResponseItem, DiscoverMeetAdapter.CardViewHolder>(COMPARATOR) {

    val TAG = DiscoverMeetAdapter::class.simpleName
    var viewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DiscoverMeetAdapter.CardViewHolder {
        return CardViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_core_open_meet, parent, false), myContext, viewPool
        )
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {

        var data = getItem(position)
        Log.i(TAG, " checkingPreviousdata:   ${data}")
        data?.name?.let {
            holder.meetName.setText(it)
        } ?: run {
            if (data?.user_id == MyApplication.SID)
                holder.meetName.setText("Add Name")
            else
                holder.meetName.setText("No Name")
        }

        holder.monthOfYear.text = data?.date?.toDate()?.formatTo("MMM")
        holder.dayOfMonth.text = data?.date?.toDate()?.formatTo("dd")
        holder.dayOfWeak.text = data?.date?.toDate()?.formatTo("EEEE")
        holder.timeOfDay.text = data?.date?.toDate()?.formatTo("hh:mmaa")
        holder.join.setTextColor(ContextCompat.getColor(myContext, R.color.white))
        holder.join.visibility = View.VISIBLE
        if (data?.user_id.equals(MyApplication.SID)) {
            if (data?.voting_closed == false) {
                holder.join.setRoundedColorBackground(myContext, R.color.gray1)
                holder.join.setOnClickListener(null)
                holder.join.visibility = View.INVISIBLE
                holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)

            } else {
                holder.join.setRoundedColorBackground(myContext, R.color.transparent)
                holder.join.setOnClickListener(null)
                holder.join.visibility = View.VISIBLE
                holder.join.text = "Closed"
                holder.join.setTextColor(ContextCompat.getColor(myContext, R.color.gray1))
                holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
            }
        } else {
            holder.join.visibility = View.VISIBLE
            if(data?.voting_closed == false) {
                if(data?.join_accepted_by_user == true){
                    holder.join.setRoundedColorBackground(myContext, R.color.gray1)
                    holder.join.setOnClickListener(null)
                    holder.join.text = "Accepted"
                    holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                }
                else if(data?.join_requested_by_user == true){
                    holder.join.setRoundedColorBackground(myContext, R.color.gray1)
                    holder.join.setOnClickListener(null)
                    holder.join.text = "Requested"
                    holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                }else{
                    holder.join.text = "Join"
                    holder.join.setRoundedColorBackground(myContext, R.color.primaryDark)
                    holder.join.onClick( {
                        val badge: Badge = Utils.getBadge(data?.min_badge)
                        val currentUser = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
                        val currentBadge: Badge = Utils.getBadge(currentUser?.social?.getbadge())
                        if(currentBadge.level>=badge.level){
                            meetUpViewModel.joinOpenMeet(data._id)
                        }else{
                            Toast.makeText(myContext, "${badge.name} Status and above are eligible for join this meetup", Toast.LENGTH_SHORT).show()
                        }
                    })
                    holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                }

            }else{
                if(data?.join_accepted_by_user == true){
                    holder.join.setRoundedColorBackground(myContext, R.color.gray1)
                    holder.join.setOnClickListener(null)
                    holder.join.text = "Accepted"
                    holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                }
                else if(data?.join_requested_by_user == true){
                    holder.join.setRoundedColorBackground(myContext, R.color.gray1)
                    holder.join.setOnClickListener(null)
                    holder.join.text = "Requested"
                    holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                }
                else{
                    holder.join.setRoundedColorBackground(myContext, R.color.transparent)
                    holder.join.setOnClickListener(null)
                    holder.join.visibility = View.VISIBLE
                    holder.join.text = "Closed"
                    holder.join.setTextColor(ContextCompat.getColor(myContext, R.color.gray1))
                    holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                }

            }

        }

        if (data?.description?.isEmpty() == true) {
            holder.desc.text = "No description added"
        } else {
            holder.desc.text = data?.description
        }

        when (data?.chosen_place?.type) {
            Constant.PlaceType.MEET.label -> {
                holder.address.text = data?.places?.firstOrNull()?.name?.en
                holder.address.onClick( {
                    var baseFragment: BaseFragment = RestaurantDetailFragment();
                    Navigation.setFragmentData(baseFragment, "_id", data.places?.firstOrNull()?._id)
                    Navigation.addFragment(
                        myContext, baseFragment,
                        RestaurantDetailFragment.TAG, R.id.homeFram, true, false
                    )
                })
            }
            Constant.PlaceType.CUSTOM.label -> {
                val firstOrNull = data.custom_places?.firstOrNull()?:return
                holder.address.setOnClickListener(null)
                holder.address.text = firstOrNull?.name
                holder.address.onClick({
                    var baseFragment: BaseFragment = ShowMapFragment.getInstance(firstOrNull?.getLatitude(), firstOrNull?.getLongitude())
                    Navigation.addFragment(myContext, baseFragment, RestaurantDetailFragment.TAG, R.id.homeFram, true, false)
                })
            }
        }



        if (data?.join_requests?.requests?.size?.compareTo(0) == 1) {
            holder.llNoRequest.visibility = View.GONE
            holder.llRequest.visibility = View.VISIBLE
            if (data?.join_requests?.requests?.size == 1)
                holder.countInterested.text =
                    data.join_requests?.requests?.size.toString().plus(" person\ninterested")
            else if (data.join_requests?.requests?.size?.compareTo(1) == 1)
                holder.countInterested.text =
                    data.join_requests?.requests?.size.toString().plus(" people\ninterested")
            holder.countInterested.onClick({
                if(data.user_id.equals(MyApplication.SID)){
                    Log.i(TAG," itsMine::: data.post_id : ${data._id}")
                    val fragment = RequestToJoinOpenFragment.getInstance(data._id)
                    Navigation.addFragment(myContext,fragment, RequestToJoinOpenFragment.TAG,R.id.homeFram,true,false)
                }else{
                    val fragment = OpenMeetIntrestList.getInstance(data._id)
                    Navigation.addFragment(myContext,fragment,"OpenMeetIntrestList",R.id.homeFram,true,false)
                }
            })
            holder.rvIntrested.adapter =
                OpenMeetJoinRqPeopleStackAdapter(myContext, data.toOpemMeet()){
                    if(data.user_id.equals(MyApplication.SID)){
                        Log.i(TAG," itsMine::: data.post_id : ${data.post_id}")
                        val fragment = RequestToJoinOpenFragment.getInstance(data.post_id)
                        Navigation.addFragment(myContext,fragment, RequestToJoinOpenFragment.TAG,R.id.homeFram,true,false)
                    }else{
                        val fragment = OpenMeetIntrestList.getInstance(data._id)
                        Navigation.addFragment(myContext,fragment,"OpenMeetIntrestList",R.id.homeFram,true,false)
                    }
                }

        } else {
            holder.llNoRequest.visibility = View.VISIBLE
            holder.llRequest.visibility = View.GONE
            holder.rvIntrested.adapter =
                OpenMeetJoinRqPeopleStackAdapter(myContext, data?.toOpemMeet())
            //Toast.makeText(myContext, "Requested", Toast.LENGTH_SHORT).show()
            if(data?.user_id.equals(MyApplication.SID)) {
                holder.tvFirst.text="Requested\n will displayed here"
            }
        }

        holder.view.onClick({
            MyApplication.putTrackMP(Constant.AcCardtoDetailOpMeet, JSONObject(mapOf("postId" to data?._id)))
            openDetailPage(data?.post_id)
        })
        holder.rlIntract.onClick({
            MyApplication.putTrackMP(Constant.AcCardtoDetailOpMeet, JSONObject(mapOf("postId" to data?._id)))
            openDetailPage(data?.post_id)
        })

    }

    fun openDetailPage(postId: String?) {
        postId?.let {
            var baseFragment: BaseFragment = DetailPostFragment()
            Navigation.setFragmentData(
                baseFragment,
                "post_id",
                postId
            )
            Navigation.addFragment(
                myContext,
                baseFragment,
                Constant.TAG_DETAIL_POST_FRAGMENT,
                R.id.homeFram,
                true,
                false
            )
        }?:run{
            MyApplication.showToast(myContext,"Oops!!! Invalid Post... ",Toast.LENGTH_SHORT)
        }

    }

    class CardViewHolder(
        itemView: View,
        myContext: Activity,
        viewPool: RecyclerView.RecycledViewPool
    ) : RecyclerView.ViewHolder(itemView) {
        var meetName = itemView.findViewById<TextView>(R.id.meetName)
        var desc = itemView.findViewById<TextView>(R.id.desc)
        var address = itemView.findViewById<TextView>(R.id.address)
        var rlIntract = itemView.findViewById<LinearLayout>(R.id.rl_intract).apply {
            setBackgroundColor(ContextCompat.getColor(myContext, R.color.gray1))
        }
        var view = itemView.findViewById<TextView>(R.id.view).apply {
            setRoundedColorBackground(
                myContext,
                R.color.transparent,
                enbleDash = true,
                strokeHeight = 1f,
                stripSize = 10f,
                Dashgap = 0f,
                strokeColor = R.color.primaryDark
            )
        }
        var monthOfYear = itemView.findViewById<TextView>(R.id.monthOfYear).apply {
            setGradient(
                myContext, GradientDrawable.Orientation.TL_BR, intArrayOf(
                    ContextCompat.getColor(myContext, R.color.primaryDark),
                    ContextCompat.getColor(myContext, R.color.gred_red),
                    ContextCompat.getColor(myContext, R.color.gred_red)
                )
            )
        }
        var dayOfWeak = itemView.findViewById<TextView>(R.id.dayOfWeak)
        var dayOfMonth = itemView.findViewById<TextView>(R.id.dayOfMonth)
        var timeOfDay = itemView.findViewById<TextView>(R.id.timeOfDay)
        var join = itemView.findViewById<TextView>(R.id.join)
        var countInterested = itemView.findViewById<TextView>(R.id.countInterested)
        var  rvIntrested = itemView.findViewById<RecyclerView>(R.id.rvIntrested).apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false)
            setRecycledViewPool(viewPool)
        }
        var llNoRequest = itemView.findViewById<LinearLayout>(R.id.llNoRequest)
        var tvFirst = itemView.findViewById<TextView>(R.id.tvFirst)
        var llRequest = itemView.findViewById<LinearLayout>(R.id.llRequest)

        /*//var seeAll = itemView.findViewById<TextView>(R.id.seeAll)
        var root = itemView.findViewById<CardView>(R.id.root)
        var image = itemView.findViewById<ImageView>(R.id.iv_image)
        var date = itemView.findViewById<TextView>(R.id.date)
        var time = itemView.findViewById<TextView>(R.id.time)
        var placeName = itemView.findViewById<TextView>(R.id.placeName)
        var rvPerson = itemView.findViewById<RecyclerView>(R.id.rvPerson)
        var tvCancel = itemView.findViewById<TextView>(R.id.tvCancel).apply {
            background = Utils.getRoundedColorBackground(myContext, R.color.cancelledRed)
        }
        var rlcancel = itemView.findViewById<RelativeLayout>(R.id.rlCancel)*/
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<GetMeetUpResponseItem>() {
            override fun areItemsTheSame(
                oldItem: GetMeetUpResponseItem,
                newItem: GetMeetUpResponseItem
            ): Boolean {
                return (oldItem._id == newItem._id)
            }

            override fun areContentsTheSame(
                oldItem: GetMeetUpResponseItem,
                newItem: GetMeetUpResponseItem
            ): Boolean {
                return oldItem == newItem
            }

        }

    }


}