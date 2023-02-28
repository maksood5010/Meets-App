package com.meetsportal.meets.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ItemModel (

    @SerializedName("key")
    var key: String?  = null,

    @SerializedName("name")
    var name: String? = null,

    @SerializedName("hide")
    var hide: Boolean? = false,
):Parcelable{
    fun getAsList(list:ArrayList<String>): ArrayList<ItemModel> {
        val myOptions: java.util.ArrayList<ItemModel> = arrayListOf()

        list.forEach{
            val itemModel = ItemModel(it, it)
            myOptions.add(itemModel)
        }
        return myOptions
    }
}