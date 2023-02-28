package com.meetsportal.meets.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.meetsportal.meets.models.Comment
import com.meetsportal.meets.models.Post
import com.meetsportal.meets.models.Story
import com.meetsportal.meets.models.UserProfile
import com.meetsportal.meets.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class NewsFeedViewmodel @Inject constructor (var repository: AppRepository) : ViewModel() {

    val TAG = NewsFeedViewmodel::class.java.simpleName




    private val disposable = CompositeDisposable()
    private val onUser: MediatorLiveData<UserProfile> = MediatorLiveData<UserProfile>()
    private val newsFeed: MediatorLiveData<ArrayList<Post>?> = MediatorLiveData<ArrayList<Post>?>()
    val pickedImage = MutableLiveData<ArrayList<Bitmap>>()


    init {
        //uid = repository.getCurrentUid()
        /*loadUserInfo(repository.getCurrentUid())
        loadNewsFeed()*/
    }

    fun populatePickImage(images: ArrayList<Bitmap>) {
        pickedImage.value = images
    }

    fun applyFilter(bitmap:Bitmap,position:Int){
        pickedImage.value?.set(position,bitmap)
    }

    /*fun getAllNewsFeedCard() : MutableLiveData<ArrayList<Post>>? {
        return repository.getAllNewsFeedCard()
    }*/

    fun loadNewsFeed(){
        repository.getNewsFeed()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())

            /*?.subscribe(object : Subscriber<QuerySnapshot?>{
                override fun onSubscribe(s: Subscription?) {

                }

                override fun onNext(querySnapshot: QuerySnapshot?) {
                    var postArray:ArrayList<Post>? = ArrayList<Post>()
                    for(doc in querySnapshot!!){
                        Log.i(TAG," document:: $doc ")

                        postArray?.add(doc.toObject(Post::class.java)!!)
                    }
                    newsFeed.value = postArray
                }

                override fun onError(t: Throwable?) {
                    Log.d(TAG, "onError: $t")
                }

                override fun onComplete() {

                }

            })*/
            ?.toObservable()
            ?.subscribe(object : Observer<QuerySnapshot?> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }
                override fun onNext(querySnapshot:  QuerySnapshot) {
                    var postArray:ArrayList<Post>? = ArrayList<Post>()
                    for(doc in querySnapshot){
                        //Log.i(TAG," document:: $doc ")

                        postArray?.add(doc.toObject(Post::class.java)!!)
                    }
                    newsFeed.value = postArray
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onError: $e")
                }

                override fun onComplete() {
                    ////TODO("Not yet implemented")
                }
            })
    }

    fun getFeedInfo(): LiveData<ArrayList<Post>?> {
        return newsFeed
    }




    fun getAllNewsFeedSotoryCard() : MutableLiveData<ArrayList<Story>>? {
        return repository.getAllNewsFeedSotoryCard()
    }

    /*fun getComments(postid :String) : MutableLiveData<ArrayList<Comment>>?{
        return repository.getComments(postid)
    }*/

    fun getUser() : FirebaseUser?{
        return repository.getCurrentUser()
    }

    fun loadUserInfo(currentUid: String?) {
        repository.getUserinfo(currentUid)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.toObservable()
            ?.subscribe(object : Observer<DocumentSnapshot?> {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                }

                override fun onNext(documentSnapshot: DocumentSnapshot) {
                    onUser.value = documentSnapshot.toObject(UserProfile::class.java)
                }

                override fun onError(e: Throwable) {
                    Log.d(TAG, "onError: $e")
                }

                override fun onComplete() {
                    //TODO("Not yet implemented")
                }
            })
    }

    fun getUserInfo(): LiveData<UserProfile?> {
        return onUser
    }

    override fun onCleared() {
        Log.i(TAG,"onCleared")
        super.onCleared()
        disposable.clear()
    }
}