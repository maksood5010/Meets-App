package com.meetsportal.meets.utils

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.Resources.NotFoundException
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.PictureDrawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.format.DateUtils
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.*
import android.view.View.OnTouchListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.fenchtose.tooltip.Tooltip
import com.fenchtose.tooltip.Tooltip.Tip
import com.fenchtose.tooltip.TooltipAnimation
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jakewharton.rxbinding2.view.RxView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.database.entity.Contact
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.networking.ErrorResponse
import com.meetsportal.meets.networking.ResultHandler
import com.meetsportal.meets.networking.exception.ApiException
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.post.DetailPostParcel
import com.meetsportal.meets.networking.post.FetchPostResponseItem
import com.meetsportal.meets.networking.profile.Badge
import com.meetsportal.meets.overridelayout.mention.MentionPerson
import com.meetsportal.meets.ui.activities.AuthenticationActivity
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.OtherProfileFragment
import com.meetsportal.meets.ui.fragments.socialfragment.ProfileFragment
import com.meetsportal.meets.utils.Constant.AcMentionOnHome
import com.meetsportal.meets.utils.Constant.KEY_FOREGROUND_ENABLED
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.onesignal.OneSignal
import com.pixplicity.sharp.OnSvgElementListener
import com.pixplicity.sharp.Sharp
import com.pixplicity.sharp.SharpDrawable
import com.pusher.client.channel.Channel
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import it.sephiroth.android.library.xtooltip.ClosePolicy
import jp.wasabeef.blurry.Blurry
import okhttp3.*
import org.json.JSONObject
import java.io.*
import java.lang.NullPointerException
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import android.content.pm.PackageInfo
import android.util.Base64.encode
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class Utils {
    companion object {

        val TAG = Utils::class.java.simpleName
        var iv: ByteArray? = null
        var httpClient: OkHttpClient? = null

        fun checkPermission(context: Context?, permission: String): Boolean {
            context?.let {
                var result = ContextCompat.checkSelfPermission(context, permission)
                return result == PackageManager.PERMISSION_GRANTED
            }
            return false
        }

        fun getAddressFromLatLong(
            context: Context,
            latitude: Double,
            longitude: Double
        ): AddressModel? {
            val geocoder = Geocoder(context, Locale.UK)
            var addresses: List<Address>? = null
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1)
            } catch (e: IOException) {
                Log.d(TAG, "getAddressFromLatLong: " + e.localizedMessage)
                return null
            }
            val model = AddressModel()
            return if (addresses != null && !addresses.isEmpty()) {
                val address = addresses[0]
                val addressLine = address.getAddressLine(0)
                model.address = addressLine
//                model.lat = latitude
//                model.lon = longitude
                model.setLocation(latitude,longitude)
                model.country_code = address.countryCode
                Log.d(TAG, "getAddressFromLatLong: $latitude $longitude ")
                Log.d(TAG, "getAddressFromLatLong: $address ")
                val locality = address.subLocality
                val street = address.thoroughfare
                val name = address.featureName
                if (name != null) {
                    model.name = name
                }else if (street != null) {
                    model.name = street
                } else if (locality != null) {
                    model.name = locality
                }
                model
            } else {
                null
            }
        }
        fun prettyCount(number: Number?): String {
            if(number!=null){
                val suffix = charArrayOf(' ', 'K', 'M', 'B', 'T', 'P', 'E')
                val numValue = number.toLong()
                val value = Math.floor(Math.log10(numValue.toDouble())).toInt()
                val base = value / 3
                return if(value >= 3 && base < suffix.size) {
                    DecimalFormat("#0.0").format(numValue / Math.pow(10.0, (base * 3).toDouble())) + suffix[base]
                } else {
                    DecimalFormat("#,##0").format(numValue)
                }
            }else{
                return "0"
            }
        }
        fun getCountryCode(myContext: Context): String? {
            val tm = myContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.networkCountryIso?.toUpperCase()
        }

        fun getTimeOfAfterDay(noOfDay : Int):String?{
            return Calendar.getInstance().apply {
                this.setTime(Date());
                this.add(Calendar.DAY_OF_YEAR,noOfDay)
                this.set(Calendar.HOUR_OF_DAY, 0);
                this.set(Calendar.MINUTE, 0);
                this.set(Calendar.SECOND, 0);
                this.set(Calendar.MILLISECOND, 0);

            }.time.formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", TimeZone.getTimeZone("UTC"))

        }
        fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap? {
            var width = image.width
            var height = image.height
            val bitmapRatio = width.toFloat() / height.toFloat()
            if (bitmapRatio > 1) {
                width = maxSize
                height = (width / bitmapRatio).toInt()
            } else {
                height = maxSize
                width = (height * bitmapRatio).toInt()
            }
            return Bitmap.createScaledBitmap(image, width, height, true)
        }

        fun getWindowWidth(acticty: Activity): Int {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                var windowMatrics = acticty.windowManager.currentWindowMetrics
                var insets = windowMatrics.windowInsets.getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars()
                )
                return windowMatrics.bounds.width() - insets.left - insets.right
            } else {
                var displayMatrics = DisplayMetrics()
                acticty.windowManager.defaultDisplay.getMetrics(displayMatrics)
                return displayMatrics.widthPixels
            }
        }

        @SuppressLint("MissingPermission")
        fun isNetworkConnected(context: Context?): Boolean {
            var data: Boolean=false
            try {
                val cm =
                    context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                data = cm.activeNetworkInfo != null
            } catch (e: Exception) {
                data = false
            }

            return data
        }

        fun isGpsOn(context: Context): Boolean {
            var locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        fun isValidEmail(target: CharSequence,context :Context?=null): Boolean {
            //val EMAIL_PATTERT = context.resources.getString(R.string.email_regex)
            val EMAIL_PATTERT = "EMAIL_PATTERT(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
            val pattern = Patterns.EMAIL_ADDRESS
            return !TextUtils.isEmpty(target) && pattern.matcher(target)
                .matches()
        }

        fun isValidPassword(target: CharSequence): Boolean {
            val pattern: Pattern
            val matcher: Matcher
            val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=\\S+$).{4,}$"
            pattern = Pattern.compile(PASSWORD_PATTERN)
            matcher = pattern.matcher(target)

            return matcher.matches()
        }

        fun checkReadWritePermission(context: Context): Boolean {
            return (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED)
        }

        fun stickImage(
            context: Context,
            view: ImageView,
            url: String?,
            requestOptions: RequestOptions?,
            showSimmer : Boolean = true
        ) {
            /*Log.i(TAG, " viewheight::: ${view.height}  ${view.width}")
            val shimmer =
                Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
                    .setDuration(1800) // how long the shimmering animation takes to do one full sweep
                    //.setBaseAlpha(0.7f) //the alpha of the underlying children
                    .setHighlightAlpha(0.6f) // the shimmer alpha amount
                    .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                    .setAutoStart(true)
                    .build()

// This is the placeholder for the imageView
            val shimmerDrawable = ShimmerDrawable().apply {
                setColorFilter(
                    ContextCompat.getColor(context, R.color.primaryDark),
                    PorterDuff.Mode.DST_OVER
                )
                setShimmer(shimmer)
            }

            requestOptions?.let {
                Glide.with(context)
                    .asBitmap()
                    .placeholder(shimmerDrawable)
                    .load(url).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(R.drawable.ic_person_placeholder)
                    .into(view)
            } ?: run {
                Glide.with(context)
                    .asBitmap()
                    .placeholder(shimmerDrawable)
                    .load(url).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(R.drawable.ic_person_placeholder)
                    .into(view)
            }*/
            view.loadImage(context, url,R.drawable.ic_person_placeholder,showSimmer)
        }
        fun ImageView.loadImage(
            context: Context,
            url: String?,
            placeholder:Int=R.drawable.ic_person_placeholder,
            showSimmer : Boolean = true,
            isItGif :Boolean = false,
            profileHolder:Int=R.drawable.ic_default_person
        ) {

            if(isItGif){
                Glide.with(context).asGif().load(url).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(placeholder).into(this)
            }else{
                if (showSimmer){
                    val shimmer = Shimmer.AlphaHighlightBuilder().setDuration(1800)
                        .setHighlightAlpha(0.6f).setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                        .setAutoStart(true).build()
                    val shimmerDrawable = ShimmerDrawable().apply {
                        setColorFilter(ContextCompat.getColor(context, R.color.primaryDark), PorterDuff.Mode.DST_OVER)
                        setShimmer(shimmer)
                    }
                    if(this is CircleImageView) {
                        Glide.with(context).asBitmap().placeholder(profileHolder).load(url)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE).error(profileHolder).into(this)
                    } else {
                        Glide.with(context).asBitmap().placeholder(shimmerDrawable).load(url).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .error(placeholder).into(this)
                    }
                }else{
                    Glide.with(context).asBitmap().placeholder(placeholder).load(url)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE).error(placeholder).into(this)
                }
            }
        }

        fun blurImage(context: Context, url: String?, view: ImageView) {
            /*Blurry.with(context)
                .radius(10)
                .sampling(8)
                .color(Color.argb(66, 255, 255, 0))
                .async()
                .onto(view);*/
            //Blurry.with(context).radius(25).sampling(2).onto(view)
            /*
             GlideUtils.loadImageBitmap(this, user.getAvatarUrl()) { bitmap ->
                 binding.imvAvatar.setImageBitmap(bitmap)
                 Blurry.with(this).radius(25).from(bitmap).into(binding.imvBackground)
             }*/


            Glide.with(context)
                .asBitmap()
                .load(url)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        Blurry.with(context).sampling(10).color(Color.argb(66, 0, 0, 0)).from(
                            resource
                        ).into(view)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })


        }

        fun stickOGSvg(
            context: Context?,
            path: String?,
            view: ImageView,
            parent: View? = null,
            marker: Marker? = null,
            markerOptions: MarkerOptions? = null
        ) {
            if(path == null){
                view.setImageResource(R.drawable.meet_logo)
                return
            }
            parent?.let {
                Log.i(TAG, "  stickOGSvgstick:::: 0 ")
                var requestBuilder = GlideApp.with(context!!)
                    .`as`(PictureDrawable::class.java)
                    .error(R.drawable.flag_saint_pierre)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(SvgSoftwareLayerSetter(context, path, parent, marker, markerOptions))
                    .load(Uri.parse(path))
                    .into(view)
            } ?: run {
                Log.i(TAG, "  stickOGSvgstick:::: 1")
                var requestBuilder = GlideApp.with(context!!)
                    .`as`(PictureDrawable::class.java)
                    .error(R.drawable.flag_saint_pierre)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(SvgSoftwareLayerSetter(context, path))
                    .load(Uri.parse(path))
                    .into(view)
            }

        }


        fun stickSvg(
            context: Context,
            view: ImageView,
            url: String?,
            color: Int,

            ) {
            if (httpClient == null) {
                httpClient = OkHttpClient.Builder()
                    .cache(Cache(context.cacheDir, 5 * 1024 * 1014))
                    .build()
            }
            url?.let {
                val request: Request = Request.Builder().url(it).build()
                httpClient?.newCall(request)?.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        //TODO("Not yet implemented")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val stream = response.body?.byteStream()
                        var svg = Sharp.loadInputStream(stream)
                        svg.setOnElementListener(object : OnSvgElementListener {
                            override fun onSvgStart(p0: Canvas, p1: RectF?) {}

                            override fun onSvgEnd(p0: Canvas, p1: RectF?) {}

                            override fun <T : Any?> onSvgElement(
                                p0: String?,
                                element: T,
                                p2: RectF?,
                                p3: Canvas,
                                p4: RectF?,
                                paint: Paint?
                            ): T {
                                paint?.setColor(color)
                                return element
                            }

                            override fun <T : Any?> onSvgElementDrawn(
                                p0: String?,
                                element: T,
                                p2: Canvas,
                                paint: Paint?
                            ) {
                            }

                        })
                        svg.getSharpPicture(Sharp.PictureCallback { picture ->
                            var drawable: Drawable = picture.drawable
                            SharpDrawable.prepareView(view)

                            view.setImageDrawable(drawable)
                            stream?.close()

                            // We don't want to use the same drawable, as we're specifying a custom size; therefore
                            // we call createDrawable() instead of getDrawable()
                            /*val iconSize: Int =
                                getResources().getDimensionPixelSize(R.dimen.icon_size)
                            mButton.setCompoundDrawables(
                                picture.createDrawable(mButton, iconSize),
                                null, null, null
                            )*/
                        })

                    }

                })
            }

        }

        fun stickSvgforBitmap(
            context: Context,
            view: ImageView,
            url: String?,
            color: Int,
            parentView: View,
            marker: Marker?,
            markerOptions: MarkerOptions?

        ) {
            Log.i(TAG, " marker svg:::: ${url}")
            marker?.let { it.isVisible = false }

            if (httpClient == null) {
                httpClient = OkHttpClient.Builder()
                    .cache(Cache(context.cacheDir, 5 * 1024 * 1014))
                    .build()
            }
            url?.let {
                val request: Request = Request.Builder().url(it).build()
                httpClient?.newCall(request)?.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, " StickSvgFailedException::: ${e.printStackTrace()}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val stream = response.body?.byteStream()
                        var svg = Sharp.loadInputStream(stream)
                        svg.setOnElementListener(object : OnSvgElementListener {
                            override fun onSvgStart(p0: Canvas, p1: RectF?) {}

                            override fun onSvgEnd(p0: Canvas, p1: RectF?) {}

                            override fun <T : Any?> onSvgElement(
                                p0: String?,
                                element: T,
                                p2: RectF?,
                                p3: Canvas,
                                p4: RectF?,
                                paint: Paint?
                            ): T {
                                paint?.setColor(color)
                                return element
                            }

                            override fun <T : Any?> onSvgElementDrawn(
                                p0: String?,
                                element: T,
                                p2: Canvas,
                                paint: Paint?
                            ) {
                            }

                        })
                        svg.getSharpPicture(Sharp.PictureCallback { picture ->
                            var drawable: Drawable = picture.drawable
                            SharpDrawable.prepareView(view)

                            view.setImageDrawable(drawable)
                            stream?.close()

                            val displayMetrics = DisplayMetrics()
                            (context as Activity).windowManager.defaultDisplay.getMetrics(
                                displayMetrics
                            )
                            parentView.setLayoutParams(
                                ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                            )
                            parentView.measure(
                                displayMetrics.widthPixels,
                                displayMetrics.heightPixels
                            )
                            parentView.layout(
                                1,
                                1,
                                displayMetrics.widthPixels,
                                displayMetrics.heightPixels
                            )

                            parentView.buildDrawingCache()
                            Log.i(
                                Utils.TAG,
                                " viewMeasure:::: ${parentView.measuredWidth} ${parentView.measuredHeight}"
                            )
                            //stickImage(myContext!!,imageView,image, RequestOptions().apply{override(80,80)})
                            val bitmap = Bitmap.createBitmap(
                                if (parentView.measuredWidth <= 0) 1 else parentView.measuredWidth,
                                if (parentView.measuredHeight <= 0) 1 else parentView.measuredHeight,
                                Bitmap.Config.ARGB_8888
                            )
                            val canvas = Canvas(bitmap)
                            parentView.draw(canvas)
                            marker?.let {
                                it.setIcon(
                                    BitmapDescriptorFactory.fromBitmap(bitmap)
                                )
                            }

                            markerOptions?.let {
                                it.icon(
                                    BitmapDescriptorFactory.fromBitmap(bitmap)
                                )
                            }
                            marker?.let { it.isVisible = true }
                            //markerOptions?.let { it.alpha = 0f }


                            // We don't want to use the same drawable, as we're specifying a custom size; therefore
                            // we call createDrawable() instead of getDrawable()
                            /*val iconSize: Int =
                                getResources().getDimensionPixelSize(R.dimen.icon_size)
                            mButton.setCompoundDrawables(
                                picture.createDrawable(mButton, iconSize),
                                null, null, null
                            )*/
                        })

                    }

                })
            }

        }

        var contactList = ArrayList<Contact>()

        @SuppressLint("Range")
        fun getContactList(context: Context, subject : PublishSubject<Contact>? = null): ArrayList<Contact> {
            //Log.i("TAG", " getContactList:: contactListSize ${contactList.size}")
           // Log.i(ContactListFragment.TAG," fetchingContact::  1")
            if (contactList.size > 0) {
                return contactList
            }

           // Log.i(ContactListFragment.TAG," fetchingContact::  2")

            var contactList = ArrayList<Contact>();
            val cr: ContentResolver = context.getContentResolver()
            val cur: Cursor? = cr.query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null
            )
            if ((if (cur != null) cur.getCount() else 0) > 0) {
                while (cur != null && cur.moveToNext()) {
                    val id: String = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID)
                    )
                    val name: String = cur.getString(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME
                        )
                    )
                    if (cur.getInt(
                            cur.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER
                            )
                        ) > 0
                    ) {
                        val pCur: Cursor? = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        while (pCur?.moveToNext()!!) {
                            val phoneNo: String = pCur.getString(
                                pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                )
                            )
                            //Log.i("Utils", "Name: $name")
                            //Log.i("Utils", "Phone Number: $phoneNo")
                            //Log.i("Utils", " Image:: ${retrieveContactPhoto(context, phoneNo)}")

                            var contact = Contact(
                                id,
                                name,
                                phoneNo,
                                retrieveContactPhoto(context, phoneNo)
                            )
                            (context as Activity).runOnUiThread{
                                subject?.onNext(contact)
                            }
                            contactList.add(
                                contact
                            )
                        }
                        pCur.close()
                    }
                }
            }
            if (cur != null) {
                cur.close()
            }
           // this.contactList = contactList
            return contactList
        }

        private fun retrieveContactPhoto(context: Context, number: String?): Bitmap? {
            val contentResolver = context.contentResolver
            var contactId: String? = null
            val uri: Uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            val projection =
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID)
            val cursor = contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    contactId =
                        cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID))
                }
                cursor.close()
            }
            var photo = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.ic_add
            )
            try {
                if (contactId != null) {
                    val inputStream: InputStream? =
                        ContactsContract.Contacts.openContactPhotoInputStream(
                            context.contentResolver,
                            ContentUris.withAppendedId(
                                ContactsContract.Contacts.CONTENT_URI,
                                contactId.toLong()
                            )
                        )
                    inputStream?.let {
                        photo = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()

                    }
                    //assert(inputStream != null)

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return photo
        }

        fun createCustomMarker(
            context: Context,
            @DrawableRes resource: Int,
            _name: String
        ): Bitmap {
            val marker =
                (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                    .inflate(R.layout.custom_marker_layout, null)
            val markerImage = marker.findViewById<View>(R.id.user_dp) as ImageView
            markerImage.setImageResource(resource)

            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            marker.layoutParams = ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT)
            marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
            marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
            marker.buildDrawingCache()
            val bitmap = Bitmap.createBitmap(
                marker.measuredWidth,
                marker.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            marker.draw(canvas)
            return bitmap
        }

        fun initializeMapUserView(context: Context, image: Bitmap?, selected: Boolean, timeStamp: String?, boosted: Boolean): View {
            if(boosted){
                val view = (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.card_pin_boosted, null)

                var imageView = view.findViewById<ImageView>(R.id.civ_image)
                val time = view.findViewById<TextView>(R.id.time)
                time.text = timeStamp
                if (selected) {
                    val bg = view.findViewById<ImageView>(R.id.bg)
                    bg.setColorFilter(ContextCompat.getColor(context, R.color.selectedUser))
                }

                val displayMetrics = DisplayMetrics()
                (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
                view.setLayoutParams(ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
                view.layout(1, 1, displayMetrics.widthPixels, displayMetrics.heightPixels)

                view.buildDrawingCache()
                return view
            }
            val view =
                (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    R.layout.card_pin,
                    null
                )
            var imageView = view.findViewById<ImageView>(R.id.civ_image)
            val time = view.findViewById<TextView>(R.id.time)
            time.text = timeStamp

            if (selected) {
                val bg = view.findViewById<ImageView>(R.id.bg)
                bg.setColorFilter(ContextCompat.getColor(context, R.color.selectedUser))
            }

            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            view.setLayoutParams(
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
            view.layout(1, 1, displayMetrics.widthPixels, displayMetrics.heightPixels)

            view.buildDrawingCache()
            return view
        }

        fun getBitmapFromView(view: View, context: Context, image: Bitmap?): Bitmap {
            val bitmap = Bitmap.createBitmap(
                view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        }

        fun createMapUserMarker(
            context: Context,
            image: Bitmap?,
            selected: Boolean,
            timestamp: String?
        ): Bitmap? {

            Log.i(TAG, " checkingnumberTime::: $selected $image")

            var view =
                (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    R.layout.card_pin,
                    null
                )
            var imageView = view.findViewById<ImageView>(R.id.civ_image)
            var time = view.findViewById<TextView>(R.id.time)
            time.text = timestamp
            Log.i(TAG, " selected :: $selected")


            //imageView.setImageResource(image)

            if (selected) {
                var bg = view.findViewById<ImageView>(R.id.bg)
                bg.setColorFilter(ContextCompat.getColor(context, R.color.selectedUser))
                //bg.setImageResource(R.drawable.ic_selected_meetup_pin)
            }

            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            view.setLayoutParams(
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
            view.layout(1, 1, displayMetrics.widthPixels, displayMetrics.heightPixels)

            view.buildDrawingCache()
            Log.i(TAG, " viewMeasure:::: ${view.measuredWidth} ${view.measuredHeight}")
            imageView.setImageBitmap(image)
            //stickImage(context, imageView, image, RequestOptions().apply { override(80, 80) })
            val bitmap = Bitmap.createBitmap(
                if (view.measuredWidth <= 0) 1 else view.measuredWidth,
                if (view.measuredHeight <= 0) 1 else view.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        }

        fun createClusterIcon(context: Context, image: String?, size: Int): Bitmap? {

            var view =
                (context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
                    R.layout.card_cluster_pin,
                    null
                )
            var count = view.findViewById<TextView>(R.id.count)
            Log.i(TAG, " count :: $size")
            count.text = size.toString()

            //imageView.setImageResource(image)


            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            view.setLayoutParams(
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
            view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
            view.buildDrawingCache()
            val bitmap = Bitmap.createBitmap(
                view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap
        }


        fun bitmapDescriptorFromVector(
            context: Context,
            @DrawableRes vectorDrawableResourceId: Int
        ): BitmapDescriptor? {
            val background =
                ContextCompat.getDrawable(context, vectorDrawableResourceId)
            background?.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
            val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)

            val bitmap = Bitmap.createBitmap(
                background?.intrinsicWidth!!,
                background.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            background.draw(canvas)
            //vectorDrawable?.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }

        fun getBitMapfromLayout(view: View, context: Context): BitmapDescriptor {
            Log.i(TAG, " getBitMapfromLayout::: 0")

            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            view.setLayoutParams(
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
            view.layout(1, 1, displayMetrics.widthPixels, displayMetrics.heightPixels)

            view.buildDrawingCache()
            Log.i(Utils.TAG, " viewMeasure:::: ${view.measuredWidth} ${view.measuredHeight}")
            // stickImage(myContext!!,imageView,image, RequestOptions().apply{override(80,80)})
            val bitmap = Bitmap.createBitmap(
                if (view.measuredWidth <= 0) 1 else view.measuredWidth,
                if (view.measuredHeight <= 0) 1 else view.measuredHeight,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            view.draw(canvas)
            Log.i(TAG, " getBitMapfromLayout::: 1")
            return BitmapDescriptorFactory.fromBitmap(bitmap)

        }

        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val capabilities =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    } else {
                        TODO("VERSION.SDK_INT < M")
                    }
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                        return true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                        return true
                    }
                }
            }
            return false
        }

        fun uriToBitmap(selectedFileUri: Uri?, context: Context?): Bitmap? {
            try {
                val parcelFileDescriptor: ParcelFileDescriptor =
                    context?.contentResolver?.openFileDescriptor(selectedFileUri!!, "r")!!
                val fileDescriptor: FileDescriptor = parcelFileDescriptor.fileDescriptor
                val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                parcelFileDescriptor.close()
                return rotateImageIfRequired(context, image, selectedFileUri!!)

                //return image
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }


        private fun rotateImageIfRequired(
            context: Context,
            img: Bitmap,
            selectedImage: Uri
        ): Bitmap? {
            var result = img
            try {
                Log.i(TAG, " rotationTakePlace::: ")
                val input = context.contentResolver.openInputStream(selectedImage)
                val ei: ExifInterface
                if (Build.VERSION.SDK_INT > 23)
                    ei = ExifInterface(input!!)
                else
                    ei = ExifInterface(selectedImage.path!!)
                val orientation: Int =
                    ei.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )

                Log.i(TAG, " orientation:: $orientation")
                var matrix = Matrix()
                return when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> {
                        Log.i(TAG, " orientation:: 90 degree")
                        matrix.postRotate(130f)
                        rotateImage(img, 90)
                    }
                    ExifInterface.ORIENTATION_ROTATE_180 -> {
                        Log.i(TAG, " orientation:: 180 degree")
                        matrix.postRotate(180f)
                        rotateImage(img, 180)
                    }
                    ExifInterface.ORIENTATION_ROTATE_270 -> {
                        Log.i(TAG, " orientation:: 270 degree")
                        matrix.postRotate(270f)
                        rotateImage(img, 270)
                    }
                    else -> img
                }
                /*result = Bitmap.createBitmap(img, 0, 0,
                    img.getWidth(), img.getHeight(), matrix,
                    true)*/

                //return result
            } catch (e: Exception) {
                Log.i(TAG, " Exceeption Came:: $e")
                return result
            }
        }

        fun getCreatedAt(createdIso: String?): String? {
            if(createdIso==null) return ""
            var diffrence = (Date().time - createdIso?.toDate()?.time!!) / 1000
            if (diffrence < 60) {
                return "Just now"
            } else if (diffrence < (60 * 60)) {
                return "${(diffrence / 60).toString().plus("  min ago")}"
            } else if (diffrence < (60 * 60 * 24)) {
                return "${(diffrence / 60 / 60).toString().plus("  hour ago")}"
            } else {
                return createdIso?.toDate()?.formatTo("dd MMM yyyy")
            }
        }

        fun getlastSeenFromFirebaseTimeStamp(timestamp: Long?): String {
            Log.i("getlastSeTimeStamp", " timestamp:: $timestamp")
            var diffrence = (Date().time - Date(timestamp!!).time) / 1000
            if (diffrence < 60) {
                return "Just now"
            } else if (diffrence < (60 * 60)) {
                return "${(diffrence / 60).toString().plus("  min ago")}"
            } else if (diffrence < (60 * 60 * 24)) {
                return "${(diffrence / 60 / 60).toString().plus("  hour ago")}"
            } else {
                return Date(timestamp)?.formatTo("dd MMM yyyy")
            }
        }

        fun fireTimeStamptoAgo(date: Date?): String? {
            Log.i(TAG, " date ::: ${date}")
            date?.let {
                var cDate = Calendar.getInstance().apply { time = it }
                var now = Calendar.getInstance()

                if (cDate.get(Calendar.DATE) == now.get(Calendar.DATE)) {
                    var diffrence = (Date().time - date.time) / 1000
                    if (diffrence < 60) {
                        //return "${diffrence.toString().plus(" sec ago")}"
                        return "just now"
                    } else if (diffrence < (60 * 60)) {
                        return "${(diffrence / 60).toString().plus("  min ago")}"
                    } else if (diffrence < (60 * 60 * 24)) {
                        //return "${(diffrence / 60 / 60).toString().plus("  hour ago")}"
                        return date.formatTo("h:mm a")
                    } else {
                        return date?.formatTo("dd MMM yyyy")
                    }
                } else {
                    return date.formatTo("h:mm a-dd-MMM")
                }
            }
            return "just now"

        }
        fun timeDifference(end: Date?): String? {
            end?.let { it1 ->
                val seconds = (it1.time - Date().time) / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                Log.d(TAG, "timeDifference:end date::: $seconds")
                if(hours == 0L) {
                    if(minutes == 0L) return "less than a minute remaining"
                    else return "$minutes minutes remaining"
                }
                return "$hours:${minutes%60}  remaining"
            }
            return "1 minute remaining"
        }
        fun timeStampToReadable(date: Date?): String? {
            Log.i(TAG, " date ::: $date::: ")
            date?.let {
                val cDate = Calendar.getInstance().apply { time = it }
                val now = Calendar.getInstance()

                val diffrence = (Date().time - date.time) / 1000
                if (cDate.get(Calendar.DATE) == now.get(Calendar.DATE)) {
                    if (diffrence < 60) {
                        //return "${diffrence.toString().plus(" sec ago")}"
                        return "just now"
                    } else if (diffrence < (60 * 60)) {
                        return (diffrence / 60).toString().plus("  min ago")
                    } else if (diffrence < (60 * 60 * 24)) {
                        return date.formatTo("h:mm a")
                    } else {
                        return date.formatTo("dd/MM/yy")
                    }
                } else if(diffrence < (60 * 60 * 48)) {
                    return "Yesterday"
                } else {
                    return date.formatTo("dd/MM/yy")
                }
            }
            return "just now"

        }

        fun timeStampInChat(date: Date?): String? {
            date?.let {
                Log.d(TAG, "timeStampInChat: DateUtils.isToday(it.time) ${DateUtils.isToday(it.time)}")
                if (DateUtils.isToday(it.time)) {
                        return "Today"
                } else if(DateUtils.isToday(it.time + DateUtils.DAY_IN_MILLIS)) {
                    return "Yesterday"
                } else {
                    return date.formatTo("dd MMMM yyyy")
                }
            }
            return null

        }

        fun getLastSeen(updateIso: String?): String {
            var diffrence = (Date().time - updateIso?.toDate()?.time!!) / 1000
            if (diffrence < 60) {
                return "${diffrence.toString().plus("s ago")}"
            } else if (diffrence < (60 * 60)) {
                return "${(diffrence / 60).toString().plus("m ago")}"
            } else if (diffrence < (60 * 60 * 24)) {
                return "${(diffrence / 60 / 60).toString().plus("d ago")}"
            } else {
                return "${(diffrence / 60 / 60 / 24).toString().plus("d ago")}"
                //return updateIso?.toDate().formatTo("dd MMM yyyy")
            }
        }

        fun setMentionInCaption(myContext : Activity,body:String?,mention:List<MentionPerson>?,postId: String? = null): Spannable {

            Log.i(TAG," setMentionInCaption::: body:: $body")
            val textColor = ResourcesCompat.getColor(
                myContext.resources, R.color.mention, null
            )
            val spannable: Spannable = SpannableString(body)
            for (person in mention?: listOf()) {

                Log.i(TAG," setMentionInCaption::: person:: ${person.alt_id}")

                var startIndex: Int? = body?.indexOf("@${person.alt_id}")
                Log.i(TAG," setMentionInCaption::: startIndex:: ${startIndex}")
                while (startIndex != null && startIndex != -1 && startIndex >= 0  ) {
                    Log.i(TAG," checkingMentionIndex:: $startIndex")
                    val endIndex = startIndex.plus("@${person.alt_id}".length)
                    spannable.setSpan(
                        ForegroundColorSpan(textColor),
                        startIndex?:0,
                        endIndex?:0,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    val clickableSpan: ClickableSpan = object : ClickableSpan() {
                        override fun onClick(textView: View) {
                            MyApplication.putTrackMP(AcMentionOnHome, JSONObject(mapOf("postId" to postId)))
                            if(person.id?.equals(MyApplication.SID) == true){
                                Navigation.addFragment(
                                    myContext,
                                    ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
                                )
                            }else{
                                var baseFragment: BaseFragment = OtherProfileFragment.getInstance(person.id)
                               /* baseFragment = Navigation.setFragmentData(
                                    baseFragment,
                                    OtherProfileFragment.OTHER_USER_ID,
                                    person.id
                                )*/
                                Navigation.addFragment(
                                    myContext, baseFragment,
                                    Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
                                )
                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.setColor(ContextCompat.getColor(myContext,R.color.mention))
                            ds.isUnderlineText = false
                        }
                    }
                    spannable.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    startIndex = body?.indexOf("@${person.alt_id}", startIndex + 1)
                }
            }


            Log.i(TAG," setMentionInCaption::: spannable:: $spannable")
            return spannable

        }


        fun getLocationTrackingPref(context: Context): Boolean =
            context.getSharedPreferences(
                "location", Context.MODE_PRIVATE
            )
                .getBoolean(KEY_FOREGROUND_ENABLED, false)

        /**
         * Stores the location updates state in SharedPreferences.
         * @param requestingLocationUpdates The location updates state.
         */
        fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
            context.getSharedPreferences(
                "location",
                Context.MODE_PRIVATE
            ).edit {
                putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
            }


        fun handleException(acticty: Activity?, e: ErrorResponse?) {
            Log.i(TAG, " ErrorResponse:::  $e")
            try {
                if (e?.errorCode == -1) {
                    PreferencesManager.deleteSavedData()
                    OneSignal.disablePush(true)
                    FirebaseAuth.getInstance().signOut()
                    acticty?.launchActivity<AuthenticationActivity>(true) {}
                    Toast.makeText(
                        acticty,
                        "Session Expired, Please Log in again...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.i(TAG, " Exception came::  $e ")
            }
        }

        inline fun <reified T> handleException(it:Throwable, livedata: MutableLiveData<ResultHandler<T>>, apiName:String){
           Log.i(TAG," handleExceptionhandleExceptionhandleException:::: ${it.printStackTrace()}")

            if (it is ApiException) {
                Log.i(TAG," handleExceptionhandleExceptionhandleException:::: 1 ${it.errorResponse?.errorCode}")
                Log.i(TAG," handleExceptionhandleExceptionhandleException:::: 2 ${it.errorResponse}")
                if (it.errorResponse != null && it.errorResponse?.errorCode == 400) {
                    FirebaseCrashlytics.getInstance().log("$apiName 400 ${it.errorResponse?.errorCode} ${MyApplication.SID}")
                    FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                    livedata.value = ResultHandler.Failure(
                        it.errorResponse?.errorCode.toString(),
                        it.errorResponse?.message,
                        it
                    )
                }else if(it.errorResponse != null && (it.errorResponse?.errorCode == 401 || it.errorResponse?.errorCode == 402 ||it.errorResponse?.errorCode == 403 || it.errorResponse?.errorCode == 404 )){
                    livedata.value = ResultHandler.Failure(
                            it.errorResponse?.errorCode.toString(),
                            it.errorResponse?.message,
                            it
                                                          )
                }else if(false) {
                    //handle Session Expired
                }
                else if(it.errorResponse != null && it.errorResponse?.errorCode == 504){
                    Log.i(TAG," handleExceptionhandleExceptionhandleException:::: 3")
                    Toast.makeText(MyApplication.getContext(),"No internet connection!!!",Toast.LENGTH_SHORT).show()
                    livedata.value = ResultHandler.Failure(it.errorResponse?.errorCode.toString(), it.errorResponse?.message, it)
                }
                else if(it.errorResponse != null && it.errorResponse?.errorCode == Constant.NO_INTERNET){
                    Toast.makeText(MyApplication.getContext(),it.errorResponse?.message,Toast.LENGTH_SHORT).show()
                    livedata.value = ResultHandler.Failure(it.errorResponse?.code, it.errorResponse?.message, it)
                }
                else {
                    FirebaseCrashlytics.getInstance().log("$apiName ${it.errorResponse?.errorCode} ${MyApplication.SID}")
                    FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                    livedata.value = getUnknownError(it)
                    Log.e(TAG, "Something went Wrong:: apiName ${it.printStackTrace()}")
                }
            } else {
                FirebaseCrashlytics.getInstance().log("$apiName ${it.localizedMessage} ${MyApplication.SID}")
                FirebaseCrashlytics.getInstance().recordException(RuntimeException(Throwable(it)))
                livedata.value = getUnknownError(it)

            }

        }

        fun getUnknownError(it: Throwable): ResultHandler.Failure {
            return ResultHandler.Failure(
                "500",
                "Something went Wrong",
                it
            )
        }

        fun cancelNotification(ctx: Context, strId: String?) {
            val notifyId=strId?.toCharArray()?.sumOf { it.toInt() }
            val ns = Context.NOTIFICATION_SERVICE
            val nMgr = ctx.getSystemService(ns) as NotificationManager
            Log.d(TAG, "remoteNotificationReceived: NotifyId::: Utils $notifyId")
            nMgr.cancel(notifyId?:0)
        }

        fun onClick(view: View, timeInMilli: Long, function: () -> Unit): Disposable {
            return RxView.clicks(view).throttleFirst(timeInMilli, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    function.invoke()
                }

        }

        fun hideSoftKeyBoard(context: Context, view: View) {
            val imm: InputMethodManager? =
                context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }


        fun getDetailPostParcel(post: FetchPostResponseItem?): DetailPostParcel {
            return DetailPostParcel(
                _id = post?._id,
                media = post?.media,
                stats = post?.stats,
                body = post?.body,
                user_meta = post?.user_meta,
                createdAt = post?.createdAt,
                type = post?.type

            )
        }
        fun getCompressFormat(extension: String): Bitmap.CompressFormat {
            return when {
                extension.contains("png", ignoreCase = true) -> Bitmap.CompressFormat.PNG
                extension.contains("webp", ignoreCase = true) -> Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }
        }

        fun getChannel(channel: String?): Channel {
            Log.i(TAG, " channelNAme:: $channel")
            return MyApplication.pusher.subscribe(channel)
        }

        fun getTouchlistener(mDetector: GestureDetector): OnTouchListener {
            val touchListener =
                OnTouchListener { v, event -> // pass the events to the gesture detector
                    // a return value of true means the detector is handling it
                    // a return value of false means the detector didn't
                    // recognize the event
                    mDetector.onTouchEvent(event)
                }
            return touchListener
        }

        fun fadeVisibilityGone(view: View, duration: Long) {
            if (view.visibility == View.VISIBLE)
                view.animate()
                    .alpha(0.0f)
                    .setDuration(duration)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            view.visibility = View.GONE
                        }
                    })
        }

        fun fadeVisibilityVisible(view: View, duration: Long) {
            if (view.visibility == View.GONE)
                view.animate()
                    .alpha(1.0f)
                    .setDuration(duration)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            super.onAnimationStart(animation)
                            view.visibility = View.VISIBLE
                        }
                    })
        }

        fun popUpVisibilityGone(view: View, duration: Long) {
            Log.d(TAG, " popUpVisibility::: Gone ${view.visibility}")
            view.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(duration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        view.visibility = View.GONE
                        Log.i(TAG, " anime::: 1onAnimationEnd::: ")
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        super.onAnimationCancel(animation)
                        Log.i(TAG, " anime::: 1onAnimationCancel::: ")
                    }

                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        Log.i(TAG, " anime::: 1onAnimationStart::: ")
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                        super.onAnimationRepeat(animation)
                        Log.i(TAG, " anime::: 1onAnimationRepeat::: ")
                    }

                    override fun onAnimationPause(animation: Animator?) {
                        super.onAnimationPause(animation)
                        Log.i(TAG, " anime::: 1onAnimationPause::: ")
                    }

                    override fun onAnimationResume(animation: Animator?) {
                        super.onAnimationResume(animation)
                        Log.i(TAG, " anime::: 1onAnimationResume::: ")
                    }
                })
        }

        fun popUpVisibilityVisible(view: View, duration: Long) {
            Log.d(TAG, " popUpVisibility::: Visible ${view.visibility}")
            view.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(duration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        view.visibility = View.VISIBLE
                        Log.i(TAG, " anime::: 2onAnimationStart::: ")
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        super.onAnimationCancel(animation)
                        Log.i(TAG, " anime::: 2onAnimationCancel::: ")
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        Log.i(TAG, " anime::: 2onAnimationEnd::: ")
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                        super.onAnimationRepeat(animation)
                        Log.i(TAG, " anime::: 2onAnimationRepeat::: ")
                    }

                    override fun onAnimationPause(animation: Animator?) {
                        super.onAnimationPause(animation)
                        Log.i(TAG, " anime::: 2onAnimationPause::: ")
                    }

                    override fun onAnimationResume(animation: Animator?) {
                        super.onAnimationResume(animation)
                        Log.i(TAG, " anime::: 2onAnimationResume::: ")
                    }
                })
        }
        fun popUpAnim(view: View, myContext: Context) {
            val animation: Animation = AnimationUtils.loadAnimation(myContext, R.anim.breath)
            animation.setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationStart(animation: Animation?) {
                    Log.d(TAG, "onAnimationStart: ")
                }

                override fun onAnimationEnd(animation: Animation?) {
                    Log.d(TAG, "onAnimationEnd: ")
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }

            })
            view.startAnimation(animation)
        }

        fun getToastBacGround(activity: Activity): GradientDrawable {
            var shape = GradientDrawable()
            shape.cornerRadii = floatArrayOf(50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f)

            shape.setColor(ContextCompat.getColor(activity, R.color.blacktextColor))
            return shape
        }

        /*fun showGuide(acticty: Activity,target:View,title:String,description:String){
            GuideView.Builder(acticty)
                .setTitle(title)
                .setContentText(description)
                .setTargetView(target)
                .setDismissType(DismissType.outside)
                .build()
                .show()
        }*/

        fun showTooltip(context:Activity,target:View,root:ViewGroup,position : Int = Tooltip.TOP,title:String? = null,desc:String?=null,dismissd:()->Unit){
            Log.i(TAG," showingToolsTip")
            try{
                val content: View = context.getLayoutInflater().inflate(R.layout.card_tooltip, null)

                var color = 0
                if(position == Tooltip.TOP || position == Tooltip.BOTTOM){
                    color = ContextCompat.getColor(context,R.color.ttpmiddle)
                }
                else if(position == Tooltip.RIGHT){
                    color = ContextCompat.getColor(context,R.color.ttpstart)
                }else{
                    color = ContextCompat.getColor(context,R.color.ttpend)
                }

                content.findViewById<TextView>(R.id.title).apply{
                    title?.let {
                        text = it
                    }?:run{
                        visibility = View.GONE
                    }
                }

                content.findViewById<TextView>(R.id.desc).apply{
                    desc?.let {
                        text = it
                    }?:run{
                        visibility = View.GONE
                    }
                }

                val customTooltip = Tooltip.Builder(context)
                    .anchor(target, position)
                    .animate(TooltipAnimation(TooltipAnimation.SCALE_AND_FADE, 100))
                    .autoAdjust(true)
                    .withPadding(20)
                    .content(content)
                    .cancelable(true)
                    .checkForPreDraw(true)
                    .withTip(Tip(30, 30, color, 10))
                    .into(root)
                    .withListener(Tooltip.Listener {
                        dismissd()
                    })
                    .debug(true)
                    .show()

                content.setOnClickListener{
                    customTooltip.dismiss(true);
                }
            } catch (e:Exception){
                Log.e(TAG, "showTooltip: ${e.message}" )
                FirebaseCrashlytics.getInstance().recordException(RuntimeException(e))
            } catch (e:NullPointerException){
                Log.e(TAG, "showTooltip: ${e.message}" )
                FirebaseCrashlytics.getInstance().recordException(RuntimeException(e))
            }

        }
        fun showToolTips(context:Activity,target:View,root:ViewGroup,position : Int = Tooltip.TOP,desc:String?=null,id:String?,dismissd:()->Unit){
            Log.i(TAG," showingToolsTip")
            val content: View = context.getLayoutInflater().inflate(R.layout.card_tooltip, null)

            var color = 0
            if(position == Tooltip.TOP || position == Tooltip.BOTTOM){
                color = ContextCompat.getColor(context,R.color.ttpmiddle)
            }
            else if(position == Tooltip.RIGHT){
                color = ContextCompat.getColor(context,R.color.ttpstart)
            }else{
                color = ContextCompat.getColor(context,R.color.ttpend)
            }
            content.findViewById<ImageView>(R.id.iv_ttp).apply {
                visibility=View.GONE
            }
            content.findViewById<TextView>(R.id.title).apply{
                visibility=View.GONE
            }

            content.findViewById<TextView>(R.id.desc).apply{
                desc?.let {
                    text = it
                }?:run{
                    visibility = View.GONE
                }
            }

            val customTooltip = Tooltip.Builder(context)
                .anchor(target, position)
                .animate(TooltipAnimation(TooltipAnimation.SCALE_AND_FADE, 100))
                .autoAdjust(true)
                .withPadding(20)
                .content(content)
                .cancelable(true)
                .checkForPreDraw(true)
                .withTip(Tip(30, 30, color, 10))
                .into(root)
                .withListener {
                    dismissd()
                }.debug(true)
                .show()

            content.setOnClickListener{
                customTooltip.dismiss(true);
            }
            if(id != null) {
                var count: Int = PreferencesManager.get<Int?>(id)?:0
                count += 1
                if(count>2){
                   customTooltip.dismiss()
                }else{
                    PreferencesManager.put<Int>(count,id)
                }

            }
        }

        fun showToolTipBadge(context:Activity,target:View,root:ViewGroup,title:String? = null,desc:String,dismissd:()->Unit){

        }

        fun <T>castData(data :Any):T{
            return (data as T)
        }

        fun getRoundedColorBackground(activity:Activity, color:Int,corner:Float = 70f,enbleDash:Boolean = false): GradientDrawable {
            var shape = GradientDrawable()
            if(enbleDash)
            shape.setStroke(dpToPx(1f,activity.resources),Color.WHITE, dpToPx(3f,activity.resources).toFloat(),dpToPx(3f,activity.resources).toFloat())
            shape.cornerRadii = floatArrayOf(corner, corner, corner, corner, corner, corner, corner, corner)
            if(isColorResource(activity,color)){
                shape.setColor(ContextCompat.getColor(activity, color))
            }else{
                shape.setColor(color)
            }
            return shape
        }

        fun isColorResource(activity: Activity,value: Int): Boolean {
            return try {
                ResourcesCompat.getColor(activity.resources, value, null)
                true
            } catch (e: NotFoundException) {
                false
            }
        }



        fun dpToPx(dp: Float, resources: Resources): Int {
            val px =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
            return px.toInt()
        }


        fun moveFile(from: File) {
            val to = File(Environment.getExternalStorageDirectory().toString() + "/Shisheo/Media/Videos/Sent")

            val nanoStart = System.nanoTime()
            val toPath=to.path
            val fromPath=from.path
            val name=from.name

            Log.d(TAG, "moveFile:path::: $fromPath , $name , $toPath")
            var inputStream: InputStream? = null
            var out: OutputStream? = null
            try {
                //create output directory if it doesn't exist
                val dir = File(toPath)
                if(!dir.exists()) {
                    dir.mkdirs()
                }
                inputStream = FileInputStream(fromPath )
                out = FileOutputStream("$toPath/$name")
                val buffer = ByteArray(1024)
                var read: Int
                while(inputStream.read(buffer).also { read = it } != -1) {
                    out.write(buffer, 0, read)
                }
                inputStream.close()
                inputStream = null

                // write the output file
                out.flush()
                out.close()
                out = null

                // delete the original file
                File(fromPath + name).delete()
                val nanoEnd = System.nanoTime()
                Log.d(TAG, "moveFile:time taken: ${nanoEnd-nanoStart}")
            } catch(fnfe1: FileNotFoundException) {
                Log.e("tag", "${fnfe1.printStackTrace()}")
            } catch(e: java.lang.Exception) {
                Log.e("tag", e.message!!)
            }
        }
        fun customizeBadgeBackground(myContext : Activity, level : Badge,radius: Float=30f):LayerDrawable{

            val layerDrawable : LayerDrawable = ContextCompat.getDrawable(myContext,R.drawable.badge_bg_gradient) as LayerDrawable
            val itemSolid = layerDrawable.findDrawableByLayerId(R.id.itemSolid) as GradientDrawable
            itemSolid.mutate()
            itemSolid.colors = intArrayOf(
                ContextCompat.getColor(myContext,level.light),
                ContextCompat.getColor(myContext,level.dark)
            )//centre ,  start , end
            itemSolid.cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
            return layerDrawable
        }
        fun getBadge(badge:String?): Badge {
//            Log.d("TAG", "getBadge:badge:String $badge")
            badge?.let {
                return Constant.badgeList.firstOrNull { badge.equals(it.key, ignoreCase = true) }?: Constant.badgeList[0]
            }
            return Constant.badgeList[0]
        }

        fun gradientFromColor(colors : IntArray, orientation : GradientDrawable.Orientation = GradientDrawable.Orientation.TL_BR,radius : Float = 0f):GradientDrawable{
            return getGradient(orientation,colors,radius)
        }

        fun getGradient(orientation : GradientDrawable.Orientation,colors : IntArray,radius : Float = 0f):GradientDrawable{
            Log.i(TAG," makingGradient::: ")
            val gd = GradientDrawable(
                //GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(-0x9e9d9f, -0xececed)
                orientation, colors
            )
            //gd.cornerRadius = Utils.dpToPx(radius, activity.resources).toFloat()
            return gd
        }

        fun getDayDiff(date1 : Date?, date2 : Date?):Long?{
            date2?.let{
                date1?.time?.minus(date2.time)?.let {
                     return TimeUnit.DAYS.convert(it,TimeUnit.MILLISECONDS)
                }?:run{return null}
            }?:run{return null}
        }
        fun getHourDiff(start: Date?, end: Date?): Long {
            start?.let{
                end?.let{
                    val seconds = (start.time - end.time) / 1000
                    val minutes = seconds / 60
                    var hours = minutes / 60
                    return hours
                }?:run{
                    return 0L
                }
            }?:run{
                return 0L
            }
        }
        fun removeTime(date: Date?): Date? {
            val cal = Calendar.getInstance()
            date?.let{
                cal.time = it
                cal[Calendar.HOUR_OF_DAY] = 0
                cal[Calendar.MINUTE] = 0
                cal[Calendar.SECOND] = 0
                cal[Calendar.MILLISECOND] = 0
            }
            return cal.time
        }

        fun allPermissionsForCamera(context : Context, arrayPermission: Array<String>) = arrayPermission.all {
            ContextCompat.checkSelfPermission(
                context, it) == PackageManager.PERMISSION_GRANTED
        }

        fun getJsonDataFromAsset(context: Context, fileName: String): String? {
            val jsonString: String
            try {
                jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (ioException: IOException) {
                ioException.printStackTrace()
                return null
            }
            return jsonString
        }

        fun printHashKey(context: Context){
            try {
                val info: PackageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES)
                for (signature in info.signatures) {
                    val md: MessageDigest = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val hashKey: String = String(encode(md.digest(), 0))
                    FirebaseCrashlytics.getInstance().log("FirebasehashKeyhashKey:::  $hashKey")
                    FirebaseCrashlytics.getInstance().recordException(RuntimeException())
                    Log.i(TAG, "printHashKey() Hash Key: $hashKey")
                }
            } catch (e: NoSuchAlgorithmException) {
                Log.e(TAG, "printHashKey()", e)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "printHashKey()", e)
            }
        }

    }


}