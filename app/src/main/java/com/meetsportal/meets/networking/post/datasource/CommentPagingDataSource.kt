package com.meetsportal.meets.networking.post.datasource

import android.util.Log
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.meetsportal.meets.networking.post.SinglePostComments
import com.meetsportal.meets.networking.post.SinglePostCommentsItem
import com.meetsportal.meets.networking.post.SinglePostResponse
import com.meetsportal.meets.repository.AppRepository
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

class CommentPagingDataSource(val service: AppRepository) : RxPagingSource<Int, SinglePostCommentsItem>() {

    val TAG = CommentPagingDataSource::class.java.simpleName
    var singlePost: SinglePostResponse? = null


    var postId:String? = null
    var mode :String? = null
    var entityType :String? = null
    var referenceId :String? = null
    var parentCommentPublisher : PublishSubject<SinglePostCommentsItem?>? = null

    fun setPost(postId: String?, mode: String?, entityType: String?, referenceId: String?,parentCommentPublisher : PublishSubject<SinglePostCommentsItem?>?){
        this.postId = postId
        this.mode = mode
        this.entityType = entityType
        this.referenceId = referenceId
        this.parentCommentPublisher = parentCommentPublisher

    }

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, SinglePostCommentsItem>> {
        val page = params.key ?: 1
        Log.i(TAG," jcbkjbfc::: $entityType -- $referenceId")
        return service.getComments(postId,mode,entityType,page,100,referenceId)
            .map {
                Log.i("TAG"," errorChecking:: $it")
                toLoadResult(it, page)
            }.onErrorReturn {
                Log.e("TAG"," error:: ${it.printStackTrace()}")
                LoadResult.Error(it)
            }

    }

    private fun toLoadResult(data: SinglePostComments, page: Int): LoadResult<Int, SinglePostCommentsItem> {
        Log.i(TAG," commetPaging ${data}")
        data.parent_comment?.let { parentCommentPublisher?.onNext(it) }
        return LoadResult.Page(
            data = data.comments,
            prevKey = if (page == 1) null else page - 1,
            nextKey = if (data.comments.size == 0) null else page + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, SinglePostCommentsItem>): Int? {
        Log.i(TAG," anchorPosition:: ${state.anchorPosition}")
        Log.i(TAG," anchorPositionProcessed:: ${state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }}")


        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }


}