package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication.Companion.SID
import com.meetsportal.meets.models.DMParent
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.OnenOneChat
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick

class DMParentAdapter(var myContext: Activity, var longPressed:(DMParent?)->Unit,val bind: (Boolean) -> Unit): RecyclerView.Adapter<DMParentAdapter.RviewHolder>() {

    val TAG = DMParentAdapter::class.java.simpleName
    var list = ArrayList<DMParent?>()
    var messageThreadSecrets: String? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.card_chat_list_item, parent, false)
                                                                                  )


    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var data = list.get(position)
        val find = data?.profiles?.find { it!=null && it?.sid != SID }
        //Utils.stickImage(myContext,holder.dp, find?.profile_image_url,null)
//        holder.dp.loadImage(myContext,find?.profile_image_url,R.drawable.ic_default_person)
        holder.dp.loadImage(myContext,"https://gateway-dev.shisheo.com/cdn/profile_resolver/".plus(find?.sid).plus("-").plus(System.currentTimeMillis()).plus(".png"),R.drawable.ic_default_person)

        holder.username.text = find?.username
        if(find?.verified_user == true) holder.verify.visibility = View.VISIBLE else holder.verify.visibility = View.GONE

        holder.root.onClick( {
            var baseFragment:BaseFragment = OnenOneChat.getInstance(find?.sid)
            Navigation.addFragment(myContext,baseFragment,
                    Constant.TAG_ONE_N_ONE_CHAT,R.id.homeFram,true,false)
        },1500)
        holder.root.setOnLongClickListener {
            longPressed(data)
            true
        }
        Log.d(TAG, "onBindViewHolder: data?.lastMessage ${data?.lastMessage}")
        holder.tvTime.text =Utils.timeStampToReadable(data?.lastMessage?.timestamp?.toDate())
        when(data?.lastMessage?.type) {
            "text"  -> {
                holder.msg.text = if (data?.lastMessage?.senderSid.equals(SID)) "You: ".plus(data?.lastMessage?.getMessage())
                else data?.lastMessage?.getMessage()
            }
            "image" -> {
                holder.msg.text=if (data?.lastMessage?.senderSid.equals(SID)) "You: \uD83D\uDCF7 Photo"
                else "\uD83D\uDCF7 Photo"
            }
            "video" -> {
                holder.msg.text=if (data?.lastMessage?.senderSid.equals(SID)) "You: 📹 Video"
                else "📹 Video"
            }
            "audio" -> {
                holder.msg.text=if (data?.lastMessage?.senderSid.equals(SID)) "You: \uD83D\uDD0A Audio"
                else "\uD83D\uDD0A Audio"
            }
        }
        Log.i(TAG," onBindViewHolder::: checking  ${find?.username}")
        if(data?.id?.indexOf(FirebaseAuth.getInstance().uid!!) == 0) {
            if(data?.unread1 == null || data.unread1 == 0){
                holder.count.visibility = View.GONE
            }else{
                holder.count.visibility = View.VISIBLE
                holder.count.text = data.unread1.toString()
            }
        } else {
            if(data?.unread2 == null || data.unread2 == 0 ){
                Log.i(TAG," onBindViewHolder::: gone ${data?.unread2}  ${find?.username}")
                holder.count.visibility = View.GONE
            } else{
                Log.i(TAG," onBindViewHolder::: visible ${data.unread2}  ${find?.username}")
                holder.count.visibility = View.VISIBLE
                holder.count.text = data.unread2.toString()
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItem(list : List<DMParent?>){
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
        bind(list.isEmpty())
    }

    class RviewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var dp = itemView.findViewById<ImageView>(R.id.dp)
        var username  = itemView.findViewById<TextView>(R.id.username)
        var msg = itemView.findViewById<TextView>(R.id.msg)
        var count = itemView.findViewById<TextView>(R.id.count)
        var tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        var verify = itemView.findViewById<ImageView>(R.id.verify)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }


}
