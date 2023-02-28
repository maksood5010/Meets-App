package com.meetsportal.meets.adapter

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.MyGestureListener
import com.meetsportal.meets.utils.Utils
import java.util.*

class ImageSamplingViewPagerAdapter(var myContext: Context, var IMAGES: ArrayList<String>,var backPressed:(Boolean) ->Unit): PagerAdapter() {

    lateinit var mDetector : GestureDetector

    private val inflater: LayoutInflater

    init {
        inflater = LayoutInflater.from(myContext)
        mDetector = GestureDetector(myContext,MyGestureListener({
            if(it == MyGestureListener.Direction.down)
                backPressed(true)
        },{

        }))
    }

    override fun getCount(): Int {
        return IMAGES.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view.equals(`object`)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var imageLayout = inflater.inflate(R.layout.card_image_sampling, container, false);
        var imageView = imageLayout.findViewById<ImageView>(R.id.image)
        var root = imageLayout.findViewById<FrameLayout>(R.id.rootCo)
        var blurimageView = imageLayout.findViewById<ImageView>(R.id.blur_image)
        imageView.setOnTouchListener(Utils.getTouchlistener(mDetector))

        Utils.stickImage(myContext,imageView,IMAGES.get(position),null)
        Utils.stickImage(myContext,blurimageView,IMAGES.get(position),null)


        container.addView(imageLayout, 0);
        return imageLayout
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        Log.i("TAG", " destroyItem:: $position ")
        container.removeView(`object` as View)
    }
}