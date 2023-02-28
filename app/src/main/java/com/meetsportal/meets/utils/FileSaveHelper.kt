package com.meetsportal.meets.utils

import android.content.ContentResolver
import android.os.Build
import androidx.lifecycle.LifecycleObserver

/**
 * General contract of this class is to
 * create a file on a device.
 * </br>
 * How to Use it-
 * Call {@linkplain FileSaveHelper#createFile(String, OnFileCreateResult)}
 * if file is created you would receive it's file path and Uri
 * and after you are done with File call {@linkplain FileSaveHelper#notifyThatFileIsNowPubliclyAvailable(ContentResolver)}
 * </br>
 * Remember! in order to shutdown executor call {@linkplain FileSaveHelper#addObserver(LifecycleOwner)} or
 * create object with the {@linkplain FileSaveHelper#FileSaveHelper(AppCompatActivity)}
 */
class FileSaveHelper(contentResolver: ContentResolver) : LifecycleObserver{



    /*private var mContentResolver: ContentResolver? = null
    private var executor: ExecutorService? = null
    private var fileCreatedResult: MutableLiveData<FileMeta>? = null
    private val resultListener: OnFileCreateResult? = null
*/
    /*init {
        this.mContentResolver = contentResolver
        executor = Executors.newSingleThreadExecutor()
        fileCreatedResult =
            MutableLiveData<FileMeta>()
    }
    */
    /*constructor(activity: AppCompatActivity) : this(activity.contentResolver){
        addObserver(activity)
    }*/

    /*private val observer: Observer<FileMeta> =
        Observer<cFileMeta> { fileMeta: FileMeta ->
            if (resultListener != null) {
                resultListener.onFileCreateResult(
                    fileMeta.isCreated,
                    fileMeta.filePath,
                    fileMeta.error,
                    fileMeta.uri
                )
            }
        }*/


    /*private fun addObserver(lifecycleOwner: LifecycleOwner) {
        fileCreatedResult?.observe(lifecycleOwner, observer)
        lifecycleOwner.lifecycle.addObserver(this)
    }
*/
    /*@OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun release() {
        if (null != executor) {
            executor!!.shutdownNow()
        }
    }*/

    fun isSdkHigherThan28(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }
}