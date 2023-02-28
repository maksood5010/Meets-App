package com.meetsportal.meets.networking.meetup.datasource

import android.util.Log
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.meetup.GetMeetUpResponse
import com.meetsportal.meets.networking.meetup.GetMeetUpResponseItem
import com.meetsportal.meets.repository.AppRepository
import com.meetsportal.meets.utils.Constant.VwOpenMeetDiscover
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

class DiscoverMeetPagerDataSource (val service: AppRepository) : RxPagingSource<Int, GetMeetUpResponseItem>()  {

    var countryCode : String? = null
    var city : String?  = null
    var to : String? = null
    var categoryFilter : String? = null
    var minBadge : String? = null

    fun setExtradData(
        countryCode: String?,
        city: String?,
        to: String?,
        categoryFilter: String?,
        minBadge: String?
    ){
        this.countryCode = countryCode
        this.city = city
        this.to = to
        this.categoryFilter = categoryFilter
        this.minBadge = minBadge
    }


    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, GetMeetUpResponseItem>> {
        val page = params.key ?: 1
        return service.discoverMeetUp(countryCode,city,to,categoryFilter,minBadge,page)
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
        MyApplication.putTrackMP(VwOpenMeetDiscover, JSONObject(mapOf("page" to page,"category" to categoryFilter,
            "city" to city,"minBadge" to minBadge
        )))

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