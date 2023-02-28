package com.meetsportal.meets.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.*
import androidx.paging.rxjava2.flowable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.meetsportal.meets.application.MyApplication.Companion.SID
import com.meetsportal.meets.database.entity.Pinned
import com.meetsportal.meets.models.*
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.directmessage.DM
import com.meetsportal.meets.networking.directmessage.DMModel
import com.meetsportal.meets.networking.directmessage.datasource.DirectMessagePagingDataSource
import com.meetsportal.meets.networking.exception.ApiException
import com.meetsportal.meets.networking.meetup.ChatDM
import com.meetsportal.meets.networking.notifications.NotificationModal
import com.meetsportal.meets.networking.notifications.datasource.NotificationPagingDataSource
import com.meetsportal.meets.repository.AppRepository
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.toCalendar
import com.meetsportal.meets.utils.toDate
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class FireBaseViewModal @Inject constructor(var repository: AppRepository) : ViewModel() {

    private val TAG = FireBaseViewModal::class.java.simpleName

    private val disposable = CompositeDisposable()
    private val onRegister: MediatorLiveData<StateResource<String?>> = MediatorLiveData()
    private val onLogin: MediatorLiveData<StateResource<String?>> = MediatorLiveData()

    private val notificationPagingdata: MediatorLiveData<PagingData<NotificationModal>> =
        MediatorLiveData()
    private val directMessagePagingData: MediatorLiveData<PagingData<DMModel>> = MediatorLiveData()

    var onPagedMessage: MediatorLiveData<ResultHandler<List<DM?>>> = MediatorLiveData()
    var lastDeletedMessage: MediatorLiveData<ResultHandler<Pair<String,Timestamp>?>> = MediatorLiveData()
    var onMeetPagedMessage: MediatorLiveData<ResultHandler<List<ChatDM?>>> = MediatorLiveData()
    var onDMChatChanged: MediatorLiveData<ResultHandler<List<DM?>>> = MediatorLiveData()
    var onMeetChatChanged: MediatorLiveData<ResultHandler<List<ChatDM?>>> = MediatorLiveData()
    var onDMParentChange: MediatorLiveData<ResultHandler<List<DMParent?>>> = MediatorLiveData()
    var onFbOnlineChange: MediatorLiveData<ResultHandler<List<FBOnlineStatus?>>> =
        MediatorLiveData()
    var onPinnedChange: MediatorLiveData<List<Pinned?>> = MediatorLiveData()
    var onDmUploadProgress: MediatorLiveData<Map<Int, Any>> = MediatorLiveData()
    var onDmVideoProgress: MediatorLiveData<ResultHandler<Map<Int, Any>>> = MediatorLiveData()
    var onDmThumbProgress: MediatorLiveData<Map<Int, Any>> = MediatorLiveData()


    fun register(email: String?, password: String?, profile: UserProfile) {
        repository.register(email, password, profile).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                    //onRegister.value = StateResource.loading()
                }

                override fun onComplete() {
                    onRegister.value = StateResource.success(null)
                }

                override fun onError(e: Throwable) {
                    onRegister.value = StateResource.error(MyError())
                }
            })
    }


    fun login(firebaseToken: String?) {
        Log.i(TAG, " Femail&pass:: $firebaseToken ")
        repository.login(firebaseToken)?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                    //onLogin.value = StateResource.loading()
                }

                override fun onComplete() {
                    onLogin.value = StateResource.success(null)
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, " onLoginError::: ${e.printStackTrace()}")
                    onLogin.value = StateResource.error(MyError())
                }
            })
    }

    fun getNotification(defaulterNotification: ArrayList<String?>) {
        disposable.add(Pager(config = PagingConfig(
            pageSize = 200,
            enablePlaceholders = false,
            prefetchDistance = 2,
        ), pagingSourceFactory = {
            NotificationPagingDataSource(repository,defaulterNotification)
        }).flowable.subscribe({
            notificationPagingdata.value = it.map { it ->
                NotificationModal.MyNotification(it)
            }.insertSeparators { before, after ->
                var today = Calendar.getInstance()
                var weekAfter = Calendar.getInstance()
                weekAfter.add(Calendar.DAY_OF_YEAR, -7)

                var beforeDate = before?.timestamp?.toDate()?.toCalendar()
                var afterDate = after?.timestamp?.toDate()?.toCalendar()

                if (before == null && after?.timestamp?.toDate()?.date == today.get(Calendar.DATE)) {
                    NotificationModal.SeparatorItem("Today") as NotificationModal
                } else if ((before?.timestamp?.toDate()?.date == today.get(Calendar.DATE) || before == null) && after?.timestamp?.toDate()?.date != today.get(
                        Calendar.DATE
                    )
                ) {
                    NotificationModal.SeparatorItem("This Week") as NotificationModal
                } else if (beforeDate?.get(Calendar.DAY_OF_YEAR)
                        ?.compareTo(weekAfter.get(Calendar.DAY_OF_YEAR)) == 1 && afterDate?.get(
                        Calendar.DAY_OF_YEAR
                    )
                        ?.compareTo(weekAfter.get(Calendar.DAY_OF_YEAR)) != 1
                ) {
                    NotificationModal.SeparatorItem("Older") as NotificationModal
                } else {
                    null
                }
            }

        }, {
            Log.i(TAG, " Something went wrong fetching Notification ${it}")
        })
        )
    }


    fun getMessages(messageThread: String) {
        disposable.add(Pager(config = PagingConfig(
            pageSize = 15,
            enablePlaceholders = false,
            prefetchDistance = 1,
        ), pagingSourceFactory = {
            DirectMessagePagingDataSource(repository).apply {
                this.messageThread = messageThread
            }
        }).flowable.subscribe({
            directMessagePagingData.value = it.map {
                DMModel.MyDM(it)
            }
        }, {
            Log.i(TAG, " Something went wrong fetching Message ${it}")
        })
        )
    }


    fun getMessageOfPage(messageThread: String) {
        Log.i(TAG, " getMessageOfPage::: 2 -- messageThread: $messageThread")
        disposable.add(repository.getDeletedTime( messageThread)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                Log.i(TAG, " getMessageOfPage::: valueCame::: ")
                // onPagedMessage.value = ResultHandler.Success(it.filter { !it.type.equals("ignore") })
                    lastDeletedMessage.value = ResultHandler.Success(Pair(messageThread,it))
//                    onPagedMessage.value = ResultHandler.Success(it.second)
            }, {
                it.printStackTrace()
                Log.i(TAG, " getMessageOfPage::: ErrorCame::: ")
//                onPagedMessage.value = ResultHandler.Failure("cutomCode", "Somthing went wrong", it)
                    lastDeletedMessage.value = ResultHandler.Failure("cutomCode", "Something went wrong", it)
            })
        )
    }
    fun getMessagesBy(timestamp: Timestamp?,deletedMsgTimestamp: Timestamp?, messageThread: String){
        Log.i(TAG, " getMessagesBy::: 2 -- messageThread: $messageThread")
        disposable.add(repository.getMessageOfPage(timestamp,messageThread,deletedMsgTimestamp)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    Log.i(TAG, " getMessageOfPage::: valueCame::: ")
                    // onPagedMessage.value = ResultHandler.Success(it.filter { !it.type.equals("ignore") })
//                    lastDeletedMessage.value = ResultHandler.Success(it)
                    onPagedMessage.value = ResultHandler.Success(it)
                }, {
                    it.printStackTrace()
                    Log.i(TAG, " getMessageOfPage::: ErrorCame::: ")
                    onPagedMessage.value = ResultHandler.Failure("cutomCode", "Somthing went wrong", it)
                })
                      )
    }


    fun getGroupChat(timestamp: Timestamp?, messageThread: String) {
        Log.i(TAG, " getGroupChat::: 2 -- $timestamp")
        disposable.add(repository.getGroupChat(timestamp, messageThread)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                Log.i(TAG, " getGroupChat::: valueCame::: $it")
                // onPagedMessage.value = ResultHandler.Success(it.filter { !it.type.equals("ignore") })
                onMeetPagedMessage.value = ResultHandler.Success(it)
            }, {
                it.printStackTrace()
                Log.i(TAG, " getGroupChat::: ErrorCame::: ")
                onMeetPagedMessage.value =
                    ResultHandler.Failure("cutomCode", "Somthing went wrong", it)
            })
        )
    }

    fun setDMProfilePic(prifilePic: String?, messageThread: String) {
        repository.setDMProfilePic(prifilePic, messageThread)
    }

    fun getChatChanges(messageThread: String, myDM: Timestamp?, deletedMsgTimestamp: Timestamp?) {

        disposable.add(repository.getChatChanges(messageThread, myDM , deletedMsgTimestamp)
                .subscribe({
                    Log.i(TAG, " getChatChanges::: valueCame::: ")
                    onDMChatChanged.value = ResultHandler.Success(it)
                }, {
                    it.printStackTrace()
                    Log.i(TAG, " getChatChanges::: ErrorCame::: ")
                    onDMChatChanged.value = ResultHandler.Failure("cutomCode", "Somthing went wrong", it)
                }))
//        deletedMsgTimestamp?.let{
//        }?:run{
//            getMessageOfPage(myDM,messageThread)
//        }
    }

    fun getMeetChatChanges(messageThread: String, myDM: ChatDM?) {
        disposable.add(repository.getMeetChatChanges(messageThread, myDM)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                Log.i(TAG, " getChatChanges::: valueCame::: ")
                onMeetChatChanged.value = ResultHandler.Success(it)
            }, {
                it.printStackTrace()
                Log.i(TAG, " getChatChanges::: ErrorCame::: ")
                onMeetChatChanged.value =
                    ResultHandler.Failure("cutomCode", "Somthing went wrong", it)
            })
        )
    }


    fun sendMessage(
        messageThread: String,
        message: String,
        parentMsg: DMModel.MyDM? = null,
        type: String,
        thumb: String? = null,
        passPhrase: String? = null,
        messageSent: () -> Unit
    ): DocumentReference {
        return repository.sendMessage(messageThread, message, parentMsg, type, messageSent, thumb,passPhrase)
    }

    fun sendMeetMessage(
        messageThread: String,
        message: String?,
        parentMsg: ChatDM? = null,
        type: String,
        messageSent: () -> Unit
    ): DocumentReference {
        return repository.sendMeetMessage(messageThread, message, parentMsg, type, messageSent)
    }

    fun confirmAnimSeen(myVote: Vote, fbChatId: String) {
        disposable.add(repository.confirmAnimSeen(myVote, fbChatId).subscribe({
            Log.i(TAG, " AnimeSeenset:: False")
        }, {
            Log.i(TAG, " AnimeSeenset:: Error")
        }))
    }

    fun getAllChat(limit: Long? = null) {
        Log.i(TAG, " getAllChat::: ViewModel")
        disposable.add(repository.getAllChat(limit).subscribe({
            Log.i(TAG, " getChatChanges::: valueCame::: ${it.size}")

            getAllPin(it) {
                onDMParentChange.value = ResultHandler.Success(it)
            }
            //onDMParentChange.value = ResultHandler.Success(it.map { it.apply { pinned = true } })
        }, {
            it.printStackTrace()
            Log.i(TAG, " getChatChanges::: ErrorCame::: ")
            onDMParentChange.value = ResultHandler.Failure("cutomCode", "Somthing went wrong", it)
        }))
    }

    fun getAllUnreadChat(limit: Long? = null) {
        Log.i(TAG, " getAllChat::: ViewModel")
        disposable.add(repository.getAllUnreadChat(limit).subscribe({
            Log.i(TAG, " getChatChanges::: valueCame::: ${it.size}")
            onDMParentChange.value = ResultHandler.Success(it)
        }, {
            it.printStackTrace()
            Log.i(TAG, " getChatChanges::: ErrorCame::: ")
            onDMParentChange.value = ResultHandler.Failure("cutomCode", "Somthing went wrong", it)
        }))
    }


    fun getOnlineStatus(fidList: List<String?>) {
        disposable.add(repository.getOnlineStatus(fidList).subscribe({
            Log.i(TAG, " getChatChanges::: valueCame::: ${it.size}")
            onFbOnlineChange.value = ResultHandler.Success(it)
        }, {
            it.printStackTrace()
            Log.i(TAG, " getChatChanges::: ErrorCame::: ")
            onFbOnlineChange.value = ResultHandler.Failure("cutomCode", "Somthing went wrong", it)
        }))
        //return repository.getOnlineStatus(fidList)
    }

    fun setPinned(sid: String?, listener: (Boolean) -> Unit) {
        disposable.add(repository.setPinned(sid).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                listener(true)
            }, {
                Log.e(TAG, " value insertion Error::: ")
                it.printStackTrace()
            })
        )
    }

    fun deleteChat(messageThread: String, listener: (Boolean) -> Unit) {
        Log.e(TAG, " value insertion Error::: messageThread $messageThread")
        disposable.add(repository.deleteChat(messageThread).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                listener(true)
            }, {
                listener(false)
                it.printStackTrace()
            })
        )
    }


    fun setUnPinned(sid: String?, listener: (Boolean) -> Unit) {
        disposable.add(repository.setUnPinned(sid).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                listener(true)
            }, {
                Log.e(TAG, " value insertion Error::: ")
                it.printStackTrace()
            })
        )
    }


    fun getAllPin(listDM: List<DMParent?>, listener: (List<DMParent?>) -> Unit) {
        disposable.add(repository.getAllPin().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({ pins ->
                Log.i(TAG, " getAllPinFailed::: success  ${pins}")
                //listener(listDM.filter {pins.map { it.sid }.contains( it?.sids?.find { it != SID })})
                listener(listDM.map {
                    it?.apply {
                        pinned = pins.map { it.sid }.contains(sids?.find { it != SID })
                    }
                })
            }, {
                Log.e(TAG, " getAllPinFailed::: failed$it")
                it.printStackTrace()
            })
        )
    }

    fun uploaDMImages(list: ArrayList<Bitmap?>) {
        var count = 0
        var resultHashMap = HashMap<Int, Any>()
        var reseultArray: ArrayList<Any> = ArrayList()
        disposable.add(repository.uploaDMImages(list).subscribe({ it ->
            onDmUploadProgress.value = it
            resultHashMap.put(
                (it as HashMap<Int, Any>).keys.first(),
                (it as HashMap<Int, Any>).values.first()
            )
            if (resultHashMap.size == list.size) {
                for (i in 0 until resultHashMap.size) {
                    reseultArray.add(resultHashMap.get(i)!!)
                }
                /*repository.uploadPost(caption,reseultArray)
                    .subscribe({
                        if(it.isSuccessful){
                            imageUploadCount.value = 100
                        }
                    },{
                        Log.i("TAG"," posterror:: ${it}")
                    })*/
            }
        }, {
            Log.e("TAG", " posterror:: ${it.printStackTrace()}")

            if (it is ApiException) {
                //errorResponse.value = it.errorResponse
            }
        }))
    }

    fun uploadDMVideo(
        context: Context,
        uri: Uri,
        thumb: Bitmap?,
        messageId: String,
        myDM: DMModel.MyDM?,
        passPhrase: String? = null,
    ) {
        Log.d(TAG, "uploadDMVideo: ")
        disposable.add(repository.uploadDMVideo(context, uri, thumb, messageId, myDM,passPhrase)
            .subscribe({ it1: Map<Int, Any> ->
                onDmVideoProgress.value = ResultHandler.Success(it1)
            }, {
                Log.e("TAG", " posterror:: ${it.printStackTrace()}")

                Utils.handleException(it, onDmVideoProgress, "uploadDMVideo")
//                    if(it is ApiException) {
//                        onDmVideoProgress.value=ResultHandler.Failure(it.errorResponse?.code, it.errorResponse?.message, it)
//                        //errorResponse.value = it.errorResponse
//                    }
            })
        )
    }

    fun votePlace(id: String?, fbChatId: String, maxRemainVote: Int?, ) {
        disposable.add(repository.votePlace(id, fbChatId,maxRemainVote).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                Log.i(TAG, " getChatChanges::: valueCame::: ")
                //onMeetChatChanged.value = ResultHandler.Success(it)
            }, {
                it.printStackTrace()
                Log.i(TAG, " getChatChanges::: ErrorCame::: ")
                //onMeetChatChanged.value = ResultHandler.Failure("Custom Code", "Something went wrong", it)
            })
        )
    }

    fun readMessages(messageThread: String) {
        repository.readMessages(messageThread)
    }

    fun observeDmImageUpload(): LiveData<Map<Int, Any>> {
        return onDmUploadProgress
    }

    fun observeDmVideoUpload(): LiveData<ResultHandler<Map<Int, Any>>> {
        return onDmVideoProgress
    }

    fun observeDmThumbUpload(): LiveData<Map<Int, Any>> {
        return onDmThumbProgress
    }

    fun observeOnPinnedChange(): LiveData<List<Pinned?>> {
        return onPinnedChange
    }


    fun observeOnlineStatus(): LiveData<ResultHandler<List<FBOnlineStatus?>>> {
        return onFbOnlineChange
    }

    fun observeDMParentChange(): LiveData<ResultHandler<List<DMParent?>>> {
        return onDMParentChange
    }

    fun observeChatChangedMessage(): LiveData<ResultHandler<List<DM?>>> {
        return onDMChatChanged
    }

    fun observerMeetChatChangesMessage(): LiveData<ResultHandler<List<ChatDM?>>> {
        return onMeetChatChanged
    }

    fun observeGetPageMessage(): LiveData<ResultHandler<List<DM?>>> {
        return onPagedMessage
    }

    fun observeDeletedMsgTimestamp(): LiveData<ResultHandler<Pair<String, Timestamp>?>> {
        return lastDeletedMessage
    }


    fun observeMeetChat(): LiveData<ResultHandler<List<ChatDM?>>> {
        return onMeetPagedMessage
    }


    fun observeNotificationPagingDataSource(): LiveData<PagingData<NotificationModal>> {
        return notificationPagingdata
    }

    fun observeDirectMessagePagingDataSource(): LiveData<PagingData<DMModel>> {
        return directMessagePagingData
    }

    fun observeRegister(): LiveData<StateResource<String?>> {
        return onRegister
    }

    fun observeLogin(): LiveData<StateResource<String?>> {
        return onLogin
    }


    override fun onCleared() {
        super.onCleared()
        Log.e(TAG, "onCleared:disposable.clear ")
        disposable.clear()
    }


}