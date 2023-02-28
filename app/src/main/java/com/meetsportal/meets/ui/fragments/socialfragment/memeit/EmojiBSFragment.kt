package com.meetsportal.meets.ui.fragments.socialfragment.memeit

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import ja.burhanrashid52.photoeditor.PhotoEditor

class EmojiBSFragment : BottomSheetDialogFragment() {

    public var mEmojiListener: EmojiListener? = null

    interface EmojiListener {

        fun onEmojiClick(emojiUnicode: String?)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    var mBottomSheetBehaviorCallback = object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if(newState == STATE_HIDDEN) dismiss()
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {

        }
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        var contentView = View.inflate(context, R.layout.fragment_bottom_sticker_emoji_dialog, null)
        dialog.setContentView(contentView)
        var params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior
        if(behavior != null && behavior is BottomSheetBehavior) {
            (behavior as BottomSheetBehavior).setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
        val rvEmoji: RecyclerView = contentView.findViewById(R.id.rvEmoji)
        val gridLayoutManager = GridLayoutManager(activity, 5)
        rvEmoji.layoutManager = gridLayoutManager
        var emojiAdapter = EmojiAdapter(requireActivity())
        rvEmoji.adapter = emojiAdapter

    }

    fun setEmojiListener(emojiListener: EmojiListener) {
        mEmojiListener = emojiListener
    }

    inner class EmojiAdapter(var context: Context) : RecyclerView.Adapter<EmojiAdapter.ViewHolder>() {

        var emojisList = PhotoEditor.getEmojis(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_emoji, parent, false))

        override fun onBindViewHolder(holder: EmojiAdapter.ViewHolder, position: Int) {
            holder.txtEmoji.text = emojisList[position]

        }

        override fun getItemCount() = emojisList.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            var txtEmoji: TextView

            init {
                txtEmoji = itemView.findViewById(R.id.txtEmoji)
                itemView.setOnClickListener {
                    mEmojiListener?.let {
                        it.onEmojiClick(emojisList.get(layoutPosition))
                    }
                    dismiss()
                }
            }


        }

    }
}