package com.meetsportal.meets.networking.post.datasource

import android.util.Log
import androidx.paging.PagingState
import androidx.paging.rxjava2.RxPagingSource
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.post.FetchPostResponse
import com.meetsportal.meets.networking.post.FetchPostResponseItem
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.repository.AppRepository
import com.meetsportal.meets.ui.bottomsheet.ChooseTimeLineFeedSheet.Companion.GLOBAL_TIMELINE
import com.meetsportal.meets.ui.bottomsheet.ChooseTimeLineFeedSheet.Companion.MY_TIMELINE
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.VwFeed_Global
import com.meetsportal.meets.utils.Constant.VwFeed_TimeLine
import com.meetsportal.meets.utils.PreferencesManager
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PostPagingDataSource @Inject constructor(private val service: AppRepository) : RxPagingSource<Int, FetchPostResponseItem>() {

    val TAG = PostPagingDataSource::class.java.simpleName

    var uid :String? = null
    var feedOption :Int ? = 0
    var totalPage : Int? = 0
    val limit = 10
    var isItTimeLine :Boolean = false

   init {
      PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)?.let {
          uid = it.social?.sid
          totalPage = (it.social?.posts_count?.toInt()?.div(limit))
      }

   }

    fun setConfigration(uid:String?, feedoption :Int,isItTimeLine : Boolean = false){
        this.uid = uid
        this.feedOption = feedoption
        this.isItTimeLine = isItTimeLine
    }

    fun setTotalPage(totalPage : Int){
        this.totalPage = totalPage
    }

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, FetchPostResponseItem>> {

        val page = params.key ?: 1

        Log.i(TAG," loadSingle:: ${page}")
        return service.fetchPostFromAws(feedOption!!,uid, page.toString(),limit.toString())
            .subscribeOn(Schedulers.io())
            .map { toLoadResult(it, page) }
            .onErrorReturn {
                Log.i("TAG"," checking error::: ")
                if(page == 1 && isItTimeLine) {
                    toLoadResult(FetchPostResponse().apply {
                        //add(FetchPostResponseItem())
                    }, page)
                }else{
                    LoadResult.Error(it)
                }
                //
            }

    }



    private fun toLoadResult(data: FetchPostResponse, page: Int): LoadResult<Int, FetchPostResponseItem> {
        if(feedOption == GLOBAL_TIMELINE){
            MyApplication.putTrackMP(VwFeed_Global, JSONObject(mapOf("page" to page)))
        }else if(feedOption == MY_TIMELINE){
            MyApplication.putTrackMP(VwFeed_TimeLine,JSONObject(mapOf("page" to page)))
        }
        if(page == 1 && isItTimeLine) data.add(0,FetchPostResponseItem())
        return LoadResult.Page(
            data = data.filter { it.deleted == false && it.hidden == false },
            prevKey = if (page <= 1) null else page - 1,
            //nextKey = if (page >= totalPage!!) null else page + 1
            nextKey = if (data.size == 0) null else page + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, FetchPostResponseItem>): Int? {
        Log.i("TAG"," getRefreshKey:: ${state.anchorPosition}")
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }


}