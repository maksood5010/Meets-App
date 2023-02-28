package com.meetsportal.meets.viewmodels

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonObject
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.models.UploadPost
import com.meetsportal.meets.models.UploadPostResource
import com.meetsportal.meets.networking.ErrorResponse
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.exception.ApiException
import com.meetsportal.meets.networking.post.*
import com.meetsportal.meets.networking.post.datasource.PostLikePagingDataSource
import com.meetsportal.meets.overridelayout.mention.MentionPerson
import com.meetsportal.meets.repository.AppRepository
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.lang.RuntimeException
import javax.inject.Inject

@HiltViewModel
class PostViewModel  @Inject constructor(var repository: AppRepository) : ViewModel(){

    private val TAG = PostViewModel::class.java.simpleName

    private val disposable = CompositeDisposable()
    var onUploadPost: MediatorLiveData<UploadPostResource> = MediatorLiveData<UploadPostResource>()
    var onSinglePost: MediatorLiveData<ResultHandler<SinglePostResponse?>> = MediatorLiveData()
    var onSinglePostComment: MediatorLiveData<PagingData<Comment>> = MediatorLiveData()
    var onPublishComment : MediatorLiveData<Int> = MediatorLiveData<Int>()
    var onPublishReplyComment : MediatorLiveData<Int> = MediatorLiveData<Int>()
    var onCommentDelete : MediatorLiveData<Boolean> = MediatorLiveData()
    var onPostDelete : MediatorLiveData<Boolean> = MediatorLiveData()
    var likeDislike : MediatorLiveData<JsonObject?> = MediatorLiveData()
    var onPostUpdate : MediatorLiveData<ResultHandler<Boolean>> = MediatorLiveData()
    var onCommentStatus : MediatorLiveData<ResultHandler<Boolean?>> = MediatorLiveData()
    var onReport : MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    var onLikeDislikeComment : MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData()
    var imageUploadCount: MediatorLiveData<ResultHandler<String?>> = MediatorLiveData()
    var textPost: MediatorLiveData<ResultHandler<JsonObject?>> = MediatorLiveData<ResultHandler<JsonObject?>>()
    val likerPagingdataSource : MediatorLiveData<PagingData<PostLikerResponseItem>> = MediatorLiveData()
    val onRecentPost : MediatorLiveData<ResultHandler<FetchPostResponse?>> = MediatorLiveData()
    val onPostSearch :MediatorLiveData<SearchPostResponse> = MediatorLiveData()
    private val postsLivedata : MediatorLiveData<PagingData<FetchPostResponseItem>> = MediatorLiveData()
    private val commentsLiveDataSource : MediatorLiveData<PagingData<SinglePostCommentsItem>> = MediatorLiveData()


    val pickedImage = MutableLiveData<UploadPost>()


    private val errorResponse: MutableLiveData<ErrorResponse?> = MutableLiveData()

    fun populatePickImage(uploadPost: UploadPost) {
        pickedImage.value = uploadPost
    }

    private fun validatePost(post: UploadPost):Boolean{
        return !post.caption.isNullOrEmpty() && post.images?.count()!! > 0
    }

    /*fun uploadPost(post: Post, list: ArrayList<Bitmap>){
        repository.uploadPost(post,list)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                    onUploadPost.value = UploadPostResource.loading()
                }

                override fun onComplete() {
                    onUploadPost.value = UploadPostResource.success()
                }

                override fun onError(e: Throwable) {
                    onUploadPost.value = UploadPostResource.error(e.message!!)
                }
            })
    }*/


