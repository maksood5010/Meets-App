package com.meetsportal.meets.networking.directmessage.datasource

import android.util.Log
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.QuerySnapshot
import com.meetsportal.meets.networking.directmessage.DM
import com.meetsportal.meets.repository.AppRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.lang.Exception

class DirectMessagePagingDataSource(val service: AppRepository):
    RxPagingSource<Int, DM>(){

    val TAG = DirectMessagePagingDataSource::class.java.simpleName

    lateinit var messageThread : String

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, DM>> {

        val page = params.key ?: 1
        return service.getDirectMessage(page,messageThread)
            .subscribeOn(Schedulers.io())
            .map {
                service.timestamp = it.documents.last().data?.get("timestamp").toString()
                Log.i(TAG," checkingTimestampupfdate::: ${service.timestamp}")
                toLoadResult(it, page)
            }
            .onErrorReturn {
                LoadResult.Error(it)
            }
    }

    private fun toLoadResult(
        data: QuerySnapshot,
        page: Int
    ): LoadResult<Int, DM> {
        Log.i("TAG"," mappingstarted:::  1 ${FirebaseAuth.getInstance().currentUser.uid}")
        var list = ArrayList<DM>();
        /*try{
            Log.i("TAG"," mappingstarted::: ${data.toObjects(DirectMessage::class.java)}")
            data.documents.map {
                list.add(it.toObject(DirectMessage::class.java)!!.apply {
                    document_id = it.id
                })
            }

        }catch (e: Exception){
            Log.e("TAG"," missMappingHappened::  $e")
        }*/

        try{
            Log.i("TAG"," mappingstarted:::  3 ${data.toObjects(DM::class.java)}")
        }catch (e:Exception){
            e.printStackTrace()
        }

        return LoadResult.Page(
            data = data.toObjects(DM::class.java),
            //data = list,
            prevKey = if (page <= 1) null else page - 1,
            //nextKey = if (page >= totalPage!!) null else page + 1
            nextKey = if (data.size() == 0) null else page + 1
        )
    }



    override fun getRefreshKey(state: PagingState<Int, DM>): Int? {
        /*return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }*/
        return 1
    }

}