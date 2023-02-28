package com.meetsportal.meets.adapter

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.socialfragment.CreatePost

class PostSelectedAdapter(
    var context: Context,var fragment :CreatePost,list: ArrayList<Bitmap>?, var editListener: (Int) -> Unit
) : RecyclerView.Adapter<PostSelectedAdapter.RviewHolder>() {


    var list:ArrayList<Bitmap>?

    init {
        this.list = list
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RviewHolder = RviewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_create_post, parent, false)
    )

    override fun getItemCount():  Int {
        if(list != null) {
            Log.i("TAG","  countListPostCreateAdapter:: ${list?.size} and hashcode ${list.hashCode()}")
            return list?.size!!
        }
        else
            return 0
    }

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        Glide.with(context)
                .asBitmap().placeholder(R.drawable.ic_person_placeholder)
                .load(list!!.get(position)).diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.drawable.ic_person_placeholder)
                .into(holder.image)
//        holder.image.setImageBitmap(list!!.get(position))
        holder.cancel.setOnClickListener {
            this.list?.removeAt(position)
            fragment.list?.removeAt(position)
            this.notifyDataSetChanged()
            fragment.showNoImage()
        }
        holder.Edit.setOnClickListener {
            editListener(position)
        }
    }

    fun updatelist(list: List<Bitmap>?){
        Log.i(" TAG"," initial listcount ${list?.size}")
        this.list?.clear()
        Log.i(" TAG"," clear listcount ${list?.size}")
        this.list?.addAll(list as Collection<Bitmap>)
        Log.i(" TaG"," postCreatelistSend ${this.list?.size} hash ${this.list}")
        this.notifyDataSetChanged()
    }

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var Edit = itemView.findViewById<ImageView>(R.id.bt_edit)
        var cancel = itemView.findViewById<ImageView>(R.id.cv_cancel)
        var image = itemView.findViewById<ImageView>(R.id.iv_image)

    }
}