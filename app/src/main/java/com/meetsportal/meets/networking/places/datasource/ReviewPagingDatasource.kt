package com.meetsportal.meets.networking.places.datasource

import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.meetsportal.meets.networking.places.FetchReviewResponse
import com.meetsportal.meets.networking.places.FetchReviewResponseItem
import com.meetsportal.meets.repository.AppRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class ReviewPagingDatasource(val service: AppRepository,var placeId : String?) :
    RxPagingSource<Int, FetchReviewResponseItem>()  {



    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, FetchReviewResponseItem>> {

        val page = params.key ?: 1

       return service.getMyReviewSingle(placeId,page)
           .subscribeOn(Schedulers.io())
            .map { toLoadResult(it, page) }
            .onErrorReturn {
                LoadResult.Error(it)
            }
    }

    private fun toLoadResult(
        data: FetchReviewResponse,
        page: Int
    ): LoadResult<Int, FetchReviewResponseItem> {

        return LoadResult.Page(
            data = data,
            prevKey = if (page <= 1) null else page - 1,
            //nextKey = if (page >= totalPage!!) null else page + 1
            nextKey = if (data.size == 0) null else page + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, FetchReviewResponseItem>): Int? {
        return state.anchorPosition
    }
}