package com.meetsportal.meets.networking.post.datasource

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.meetsportal.meets.networking.post.*
import com.meetsportal.meets.repository.AppRepository
import io.reactivex.Single

class PostLikePagingDataSource(val service: AppRepository) : RxPagingSource<Int, PostLikerResponseItem>()  {

    var postId:String? = null

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, PostLikerResponseItem>> {

        val page = params.key ?: 1

        return service.getPostLiker(postId,page)
            .map { toLoadResult(it, page) }
            .onErrorReturn {
                LoadResult.Error(it)
            }

    }

    private fun toLoadResult(data: PostLikerResponse, page: Int): LoadResult<Int, PostLikerResponseItem> {
        return LoadResult.Page(
            data = data,
            prevKey = if (page <= 1) null else page - 1,
            //nextKey = if (page >= totalPage!!) null else page + 1
            nextKey = if (data.size == 0) null else page + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, PostLikerResponseItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
       // return state.anchorPosition
    }
}