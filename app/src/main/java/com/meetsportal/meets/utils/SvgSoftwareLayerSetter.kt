package com.meetsportal.meets.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.PictureDrawable
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class SvgSoftwareLayerSetter(
    var myContext: Context,
    var path : String?,
    var parent: View? = null,
    var marker:Marker? = null,
    var markerOptions: MarkerOptions? = null) : RequestListener<PictureDrawable> {

    val TAG  = SvgSoftwareLayerSetter::class.java.simpleName

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<PictureDrawable>?,
        isFirstResource: Boolean
    ): Boolean {
        Log.i("TAG"," onLoadFailed::::::: ${e?.printStackTrace()}")
        val view: ImageView = (target as ImageViewTarget<*>).view
        view.setLayerType(ImageView.LAYER_TYPE_NONE, null)
        return false
    }

    override fun onResourceReady(
        resource: PictureDrawable?,
        model: Any?,
        target: Target<PictureDrawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {

        Log.i(TAG," dataSource::: ${marker?.title} ${dataSource} target ${target} isFirstResource ${isFirstResource}")
        val view = (target as ImageViewTarget<*>).view
        view.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null)


        Handler(Looper.getMainLooper()).postDelayed({
                changeMarker()
        },1)

        return false
    }


    fun changeMarker(){
        parent?.let {it->
            Log.i("TAG"," markerAdding")
            val displayMetrics = DisplayMetrics()
            (myContext as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            it.setLayoutParams(
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            it.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
            it.layout(1, 1, displayMetrics.widthPixels, displayMetrics.heightPixels)

            it.buildDrawingCache()
            Log.i(Utils.TAG," viewMeasure:::: ${it.measuredWidth} ${it.measuredHeight}")
           // stickImage(myContext!!,imageView,image, RequestOptions().apply{override(80,80)})
            val bitmap = Bitmap.createBitmap(
                if(it.measuredWidth <= 0) 1 else it.measuredWidth,
                if(it.measuredHeight <= 0) 1 else it.measuredHeight,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            it.draw(canvas)
            marker?.let {marker->
                Log.i("TAG"," markerVISIBLE:: ")
                marker?.let { it.isVisible = true }
                Log.i(TAG," markerTag::: 2 ${marker.tag}")
                marker.tag?.let {
                    marker.setIcon(
                        BitmapDescriptorFactory.fromBitmap(bitmap)
                    )
                }
            }

            markerOptions?.let{
                markerOptions?.visible(true)
                Log.i("TAG"," markerOptions:: not null")
                it.icon(
                    BitmapDescriptorFactory.fromBitmap(bitmap)
                )
            }

            Log.i("TAG"," markerOptionsVisibility:::: ${markerOptions?.isVisible}")

        }
    }

}