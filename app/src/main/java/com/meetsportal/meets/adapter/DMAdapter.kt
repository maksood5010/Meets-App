package com.meetsportal.meets.adapter

import android.app.Activity
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentOneNOneChatBinding
import com.meetsportal.meets.networking.directmessage.DM
import com.meetsportal.meets.networking.directmessage.DMModel
import com.meetsportal.meets.networking.profile.OtherProfileGetResponse
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.DmImageDetail
import com.meetsportal.meets.ui.fragments.socialfragment.OnenOneChat
import com.meetsportal.meets.ui.fragments.socialfragment.VideoTrimerFragment
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.TAG_DM_IMAGE_DETAIL
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import java.util.concurrent.TimeUnit


class DMAdapter(var myContext: Activity, val fragment: OnenOneChat, var layoutManager: LinearLayoutManager, var binding: FragmentOneNOneChatBinding, private var mp: MediaPlayer) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), SeekBar.OnSeekBarChangeListener {

    private val callBack: Handler.Callback = object : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when(msg.what) {
                MSG_UPDATE_SEEK_BAR -> {
                    playingHolder?.let {
                        try {
                            it.ownSeekBar.progress = mp.currentPosition
                            it.otherSeekBar.progress = mp.currentPosition
                            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100)
                        } catch(e: IllegalStateException) {
                        }
                    }
                    return true
                }
            }
            return false
        }

    }
    var messageThreadSecrets: String? = null
    private var MSG_UPDATE_SEEK_BAR = 1845
    private var playingPosition: Int = -1
    private var playingHolder: AudioViewHolder? = null
    val TAG = DMAdapter::class.java.simpleName
    var dmList: ArrayList<DMModel?> = ArrayList()
    var isItReply = false
    var parentMsg: DMModel.MyDM? = null
    var myProfile: ProfileGetResponse?
    var otherProfile: OtherProfileGetResponse? = null
    val recycler: RecyclerView = binding.recycler

    val fl2f = 15f
    val fl50f = 60f

    var hasCodeOfOG: Int
    var mHandler: Handler = Handler(Looper.getMainLooper(), callBack)
    fun getHandler(): Handler {
        return mHandler
    }

    var runnable: Runnable? = null
    fun getRunnables(): Runnable? {
        return runnable
    }

    init {

        hasCodeOfOG = dmList.hashCode()
        myProfile = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            DIRECT_MESSAGE -> RviewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_direct_message, parent, false))
            /*IMAGE_MESSAGE -> ImageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_image_message, parent, false)
            )*/
            IMAGE_MESSAGE  -> ImageViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_direct_message_image, parent, false))

            VIDEO_MESSAGE  -> VideoViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_direct_message_video, parent, false))
            AUDIO_MESSAGE  -> AudioViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_direct_message_audio, parent, false))

            else           -> RviewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_direct_message, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dmList.get(position)
        Log.i(TAG, " holder.itemViewType:: ${holder.itemViewType}")

        when(holder.itemViewType) {
            DIRECT_MESSAGE -> bindTextMessages(holder as RviewHolder, item as DMModel.MyDM, position) //IMAGE_MESSAGE -> bindImageMessage(holder as ImageViewHolder, item as DMModel.MyDM, position)
            IMAGE_MESSAGE  -> bindImageMessage(holder as ImageViewHolder, item as DMModel.MyDM, position)
            VIDEO_MESSAGE  -> bindVideoMessage(holder as VideoViewHolder, item as DMModel.MyDM, position)
            AUDIO_MESSAGE  -> bindAudioMessage(holder as AudioViewHolder, item as DMModel.MyDM, position)

            else           -> {
                holder.itemView.visibility = View.GONE
            }   //else  -> bindSeparatror(holder as SviewHolder, item as DMModel.SeparatorItem)

        }/*if (item is DMModel.MyDM) {
            when(item.type){
                text->
                "images"-> bindImageMessage(holder as )

            }


        } else if (item is DMModel.SeparatorItem) {
            bindSeparatror(holder as SviewHolder, item as DMModel.SeparatorItem)
        }*/
    }

    override fun getItemCount(): Int {
        return dmList.size
    }

    override fun getItemViewType(position: Int): Int {
        var item = dmList.get(position)
        if(item is DMModel.MyDM) return when(item.type) {
            text     -> DIRECT_MESSAGE
            image    -> IMAGE_MESSAGE
            video    -> VIDEO_MESSAGE
            audio    -> AUDIO_MESSAGE
            "ignore" -> MY_SEPRATOR
            else     -> super.getItemViewType(position)
        }
        return super.getItemViewType(position)

    }

    fun getList(): ArrayList<DMModel?> {
        return dmList
    }

    fun initReply(isItReply: Boolean, i: Int? = null) {
        this.isItReply = isItReply
        binding.attach.isVisible = !isItReply

        binding.send.isVisible=isItReply
        binding.recordView.isVisible=!isItReply
        i?.let {
            parentMsg = getItem(it)
        }
    }


    fun addItems(list: List<DMModel?>) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dmList.removeIf {
                Log.d(TAG, "addReplaceMessage: (it as DMModel.MyDM) ${(it as DMModel.MyDM).getMessage(messageThreadSecrets)}")
                Log.i(TAG," $it")
                (it as DMModel.MyDM).timestamp == null
            }
        }
        var start = dmList.size
        dmList.addAll(list)
        Log.i(TAG, " adding---:: 00 --${dmList.hashCode()} -- ")
        //notifyItemRangeInserted(start, list.size)
        notifyDataSetChanged()
        if(start == 0) recycler.scrollToPosition(0)
    }

    fun addNewDummyMessage(item: DMModel?) {
        Log.i(TAG, " iteminserted:: 0   ${dmList.size}")
        dmList.add(0, item)
        if(dmList.size > 1) {
            notifyItemInserted(0)
            notifyItemChanged(1)
        } else {
            notifyItemInserted(0)
        }
        Log.i(TAG, " iteminserted:: 1   ${dmList.size}")
        recycler.scrollToPosition(0)
    }

    fun showRetry() {
        val item: DMModel.MyDM = getItem(0) ?: return
        item.bool = true
        notifyItemChanged(0)
        Log.i(TAG, "notifyItemChanged:: 1   item.reSend=true")

    }

    fun getItem(it: Int): DMModel.MyDM? {
        return dmList.get(it) as DMModel.MyDM
    }

    fun otherProfile(it: OtherProfileGetResponse) {
        otherProfile = it
    }

    fun addReplaceMessage(list: ArrayList<DM?>) {
        list.sortByDescending { it?.timestamp }
        var b = list.map { DMModel.MyDM(it) } as ArrayList

        Log.i(TAG, " sortByDescending::: ${b.size}")

        Log.i(TAG, " previous:: 1 --- ${dmList}")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dmList.removeIf {
                Log.d(TAG, "addReplaceMessage: (it as DMModel.MyDM).timestamp ${(it as DMModel.MyDM).timestamp}")
                Log.i(TAG," $it")
                (it as DMModel.MyDM).timestamp == null
            }
        }
        Log.i(TAG, " previous:: 2 --- ${dmList}")

        dmList.addAll(0, b)
        Log.i(TAG, " adding---:: ${this.hasCodeOfOG} -- ${dmList.hashCode()}")
        notifyDataSetChanged()

        if(layoutManager.findFirstVisibleItemPosition() < 10) recycler.smoothScrollToPosition(0)
        else {
            Toast.makeText(myContext, "New Message", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bindImageMessage(holder: ImageViewHolder, item: DMModel.MyDM, position: Int) {
        showTime(position, holder.tvDate)
        if(item.senderSid == MyApplication.SID) {
            Log.i(TAG, " itemMessage.timeStamp:: 1")
            holder.llOther.visibility = View.GONE
            holder.llOwn.visibility = View.VISIBLE
            Log.i(TAG, " itemMessage.timeStamp:: 2")
            item.getMessage(messageThreadSecrets)?.let {
                holder.ownDMImage.loadImage(myContext, it, R.drawable.bg_dm_others, false)
            } ?: run {
                if(item.bitmap != null) holder.ownDMImage.setImageBitmap(item.bitmap)
            }
            Log.i(TAG, " itemMessage.timeStamp:: 3")
//            holder.llbg.setBackground(getBackground(position, true, holder.ownTime))
            Log.i(TAG, " itemMessage.timeStamp:: 4")
            item.timestamp?.let {
                holder.progressOwn.visibility = View.GONE
                Log.i(TAG, " itemMessage.timeStamp:: 5")
                holder.ownTime.iconTop(R.drawable.ic_check_in)
                holder.ownTime.text = it.toDate().formatTo("h:mm a")
            } ?: run {
                holder.ownTime.iconTop(0)
                holder.progressOwn.visibility = View.VISIBLE
                holder.ownTime.text = "Sending..."
            }


            holder.ownDMImage.onClick({
                item.getMessage(messageThreadSecrets)?.let {
                    openImage(item,it)
                }
            }, 1000)
        } else {
            holder.llOwn.visibility = View.GONE
            holder.llOther.visibility = View.VISIBLE
            holder.otherTime.text = item.timestamp?.toDate()?.formatTo("h:mm a")
            holder.otherDMImage.loadImage(myContext, item.getMessage(messageThreadSecrets), R.drawable.bg_dm_others, false)
            holder.otherDMImage.onClick({
                item.getMessage(messageThreadSecrets)?.let {
                    openImage(item, it)
                }
            }, 1000)
        }
    }

    private fun bindVideoMessage(holder: VideoViewHolder, item: DMModel.MyDM, position: Int) {
        showTime(position, holder.tvDate)
        if(item.senderSid == MyApplication.SID) {
            Log.i(TAG, " itemMessage.timeStamp:: 1")
            holder.llOther.visibility = View.GONE
            holder.llOwn.visibility = View.VISIBLE
            Log.i(TAG, " itemMessage.timeStamp:: 2")
            item.thumbnail?.let {
                holder.ownDMVideo.loadImage(myContext, it, R.drawable.bg_dm_others, false)
            } ?: run {
                if(item.bitmap != null) holder.ownDMVideo.setImageBitmap(item.bitmap)

            }
            Log.i(TAG, " itemMessage.timeStamp:: 3")
//            holder.llbg.setBackground(getBackground(position, true, holder.ownTime))
            Log.i(TAG, " itemMessage.timeStamp:: 4")
            item.timestamp?.let {
                holder.progressOwn.visibility = View.GONE
                Log.i(TAG, " itemMessage.timeStamp:: 5")
                holder.ownTime.iconTop(R.drawable.ic_check_in)
                holder.ownTime.text = it.toDate().formatTo("h:mm a")
            } ?: run {
                holder.ownTime.iconTop(0)
                holder.progressOwn.visibility = View.VISIBLE
                holder.ownTime.text = "Sending..."
            }

            holder.flOwnVideo.onClick({
                item.getMessage(messageThreadSecrets)?.let {
                    openVideoFragment(it)
                }
            }, 1000)
            if(item.bool) {
                holder.ownRetry.visibility = View.VISIBLE
                holder.ownRetry.setOnClickListener {
                    item.uri?.let {
                        fragment.sendVideoImage(it, item.bitmap, if(isItReply) item.parentMsg else null, false)
                    }
                }
            } else {
                holder.ownRetry.visibility = View.GONE
            }
            item.duration?.let {
                val duration: Long = it.toLong()
                val format = getDuration(duration)
                holder.tvOwnDuration.text = format
            }
        } else {
            holder.llOwn.visibility = View.GONE
            holder.llOther.visibility = View.VISIBLE
            holder.otherTime.text = item.timestamp?.toDate()?.formatTo("h:mm a")

            item.thumbnail?.let {
                holder.otherDMVideo.loadImage(myContext, it, R.drawable.bg_dm_others, false)
            } ?: run {
                if(item.bitmap != null) holder.otherDMVideo.setImageBitmap(item.bitmap)
            }

            holder.flOtherVideo.onClick({
                item.getMessage(messageThreadSecrets)?.let {
                    openVideoFragment(it)
                }
            }, 1000)
//            holder.llotherbg.setBackground(getBackground(position, false, holder.otherTime))
            if(item.bool) {
                holder.otherRetry.visibility = View.VISIBLE
                holder.otherRetry.setOnClickListener {
                    item.uri?.let {
                        fragment.sendVideoImage(it, item.bitmap, if(isItReply) item.parentMsg else null, false)
                    }
                }
            } else {
                holder.otherRetry.visibility = View.GONE
            }

            item.duration?.let {
                val duration: Long = it.toLong()
                val format = getDuration(duration)
                holder.tvOtherDuration.text = format
            }
        }
    }


    private fun bindAudioMessage(holder: AudioViewHolder, item: DMModel.MyDM, position: Int) {

        showTime(position, holder.tvDate)

        holder.root.visibility = View.VISIBLE
        showProgress(item, holder)
        if(item.senderSid == MyApplication.SID) {
            //Your message
            holder.llOther.visibility = View.GONE
            holder.llOwn.visibility = View.VISIBLE

//            holder.llbg.setBackground(getBackground(position, true, holder.ownTime))
            item.timestamp?.let {
                holder.ownTime.iconTop(R.drawable.ic_check_in)
                holder.ownTime.text = it.toDate().formatTo("h:mm a")
            } ?: run {
                holder.ownTime.iconTop(0)
                holder.ownTime.text = "Sending..."
            }
            holder.ownSeekBar.setOnSeekBarChangeListener(this)
            holder.ivPlayOwn.setOnClickListener {
                onPlayOrPause(position, item, holder)
            }
            if(position == playingPosition) {
                playingHolder = holder
                updatePlayingView(holder, item)
            } else {
                updateNonPlayingView(holder, item)
            }
            item.duration?.let {
                val duration: Long = it.toLong()
                val format = getDuration(duration)
                holder.tvOwnDuration.text = format
            }

        } else {
            //other Message
            holder.llOwn.visibility = View.GONE
            holder.llOther.visibility = View.VISIBLE
            holder.otherTime.text = item.timestamp?.toDate()?.formatTo("h:mm a")
//            holder.llotherbg.setBackground(getBackground(position, false, holder.otherTime))


            holder.otherSeekBar.setOnSeekBarChangeListener(this)
            holder.ivPlayOther.setOnClickListener {
                onPlayOrPause(position, item, holder)
            }
            if(position == playingPosition) {
                playingHolder = holder
                updatePlayingView(holder, item)
            } else {
                updateNonPlayingView(holder, item)
            }
            item.duration?.let {
                val duration: Long = it.toLongOrNull() ?: 0L
                val format = getDuration(duration)
                holder.tvOtherDuration.text = format
            }
        }
    }

    private fun onPlayOrPause(position: Int, item: DMModel.MyDM, holder: AudioViewHolder) {
        if(playingPosition == position) {
            if(mp.isPlaying) {
                mp.pause()
            } else {
                mp.start()
            }
        } else {
            // start another audio playback
            playingHolder?.let {
                updateNonPlayingView(it, item)
                Log.d(TAG, "onPlayOrPause:playingHolder not null ")
//                it.otherSeekBar.progress = 0
//                it.ownSeekBar.progress = 0
//                it.ivPlayOwn?.setImageResource(play)
            }
            playingPosition = position
    //                    mp.release()
            playingHolder = holder
            startAudio(holder, item)
        }
        updatePlayingView(holder, item)
    }

    private fun showProgress(item: DMModel.MyDM, holder: AudioViewHolder) {
        if(item.senderSid == MyApplication.SID) {
            if(item.bool) {
                holder.ownFlipper.displayedChild = 2
            } else {
                holder.ownFlipper.displayedChild = 1
            }
        } else {
            if(item.bool) {
                holder.otherFlipper.displayedChild = 2
            } else {
                holder.otherFlipper.displayedChild = 1
            }
        }
    }

    private fun getDuration(duration: Long): String {
        val format = String.format("%d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)))
        return format
    }

    private fun startAudio(holder: AudioViewHolder, item: DMModel.MyDM) {
        mp.reset()
        val message = item.getMessage(messageThreadSecrets)
        if(message=="invalid key"|| message=="something went wrong!!"){
            Log.d(TAG, "startAudio: invalid key")
//            Toast.makeText(myContext, message, Toast.LENGTH_SHORT).show()
            return
        }
        mp?.setDataSource(message)
        mp?.prepare()
        mp.setOnCompletionListener {
            Log.d(TAG, "startAudio:setOnCompletionListener ")
            mHandler.removeMessages(MSG_UPDATE_SEEK_BAR)
            if(item.senderSid == MyApplication.SID) {
                holder.ownSeekBar.progress=0
                holder.ivPlayOwn.setImageResource(play_icon)
            } else {
                holder.otherSeekBar.progress=0
                holder.ivPlayOther.setImageResource(play_icon)
            }
            releaseMediaPlayer()
        }
        mp.start()
    }

    private fun releaseMediaPlayer() {
        playingPosition = -1
//        mp.stop()
    }


    private fun bindTextMessages(holder: RviewHolder, item: DMModel.MyDM, position: Int) {
        holder.root.visibility = View.VISIBLE
        showTime(position, holder.tvDate)
        if(item.senderSid == MyApplication.SID) {
            holder.llOther.visibility = View.GONE
            holder.llOwn.visibility = View.VISIBLE
            if(item.type == text) {
                holder.ownDM.visibility = View.VISIBLE
                holder.ownDM.text = item.getMessage(messageThreadSecrets)
            }

//            holder.llOwn.setBackground(getBackground(position, true))
            item.timestamp?.let {
                holder.ownTime.iconTop(R.drawable.ic_check_in)
                holder.ownTime.text = it.toDate().formatTo("h:mm a")
            } ?: run {
                holder.ownTime.iconTop(0)
                holder.ownTime.text = "Sending..."
            }

            holder.ownPImage.visibility = View.GONE
            holder.ownPbody.visibility = View.GONE
            item.parentMsg?.let {
                if(it.type == text) {
                    holder.ownPImage.visibility = View.GONE
                    holder.ownPbody.isVisible = true
                    holder.ownPbody.text = it.getMessage(messageThreadSecrets)
                } else if(it.type == image) {
                    holder.ownPImage.visibility = View.VISIBLE
                    Utils.stickImage(myContext, holder.ownPImage, it.getMessage(messageThreadSecrets), null)
                } else if(it.type == video) {
                    holder.ownPImage.visibility = View.VISIBLE
                    Utils.stickImage(myContext, holder.ownPImage, it.thumbnail, null)
                } else if(it.type == audio) {
                    holder.ownPbody.visibility = View.VISIBLE
                    holder.ownPbody.iconStart(R.drawable.ic_play_new)
                    holder.ownPbody.text = "Voice Message"
                    it.duration?.let { it1: String ->
                        val duration: Long? = it1.toLongOrNull()
                        val format = getDuration(duration?:0)
                        holder.ownPbody.text = "Voice Message (${format})"
                    }
                }
            } ?: run {
                holder.ownPbody.visibility = View.GONE
                holder.ownPImage.visibility = View.GONE
            }

        } else {
            holder.llOwn.visibility = View.GONE
            holder.otherPbody.visibility = View.GONE
            holder.otherPImage.visibility = View.GONE
            holder.llOther.visibility = View.VISIBLE
            holder.otherTime.text = item.timestamp?.toDate()?.formatTo("h:mm a")
            if(item.type == text) {
                holder.otherDM.visibility = View.VISIBLE
                holder.otherDM.text = item.getMessage(messageThreadSecrets)
            }
//            holder.llOther.setBackground(getBackground(position, false))

            item.parentMsg?.let {
                if(it.type == text) {
                    holder.otherPbody.visibility = View.VISIBLE
                    holder.otherPbody.text = it.getMessage(messageThreadSecrets)
                } else if(it.type == image) {
                    holder.otherPImage.visibility = View.VISIBLE
                    holder.otherPImage.loadImage(myContext, it.getMessage(messageThreadSecrets), R.drawable.ic_person_placeholder)

                } else if(it.type == video) {
                    holder.otherPImage.visibility = View.VISIBLE
                    holder.otherPImage.loadImage(myContext, it.thumbnail, R.drawable.ic_person_placeholder)
                } else if(it.type == audio) {
                    holder.otherPbody.visibility = View.VISIBLE
                    holder.otherPbody.iconStart(R.drawable.ic_play_new)
                    holder.otherPbody.text = "Voice Message"
                    it.duration?.let { it1: String ->
                        val duration: Long = it1.toLongOrNull()?:0L
                        val format = getDuration(duration)
                        holder.otherPbody.text = "Voice Message (${format})"
                    }
                }
            } ?: run {
                holder.otherPbody.visibility = View.GONE
                holder.otherPImage.visibility = View.GONE
            }
        }
    }


    fun openImage(item: DMModel.MyDM, passPhrase: String) {
        if(mp.isPlaying) {
            mp.pause()
        }

        var fragment = DmImageDetail.getInstance(item, binding.name.text as String,passPhrase)
        Navigation.addFragment(myContext, fragment, TAG_DM_IMAGE_DETAIL, R.id.homeFram, true, null)
    }

    private fun openVideoFragment(body: String) {
        if(mp.isPlaying) {
            mp.pause()
        }
        val videoTrimerFragment: BaseFragment = VideoTrimerFragment(body)
        Navigation.addFragment(myContext, videoTrimerFragment, videoTrimerFragment.javaClass.simpleName, R.id.homeFram, true, null)
    }

    fun showTime(position: Int, tvDate: TextView) {
        val current = (dmList.get(position) as DMModel.MyDM)
        val previous = if((position + 1) < dmList.size) (dmList.get(position + 1) as DMModel.MyDM) else {
            val date = Utils.timeStampInChat(current.timestamp?.toDate())
            date?.let {
                tvDate.isVisible = true
                tvDate.text = date
            } ?: run {
                tvDate.isVisible = false
            }
            return
        }
        val fromDate = Utils.removeTime(current.timestamp?.toDate())
        val toDate = Utils.removeTime(previous.timestamp?.toDate())
        val different = Utils.getDayDiff(fromDate, toDate)
        Log.d(TAG, "showTime: different: $different::: \n $toDate::---: $fromDate")

        different?.let {
            if(it > 0) {
                val date = Utils.timeStampInChat(current.timestamp?.toDate())
                date?.let {
                    tvDate.isVisible = true
                    tvDate.text = date
                } ?: run {
                    tvDate.isVisible = false
                }
            } else {
                tvDate.isVisible = false
            }
        } ?: run {
            tvDate.isVisible = false
        }
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var otherDM = itemView.findViewById<TextView>(R.id.others)

        var otherTime = itemView.findViewById<TextView>(R.id.otherTime)
        var ownDM = itemView.findViewById<TextView>(R.id.own)

        var ownTime = itemView.findViewById<TextView>(R.id.ownTime)
        var llOther = itemView.findViewById<ConstraintLayout>(R.id.ll_other)
        var llOwn = itemView.findViewById<ConstraintLayout>(R.id.ll_own)
        var llbg = itemView.findViewById<ConstraintLayout>(R.id.llbgColor)
        var llotherbg = itemView.findViewById<ConstraintLayout>(R.id.llotherbg)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)

        var ownPbody = itemView.findViewById<TextView>(R.id.ownParentBody)
        var ownPImage = itemView.findViewById<ImageView>(R.id.ownParentImage)

        var otherPImage = itemView.findViewById<ImageView>(R.id.otherParentImage)
        var otherPbody = itemView.findViewById<TextView>(R.id.otherParentBody)
        var tvDate = itemView.findViewById<TextView>(R.id.tvDate)
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var otherDMImage = itemView.findViewById<ImageView>(R.id.otherImage)
        var otherTime = itemView.findViewById<TextView>(R.id.otherTime)
        var ownDMImage = itemView.findViewById<ImageView>(R.id.ownImage)
        var ownTime = itemView.findViewById<TextView>(R.id.ownTime)

        var llOther = itemView.findViewById<LinearLayout>(R.id.ll_other)
        var llOwn = itemView.findViewById<LinearLayout>(R.id.ll_own)
        var llbg = itemView.findViewById<LinearLayoutCompat>(R.id.llbgColor)
        var llotherbg = itemView.findViewById<LinearLayoutCompat>(R.id.llotherbg)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var progressOwn = itemView.findViewById<ProgressBar>(R.id.progressOwn)
        var tvDate = itemView.findViewById<TextView>(R.id.tvDate)
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var otherDMVideo: ImageView = itemView.findViewById(R.id.otherVideo)
        var ownDMVideo: ImageView = itemView.findViewById(R.id.ownVideo)

        var flOtherVideo: FrameLayout = itemView.findViewById(R.id.flOtherVideo)
        var flOwnVideo: FrameLayout = itemView.findViewById(R.id.flOwnVideo)

        var ownRetry: TextView = itemView.findViewById(R.id.ownRetry)
        var otherRetry: TextView = itemView.findViewById(R.id.otherRetry)

        var tvOtherDuration: TextView = itemView.findViewById(R.id.tvOtherDuration)
        var tvOwnDuration: TextView = itemView.findViewById(R.id.tvOwnDuration)

        var otherTime: TextView = itemView.findViewById(R.id.otherTime)
        var ownTime: TextView = itemView.findViewById(R.id.ownTime)

        var llOther: LinearLayout = itemView.findViewById(R.id.ll_other)
        var llOwn: LinearLayout = itemView.findViewById(R.id.ll_own)
        var llbg: LinearLayoutCompat = itemView.findViewById(R.id.llbgColor)
        var llotherbg: LinearLayoutCompat = itemView.findViewById(R.id.llotherbg)
        var root: ConstraintLayout = itemView.findViewById(R.id.rootCo)


        var progressOther: ProgressBar = itemView.findViewById(R.id.progressOther)
        var progressOwn: ProgressBar = itemView.findViewById(R.id.progressOwn)
        var tvDate = itemView.findViewById<TextView>(R.id.tvDate)
    }

    class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var otherTime = itemView.findViewById<TextView>(R.id.otherTime)


        var tvOtherDuration: TextView = itemView.findViewById(R.id.tvOtherDuration)
        var tvOwnDuration: TextView = itemView.findViewById(R.id.tvOwnDuration)

        var ownSeekBar: SeekBar = itemView.findViewById(R.id.ownSeekBar)
        var otherSeekBar: SeekBar = itemView.findViewById(R.id.otherSeekBar)

        var ownTime = itemView.findViewById<TextView>(R.id.ownTime)
        var llOther = itemView.findViewById<LinearLayout>(R.id.ll_other)
        var llOwn = itemView.findViewById<LinearLayout>(R.id.ll_own)
        var llbg = itemView.findViewById<LinearLayoutCompat>(R.id.llbgColor)
        var llotherbg = itemView.findViewById<LinearLayoutCompat>(R.id.llotherbg)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)

        var ivPlayOther = itemView.findViewById<ImageView>(R.id.ivPlayOther)
        var ivPlayOwn = itemView.findViewById<ImageView>(R.id.ivPlayOwn)
        var tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        var progressOwn = itemView.findViewById<ProgressBar>(R.id.progressOwn)
        var progressOther = itemView.findViewById<ProgressBar>(R.id.progressOther)
        var ownFlipper = itemView.findViewById<ViewFlipper>(R.id.ownFlipper)
        var otherFlipper = itemView.findViewById<ViewFlipper>(R.id.otherFlipper)

    }


    companion object {

        private const val DIRECT_MESSAGE = 1
        private const val IMAGE_MESSAGE = 2
        private const val VIDEO_MESSAGE = 3
        private const val AUDIO_MESSAGE = 4
        private const val MY_SEPRATOR = 99
        private const val text = "text"
        private const val image = "image"
        private const val video = "video"
        private const val audio = "audio"
    }
    private fun updatePlayingView(holder: AudioViewHolder, item: DMModel.MyDM) {
        Log.d(TAG, "updatePlayingView: mp.currentPosition ${mp.currentPosition}")
        if(item.senderSid == MyApplication.SID) {
            holder.ownSeekBar.max = mp.duration
            holder.ownSeekBar.progress = mp.currentPosition
            if(mp.isPlaying) {
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100)
                holder.ivPlayOwn.setImageResource(pause_icon)
            } else {
                mHandler.removeMessages(MSG_UPDATE_SEEK_BAR)
                holder.ivPlayOwn.setImageResource(play_icon)
            }
        } else {
            holder.otherSeekBar.max = mp.duration
            holder.otherSeekBar.progress = mp.currentPosition
            if(mp.isPlaying) {
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100)
                holder.ivPlayOther.setImageResource(pause_icon)
            } else {
                mHandler.removeMessages(MSG_UPDATE_SEEK_BAR)
                holder.ivPlayOther.setImageResource(play_icon)
            }

        }
    }

    val pause_icon = R.drawable.ic_pause_new
    val play_icon = R.drawable.ic_play_new

    fun updateNonPlayingView(holder: AudioViewHolder, item: DMModel.MyDM) {
        Log.d(TAG, "updateNonPlayingView: updatePlayingView:senderSid= ${item.senderSid==MyApplication.SID}")

        if(holder == playingHolder) {
            mHandler.removeMessages(MSG_UPDATE_SEEK_BAR)
        }
        holder.ivPlayOther.setImageResource(play_icon)
        holder.otherSeekBar.progress = 0
        holder.ivPlayOwn.setImageResource(play_icon)
        holder.ownSeekBar.progress = 0
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if(playingPosition != -1) {
            if(playingPosition == holder.bindingAdapterPosition) {
                playingHolder?.let { getItem(playingPosition)?.let { it1 -> updateNonPlayingView(it, it1) } }
                playingHolder = null
            }
        }
        super.onViewRecycled(holder)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if(fromUser) {
            mp.seekTo(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
}
