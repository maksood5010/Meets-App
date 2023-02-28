package com.meetsportal.meets.networking.meetup.datasource

import android.util.Log
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.meetsportal.meets.networking.meetup.GetMeetUpResponse
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.repository.AppRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class MeetUpListPagerDataSource(val service: AppRepository) : RxPagingSource<Int, GetMeetUpResponseItem>()  {

    var scope : String? = null
    var dateRange : String? = null
    var filter : String? = null
    var type : String? = null

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, GetMeetUpResponseItem>> {
        val page = params.key ?: 1
        return service.fetchMeetUPFilter(scope,dateRange,type,page,filter=filter)
            .subscribeOn(Schedulers.io())
            .map {
                Log.i("TAG"," errorChecking:: $it")
                toLoadResult(if(it.isSuccessful) it.body()!! else GetMeetUpResponse(), page)
            }
            .onErrorReturn {
                Log.e("TAG"," Error:: $it")
                Log.e("TAG"," Error:: ${it.printStackTrace()}")
                LoadResult.Error(it)
            }


    }

    private fun toLoadResult(
        data: GetMeetUpResponse,
        page: Int
    ): LoadResult<Int, GetMeetUpResponseItem> {

        Log.i("TAG"," FetchPlacesResponse:: $data ")

        return LoadResult.Page(
            data = data,
            prevKey = if (page <= 1) null else page - 1,
            //nextKey = if (page >= totalPage!!) null else page + 1
            nextKey = if (data?.size == 0) null else page + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, GetMeetUpResponseItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}