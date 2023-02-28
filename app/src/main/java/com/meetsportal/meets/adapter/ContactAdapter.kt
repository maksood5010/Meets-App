package com.meetsportal.meets.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hbb20.CountryCodePicker
import com.meetsportal.meets.R
import com.meetsportal.meets.database.entity.Contact
import com.meetsportal.meets.networking.profile.ProfileGetResponse
import com.meetsportal.meets.utils.Constant
import com.meetsportal.meets.utils.PreferencesManager
import com.meetsportal.meets.utils.onClick
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.card_contact.view.*

class ContactAdapter(
    var context: Context,
    var list: ArrayList<Contact>?,
    var listener: (Int) -> Unit
) :
    RecyclerView.Adapter<ContactAdapter.RviewHolder>() {

    val differ: AsyncListDiffer<Contact> = AsyncListDiffer<Contact>(this, ContactAdapter.DIFF_CALLBACK)


    var previousletter = ""
    var searchedlist: ArrayList<Contact> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_contact, parent, false)
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        Log.d("TAG", "onBindViewHolder:currentList ${differ.currentList.size}")
        differ.currentList.getOrNull(position)?.let {
            Log.i("TAG"," hdbcbdc::: ${searchedlist?.size}")
           if(searchedlist.isNotEmpty() == true){
               if(searchedlist.filter { filter->
                       Log.i("TAG"," filterId:: ${filter.id} --- ${it.id}")
                       filter.id == it.id
               }.isNotEmpty()){
                   Log.i("TAG"," hdbcbdc::: 6")
                   holder.root.visibility = View.VISIBLE
               }
               else{
                   Log.i("TAG"," hdbcbdc::: 7")
                   holder.root.visibility = View.GONE
                   return@let
               }
           }
            Log.i("TAG"," Onlyhdbcbdc::: 7")
            holder.root.visibility = View.VISIBLE
            it.image?.let { image ->
                holder.image.setImageBitmap(image)
            } ?: run {
                holder.image.setImageResource(R.drawable.ic_default_person)
            }
            it.name?.let { name ->
                holder.name.text = name
                /*if (previousletter.equals(name[0].toString())) {
                    holder.letter.visibility = View.INVISIBLE
                } else {
                    holder.letter.visibility = View.VISIBLE
                    holder.letter.text = name[0].toString()
                    previousletter = name[0].toString()
                }*/

            } ?: run {
                holder.name.text = ""
            }
            it.number?.let { number ->
                holder.number.text = number
            } ?: run {
                holder.number.text = ""
            }
            if (it.selected) {
                holder.check.setImageResource(R.drawable.ic_checked)
            } else {
                holder.check.setImageResource(R.drawable.ic_unchecked)
            }
            holder.itemView.onClick( {
                val item  = differ.currentList.get(position)
                val value = item?.number?.replace(" ","")?.replace("-","")?.get(0) ?: ""
                if(value != '+') {
                    val yourCountryCode = CountryCodePicker(context)
                    val myProfile = PreferencesManager.get<ProfileGetResponse>(Constant.PREFRENCE_PROFILE)
                    yourCountryCode.fullNumber=myProfile?.cust_data?.phone_number?.replaceFirst("+","")
                    if(value=="0"){
                        item?.number=yourCountryCode.selectedCountryCodeWithPlus.plus(item?.number?.replaceFirst("0","")).replace(Regex("[^0-9+]"),"")
                    }else{
                        item?.number=yourCountryCode.selectedCountryCodeWithPlus.plus(item?.number).replace(Regex("[^0-9+]"),"")
                    }
                }
//                Toast.makeText(context, item?.number, Toast.LENGTH_SHORT).show()

                Log.i("TAG", " check.:: setOnClickListener number :: ${item?.number}")
                item?.selected = !item?.selected!!
                if (item?.selected!!) {
                    item?.selectTimestamp = System.currentTimeMillis()
                    holder.check.setImageResource(R.drawable.ic_checked)
                }
                else {
                    holder.check.setImageResource(R.drawable.ic_unchecked)
                }
                listener(position)
            })
        }

    }

    fun setSearchedList(list: List<Contact>) {
        Log.d("TAG", "setSearchedList:list::: ${list.size}")
        differ.submitList(list)
        /*searchedlist?.clear()
        searchedlist?.addAll(list)
        notifyDataSetChanged()*/
    }

    override fun getItemCount(): Int {
        /*list?.let {
            Log.i("TAG"," ContactCount:: ${list?.size}")
            return list?.size!!
        } ?: run {
            return 0
        }*/
        return differ.currentList.size
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
        //var letter = itemView.findViewById<TextView>(R.id.letter)
        var image = itemView.findViewById<CircleImageView>(R.id.civ_image)
        var name = itemView.findViewById<TextView>(R.id.tv_name)
        var number = itemView.findViewById<TextView>(R.id.tv_number)
        var check = itemView.findViewById<ImageView>(R.id.iv_checkbox)
    }

    companion object {

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.id.equals(newItem.id
                                        )
            }

            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
                return oldItem.id.equals(newItem.id)
            }

        }
    }
}