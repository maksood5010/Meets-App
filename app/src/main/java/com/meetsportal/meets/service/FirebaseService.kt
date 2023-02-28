package com.meetsportal.meets.service

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cossacklabs.themis.SecureCell
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.StorageReference
import com.meetsportal.meets.BuildConfig
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.models.Comment
import com.meetsportal.meets.models.Post
import com.meetsportal.meets.models.Story
import com.meetsportal.meets.models.UserProfile
import com.meetsportal.meets.networking.ErrorResponse
import com.meetsportal.meets.networking.directmessage.DMModel
import com.meetsportal.meets.networking.exception.ApiException
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.NOTIFICATION_NODE
import com.meetsportal.meets.utils.Constant.POST_IMAGE_FOLDER
import com.meetsportal.meets.utils.Constant.POST_NODE
import com.meetsportal.meets.utils.Constant.RESTAURANTS_NODE
import com.meetsportal.meets.utils.Constant.USER_NOTIFICATION_NODE
import com.meetsportal.meets.utils.DataConverter
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class FirebaseService @Inject constructor(var application: Application) {

    @Inject
    lateinit var firebaseAuth: FirebaseAuthSource

    @Inject
    lateinit var fireStore: FirebaseFirestore

    @Inject
    lateinit var storageReference: StorageReference

    val TAG = FirebaseService::class.java.simpleName


    /*@Inject
    lateinit var application: Application*/

    private var post: ArrayList<Post> = ArrayList()
    private var storyPost: ArrayList<Story> = ArrayList()
    var images: ArrayList<Int> = ArrayList()
    var dp: ArrayList<Int> = ArrayList()
    var name: Array<String>? = null
    var lastSeen: Array<String>? = null
    var likes: Array<String>? = null
    var comments: Array<String>? = null
    var shares: Array<String>? = null
    var caption: Array<String>? = null

    init {
        images.add(R.drawable.demo_post_image)
        images.add(R.drawable.demo_post_image_2)
        images.add(R.drawable.demo_post_image)

        dp.add(R.drawable.demo_post_image)
        dp.add(R.drawable.demo_post_image_2)
        dp.add(R.drawable.demo_post_image)

        name = arrayOf("Julia", "Elon", "Jennifer")
        lastSeen = arrayOf("1hr ago", "2hr ago", "3hr ago")
        likes = arrayOf("2.3k", "5k", "10k")
        comments = arrayOf("200", "159", "230")
        shares = arrayOf("2.5k", "800", "250")
        caption = arrayOf(application.resources.getString(R.string.lorum_ipsum), application.resources.getString(R.string.lorum_ipsum), application.resources.getString(R.string.lorum_ipsum))


    }

    fun getBestMeetUpInitMutableLivedata(): Flowable<QuerySnapshot?>? {


        return Flowable.create({ emitter ->

            fireStore.collection(RESTAURANTS_NODE).limit(10)
                    .addSnapshotListener { documentSnapshot, e ->
                        e?.let {
                            emitter.onError(e)
                        }
                        documentSnapshot?.let {
                            emitter.onNext(documentSnapshot)
                        }
                    }
        }, BackpressureStrategy.BUFFER)

    }

    fun getCategoryListMutableLivedata(): Flowable<QuerySnapshot?>? {


        return Flowable.create({ emitter ->

            fireStore.collection(RESTAURANTS_NODE).limit(10)
                    .addSnapshotListener { documentSnapshot, e ->
                        e?.let {
                            emitter.onError(e)
                        }
                        documentSnapshot?.let {
                            emitter.onNext(documentSnapshot)
                        }
                    }
        }, BackpressureStrategy.BUFFER)

    }

    fun getNewsFeed(): Flowable<QuerySnapshot?>? {
        Log.i(TAG, " getNewsFeed:: ")
        return Flowable.create({ emitter ->

            fireStore.collection(POST_NODE).addSnapshotListener { documentSnapshot, e ->
                e?.let {
                    emitter.onError(e)
                }
                documentSnapshot?.let {
                    emitter.onNext(documentSnapshot)
                }
            }
        }, BackpressureStrategy.BUFFER)

        /*val livePost = MutableLiveData<ArrayList<Post>>()

        post.clear()
        for (i in 0..(NewsFeedPostListRepository.newsFeedPostLists.size -1 )) {

            post.add(NewsFeedPostListRepository.newsFeedPostLists[i])

        }

        //var postObservable  = Observable.fromIterable()

        livePost.value = arrayListOf();
        livePost.postValue(post)*/


        //return livePost;
    }


    fun getAllNewsFeedSotoryCard(): MutableLiveData<ArrayList<Story>> {

        val livePost = MutableLiveData<ArrayList<Story>>()

        var name: Array<String> = arrayOf("Julia", "Micheal", "Jhon", "Munna", "Elon", "Musk", "Ronaldo", "Mike")
        var followCounts: Array<String> = arrayOf("2M Followers", "12M Followers", "120M Followers", "50M Followers", "46M Followers", "33M follower", "89M Followers", "63M Followers")
        var image: Array<Int> = arrayOf(R.drawable.demo_post_image, R.drawable.demo_post_image_2, R.drawable.demo_post_image3, R.drawable.demo_post_image4, R.drawable.demo_post_image5, R.drawable.demo_post_image_6, R.drawable.demo_image_post7, R.drawable.demo_post_image8)


        storyPost.clear()
        for(i in 0..(followCounts.size - 1)) {

            storyPost.add(Story(image.get(i), name.get(i), followCounts.get(i)))
            Log.i("TAG", " chhecking:: NewsFeed data $storyPost ")
        }

        livePost.value = arrayListOf();
        livePost.postValue(storyPost)


        return livePost;
    }

    fun getComments(postId: String): MutableLiveData<ArrayList<Comment>> {

        var comments: ArrayList<Comment> = ArrayList()
        val livePost = MutableLiveData<ArrayList<Comment>>()

        comments.clear()
        for(i in 0..(CommentsRepositry.commentList.size - 1)) {

            comments.add(CommentsRepositry.commentList[i])

        }

        livePost.value = arrayListOf();
        livePost.postValue(comments)


        return livePost;

    }

    fun uploadPost(post: Post, list: ArrayList<Bitmap>): Completable {
        return Completable.create(CompletableOnSubscribe { emitter ->
            //create new user

            post.user = UserProfile.currentUserProile
            post.createdOn = Timestamp.now()
            post.mediaType = "IMAGES"

            val fireStoreImagelists = ArrayList<Map<String, String>>()


            for(i in 0 until list.size) {
                Log.i("uploadPost", " $i")
                val reference = storageReference.child(POST_IMAGE_FOLDER)
                        .child("${firebaseAuth.getCurrentUid()}")
                        .child("${System.currentTimeMillis()}.jpg")
                reference.putBytes(DataConverter.convertImage2ByteArray(list[i]))
                        .addOnFailureListener {
                            emitter.onError(it)
                        }.addOnSuccessListener {
                            reference.downloadUrl.addOnFailureListener {
                                emitter.onError(it)
                            }.addOnCompleteListener {
                                if(!it.isSuccessful) emitter.onError(it.exception!!)
                                else {
                                    fireStoreImagelists.add(mapOf("path" to it.result.toString()))
                                    if(fireStoreImagelists.size == list.size) {
                                        post.images = fireStoreImagelists
                                        fireStore.collection(POST_NODE).document().set(post)
                                                .addOnFailureListener { e -> emitter.onError(e) }
                                                .addOnSuccessListener { emitter.onComplete() }
                                    }
                                }
                            }
                        }

            }

        })
    }


    //get user information
    fun getUserInfo(uid: String?): Flowable<DocumentSnapshot?>? {
        return Flowable.create({ emitter ->
            val reference: DocumentReference? = uid?.let {
                fireStore.collection(Constant.USERS_NODE).document(it)
            }
            val registration = reference?.addSnapshotListener { documentSnapshot, e ->
                if(e != null) {
                    emitter.onError(e)
                }
                if(documentSnapshot != null) {
                    emitter.onNext(documentSnapshot)
                }
            }
            emitter.setCancellable { registration?.remove() }
        }, BackpressureStrategy.BUFFER)
    }

    //update status
    fun updateProfile(profile: UserProfile?): Completable? {
        return Completable.create { emitter ->
//            val reference: DocumentReference =
            fireStore.collection(Constant.USERS_NODE).document(firebaseAuth.getCurrentUid()!!)
                    .set(profile!!).addOnFailureListener { e -> emitter.onError(e) }
                    .addOnSuccessListener { emitter.onComplete() }
        }
    }

    fun getNotification() {
        Log.i(TAG, " firebaseAuth::::: $firebaseAuth")
        fireStore.collection(NOTIFICATION_NODE).document(firebaseAuth.getCurrentUid()!!)
                .collection(USER_NOTIFICATION_NODE).addSnapshotListener { value, error ->

                }

    }

    fun uploadPDmImages(list: ArrayList<Bitmap?>): Flowable<Map<Int, Any>> {


        var flowable: Flowable<Map<Int, Any>> = Flowable.create({}, BackpressureStrategy.BUFFER)

        for(i in 0 until list.size) {
            flowable = flowable?.mergeWith(makeCall(list.get(i), i).subscribeOn(Schedulers.io()))
        }
        return flowable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    }

    fun uploadDmVideo(context: Context, uri: Uri, thumb: Bitmap?, messageId: String, myDM: DMModel.MyDM?,passPhrase: String?): Flowable<Map<Int, Any>> {
        return uploadUri(uri, context, thumb, messageId, myDM,passPhrase).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    }

    fun makeCall(image: Bitmap?, index: Int): Flowable<Map<Int, Any>> {
        return Flowable.create(FlowableOnSubscribe<Map<Int, Any>> { emitter ->
            image?.let {
                MediaManager.get().upload(DataConverter.convertImage2ByteArray(image))
                        .unsigned(BuildConfig.SERVER_CODE).callback(object : UploadCallback {
                            override fun onStart(requestId: String?) {
                                Log.i("TAG", " onStart:::   ")
                            }

                            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                                var b: HashMap<Int, Any> = HashMap<Int, Any>()
                                var percent = (bytes * 100) / totalBytes
                                b.put(index, percent)
                                emitter.onNext(b)
                            }

                            override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                                Log.i("TAG", " gettingurl:: ${
                                    resultData?.get("secure_url").toString()
                                }")
                                var b: HashMap<Int, String> = HashMap<Int, String>()
                                b.put(index, resultData?.get("secure_url").toString())
                                emitter.onNext(b)
                            }

                            override fun onError(requestId: String?, error: ErrorInfo?) {
                                Log.i("TAG", " onError:::  ${error?.code}--${error?.description}")
                                emitter.onError(ApiException(ErrorResponse(error?.code, "Something went wrong please try again later")));
                            }

                            override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                                Log.i("TAG", " onReschedule:::   ")
                            }

                        }).dispatch()
            }
        }, BackpressureStrategy.BUFFER)
    }

    fun sendMessage(
        messageThred: String,
        message: String,
        parentMsg: DMModel.MyDM? = null,
        type: String = "text",
        messageSent: () -> Unit,
        thumbUrl: String? = null,
        duration: String? = null,
        passPhrase: String? ,
                   ): DocumentReference {
        val document = FirebaseFirestore.getInstance().collection(Constant.DIRECTMESSAGE_NODE)
                .document(messageThred).collection(Constant.MESSAGE_NODE).document()
        val key = if (messageThred.indexOf(FirebaseAuth.getInstance().uid!!) == 0) "unread2" else "unread1"
        val pass = passPhrase?.split(",")
        val cell = SecureCell.SealWithPassphrase(pass?.get(0))
        val plaintext = message.toByteArray()
        val context = pass?.get(1)?.toByteArray()

        val encrypted = cell.encrypt(plaintext, context)
        var messageMap = mutableMapOf("id" to document.id, "type" to type, "timestamp" to FieldValue.serverTimestamp(), "body_encrypted" to Base64.encodeToString(encrypted, Base64.NO_WRAP), "senderFid" to FirebaseAuth.getInstance().currentUser!!.uid, "senderSid" to MyApplication.SID)


        parentMsg?.let {
            messageMap.put("parentMsg", it)
        }
        thumbUrl?.let {
            messageMap.put("thumbnail", it)
        }
        duration?.let {
            messageMap.put("duration", it)
        }

        document.set(messageMap).addOnSuccessListener {
            Log.i("TAG", " message sent Successfully")
            messageSent.invoke()
        }
        message?.let {
            messageMap.put("body" , message)
        }
        FirebaseFirestore.getInstance().collection(Constant.DIRECTMESSAGE_NODE)
                .document(messageThred)
                .update(mapOf("lastMessage" to messageMap, "lastMessageAddedAt" to FieldValue.serverTimestamp(), key to FieldValue.increment(1)))
        return document
    }

    fun uploadUri(videoOrAudio: Uri?, context: Context?, thumb: Bitmap?, messageId: String, myDM: DMModel.MyDM?,passPhrase: String? = null,): Flowable<Map<Int, Any>> {
        return Flowable.create(FlowableOnSubscribe<Map<Int, Any>> { emitter ->
            videoOrAudio?.let {

                try {
                    Log.d(TAG, "uploadUri:video 2")
                    MediaManager.get().upload(it.toString()).option("resource_type", "auto")
                            .unsigned(BuildConfig.SERVER_CODE)
//                        .preprocess(VideoPreprocessChain().addStep(Transcode(parameters)))
                            .callback(object : UploadCallback {
                                override fun onStart(requestId: String?) {
                                    Log.i("TAG", " Video onStart:::   ")
                                }

                                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                                    val b: HashMap<Int, Any> = HashMap<Int, Any>()
                                    val percent = (bytes * 100) / totalBytes
                                    Log.d(TAG, "uploadDMVideo: ${percent}")
                                    b.put(0, percent)
                                    emitter.onNext(b)
                                }

                                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                                    val videoMap: HashMap<Int, String> = HashMap<Int, String>()
                                    videoMap.put(0, resultData?.get("secure_url").toString())
//                                emitter.onNext(b)
                                    val videoUrl = resultData?.get("secure_url").toString()
                                    thumb?.let { it: Bitmap ->
                                        Log.d(TAG, "uploadUri:thumb?.let ")
                                        MediaManager.get()
                                                .upload(DataConverter.convertImage2ByteArray(it))
                                                .unsigned(BuildConfig.SERVER_CODE)
                                                .callback(object : UploadCallback {
                                                    override fun onStart(requestId: String?) {
                                                        Log.i("TAG", " onStart2:::   ")
                                                    }

                                                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                                                        val b: HashMap<Int, Any> = HashMap<Int, Any>()
                                                        val percent = (bytes * 100) / totalBytes
                                                        b.put(0, percent)
                                                        Log.d(TAG, "onProgress: upload thumb : $percent")
                                                        emitter.onNext(b)
                                                    }

                                                    override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                                                        Log.d(TAG, "onSuccess: is string $videoUrl")
                                                        Log.i("TAG", " gettingurl 2:: ${resultData?.get("secure_url")} ")

                                                        val b: HashMap<Int, String> = HashMap<Int, String>()
                                                        b.put(0, resultData?.get("secure_url")
                                                                .toString())

                                                        val mmr = MediaMetadataRetriever()
                                                        mmr.setDataSource(videoOrAudio.path)
                                                        val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                                        sendMessage(messageId, message = videoUrl, parentMsg = myDM, type = "video", thumbUrl = resultData?.get("secure_url")
                                                                .toString(),duration=duration,passPhrase=passPhrase, messageSent = {
                                                            Log.i(TAG, "Messaged Sent")
                                                            emitter.onNext(b)
                                                        })
                                                    }

                                                    override fun onError(requestId: String?, error: ErrorInfo?) {
                                                        Log.i("TAG", " onError:::  ${error?.code}--${error?.description}")
                                                        emitter.onError(ApiException(ErrorResponse(error?.code, "Something went wrong please try again later")));
                                                    }

                                                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                                                        Log.i("TAG", " onReschedule:::   ")
                                                        emitter.onError(ApiException(ErrorResponse(error?.code, "Something went wrong please try again later")))
                                                        emitter.onComplete()

                                                    }

                                                }).dispatch()
                                    } ?: run {
                                        val b: HashMap<Int, String> = HashMap<Int, String>()
                                        b.put(0, resultData?.get("secure_url").toString())

                                        val mmr = MediaMetadataRetriever()
                                        mmr.setDataSource(videoOrAudio.path)
                                        val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                        mmr.release()
                                        Log.d(TAG, "onSuccess:audio duration ::$duration")
                                        sendMessage(messageId, message = videoUrl, parentMsg = myDM, type = "audio",
                                                thumbUrl = resultData?.get("secure_url").toString(),
                                                duration = duration,passPhrase = passPhrase,messageSent = {
                                            Log.i(TAG, "Messaged Sent")
                                            emitter.onNext(b)
                                        })
                                    }
                                }

                                override fun onError(requestId: String?, error: ErrorInfo?) {
                                    Log.i("TAG", "Video onError:::  ${error?.code}--${error?.description}")
                                    emitter.onError(ApiException(ErrorResponse(error?.code, "Something went wrong please try again later")))
                                }

                                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                                    Log.i("TAG", "Video onReschedule:::   ")
                                    emitter.onError(ApiException(ErrorResponse(error?.code, "Something went wrong please try again later")))
                                    emitter.onComplete()
                                }

                            }).dispatch(context)
                } catch(e: Exception) {
                    Log.e(TAG, "uploadUri:video internet error ")
                    emitter.onError(e)
                }

            }
            Log.d(TAG, "uploadUri:video $videoOrAudio")
        }, BackpressureStrategy.BUFFER)
    }

}