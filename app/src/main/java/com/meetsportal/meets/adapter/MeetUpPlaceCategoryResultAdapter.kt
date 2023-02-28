package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.databinding.FragmentMeetUpRestaurantBottomBinding
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.models.MeetsPlace
import com.meetsportal.meets.networking.places.Category
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.networking.places.FetchPlacesResponseItem
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.RestaurantDetailFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.Utils
import java.lang.StringBuilder
import java.util.*

class MeetUpPlaceCategoryResultAdapter(
    var myContext: Activity,
    var binding: FragmentMeetUpRestaurantBottomBinding,
    var selectedSlug: MutableMap<String?, MeetsPlace?>,
    var customPlace : ArrayList<AddressModel>,
    var meetType : MeetUpViewPageFragment.MeetType,
    var click: () ->Unit,
    var prePlace : ArrayList<String?>? = ArrayList(),
    var preCustomPlace : ArrayList<String?>? = ArrayList()
):
    PagingDataAdapter<FetchPlacesResponseItem, RecyclerView.ViewHolder>(COMPARATOR){

    private val TAG = MeetUpPlaceCategoryResultAdapter::class.java.simpleName
    var day: Int= Calendar.getInstance().get(Calendar.DAY_OF_WEEK)


    var categories : List<Category>?
    init {
        categories = PreferencesManager.get<CategoryResponse>(Constant.PREFRENCE_CATEGORY)?.definition
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.i(TAG,"item ${getItem(position)}")
        bindData(getItem(position),holder as RviewHolder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RviewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_meetup_place_search, parent, false))
    }


    private fun bindData(item: FetchPlacesResponseItem?, holder: RviewHolder) {
        Log.i(TAG," place::: ${item?.slug}")
        Utils.stickImage(myContext,holder.image,item?.featured_image_url?:item?.main_image_url,null)

        if(selectedSlug.contains(item?.slug)) holder.check.setImageResource(R.drawable.ic_checked) else holder.check.setImageResource(R.drawable.ic_unchecked)
        if(prePlace?.contains(item?._id) == true){
            holder.background.setBackgroundColor(ContextCompat.getColor(myContext,R.color.gray1))
            holder.check.visibility = View.INVISIBLE
        }else{
            holder.background.setBackgroundColor(ContextCompat.getColor(myContext,R.color.white))
            holder.check.visibility = View.VISIBLE
        }

        item?.rating?.let {
            holder.tvRating.visibility = View.VISIBLE
            holder.ivStar.visibility = View.VISIBLE
            holder.tvRating.text = "".plus(it).plus("/5.0")
        }?:run{
            holder.tvRating.visibility = View.INVISIBLE
            holder.ivStar.visibility = View.INVISIBLE
        }
        if(item?.timings?.isNotEmpty() == true && item?.timings?.get(0)?.opentime != null){
            holder.timing.visibility = View.VISIBLE
            holder.timing.text = "Open:".plus(item?.timings?.get(0)?.opentime).plus(" - ").plus(item?.timings?.get(0)?.closetime)
        }else{
            //holder.timing.visibility = View.INVISIBLE
            holder.timing.text = "Timing not available"
        }

        if(getAddress(item)?.isNotEmpty() == true){
            holder.address.visibility = View.VISIBLE
            holder.address.text = getAddress(item)
        }else{
            holder.address.visibility = View.INVISIBLE
        }
        holder.name.text = item?.name?.en
        categories?.let {
            categories?.firstOrNull() { it.key.equals(item?.primary_kind_of_place?.get(0)) == true }?.let {
                holder.tvCategory.text = it.en
                Utils.stickOGSvg(myContext,it.color_svg_url,holder.catIcon)
            }
        }
        //--------------start putting listener--------------------//
//        holder.name.setOnClickListener {
//            openDetailPage(item?._id)
//        }
        holder.image.setOnClickListener {
            openDetailPage(item?._id)
        }
        holder.itemView.setOnClickListener{
            if(prePlace?.contains(item?._id) == false) {
                if (selectedSlug.contains(item?.slug)) {
                    selectedSlug.remove(item?.slug)
                    holder.check.setImageResource(R.drawable.ic_unchecked)
                } else {
                    if (meetType == MeetUpViewPageFragment.MeetType.OPEN) {
                        selectedSlug.clear()
                        selectedSlug.put(item?.slug, item?.toMeetUpPlace())
                        holder.check.setImageResource(R.drawable.ic_checked)
                    } else {
                        if (selectedSlug.size.plus(customPlace.size).plus(prePlace?.size?:0).plus(preCustomPlace?.size?:0) < Constant.MAX_PLACE_SIZE) {
                            selectedSlug.put(item?.slug, item?.toMeetUpPlace())
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

    override fun getItemCount(): Int {
        Log.i(TAG, "checking Count:: ${super.getItemCount()}")
        if(super.getItemCount()> 0){
            binding.noData.visibility = View.GONE
        }
        else{
            binding.noData.visibility = View.VISIBLE
        }
        return super.getItemCount()
    }

    fun  getAddress(data: FetchPlacesResponseItem?):String?{
        val sbAddress = StringBuilder()
        data?.street?.en?.let { street -> sbAddress.append(street);sbAddress.append(" ") }
        data?.city?.let { sbAddress.append(it);sbAddress.append(" ") }
        data?.state?.let { sbAddress.append(it);sbAddress.append(" ") }
        data?.country?.let { sbAddress.append(it);sbAddress.append(" ") }
        return sbAddress.toString()
    }

    fun openDetailPage(id:String?){
        var baseFragment : BaseFragment = RestaurantDetailFragment();
        Navigation.setFragmentData(baseFragment,"_id",id)
        Navigation.addFragment(myContext,baseFragment,
            RestaurantDetailFragment.TAG,R.id.homeFram,true,false)
    }


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
        var root  = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }



    companion object{
        private val COMPARATOR = object : DiffUtil.ItemCallback<FetchPlacesResponseItem>(){
            override fun areItemsTheSame(
                oldItem: FetchPlacesResponseItem,
                newItem: FetchPlacesResponseItem
            ): Boolean {
                return (oldItem._id == newItem._id)
            }

            override fun areContentsTheSame(
                oldItem: FetchPlacesResponseItem,
                newItem: FetchPlacesResponseItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


}