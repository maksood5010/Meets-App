package com.meetsportal.meets.overridelayout

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import java.lang.Exception

class StickerEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
):AppCompatEditText(context,attrs) {

    init {
        initView()
    }

    private var imgTypeString: Array<String?>? = null
    private var keyBoardInputCallbackListener: KeyBoardInputCallbackListener? = null

    private fun initView(){
        Log.e("TAG", " debugging:  1")
        imgTypeString = arrayOf(
            "image/jpeg",
            "image/webp",
            "image/png",
        )
    }

    val callback = InputConnectionCompat.OnCommitContentListener{ inputContentInfo: InputContentInfoCompat, flags: Int, bundle: Bundle? ->
        Log.e("TAG", " debugging:  3 ${bundle}")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && (flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION)!= 0){
            try {
                inputContentInfo.requestPermission()
            } catch (e: Exception) {
                return@OnCommitContentListener false // return false if failed
            }
        }
        Log.d("TAG", " checkingMiMeType: ${inputContentInfo.linkUri} ")
        Log.d("TAG", " checkingMiMeType: ${inputContentInfo.contentUri} ")
        Log.d("TAG", " checkingMiMeType: ${inputContentInfo.unwrap()} ")
        var supported = false
        for (mimeType in imgTypeString!!) {
            if (inputContentInfo.description.hasMimeType(mimeType)) {
                supported = true
                break
            }
        }
        if (!supported) {
            return@OnCommitContentListener false;
        }
        if (keyBoardInputCallbackListener != null) {
            keyBoardInputCallbackListener?.onCommitContent(inputContentInfo, flags, bundle);
        }
        true
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo?): InputConnection? {
        Log.e("TAG", " debugging:  2")
        val  ic:InputConnection? = super.onCreateInputConnection(outAttrs);

        EditorInfoCompat.setContentMimeTypes(outAttrs!!, imgTypeString);
        return InputConnectionCompat.createWrapper(ic!!, outAttrs, callback);
    }



    interface KeyBoardInputCallbackListener{
        fun onCommitContent(inputContentInfo: InputContentInfoCompat, flag:Int, opts:Bundle?)
    }

    fun setKeyBoardInputCallbackListener(keyBoardInputCallbackListener:KeyBoardInputCallbackListener){
        Log.e("TAG", " debugging:  4")
        this.keyBoardInputCallbackListener = keyBoardInputCallbackListener;
    }

    fun getImgTypeString(): Array<String?>? {
        Log.e("TAG", " debugging:  5")
        return imgTypeString
    }

    fun setImgTypeString(imgTypeString: Array<String?>?) {
        Log.e("TAG", " debugging:  6")
        this.imgTypeString = imgTypeString
    }

}