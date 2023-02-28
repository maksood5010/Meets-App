package com.meetsportal.meets.overridelayout

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground

class CenterZoomLinearLayoutManager(
    var myContext: Activity,
    private val mShrinkDistance: Float = 2f,
    val mShrinkAmount: Float = 0.4f,val callBack : (View,Int,Boolean)->Unit)
    : LinearLayoutManager(myContext, LinearLayoutManager.HORIZONTAL, false) {

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        scaleChildren()
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        return if (orientation == HORIZONTAL) {
            super.scrollHorizontallyBy(dx, recycler, state).also {
                scaleChildren()
            }
        } else {
            0
        }
    }

    fun getPosition():Int?{
        for(i in 0 until childCount){
            val child = getChildAt(i) as View
            if(child.scaleX > 0.99){
                return this.getPosition(child)
            }
        }
        return null
    }


    fun scaleChildren() {
        val midpoint = width / 2f
        val d1 = mShrinkDistance * midpoint
        for (i in 0 until childCount) {
            val child = getChildAt(i) as View
            val d = Math.min(d1, Math.abs(midpoint - (getDecoratedRight(child) + getDecoratedLeft(child)) / 2f))
            val scale = 1f - mShrinkAmount * d / d1
            child.scaleX = scale
            child.scaleY = scale
            //child.onClick({scrollItem},100)
            if(scale  > 0.99){
                getPosition()?.let{
                    callBack(child,it,true)
                }
                /*child.findViewById<LinearLayout>(R.id.bottomBg).apply {
                    setRoundedColorBackground(myContext, corner = 0f, enbleDash = false, color = Color.parseColor("#42B05E"))
                }*/
            }else{
                getPosition()?.let{
                    callBack(child,it,false)
                }
                /*child.findViewById<LinearLayout>(R.id.bottomBg).apply {
                    setRoundedColorBackground(myContext, corner = 0f, enbleDash = false, color = Color.parseColor("#73D399"))
                }*/
            }

            /*if(child.scaleX > 0.9){
                middleItemIndex(i)
            }*/
        }
    }
}