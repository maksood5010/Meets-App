package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetupVotingBinding
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.networking.meetup.ChatDM
import com.meetsportal.meets.networking.meetup.MeetPerson
import com.meetsportal.meets.networking.meetup.Place
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import java.util.*
import kotlin.collections.ArrayList

class MeetChatAdapter(
    var myContext: Activity,
    var recycler: RecyclerView,
    var layoutManager: LinearLayoutManager,
    var binding: FragmentMeetupVotingBinding,
    var placePreView : (String?)->Unit,
    var openProfile:(String?)->Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG = MeetChatAdapter::class.simpleName
    var dmList: ArrayList<ChatDM?> = ArrayList()

    var myProfile: ProfileGetResponse?

    var isItReply = false
    var parentMsg: ChatDM? = null

    var participants: List<MeetPerson>? = null
    var places = ArrayList<Place?>()
    var customPlaces = ArrayList<AddressModel>()
    lateinit var colorArray: IntArray

    var initial: ArrayList<ChatDM?>

    val fl2f = 15f
    val fl3f = 3.5f
    val fl50f = 50f

    init {
        myProfile = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
        initial = dmList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            DIRECT_MESSAGE -> RviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_meet_message, parent, false)
            )
            IMAGE_MESSAGE -> VviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_voted_message, parent, false)
            )
            NOTIFY -> NotiViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_meet_notification, parent, false),myContext
            )
            NO_VIEW -> NoViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_view, parent, false)
            )
            else -> RviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_meet_message, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dmList.getOrNull(position)
        Log.i(TAG, " holder.itemViewType:: ${holder.itemViewType}")
        when(holder){
            is RviewHolder ->  {
                Log.i(TAG," checking :: 1")
                bindTextMessages(holder , item, position)
                binding.lottiAnim.visibility = View.GONE
            }
            is VviewHolder -> {
                bindVotes(holder , item, position)
                binding.lottiAnim.visibility = View.GONE
            }
            is NotiViewHolder -> {
                bindNotification(holder , item, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        var item = dmList.get(position)
        if (item is ChatDM)
            return when (item.type) {
                "text" -> DIRECT_MESSAGE
                "image" -> IMAGE_MESSAGE
                "notification"-> NOTIFY
                "ignore" -> NO_VIEW
                else -> super.getItemViewType(position)
            }
        return super.getItemViewType(position)

    }

    fun getList(): ArrayList<ChatDM?> {
        return dmList
    }


    fun getItem(it: Int): ChatDM? {
        return dmList.get(it)
    }

    fun initReply(isItReply: Boolean, i: Int? = null) {
        this.isItReply = isItReply
        i?.let {
            parentMsg = getItem(it)
        }
    }


    fun addNewDummyMessage(item: ChatDM?) {
        Log.i(TAG, " iteminserted:: 0   ${dmList.size}")
        dmList.add(0, item)
        //notifyDataSetChanged()
        notifyItemInserted(0)
        Log.i(TAG, " iteminserted:: 1   ${dmList.size}")
        recycler.scrollToPosition(0)
    }

    fun getItemChat(it: Int): ChatDM? {
        return dmList.get(it)
    }

    fun addReplaceMessage(list: ArrayList<ChatDM?>) {
        Log.i(TAG," addReplaceMessage0000::: ${list}")
        list.sortByDescending { it?.timestamp }
//        var b = list.map { ChatDM(it) } as ArrayList


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dmList.removeIf {
                it?.timestamp == null
            }
        }
        dmList.addAll(0, list)
        //notifyItemRangeInserted(0,list.size)
        notifyDataSetChanged()

        Log.i(TAG, " visibleItem:: ${layoutManager.findFirstVisibleItemPosition()}")
        if (layoutManager.findFirstVisibleItemPosition() < 10) {
            Log.i(TAG, " scrollingSmootly::: ")
            //recycler.smoothScrollToPosition(0)
            Handler(Looper.getMainLooper()).postDelayed({
                recycler.smoothScrollToPosition(0)
            }, 1)
            //recycler.smoothScrollToPosition(0)
        } else {
            Toast.makeText(myContext, "New Message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bindTextMessages(
        holder: RviewHolder,
        item: ChatDM?,
        position: Int
    ) {
        holder.otherDp.setOnClickListener(null)
        holder.otSenderName.setOnClickListener(null)
        holder.root.visibility = View.VISIBLE
        Log.i(TAG, " item.timeStamp:: ${item?.body} --- ${item?.timestamp}")
        if (item?.senderSid == MyApplication.SID) {
            holder.llOther.visibility = View.GONE
            holder.llOwn.visibility = View.VISIBLE
            when (item?.type) {
                "text" -> {
                    holder.ownDM.visibility = View.VISIBLE
                    holder.ownDMImageCard.visibility = View.GONE
//                    holder.ownTime.visibility = View.VISIBLE
                    holder.ownDM.text = item.body
                    //holder.ownDM.isSingleLine = true
                    //holder.ownDM.ellipsize = (TextUtils.TruncateAt.END)
                }
            }

            holder.ownDMImage.setOnClickListener {
                item?.body?.let {
                    //Open Detail of
                    // openImage(item)
                }
            }

            var type = getBackground(position, true, item?.type, holder.ownTime)
            holder.otherDp.visibility = View.INVISIBLE
            //holder.otherDp.
            holder.llbg.setBackground(type.second)
            holder.otherDp.getLayoutParams().height = Utils.dpToPx(20f,myContext.resources)

            item?.timestamp?.let {
                //holder.ownTime.visibility = View.VISIBLE
                holder.ownTime.text = Utils.fireTimeStamptoAgo(item?.timestamp?.toDate())
            } ?: run {
                holder.ownTime.text = "Sending..."
            }

            item?.parentMsg?.let { chat ->
                holder.ownProot.visibility = View.VISIBLE
                holder.ownPuser.text =
                    if (chat.senderSid == MyApplication.SID) myProfile?.cust_data?.username else participants?.firstOrNull { it.sid == chat.senderSid }?.username
                        ?: ""
                when (chat.type) {
                    "text" -> {
                        holder.ownPImage.visibility = View.GONE
                        holder.ownPbody.text = chat.body
                    }
                    "image" -> {
                        /*holder.ownPImage.visibility = View.VISIBLE
                        Utils.stickImage(myContext, holder.ownPImage, chat.body, null)
                        places.firstOrNull { it._id == chat.body }?.let{
                            holder.ownPbody.text =
                                "\uD83D\uDCF7 ${it.name?.en}"
                        }?:run{
                            holder.ownPbody.text = customPlaces.firstOrNull { it._id == chat.body }?.name
                        }*/
                        holder.ownPImage.visibility = View.GONE
                        var text : String = ""
                        Log.i(TAG, "bindTextMessages: Image:: 1 $customPlaces")
                        if(chat?.senderSid?.equals(MyApplication.SID) == true){
                            places.firstOrNull { it?._id == chat.body }?.let {
                                text = "You".plus(" voted ").plus(it.name.en)
                            }?:run{
                                text = "You".plus(" voted ").plus(customPlaces.firstOrNull { it._id == item.body }?.name)
                            }
                        }
                        else{
                            places.firstOrNull { it?._id == chat.body }?.let {
                                text  = participants?.firstOrNull { it.sid == chat.senderSid }?.username.plus(" voted ").plus(it?.name?.en)
                            }?:run{
                                text  = participants?.firstOrNull { it.sid == chat.senderSid }?.username.plus(" voted ").plus(customPlaces.firstOrNull { it._id == chat.body }?.name)
                            }
                        }
                        //holder.ownPbody.text = chat.body
                        holder.ownPbody.text = text
                    }
                }
                //holder.ownPbody.text = it.body
            } ?: run {
                holder.ownProot.visibility = View.GONE
            }

            //holder.ownDM.text = item.body
        } else {
            holder.llOwn.visibility = View.GONE
            holder.llOther.visibility = View.VISIBLE
            holder.otherTime.text = Utils.fireTimeStamptoAgo(item?.timestamp?.toDate()?: Date())
            when (item?.type) {
                "text" -> {
                    holder.otherDM.visibility = View.VISIBLE
                    holder.otherDMImageCard.visibility = View.GONE
                    holder.otherDM.text = item?.body
                }
            }

            holder.otherDMImage.setOnClickListener {
                item?.body?.let {
                    //Open Place
                    //openImage(item)
                }
            }
            holder.otSenderName.text = participants?.firstOrNull { it.sid == item?.senderSid }?.username
            var type = getBackground(position, false, item?.type, holder.otherTime)
            if (type.first) {
                holder.otherDp.visibility = View.VISIBLE
                holder.otherDp.getLayoutParams().height = Utils.dpToPx(35f,myContext.resources)
                holder.otSenderName.visibility = View.VISIBLE
                /*Utils.stickImage(
                    myContext,
                    holder.otherDp,
                    participants?.firstOrNull { it.sid == item.senderSid }?.profile_image_url,
                    null
                )*/
                holder.otherDp.loadImage(myContext,participants?.firstOrNull { it.sid == item?.senderSid }?.profile_image_url,R.drawable.ic_default_person)
                holder.otherDp.onClick( {
                    openProfile(item?.senderSid)
                })
                holder.otSenderName.onClick( {
                    openProfile(item?.senderSid)
                })
            } else {
                holder.otherDp.visibility = View.INVISIBLE
                holder.otSenderName.visibility = View.GONE
                holder.otherDp.getLayoutParams().height = Utils.dpToPx(20f,myContext.resources)
            }
            holder.llotherbg.setBackground(type.second)


            item?.parentMsg?.let { chat ->
                holder.otherProot.visibility = View.VISIBLE
                holder.otherPuser.text =
                    if (chat.senderSid == MyApplication.SID) myProfile?.cust_data?.username else participants?.firstOrNull { it.sid == chat.senderSid }?.username
                        ?: ""

                when(chat.type){
                    "text" -> {
                        holder.otherPbody.text = chat.body
                    }
                    "image" ->{
                        var text : String = ""
                        Log.i(TAG," debugkc::: 66 ${item.body}")
                        if(chat?.senderSid?.equals(MyApplication.SID) == true){
                            places.firstOrNull { it?._id == chat.body }?.let {
                                text = "You".plus(" voted ").plus(it.name.en)
                                Log.i(TAG," debugkc::: 01 ${it.name.en}")
                            }?:run{
                                text = "You".plus(" voted ").plus(customPlaces.firstOrNull { it._id == item.body }?.name)
                                Log.i(TAG," debugkc::: 02 ${customPlaces.firstOrNull { it._id == item.body }?.name}")
                            }
                        }
                        else{
                            Log.i(TAG, "bindTextMessages: Image:: 3 $customPlaces")
                            places.firstOrNull { it?._id == chat.body }?.let {
                                text  = participants?.firstOrNull { it.sid == chat.senderSid }?.username.plus(" voted ").plus(it?.name?.en)
                                Log.i(TAG," debugkc::: 03 ${it?.name?.en}")
                            }?:run{
                                Log.i(TAG, "bindTextMessages: compare ${item}")
                                text  = participants?.firstOrNull { it.sid == chat.senderSid }?.username.plus(" voted ").plus(customPlaces.firstOrNull { it._id == chat.body }?.name)
                                Log.i(TAG," debugkc::: 04 ${customPlaces.firstOrNull { it._id == chat.body }?.name}")
                            }
                        }
                        Log.i(TAG," debugkc::: 88 $text")
                        //holder.ownPbody.text = chat.body
                        holder.otherPbody.text = text
                    }
                }

            } ?: run {
                holder.otherProot.visibility = View.GONE
            }
        }
    }

    /*fun openProfile(item: ChatDM) {
        if(item.senderSid.equals(MyApplication.SID)){
            Navigation.addFragment(
                myContext,
                ProfileFragment(),
                Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        }else{
            var baseFragment : BaseFragment = OtherProfileFragment.getInstance(item.senderSid)
            *//*baseFragment = Navigation.setFragmentData(
                baseFragment,
                OtherProfileFragment.OTHER_USER_ID,
                item.senderSid
            )*//*
            Navigation.addFragment(
                myContext, baseFragment,
                Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        }
    }*/

    fun bindVotes(holder: VviewHolder, item: ChatDM?, position: Int) {
        item?.let {
            holder.voteText.setText( getVotedText(participants?.firstOrNull { it.sid == item.senderSid }?.sid == MyApplication.SID,item),TextView.BufferType.SPANNABLE)
            holder.voteText.movementMethod = LinkMovementMethod.getInstance()
            holder.date.text = Utils.fireTimeStamptoAgo(item?.timestamp?.toDate())
        }
    }

    fun bindNotification(holder: NotiViewHolder, item: ChatDM?, position: Int) {
        item?.let {
            Log.i(TAG," ${it.body}")
            Log.i(TAG,"replaced::  ${it.body?.replace(myProfile?.cust_data?.username!!,"you")?.replace("has","have")}")

            if(it.body?.contains(myProfile?.cust_data?.username?:"xtwq") == true){
                holder.notiText.text = it.body?.replace(myProfile?.cust_data?.username!!,"you")?.replace("has","have")
            }else{
                holder.notiText.text = it.body
            }
            holder.date.text = Utils.fireTimeStamptoAgo(item?.timestamp?.toDate())
        }
    }

    override fun getItemCount(): Int {
        return dmList.size
    }

    fun getVotedText(isItOwn: Boolean, item: ChatDM,):SpannableString{
        var text : String = ""
        Log.i(TAG, "bindTextMessages: Image:: 2 $customPlaces")
        if(isItOwn){
            places.firstOrNull { it?._id == item.body }?.let {
                text = "You".plus(" voted ").plus(it.name.en)
            }?:run{
                text = "You".plus(" voted ").plus(customPlaces.firstOrNull { it._id == item.body }?.name)
            }
        }
        else{
            places.firstOrNull { it?._id == item.body }?.let {
                text  = participants?.firstOrNull { it.sid == item.senderSid }?.username.plus(" voted ").plus(it?.name?.en)
            }?:run{
                text  = participants?.firstOrNull { it.sid == item.senderSid }?.username.plus(" voted ").plus(customPlaces.firstOrNull { it._id == item.body }?.name)
            }
        }

        var spannable = SpannableString(text)

        val votedPlace: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                Log.i(TAG," PlaceClicked:: ")
                openProfile(item.senderSid)
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }

        val userName: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                Log.i(TAG," PlaceClicked:: ")
                places.firstOrNull{ it?._id == item.body }?.let{
                    placePreView(it._id)
                }
                customPlaces.firstOrNull{ it._id == item.body }?.let{
                    placePreView(it._id)
                }
            }
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        spannable.setSpan(userName,text.lastIndexOf("voted").plus(5),text.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(votedPlace,0,text.lastIndexOf("voted"),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)



        if(places.indexOfFirst { it?._id == item?.body } != -1){
            val col = colorArray.getOrNull(places.indexOfFirst { it?._id == item?.body })
            col?.let{
                spannable.setSpan(
                        ForegroundColorSpan(it),
                        text.lastIndexOf("voted").plus(5),
                        text.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                 )
            }
        }else{
            val col = colorArray.getOrNull(customPlaces.indexOfFirst { it._id == item?.body }
                    .plus(places.size))
            col?.let{
                spannable.setSpan(
                        ForegroundColorSpan(it),
                        text.lastIndexOf("voted").plus(5),
                        text.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                 )
            }
        }

        spannable.setSpan( StyleSpan(Typeface.BOLD),text.lastIndexOf("voted").plus(5),text.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan( StyleSpan(Typeface.BOLD),0,text.lastIndexOf("voted"),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    fun addItems(list: List<ChatDM?>) {
        Log.i(TAG, "addReplaceMessage111:::: ${list}")
        var start = dmList.size
        dmList.addAll(list)
        notifyItemRangeInserted(start, list.size)
        if (start == 0)
            recycler.scrollToPosition(0)
    }

    fun setAdditionData(
        participants: List<MeetPerson>?,
        places: List<Place?>?,
        customPlaces : List<AddressModel>?,
        colorArray: IntArray
    ) {
        this.colorArray = colorArray
        this.participants = participants
        if (!places.isNullOrEmpty()) {
            this.places.clear()
            this.places.addAll(places)
        }
        if(!customPlaces.isNullOrEmpty()){
            this.customPlaces.clear()
            this.customPlaces.addAll(customPlaces)
        }
    }

    fun getBackGroundForVote() {

    }


    /**
     * return :Pair< ShouldShowDp : Boolean,Background : GradientDrawable>
     *
     */
    fun getBackground(
        position: Int,
        isitOwn: Boolean,
        type: String?,
        tvTime: TextView
    ): Pair<Boolean, GradientDrawable> {
        var shape = GradientDrawable()

        var current = dmList.get(position)
        var previous =
            if ((position + 1) < dmList.size) (dmList.get(position + 1)) else null
        var further = if ((position - 1) >= 0) (dmList.get(position - 1)) else null


        //above and below both is same sender
        if (previous != null && further != null &&
            previous.senderSid == current?.senderSid &&
            further.senderSid == current?.senderSid
        ) {
            tvTime.visibility = View.GONE
            if (isitOwn) {
                shape.cornerRadii = floatArrayOf(fl50f, fl50f, fl2f, fl2f, fl2f, fl2f, fl50f, fl50f)
                shape.setColor(ContextCompat.getColor(myContext, R.color.blacktextColor))
            } else {
                shape.cornerRadii = floatArrayOf(fl2f, fl2f, fl50f, fl50f, fl50f, fl50f, fl2f, fl2f)
                shape.setColor(ContextCompat.getColor(myContext, R.color.darkerGray))
            }
            //if(previous)
            if(previous.type.equals("text"))
                return Pair(false, shape)
            else
                return Pair(true, shape)
        }
        //previous same but below not same sender
        else if (previous != null &&
            previous.senderSid == current?.senderSid
        ) {
            tvTime.visibility = View.VISIBLE
            if (isitOwn) {
                shape.cornerRadii = floatArrayOf(fl50f, fl50f, fl2f, fl2f, fl50f, fl50f, fl50f, fl50f)
                shape.setColor(ContextCompat.getColor(myContext, R.color.blacktextColor))
            } else {
                shape.cornerRadii = floatArrayOf(fl2f, fl2f, fl50f, fl50f, fl50f, fl50f, fl50f, fl50f)
                shape.setColor(ContextCompat.getColor(myContext, R.color.darkerGray))
            }
            if(previous.type.equals("text"))
                return Pair(false, shape)
            else
                return Pair(true, shape)

        }
        //below is same but above is not same sender
        else if (further != null &&
            further.senderSid == current?.senderSid
        ) {
            tvTime.visibility = View.GONE
            if (isitOwn) {
                shape.cornerRadii = floatArrayOf(fl50f, fl50f, fl50f, fl50f, fl2f, fl2f, fl50f, fl50f)
                shape.setColor(ContextCompat.getColor(myContext, R.color.blacktextColor))
                return Pair(false, shape)
            } else {
                shape.cornerRadii = floatArrayOf(fl50f, fl50f, fl50f, fl50f, fl50f, fl50f, fl2f, fl2f)
                shape.setColor(ContextCompat.getColor(myContext, R.color.darkerGray))
                return Pair(true, shape)
            }
        }

        //not above not below sender same
        shape.cornerRadii = floatArrayOf(fl50f, fl50f, fl50f, fl50f, fl50f, fl50f, fl50f, fl50f)
        tvTime.visibility = View.VISIBLE
        if (isitOwn) {
            shape.setColor(ContextCompat.getColor(myContext, R.color.blacktextColor))
            return Pair(false, shape)
        } else {
            shape.setColor(ContextCompat.getColor(myContext, R.color.darkerGray))
            return Pair(true, shape)
        }

    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var otherDM = itemView.findViewById<TextView>(R.id.others)
        var otherDMImage = itemView.findViewById<ImageView>(R.id.otherImage)
        var otherDMImageCard = itemView.findViewById<CardView>(R.id.otherImageCard)
        var otherDp = itemView.findViewById<ImageView>(R.id.senderImage)
        var otSenderName = itemView.findViewById<TextView>(R.id.username)
        var otherVoted = itemView.findViewById<TextView>(R.id.otherVoted)
        var otherPlacName = itemView.findViewById<TextView>(R.id.otherPlaceName)

        var otherTime = itemView.findViewById<TextView>(R.id.otherTime)
        var ownDM = itemView.findViewById<TextView>(R.id.own)
        var ownDMImage = itemView.findViewById<ImageView>(R.id.ownImage)
        var ownDMImageCard = itemView.findViewById<CardView>(R.id.ownImageCard)
        var ownVoted = itemView.findViewById<TextView>(R.id.ownVoted)
        var ownPlaceName = itemView.findViewById<TextView>(R.id.ownPlaceName)

        var ownTime = itemView.findViewById<TextView>(R.id.ownTime)
        var llOther = itemView.findViewById<LinearLayout>(R.id.ll_other)
        var llOwn = itemView.findViewById<LinearLayout>(R.id.ll_own)
        var llbg = itemView.findViewById<LinearLayoutCompat>(R.id.llbgColor)
        var llotherbg = itemView.findViewById<LinearLayoutCompat>(R.id.llotherbg)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)

        var ownProot = itemView.findViewById<LinearLayout>(R.id.ownParentRoot)
        var ownPuser = itemView.findViewById<TextView>(R.id.ownParentUser)
        var ownPbody = itemView.findViewById<TextView>(R.id.ownParentBody)
        var ownPImage = itemView.findViewById<ImageView>(R.id.ownParentImage)

        var otherProot = itemView.findViewById<LinearLayout>(R.id.otherParentRoot)
        var otherPuser = itemView.findViewById<TextView>(R.id.otherParentUser)
        var otherPbody = itemView.findViewById<TextView>(R.id.otherParentBody)
    }

    class VviewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var voteText = itemView.findViewById<TextView>(R.id.votingText)
        var date = itemView.findViewById<TextView>(R.id.date)
    }

    class NoViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

    }

    class NotiViewHolder(itemView: View,var myContext: Activity):RecyclerView.ViewHolder(itemView) {
        var notiText = itemView.findViewById<TextView>(R.id.notifyText).apply {
            background = Utils.getRoundedColorBackground(myContext,R.color.extraLightGray)
        }
        var date = itemView.findViewById<TextView>(R.id.date)

    }

    companion object {
        private const val DIRECT_MESSAGE = 1
        private const val IMAGE_MESSAGE = 2
        private const val NO_VIEW = 3
        const val NOTIFY = 50
    }
}