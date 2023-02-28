package com.meetsportal.meets.ui.fragments.verification

import android.net.Uri
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment
import java.io.File

abstract class BaseVerificationFragment : BaseFragment() {

    companion object {

        var image1Uri: Uri? = null
        var image2Uri: Uri? = null
        /*get() = field
        set(value) {
            field = value
            Log.i("BaseVerification"," valueSet:: image2Uri")
            image2UriCame(value)
            value?.let {
                //image2UriCame(it)
            }
        }*/


    }

    abstract fun image1UriCame(uri: Uri?)
    abstract fun image2UriCame(uri: Uri?)

    fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if(mediaDir != null && mediaDir.exists()) mediaDir else requireActivity().filesDir
    }


}