    fun uploadPostImages(caption:String, list: ArrayList<Bitmap>,mentionList : ArrayList<MentionPerson>? = null) {
        Log.i(TAG," Mentioned people::: $mentionList")

        if(Utils.isNetworkConnected(MyApplication.getContext())) {
            var count = 0
            var resultHashMap = HashMap<Int, String>()
            var reseultArray: ArrayList<String> = ArrayList()
            disposable.add(repository.uploadPostImages(caption, list).subscribe({ it ->
                        Log.d("TAG", "sequence:::   9")
                        count++
                        imageUploadCount.value = ResultHandler.Success(count.toString())
                        resultHashMap.put((it as HashMap<Int, String>).keys.first(), (it as HashMap<Int, String>).values.first())
                        if(resultHashMap.size == list.size) {
                            for(i in 0 until resultHashMap.size) {
                                reseultArray.add(resultHashMap.get(i)!!)
                            }
                            disposable.add(repository.uploadPost(caption, reseultArray, mentionList)
                                    .subscribe({
                                        if(it.isSuccessful) {
                                            imageUploadCount.value = ResultHandler.Success(it.body()?.get("_id")?.asString)
                                        }
                                    }, {
                                        Log.i("TAG", " posterror:: ${it}")
                                        Utils.handleException(it, imageUploadCount, "uploadPostImages")
                                    }))
                        }
                    }, {
                        Log.d("TAG", "sequence:::   10")
                        Log.i("TAG", " posterror:: ${it}")
                        MyApplication.initClodinary()
                        Utils.handleException(it, imageUploadCount, "uploadPostImages")

                    }))
        }else{
            Utils.handleException(ApiException(ErrorResponse(Constant.NO_INTERNET, Constant.CODE_NO_INTERNET, Constant.MESSAGE_NO_INTERNET)),imageUploadCount,"uploadPostImages" )
        }
    }
    fun uploadTextPost(
        text: String?,
        gradientPosition: Int,
        allMentionPerson: ArrayList<MentionPerson>? = null
    ) {
        disposable.add(repository.uploadTextPost(text,gradientPosition,allMentionPerson)
            .subscribe({
                if(it.isSuccessful){
                    textPost.value = ResultHandler.Success(it.body())
                }
            },{
                handleException(it,textPost,"uploadTextPost")
            })
        )
    }

    fun fetchPost(uid:String?,feedOption:Int, isItTimeLine:Boolean){
        disposable.add(
            repository.fetchPost(uid,feedOption,isItTimeLine)
                .subscribe({
                    postsLivedata.value = it
                },{
                    Log.i(TAG," Something went wrong feetching post ${it}")
                    if(it is ApiException){
                        errorResponse.value = it.errorResponse
                    }
                })

        )
    }



