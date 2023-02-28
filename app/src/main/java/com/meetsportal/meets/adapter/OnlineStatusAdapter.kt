package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.FBOnlineStatus
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.OnenOneChat
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import de.hdodenhof.circleimageview.CircleImageView

class OnlineStatusAdapter(var myContext : Activity) : RecyclerView.Adapter<OnlineStatusAdapter.RviewHolder>() {

    var list  = ArrayList<FBOnlineStatus?>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RviewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.card_recent_chat_one, parent, false)
    )

    override fun getItemViewType(position: Int): Int {

        return super.getItemViewType(position)
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var data = list.get(position)
//         holder.itemView.visibility=View.GONE
        holder.dp.loadImage(myContext, data?.profileImage,R.drawable.ic_default_person,false)
        data?.userName?.let {
            holder.username.visibility = View.VISIBLE
            holder.username.text = it
        }?:run{
            holder.username.visibility = View.GONE
        }
        holder.itemView.onClick({
            if(data?.sid !=null){
                var baseFragment: BaseFragment = OnenOneChat.getInstance(data?.sid)
                Navigation.addFragment(myContext, baseFragment, Constant.TAG_ONE_N_ONE_CHAT, R.id.homeFram, true, false)
            }
        })
        if(data?.status == "online"){
            holder.status.setImageResource(R.color.primaryDark)
        }else{
            holder.status.setImageResource(R.color.gray)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItem(list : List<FBOnlineStatus?>){
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dp = itemView.findViewById<ImageView>(R.id.dp)
        var username = itemView.findViewById<TextView>(R.id.username)
        var status = itemView.findViewById<CircleImageView>(R.id.status)
    }

}