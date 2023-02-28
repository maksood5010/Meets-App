package com.meetsportal.meets.models

import android.os.Parcelable
import android.util.Log
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

class AddressModelResponse:ArrayList<AddressModel>()

@Parcelize
data class AddressModel (
    @SerializedName("_id")
    var _id: String?  = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("address")
    var address: String? = null,
//    var lat: Double? = null,
//    var lon: Double? = null,
    @SerializedName("location")
    var location: @RawValue AddressLocation? = null,
    @SerializedName("country_code")
    var country_code: String? = null,
    @SerializedName("image_url")
    var image_url: String? = null,
    @SerializedName("createdAt")
    var createdAt: String? = null,
    @SerializedName("updatedAt")
    var updatedAt : String? = null,
    @SerializedName("selected")
    var selected : Boolean? = false,
    @SerializedName("city")
    var city : String? = null,
    @SerializedName("country")
    var country : String? = null,
    @SerializedName("custom_uid")
    var custom_uid : String? = null,
):Parcelable
{
    fun setLocation(lat: Double?,lon: Double?){
        lat?.let{ la: Double ->
            lon?.let{ lo: Double ->
                location = AddressLocation(arrayListOf(la, lo))
            }?:run{
                Log.d("TAG", "Error : setLocation: longitude is null ")
            }
        }?:run{
            Log.d("TAG", "Error : setLocation: latitude is null ")
        }
    }
    fun getLatitude(): Double {
        return location?.coordinates?.get(0)?: 0.0
    }
    fun getLongitude(): Double {
        return location?.coordinates?.get(1)?: 0.0
    }

    /*fun isItSame(address:AddressModel?):Boolean{
        address?.let {
            if(it.name.equals(address.name) && it.createdAt)
        }?:run{
            return false
        }

    }*/
}


data class AddressLocation(
    @SerializedName("coordinates")
    var coordinates: List<Double> = arrayListOf(),
    @SerializedName("type")
    val type: String= "point"
                       )
//data class CustomLocation(
//    val coordinates: List<Double?>?,
//    val type: String
//                       )