package com.meetsportal.meets.networking.profile.dastasource

import android.util.Log
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.meetsportal.meets.networking.profile.FollowerFollowingResponse
import com.meetsportal.meets.networking.profile.FollowerFollowingResponseItem
import com.meetsportal.meets.repository.AppRepository
import io.reactivex.Single

class FollowerFollowingPagerDataSource(val service: AppRepository) : RxPagingSource<Int, FollowerFollowingResponseItem>()  {

    var sid:String? = null
    var relation : String? = null
    var searchString : String? = null

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, FollowerFollowingResponseItem>> {
        val page = params.key ?: 1

        return if(searchString == null)
            service.getRelations(sid,relation,page)
            .map {
                Log.i("TAG"," follwing Responsehere:: ${it}")
                toLoadResult(it, page)
            }
            .onErrorReturn {
                LoadResult.Error(it)
            }
        else
            service.searchRelation(sid,relation,page,searchString)
                .map { toLoadResult(it, page) }
                .onErrorReturn {
                    LoadResult.Error(it)
                }

    }

    private fun toLoadResult(data: FollowerFollowingResponse, page: Int): LoadResult<Int, FollowerFollowingResponseItem> {
        return LoadResult.Page(
            data = data,
            prevKey = if (page <= 1) null else page - 1,
            //nextKey = if (page >= totalPage!!) null else page + 1
            nextKey = if (data.size == 0) null else page + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, FollowerFollowingResponseItem>): Int? {
        return state.anchorPosition
    }
}