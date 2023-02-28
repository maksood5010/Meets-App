package com.meetsportal.meets.adapter

import android.content.Context
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.google.firebase.storage.FirebaseStorage
import com.jsibbold.zoomage.ZoomageView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.RestaurantImage
import com.meetsportal.meets.utils.Utils

class SlidingImage_Adapter(
    private val context: Context,
    private val imageModelArrayList: ArrayList<RestaurantImage?>
) : PagerAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return imageModelArrayList.size
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val imageLayout = inflater.inflate(R.layout.slidingimages_layout, view, false)!!

        val imageView = imageLayout
            .findViewById(R.id.image) as ZoomageView


        imageModelArrayList.get(position)?.imageUrl?.let {
            if (it.contains("firebasestorage")) {
                Utils.stickImage(context,imageView,it,null)
            } else
                FirebaseStorage.getInstance()
                    .getReference(it).downloadUrl.addOnCompleteListener { it1 ->
                        if (it1.isSuccessful) {
                            Utils.stickImage(context.applicationContext,imageView,it1.result.toString(),null)
                            imageModelArrayList[position]?.imageUrl  = it1.result.toString()
                        }
                    }
        }


        //Glide.with(context).load(imageModelArrayList[position]?.imageUrl).into(imageView)
        view.addView(imageLayout, 0)

        return imageLayout
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}

    override fun saveState(): Parcelable? {
        return null
    }


}