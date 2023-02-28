package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.profile.SuggestionResponse
import com.meetsportal.meets.networking.profile.SuggestionResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.OtherProfileFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Constant.AcFollowOnSuggestion
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import com.meetsportal.meets.utils.onClick
import org.json.JSONObject

class SuggesionAdapter(var myContext: Activity, var suggestionResponse: SuggestionResponse?, var listener: (String, Boolean) -> Unit) : RecyclerView.Adapter<SuggesionAdapter.RviewHolder>() {

    private val TAG = SuggesionAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder {
        return RviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.card_news_feed_story, parent, false))
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        val data = suggestionResponse?.get(position)
        Log.i(TAG, " datasetChanges::: ${data?.profile_image_url}")
        data?.social?.getbadge()?.let { badge: String ->
            val firstOrNull = Utils.getBadge(badge)
            holder.verified.setImageResource(firstOrNull.foreground)
        }
        data?.profile_image_url?.let {
            //Utils.stickImage(myContext, holder.image, data?.profile_image_url, null)
            //Utils.stickImage(myContext, holder.image, data?.profile_image_url, null)
            holder.image.loadImage(myContext,data?.profile_image_url,R.drawable.ic_default_person)
        } ?: run {
            //Utils.stickImage(myContext, holder.image, null, null)
            holder.image.loadImage(myContext,null,R.drawable.ic_default_person)
        }

//        if(data?.verified_user == false) holder.verified.visibility = View.GONE
        //holder.name.text = data?.user?.first_name ?: data?.user?.username
        holder.name.text = data?.username
        holder.followCount.text = (data?.social?.followers_count ?: 0).toString().plus(" Followers")
        if(data?.followed_by_user == true) {
            holder.followUnfollow.text = "Unfollow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext, R.color.primaryDark))
            var bg = ContextCompat.getDrawable(myContext, R.drawable.horizontal_round_shape) as GradientDrawable
            bg.setStroke(1, ContextCompat.getColor(myContext, R.color.primaryDark))
            holder.followUnfollow.setBackgroundDrawable(bg)
        } else {
            holder.followUnfollow.text = "Follow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext, R.color.white))
            holder.followUnfollow.background = ContextCompat.getDrawable(myContext, R.drawable.round_border_primary_bg)
        }

        setListener(holder, data)
    }

    private fun setListener(holder: RviewHolder, data: SuggestionResponseItem?) {

        Utils.onClick(holder.followUnfollow, 500) {
            if(holder.followUnfollow.text.equals("Unfollow")) {
                listener(data?.sid!!, false)
            }
            else if(holder.followUnfollow.text.equals("Follow")) {
                MyApplication.putTrackMP(AcFollowOnSuggestion, JSONObject(mapOf("sid" to data?.sid)))
                listener(data?.sid!!, true)
            }
            toggleFollow(holder, data)
        }
        /*var b = RxView.clicks(holder.followUnfollow).throttleFirst(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer { any->
                if(holder.followUnfollow.text.equals("Unfollow"))
                    listener(data?.user?.sid!!,false)
                else if(holder.followUnfollow.text.equals("Follow")){
                    listener(data?.user?.sid!!,true)
                }
                toggleFollow(holder,data)
            })*/

        holder.image.onClick( {
            var baseFragment: BaseFragment = OtherProfileFragment.getInstance(data?.sid,Constant.Source.SUGGESTION.sorce.plus(MyApplication.SID))
            Navigation.addFragment(myContext, baseFragment, Constant.OTHER_PROFILE_FRAGMENT, R.id.homeFram, true, false)

        })

    }

    private fun toggleFollow(holder: RviewHolder, data: SuggestionResponseItem?) {
        if(holder.followUnfollow.text.equals("Unfollow")) {
            holder.followUnfollow.text = "Follow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext, R.color.white))
            holder.followUnfollow.background = ContextCompat.getDrawable(myContext, R.drawable.round_border_primary_bg)
            if(data?.followed_by_user == true) holder.followCount.text = (data?.social?.followers_count ?: 0).minus(1)
                    .toString().plus(" Followers")
            else holder.followCount.text = (data?.social?.followers_count ?: 0).toString()
                    .plus(" Followers")

        } else if(holder.followUnfollow.text.equals("Follow")) {
            holder.followUnfollow.text = "Unfollow"
            holder.followUnfollow.setTextColor(ContextCompat.getColor(myContext, R.color.primaryDark))
            var bg = ContextCompat.getDrawable(myContext, R.drawable.horizontal_round_shape) as GradientDrawable
            bg.setStroke(1, ContextCompat.getColor(myContext, R.color.primaryDark))
            holder.followUnfollow.setBackgroundDrawable(bg)
            if(data?.followed_by_user == true) holder.followCount.text = (data?.social?.followers_count ?: 0).toString()
                    .plus(" Followers")
            else holder.followCount.text = (data?.social?.followers_count ?: 0).plus(1).toString()
                    .plus(" Followers")

        }
    }

    override fun getItemCount(): Int {
//        Log.i(TAG, " suggestionChanges::  200  ${suggestionResponse?.size}")
        if(suggestionResponse != null) return suggestionResponse!!.size
        else {
            return 0
        }

    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var image = itemView.findViewById<ImageView>(R.id.civ_image)
        var verified = itemView.findViewById<ImageView>(R.id.iv_verify)
        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var followCount = itemView.findViewById<TextView>(R.id.tv_follow_count)
        var followUnfollow = itemView.findViewById<TextView>(R.id.tv_follow)
    }
}