package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
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
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpListFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpVotingFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.FilterBottomSheet
import com.meetsportal.meets.utils.*
import org.json.JSONObject
import java.util.*

class MeetUpListAdapter(
    var myContext: Activity,
    var list: GetMeetUpResponse?,
    var type :Int,
    var fragment: MeetUpListFragment,
    var option : (String?,GetMeetUpResponseItem?)-> Unit,
    var click : (String?)-> Unit={},
) : PagingDataAdapter<GetMeetUpResponseItem, MeetUpListAdapter.CardViewHolder>(
    COMPARATOR
) {
    val TAG = MeetUpListAdapter::class.simpleName

    var cardPosition = 0
    var cartItem :GetMeetUpResponseItem? = null

    var viewPool = RecyclerView.RecycledViewPool()
    /*var meetOptions = BottomSheet.getMeetOption { button,position->
        Log.i(TAG," meetOptionsmeetOptions:: ${position} ${button} ${getItem(cardPosition)}")
        option(button,getItem(position))
    }
*/
    var meetOptions =  BottomSheetOptions.getInstance("Change Name", "Add to calendar", "Make open Meetup", "Add friend",null,"I’m here","Opt Out","Cancel Meetup").apply {
        setBottomSheetLitener(object : BottomSheetOptions.BottomSheetListener{
            override fun bottomSheetClickedOption(buttonClicked: String) {
                try {
                    option(buttonClicked,getItem(cardPosition))
                } catch(e: Exception) {
                }
            }
        })
    }
    val filterSheet=FilterBottomSheet(myContext, arrayListOf("Delete")){
        if(it==0){
            option("delete", cartItem)
//            Log.d(TAG, "Index out of bound :cardPosition:$cartItem:in:$itemCount")
//            try {
//            } catch(e: IndexOutOfBoundsException) {
//                Log.d(TAG, "Index out of bound :cardPosition:$cardPosition:in:$itemCount")
//            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_meetup, parent, false),myContext)

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        var data = getItem(position)
        Log.i(TAG," meetName:: ${position} ${data?.name}")
        data?.name?.let {
            holder.meetName.setText(it)
        }?:run{
            holder.meetName.setText("No Name")
        }
        when(data?.status){
            MeetStatus.ACTIVE.status -> holder.rlcancel.visibility = View.GONE
            MeetStatus.CANCELLED.status -> holder.rlcancel.visibility = View.VISIBLE
        }
        var place = data?.places?.firstOrNull{it?._id == data?.chosen_place?.id}
        place?.let {
            Utils.stickImage(myContext,holder.image,place.main_image_url,null)
            holder.placeName.text = place.name.en
        }?:run{
            holder.image.setImageResource(R.drawable.bg_default_place_v)
            holder.placeName.text = "Yet to Vote"
        }
        holder.date.setText(data?.date?.toDate()?.formatTo("dd MMM yyyy"))
        holder.time.setText(data?.date?.toDate()?.formatTo("hh:mm aa"))
        holder.rvPerson.layoutManager = object : LinearLayoutManager(myContext,
            LinearLayoutManager.HORIZONTAL,false){
            override fun canScrollVertically(): Boolean { return false }
        }
        //holder.rvPerson.addItemDecoration(OverlapDecoration())
        holder.rvPerson.adapter = MeetPeopleAdapter(myContext,data?.invitees?.take(4),data){
            MyApplication.putTrackMP(Constant.AcMeetupListInterested, JSONObject(mapOf("meetId" to data?._id)))
            click(data?._id)
        }
        holder.rvPerson.setRecycledViewPool(viewPool)
        holder.root.onClick({
            MyApplication.putTrackMP(Constant.AcMeetupListEnterChat, JSONObject(mapOf("meetId" to data?._id)))
            var baseFragment: BaseFragment = MeetUpVotingFragment.getInstance(data?._id!!)
            Navigation.addFragment(myContext, baseFragment, MeetUpVotingFragment.TAG, R.id.homeFram, true, true)
        })
        holder.option.setOnClickListener {
            Log.i(TAG," setOnClickListener:: ${position}")
            cartItem = data
            openOption(data,position)
            /*BottomSheet.openOption(data,position){
                showOption(it)
            }*/
        }
        holder.root.setOnLongClickListener {
//            cardPosition = position
            cartItem = data
            openOption(data,position)
            /*BottomSheet.openOption(data,position){
                showOption(it)
            }*/
            return@setOnLongClickListener true
        }
    }

    fun openOption(data: GetMeetUpResponseItem?, position: Int) {
        cardPosition = position
        Log.i(TAG," cardPosition:::: $cardPosition")
        if(data?.status == MeetStatus.CANCELLED.status){
            filterSheet.show()
            return
        }
        if(type==0){
            var invisibleOption = arrayListOf<Int>()
            invisibleOption.add(2)
            if(data?.user_id.equals(MyApplication.SID)) {
                Log.d(TAG, "openOption: data?.status ${data?.status}")
//                if(!DateUtils.isToday(data?.date?.toDate()?.time ?: Date().time))
                    invisibleOption.add(4)
                when(data?.status) {
                    MeetStatus.CANCELLED.status -> {
                        invisibleOption.add(6)//cancel meetUp
                        invisibleOption.add(3)
                    }
                }
                invisibleOption.add(5) //OptOut
                meetOptions.hideOption(invisibleOption.toTypedArray())
                showOption(meetOptions)
            } else {
                invisibleOption.add(0)//change Name
                invisibleOption.add(6)//cancel meetUp
                Log.d(TAG, "openOption: data?.status ${data?.status}")
                if(data?.status == MeetStatus.CANCELLED.status) {
                    invisibleOption.add(3)
                }
//                if(!DateUtils.isToday(data?.date?.toDate()?.time ?: Date().time))
                    invisibleOption.add(4) //I am here
                meetOptions.hideOption(invisibleOption.toTypedArray())
                showOption(meetOptions)
            }
        }else{
            filterSheet.show()
        }
    }

    class CardViewHolder(itemView: View, myContext: Activity) : RecyclerView.ViewHolder(itemView){
        var meetName = itemView.findViewById<EditText>(R.id.meetName)
        var date = itemView.findViewById<TextView>(R.id.date)
        var time = itemView.findViewById<TextView>(R.id.time)
        var image = itemView.findViewById<ImageView>(R.id.iv_image)
        var root = itemView.findViewById<CardView>(R.id.rootCo)
        var placeName = itemView.findViewById<TextView>(R.id.placeName)
        var rvPerson = itemView.findViewById<RecyclerView>(R.id.rvPerson)
        var option = itemView.findViewById<TextView>(R.id.tv_options)
        var tvCancel = itemView.findViewById<TextView>(R.id.tvCancel).apply {
            background = Utils.getRoundedColorBackground(myContext,R.color.cancelledRed)
        }
        var rlcancel = itemView.findViewById<RelativeLayout>(R.id.rlCancel)
        //61516cbe8d2e0700131ac5fb
    }

    fun showOption(sheet: BottomSheetDialogFragment?){
        if (sheet == null || sheet.isAdded) {
            return
        }
        sheet.show(this.fragment.childFragmentManager, sheet.tag)
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