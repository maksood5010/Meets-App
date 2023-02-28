package com.meetsportal.meets.ui.fragments.socialfragment.memeit

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meetsportal.meets.R
import com.meetsportal.meets.models.ItemModel
import com.meetsportal.meets.utils.iconStart

class NewBottomSheet(val myContext: Activity, var list: ArrayList<ItemModel> = arrayListOf(), var dismissListener: (String?) -> Unit) : BottomSheetDialog(myContext, R.style.BottomSheetDialog) {

    private var adapter: FilterAdapter
    var allList: ArrayList<ItemModel> = arrayListOf()

    val drawable: ArrayList<Int> = arrayListOf()
    var hidingOption = arrayListOf<Int>()
    fun setDrawables(drawables: ArrayList<Int>) {
        drawable.addAll(drawables)
    }

    fun setArrayList(list: ArrayList<ItemModel>) {
        this.list.clear()
        this.list.addAll(list)
    }

    fun hideOption(ar: Array<Int>) {
//        list.clear()
//        list.addAll(allList)
        Log.d("TAG", "hideOption:list ${list.size} ")
        list.forEach {
            it.hide = false
        }
        ar.forEach {
            list.get(it).hide = true
//            if(it<list.size-1) {
//                Log.d("TAG", "hideOption:forEach $it ")
//                list.get(it).hide=true
//            }
        }
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

    class FilterAdapter(var myContext: Context, val list: ArrayList<ItemModel>, val drawables: ArrayList<Int>, var clickListener: (String?) -> Unit) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

        var lastPosition: Int = -1
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_filter, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            if(item.hide == true) {
                holder.tvFilter.visibility = View.GONE
                holder.divider.visibility = View.GONE
            } else {
                holder.tvFilter.visibility = View.VISIBLE
                holder.divider.visibility = View.VISIBLE
            }
            if(drawables.isNotEmpty()) {
                holder.tvFilter.iconStart(drawables[position])
            }
            holder.tvFilter.text = item.name
            holder.tvFilter.setOnClickListener {
                lastPosition = position
                clickListener(item.key)
            }

        }

        override fun getItemCount(): Int {
            return list.size
        }

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val tvFilter: TextView = itemView.findViewById(R.id.tvFilterText)
            val divider: View = itemView.findViewById(R.id.divider)
        }
    }
}