package com.meetsportal.meets.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class Offer(
    @SerializedName("id")
    val id: String?,

    @SerializedName("subtitle")
    val subtitle: String?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("buttonLabel")
    val buttonLabel: String?,

    @SerializedName("imageUrl")
    val imageUrl: String?,

    @SerializedName("restaurantId")
    val restaurantId: String?,

    @SerializedName("startDate")
    val startDate: Date?,

    @SerializedName("endDate")
    val endDate: Date?,

    @SerializedName("code")
    val code: String?,

    @SerializedName("isActive")
    val isActive: Boolean?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("conditions")
    val conditions: String?,

    @SerializedName("descriptions")
    val descriptions: String?

                 )