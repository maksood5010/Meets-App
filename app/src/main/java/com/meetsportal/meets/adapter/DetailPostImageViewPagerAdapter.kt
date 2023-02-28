package com.meetsportal.meets.adapter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ablanco.zoomy.Zoomy
import com.meetsportal.meets.R
import com.meetsportal.meets.networking.post.SinglePostResponse
import com.meetsportal.meets.ui.activities.MemeIt
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.ImageSamplingFragment
import com.meetsportal.meets.utils.Constant.URL_EXTRA
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import java.util.ArrayList

class DetailPostImageViewPagerAdapter(
    var pager: ViewPager,
    var myContext: Activity,
    var isResizabel: Boolean,
    var post: SinglePostResponse,
    var optionListner: (Int) -> Unit,
    var backPressed:(Boolean) ->Unit
) : PagerAdapter() {

    private val TAG = DetailPostImageViewPagerAdapter::class.java.simpleName

    private val inflater: LayoutInflater
    var b = HashMap<Int, Int>()
    var firsTime = true
    var screenWidth: Int

    init {
        inflater = LayoutInflater.from(myContext)
        screenWidth = Utils.getWindowWidth(myContext as Activity)
        Log.i("TAG", " isResizabel::: $isResizabel ")
        //if(isResizabel)
        pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.i("TAG", " selectedPage:: $position ")
                // if (position == 0) {
//                    var a = pager.layoutParams
//                    a.height = b.get(position)!!
//                    pager.layoutParams = a
                //changePagerHight(b.get(position)!!)

                // }
            }
        })
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view.equals(`object`)
    }

    override fun getCount() = post.media?.size!!

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        Log.i("TAG", " destroyItem:: $position ")
        container.removeView(`object` as View)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var imageLayout = inflater.inflate(R.layout.card_sliding_post, container, false);
        var imageView = imageLayout.findViewById<ImageView>(R.id.image)
        var root = imageLayout.findViewById<FrameLayout>(R.id.rootCo)
        var blurimageView = imageLayout.findViewById<ImageView>(R.id.blur_image)

        Utils.stickImage(myContext,imageView,post.media?.getOrNull(position),null)
//        Utils.stickImage(myContext,imageView,post.media?.get(position),null)

        val builder: Zoomy.Builder = Zoomy.Builder(myContext)
            .target(imageView)
            .enableImmersiveMode(false)
            .animateZooming(false)
            .tapListener {
                var fragment: BaseFragment = ImageSamplingFragment() {
                    backPressed(true)
                }
                var bundle: Bundle = Bundle()
                bundle.putSerializable("media", ArrayList<String>().apply {
                    addAll(post.media as Collection<String>)
                })
                fragment.arguments = bundle
                Navigation.addFragment(
                    myContext,
                    fragment,
                    "ImageSamplingFragment",
                    R.id.homeFram,
                    true,
                    false,
                    exitAnim = R.anim.slide_out_bottom
                )
            }.longPressListener{
                optionListner(0)
            }
        builder.register()

        imageView.setOnClickListener {}



        container.addView(imageLayout, 0);

        return imageLayout
    }

    fun changePagerHight(hight: Int) {
        /*if (isResizabel) {
            Log.i("TAG", " changesTo:: $hight")
            var a = pager.layoutParams
            a.height = hight
            pager.layoutParams = a
        }*/
    }

    fun calculateHight(h: Int, w: Int): Int {
        Utils
        Log.i("TAG", " calculateHight:: ${screenWidth} $h $w")
        return (screenWidth * h / w)
    }

    fun openMemeIt(url: String) {
        Intent(myContext, MemeIt::class.java).also {
            it.putExtra(URL_EXTRA, url)
            myContext.startActivity(it)
        }


    }
}