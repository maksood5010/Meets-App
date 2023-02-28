package com.meetsportal.meets.adapter

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.profile.SearchPeopleResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.OnenOneChat
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage

class SearchPeopleChatAdapter (
    var myContext: Activity,  var clickListener: (Boolean) -> Unit
) : RecyclerView.Adapter<SearchPeopleChatAdapter.RviewHolder>() {

    val TAG = SearchPeopleChatAdapter::class.simpleName
    var list: SearchPeopleResponse = SearchPeopleResponse()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_search_people_item, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var data = list?.get(position)
        //Utils.stickImage(myContext,holder.dp,data?.profile_image_url,null)
        holder.dp.loadImage(myContext,data?.profile_image_url,R.drawable.ic_default_person)
        holder.name.text = data?.username
        holder.username.text = data?.first_name?:data?.username
        if(data?.sid?.equals(MyApplication.SID) == true){
            holder.root.setBackgroundColor(ContextCompat.getColor(myContext,R.color.extraLightGray))
        }else{
            holder.root.setBackgroundColor(ContextCompat.getColor(myContext,R.color.transparent))
        }


        Utils.onClick(holder.root,1000){
            Utils.hideSoftKeyBoard(myContext,holder.root)
            if(data?.sid?.equals(MyApplication.SID) == true){
            }else{
                /*var baseFragment : BaseFragment = OtherProfileFragment()
                baseFragment = Navigation.setFragmentData(baseFragment,
                    OtherProfileFragment.OTHER_USER_ID, data?.sid)
                Navigation.addFragment(myContext,baseFragment,
                    Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram,true,false)*/
                var baseFragment:BaseFragment = OnenOneChat.getInstance(data?.sid)
                Navigation.addFragment(myContext,baseFragment,
                    Constant.TAG_ONE_N_ONE_CHAT,R.id.homeFram,true,false)
            }
        }
    }

    fun setData(list: SearchPeopleResponse){
        Log.i(TAG," startsearching:: notifyDataSetChanged ")
        this.list?.clear()
        this.list?.addAll(list)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.list.removeIf{it.sid==MyApplication.SID}
        }
        notifyDataSetChanged()
        clickListener(list.isEmpty())
    }

    override fun getItemCount(): Int {
        Log.i(TAG," getItemCount:: ${list?.size?:0}")
        return list?.size?:0
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var dp = itemView.findViewById<ImageView>(R.id.image)
        var name = itemView.findViewById<TextView>(R.id.name)
        var username = itemView.findViewById<TextView>(R.id.username)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }

}