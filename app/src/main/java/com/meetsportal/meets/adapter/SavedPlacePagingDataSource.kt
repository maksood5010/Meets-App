package com.meetsportal.meets.adapter

import android.util.Log
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.meetsportal.meets.networking.places.FetchPlacesResponse
import com.meetsportal.meets.networking.places.FetchPlacesResponseItem
import com.meetsportal.meets.repository.AppRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class SavedPlacePagingDataSource(val service: AppRepository):
    RxPagingSource<Int, FetchPlacesResponseItem>() {


    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, FetchPlacesResponseItem>> {
        val page = params.key ?: 1
        Log.i("TAG"," nbcjhbdjc:: ")
        return service.getSavedPlaces(page)
            .subscribeOn(Schedulers.io())
            .map {
                Log.i("TAG"," errorChecking:: $it")
                toLoadResult(it, page)
            }
            .onErrorReturn {
                Log.e("TAG"," Error:: $it")
                Log.e("TAG"," Error:: ${it.printStackTrace()}")
                LoadResult.Error(it)
            }
    }

    private fun toLoadResult(
        data: FetchPlacesResponse,
        page: Int
    ): LoadResult<Int, FetchPlacesResponseItem> {

        Log.i("TAG"," FetchPlacesResponse:: $data ")

        return LoadResult.Page(
            data = data,
            prevKey = if (page <= 1) null else page - 1,
            //nextKey = if (page >= totalPage!!) null else page + 1
            nextKey = if (data.size == 0) null else page + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, FetchPlacesResponseItem>): Int? {
        return state.anchorPosition
    }
}