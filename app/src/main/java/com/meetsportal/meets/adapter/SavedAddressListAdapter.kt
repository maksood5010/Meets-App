package com.meetsportal.meets.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.models.AddressModelResponse
import com.meetsportal.meets.ui.fragments.socialfragment.SavedPlaceListFragment
import com.meetsportal.meets.ui.fragments.socialfragment.memeit.BottomSheetOptions
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage

class SavedAddressListAdapter(
    var myContext: Context,
    var list: AddressModelResponse?,
    val fragment: SavedPlaceListFragment,
    var clickListener: (Boolean) -> Unit
) : RecyclerView.Adapter<SavedAddressListAdapter.ViewHolder>() {


    fun setListData(list: AddressModelResponse?) {
        this.list = list
        this.list?.reverse()
        notifyDataSetChanged()
        clickListener(list?.isEmpty() ?:true)
    }

    fun deleteItem(id: String) {
        if (id.isNotEmpty() && list != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                list!!.removeIf { it._id == id }
            }else {
                val temp= list!!.filter { it._id!=id }
                list!!.clear()
                list!!.addAll(temp)
            }
            notifyDataSetChanged()
            clickListener(list?.isEmpty() ?:true)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_near_by, parent, false)
        )

    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address: AddressModel = list?.get(position) ?: return
        holder.tvPLaceDate.visibility = View.VISIBLE
        if (address.image_url.isNullOrEmpty()) {
            holder.image.visibility = View.GONE
            holder.cvTextView.visibility = View.VISIBLE
            val firstLetter: String = address.name?.substring(0, 1) ?: return
            holder.tvDrawableText.text = firstLetter
        } else {
            holder.image.visibility = View.VISIBLE
            holder.cvTextView.visibility = View.GONE
            holder.image.loadImage(myContext, address.image_url, R.drawable.ic_direction)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP
        }
        holder.rvCheckbox.layoutParams = params
        holder.rvCheckbox.setImageResource(R.drawable.ic_more_vert)
        holder.tvPlaceName.text = address.name
        holder.tvPLaceAddress.text = address.address
        holder.tvPLaceDate.text =
            myContext.getString(R.string.saved_on, Utils.getCreatedAt(address.createdAt))
        val mBottomSheetOptions =
            BottomSheetOptions.getInstance("Delete", null, null, null, null)

        mBottomSheetOptions?.setBottomSheetLitener(object : BottomSheetOptions.BottomSheetListener {
            override fun bottomSheetClickedOption(buttonClicked: String) {
                when (buttonClicked) {
                    BottomSheetOptions.BUTTON1 -> {
                        fragment.deleteAddress(address._id!!)
                    }
                    BottomSheetOptions.CANCEL -> {
                    }
                }
            }

        })
        holder.itemView.setOnClickListener{
            Log.d("TAG", "onBindViewHolder: address.location ${address.location}")
        }
        holder.rvCheckbox.setOnClickListener {
            mBottomSheetOptions.show(fragment.childFragmentManager, mBottomSheetOptions.tag)
        }
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.ivPlace)
        var cvTextView: CardView = itemView.findViewById(R.id.cvTextView)
        var tvDrawableText: TextView = itemView.findViewById(R.id.tvDrawableText)
        var rvCheckbox: ImageView = itemView.findViewById(R.id.rvCheckbox)
        var tvPlaceName: TextView = itemView.findViewById(R.id.tvPlaceName)
        var tvPLaceAddress: TextView = itemView.findViewById(R.id.tvPLaceAddress)
        var tvPLaceDate: TextView = itemView.findViewById(R.id.tvPLaceDate)
    }
}