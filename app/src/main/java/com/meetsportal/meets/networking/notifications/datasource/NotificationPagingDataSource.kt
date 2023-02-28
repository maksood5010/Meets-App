package com.meetsportal.meets.networking.notifications.datasource

import android.util.Log
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource


import com.google.firebase.auth.FirebaseAuth
import com.meetsportal.meets.networking.notifications.Notification
import com.meetsportal.meets.repository.AppRepository

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class NotificationPagingDataSource(val service: AppRepository,val defaulterNotification: ArrayList<String?>) :
    RxPagingSource<Int, Notification>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, Notification>> {

        val page = params.key ?: 1
        if(page == 1){
            Log.i("TAG"," PageNumberOne::: ")

        }
        return service.getFireNotification(page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
//                service.timestamp = it.first?.documents?.last()?.data?.get("timestamp").toString()
               // if(it.first.isNotEmpty())
//                defaulterNotification.add
                toLoadResult(it.first, page)
            }
            .onErrorReturn {
                Log.i("TAG", "loadSingle: jhfbdhkmn")
                LoadResult.Error(it)
                //toLoadResult(ArrayList(), page)
            }
    }


    private fun toLoadResult(
        data: List<Notification>,
        page: Int
    ): LoadResult<Int, Notification> {
        Log.i("TAG"," mappingstarted:::  1 ${FirebaseAuth.getInstance().currentUser.uid}")
        Log.i("TAG"," mappingstarted:::  2 ${data}")
        var filteredData = data.filter { !defaulterNotification.contains(it.entity_id) }
        //var list = ArrayList<Notification>();

        return LoadResult.Page(
            //data = data.toObjects(Notification::class.java),
            data = filteredData,
            prevKey = if (page <= 1) null else page - 1,
            //nextKey = if (page >= totalPage!!) null else page + 1
            nextKey = if (filteredData.isEmpty()) null else page + 1
        )
    }


    override fun getRefreshKey(state: PagingState<Int, Notification>): Int? {
        Log.i("TAG", " state.anchorPosition :: ${state.anchorPosition}")
        /*var position  = state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
        Log.i("TAG"," position::::::: $position")*/
        return 1
    }


}