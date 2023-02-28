package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.notifications.NotificationModal
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.*
import com.meetsportal.meets.utils.*
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import java.util.*

class NotificationPagingAdapter(var myContext: Activity,var meetUpInvitation:(String?,String?,String?,String?,String?)->Unit) :
    PagingDataAdapter<NotificationModal, RecyclerView.ViewHolder>(COMPARATOR) {

    private val TAG = NotificationPagingAdapter::class.java.simpleName

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (item is NotificationModal.MyNotification) {
            Log.i(TAG, " MyNotification:::: $item")
            bindNotification(holder as RviewHolder, item)
        } else {
            bindSeparatror(holder as SviewHolder, item as NotificationModal.SeparatorItem)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == MY_NOTIFICATION) {
            RviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_notification, parent, false)
            )
        } else {
            SviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.notification_seprator, parent, false)
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) is NotificationModal.MyNotification) MY_NOTIFICATION
        else MY_SEPRATOR
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dp = itemView.findViewById<ImageView>(R.id.dp)
        var image = itemView.findViewById<ImageView>(R.id.image)
        var textPost = itemView.findViewById<TextView>(R.id.etTextpost)
        var desc = itemView.findViewById<TextView>(R.id.desc)
        var card = itemView.findViewById<CardView>(R.id.card)
        var time = itemView.findViewById<TextView>(R.id.time)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }

    class SviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var type = itemView.findViewById<TextView>(R.id.type)
    }

    fun bindNotification(holder: RviewHolder, item: NotificationModal.MyNotification) {
        holder.time.text = Utils.getCreatedAt(item.timestamp)
        Log.i(TAG, " DocumentID:::: ${item.document_id}")
        if (item.seen == false) {
            holder.root.setBackgroundColor(ContextCompat.getColor(myContext, R.color.bg_card))
        } else {
            holder.root.setBackgroundColor(ContextCompat.getColor(myContext, R.color.page_bg))
        }

        when (item.type) {
            "like_on_post" -> {
                bindLikePostnotification(holder, item)
            }
            "follow_user" -> {
                bindFollowUserNotification(holder, item)
            }
            "comment_on_post" -> {
                bindCommentOnPostNotification(holder, item)
            }
            "reply_on_comment" -> {
                //PENDING
                bindReplyCommentNotification(holder, item)
            }
            "like_on_comment" -> {
                bindLikeOnCommentNotification(holder, item)
            }
            "meetup_invitation" -> {
                bindMeetUpInvitaionNotification(holder, item)
            }
            "meetup_positive_experience_reminder" ->{
                bindMeetUpPositiveResNotification(holder, item)
            }
            "meetup_join_requests" ->{
                bindJoinRequest(holder, item)
            }
        }
        holder.desc.onClick( {
            holder.root.setBackgroundColor(ContextCompat.getColor(myContext, R.color.page_bg))
            MyApplication.putTrackMP(Constant.AcNotiText, null)
            handleClick(holder, item,holder.desc)
            FirebaseFirestore.getInstance().collection("notifications")
                .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                .collection(Constant.USER_NOTIFICATION_NODE).document(item.document_id!!).update(
                    mapOf(
                        "seen" to true,
                        "seen_at" to Date().formatTo(
                            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                            TimeZone.getTimeZone("Europe/London")
                        )
                    )
                )

        })
        holder.time.onClick( {
            holder.root.setBackgroundColor(ContextCompat.getColor(myContext, R.color.page_bg))
            handleClick(holder, item, holder.time)
            FirebaseFirestore.getInstance().collection("notifications")
                .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                .collection(Constant.USER_NOTIFICATION_NODE).document(item.document_id!!).update(
                    mapOf(
                        "seen" to true,
                        "seen_at" to Date().formatTo(
                            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                            TimeZone.getTimeZone("Europe/London")
                        )
                    )
                )

        })
        holder.image.onClick( {
            holder.root.setBackgroundColor(ContextCompat.getColor(myContext, R.color.page_bg))
            handleClick(holder, item, holder.image)
            FirebaseFirestore.getInstance().collection("notifications")
                .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                .collection(Constant.USER_NOTIFICATION_NODE).document(item.document_id!!).update(
                    mapOf(
                        "seen" to true,
                        "seen_at" to Date().formatTo(
                            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                            TimeZone.getTimeZone("Europe/London")
                        )
                    )
                )

        })
    }


    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    private fun handleClick(holder: RviewHolder, item: NotificationModal.MyNotification, view: View) {
        when (item.type) {
            "like_on_post" -> {
                handleLikePostnotification(holder, item)
            }
            "follow_user" -> {
                handleFollowUserNotification(holder, item)
            }
            "comment_on_post" -> {
                handleCommentOnPost(holder, item)
            }
            "reply_on_comment" -> {
                //PENDING
                handleReplyCommentNotification(holder, item)
            }
            "like_on_comment" -> {
                handleLikeOnCommentNotification(holder, item)
            }
            "meetup_invitation" -> {
                handleMeetUpInvitation(holder, item,view)
            }
            "meetup_positive_experience_reminder" ->{
                handleMeetPositiveRes(holder, item,view)
            }
            "meetup_join_requests" ->{
                handleMeetRequest(holder, item,view)
            }

        }
    }

    private fun handleMeetRequest(holder: RviewHolder, item: NotificationModal.MyNotification, view: View) {
        Utils.cancelNotification(myContext,item.entity_id)
        val meetUpId = item.payload?.get("meetup_id") as String?
        var fragment = RequestToJoinOpenFragment.getInstance(meetUpId)
        Navigation.addFragment(myContext, fragment, RequestToJoinOpenFragment.TAG, R.id.homeFram, true, false)
    }

    private fun handleMeetUpInvitation(
        holder: RviewHolder,
        item: NotificationModal.MyNotification,
        view: View
    ) {
        Log.i(TAG," MeetInvitaionNotificationClicked:: ")
        val invitationId = item.entity_id
        val meetUpId = item.payload?.get("meetup_id") as String?
        val status = item.payload?.get("status") as String?
        val type = item.payload?.get("type") as String?
        val post_id = item.payload?.get("post_id") as String?
        var usermeta = item.payload?.get("user") as Map<String, String>?

        if(status.equals("pending"))
            meetUpInvitation(meetUpId,invitationId,type,post_id,usermeta?.get("username"))
    }

    private fun handleMeetPositiveRes(
        holder: RviewHolder,
        item: NotificationModal.MyNotification,
        view: View
    ) {
        val meetUpId = item.payload?.get("meetup_id") as String?
        var fragment = MeetAttendeeList.getInstance(meetUpId,Constant.EnumMeetPerson.ATTENDEE)
        Navigation.addFragment(myContext,fragment,
            MeetAttendeeList.TAG,R.id.homeFram,true,false)

    }

    private fun handleLikeOnCommentNotification(
        holder: RviewHolder,
        item: NotificationModal.MyNotification
    ) {

        holder.root.setBackgroundColor(ContextCompat.getColor(myContext, R.color.page_bg))

        var parentIds =
            (item.payload?.get("comment_meta") as Map<String?, String?>?)?.get("parent_id")
                ?.split(",")
        var entityId =
            (item.payload?.get("comment_meta") as Map<String?, String?>?)?.get("entity_id")

        if (parentIds != null && entityId != null) {

            var baseFragment: BaseFragment = DetailPostFragment()
            if (parentIds?.get(1).equals(parentIds?.get(0))) {
                Navigation.setFragmentData(baseFragment, "post_id", parentIds?.get(0))
                Navigation.setFragmentData(baseFragment, "comment_id", entityId)
                Navigation.addFragment(
                    myContext, baseFragment,
                    Constant.TAG_DETAIL_POST_FRAGMENT, R.id.homeFram, true, false
                )
            } else {
                Log.i(TAG, "handleLikeOnCommentNotification: like reply")
                Navigation.setFragmentData(baseFragment, "post_id", parentIds?.get(0))
                Navigation.setFragmentData(baseFragment, "comment_id", parentIds?.get(1))
                Navigation.setFragmentData(baseFragment, "reply_comment_id", entityId)
                Navigation.addFragment(
                    myContext, baseFragment,
                    Constant.TAG_DETAIL_POST_FRAGMENT, R.id.homeFram, true, false
                )
            }

            /*FirebaseFirestore.getInstance().collection("notifications")
                .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                .collection(Constant.USER_NOTIFICATION_NODE).document(item.document_id!!).update(
                    mapOf(
                        "seen" to true,
                        "seen_at" to Date().formatTo(
                            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                            TimeZone.getTimeZone("Europe/London")
                        )
                    )
                )*/

        } else {
            Log.e(TAG, " Not Getting data from firebase ")
        }
    }

    private fun handleReplyCommentNotification(
        holder: RviewHolder,
        item: NotificationModal.MyNotification
    ) {

        var postId = item.payload?.get("post_id") as String?
        var parentIds =
            (item.payload?.get("comment_meta") as Map<String?, String?>?)?.get("parent_id")
                ?.split(",")
        var entityId =
            (item.payload?.get("comment_meta") as Map<String?, String?>?)?.get("entity_id")

        Log.i(TAG, " reply_comment_id:- ${item.entity_id} ")
        if (postId != null && parentIds != null && entityId != null) {

            var baseFragment: BaseFragment = DetailPostFragment()
            Navigation.setFragmentData(baseFragment, "post_id", postId)
            Navigation.setFragmentData(baseFragment, "comment_id", parentIds?.get(1))
            Navigation.setFragmentData(baseFragment, "reply_comment_id", entityId)
            Navigation.addFragment(
                myContext, baseFragment,
                Constant.TAG_DETAIL_POST_FRAGMENT, R.id.homeFram, true, false
            )

            Log.i(
                TAG,
                " postId:- $postId ;; comment_id:- ${parentIds?.get(1)} ;; reply_comment_id:- ${entityId}"
            )

            /*FirebaseFirestore.getInstance().collection("notifications")
                .document(FirebaseAuth.getInstance().currentUser?.uid!!)
                .collection(Constant.USER_NOTIFICATION_NODE).document(item.document_id!!).update(
                    mapOf(
                        "seen" to true,
                        "seen_at" to Date().formatTo(
                            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                            TimeZone.getTimeZone("Europe/London")
                        )
                    )
                )
*/
        } else {
            Log.e(TAG, " Not Getting data from firebase")
        }
    }

    private fun handleFollowUserNotification(
        holder: RviewHolder,
        item: NotificationModal.MyNotification
    ) {
        var baseFragment: BaseFragment = OtherProfileFragment.getInstance(item.entity_id,Constant.Source.NOTIFICATION.sorce)
        /*baseFragment = Navigation.setFragmentData(
            baseFragment,
            OtherProfileFragment.OTHER_USER_ID,
            item.entity_id
        )*/
        Navigation.addFragment(
            myContext,
            baseFragment,
            Constant.OTHER_PROFILE_FRAGMENT,
            R.id.homeFram,
            true,
            false
        )


    }

    private fun handleLikePostnotification(
        holder: RviewHolder,
        item: NotificationModal.MyNotification
    ) {

        var baseFragment: BaseFragment = DetailPostFragment()
        Navigation.setFragmentData(baseFragment, "post_id", item.entity_id)
        Navigation.addFragment(
            myContext, baseFragment,
            Constant.TAG_DETAIL_POST_FRAGMENT, R.id.homeFram, true, false
        )

        /*FirebaseFirestore.getInstance().collection("notifications").document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .collection(Constant.USER_NOTIFICATION_NODE).document(item.document_id!!).update(mapOf("seen" to true,"seen_at" to Date().formatTo("yyyy-MM-dd'T'HH:mm:ss.SSSXXX",TimeZone.getTimeZone("Europe/London"))))
*/


    }

    private fun handleCommentOnPost(
        holder: RviewHolder,
        item: NotificationModal.MyNotification
    ) {

        var parentId =
            (item.payload?.get("comment_meta") as Map<String?, String?>?)?.get("parent_id")
        var entityId =
            (item.payload?.get("comment_meta") as Map<String?, String?>?)?.get("entity_id")


        if (parentId != null && entityId != null) {

            var baseFragment: BaseFragment = DetailPostFragment()
            var ids = parentId.split(",")
            var postId = ids.getOrNull(0)
            Log.i(TAG, " post_id:: $postId")
            Log.i(TAG, " comment_id:: $entityId")
            Navigation.setFragmentData(baseFragment, "post_id", postId)
            Navigation.setFragmentData(baseFragment, "comment_id", entityId)
            Navigation.addFragment(
                myContext, baseFragment,
                Constant.TAG_DETAIL_POST_FRAGMENT, R.id.homeFram, true, false
            )

        } else {
            Log.e(TAG, " Not Getting data from FireBase")
        }
    }




    private fun bindSeparatror(holder: SviewHolder, item: NotificationModal.SeparatorItem) {
        holder.type.text = item.description
    }

    private fun bindReplyCommentNotification(
        holder: RviewHolder,
        item: NotificationModal.MyNotification
    ) {
        holder.card.visibility = View.GONE
        var usermeta = item.payload?.get("user_meta") as Map<String?, String?>?
        //Utils.stickImage(myContext, holder.dp, usermeta?.get("profile_image_url") as String?, null)
//        holder.dp.loadImage(myContext,usermeta?.get("profile_image_url"),R.drawable.ic_default_person)
        holder.dp.loadImage(myContext,"https://gateway-dev.shisheo.com/cdn/profile_resolver/".plus(usermeta?.get("sid")).plus("-").plus(System.currentTimeMillis()).plus(".png"),R.drawable.ic_default_person)

        Utils.stickImage(myContext, holder.image, item.payload?.get("media") as String?, null)

        bindImagedata(item.payload,holder)

        var desc: String

        if (item.payload?.get("count") as Long? == 1L) {
            desc = "<b>".plus(usermeta?.get("username")).plus("</b> replied on your comment:")
                .plus(" ${item.payload?.get("body")}")

        } else {
            desc = "<b>".plus(usermeta?.get("username")).plus("</b> replied on your comment:")
                .plus(" ${item.payload?.get("body")}\n<b>+")
                .plus("${((item.payload?.get("count") as Long?)?.minus(1))}").plus("</b> More")
        }

        //desc = "<b>".plus(usermeta.get("username")).plus("</b> replied on your comment:").plus(" ${item.payload.get("body")}\n<b>+").plus("${((item.payload.get("count") as Long?)?.minus(1))}").plus("</b> More")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.desc.text = Html.fromHtml(desc, Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE)
        } else {
            holder.desc.text = desc
        }
        holder.dp.onClick( {
            var baseFragment: BaseFragment = OtherProfileFragment.getInstance(usermeta?.get("sid"),Constant.Source.NOTIFICATION.sorce)
            /*baseFragment = Navigation.setFragmentData(
                baseFragment,
                OtherProfileFragment.OTHER_USER_ID,
                usermeta?.get("sid")
            )*/
            Navigation.addFragment(
                myContext, baseFragment,
                Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        })

    }

    private fun bindLikeOnCommentNotification(
        holder: RviewHolder,
        item: NotificationModal.MyNotification
    ) {
        holder.card.visibility = View.GONE
        var usermeta = item.payload?.get("user_meta") as Map<String?, String?>?
        //Utils.stickImage(myContext, holder.dp, usermeta?.get("profile_image_url") as String?, null)
//        holder.dp.loadImage(myContext,usermeta?.get("profile_image_url"),R.drawable.ic_default_person)
        holder.dp.loadImage(myContext,"https://gateway-dev.shisheo.com/cdn/profile_resolver/".plus(usermeta?.get("sid")).plus("-").plus(System.currentTimeMillis()).plus(".png"),R.drawable.ic_default_person)
        Utils.stickImage(myContext, holder.image, item.payload?.get("media") as String?, null)
        holder.desc.text = "SomeOne Liked Comment"
        Log.i(TAG, " SomeOne Liked Comment ")

        bindImagedata(item.payload,holder)

        var desc: String
        if (item.payload?.get("count") as Long == 1L) {
            desc = "<b>".plus(usermeta?.get("username")).plus("</b>").plus(" liked your comment")
        } else {
            desc = "<b>".plus(usermeta?.get("username")).plus("</b>").plus(" and <b>")
                .plus("${(item.payload.get("count") as Long - 1)}")
                .plus(" others</b> Liked Comment")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.desc.text = Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY)
        } else {
            holder.desc.text = desc
        }
        holder.dp.onClick( {
            var baseFragment: BaseFragment = OtherProfileFragment.getInstance(usermeta?.get("sid"),Constant.Source.NOTIFICATION.sorce)
           /* baseFragment = Navigation.setFragmentData(
                baseFragment,
                OtherProfileFragment.OTHER_USER_ID,
                usermeta?.get("sid")
            )*/
            Navigation.addFragment(
                myContext, baseFragment,
                Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        })

    }

    private fun bindCommentOnPostNotification(
        holder: RviewHolder,
        item: NotificationModal.MyNotification
    ) {

        holder.card.visibility = View.VISIBLE
        var usermeta = item.payload?.get("user_meta") as Map<String, String>?
        //Utils.stickImage(myContext, holder.dp, usermeta?.get("profile_image_url") as String?, null)
//        holder.dp.loadImage(myContext,usermeta?.get("profile_image_url"),R.drawable.ic_default_person)
        holder.dp.loadImage(myContext,"https://gateway-dev.shisheo.com/cdn/profile_resolver/".plus(usermeta?.get("sid")).plus("-").plus(System.currentTimeMillis()).plus(".png"),R.drawable.ic_default_person)

        Utils.stickImage(myContext, holder.image, item.payload?.get("media") as String?, null)
        Log.d(TAG, "bindCommentOnPostNotification: check1")
        bindImagedata(item.payload,holder)
        Log.i(TAG, "bindCommentOnPostNotification: 333  ${holder.card.visibility == View.VISIBLE}")



        var desc: String
        //desc = "<b>".plus(usermeta.get("username")).plus("</b> commented on your post:").plus(" ${item.payload.get("body")}\n<b>+").plus("${(item.payload.get("count") as Long - 1)}").plus("</b> More")
        if (item.payload?.get("count") as Long == 1L) {
            desc = "<b>".plus(usermeta?.get("username")).plus("</b> commented on your post:")
                .plus(" ${item.payload.get("body")}")

        } else {
            desc = "<b>".plus(usermeta?.get("username")).plus("</b> commented on your post:")
                .plus(" ${item.payload.get("body")}\n<b>+")
                .plus("${(item.payload.get("count") as Long - 1)}").plus("</b> More")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.desc.text = Html.fromHtml(desc, Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE)
        } else {
            holder.desc.text = desc
        }
        holder.dp.onClick( {
            var baseFragment: BaseFragment = OtherProfileFragment.getInstance(usermeta?.get("sid"),Constant.Source.NOTIFICATION.sorce)
            /*baseFragment = Navigation.setFragmentData(
                baseFragment,
                OtherProfileFragment.OTHER_USER_ID,
                usermeta?.get("sid")
            )*/
            Navigation.addFragment(
                myContext, baseFragment,
                Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        })
    }


    fun bindLikePostnotification(holder: RviewHolder, item: NotificationModal.MyNotification) {
        Log.d(TAG, "bindLikePostnotification: $item")
        holder.card.visibility = View.VISIBLE
        var usermeta = item.payload?.get("user_meta") as Map<String, String>?
       // Utils.stickImage(myContext, holder.dp, usermeta?.get("profile_image_url") as String?, null)
        //holder.dp.loadImage(myContext,usermeta?.get("profile_image_url"),R.drawable.ic_default_person)
        holder.dp.loadImage(myContext,"https://gateway-dev.shisheo.com/cdn/profile_resolver/".plus(usermeta?.get("sid")).plus("-").plus(System.currentTimeMillis()).plus(".png"),R.drawable.ic_default_person)
        Utils.stickImage(myContext, holder.image, item.payload?.get("media") as String?, null)

        bindImagedata(item.payload,holder)

        var desc: String
        Log.i(TAG," checkingDataType:: ${item.payload}")
        if (item.payload?.get("count") as Long == 1L) {
            desc = "<b>".plus(usermeta?.get("username")).plus("</b>").plus(" liked your post")

        } else {
            desc = "<b>".plus(usermeta?.get("username")).plus("</b>").plus(" and <b>")
                .plus("${(item.payload.get("count") as Long - 1)}")
                .plus(" others</b> liked your post")

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.desc.text = Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY)
        } else {
            holder.desc.text = desc
        }

        holder.dp.onClick( {
            var baseFragment: BaseFragment = OtherProfileFragment.getInstance(usermeta?.get("sid"),Constant.Source.NOTIFICATION.sorce)
            /*baseFragment = Navigation.setFragmentData(
                baseFragment,
                OtherProfileFragment.OTHER_USER_ID,
                usermeta?.get("sid")
            )*/
            Navigation.addFragment(
                myContext, baseFragment,
                Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        })

    }

    private fun bindFollowUserNotification(
        holder: RviewHolder,
        item: NotificationModal.MyNotification
    ) {
        holder.card.visibility = View.GONE

        /*Utils.stickImage(
            myContext,
            holder.dp,
            item.payload?.get("profile_image_url") as String?,
            null
        )*/
//        holder.dp.loadImage(myContext,item.payload?.get("profile_image_url") as String?,R.drawable.ic_default_person)
        holder.dp.loadImage(myContext,"https://gateway-dev.shisheo.com/cdn/profile_resolver/".plus(item.payload?.get("sid")).plus("-").plus(System.currentTimeMillis()).plus(".png"),R.drawable.ic_default_person)


        var desc =
            "<b>".plus(item.payload?.get("username") as String).plus("</b> started following you")
        Log.i(TAG, " desc::: $desc")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.desc.text = Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY)
        } else {
            holder.desc.text = desc
        }
        holder.dp.onClick( {
            var baseFragment: BaseFragment = OtherProfileFragment.getInstance(item.payload?.get("sid") as String,Constant.Source.NOTIFICATION.sorce)
            /*baseFragment = Navigation.setFragmentData(
                baseFragment,
                OtherProfileFragment.OTHER_USER_ID,
                item.payload?.get("sid") as String
            )*/
            Navigation.addFragment(
                myContext, baseFragment,
                Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
            )
        })
    }

    private fun bindMeetUpInvitaionNotification(
        holder: NotificationPagingAdapter.RviewHolder,
        item: NotificationModal.MyNotification
    ) {

        holder.card.visibility = View.GONE
        Log.i(TAG," payload::item.payload:: ${item.payload}")
        Log.i(TAG," payload::item.uid:: ${FirebaseAuth.getInstance().currentUser?.uid}")

        var meetUpId = item.payload?.get("meetup_id") as String?
        Log.i(TAG," meetUpId:: $meetUpId")
        var usermeta = item.payload?.get("user") as Map<String, String>?
        var status = item.payload?.get("status") as String?
        /*Utils.stickImage(
            myContext,
            holder.dp,
            usermeta?.get("profile_image_url"),
            null
        )*/
//        holder.dp.loadImage(myContext,usermeta?.get("profile_image_url"),R.drawable.ic_default_person)
        holder.dp.loadImage(myContext,"https://gateway-dev.shisheo.com/cdn/profile_resolver/".plus(usermeta?.get("sid")).plus("-").plus(System.currentTimeMillis()).plus(".png"),R.drawable.ic_default_person)




        var desc = ""
        Log.i(TAG," status::: ${status}")
        when(status){
            "pending" -> desc = "<b>".plus(usermeta?.get("username")).plus("</b> sent you invitation to Meetup")
            "accepted" -> desc = "you accepted <b>".plus(usermeta?.get("username")).plus("</b> Meetup invitation. ")
            "rejected" -> desc = "you declined <b>".plus(usermeta?.get("username")).plus("</b> Meetup invitation. ")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.desc.text = Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY)
        } else {
            holder.desc.text = desc
        }
        holder.dp.onClick( {
            if(usermeta?.get("sid").equals(MyApplication.SID)){
                Navigation.addFragment(
                    myContext,
                    ProfileFragment(), Constant.TAG_PROFILE_FRAGMENT, R.id.homeFram, true, false
                )
            }else{
                var baseFragment: BaseFragment = OtherProfileFragment.getInstance(usermeta?.get("sid") as String,Constant.Source.NOTIFICATION.sorce)
                /*baseFragment = Navigation.setFragmentData(
                    baseFragment,
                    OtherProfileFragment.OTHER_USER_ID,
                    usermeta?.get("sid") as String
                )*/
                Navigation.addFragment(
                    myContext, baseFragment,
                    Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false
                )
            }
        })

    }
    fun bindMeetUpPositiveResNotification(
        holder: RviewHolder,
        item: NotificationModal.MyNotification
    ) {
        holder.card.visibility = View.GONE
        var meetUpId = item.payload?.get("meetup_id") as String?
        var meetName = item.payload?.get("name") as String?
        holder.dp.setImageResource(R.drawable.ic_meets_coloured)
        var desc = ""
        desc = "<b>".plus(meetName?.take(20)).plus(if(meetName?.length?.compareTo(20) == 1)"..." else "").plus(" :</b> Give a Positive Experience review to the participants of this meetup")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.desc.text = Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY)
        } else {
            holder.desc.text = desc
        }
        holder.dp.onClick({
            var fragment = MeetAttendeeList.getInstance(meetUpId,Constant.EnumMeetPerson.ATTENDEE)
            Navigation.addFragment(myContext,fragment,
                MeetAttendeeList.TAG,R.id.homeFram,true,false)
        })

    }

    private fun bindJoinRequest(holder: RviewHolder, item: NotificationModal.MyNotification) {
        holder.card.visibility = View.GONE
        var meetUpId = item.payload?.get("meetup_id") as String?
        var meetName = item.payload?.get("name") as String?
        var usermeta = item.payload?.get("user_meta") as Map<String?, String?>?
        holder.dp.setImageResource(R.drawable.bg_default_place_v)
        var desc = ""
        if (item.payload?.get("count") as Long == 1L) {
            desc = "<b>".plus(usermeta?.get("username")).plus("</b>").plus(" requested to join your open meetup $meetName")
        } else {
            desc = "<b>".plus(usermeta?.get("username")).plus("</b>").plus(" and <b>")
                    .plus("${(item.payload.get("count") as Long - 1)}")
                    .plus(" others</b> requested to join your open meetup $meetName")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.desc.text = Html.fromHtml(desc, Html.FROM_HTML_MODE_LEGACY)
        } else {
            holder.desc.text = desc
        }
        holder.dp.onClick({
            var fragment = RequestToJoinOpenFragment.getInstance(meetUpId)
            Navigation.addFragment(myContext, fragment, RequestToJoinOpenFragment.TAG, R.id.homeFram, true, false)
        })

    }


    fun bindImagedata(payload: Map<String, Any>?, holder: RviewHolder) {
        Log.d(TAG, "bindCommentOnPostNotification: check2")

        when(payload?.getOrDefault("type","default")){
            "text_post" ->{
                holder.card.visibility = View.GONE
                holder.textPost.visibility =View.VISIBLE
                holder.image.visibility = View.GONE
                Log.i(TAG, "bindImagedata: ${payload}")
//                var gradient = Constant.GradientTypeArray.firstOrNull {first-> first.label.equals((payload.get("body_obj").get("text_post") as Map<String,String>?)?.get("gradient_type")) }
                var gradient = Constant.GradientTypeArray.firstOrNull {first-> first.label.equals(((payload.get("body_obj") as Map<String,Map<*,*>>?)?.get("text_post") as Map<String,String>?)?.get("gradient_type"))}
                holder.textPost.text = payload.get("post_body") as String?
                gradient?.let {
                    Log.i(TAG, "bindImagedata: 1")
                    holder.textPost.background =  Utils.gradientFromColor(gradient.gradient)
                    holder.textPost.setTextColor(Color.parseColor(it.textColor))
                } ?: run {
                    Log.i(TAG, "bindImagedata: 2")
                    holder.textPost.background = Utils.gradientFromColor(Constant.GradientTypeArray.first().gradient)
                    holder.textPost.setTextColor(Color.parseColor("#ffffff"))
                }
            }
            "default"->{
                holder.textPost.visibility = View.GONE
                holder.image.visibility = View.VISIBLE
            }
            "open_meetup"->{
                holder.textPost.visibility = View.GONE
                holder.image.visibility = View.VISIBLE
                holder.image.setImageResource(R.drawable.ic_bg_default_place_h)
            }
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<NotificationModal>() {
            override fun areItemsTheSame(
                oldItem: NotificationModal,
                newItem: NotificationModal
            ): Boolean {

                val isSameNotification = oldItem is NotificationModal.MyNotification
                        && newItem is NotificationModal.MyNotification
                        && oldItem.entity_id == newItem.entity_id

                val isSameSeprator = oldItem is NotificationModal.SeparatorItem
                        && newItem is NotificationModal.SeparatorItem
                        && newItem.description == oldItem.description

                return isSameNotification || isSameSeprator


            }

            override fun areContentsTheSame(
                oldItem: NotificationModal,
                newItem: NotificationModal
            ): Boolean {
                return oldItem == newItem
            }
        }

        private const val MY_NOTIFICATION = 0
        private const val MY_SEPRATOR = 1
    }

}