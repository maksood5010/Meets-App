package com.meetsportal.meets.utils

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent

class MyGestureListener(var swipeDown: (Direction?) -> Unit,var clicked:()->Unit) :
    GestureDetector.SimpleOnGestureListener() {

    val TAG = MyGestureListener::class.java.simpleName

    override fun onDown(e: MotionEvent?): Boolean {
        Log.i(TAG, " onDown:: ${e?.action}")
        return super.onDown(e)
    }

    override fun onShowPress(e: MotionEvent?) {
        Log.i(TAG, " onShowPress:: ${e?.action}")
        super.onShowPress(e)
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        Log.i(TAG, " onSingleTapUp:: ${e?.action}")
        return super.onSingleTapUp(e)
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {

        val x1 = e1?.x
        val y1 = e1?.y

        val x2 = e2?.x
        val y2 = e2?.y

        var direction :Direction? = null
            try {
                direction = getDirection(x1, y1, x2, y2)
            }catch(e:Exception){

            }
        swipeDown(direction)
        return super.onFling(e1, e2, velocityX, velocityY)
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        Log.i(TAG, " onSingleTapConfirmed:: ${e?.action}")
        clicked()
        swipeDown(Direction.singleTap)
        return super.onSingleTapConfirmed(e)
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        Log.i(TAG, " onDoubleTap:: ${e?.action}")
        return super.onDoubleTap(e)
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        Log.i(TAG, " onDoubleTapEvent:: ${e?.action}")
        swipeDown(Direction.doubleTap)
        return super.onDoubleTapEvent(e)
    }

    override fun onContextClick(e: MotionEvent?): Boolean {
        Log.i(TAG, " onContextClick:: ${e?.action}")
        return super.onContextClick(e)
    }

    /**
     * Given two points in the plane p1=(x1, x2) and p2=(y1, y1), this method
     * returns the direction that an arrow pointing from p1 to p2 would have.
     * @param x1 the x position of the first point
     * @param y1 the y position of the first point
     * @param x2 the x position of the second point
     * @param y2 the y position of the second point
     * @return the direction
     */
    fun getDirection(x1: Float?, y1: Float?, x2: Float?, y2: Float?): Direction? {
        val angle: Double = getAngle(x1, y1, x2, y2)
        return fromAngle(angle)
    }

    fun getAngle(x1: Float?, y1: Float?, x2: Float?, y2: Float?): Double {
        val rad = Math.atan2(
            (y1?.minus(y2!!))
                ?.toDouble()!!, (x2?.minus(x1!!))
                ?.toDouble()!!
        ) + Math.PI
        return (rad * 180 / Math.PI + 180) % 360
    }

    fun fromAngle(angle: Double): Direction? {
        if (angle >= 45 && angle < 135) {
            return Direction.up;
        } else if ((angle >= 0 && angle < 45) || (angle >= 315 && angle < 360)) {
            return Direction.right;
        } else if (angle >= 225 && angle < 315) {
            return Direction.down;
        } else {
            return Direction.left;
        }
    }


    enum class Direction {
        up,
        down,
        left,
        right,
        singleTap,
        doubleTap;
    }

}