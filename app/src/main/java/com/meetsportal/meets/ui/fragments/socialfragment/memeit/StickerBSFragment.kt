package com.meetsportal.meets.ui.fragments.socialfragment.memeit

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R

class StickerBSFragment() : BottomSheetDialogFragment() {

    private var mStickerListener: StickerListener? = null

    fun setStickerListener(stickerListener: StickerListener) {
        mStickerListener = stickerListener
    }

    interface StickerListener {

        fun onStickerClick(bitmap: Bitmap?)
    }

    var mBottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if(newState == BottomSheetBehavior.STATE_HIDDEN) dismiss()
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.fragment_bottom_sticker_emoji_dialog, null)
        dialog.setContentView(contentView)
        val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior
        if(behavior != null && behavior is BottomSheetBehavior) {
            behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
        val rvEmoji: RecyclerView = contentView.findViewById(R.id.rvEmoji)
        val gridLayoutManager = GridLayoutManager(activity, 3)
        rvEmoji.layoutManager = gridLayoutManager
        val stickerAdapter: StickerAdapter = StickerAdapter()
        rvEmoji.adapter = stickerAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    inner class StickerAdapter : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {

        var stickerList = intArrayOf(R.drawable.aa, R.drawable.bb)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_sticker, parent, false))

        override fun onBindViewHolder(holder: StickerAdapter.ViewHolder, position: Int) {
            holder.imgSticker.setImageResource(stickerList[position])
        }

        override fun getItemCount() = stickerList.size


        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            var imgSticker: ImageView = itemView.findViewById(R.id.imgSticker)

            init {
                itemView.setOnClickListener {
                    mStickerListener?.let {
                        it.onStickerClick(BitmapFactory.decodeResource(resources, stickerList[layoutPosition]))
                    }
                    dismiss()
                }
            }

        }

    }
}