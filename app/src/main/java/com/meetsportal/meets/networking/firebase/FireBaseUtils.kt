package com.meetsportal.meets.networking.firebase

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.*
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.models.MeetFireData
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.networking.registration.OtpResponse
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils

object FireBaseUtils {

    private val TAG = FireBaseUtils::class.java.simpleName

    val database = Firebase.database

    private var lastOnlineRef: DatabaseReference? = null
    private var profileImageRef: DatabaseReference? = null
    private var userNameRefrence: DatabaseReference? = null

    // val statusRef = database.getReference("/users/".plus(FirebaseAuth.getInstance().currentUser.uid).plus("/status"))
    private var statusRef: DatabaseReference? = null
    private var sidRefrence: DatabaseReference? = null
    private var con: DatabaseReference? = null


    fun init() {
        FirebaseAuth.getInstance().currentUser?.let {
            lastOnlineRef = database.getReference("/users/".plus(FirebaseAuth.getInstance().currentUser.uid)
                    .plus("/lastOnline"))
            statusRef = database.getReference("/users/".plus(FirebaseAuth.getInstance().currentUser.uid)
                    .plus("/status"))
            profileImageRef = database.getReference("/users/".plus(FirebaseAuth.getInstance().currentUser.uid)
                    .plus("/profileImage"))
            sidRefrence = database.getReference("/users/".plus(FirebaseAuth.getInstance().currentUser.uid)
                    .plus("/sid"))
            userNameRefrence = database.getReference("/users/".plus(FirebaseAuth.getInstance().currentUser.uid)
                    .plus("/userName"))
        }
    }

