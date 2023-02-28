package com.meetsportal.meets.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.application.MyApplication
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.models.AddressModelResponse
import com.meetsportal.meets.models.MeetsPlace
import com.meetsportal.meets.ui.fragments.socialfragment.AddPlaceToMeetUp
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpPlaceBottomFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment
import com.meetsportal.meets.ui.fragments.socialfragment.MeetUpViewPageFragment.MeetType
import com.meetsportal.meets.ui.fragments.socialfragment.OpenMeetPlaceBottomFragment
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage

class MeetAddressListAdapter <out T>(
    var myContext: Activity,
    val fragment : T? = null,
    var customPlace : ArrayList<AddressModel>,
    var selectedSlug : MutableMap<String?, MeetsPlace?>,
    var meetType : MeetUpViewPageFragment.MeetType,
    var click: (AddressModel) -> Unit,
    var prePlace : ArrayList<String?>? = ArrayList(),
    var preCustomPlace : ArrayList<String?>? = ArrayList()
) : RecyclerView.Adapter<MeetAddressListAdapter.ViewHolder>() {


    val TAG = MeetAddressListAdapter::class.simpleName

    var list: AddressModelResponse = AddressModelResponse()

    fun setListData(
        list: AddressModelResponse,
        second: AddressModel?,
    ) {

        Log.i(TAG," from::: 0---second $second")
        list.reverse()
        var selectedList = ArrayList(this.list.filter { it.selected == true })
        var unselectedList = ArrayList(list.filter { !selectedList.map{ it._id }.contains(it._id) })

        this.list.clear()
        this.list.addAll(selectedList)

        this.list.addAll(unselectedList)


        second?.let{ second->
            if (selectedSlug.size.plus(customPlace.size).plus(prePlace?.size?:0).plus(preCustomPlace?.size?:0) < Constant.MAX_PLACE_SIZE) {
                Log.i(TAG, " from::: 0")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Log.i(TAG, " from::: 3")
                    var selectedPlace =
                        this.list.firstOrNull { it.custom_uid?.equals(second.custom_uid) == true }
                    selectedPlace?.let { selectedPlace ->
                        Log.i(TAG, " from::: 4")
                        this.list.removeIf { it.custom_uid?.equals(second.custom_uid) == true }
                        if(meetType.type.equals(MeetType.OPEN.type))
                            this.list.map{ it.selected = false }
                        this.list.add(0, selectedPlace?.apply { selected = true })
                    }
                }
            }
            else{
                MyApplication.showToast(myContext, "You can select up to ".plus(Constant.MAX_PLACE_SIZE).plus(" places only") )
            }
        }
        //second?.let { second-> this.list.firstOrNull{ it._id == second._id}?.let{ it.selected = true } }
        notifyDataSetChanged()
        Log.i(TAG," from::: 1")
        rePopulateCustomPlace()

    }

    fun rePopulateCustomPlace(){
        Log.i(TAG,"   customPlace:::: --- ${list.filter { it?.selected?:false }.size}")
        customPlace.clear()
        customPlace.addAll( list.filter { it?.selected?:false })
        Log.i(TAG, " customPlace:: ${customPlace.size} ")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.card_meet_custom_address, parent, false)
        )

    @SuppressLint("RecyclerView")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val address = customPlace.firstOrNull{ it._id == list?.get(position)._id}?:list?.get(position).apply { selected = false }
        Log.i(TAG, " started NewBinding::: ${position}  ${address.selected}")

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

        if(address.selected == true){
            holder.rvCheckbox.setImageResource(R.drawable.ic_checked)
        }else{
            holder.rvCheckbox.setImageResource(R.drawable.ic_unchecked)
        }

        if(preCustomPlace?.contains(address._id) == true){
            holder.root.setBackgroundColor(ContextCompat.getColor(myContext,R.color.gray1))
            holder.rvCheckbox.visibility = View.INVISIBLE
        }else{
            holder.root.setBackgroundColor(ContextCompat.getColor(myContext,R.color.white))
            holder.rvCheckbox.visibility = View.VISIBLE
        }

        holder.tvPlaceName.text = address.name
        holder.tvPLaceAddress.text = address.address
        holder.tvPLaceDate.text = myContext.getString(R.string.saved_on, Utils.getCreatedAt(address.createdAt))
        holder.root.setOnClickListener{

            if(preCustomPlace?.contains(address._id) == false) {
                if (address.selected == true) {
                    if (meetType == MeetType.OPEN)
                        list.map { it.selected = false }
                    address.selected = false
                    holder.rvCheckbox.setImageResource(R.drawable.ic_unchecked)
                    Log.i(TAG, " from--2")
                    rePopulateCustomPlace()
                } else {
                    if (meetType == MeetType.OPEN) {
                        list.map { it.selected = false }
                        address.selected = true
                        holder.rvCheckbox.setImageResource(R.drawable.ic_checked)
                    } else {
                        if (selectedSlug.size.plus(customPlace.size).plus(prePlace?.size?:0).plus(preCustomPlace?.size?:0) < Constant.MAX_PLACE_SIZE) {
                            address.selected = true
                            holder.rvCheckbox.setImageResource(R.drawable.ic_checked)
                        } else {
                            MyApplication.showToast(myContext, "You can select up to ".plus(Constant.MAX_PLACE_SIZE).plus(" places only") )
                        }
                    }

                    Log.i(TAG, " from--3")

                    rePopulateCustomPlace()
                    click(address)
                }
                when (fragment) {
                    is MeetUpPlaceBottomFragment -> fragment.refreshedChecked()
                    is OpenMeetPlaceBottomFragment -> fragment.refreshedChecked()
                    is AddPlaceToMeetUp -> fragment.refreshedChecked()
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root : LinearLayout = itemView.findViewById(R.id.rootCo)
        var image: ImageView = itemView.findViewById(R.id.ivPlace)
        var cvTextView: CardView = itemView.findViewById(R.id.cvTextView)
        var tvDrawableText: TextView = itemView.findViewById(R.id.tvDrawableText)
        var rvCheckbox: ImageView = itemView.findViewById(R.id.rvCheckbox)
        var remove  = itemView.findViewById<ImageView>(R.id.remove).apply{
            visibility =View.GONE
        }
        var tvPlaceName: TextView = itemView.findViewById(R.id.tvPlaceName)
        var tvPLaceAddress: TextView = itemView.findViewById(R.id.tvPLaceAddress)
        var tvPLaceDate: TextView = itemView.findViewById(R.id.tvPLaceDate)
    }
}