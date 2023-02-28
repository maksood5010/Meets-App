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
import com.meetsportal.meets.networking.places.FetchPlacesResponse
import com.meetsportal.meets.networking.places.FetchPlacesResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import org.json.JSONObject

class MeetUpPlaceSavedAdapter(
    var myContext: Activity,
    var list: FetchPlacesResponse,
    var selectedSlug: MutableMap<String?, MeetsPlace?>,
    var customPlace: ArrayList<AddressModel>,
    var meetType: MeetUpViewPageFragment.MeetType,
    var click: () -> Unit,
    var prePlace : ArrayList<String?>? = ArrayList(),
    var preCustomPlace : ArrayList<String?>? = ArrayList()
) : RecyclerView.Adapter<MeetUpPlaceSavedAdapter.RviewHolder>() {

    private val TAG = MeetUpPlaceTrandingAdapter::class.java.simpleName
    var categories: List<Category>?

    init {
        categories =
            PreferencesManager.get<CategoryResponse>(Constant.PREFRENCE_CATEGORY)?.definition
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RviewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.card_meetup_place_search, parent, false)
    )

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        var data = list.get(position)
        holder.name.text = data.name?.en
        Utils.stickImage(
            myContext,
            holder.image,
            data.featured_image_url ?: data.main_image_url,
            null
        )
        if (selectedSlug.contains(data.slug)) holder.check.setImageResource(R.drawable.ic_checked) else holder.check.setImageResource(
            R.drawable.ic_unchecked
        )

        if(prePlace?.contains(data._id) == true){
            holder.background.setBackgroundColor(ContextCompat.getColor(myContext,R.color.gray1))
            holder.check.visibility = View.INVISIBLE
        }else{
            holder.background.setBackgroundColor(ContextCompat.getColor(myContext,R.color.white))
            holder.check.visibility = View.VISIBLE
        }

        data?.rating?.let {
            holder.tvRating.visibility = View.VISIBLE
            holder.ivStar.visibility = View.VISIBLE
            holder.tvRating.text = "".plus(it).plus("/5.0")
        } ?: run {
            holder.tvRating.visibility = View.INVISIBLE
            holder.ivStar.visibility = View.INVISIBLE
        }
        if (data?.timings?.isNotEmpty() == true && data?.timings?.get(0)?.opentime != null) {
            holder.timing.visibility = View.VISIBLE
            holder.timing.text = "Open:".plus(data?.timings?.get(0)?.opentime).plus(" - ")
                .plus(data?.timings?.get(0)?.closetime)
        } else {
            //holder.timing.visibility = View.INVISIBLE
            holder.timing.text = "Timing not available"
        }

        if (getAddress(data)?.isNotEmpty() == true) {
            holder.address.visibility = View.VISIBLE
            holder.address.text = getAddress(data)
        } else {
            holder.address.visibility = View.INVISIBLE
        }
        holder.name.text = data?.name?.en
        categories?.let {
            categories?.firstOrNull() { it.key.equals(data?.primary_kind_of_place?.get(0)) == true }
                ?.let {
                    holder.tvCategory.text = it.en
                    Utils.stickOGSvg(myContext, it.color_svg_url, holder.catIcon)
                }
        }
//        holder.name.setOnClickListener {
//            openDetailPage(data?._id)
//        }
        holder.image.setOnClickListener {
            openDetailPage(data._id)
        }

        holder.itemView.setOnClickListener {
            Log.i(TAG, " SavedSlug:: ${data.slug} -- ${selectedSlug}")
            if(prePlace?.contains(data._id) == false) {
                if (selectedSlug.contains(data.slug)) {
                    selectedSlug.remove(data.slug)
                    holder.check.setImageResource(R.drawable.ic_unchecked)
                } else {
                    if (meetType == MeetUpViewPageFragment.MeetType.OPEN) {
                        selectedSlug.clear()
                        selectedSlug.put(data.slug, data.toMeetUpPlace())
                        holder.check.setImageResource(R.drawable.ic_checked)
                    } else {
                        if (selectedSlug.size.plus(customPlace.size).plus(prePlace?.size?:0).plus(preCustomPlace?.size?:0) < Constant.MAX_PLACE_SIZE) {
                            selectedSlug.put(data.slug, data.toMeetUpPlace())
                            holder.check.setImageResource(R.drawable.ic_checked)
                        } else {
                            MyApplication.showToast(myContext, "You can select up to ".plus(Constant.MAX_PLACE_SIZE).plus(" places only") )

                        }
                    }

                }
                click()
            }
        }
    }

    fun openDetailPage(id:String?){
        MyApplication.putTrackMP(Constant.AcCreateMeetPlaceImage, JSONObject(mapOf("placeId" to id)))
        var baseFragment : BaseFragment = RestaurantDetailFragment();
        Navigation.setFragmentData(baseFragment,"_id",id)
        Navigation.addFragment(myContext,baseFragment,
            RestaurantDetailFragment.TAG, R.id.homeFram,true,false)
    }

    fun getAddress(data: FetchPlacesResponseItem?): String? {
        val sbAddress = StringBuilder()
        data?.street?.en?.let { street -> sbAddress.append(street);sbAddress.append(" ") }
        data?.city?.let { sbAddress.append(it);sbAddress.append(" ") }
        data?.state?.let { sbAddress.append(it);sbAddress.append(" ") }
        data?.country?.let { sbAddress.append(it);sbAddress.append(" ") }
        return sbAddress.toString()
    }


    /*class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image = itemView.findViewById<ImageView>(R.id.iv_img)
        var check = itemView.findViewById<ImageView>(R.id.checkbox)
        var name = itemView.findViewById<TextView>(R.id.tvName)
        var rlCheck = itemView.findViewById<RelativeLayout>(R.id.rlCheck)
        var root  = itemView.findViewById<ConstraintLayout>(R.id.root)
    }*/

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var background = itemView.findViewById<LinearLayout>(R.id.ll_desc)
        var image = itemView.findViewById<ImageView>(R.id.iv_image)
        var catIcon = itemView.findViewById<ImageView>(R.id.cat_icon)
        var check = itemView.findViewById<ImageView>(R.id.checkbox)
        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var tvRating = itemView.findViewById<TextView>(R.id.tv_rate)
        var ivStar = itemView.findViewById<ImageView>(R.id.iv_star)
        var tvCategory = itemView.findViewById<TextView>(R.id.tv_categories)
        var timing = itemView.findViewById<TextView>(R.id.timing)
        var address = itemView.findViewById<TextView>(R.id.address)
        var rlCheck = itemView.findViewById<RelativeLayout>(R.id.rl_check)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }

}
