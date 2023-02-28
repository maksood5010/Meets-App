package com.meetsportal.meets.adapter

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.networking.places.CategoryResponse
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.restaurantdetailfragment.PlacesListFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Navigation
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.Utils
import org.json.JSONObject


class HomeCategoriesAdapter(var myContext: Activity, var list: CategoryResponse, var click: (Int) -> Unit) : RecyclerView.Adapter<HomeCategoriesAdapter.RviewHolder>() {

    private val TAG = HomeCategoriesAdapter::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_home_category, parent, false)
    )

    override fun getItemCount(): Int {
        if (list.definition == null)
            return 0
        else if(list.definition?.isNotEmpty() == true)
            return list.definition?.size!!
        else
            return 0
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {

        var data = list.definition?.get(position)
        holder.itemView.setOnClickListener {
            click(position)
        }
        Utils.stickOGSvg(myContext,data?.color_svg_url,holder.image)
        holder.titleName.text = data?.en
        holder.titleName.setGravity(Gravity.CENTER_HORIZONTAL)
        holder.root.setOnClickListener {
            var categoryResponse = PreferencesManager.get<CategoryResponse>(Constant.PREFRENCE_CATEGORY)
            categoryResponse?.definition?.get(position)?.isSelected = true

            PreferencesManager.put(categoryResponse, Constant.PREFRENCE_CATEGORY)
            MyApplication.putTrackMP(Constant.AcPlacesCategories, JSONObject(mapOf("categoryId" to categoryResponse?._id)))
            var baseFragment: BaseFragment = PlacesListFragment()
            Navigation.addFragment(myContext,baseFragment,
                Constant.TAG_PLACE_LIST_FRAGMENT,R.id.homeFram,true,false)
        }
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //var mainImage = itemView.findViewById<>(R.id.mainImage)
        var image = itemView.findViewById<ImageView>(R.id.civ_image)
        var titleName = itemView.findViewById<TextView>(R.id.tv_name)
        var root  = itemView.findViewById<RelativeLayout>(R.id.rootCo)
    }

}

