package com.meetsportal.meets.networking.profile.dastasource

import android.util.Log
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.meetsportal.meets.models.ProfileInsight
import com.meetsportal.meets.models.ProfileView
import com.meetsportal.meets.models.ViewdMe
import com.meetsportal.meets.models.ViewdMeItem
import com.meetsportal.meets.repository.AppRepository
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.*

class ProfileInsightPeosonDataSource (private val service: AppRepository) : RxPagingSource<Int, ViewdMeItem>() {

    val TAG = ProfileInsightPeosonDataSource::class.java.simpleName

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, ViewdMeItem>> {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val current = Calendar.getInstance();

        val page = params.key ?: 1
        Log.i(TAG," checkingPageNuber::: $page -- ${params.key}")

        return service.getViewedMe(page)
                .map {
                    Log.i("TAG"," follwing Responsehere:: ${it}")
                    Log.i("TAG"," follwing Responsehere:: ${it.body()?.isEmpty()}")
                    toLoadResult(it.body()!!, page)
                }
                .onErrorReturn {
                    LoadResult.Error(it)
                }
    }

    private fun toLoadResult(data: ViewdMe, page: Int): LoadResult<Int, ViewdMeItem> {
        return LoadResult.Page(
            data = data,
            prevKey = if (page <= 1) null else page - 1,
            //nextKey = if (page >= totalPage!!) null else page + 1
            nextKey = if (data.isEmpty()) null else page + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, ViewdMeItem>): Int? {
        return state.anchorPosition
    }

}