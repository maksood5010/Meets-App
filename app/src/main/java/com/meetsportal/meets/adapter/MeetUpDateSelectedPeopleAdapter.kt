package com.meetsportal.meets.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.models.SelectedContactPeople
import com.meetsportal.meets.utils.Utils
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import de.hdodenhof.circleimageview.CircleImageView

class MeetUpDateSelectedPeopleAdapter (var myContext: Activity, var list: ArrayList<SelectedContactPeople>?, var removedItem: (SelectedContactPeople?) -> Unit,var click:()->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG = MeetUpDateSelectedPlaceAdapter::class.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : RecyclerView.ViewHolder {
        if(viewType == ITEM)
            return RviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_selected_contact, parent, false)
            )
        else
            return AddviewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_meet_friend_adder, parent, false)
            )
    }

    override fun getItemViewType(position: Int): Int {
        list?.let {
            if(position == it.size) return ADDER
            return ITEM
        }
        return ADDER
    }

    override fun getItemCount(): Int {
        Log.i(TAG," getItemCountgetItemCount::: ${list?.size}")
        /*list?.let{
            if(it.isNotEmpty())
                return it.size.plus(1)
            else
                return 1
        }*/
        return list?.size?:0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position:  Int) {
        when(holder){
            is RviewHolder -> bindItem(holder,position)
            is  AddviewHolder -> bindAdder(holder,position)

        }

    }

    fun bindItem(holder: RviewHolder, position: Int) {
        list?.get(position)?.let {
            when(it){
                is SelectedContactPeople.SelectedPeople ->{
                    //it.profile_image_url?.let {
                        //Utils.stickImage(myContext,holder.pic,it,null)
                        holder.pic.loadImage(myContext,it.profile_image_url,R.drawable.ic_default_person)
                    //}?: run {
                     //   holder.pic.setImageResource(R.drawable.ic_default_person)
                    //}
                    holder.name.text =  it.username

                }
                is SelectedContactPeople.SelectedContact->{
                    //it.image?.let { img->
                      //  holder.pic.setImageBitmap(img)
                    //}?:run{
                        holder.pic.setImageResource(R.drawable.ic_default_person)
                    //}
                    it.name?.let { name ->holder.name.text =  name}?:run{ holder.name.visibility = View.INVISIBLE}
                }
            }
            holder.cancel.setOnClickListener {

                Log.i(TAG," selectPList:: before $position --${list?.size}")
                var removedItem = list?.get(position)
                Log.i(TAG," selectPList:: removed $position --${removedItem}")
                var b= list?.remove(removedItem)
                Log.i(TAG," selectPList:: after $position --${b}--${list}")
                notifyItemRemoved(position)
                notifyItemRangeChanged(position,list?.size?.plus(1)!!)
                //notifyDataSetChanged()
                removedItem(removedItem)
                Log.i(TAG," selectPList:: afterafter $position --${list?.size}--${list}")
            }
        }
    }

    fun bindAdder(holder: AddviewHolder, position: Int) {
        holder.root.setOnClickListener {
            click()
        }
    }


    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pic = itemView.findViewById<CircleImageView>(R.id.civ_image)
        var cancel = itemView.findViewById<ImageView>(R.id.iv_cancel)
        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }

    class AddviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root  = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }

    companion object{
        val ITEM = 0
        val ADDER = 1

    }

}

