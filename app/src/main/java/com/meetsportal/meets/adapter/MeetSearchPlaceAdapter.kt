package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.models.MeetsPlace
import com.meetsportal.meets.networking.places.Category
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.places.SearchPlaceResponse
import com.meetsportal.meets.networking.places.SearchPlaceResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import org.json.JSONObject


class MeetSearchPlaceAdapter(
    var myContext: Activity,
    var list: SearchPlaceResponse?,
    var selectedSlug: MutableMap<String?, MeetsPlace?>,
    var customPlace: ArrayList<AddressModel>,
    var meetType: MeetUpViewPageFragment.MeetType,
    var clickListener: () -> Unit,
    var prePlace : java.util.ArrayList<String?>? = java.util.ArrayList(),
    var preCustomPlace : java.util.ArrayList<String?>? = java.util.ArrayList()
) : RecyclerView.Adapter<MeetSearchPlaceAdapter.RviewHolder>() {
    private val TAG = MeetSearchPlaceAdapter::class.java.simpleName

    var categories: List<Category>?

    init {
        categories =
            PreferencesManager.get<CategoryResponse>(Constant.PREFRENCE_CATEGORY)?.definition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder = RviewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.card_meetup_place_search, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var item = list?.get(position)
        Log.i(TAG, " place::: ${item?.slug}")
        Utils.stickImage(
            myContext,
            holder.image,
            item?.featured_image_url ?: item?.main_image_url,
            null
        )
        if (selectedSlug.contains(item?.slug)) holder.check.setImageResource(R.drawable.ic_checked) else holder.check.setImageResource(
            R.drawable.ic_unchecked
        )
        if(prePlace?.contains(item?.elastic_id) == true){
            holder.background.setBackgroundColor(ContextCompat.getColor(myContext,R.color.gray1))
            holder.check.visibility = View.INVISIBLE
        }else{
            holder.background.setBackgroundColor(ContextCompat.getColor(myContext,R.color.white))
            holder.check.visibility = View.VISIBLE
        }
        item?.rating?.let {
            holder.tvRating.visibility = View.VISIBLE
            holder.tvRating.text = "".plus(it).plus("/5.0")
        } ?: run {
            holder.tvRating.visibility = View.INVISIBLE
        }
        if (item?.timings?.isNotEmpty() == true && item?.timings?.get(0)?.opentime != null) {
            holder.timing.visibility = View.VISIBLE
            holder.timing.text = "Open:".plus(item?.timings?.get(0)?.opentime).plus(" - ")
                .plus(item?.timings?.get(0)?.closetime)
        } else {
            holder.timing.visibility = View.INVISIBLE
        }

        if (getAddress(item)?.isNotEmpty() == true) {
            holder.address.visibility = View.VISIBLE
            holder.address.text = getAddress(item)
        } else {
            holder.address.visibility = View.INVISIBLE
        }
        holder.name.text = item?.name_en
        categories?.let {
            categories?.firstOrNull() { it.key.equals(item?.primary_kind_of_place?.get(0)) == true }
                ?.let {
                    holder.tvCategory.text = it.en
                    Utils.stickOGSvg(myContext, it.color_svg_url, holder.catIcon)
                }
        }
        //--------------start putting listener--------------------//
//        holder.name.setOnClickListener {
//            openDetailPage(item?.elastic_id)
//        }
        holder.image.setOnClickListener {
            openDetailPage(item?.elastic_id)
        }
        holder.itemView.setOnClickListener {
            if(prePlace?.contains(item?.elastic_id) == false) {
                if (selectedSlug.contains(item?.slug)) {
                    selectedSlug.remove(item?.slug)
                    holder.check.setImageResource(R.drawable.ic_unchecked)
                } else {
                    if (meetType == MeetUpViewPageFragment.MeetType.OPEN) {
                        selectedSlug.clear()
                        selectedSlug.put(item?.slug, item?.toMeetUpPlace())
                        holder.check.setImageResource(R.drawable.ic_checked)
                    } else {
                        if (selectedSlug.size.plus(customPlace.size).plus(prePlace?.size ?: 0)
                                .plus(preCustomPlace?.size ?: 0) < Constant.MAX_PLACE_SIZE
                        ) {
                            selectedSlug.put(item?.slug, item?.toMeetUpPlace())
                            holder.check.setImageResource(R.drawable.ic_checked)
                        } else {
                            MyApplication.showToast(myContext,
                                "You can select up to ".plus(Constant.MAX_PLACE_SIZE)
                                    .plus(" places only")
                            )
                        }
                    }
                }
                clickListener()
            }

        }

    }
//
    fun openDetailPage(id:String?){
    MyApplication.putTrackMP(Constant.AcCreateMeetPlaceImage, JSONObject(mapOf("placeId" to id)))
        var baseFragment : BaseFragment = RestaurantDetailFragment();
        Navigation.setFragmentData(baseFragment,"_id",id)
        Navigation.setFragmentData(baseFragment,"isItElastic","yes")
        Navigation.addFragment(myContext,baseFragment,
            RestaurantDetailFragment.TAG,R.id.homeFram,true,false)
    }

    fun getAddress(data: SearchPlaceResponseItem?): String? {
        val sbAddress = StringBuilder()
        //data?.street?.en?.let { street -> sbAddress.append(street);sbAddress.append(" ") }
        data?.city?.let { sbAddress.append(it);sbAddress.append(" ") }
        data?.state?.let { sbAddress.append(it);sbAddress.append(" ") }
        data?.country?.let { sbAddress.append(it);sbAddress.append(" ") }
        return sbAddress.toString()
    }

    override fun getItemCount(): Int {
        if (list == null)
            return 0
        else {
            Log.i(TAG, " getItemCountplaces:: ${list?.size}")
            return list?.size!!
        }

    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var background = itemView.findViewById<LinearLayout>(R.id.ll_desc)
        var image = itemView.findViewById<ImageView>(R.id.iv_image)
        var catIcon = itemView.findViewById<ImageView>(R.id.cat_icon)
        var check = itemView.findViewById<ImageView>(R.id.checkbox)
        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var tvRating = itemView.findViewById<TextView>(R.id.tv_rate)
        var tvCategory = itemView.findViewById<TextView>(R.id.tv_categories)
        var timing = itemView.findViewById<TextView>(R.id.timing)
        var address = itemView.findViewById<TextView>(R.id.address)
        var rlCheck = itemView.findViewById<RelativeLayout>(R.id.rl_check)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }

}