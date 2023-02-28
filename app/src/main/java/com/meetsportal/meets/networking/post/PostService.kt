package com.meetsportal.meets.networking.post

import android.graphics.Bitmap
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.gson.JsonObject
import com.meetsportal.meets.BuildConfig
import com.meetsportal.meets.networking.Api
import com.meetsportal.meets.networking.ApiClient
import com.meetsportal.meets.networking.ErrorResponse
import com.meetsportal.meets.overridelayout.mention.MentionPerson
import com.meetsportal.meets.ui.bottomsheet.ChooseTimeLineFeedSheet.Companion.GLOBAL_TIMELINE
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.GradientTypeArray
import com.meetsportal.meets.utils.DataConverter
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostService @Inject constructor(){

    private var api : Api = ApiClient.getRetrofit()

    fun uploadPostImages(caption:String, list: ArrayList<Bitmap>): Flowable<Map<Int, String>> {

        Log.d("TAG", "sequence:::   -1")

        var flowable: Flowable<Map<Int,String>> = Flowable.create({},BackpressureStrategy.BUFFER)

        for(i in 0 until list.size) {
            flowable = flowable?.mergeWith(makeCall(list.get(i),i).subscribeOn(Schedulers.io()))
        }
       return flowable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    }

    fun uploadPost(caption: String, urls: ArrayList<String>, mentionList: ArrayList<MentionPerson>?): Flowable<Response<JsonObject?>> {
       return api.addPost(PostRequest(caption,urls,mentions = mentionList))

        /*return Flowable.create({ emitter ->
            api.addPost(PostRequest(caption,urls))
                .subscribeOn(Schedulers.io())
                .subscribe({
                    emitter.onNext(it)
                })
        },BackpressureStrategy.BUFFER)*/
    }



    fun uploadTextPost(
        text: String?,
        gradientPosition: Int,
        allMentionPerson: ArrayList<MentionPerson>?
    ): Flowable<Response<JsonObject?>> {
        val postRequest = PostRequest(text,null,"text_post", BodyObj(GradientTypeArray.get(gradientPosition).label),mentions = allMentionPerson)
        return api.addPost(postRequest)
    }

    fun makeCall(image : Bitmap,index:Int):Flowable<Map<Int,String>> {
        return Flowable.create( FlowableOnSubscribe<Map<Int,String>>{ emitter ->
            Log.d("TAG", "sequence:::   0")
            MediaManager.get().upload(DataConverter.convertImage2ByteArray(image))
                .unsigned(BuildConfig.SERVER_CODE)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        Log.d("TAG", "sequence:::   1")
                        Log.i("TAG", " onStart:::   ")
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        Log.d("TAG", "sequence:::   2")
                    }

                    override fun onSuccess(
                        requestId: String?,
                        resultData: MutableMap<Any?, Any?>?
                    ) {
                        Log.d("TAG", "sequence:::   3")
                        Log.i("TAG"," gettingurl:: ${resultData?.get("secure_url").toString()}")
                        var b: HashMap<Int,String>  = HashMap<Int,String> ()
                        b.put(index,resultData?.get("secure_url").toString())
                        emitter.onNext(b)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        Log.i("TAG", " onError:::  ${error?.code}--${error?.description}")
                        Log.d("TAG", "sequence:::   4")
                        emitter.onError(
                            com.meetsportal.meets.networking.exception.ApiException(
                                ErrorResponse(
                                    error?.code,
                                    "Something went wrong please try again later"
                                )
                            )
                        );
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        Log.d("TAG", "sequence:::   5")
                        Log.i("TAG", " onReschedule:::   ")
                    }

                }).dispatch()
        },BackpressureStrategy.BUFFER)
    }

    fun fetchPostFromAws(feedOption : Int,userId:String?,page:String?,limit:String?): Single<FetchPostResponse> {
        if(feedOption == GLOBAL_TIMELINE) {
            userId?.let {
                return api.fetchPost("-createdAt", userId, page, limit)
            }?:run{
                return api.fetchGlobal(page)
            }

        }else {
            return api.fetchTimeLine( userId, page, limit)
        }
    }
    fun fetchRecentPost(limit: String): Observable<Response<FetchPostResponse>> {
        return api.fetchRecentPost( limit = limit)
    }

    fun fetchSinglePost(postId:String?,mode :String?):Flowable<Response<SinglePostResponse>>{
        return  api.fetchSinglePost(postId,mode)
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun getComments(
        postId: String?,
        mode: String?,
        entityType: String?,
        page: Int?,
        limit: Int?,
        referenceId: String?
    ):Single<SinglePostComments>{
        if(entityType.equals("post")){
            return  api.fetchCommnets(postId,mode,entityType,page,limit,referenceId)
        }else{
            return  api.fetchReplies(postId,mode,entityType,page,limit,referenceId)
        }
    }

    fun toggleLike(appId:String?): Observable<Response<JsonObject?>> {
        return api.toggleLike(appId)
    }

    fun toggleCommentLike(appId:String?): Observable<Response<JsonObject?>> {
        return api.toggleCommentLike(appId)
    }


    fun publishComment(postId : String?,body:JsonObject): Flowable<Response<JsonObject>>{
        return api.publishComment(postId,body)
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun replyComment(postId : String?,body:JsonObject): Flowable<Response<JsonObject>>{
        return api.replyComment(postId,body)
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun searchPost(searchString:String?):Flowable<Response<SearchPostResponse>>{
        return api.searchPost(searchString)
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    fun deleteComment(commentId: String?):Flowable<Response<JsonObject>>{
        return api.deleteComment(commentId)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun deleteCommentReply(commentId: String?):Flowable<Response<JsonObject>>{
        return api.deleteCommentReply(commentId)
            .doOnSubscribe {  }
            .doOnTerminate {  }
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun deletePost(postid:String?):Flowable<Response<JsonObject>>{
        return api.deletePost(postid)
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    }

    fun updatePost(postid: String?, request: PostUpdateRequest):Flowable<Response<JsonObject>>{
        return api.updatePost(postid,request)
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    fun ingestPost(body:JsonObject):Flowable<Response<JsonObject>>{
        return api.ingestPost(body)
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }



    fun reportContent(body:JsonObject):Flowable<Response<JsonObject>>{
        return api.reportContent(body)
            .toFlowable(BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }


}