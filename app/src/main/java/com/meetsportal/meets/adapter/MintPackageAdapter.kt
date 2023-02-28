package com.meetsportal.meets.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import com.meetsportal.meets.R
import com.meetsportal.meets.utils.onClick

class MintPackageAdapter(
    var context: Activity,
    var skuList: ArrayList<SkuDetails>?,
    var skuDetailss : (Int)->Unit
) : RecyclerView.Adapter<MintPackageAdapter.RviewHolder>() {


    val TAG = MintPackageAdapter::class.simpleName
    //val array = arrayOf<Boolean>(false, true, false, false, false, false, false, false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RviewHolder {
        return RviewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mint_package, parent, false))
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: RviewHolder, position: Int) {
        /*if(array[position]) {
            holder.bottomBg.setRoundedColorBackground(context, corner = 0f, enbleDash = false, color = Color.parseColor("#42B05E"))
        } else {
            holder.bottomBg.setRoundedColorBackground(context, corner = 0f, enbleDash = false, color = Color.parseColor("#73D399"))
        }*/
//        holder.cvMintPackage.setRoundedColorBackground(context, corner = 20f, enbleDash = false, color = R.color.white)
        Log.i(TAG,"${skuList?.getOrNull(position)?.originalJson}")
        holder.tvMint.text = skuList?.getOrNull(position)?.title?.filter { it.isDigit() }
        holder.tvPrice.text = skuList?.getOrNull(position)?.price//?.plus(skuList?.getOrNull(position)?.priceCurrencyCode)
        holder.bottomBg.onClick({

            skuDetailss(position)
        },500)
    }

    override fun getItemCount(): Int {
        return skuList?.size?:0
    }

    fun setData(value: List<SkuDetails>?) {
       //value?.sortedBy { it.price }


        value?.sortedBy { it.priceAmountMicros }?.let{
            Log.i(TAG," checkingSorted::: ${it}")
            skuList?.clear()
            skuList?.addAll(it)
            notifyDataSetChanged()
        }
    }

   /* fun setSelected(lastCheckedPos: Int) {
        if(lastCheckedPos != -1) {
            for((index, b) in array.withIndex()) {
                array[index] = false
            }
            array[lastCheckedPos] = true
            notifyDataSetChanged()
        }
    }*/

    class RviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var bottomBg = itemView.findViewById<LinearLayout>(R.id.bottomBg)
        var tvPrice = itemView.findViewById<TextView>(R.id.tvPrice)
        var tvMint = itemView.findViewById<TextView>(R.id.tvMint)
        var cvMintPackage = itemView.findViewById<CardView>(R.id.cvMintPackage)
    }
}
