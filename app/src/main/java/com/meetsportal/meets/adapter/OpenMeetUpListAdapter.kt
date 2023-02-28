package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.meetup.GetMeetUpResponse
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.meetup.MeetStatus
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.ui.fragments.socialfragment.DetailPostFragment
import com.meetsportal.meets.ui.fragments.socialfragment.OpenMeetIntrestList
import com.meetsportal.meets.ui.fragments.socialfragment.OpenMeetUpListFragment
import com.meetsportal.meets.ui.fragments.socialfragment.RequestToJoinOpenFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.utils.*
import org.json.JSONObject
import java.util.*

class OpenMeetUpListAdapter(
    var myContext: Activity,
    var list: GetMeetUpResponse?,
    var fragment: OpenMeetUpListFragment,
    var option : (String?, GetMeetUpResponseItem?)-> Unit,
) : PagingDataAdapter<GetMeetUpResponseItem, OpenMeetUpListAdapter.CardViewHolder>(
    COMPARATOR
) {
    var cardPosition = 0
    var viewPool = RecyclerView.RecycledViewPool()

    var meetOptions =  BottomSheetOptions.getInstance("Opt out", "Close meetup", "Cancel meetup","Delete", positionForColor = 3).apply {
        setBottomSheetLitener(object : BottomSheetOptions.BottomSheetListener{
            override fun bottomSheetClickedOption(buttonClicked: String) {
                option(buttonClicked,getItem(cardPosition))
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)=
        CardViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.card_core_open_meet, parent, false),
            myContext,viewPool
        )


    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        var data = getItem(position)
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
        holder.join.setTextColor(ContextCompat.getColor(myContext,R.color.white))
        holder.join.visibility = View.VISIBLE
        if(data?.user_id.equals(MyApplication.SID)){
            if(data?.voting_closed == false){
                holder.join.setRoundedColorBackground(myContext,R.color.gray1)
                holder.join.setOnClickListener(null)
                holder.join.visibility = View.INVISIBLE
                holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f)

            }else{
                holder.join.setRoundedColorBackground(myContext,R.color.transparent,)
                holder.join.setOnClickListener(null)
                holder.join.visibility = View.VISIBLE
                holder.join.text = "Closed"
                holder.join.setTextColor(ContextCompat.getColor(myContext,R.color.gray1))
                holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f)
            }
        }else{
            holder.join.visibility = View.VISIBLE
            holder.join.setRoundedColorBackground(myContext,R.color.gray1)
            holder.join.setOnClickListener(null)
            holder.join.text = "Requested"
            holder.join.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14f)
        }

        if(data?.description?.isEmpty() == true){
            holder.desc.text = "No description added"
        }else{
            holder.desc.text = data?.description
        }

        when(data?.chosen_place?.type){
            Constant.PlaceType.MEET.label ->{
                holder.address.text  = data?.places?.firstOrNull()?.name?.en
                holder.address.setOnClickListener {
                    var baseFragment: BaseFragment = RestaurantDetailFragment();
                    Navigation.setFragmentData(baseFragment, "_id", data.places?.firstOrNull()?._id)
                    Navigation.addFragment(
                        myContext, baseFragment,
                        RestaurantDetailFragment.TAG, R.id.homeFram, true, false
                    )
                }
            }
            Constant.PlaceType.CUSTOM.label -> {
                holder.address.setOnClickListener(null)
                holder.address.text = data.custom_places?.firstOrNull()?.name
            }
        }



        if(data?.join_requests?.requests?.size?.compareTo(0) == 1){
            holder.llNoRequest.visibility = View.GONE
            holder.llRequest.visibility = View.VISIBLE
            if(data?.join_requests?.requests?.size == 1)
                holder.countInterested.text = data.join_requests?.requests?.size.toString().plus(" person\ninterested")
            else if(data.join_requests?.requests?.size?.compareTo(1) == 1 )
                holder.countInterested.text = data.join_requests?.requests?.size.toString().plus(" people\ninterested")
            holder.countInterested.onClick({
                if(data.user_id.equals(MyApplication.SID)) {
                    var fragment = RequestToJoinOpenFragment.getInstance(data?._id)
                    Navigation.addFragment(myContext, fragment, RequestToJoinOpenFragment.TAG, R.id.homeFram, true, false)
                }else{
                    var fragment = OpenMeetIntrestList.getInstance(data?._id)
                    Navigation.addFragment(myContext,fragment,"OpenMeetIntrestList",R.id.homeFram,true,false)
                }
            })
            holder.rvIntrested.adapter = OpenMeetJoinRqPeopleStackAdapter(myContext,data.toOpemMeet()){
                if(data.user_id.equals(MyApplication.SID)) {
                    var fragment = RequestToJoinOpenFragment.getInstance(data?._id)
                    Navigation.addFragment(myContext, fragment, RequestToJoinOpenFragment.TAG, R.id.homeFram, true, false)
                }else{
                    var fragment = OpenMeetIntrestList.getInstance(data?._id)
                    Navigation.addFragment(myContext,fragment,"OpenMeetIntrestList",R.id.homeFram,true,false)
                }
            }
        }else{
            holder.llNoRequest.visibility = View.VISIBLE
            holder.llRequest.visibility = View.GONE
            holder.rvIntrested.adapter = OpenMeetJoinRqPeopleStackAdapter(myContext,data?.toOpemMeet())
            if(data?.user_id.equals(MyApplication.SID)) {
                holder.tvFirst.text="Requested\n will displayed here"
            }
        }

        if(data?.status.equals(MeetStatus.CANCELLED.status)){
            holder.rlCancel.visibility = View.VISIBLE
            holder.join.visibility = View.INVISIBLE
            holder.view.visibility = View.GONE
            holder.join.onClick({})
        }

        holder.view.onClick({
            MyApplication.putTrackMP(Constant.AcCardtoDetailOpMeet, JSONObject(mapOf("postId" to data?._id)))
            openDetailPage(data?.post_id)
        })
        holder.rlIntract.onClick({
            MyApplication.putTrackMP(Constant.AcCardtoDetailOpMeet, JSONObject(mapOf("postId" to data?._id)))
            openDetailPage(data?.post_id)
        })
        holder.option.onClick({
            openOption(data,position)
        })
    }

    fun openDetailPage(postId: String?) {
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
        RestaurantDetailFragment.TAG
    }

    fun openOption(data: GetMeetUpResponseItem?, position: Int) {
        cardPosition = position
        var invisibleOption = arrayListOf<Int>()
        Log.i("TAG"," isitMine:: ${data?.user_id} -- ${MyApplication.SID}")
        if(data?.user_id.equals(MyApplication.SID)){
            invisibleOption.add(0)//optout
        }else{
            invisibleOption.add(1); invisibleOption.add(2);invisibleOption.add(3)
        }
        if(Utils.getDayDiff(data?.date?.toDate(), Date())?.compareTo(0) == -1 || data?.voting_closed == true || data?.status.equals(MeetStatus.CANCELLED.status)){
            invisibleOption.add(1)//close Meetup
        }
        if(data?.status.equals(MeetStatus.CANCELLED.status)){
            invisibleOption.add(2)
        }else{
            invisibleOption.add(3)
        }
        meetOptions.hideOption(invisibleOption.toTypedArray())
        showOption(meetOptions)

    }

    fun showOption(sheet: BottomSheetDialogFragment?){
        if (sheet == null || sheet.isAdded) {
            return
        }
        sheet.show(this.fragment.childFragmentManager, sheet.tag)
    }


    class CardViewHolder(itemView: View, myContext: Activity,viewPool: RecyclerView.RecycledViewPool) : RecyclerView.ViewHolder(itemView) {
        var meetName = itemView.findViewById<TextView>(R.id.meetName)
        var desc = itemView.findViewById<TextView>(R.id.desc)
        var address = itemView.findViewById<TextView>(R.id.address)
        var rlIntract = itemView.findViewById<LinearLayout>(R.id.rl_intract).apply {
            setBackgroundColor(ContextCompat.getColor(myContext,R.color.gray1))
        }
        var view = itemView.findViewById<TextView>(R.id.view).apply {
            setRoundedColorBackground(myContext,R.color.transparent,enbleDash = true,strokeHeight = 1f,stripSize = 10f,Dashgap = 0f,strokeColor = R.color.primaryDark)
        }
        var monthOfYear = itemView.findViewById<TextView>(R.id.monthOfYear).apply {
            setGradient(myContext, GradientDrawable.Orientation.TL_BR, intArrayOf(
                ContextCompat.getColor(myContext,R.color.primaryDark),
                ContextCompat.getColor(myContext,R.color.gred_red),
                ContextCompat.getColor(myContext,R.color.gred_red)
            ))
        }
        var option =itemView.findViewById<TextView>(R.id.option).apply {
            visibility = View.VISIBLE
        }
        var rlCancel = itemView.findViewById<CardView>(R.id.rlCancel)
        var tvCancel = itemView.findViewById<TextView>(R.id.tvCancel).apply {
            background = Utils.getRoundedColorBackground(myContext,R.color.cancelledRed)
        }

        var dayOfWeak = itemView.findViewById<TextView>(R.id.dayOfWeak)
        var dayOfMonth = itemView.findViewById<TextView>(R.id.dayOfMonth)
        var timeOfDay = itemView.findViewById<TextView>(R.id.timeOfDay)
        var join = itemView.findViewById<TextView>(R.id.join)
        var countInterested = itemView.findViewById<TextView>(R.id.countInterested)
        var rvIntrested = itemView.findViewById<RecyclerView>(R.id.rvIntrested).apply {
            layoutManager = LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL,false)
            setRecycledViewPool(viewPool)
        }
        var llNoRequest = itemView.findViewById<LinearLayout>(R.id.llNoRequest)
        var tvFirst = itemView.findViewById<TextView>(R.id.tvFirst)
        var llRequest = itemView.findViewById<LinearLayout>(R.id.llRequest)


    }

    companion object{
        private val COMPARATOR = object : DiffUtil.ItemCallback<GetMeetUpResponseItem>(){
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