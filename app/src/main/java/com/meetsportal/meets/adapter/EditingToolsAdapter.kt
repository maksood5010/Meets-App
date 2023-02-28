package com.meetsportal.meets.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.models.ToolType

class EditingToolsAdapter(onItemSelected: OnItemSelected): RecyclerView.Adapter<EditingToolsAdapter.ViewHolder>() {

    private var mToolList : ArrayList<ToolModel>  = ArrayList<ToolModel>()
    private var mOnItemSelected : OnItemSelected? = null

    init {
        this.mOnItemSelected = onItemSelected
        /*mToolList.add(ToolModel("Brush", R.drawable.ic_brush, ToolType.BRUSH))
        mToolList.add(ToolModel("Text", R.drawable.ic_text, ToolType.TEXT))
        mToolList.add(ToolModel("Eraser", R.drawable.ic_eraser, ToolType.ERASER))
        mToolList.add(ToolModel("Filter", R.drawable.ic_photo_filter, ToolType.FILTER))
        mToolList.add(ToolModel("Emoji", R.drawable.ic_insert_emoticon, ToolType.EMOJI))
        mToolList.add(ToolModel("Sticker", R.drawable.ic_sticker, ToolType.STICKER))*/

    }

    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType?)
    }

    internal class ToolModel(
        private val mToolName: String,
        private val mToolIcon: Int,
        toolType: ToolType
    ) {
        private val mToolType: ToolType

        init {
            mToolType = toolType
        }
    }



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EditingToolsAdapter.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: EditingToolsAdapter.ViewHolder, position: Int) {
        //TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){

    }

}