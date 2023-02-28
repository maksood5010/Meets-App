package com.meetsportal.meets.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

class DataConverter {

    companion object{
        fun convertImage2ByteArray(bitmap : Bitmap):ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG,50,stream)
            return stream.toByteArray()
        }
    }

}