    fun setOnline() {
        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue() ?: false
                if(connected as Boolean) {
                    //con = myConnectionsRef.push()

                    database.getReference("syncref").keepSynced(true)

                    lastOnlineRef?.onDisconnect()?.setValue(ServerValue.TIMESTAMP)
                    statusRef?.onDisconnect()?.setValue("offline")

                    statusRef?.setValue("online")
                    sidRefrence?.setValue(PreferencesManager.get<OtpResponse>(Constant.PREFRANCE_OTPRESPONSE)?.user?.sid)
                    userNameRefrence?.setValue(PreferencesManager.get<OtpResponse>(Constant.PREFRANCE_OTPRESPONSE)?.user?.username)
                    profileImageRef?.setValue(PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.cust_data?.profile_image_url)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "  Listener:::  ${error}")
            }
        })
    }

    fun setOffline() {
        statusRef?.setValue("offline")
        // When I disconnect, update the last time I was seen online
        lastOnlineRef?.setValue(ServerValue.TIMESTAMP)
    }

    fun getStatusRefrence(fid: String, status: (String?) -> Unit): ListenerRegistration {
        return FirebaseFirestore.getInstance().collection("status").document(fid)
                .addSnapshotListener { value, error ->
                    //Log.i(TAG," changeListener::: ${value} -- $error")
                    if(error == null && value != null && value.exists()) {
                        if(value?.getString("status").equals("offline")) {
                            Log.i(TAG, " getStatusRefrence::: offline ${value?.getLong("lastOnline")}")
                            status(Utils.getlastSeenFromFirebaseTimeStamp(value?.getLong("lastOnline")))
                        } else if(value?.getString("status").equals("online")) {
                            Log.i(TAG, " getStatusRefrence::: online ")
                            status("Online")
                        } else {
                            Log.i(TAG, " getStatusRefrence::: noData ")
                            status("")
                        }
                    } else {
                        Log.e(TAG, " getStatusRefrence::: error")
                        status("")
                    }
                }
    }

    fun getChatListener(messageThread : String,deletedMsgTimestamp:Timestamp? ,messageCome: ()->Unit):ListenerRegistration{
        return FirebaseFirestore.getInstance().collection(Constant.DIRECTMESSAGE_NODE)
            .document(messageThread)
            .collection(Constant.MESSAGE_NODE)
                .whereGreaterThan("timestamp",deletedMsgTimestamp?:Timestamp(0,0))
                .addSnapshotListener { value, error ->
                //Log.i(TAG," chatChangeListener::: ${value} -- $error")
                if(error == null && value != null){

                    if(value.documentChanges.size != value.documents.size && !value.metadata.hasPendingWrites()){
                        Log.i("TAG","getChatListener pendingWrite::: ${value.metadata.hasPendingWrites()}")
                        messageCome()
                        //messageCome.invoke(value.documentChanges.get(0).document)
                    }
                }
            }
    }

    fun getDMParentChatListener(messageCome: () -> Unit): ListenerRegistration {
        return FirebaseFirestore.getInstance().collection(Constant.DIRECTMESSAGE_NODE)
                .orderBy("lastMessageAddedAt", Query.Direction.DESCENDING)
                .whereArrayContains("sids", MyApplication.SID.toString())
                .addSnapshotListener { value, error ->
                    //Log.i(TAG," chatChangeListener::: ${value} -- $error")
                    if(error == null && value != null) {

                        Log.i(TAG, " pendingWrite::: ${value.metadata.hasPendingWrites()}")
                        //Log.i(TAG, " pendingWrite::: ${value.documents}")
                        messageCome()
                    }
                }
    }

    fun getParentChatListener(messageThread: String, messageCome: (List<String>?) -> Unit): ListenerRegistration {
        return FirebaseFirestore.getInstance().collection(Constant.DIRECTMESSAGE_NODE)
                .document(messageThread).addSnapshotListener { value, error ->
                    if(error == null && value != null&& value.exists()&& !value.metadata.hasPendingWrites()) {
                        val get = value?.get("blocked")
                        messageCome(get as List<String>?)
                    }
                }
    }


    fun getOnlineStatusListener(fIdList: List<String?>, listen: () -> Unit): ListenerRegistration {

        return FirebaseFirestore.getInstance().collection(Constant.STATUS_NODE)
                .whereIn(FieldPath.documentId(), fIdList).addSnapshotListener { value, error ->
                    if(error == null && value != null) {

                        Log.i(TAG, " pendingWrite::: ${value.metadata.hasPendingWrites()}")
                        listen()
                    }
                }
    }

    fun getMeetChatListener(messageThread: String, messageCome: () -> Unit): ListenerRegistration {
        return FirebaseFirestore.getInstance().collection(Constant.MEETUP_CHATS)
                .document(messageThread).collection(Constant.MESSAGE_NODE)
                .addSnapshotListener { value, error ->
                    //Log.i(TAG," chatChangeListener::: ${value} -- $error")
                    if(error == null && value != null) {
                        if(value.documentChanges.size != value.documents.size && !value.metadata.hasPendingWrites()) {
                            messageCome()
                            //messageCome.invoke(value.documentChanges.get(0).document)
                        }
                    }
                }
    }

    fun getMeetVoteListener(messageThread: String, messageCome: (MeetFireData?) -> Unit): ListenerRegistration {
        Log.i(TAG, " meetThread::: $messageThread")
        return FirebaseFirestore.getInstance()
            .collection(Constant.MEETUP_CHATS)
            .document(messageThread)
            .addSnapshotListener { value, error ->
                if(error == null && value != null) {
                    if(!value.metadata.hasPendingWrites()) {
                        Log.i(TAG," pendingWrite:::-- ${value.data}  ${value.data?.get("votes") is Map<*,*>}")
                        var a: MeetFireData? = null

                        FirebaseFirestore.getInstance()
                            .collection(Constant.MEETUP_CHATS)
                            .document(messageThread)
                            .get()
                            .addOnSuccessListener {
                                try {
                                    Log.i(TAG, "getMeetVoteListener: ${value.data}")
                                    a = value.toObject(MeetFireData::class.java)
                                    Log.i(TAG, " checkingModel::: ${a}")
                                    messageCome(a)
                                } catch(e: Exception) {
                                    e.printStackTrace()
                                    Log.e(TAG, " Firebase MeetUp Map Deserialize fail ${e.message}")
                                }
                            }
                    }
                }
            }
    }

    fun getNotificationListener(notify :()->Unit): ListenerRegistration {
        return FirebaseFirestore.getInstance().collection(Constant.NOTIFICATION_NODE)
            .document(FirebaseAuth.getInstance().currentUser.uid)
            .collection(Constant.USER_NOTIFICATION_NODE)
            .addSnapshotListener { value, error ->
                //Log.i(TAG," chatChangeListener::: ${value} -- $error")
                if(error == null && value != null) {
                    if(value.documentChanges.size != value.documents.size && !value.metadata.hasPendingWrites()) {
                        notify()
                        //messageCome.invoke(value.documentChanges.get(0).document)
                    }
                }
            }
    }

}