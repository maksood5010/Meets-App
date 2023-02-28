package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.meetup.GetMeetUpResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetAttendeeList
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpVotingFragment
import com.meetsportal.meets.utils.*
import org.json.JSONObject

class MeetPreviousAdapter (var myContext: Activity,
                           var fragment: Fragment,
                           var list: GetMeetUpResponse?, ) :
    RecyclerView.Adapter<MeetPreviousAdapter.CardViewHolder>() {

    val TAG = MeetPreviousAdapter::class.simpleName

    var viewPool = RecyclerView.RecycledViewPool()

    /* var meetOptions = BottomSheet.getMeetOption {
         //option(it,list?.getOrNull(cardPosition))
     }*/

    var cardPosition = 0

    /*var meetOptions =  BottomSheetOptions.getInstance("Change Name", "Add to calendar", "Make open Meetup", "Add friend",null,"I’m here","OptOut","Cancel Meetup").apply {
        setBottomSheetLitener(object : BottomSheetOptions.BottomSheetListener{
            override fun bottomSheetClickedOption(buttonClicked: String) {
                option(buttonClicked,list?.getOrNull(cardPosition))
            }
        })
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CardViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.card_previous_meetup, parent, false),myContext
        )

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        var data = list?.get(position)
        Log.i(TAG, " checkingPreviousdata:   ${data}")
        data?.name?.let {
            holder.meetName.setText(it)
        } ?: run {
            if (data?.user_id == MyApplication.SID)
                holder.meetName.setText("Add Name")
            else
                holder.meetName.setText("No Name")
        }

        /*when(data?.status){
            MeetStatus.ACTIVE.status -> holder.rlcancel.visibility = View.GONE
            MeetStatus.CANCELLED.status -> holder.rlcancel.visibility = View.VISIBLE
        }*/

        //Log.i(TAG," data?.places::: ${data}")
        var place = data?.places?.firstOrNull{ it?._id.equals(data.chosen_place?.id) }
        place?.let {
            Utils.stickImage(myContext,holder.image,place.main_image_url,null)
            holder.placeName.text = place.name.en
        }?:run{
            holder.image.setImageResource(R.drawable.ic_bg_default_place_h)
            holder.placeName.text = ""
        }

        holder.date.setText(data?.date?.toDate()?.formatTo("dd MMM yyyy"))
        holder.time.setText(data?.date?.toDate()?.formatTo("hh:mm aa"))
        holder.rvPerson.layoutManager =
            object : LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        holder.rvPerson.adapter = MeetPeopleAdapter(myContext, data?.attendees?.take(4),data){
            Log.i(TAG," ListClicked:::: ")
            var fragment = MeetAttendeeList.getInstance(data?._id,Constant.EnumMeetPerson.ATTENDEE)
            Navigation.addFragment(myContext,fragment,
                    MeetAttendeeList.TAG,R.id.homeFram,true,false)
        }
        holder.rvPerson.setRecycledViewPool(viewPool)
        holder.itemView.onClick( {
            MyApplication.putTrackMP(Constant.AcPreviousDetailChat, JSONObject(mapOf("meetId" to data?._id)))
            var baseFragment : BaseFragment = MeetUpVotingFragment.getInstance(data?._id!!)
            Navigation.addFragment(myContext,baseFragment,
                MeetUpVotingFragment.TAG, R.id.homeFram,true,true)
        })
    }

    /*fun openOption(data: GetMeetUpResponseItem?, position: Int) {
        var invisibleOption = arrayListOf<Int>()
        cardPosition = position
        if(data?.user_id.equals(MyApplication.SID)){
            if(!DateUtils.isToday(data?.date?.toDate()?.time?: Date().time))
                invisibleOption.add(4)
            when(data?.status){
                MeetStatus.CANCELLED.status -> invisibleOption.add(6)//cancel meetUp
            }
            invisibleOption.add(5) //OptOut
            meetOptions.hideOption(invisibleOption.toTypedArray())
            showOption(meetOptions)
        }else{
            invisibleOption.add(0)//change Name
            invisibleOption.add(2)//make Open meetUp
            invisibleOption.add(6)//cancel meetUp
            if(!DateUtils.isToday(data?.date?.toDate()?.time?: Date().time))
                invisibleOption.add(4) //I am here
            meetOptions.hideOption(invisibleOption.toTypedArray())
            showOption(meetOptions)
        }
    }*/

    override fun getItemCount(): Int {
        list?.let {
            return it.size
        } ?: run {
            return 0
        }
    }

    fun setData(list: GetMeetUpResponse?) {
        this.list?.clear()
        this.list?.addAll(list?.take(7)!!)
        notifyDataSetChanged()
    }

    class CardViewHolder(itemView: View, myContext: Activity) : RecyclerView.ViewHolder(itemView) {
        var meetName = itemView.findViewById<TextView>(R.id.meetName)

        //var seeAll = itemView.findViewById<TextView>(R.id.seeAll)
        var root = itemView.findViewById<CardView>(R.id.rootCo)
        var image = itemView.findViewById<ImageView>(R.id.iv_image)
        var date = itemView.findViewById<TextView>(R.id.date)
        var time = itemView.findViewById<TextView>(R.id.time)
        var placeName = itemView.findViewById<TextView>(R.id.placeName)
        var rvPerson = itemView.findViewById<RecyclerView>(R.id.rvPerson)
        var tvCancel = itemView.findViewById<TextView>(R.id.tvCancel).apply {
            background = Utils.getRoundedColorBackground(myContext, R.color.cancelledRed)
        }
        var rlcancel = itemView.findViewById<RelativeLayout>(R.id.rlCancel)
    }

    fun showOption(sheet: BottomSheetDialogFragment?){
        if (sheet == null || sheet.isAdded) {
            return
        }
        sheet.show(this.fragment.childFragmentManager, sheet.tag)
    }

}