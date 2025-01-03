package com.meetsportal.meets.ui.fragments.socialfragment.memeit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meetsportal.meets.R
import com.meetsportal.meets.adapter.ColorPickerAdapter

class PropertiesBSFragment : BottomSheetDialogFragment(), OnSeekBarChangeListener {

    private var mProperties: Properties? = null

    interface Properties {

        fun onColorChanged(colorCode: Int)
        fun onOpacityChanged(opacity: Int)
        fun onBrushSizeChanged(brushSize: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_properties_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvColor: RecyclerView = view.findViewById(R.id.rvColors)
        val sbOpacity = view.findViewById<SeekBar>(R.id.sbOpacity)
        val sbBrushSize = view.findViewById<SeekBar>(R.id.sbSize)

        sbOpacity.setOnSeekBarChangeListener(this)
        sbBrushSize.setOnSeekBarChangeListener(this)

        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvColor.layoutManager = layoutManager
        //   rvColor.setHasFixedSize(true)

        val colorPickerAdapter = ColorPickerAdapter(requireActivity())
        colorPickerAdapter.setOnColorPickerClickListener(object : ColorPickerAdapter.OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                mProperties?.let {
                    dismiss()
                    it.onColorChanged(colorCode)
                }
            }
        })
        rvColor.adapter = colorPickerAdapter

    }

    fun setPropertiesChangeListener(properties: Properties) {
        mProperties = properties
    }

    override fun onProgressChanged(seekBar: SeekBar?, i: Int, b: Boolean) {
        when(seekBar?.id) {
            R.id.sbOpacity -> {
                mProperties?.let {
                    it.onOpacityChanged(i)
                }
            }

            R.id.sbSize    -> {
                mProperties?.let {
                    it.onBrushSizeChanged(i)
                }
            }
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {

    }

    override fun onStopTrackingTouch(p0: SeekBar?) {

    }
}