package com.meetsportal.meets.ui.fragments.socialfragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.fenchtose.tooltip.Tooltip
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.DMAdapter
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.application.MyApplication.Companion.SID
import com.meetsportal.meets.databinding.FragmentOneNOneChatBinding
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.directmessage.DM
import com.meetsportal.meets.networking.directmessage.DMModel
import com.meetsportal.meets.networking.firebase.FireBaseUtils
import com.meetsportal.meets.networking.profile.OtherProfileGetResponse
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.overridelayout.StickerEditText
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.FilterBottomSheet
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Constant.PREFRENCE_PROFILE
import com.meetsportal.meets.viewmodels.FireBaseViewModal
import com.meetsportal.meets.viewmodels.UserAccountViewModel
import com.tougee.recorderview.AudioRecordView
import com.videotrimmer.library.utils.CompressOption
import com.videotrimmer.library.utils.TrimType
import com.videotrimmer.library.utils.TrimVideo
import com.videotrimmer.library.utils.TrimmerUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException


@AndroidEntryPoint
class OnenOneChat() : BaseFragment(), AudioRecordView.Callback {

    private var messageThreadSecrets: String? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var isMessage: Boolean = true
    private var recorder: MediaRecorder? = null
    private var mp: MediaPlayer = MediaPlayer()
    private var audioPath: String? = null

    val fireViewModel: FireBaseViewModal by viewModels()
    val userAccountViewModel: UserAccountViewModel by viewModels()

    val TAG = OnenOneChat::class.java.simpleName
    var otherprofile: OtherProfileGetResponse? = null
    var image_uri: Uri? = null
    var videoUrl: String? = null

    lateinit var dmAdapter: DMAdapter
    lateinit var layoutManager: LinearLayoutManager
    var statusListener: ListenerRegistration? = null
    var chatListener: ListenerRegistration? = null
    var parentChatListener: ListenerRegistration? = null
    var chatNodeChanged = false
    var loading = false

    private var _binding: FragmentOneNOneChatBinding? = null
    private val binding get() = _binding!!

    var mostPreviousTimeStamp: Timestamp? = null
    var deletedMsgTimestamp: Timestamp? = null

    init {
        hideKey = false
    }

