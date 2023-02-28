package com.meetsportal.meets.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R
import com.meetsportal.meets.database.entity.Contact
import com.meetsportal.meets.databinding.FragmentMeetUpFriendBottomBinding
import com.meetsportal.meets.models.SelectedContactPeople
import com.meetsportal.meets.utils.Utils.Companion.loadImage
import de.hdodenhof.circleimageview.CircleImageView

class SelectedContactAdapted (var myContext: Context, var list: ArrayList<SelectedContactPeople>?,var fragment : FragmentMeetUpFriendBottomBinding, var listener: (Int) -> Unit) :
    RecyclerView.Adapter<SelectedContactAdapted.RviewHolder>() {

    private val TAG = SelectedContactAdapted::class.java.simpleName
    var previousletter = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_selected_contact, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        list?.get(position)?.let {
            when(it){
                is SelectedContactPeople.SelectedPeople ->{
                   // it.profile_image_url?.let {
                        //Utils.stickImage(myContext,holder.pic,it,null)
                        holder.pic.loadImage(myContext,it.profile_image_url,R.drawable.ic_default_person)
                    /*}?: run {
                        holder.pic.setImageResource(R.drawable.ic_default_person)
                    }*/
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
                listener(position)
            }
        }
    }

    fun notifydataChanged(count : (Int)->Unit){
        notifyDataSetChanged()
        if(list?.isEmpty() == true){
            fragment.rvSelectedContact.visibility = View.GONE
        }else{
            fragment.rvSelectedContact.visibility = View.VISIBLE
        }
        count(list?.size?:0)
    }
    fun setData(list:List<Contact>){
        this.list?.clear()
        //this.list?.addAll(list.filter { it.selected == true })
        notifyDataSetChanged()

    }
    override fun getItemCount(): Int {
        list?.let {
            Log.i(TAG," count:: ${list?.size}")
            return list?.size!!
        }?: run {
            return 0
        }
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var pic = itemView.findViewById<CircleImageView>(R.id.civ_image)
        var cancel = itemView.findViewById<ImageView>(R.id.iv_cancel)
        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
    }


    companion object{
        class SelecedPlaceDiffCallBack(private var oldList: ArrayList<SelectedContactPeople>?, private var newList : ArrayList<SelectedContactPeople>?):
            DiffUtil.Callback(){
            override fun getOldListSize(): Int {
                return oldList?.size?:0
            }

            override fun getNewListSize(): Int {
                return newList?.size?:0
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

                if(oldList?.get(oldItemPosition) is SelectedContactPeople.SelectedContact && newList?.get(oldItemPosition) is SelectedContactPeople.SelectedContact){
                    return (oldList?.get(oldItemPosition) as SelectedContactPeople.SelectedContact).number!!.equals((newList?.get(oldItemPosition) as SelectedContactPeople.SelectedContact).number)
                }

                if(oldList?.get(oldItemPosition) is SelectedContactPeople.SelectedPeople && newList?.get(oldItemPosition) is SelectedContactPeople.SelectedPeople){
                    return (oldList?.get(oldItemPosition) as SelectedContactPeople.SelectedPeople).username!!.equals((newList?.get(oldItemPosition) as SelectedContactPeople.SelectedPeople).username)
                }
                return false
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                if(oldList?.get(oldItemPosition) is SelectedContactPeople.SelectedContact && newList?.get(oldItemPosition) is SelectedContactPeople.SelectedContact){
                    return (oldList?.get(oldItemPosition) as SelectedContactPeople.SelectedContact).number!!.equals((newList?.get(oldItemPosition) as SelectedContactPeople.SelectedContact).number)
                }

                if(oldList?.get(oldItemPosition) is SelectedContactPeople.SelectedPeople && newList?.get(oldItemPosition) is SelectedContactPeople.SelectedPeople){
                    return (oldList?.get(oldItemPosition) as SelectedContactPeople.SelectedPeople).username!!.equals((newList?.get(oldItemPosition) as SelectedContactPeople.SelectedPeople).username)
                }
                return false
            }

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                return super.getChangePayload(oldItemPosition, newItemPosition)
            }

        }
    }
}