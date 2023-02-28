package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.AddressModel
import com.meetsportal.meets.networking.meetup.Place
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.onClick
import com.meetsportal.meets.utils.setRoundedColorBackground

class MeetUpChatChosenPlacesAdapter (
    var myContext: Activity,
    val isCreator : Boolean,
    val places: List<Place?>?,
    val customPlaces : List<AddressModel>?,
    var colorArray: IntArray,
    var meetPlaceClicked:(String?, Int?)->Unit,
    var customPlacClicked: (AddressModel?, Int)->Unit,
    var addMorePlace : () -> Unit
):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG = MeetUpChatChosenPlacesAdapter::class.simpleName

    companion object{
        val MEET_PLACE = 101
        val CUSTOM_PLACE = 102
        val ADD_PLACE = 103

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)  : RecyclerView.ViewHolder {
        return when (viewType) {
            MEET_PLACE -> RviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.card_selected_place, parent, false))
            CUSTOM_PLACE -> CviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.card_selected_custom_place, parent, false))
            else -> AddView(LayoutInflater.from(parent.context)
                .inflate(R.layout.card_meet_place_adder, parent, false))
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is RviewHolder ->{
                bindMeetPlace(holder,position)
            }
            is CviewHolder ->{
                bindCustomPlace(holder,(position-(places?.size?:0)))
            }
        }
    }

    fun bindMeetPlace(holder: RviewHolder, position: Int) {
        var place = places?.get(position)
        Utils.stickImage(myContext,holder.image,place?.main_image_url,null)

        holder.tvLetter.text = place?.name?.en?.take(2)
        val col = colorArray.getOrNull(position)
        col?.let{
            holder.root.background = Utils.getRoundedColorBackground(myContext, it, 40f)
        }
//        holder.image.setOnLongClickListener {
//            longPress(place)
//            true
//        }
        holder.image.setOnClickListener {
            /*if(place?.selected == true){
                MyApplication.showToast(myContext,"Same vote!!!")
                return@setOnClickListener
            }
            places?.map{ it?.selected = false }
            customPlaces?.map{it.selected = false}
//            place?.selected = true
            notifyDataSetChanged()*/
            meetPlaceClicked(place?._id,position)
        }
        holder.name.text = places?.get(position)?.name?.en
    }

    fun bindCustomPlace(holder: CviewHolder, position: Int) {
        var data = customPlaces?.get(position)
        holder.name.text = data?.name
        if(data?.address != null && data?.address?.isNotEmpty() == true){
            holder.address.visibility = View.VISIBLE
            holder.address.text = data?.address
        }else{
            holder.address.visibility = View.INVISIBLE
            //holder.address.text = data?.address
        }
        val col = colorArray.getOrNull(position.plus(places?.size?:0))
        col?.let{
            holder.root.background = Utils.getRoundedColorBackground(myContext, it,40f)
        }
        holder.card.setOnClickListener {
            /*if(data?.selected == true){
                MyApplication.showToast(myContext,"Same vote!!!")
                return@setOnClickListener
            }
            places?.map{ it?.selected = false }
            customPlaces?.map{it.selected = false}
            data?.selected = true
            notifyDataSetChanged()*/
            customPlacClicked(data,position)
        }

        holder.card.setOnLongClickListener {
            customPlacClicked(data,position)
            true
        }

    }

    override fun getItemViewType(position: Int): Int {
        Log.i(TAG," getItemViewType::: $position -- ${places?.size} -- ${customPlaces?.size}")
        return when(position){
            in 0..places?.size?.minus(1)!! -> MEET_PLACE
            in  places.size..places?.size?.plus(customPlaces?.size!!).minus(1)!! -> CUSTOM_PLACE
            else -> ADD_PLACE
        }
    }

    override fun getItemCount(): Int {
        //Log.i(TAG," getItemCountcustom:: 1 --  ${places?.size?:0} -- ${customPlaces?.size?:0}")
        var placeSize = places?.size?:0
        var customSIze = customPlaces?.size?:0
        var size = placeSize.plus(customSIze)
        size = if(size < 5 && isCreator) (size+1) else size

        return size
    }


    fun setSelectedPlace(id: String?) {
        places?.firstOrNull(){ it?._id == id }?.selected = true
        notifyDataSetChanged()
    }

    inner class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var image = itemView.findViewById<ImageView>(R.id.image)
        var tvLetter = itemView.findViewById<TextView>(R.id.tvLetter)
        var circle = itemView.findViewById<View>(R.id.circle)
        var name = itemView.findViewById<TextView>(R.id.name)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }

    inner class CviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name = itemView.findViewById<TextView>(R.id.name)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        var card= itemView.findViewById<CardView>(R.id.card)
        var address = itemView.findViewById<TextView>(R.id.address)
    }

    inner class AddView(itemView: View): RecyclerView.ViewHolder(itemView) {
        var bg = itemView.findViewById<ConstraintLayout>(R.id.bg).apply {
            setRoundedColorBackground(myContext,R.color.gray1,corner = 10f,enbleDash = true,strokeHeight = 2f,Dashgap = 0f,stripSize = 10f,R.color.darkerGray)
            onClick({
                addMorePlace()
            })
        }
    }
}