    companion object {

        val OTHERS_PROFILE_SID = "othersProfile"

        private const val text = "text"
        private const val image = "image"
        private const val video = "video"
        private const val audio = "audio"

        fun getInstance(otherProfileSID: String?): OnenOneChat {
            var bundle = Bundle()
            bundle.putString(OTHERS_PROFILE_SID, otherProfileSID)
            return OnenOneChat().apply { arguments = bundle }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOneNOneChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        setUp()
        addObserver()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initView(view: View) {
        Log.d(TAG, "Checking the flow : 1:: init")
        view.setOnTouchListener { v, event ->
            true
        }
        Utils.showToolTips(requireActivity(), binding.ivCreateMeetUp, binding.mainRoot, Tooltip.TOP, "Click here to create a meetup\nWith this person", "ivCreateMeetUp") {}
        binding.send.isVisible = false
        binding.recordView.isVisible = true
        layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, true).apply { stackFromEnd = false }
        binding.recycler.layoutManager = layoutManager
        dmAdapter = DMAdapter(requireActivity(), this@OnenOneChat, layoutManager, binding, mp) //binding.recycler.adapter = adapter
        binding.recycler.adapter = dmAdapter
        binding.recycler.setHasFixedSize(true)

        binding.recordView.apply {
            activity = requireActivity()
            callback = this@OnenOneChat
        }
        binding.recordView.setTimeoutSeconds(120)

        binding.message.onText {
            if(it.isNotEmpty()) {
                binding.send.isVisible = true
                binding.recordView.isVisible = false
            } else {
                binding.send.isVisible = false
                binding.recordView.isVisible = true
            }
        }

        binding.message.setKeyBoardInputCallbackListener(object : StickerEditText.KeyBoardInputCallbackListener {
            override fun onCommitContent(inputContentInfo: InputContentInfoCompat, flag: Int, opts: Bundle?) {

                val myDM = if(dmAdapter.isItReply) dmAdapter.parentMsg else null
                var imageBitmap = Utils.uriToBitmap(inputContentInfo.contentUri, requireContext())

                Log.i(TAG, " sizeOFiamges:: -- ${imageBitmap}")
                fireViewModel.uploaDMImages(ArrayList<Bitmap?>().apply { add(imageBitmap) })

                dmAdapter.addNewDummyMessage(DMModel.MyDM(DM(id = "", type = image, senderSid = SID, parentMsg = myDM, bitmap = imageBitmap)))
                dmAdapter.initReply(false)
                //Log.d(TAG, "onCommitContent: 3 =  ${inputContentInfo.contentUri}")
            }

        })
    }

    private fun setUp() {
        if(arguments != null) {
            var sid = requireArguments().getString(OTHERS_PROFILE_SID)
            userAccountViewModel.getOtherProfile(sid)
        }
        binding.back.setOnClickListener {
            Utils.hideSoftKeyBoard(requireContext(), it)
            activity?.onBackPressed()
        }
        val sheet = FilterBottomSheet(requireActivity(), arrayListOf("Camera", "Gallery", "Video")) {
            when(it) {
                0 -> {
                    if(Utils.checkPermission(requireContext(), Manifest.permission.CAMERA)) {
                        val values = ContentValues()
                        values.put(MediaStore.Images.Media.TITLE, "New Picture")
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
                        image_uri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
                        startActivityForResult(cameraIntent, Constant.CAMERA_REQUEST)
                    } else {
                        //ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), Constant.CAMERA_REQUEST)
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            Constant.CAMERA_REQUEST
                        )
                    }
                }

                1 -> {
                    val photoPickerIntent = Intent(Intent.ACTION_PICK)
                    photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    photoPickerIntent.type = "image/*"
                    startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Image"), Constant.GALLERY_IMAGE_REQUEST)
                }

                2 -> {
                    if(Utils.checkPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "video/*"
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                        intent.action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(Intent.createChooser(intent, "Select Video"), Constant.GALLERY_VIDEO_REQUEST)
                    } else {
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.GALLERY_VIDEO_REQUEST)
                    }
                }
            }
        }
        sheet.setDrawables(arrayListOf(R.drawable.ic_camera, R.drawable.ic_gallery, R.drawable.ic_video))
        binding.attach.setOnClickListener {
            sheet.show()
        }
        binding.cancelReply.setOnClickListener {
            binding.rootReply.visibility = View.GONE;
            dmAdapter.initReply(false)
        }

