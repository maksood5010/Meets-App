package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.profile.Badge
import com.meetsportal.meets.networking.profile.FollowerFollowingResponse
import com.meetsportal.meets.overridelayout.BlurBuilder
import com.meetsportal.meets.ui.fragments.socialfragment.MineMoreMintsFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import com.skydoves.progressview.ProgressView
import it.sephiroth.android.library.xtooltip.ClosePolicy
import it.sephiroth.android.library.xtooltip.Tooltip
import kotlin.math.roundToInt


class MeetsCredibilityStatusAdapter(val context: Activity, val currentBadge: Badge, val mints: String?,val isProfile: Boolean, var listener: (name: String, index: Int, b: Boolean) -> Unit) : RecyclerView.Adapter<MeetsCredibilityStatusAdapter.RviewHolder>() {

    val arrayCount = arrayOfNulls<String?>(Constant.badgeList.size)
    val TAG = MeetsCredibilityStatusAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder {
        val holder = RviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.card_meets_credibility_status, parent, false))

        return holder
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {

        holder.pvMeetsCredibility.setOnProgressClickListener {
            Tooltip.Builder(context!!).anchor(holder.pvMeetsCredibility, 0, 0, false)
                    .closePolicy(ClosePolicy.TOUCH_ANYWHERE_CONSUME).showDuration(0)
                    .text("${String.format("%.0f", mints?.toDouble())} Mints")
                    //.styleId(style)
                    .create().show(holder.pvMeetsCredibility, Tooltip.Gravity.TOP, false)
        }

        holder.tvMineMoreMints.setOnClickListener {
            MyApplication.putTrackMP(Constant.AcMineMoreMints, null)
            Navigation.addFragment(context, MineMoreMintsFragment(), Constant.TAG_MINEMOREMINTS_FRAGMENT, R.id.homeFram, true, false)
            listener("", 0, true)
//            dialog.dismiss()
        }
        var badgeitem = Constant.badgeList[position]
        if(!isProfile){
            badgeitem=currentBadge
        }

        holder.ivBadge.setImageResource(badgeitem.foreground)
        holder.tvStatus.setText("${badgeitem.name}")
        holder.tvLevel.setText("Level ${badgeitem.level}")
        holder.tvStatusHolder.setText("${badgeitem.name} Status holder")
        holder.tvStatusHolderText.setText("Mine ${badgeitem.mintsForNext} mints to get to your next level.")
        if(badgeitem.key=="blue_diamond"){
            holder.tvStatusHolderText.setText("Congratulations on reaching the highest level")
        }
        holder.pvMeetsCredibility.highlightView.colorGradientStart = ContextCompat.getColor(context, badgeitem.light)
        holder.pvMeetsCredibility.highlightView.colorGradientEnd = ContextCompat.getColor(context, badgeitem.dark)
        holder.pvMeetsCredibility.setBackgroundResource(R.drawable.inner_shadow)
        val badgeBackground = Utils.customizeBadgeBackground(context, badgeitem)
        holder.flBadge.background = badgeBackground
        holder.tvMints.text="${badgeitem.mintsForNext} Mints"

        var progress = 0.0
        if(mints != null) {
            progress = (mints.toDouble() / badgeitem.mintsForNext) * 100
            val d:Int = (badgeitem.mintsForNext - mints?.toDouble()).roundToInt()
            holder.tvMineMore.text="Mine $d more mints to unlock this Status"
        }
        if(progress >= 100.0) {
            progress = 100.00
            holder.flBlurView.visibility=View.GONE
            holder.ivLockIcon.visibility=View.GONE
            holder.tvMineMore.visibility=View.GONE
            holder.tvPeopleText.visibility=View.VISIBLE
        } else {
        val content = holder.rootView
        if(content.width > 0) {
            val image = BlurBuilder.blur(content)
            holder.ivLock.setBackground(BitmapDrawable(context.resources, image))
        } else {
            content.viewTreeObserver.addOnGlobalLayoutListener {
                val image = BlurBuilder.blur(content)
                holder.ivLock.setBackground(BitmapDrawable(context.resources, image))
            }
        }
            holder.ivLockIcon.visibility=View.VISIBLE
            holder.tvMineMore.visibility=View.VISIBLE
            holder.flBlurView.visibility=View.VISIBLE
            holder.tvPeopleText.visibility=View.INVISIBLE

        }
        holder.pvMeetsCredibility.progress = progress.toFloat()
        holder.pvMeetsCredibility.labelText = "${String.format("%.1f", progress)}%"

        if(arrayCount[position].isNullOrEmpty()) {
            listener(badgeitem.key, position, false)
        } else {
            val count = arrayCount[position]?.toInt()
            if(count!! <20)
                holder.tvPeopleText.text = "${arrayCount[position]} people you know are ${badgeitem.name} Status holders"
            else
                holder.tvPeopleText.text = "20+ people you know are ${badgeitem.name} Status holders"
        }
    }

    override fun getItemCount(): Int {
        return if(isProfile){
            Constant.badgeList.size
        }else{
            1
        }
    }

    fun setData(map: Map<Int, FollowerFollowingResponse>) {
        val index = map.keys.first()
        val total = map.values.first().size
        arrayCount.set(index, "$total")
        notifyItemChanged(index)
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var flBadge = itemView.findViewById<FrameLayout>(R.id.fl_badge)
        var flBlurView = itemView.findViewById<FrameLayout>(R.id.flBlurView)
        var ivBadge = itemView.findViewById<ImageView>(R.id.iv_badge)
        var ivLockIcon = itemView.findViewById<ImageView>(R.id.ivLockIcon)
        var ivLock = itemView.findViewById<ImageView>(R.id.ivLock)
        var tvMineMore = itemView.findViewById<TextView>(R.id.tvMineMore)
        var tvStatus = itemView.findViewById<TextView>(R.id.tv_status)
        var tvLevel = itemView.findViewById<TextView>(R.id.tv_level)
        var tvStatusHolder = itemView.findViewById<TextView>(R.id.tv_status_holder)
        var tvMineMoreMints = itemView.findViewById<TextView>(R.id.tv_mine_more_mints)
        var tvStatusHolderText = itemView.findViewById<TextView>(R.id.tv_status_holder_text)
        var tvPeopleText = itemView.findViewById<TextView>(R.id.tv_people_text)
        var tvMints = itemView.findViewById<TextView>(R.id.tvMints)
        var pvMeetsCredibility = itemView.findViewById<ProgressView>(R.id.pv_meets_credibility)
        var rootView = itemView.findViewById<ConstraintLayout>(R.id.linearLayout2)

    }
}