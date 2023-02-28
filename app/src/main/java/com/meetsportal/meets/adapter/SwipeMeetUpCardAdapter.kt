package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fenchtose.tooltip.Tooltip
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.models.ItemModel
import com.meetsportal.meets.networking.meetup.GetMeetUpResponse
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.networking.meetup.MeetStatus
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpNew
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpVotingFragment
import com.meetsportal.meets.ui.fragments.socialfragment.ParticipantInviteeFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.FilterBottomSheet
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.NewBottomSheet
import com.meetsportal.meets.utils.*
import org.json.JSONObject
import java.util.*

class SwipeMeetUpCardAdapter(var myContext: Activity,
                             var fragment: MeetUpNew,
                             var list: GetMeetUpResponse?,
                             var option : (String, GetMeetUpResponseItem?)-> Unit) :
    RecyclerView.Adapter<SwipeMeetUpCardAdapter.CardViewHolder>() {

    val TAG = SwipeMeetUpCardAdapter::class.simpleName

    var viewPool = RecyclerView.RecycledViewPool()
    var isShown = false

   /* var meetOptions = BottomSheet.getMeetOption {
        //option(it,list?.getOrNull(cardPosition))
    }*/

    var cardPosition = 0

    var optionList: ArrayList<ItemModel> = getOptions()

    private fun getOptions(): ArrayList<ItemModel> {
        val myOptions:ArrayList<ItemModel> = arrayListOf()
        var item= ItemModel("Change Name","Change Name")
        myOptions.add(item)
        item= ItemModel("Add to calendar","Add to calendar")
        myOptions.add(item)
        item= ItemModel("Make open Meetup","Make open Meetup",true)
        myOptions.add(item)
        item= ItemModel("Add friend","Add friend")
        myOptions.add(item)
        item= ItemModel("I’m here","I’m here")
        myOptions.add(item)
        item= ItemModel("Opt Out","Opt Out")
        myOptions.add(item)
        item= ItemModel("Cancel Meetup","Cancel Meetup")
        myOptions.add(item)
        return myOptions
    }

    val filterBottomSheet:NewBottomSheet= NewBottomSheet(myContext,optionList){
        option("$it",list?.getOrNull(cardPosition))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CardViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.card_swipe_meetup, parent, false),myContext
        )

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        var data = list?.get(position)

        if(position==0 && !isShown && fragment.rootCo!=null){
            isShown=true
            Utils.showToolTips(myContext, holder.tvEnterChat, fragment.rootCo!!, Tooltip.BOTTOM, "Click here to enter group chat. Begin your chat with participants here","tvEnterChat") {}
        }
        data?.name?.let {
            holder.meetName.setText(it)
        } ?: run {
            if (data?.user_id == MyApplication.SID)
                holder.meetName.setText("Add Name")
            else
                holder.meetName.setText("No Name")
        }

        when(data?.status){
            MeetStatus.ACTIVE.status -> holder.rlcancel.visibility = View.GONE
            MeetStatus.CANCELLED.status -> holder.rlcancel.visibility = View.VISIBLE
        }

        /*if (data?.user_id == MyApplication.SID){
            Log.i(TAG," EditNameClicked:::  0  ")
            holder.meetName.isFocusable = true
            holder.meetName.isClickable = true
            holder.meetName.isFocusableInTouchMode = true;
            //holder.meetName.background = ContextCompat.getDrawable(myContext,R.drawable.dash)
            //holder.ivUnderLine.visibility = View.VISIBLE
        }else{
            Log.i(TAG," EditNameClicked:::  1  ")
            holder.meetName.isFocusable = false
            holder.meetName.isClickable = false
            //holder.meetName.setBackgroundColor(ContextCompat.getColor(myContext,R.color.white))
            //holder.ivUnderLine.visibility = View.GONE
        }*/

        var place = data?.places?.firstOrNull{it?._id == data.chosen_place?.id}
        place?.let {
            Utils.stickImage(myContext,holder.image,place.main_image_url,null)
            holder.placeName.text = place.name.en
        }?:run{
            holder.image.setImageResource(R.drawable.bg_default_place_v)
            holder.placeName.text = "Yet to Vote"
        }

        /*holder.meetName.setOnClickListener {
            Log.i(TAG," EditNameClicked:::  ----- ${data?.user_id}")

        }*/
        holder.date.setText(data?.date?.toDate()?.formatTo("dd MMM yyyy"))
        holder.time.setText(data?.date?.toDate()?.formatTo("hh:mm aa"))
        holder.rvPerson.layoutManager =
            object : LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        //holder.rvPerson.addItemDecoration(OverlapDecoration())
        holder.rvPerson.adapter = MeetPeopleAdapter(myContext, data?.invitees?.take(4),data){
            MyApplication.putTrackMP(Constant.AcPMInterestedList, JSONObject(mapOf("meetId" to data?._id)))
            var baseFragment : BaseFragment = ParticipantInviteeFragment.getInstance(data?._id!!)
            Navigation.addFragment(myContext,baseFragment,
                    ParticipantInviteeFragment::class.simpleName!!,R.id.homeFram,true,true)
        }
        holder.rvPerson.setRecycledViewPool(viewPool)
        holder.root.onClick( {
            MyApplication.putTrackMP(Constant.AcMeetupEnterChat, JSONObject(mapOf("meetId" to data?._id)))
            var baseFragment : BaseFragment = MeetUpVotingFragment.getInstance(data?._id!!)
            Navigation.addFragment(myContext,baseFragment,
                MeetUpVotingFragment.TAG,R.id.homeFram,true,true)
        })

        holder.option.onClick( {
            openOption(data,position)
        })
        holder.root.setOnLongClickListener {
            openOption(data,position)
            return@setOnLongClickListener true
        }
    }
    val deleteSheet= FilterBottomSheet(myContext, arrayListOf("Delete")){
        if(it==0){
            option("delete", list?.getOrNull(cardPosition))
        }
    }
    fun openOption(data: GetMeetUpResponseItem?, position: Int) {
        var invisibleOption = arrayListOf<Int>()
        invisibleOption.add(2)
        cardPosition = position
        if(data?.status==MeetStatus.CANCELLED.status){
            deleteSheet.show()
            return
        }
        if(data?.user_id.equals(MyApplication.SID)){
//            if(!DateUtils.isToday(data?.date?.toDate()?.time?: Date().time))
                invisibleOption.add(4)
//            when(data?.status){
//                MeetStatus.CANCELLED.status -> {
//                    invisibleOption.add(6)//cancel meetUp
//                }
//            }
            invisibleOption.add(5) //OptOut
//            meetOptions.hideOption(invisibleOption.toTypedArray())
            Log.d(TAG, "hideOption: openOption:1 ")
            filterBottomSheet.hideOption(invisibleOption.toTypedArray())
//            showOption(meetOptions)
            filterBottomSheet.show()
        }else{
            invisibleOption.add(0)//change Name
            invisibleOption.add(3)//add friend
//            if(!DateUtils.isToday(data?.date?.toDate()?.time?: Date().time))
                invisibleOption.add(4) //I am here
            invisibleOption.add(6)//cancel meetUp
//            meetOptions.hideOption(invisibleOption.toTypedArray())
            Log.d(TAG, "hideOption: openOption:2 ")
            filterBottomSheet.hideOption(invisibleOption.toTypedArray())
//            showOption(meetOptions)
            filterBottomSheet.show()
        }
    }

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

    class CardViewHolder(itemView: View,myContext: Activity) : RecyclerView.ViewHolder(itemView) {
        var meetName = itemView.findViewById<TextView>(R.id.meetName)

        //var seeAll = itemView.findViewById<TextView>(R.id.seeAll)
        var root = itemView.findViewById<LinearLayout>(R.id.rootCo)
        var tvEnterChat: TextView = itemView.findViewById(R.id.tvEnterChat)
        var image = itemView.findViewById<ImageView>(R.id.iv_image)
        var date = itemView.findViewById<TextView>(R.id.date)
        var time = itemView.findViewById<TextView>(R.id.time)
        var option = itemView.findViewById<TextView>(R.id.tv_options)
        var placeName = itemView.findViewById<TextView>(R.id.placeName)
        var rvPerson = itemView.findViewById<RecyclerView>(R.id.rvPerson)
        var tvCancel = itemView.findViewById<TextView>(R.id.tvCancel).apply {
            background = Utils.getRoundedColorBackground(myContext,R.color.cancelledRed)
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