        val messageSwipeController = MessageSwipeController(requireContext()) {
            if(dmAdapter.getItem(it)?.type == "ignore") return@MessageSwipeController
            binding.rootReply.visibility = View.VISIBLE
            binding.replyinto.text = if(dmAdapter.getItem(it)?.senderSid == MyApplication.SID) "Replying to Yourself" else "Replying to ".plus(otherprofile?.cust_data?.username) //binding.replyMsg.text = dmAdapter.getItem(it)?.body
            when(dmAdapter.getItem(it)?.type) {
                text  -> {
                    binding.replyImage.visibility = View.GONE
                    binding.replyMsg.text = dmAdapter.getItem(it)?.getMessage(messageThreadSecrets)
                }

                image -> {
                    binding.replyImage.visibility = View.VISIBLE
                    binding.replyMsg.text = "\uD83D\uDCF7 Photo"
                    Utils.stickImage(requireContext(), binding.replyImage, dmAdapter.getItem(it)
                            ?.getMessage(messageThreadSecrets), null)

                }

                video -> {
                    binding.replyImage.visibility = View.VISIBLE
                    binding.replyMsg.text = "\uD83D\uDCF9 Video"
                    Utils.stickImage(requireContext(), binding.replyImage, dmAdapter.getItem(it)?.thumbnail, null)

                }

                audio -> {
                    binding.replyImage.visibility = View.GONE
                    binding.replyMsg.text = "\uD83D\uDD0A Audio"
                }
            }
            dmAdapter.initReply(true, it)
            binding.message.requestFocus()
            val inputMethodManager = getSystemService(requireContext(), InputMethodManager::class.java)
            inputMethodManager?.showSoftInput(binding.message, InputMethodManager.SHOW_IMPLICIT)
        }
        itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper?.attachToRecyclerView(binding.recycler)
        binding.ivCreateMeetUp.onClick({
            MyApplication.smallVibrate()
            otherprofile?.let {
                var baseFragment: BaseFragment = MeetUpViewPageFragment.getInstance(it?.social?.sid, false)
                Navigation.addFragment(requireActivity(), baseFragment, MeetUpViewPageFragment.TAG, R.id.homeFram, true, true)
            }
        })

    }


    private fun isPermissionEnabled(): Boolean {
        if(Utils.checkPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if(Utils.checkPermission(requireContext(), Manifest.permission.RECORD_AUDIO)) {
                return true
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), Constant.AUDIO_REQUEST)
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.AUDIO_REQUEST)
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when(requestCode) {
                Constant.CAMERA_REQUEST -> {
                    if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if(Utils.checkPermission(requireContext(), Manifest.permission.CAMERA)) {
                            val values = ContentValues()
                            values.put(MediaStore.Images.Media.TITLE, "New Picture")
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
                            image_uri = requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
                            startActivityForResult(cameraIntent, Constant.CAMERA_REQUEST)
                        } else {
                            showToast("Permission Required For Camera")
                        }
                    }else{
                        if (Build.VERSION.SDK_INT > 23 && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            Log.i(
                                TAG,
                                " checkingratinale:: ${shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)}"
                            )
                            rationale()
                        }
                    }
                }

                Constant.GALLERY_VIDEO_REQUEST -> {
                    if(Utils.checkPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "video/*"
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                        intent.action = Intent.ACTION_GET_CONTENT
                        startActivityForResult(Intent.createChooser(intent, "Select Video"), Constant.GALLERY_VIDEO_REQUEST)
                    } else {
                        showToast("Permission Required For Storage")
                    }
                }
            }
        /*} else {
            showToast("Permission Required")
        }*/
    }


    private fun populatView() {
        otherprofile?.let {
            Log.d(TAG, "populatView: observeDeletedMsgTimestamp1")
            messageThreadSecrets = it.social.message_thread_secrets
            dmAdapter.messageThreadSecrets = messageThreadSecrets
            it.social.message_thread_id?.let { //fireViewModel.getMessages(it)
                Log.d(TAG, "populatView: observeDeletedMsgTimestamp 2::")
                fireViewModel.readMessages(it)
                Utils.cancelNotification(requireActivity(), it)
                fireViewModel.setDMProfilePic(PreferencesManager.get<ProfileGetResponse>(PREFRENCE_PROFILE)?.cust_data?.profile_image_url, it)
                fireViewModel.getMessageOfPage(it)
                chatListener = FireBaseUtils.getChatListener(it, deletedMsgTimestamp) {
                    Log.i(TAG, "populatView: getChatListener called ${(dmAdapter.getList().size > 0)}")
                    fireViewModel.readMessages(it)
                    Utils.cancelNotification(requireActivity(), it)
                    if(dmAdapter.getList().size > 0) fireViewModel.getChatChanges(it, ((dmAdapter.getList()
                            .firstOrNull { it is DMModel.MyDM && it.timestamp != null }) as DMModel.MyDM?)?.timestamp, deletedMsgTimestamp)
                    Log.i(TAG, "getChatListener:: populatViewOnen:::   2")
                }
                parentChatListener = FireBaseUtils.getParentChatListener(it) {
                    Log.d(TAG, "populatView: getParentChatListener")
                    it?.let {
                        Log.d(TAG, "populatView:getListenBlocked 6 ")
                        if(it.isNotEmpty()) {
                            it.forEach { blocked: String ->
                                if(SID?.let { it1 -> blocked.indexOf(it1) } == 0) {
                                    isBlocked(true)
                                } else {
                                    Log.d(TAG, "populatView:getListenBlocked 7 ")
                                    isBlocked(false)
                                }
                            }
                        } else {
                            isBlocked(null)
                        }
                    } ?: run {
                        isBlocked(null)
                    }
                }

            } ?: run {

               showToast("Something went wrong")
                return
            }
            dmAdapter.otherProfile(it)
            val blocked = it.social.blocked
            val blocked_by_you = it.social.blocked_by_you

            Log.d(TAG, "populatView:blocked_by_you $blocked_by_you $blocked")
            if(blocked) {
                isBlocked(true)
            } else if(blocked_by_you) {
                isBlocked(false)

            } else {
                isBlocked(null)
            }
            it.social.badge?.let { badge: String ->
                val badgeItem = Utils.getBadge(badge)
                val badgeBackground = Utils.customizeBadgeBackground(requireActivity(), badgeItem, 0f)
                binding.ivDpBadge.setImageResource(badgeItem.foreground)
                binding.divider.background = badgeBackground
            }

            Utils.stickImage(requireActivity(), binding.userDp, it.cust_data.profile_image_url, null)
            binding.userDp.onClick({
                openProfile(it.cust_data.sid, Constant.Source.ONENONECHAT.sorce.plus(SID))
                /*var baseFragment: BaseFragment = OtherProfileFragment.getInstance(it.cust_data.sid)
                Navigation.addFragment(
                    requireActivity(), baseFragment,
                    Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
                )*/

                /*val bundle=Bundle()
                bundle.putString(OtherProfileFragment.OTHER_USER_ID, it.cust_data.sid)
                navigate(OtherProfileFragment(),bundle)*/
            })
            binding.name.onClick({
                openProfile(it.cust_data.sid, Constant.Source.ONENONECHAT.sorce.plus(SID))

                /*var baseFragment: BaseFragment = OtherProfileFragment.getInstance(it.cust_data.sid)
                Navigation.addFragment(
                    requireActivity(), baseFragment,
                    Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
                )*/

                /*val bundle=Bundle()
                bundle.putString(OtherProfileFragment.OTHER_USER_ID, it.cust_data.sid)
                navigate(OtherProfileFragment(),bundle)*/
            })
            binding.name.text = it.cust_data.username

            //binding.status.text = "Online"
            statusListener = FireBaseUtils.getStatusRefrence(it.cust_data.firebase_id) {
                Log.i(TAG, " checkingStatus:: $it")
                binding.status.text = it
            }

            Utils.onClick(binding.send, 500) {
                Log.i(TAG, " msg::: ${binding.message.text}")

                if(binding.message.text.toString().trim().isNotEmpty()) {
                    var ducument = fireViewModel.sendMessage(it.social.message_thread_id!!, binding.message.text.toString()
                            .trim(), if(dmAdapter.isItReply) dmAdapter.parentMsg else null, text, passPhrase = messageThreadSecrets) {
                        Log.i(TAG, "Messaged Sent")
                    }
                    binding.rootReply.visibility = View.GONE
                    val myDM = if(dmAdapter.isItReply) dmAdapter.parentMsg
                    else null
                    dmAdapter.addNewDummyMessage(DMModel.MyDM(DM(id = ducument.id, body = binding.message.text.toString()
                            .trim(), type = text, senderSid = SID, parentMsg = myDM)))
                    Log.d(TAG, "populatView: myDM:: $myDM")
                    binding.message.setText("")
                    dmAdapter.initReply(false)
                }
            }

            binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    //if (!isLoading() && !isLastPage()) {
                    if(!loading) {
                        if(visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                            loading = true
                            Log.d(TAG, "fireViewModel.getMessagesBy 2::")
                            if(mostPreviousTimeStamp != null) {
                                fireViewModel.getMessagesBy(mostPreviousTimeStamp, deletedMsgTimestamp, it.social.message_thread_id ?: "")
                            }
                        }
                    }
                }
            })
        }
    }

    private fun isBlocked(bool: Boolean?) {
        bool?.let {
            if(!it) {
                binding.flReply.visibility = View.INVISIBLE
                binding.recordView.visibility = View.INVISIBLE
                itemTouchHelper?.attachToRecyclerView(null)
                binding.tvBlocked.visibility = View.VISIBLE
                binding.tvBlocked.text = "You blocked this user"
//                binding.tvBlocked.onClick({
//
//                })
            } else {
                binding.flReply.visibility = View.INVISIBLE
                binding.recordView.visibility = View.INVISIBLE
                itemTouchHelper?.attachToRecyclerView(null)
                binding.tvBlocked.visibility = View.VISIBLE
                binding.tvBlocked.text = "You are blocked by this user"
            }
        } ?: run {
            binding.flReply.visibility = View.VISIBLE
            binding.recordView.visibility = View.VISIBLE
            itemTouchHelper?.attachToRecyclerView(binding.recycler)
            binding.tvBlocked.visibility = View.GONE

        }
    }


    private val infoListener = MediaRecorder.OnInfoListener { mr, what, extra ->
        Log.d(TAG, "infoListener: mr, what, extra  $mr, $what, $extra  ")
    }
    private val errorListener = MediaRecorder.OnErrorListener { mr, what, extra ->
        Log.d(TAG, "errorListener: mr, what, extra  $mr, $what, $extra  ")
    }


    fun sendVideoImage(uri: Uri, thumb: Bitmap?, myDM: DMModel.MyDM?, retry: Boolean = true) {
        otherprofile?.let {
            fireViewModel.uploadDMVideo(requireContext(), uri, thumb, it.social.message_thread_id!!, if(dmAdapter.isItReply) dmAdapter.parentMsg else null, passPhrase = messageThreadSecrets)
            if(retry) {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(uri.path)
                val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                dmAdapter.addNewDummyMessage(DMModel.MyDM(DM(id = "", type = video, senderSid = SID, parentMsg = myDM, bitmap = thumb, uri = uri, duration = duration, bool = false)))
            }
            dmAdapter.initReply(false)
        }
    }

    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L
    private fun actionDown() {
        startTime = System.currentTimeMillis()
        setUpRecording()
        try {
            recorder?.prepare()
            recorder?.start()
            //Toast.makeText(requireContext(), "Recording started", Toast.LENGTH_SHORT).show()
        } catch(e: RuntimeException) {
            e.printStackTrace()
        } catch(e: IllegalStateException) {
            e.printStackTrace()
        } catch(e: IOException) {
            e.printStackTrace()
        }

    }


    private fun actionUp() {
        elapsedTime = System.currentTimeMillis() - startTime
        if(isLessThanOneSecond(elapsedTime)) {
            val file = File(audioPath)
            if(file.exists()) file.delete()
            Toast.makeText(requireContext(), "Too short", Toast.LENGTH_SHORT).show()
        } else {
            stopRecording()
        }
    }

    private fun isLessThanOneSecond(time: Long): Boolean {
        return time <= 1000
    }

    private fun setUpRecording() {
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder?.setOutputFile(getFilename())
        recorder?.setAudioEncodingBitRate(96000)
        recorder?.setAudioSamplingRate(44100)
        recorder?.setOnErrorListener(errorListener)
        recorder?.setOnInfoListener(infoListener)

    }

    private fun stopRecording() {
        if(null != recorder) {
            try {
                recorder?.stop()
                recorder?.release()
                recorder = null
                sentAudio()
                //Toast.makeText(requireContext(), "Recording stoped", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "stopRecording: audio path $audioPath")
            } catch(e: Exception) {
            }
        }
    }

    private fun sentAudio() {
        audioPath?.let { path: String ->
            val uri = Uri.parse(path)
            otherprofile?.let {
                val myDM = if(dmAdapter.isItReply) dmAdapter.parentMsg else null

                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(uri.path)
                val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                mmr.release()
                dmAdapter.addNewDummyMessage(DMModel.MyDM(DM(id = "", type = audio, senderSid = SID, parentMsg = myDM, duration = duration, bool = true)))
                fireViewModel.uploadDMVideo(requireContext(), uri, null, it.social.message_thread_id!!, if(dmAdapter.isItReply) dmAdapter.parentMsg else null, passPhrase = messageThreadSecrets)
            }
        }
    }

    private fun getFilename(): String {
        val filepath = requireActivity().getExternalFilesDir("Music")?.path
        val file = File(filepath, "sent")
        if(!file.exists()) {
            file.mkdirs()
        }
        //.getExternalCacheDir() + "/audio.aac"
        audioPath = file.absolutePath + "/" + System.currentTimeMillis() + ".aac"
        return audioPath!!
    }


    override fun onRecordStart() {
        actionDown()
    }

    override fun isReady() = isMessage

    override fun onRecordEnd() {
        actionUp()
    }

    override fun onRecordCancel() {
        if(null != recorder) {
            try {
                recorder?.stop()
                recorder?.release()
                recorder = null
                val file = File(audioPath)
                if(file.exists()) file.delete()
                //Toast.makeText(requireContext(), "Recording stoped", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "stopRecording: audio path $audioPath")
            } catch(e: Exception) {
            }
        }
    }

    private fun addObserver() {

        Log.d(TAG, "Checking the flow : 1:: addObserver")
        fireViewModel.observeGetPageMessage().observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    if(it.value.isNotEmpty()) {
                        Log.d(TAG, "Checking the flow : 1:: mostPreviousTimeStamp $mostPreviousTimeStamp")
                        mostPreviousTimeStamp = it.value.last()?.timestamp
                        dmAdapter.addItems(it.value.map { DMModel.MyDM(it!!) })
                    }
                }

                is ResultHandler.Failure -> {
                    Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
            loading = false
        })

        fireViewModel.observeDeletedMsgTimestamp().observe(viewLifecycleOwner, {
            when(it) {
                is ResultHandler.Success -> {
                    Log.d(TAG, "Checking the flow : 1:: observeDeletedMsgTimestamp ${it.value?.second}")
                    deletedMsgTimestamp = it.value?.second
                    Log.d(TAG, "fireViewModel.getMessagesBy 1::")
                    fireViewModel.getMessagesBy(mostPreviousTimeStamp, deletedMsgTimestamp, it.value?.first ?: "")
//                    otherprofile?.social?.message_thread_id?.let{ _id: String ->
//                        chatListener = FireBaseUtils.getChatListener(_id,deletedMsgTimestamp) {
//                            fireViewModel.readMessages(_id)
//                            Log.i(TAG, " populatViewOnen:::   1 deletedMsgTimestamp ${deletedMsgTimestamp?.toDate()}")
//                            if(dmAdapter.getList().size > 0) fireViewModel.getChatChanges(_id, mostPreviousTimeStamp, deletedMsgTimestamp)
//                            Log.i(TAG, " populatViewOnen:::   2")
//                        }
//                    }
                }

                is ResultHandler.Failure -> {
                    // Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
            //deletedMsgTimestamp
        })

        fireViewModel.observeChatChangedMessage().observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    if(it.value.isNotEmpty()) {
                        Log.d(TAG, "Checking the flow : 1:: observeChatChangedMessage ${it.value.size}")
                        dmAdapter.addReplaceMessage(ArrayList(it.value))
                    }
                }

                is ResultHandler.Failure -> {
                    Log.e(TAG, "${it.throwable?.printStackTrace()}")
                }
            }
        })

        userAccountViewModel.observerFullOtherProfileChange().observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResultHandler.Success -> {
                    Log.d(TAG, "Checking the flow : 1:: observerFullOtherProfileChange")
                    otherprofile = it.value
                    populatView()
                }

                is ResultHandler.Failure -> {
                    MyApplication.showToast(requireActivity(), "Something went wrong")
                }
            }

        })
        fireViewModel.observeDmImageUpload().observe(viewLifecycleOwner, Observer { map ->
            otherprofile?.let {
                when(map.values.first()) {
                    is String -> {
                        var ducument = fireViewModel.sendMessage(it.social.message_thread_id!!, map.values.first() as String, if(dmAdapter.isItReply) dmAdapter.parentMsg else null, image, passPhrase = messageThreadSecrets) {
                            Log.i(TAG, "Messaged Sent")
                        }
                        dmAdapter.initReply(false)
                        binding.rootReply.visibility = View.GONE


//                        binding.message.setText("")
                    }
                }
            }
        })
        fireViewModel.observeDmVideoUpload()
                .observe(viewLifecycleOwner, Observer { it: ResultHandler<Map<Int, Any>> ->
                    when(it) {
                        is ResultHandler.Success -> {
                            dmAdapter.initReply(false)
                        }

                        is ResultHandler.Failure -> {
                        }
                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK) { // Get the Image from data
            val myDM = if(dmAdapter.isItReply) dmAdapter.parentMsg else null
            when(requestCode) {
                Constant.GALLERY_IMAGE_REQUEST   -> {
                    if(data?.getData() != null) {
                        val mImageUri: Uri? = data.getData()
                        var imageBitmap = Utils.uriToBitmap(mImageUri, requireContext())

                        Log.i(TAG, " sizeOFiamges:: -- ${imageBitmap}")
                        fireViewModel.uploaDMImages(ArrayList<Bitmap?>().apply { add(imageBitmap) })

                        dmAdapter.addNewDummyMessage(DMModel.MyDM(DM(id = "", type = image, senderSid = SID, parentMsg = myDM, bitmap = imageBitmap)))
                        dmAdapter.initReply(false)
                    } else {
                        if(data?.getClipData() != null) {
                            val mClipData: ClipData? = data.getClipData()
                            var imageBitmap = ArrayList<Bitmap?>()
                            for(i in 0 until mClipData!!.itemCount) {
                                val item = mClipData!!.getItemAt(i)
                                val uri = item.uri
                                val uriToBitmap = Utils.uriToBitmap(uri, requireContext())
                                imageBitmap.add(uriToBitmap!!)
                                dmAdapter.addNewDummyMessage(DMModel.MyDM(DM(id = "", type = image, senderSid = SID, parentMsg = myDM, bitmap = uriToBitmap)))
                            }
                            fireViewModel.uploaDMImages(imageBitmap)

                            dmAdapter.initReply(false)
                            Log.i(TAG, " sizeOFiamges:: ${imageBitmap.size}")
                        }
                    }
                }

                Constant.CAMERA_REQUEST          -> {
                    Utils.uriToBitmap(image_uri, requireContext())?.let {

                        dmAdapter.addNewDummyMessage(DMModel.MyDM(DM(id = "", type = image, senderSid = SID, parentMsg = myDM, bitmap = it)))
                        fireViewModel.uploaDMImages(ArrayList<Bitmap?>().apply { add(it) })

                        dmAdapter.initReply(false)
                    }
                }

                Constant.GALLERY_VIDEO_REQUEST   -> {
                    if(data?.data != null) {
                        Log.d(TAG, "onActivityResult: TrimVideo  ${data.data}")
                        Log.d(TAG, "onActivityResult: TrimVideo  ${data.data?.path}")
                        val widthHeight: IntArray = TrimmerUtils.getVideoWidthHeight(requireActivity(), data.data)
                        TrimVideo.activity(data.data.toString())
//                                .setFileName("")
                                .setCompressOption(CompressOption(30, 10, widthHeight[0], widthHeight[1]))
                                .setHideSeekBar(false).setTrimType(TrimType.MIN_MAX_DURATION)
                                .setMinToMax(1, 300)  //seconds
                                .start(this)
                    }
                }

                TrimVideo.VIDEO_TRIMMER_REQ_CODE -> {
                    val uri = Uri.parse(TrimVideo.getTrimmedVideoPath(data!!))
                    Log.d(TAG, "onActivityResult: Video ${uri.toString()}")
                    val interval: Long = 1000000
                    val options = RequestOptions().frame(interval)

                    Glide.with(requireContext()).asBitmap().load(uri.toString()).apply(options)
                            .addListener(object : RequestListener<Bitmap> {
                                override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                                    Log.e(TAG, "onLoadFailed:asBitmap ")
                                    val thumb = ThumbnailUtils.createVideoThumbnail(uri.toString(), MediaStore.Video.Thumbnails.MINI_KIND)
                                    sendVideoImage(uri, thumb, myDM)
                                    return true
                                }

                                override fun onResourceReady(resource: Bitmap?, model: Any?, target: com.bumptech.glide.request.target.Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                    Log.d(TAG, "onResourceReady: asBitmap")
                                    sendVideoImage(uri, resource, myDM)
                                    return true
                                }
                            }).into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    Log.d(TAG, "onResourceReady: CustomTarget")
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    Log.d(TAG, "onLoadCleared: CustomTarget")
                                }

                            })


//                    if(data?.data == null) {
//                        val videoTrimerFragment: BaseFragment = VideoTrimerFragment(uri)
//                        Navigation.addFragment(requireActivity(), videoTrimerFragment, videoTrimerFragment.javaClass.simpleName, R.id.homeFram, true, false)
//                    }
                }
            }
        }
    }

    override fun onPause() {
        stopRecording()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mp.stop()
        mp.reset()
        mp.release()
        recorder = null
        dmAdapter.getRunnables()?.let { dmAdapter.getHandler()?.removeCallbacks(it) }

        _binding = null
        statusListener?.remove()
        chatListener?.remove()
        parentChatListener?.remove()
    }

    override fun hideNavBar(): Boolean {
        return true
    }

    override fun onBackPageCome() {
        Log.i(TAG, " clasCameOnTop:: ${this::class.simpleName}")
//        otherprofile?.let {
//            userAccountViewModel.getOtherProfile(it?.cust_data.sid)
//        }
    }
}
