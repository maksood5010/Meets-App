package com.meetsportal.meets.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meetsportal.meets.R


public class ColorPickerAdapter(var context: Context):
    RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {

    private var inflater: LayoutInflater? = null
    private var onColorPickerClickListener: OnColorPickerClickListener? =null

    private var colorPickerColors: ArrayList<Int>? = null

    constructor(context: Context, colorPickerColors: ArrayList<Int>) : this(context) {
        this.colorPickerColors = colorPickerColors
    }

    init {
        this.colorPickerColors = getDefaultColors(context)
        inflater = LayoutInflater.from(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)= ViewHolder(
        inflater?.inflate(R.layout.color_picker_item_list, parent, false)!!
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.colorPickerView.setBackgroundColor(colorPickerColors!![position])
        holder.colorPickerView.setOnClickListener {
            onColorPickerClickListener?.let{
                it.onColorPickerClickListener(colorPickerColors?.get(position)!!)
            }
        }
    }

    override fun getItemCount() =  colorPickerColors?.size!!

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var colorPickerView = itemView.findViewById<View>(R.id.color_picker_view)

    }

    interface OnColorPickerClickListener {
        fun onColorPickerClickListener(colorCode: Int)
    }

    fun setOnColorPickerClickListener(onColorPickerClickListener: OnColorPickerClickListener?) {
        this.onColorPickerClickListener = onColorPickerClickListener
    }



    fun getDefaultColors(context: Context?): ArrayList<Int>? {
        val colorPickerColors = ArrayList<Int>()
        colorPickerColors.add(ContextCompat.getColor(context!!, R.color.blue_color_picker))
        colorPickerColors.add(ContextCompat.getColor(context, R.color.brown_color_picker))
        colorPickerColors.add(ContextCompat.getColor(context, R.color.green_color_picker))
        colorPickerColors.add(ContextCompat.getColor(context, R.color.orange_color_picker))
        colorPickerColors.add(ContextCompat.getColor(context, R.color.red_color_picker))
        colorPickerColors.add(ContextCompat.getColor(context, R.color.black))
        colorPickerColors.add(ContextCompat.getColor(context, R.color.red_orange_color_picker))
        colorPickerColors.add(ContextCompat.getColor(context, R.color.sky_blue_color_picker))
        colorPickerColors.add(ContextCompat.getColor(context, R.color.violet_color_picker))
        colorPickerColors.add(ContextCompat.getColor(context, R.color.white))
        colorPickerColors.add(ContextCompat.getColor(context, R.color.yellow_color_picker))
        colorPickerColors.add(ContextCompat.getColor(context, R.color.yellow_green_color_picker))
        return colorPickerColors
    }



}