    fun toggleLike(postId:String?){
        disposable.add(
            repository.toggleLike(postId)
                .subscribe({
                    if(it.isSuccessful){
                        likeDislike.value = it.body()
                    }
                }, {
                    Log.e(TAG, " liked api giving exception")
                    if(it is ApiException){
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun toggleCommentLike(commentId:String?){
        disposable.add(
            repository.toggleCommentLike(commentId)
                .subscribe({
                    onLikeDislikeComment.value = ResultHandler.Success(it.body())
                }, {
                    Log.e(TAG, " liked api giving exception")
                    if(it is ApiException){
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }


    fun fetchSinglePost(postId:String?,mode :String?){
        disposable.add(repository.fetchSinglePost(postId,mode)
            .subscribe({
                if(it.isSuccessful){
                    onSinglePost.value = ResultHandler.Success(it.body())
                    //onSinglePost.postValue(ResultHandler.Success(it.body()))
                   /* disposable.add(repository.fetchComments(it.body())
                        .subscribe({
                            onSinglePostComment.value = it
                        },{
                            Log.d(TAG, "fetchSinglePost: error")
                        })
                    )*/
                }
            },{
                Log.e(TAG," fetch single post api giving exception ${it}")
                if(it is ApiException){
                    if(it.errorResponse != null && it.errorResponse?.errorCode == 404){
                        onSinglePost.value = ResultHandler.Failure(it.errorResponse?.code,"This post no longer exists",it)
                    }else{
                        Utils.handleException(it,onSinglePost,"fetchSinglePost")
                    }
                }else{
                    Utils.handleException(it,onSinglePost,"fetchSinglePost")
                }
            })
        )

    }

    fun fetchCommnets(postId:String?,mode :String?,entity_type : String?,referenceId : String?=null,parentCommentPublisher : PublishSubject<SinglePostCommentsItem?>? = null){
        disposable.add(repository.fetchComments(postId,mode,entity_type,referenceId,parentCommentPublisher)
            .subscribe({
                commentsLiveDataSource.value = it
            },{
                Log.e(TAG," fetch single post api giving exception ${it}")
                /*if(it is ApiException){
                    if(it.errorResponse != null && it.errorResponse?.errorCode == 404){
                        onSinglePost.value = ResultHandler.Failure(it.errorResponse?.code,"This post no longer exists",it)
                    }else{
                        Utils.handleException(it,onSinglePost,"fetchSinglePost")
                    }
                }else{
                    Utils.handleException(it,onSinglePost,"fetchSinglePost")
                }*/
            })
        )
    }

    fun fetchRecentPost(){
        disposable.add(repository.fetchRecentPost()
            .subscribe({
                if(it.isSuccessful){
                    onRecentPost.value = ResultHandler.Success(it.body())
                }
            },{
                Log.e(TAG," fetch single post api giving exception ${it}")
                Utils.handleException(it,onRecentPost,"fetchRecentPost")
            })
        )

    }

    fun publishComment(postId:String?,commentText : String?,position:Int){
        var body : JsonObject = JsonObject()
        body.addProperty("body",commentText)
        disposable.add(repository.publishComment(postId,body).
                subscribe({
                    if(it.isSuccessful){
                        onPublishComment.value = position
                        Log.i(TAG," commentPublished:: ${it.body()}")
                    }
                },{
                    Log.i(TAG," publish comment api giving exception  ${it}")
                    if(it is ApiException){
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun replyComment(commentId:String?,commentText : String?,position: Int){
        var body : JsonObject = JsonObject()
        body.addProperty("body",commentText)
        disposable.add(repository.replyComment(commentId,body).
        subscribe({
            if(it.isSuccessful){
                onPublishReplyComment.value = position
                Log.i(TAG," commentPublished:: ${it.body()}")
            }
        },{
            Log.i(TAG," publish comment api giving exception  ${it}")
            if(it is ApiException){
                errorResponse.value = it.errorResponse
            }
        })
        )
    }

    fun deleteComment(commentId: String?){
        disposable.add(repository.deleteComment(commentId).
        subscribe({
            if(it.isSuccessful){
                onCommentDelete.value = true
                Log.i(TAG," commentPublished:: ${it.body()}")
            }
        },{
            Log.i(TAG," publish comment api giving exception  ${it}")
            if(it is ApiException){
                errorResponse.value = it.errorResponse
            }
        })
        )
    }

    fun deleteCommentReply(commentId: String?){
        disposable.add(repository.deleteCommentReply(commentId).
        subscribe({
            if(it.isSuccessful){
                onCommentDelete.value = true
                Log.i(TAG," commentPublished:: ${it.body()}")
            }
        },{
            Log.i(TAG," publish comment api giving exception  ${it}")
            if(it is ApiException){
                errorResponse.value = it.errorResponse
            }
        })
        )
    }

    fun deletePost(postId:String?){
        disposable.add(repository.deletePost(postId).
        subscribe({
            if(it.isSuccessful){
                onPostDelete.value = true
                Log.i(TAG," commentPublished:: ${it.body()}")
            }
        },{
            Log.i(TAG," delete post api giving exception  ${it}")
            if(it is ApiException){
                errorResponse.value = it.errorResponse
            }
        })
        )
    }

    fun updatePost(postId: String?, caption: String, allMentionPerson: ArrayList<MentionPerson>?= null){
        var body = JsonObject()
        var request = PostUpdateRequest(body = caption,mentions = allMentionPerson)
        /*body.addProperty("body",caption)
        val menstionJasonArray = JsonArray()
        body.addProperty("mentions",allMentionPerson)*/
        disposable.add(repository.updatePost(postId,request).
        subscribe({
            if(it.isSuccessful){
                onPostUpdate.value = ResultHandler.Success(true)
                Log.i(TAG," commentPublished:: ${it.body()}")
            }
        },{
            Log.i(TAG," delete post api giving exception  ${it}")
            Utils.handleException(it,onPostUpdate,"updatePost")
            if(it is ApiException){
                errorResponse.value = it.errorResponse
            }
        })
        )
    }
    fun disableComment(postId:String? , enable:Boolean?){
        /*val body = JsonObject()
        body.addProperty("comments_enabled",enable)*/
        val request = PostUpdateRequest(comments_enabled = enable)
        disposable.add(repository.updatePost(postId,request).
        subscribe({
            if(it.isSuccessful){
                onCommentStatus.value = ResultHandler.Success(enable)
                Log.i(TAG," commentPublished:: ${it.body()}")
            }
        },{
            Utils.handleException(it,onCommentStatus,"disableComment")
        })
        )
    }

    fun reportContent(body:JsonObject){
        disposable.add(repository.reportContent(body).
        subscribe({
            if(it.isSuccessful){
                onReport.value = ResultHandler.Success(it.body())
                Log.i(TAG," commentPublished:: ${it.body()}")
            }
        },{
            Log.i(TAG," delete post api giving exception  ${it}")
            if(it is ApiException){
                Log.i(TAG," checking::: 0")
                if(it.errorResponse != null && it.errorResponse?.errorCode == 400){
                    Log.i(TAG," checking::: 1")
                    onReport.value = ResultHandler.Failure(it.errorResponse?.code,it.errorResponse?.message,it)
                }else{
                    Log.i(TAG," checking::: 2")
                    errorResponse.value = it.errorResponse
                }
            }
        })
        )
    }




    fun searchPost(searchString:String?){
        disposable.add(
            repository.searchPost(searchString)
                .subscribe({
                    if(it.isSuccessful)
                        onPostSearch.value = it.body()
                },{
                    if(it is ApiException){
                        errorResponse.value = it.errorResponse
                    }
                })
        )
    }

    fun getLikePageDataSource(postId: String?){
        disposable.add(Pager(
            config = PagingConfig(
                pageSize = 2,
                enablePlaceholders = false,
                prefetchDistance = 2,
            ),
            pagingSourceFactory = {
                PostLikePagingDataSource(repository).apply {
                    this.postId = postId
                }
            }
        ).flowable
            .subscribe({
                likerPagingdataSource.value = it
            }, {
                Log.i(TAG, " Something went wrong feetching post ${it}")
            })
        )
    }

    fun ingestPost(post: FetchPostResponseItem) {

        val body = JsonObject()
        body.addProperty("post_id",post._id)
        body.addProperty("user_id",MyApplication.SID)
        body.addProperty("post_creator_id",post.user_id)
        disposable.add(repository.ingestPost(body).
        subscribe({
            if(it.isSuccessful){
                Log.i(TAG," Post Alalytics Succesfull")
            }
        },{
            Log.e(TAG," Post Alalytics Failed")
        }))
    }

    fun observePostCommentsDataSource(): LiveData<PagingData<SinglePostCommentsItem>> {
        return commentsLiveDataSource
    }

    fun observePostLikeDislike():LiveData<JsonObject?>{
        return likeDislike
    }

    fun observerReportContent():LiveData<ResultHandler<JsonObject?>>{
        return onReport
    }

    fun observerLikeDislike():LiveData<ResultHandler<JsonObject?>>{
        return onLikeDislikeComment
    }



    fun onPostUpdate(): LiveData<ResultHandler<Boolean>> {
        return onPostUpdate
    }
    fun onCommentStatusUpdate(): LiveData<ResultHandler<Boolean?>> {
        return onCommentStatus
    }

    fun observePostDelete():LiveData<Boolean>{
        return onPostDelete
    }

    fun observeOnCommentDelete():LiveData<Boolean>{
        return onCommentDelete
    }

    fun observePostLikeDataSource():LiveData<PagingData<PostLikerResponseItem>>{
        return likerPagingdataSource
    }

    fun observeUploadPost(): LiveData<UploadPostResource?>? {
        return onUploadPost
    }

    fun observeUploadedImageCount():LiveData<ResultHandler<String?>>{
        return imageUploadCount
    }

    fun observeTextPost(): LiveData<ResultHandler<JsonObject?>> {
        return textPost
    }

    fun observePostChange():LiveData<PagingData<FetchPostResponseItem>>{
        return postsLivedata
    }

    fun observeSinglePost(): LiveData<ResultHandler<SinglePostResponse?>>{
        return onSinglePost
    }

    fun observePublishComment():LiveData<Int>{
        return  onPublishComment
    }

    fun observePublishReplyComment():LiveData<Int>{
        return  onPublishReplyComment
    }


    fun observeSinglepostComment(): LiveData<PagingData<Comment>> {
        return onSinglePostComment
    }

    fun observeException(): LiveData<ErrorResponse?> {
        return errorResponse
    }

    fun observePostSearch():LiveData<SearchPostResponse>{
        return onPostSearch
    }
    fun observePostRecent(): LiveData<ResultHandler<FetchPostResponse?>> {
        return onRecentPost
    }

    fun getUnknownError(it: Throwable): ResultHandler.Failure {
        return ResultHandler.Failure(
            "500",
            "Something went Wrong",
            it
        )
    }

    inline fun <reified T> handleException(it:Throwable, livedata: MutableLiveData<ResultHandler<T>>, apiName:String){

        if (it is ApiException) {
            if (it.errorResponse != null && it.errorResponse?.errorCode == 400) {
                FirebaseCrashlytics.getInstance().log("$apiName 400 ${it.errorResponse?.errorCode}")
                FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                livedata.value = ResultHandler.Failure(
                    it.errorResponse?.code,
                    it.errorResponse?.message,
                    it
                )
            } else {
                FirebaseCrashlytics.getInstance().log("$apiName ${it.errorResponse?.errorCode}")
                FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                livedata.value = getUnknownError(it)
                Log.e("TAG", "Something went Wrong ${it.printStackTrace()}")
            }
        } else {
            FirebaseCrashlytics.getInstance().log("$apiName ${it.localizedMessage}")
            FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
            livedata.value = getUnknownError(it)
        }

    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}