package com.meetsportal.meets.overridelayout.mention

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MentionPerson(
    @SerializedName("alt_id")
    var alt_id: String? = null,
    @SerializedName("type")
    var type : String? = "user",
    @SerializedName("id")
    var id : String? = null,
    @SerializedName("id_type")
    var id_type : String? = "sid"
): Parcelable {


    fun getFormattedValue(): String? {
        return "@[$alt_id]($id)"
    }
}
