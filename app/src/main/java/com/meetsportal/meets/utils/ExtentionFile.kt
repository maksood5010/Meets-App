package com.meetsportal.meets.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.location.Location
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import com.androidadvance.topsnackbar.TSnackbar
import com.jakewharton.rxbinding2.view.RxView
import com.meetsportal.meets.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


fun EditText.count(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            cb(s.toString().count().toString())
        }
    })
}

fun EditText.currentText(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            cb(s.toString())
        }
    })
}

fun View.onClick(function: () -> Unit, timeInMilli: Long = 1000): Disposable {
    return RxView.clicks(this).throttleFirst(timeInMilli, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread()).subscribe {
            function.invoke()
        }
}
fun TextView.verifyIcon(verifiedUser: Boolean?) {
    Log.d("TAG", "verifyIcon:verifiedUser:$verifiedUser ")
    verifiedUser?.let{
        if (verifiedUser){ this.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_verified_tick, 0) }
        else { this.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0) }
    }?:run{
        this.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
    }
}
fun TextView.iconStart(int: Int) {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds( int, 0,0, 0)
}
fun TextView.iconTop(int: Int) {
    this.setCompoundDrawablesRelativeWithIntrinsicBounds( 0, int,0, 0)
}
fun EditText.onText(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            cb(s.toString())
        }
    })
}

fun CompoundButton.enabling(cb: (Boolean) -> Unit){
    this.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
            cb(p1)
        }
    })

}

fun String.toDate(
    dateFormat: String = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timeZone: TimeZone = TimeZone.getTimeZone(
        "UTC"
    )
): Date? {
    try{
        val parser = SimpleDateFormat(dateFormat, Locale.UK)
        parser.timeZone = timeZone
        return parser.parse(this)
    }catch (e:Exception){
        return null
    }
}


fun Date.formatTo(dateFormat: String, timeZone: TimeZone = TimeZone.getDefault()): String {
    val formatter = SimpleDateFormat(dateFormat, Locale.UK)
    formatter.timeZone = timeZone
    return formatter.format(this)
}

fun Date.toCalendar():Calendar{
    val cal = Calendar.getInstance()
    cal.time = this
    return cal
}

fun EditText.singleCharector(cb: (Int) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            cb(count)
        }
    })
}

fun View.showSnackBar(message: String?) {
    message?.let {
        TSnackbar.make(this, message, TSnackbar.LENGTH_LONG).also {
            it.setActionTextColor(Color.WHITE)
            var view = it.view
            view.setBackgroundColor(resources.getColor(R.color.black))
            var textView: TextView =
                view.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text)
            textView.setTextColor(resources.getColor(R.color.white))
            it.show()
        }
    }
}

fun Group.setAllOnClickListener(listener: View.OnClickListener?){
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun EditText.onChange(cb: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            cb(s.toString())
        }
    })
}

inline fun <reified T : Any> Activity.launchActivity(
    finishAffinity: Boolean = false,
    noinline init: Intent.() -> Unit = {}
) {
    val intent = newIntent<T>(this)
    intent.init()
    if (finishAffinity) {
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        if (finishAffinity)
            finishAffinity()
        startActivity(intent)
    } else {
        if (finishAffinity)
            finishAffinity()
        startActivity(intent)
    }
}

inline fun <reified T : Any> newIntent(context: Context): Intent =
    Intent(context, T::class.java)

fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}

@SuppressLint("ResourceAsColor")
fun View.setRoundedColorBackground(activity:Activity, color:Int = R.color.white, corner:Float = 30f, enbleDash:Boolean = false, strokeHeight:Float = 2f, Dashgap:Float = 5f, stripSize:Float = 5f, strokeColor : Int = R.color.white ){
    var shape = GradientDrawable()
    if(enbleDash)
        shape.setStroke(
            Utils.dpToPx(strokeHeight, activity.resources),ContextCompat.getColor(activity,strokeColor), Utils.dpToPx(
                stripSize,
                activity.resources
            ).toFloat(), Utils.dpToPx(Dashgap, activity.resources).toFloat())
    var c=Utils.dpToPx(corner,activity.resources).toFloat()
    shape.cornerRadii = floatArrayOf(c, c, c, c, c, c, c, c)
    if(Utils.isColorResource(activity, color)){
        shape.setColor(ContextCompat.getColor(activity, color))
    }else{
        shape.setColor(color)
    }
    this.background = shape
}
@SuppressLint("ResourceAsColor")
fun View.setBackgroundDash(activity:Activity, color:Int = R.color.white, corner:Float = 30f, enbleDash:Boolean = false, strokeHeight:Float = 2f, Dashgap:Float = 5f, stripSize:Float = 5f, strokeColor : Int = R.color.white ){
    var shape = GradientDrawable()
    if(enbleDash)
        shape.setStroke(
            Utils.dpToPx(strokeHeight, activity.resources),ContextCompat.getColor(activity,strokeColor), Utils.dpToPx(
                stripSize,
                activity.resources
            ).toFloat(), Utils.dpToPx(Dashgap, activity.resources).toFloat())
    var c=Utils.dpToPx(corner,activity.resources).toFloat()
    shape.cornerRadii = floatArrayOf(c, c, c, c, c, c, c, c)
    if(Utils.isColorResource(activity, color)){
        shape.setColor(ContextCompat.getColor(activity, color))
    }else{
        shape.setColor(color)
    }
    this.background = shape
}

fun View.customizeCountBG(myContext : Activity, borderColor:Int, solidColor:Int,dashColor:Int=Color.parseColor("#F7F7F7"),) {
    val layerDrawable : LayerDrawable = ContextCompat.getDrawable(myContext,R.drawable.bg_types) as LayerDrawable
    val itemStroke = layerDrawable.findDrawableByLayerId(R.id.itemStroke) as GradientDrawable
    val itemSolid = layerDrawable.findDrawableByLayerId(R.id.itemSolid) as GradientDrawable
    val itemDash = layerDrawable.findDrawableByLayerId(R.id.itemDash) as GradientDrawable
    itemDash.mutate()
    itemSolid.mutate()
    itemStroke.mutate()
    itemDash.setStroke(
            Utils.dpToPx(1.5f, myContext.resources),dashColor, Utils.dpToPx(10f, myContext.resources).toFloat(),
            Utils.dpToPx(5f, myContext.resources).toFloat())
    itemStroke.setColor(borderColor)
    itemSolid.setColor(solidColor)
    this.background= layerDrawable
}
fun View.setGradient(activity:Activity,orientation : GradientDrawable.Orientation,colors : IntArray,radius : Float = 0f){
    val gd = GradientDrawable(
        //GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(-0x9e9d9f, -0xececed)
        orientation, colors
    )
    gd.cornerRadius = Utils.dpToPx(radius, activity.resources).toFloat()
    this.background = gd
}
fun View.setOneSideBg(color : Int,tLeft:Float=0f,tRight:Float=0f,bLeft:Float=0f,bRight:Float=0f){

    var shape = GradientDrawable()
    shape.cornerRadii = floatArrayOf(tLeft, tLeft, tRight, tRight, bRight, bRight, bLeft, bLeft)
    shape.setColor(color)
    this.background=shape
}