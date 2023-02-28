package com.meetsportal.meets.adapter

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.palette.graphics.Palette

import androidx.recyclerview.widget.RecyclerView
import com.ablanco.zoomy.Zoomy
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.meetsportal.meets.R
import com.meetsportal.meets.ui.fragments.BaseFragment
import com.meetsportal.meets.ui.fragments.socialfragment.ImageSamplingFragment
import com.meetsportal.meets.utils.Navigation
import java.util.ArrayList


class CardImageDetailPostAdapter(var myContext: Activity,var media: List<String>?) : RecyclerView.Adapter<CardImageDetailPostAdapter.RviewHolder>() {

    init {

        Log.i("TAG", " checkingImageCount::: ${media?.size}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= RviewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.card_image_detail_post, parent, false)
            //return RviewHolder(CardImageDetailPostBinding.inflate(myContext.layoutInflater))
    )

    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        //holder.image.loadImage(myContext,media?.getOrNull(position))
        Glide.with(myContext).asBitmap().placeholder(R.drawable.profile_pic)
            .load(media?.getOrNull(position))
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    Log.e("TAG", "onLoadFailed:asBitmap ")
                    /*val thumb = ThumbnailUtils.createVideoThumbnail(uri.toString(), MediaStore.Video.Thumbnails.MINI_KIND)
                    sendVideoImage(uri, thumb, myDM)*/
                    return true
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: com.bumptech.glide.request.target.Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    Log.i("TAG", "onResourceReady: ${resource}")
                    resource?.let {
                        holder.image.setImageBitmap(it)
                        val builder = Palette.Builder(resource)
                        builder.generate {
                            holder.root.setBackgroundColor(
                                it?.dominantSwatch?.rgb?:
                                it?.lightVibrantSwatch?.rgb?:
                                it?.darkVibrantSwatch?.rgb?:
                                Color.parseColor("#22000000"))
                        }
                    }
                    return true
                }
            })
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .error(R.drawable.ic_default_person)
            .into(holder.image)

        val builder: Zoomy.Builder = Zoomy.Builder(myContext)
            .target(holder.image)
            .enableImmersiveMode(false)
            .animateZooming(false)
            .tapListener {
                var fragment: BaseFragment = ImageSamplingFragment() {
                    myContext.onBackPressed()
                }
                var bundle = Bundle()
                bundle.putInt("position",position)
                bundle.putSerializable("media", ArrayList<String>().apply {
                    addAll(media as Collection<String>)
                })
                fragment.arguments = bundle
                Navigation.addFragment(
                    myContext,
                    fragment,
                    "ImageSamplingFragment",
                    R.id.homeFram,
                    true,
                    false,
                    exitAnim = R.anim.slide_out_bottom
                )
            }
        builder.register()

        //holder.image.text = "change kiya"
    }

    override fun getItemCount()= media?.size?:0

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var root = itemView.findViewById<ConstraintLayout>(R.id.rootCo)
       var image = itemView.findViewById<ImageView>(R.id.image)
    }

}

