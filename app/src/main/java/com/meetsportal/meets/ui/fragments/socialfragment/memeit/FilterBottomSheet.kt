package com.meetsportal.meets.ui.fragments.socialfragment.memeit

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.iconStart

class FilterBottomSheet(val myContext: Activity, var list: ArrayList<String> = arrayListOf("All", "Confirmed", "Un-Confirmed", "Cancelled"), var dismissListener: (Int) -> Unit) : BottomSheetDialog(myContext, R.style.BottomSheetDialog) {

    private var adapter: FilterAdapter

    val drawable: ArrayList<Int> = arrayListOf()
    fun setDrawables(drawables: ArrayList<Int>) {
        drawable.addAll(drawables)
    }

    fun setArrayList(list: ArrayList<String>) {
        this.list.clear()
        this.list.addAll(list)
    }

    init {
        val inflate = layoutInflater.inflate(R.layout.bottomsheet_common, null)
        setContentView(inflate)
        val recyclerView: RecyclerView = inflate.findViewById(R.id.rvFilterItems)
        val tvCancel: TextView = inflate.findViewById(R.id.tvCancel)
        adapter = FilterAdapter(myContext, list, drawable) {
            dismissListener(it)
            dismiss()
        }
        recyclerView.adapter = adapter
        tvCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun show() {
        adapter.notifyDataSetChanged()
        super.show()
    }

    class FilterAdapter(var myContext: Context, val list: ArrayList<String>, val drawables: ArrayList<Int>, var clickListener: (Int) -> Unit) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

        var lastPosition: Int = -1
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_filter, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            if(drawables.isNotEmpty()) {
                holder.tvFilter.iconStart(drawables[position])
            }
            holder.tvFilter.text = item
            holder.itemView.setOnClickListener {
                lastPosition = position
                clickListener(lastPosition)
            }

        }

        override fun getItemCount(): Int {
            return list.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            var tvFilter: TextView = itemView.findViewById(R.id.tvFilterText)
        }
    }
}