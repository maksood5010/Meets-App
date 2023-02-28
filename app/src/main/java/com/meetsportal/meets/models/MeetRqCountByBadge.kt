package com.meetsportal.meets.models

import android.util.Log
import com.google.gson.annotations.SerializedName

data class MeetRqCountByBadge(

    @SerializedName("counts")
    val counts: Counts,

    @SerializedName("divisions")
    val divisions: Divisions
)

data class Counts(

    @SerializedName("alexandrite")
    val alexandrite: Int,

    @SerializedName("black_opal")
    val black_opal: Int,

    @SerializedName("blue_diamond")
    val blue_diamond: Int,

    @SerializedName("bronze")
    val bronze: Int,

    @SerializedName("emerald")
    val emerald: Int,

    @SerializedName("gold")
    val gold: Int,

    @SerializedName("jadeite")
    val jadeite: Int,

    @SerializedName("musgravite")
    val musgravite: Int,

    @SerializedName("pink_diamond")
    val pink_diamond: Int,

    @SerializedName("platinum")
    val platinum: Int,

    @SerializedName("red_beryl")
    val red_beryl: Int,

    @SerializedName("rhodium")
    val rhodium: Int,

    @SerializedName("ruby")
    val ruby: Int,

    @SerializedName("silver")
    val silver: Int,

    @SerializedName("tanzanite")
    val tanzanite: Int
){
    fun printFields() {
        this::class.members.forEach {
                println(it.name)
                Log.d("TAG", "printFields:it.name${it.name} ")
        }
    }
}

data class Divisions(

    @SerializedName("alexandrite")
    val alexandrite: List<Any>,

    @SerializedName("black_opal")
    val black_opal: List<Any>,

    @SerializedName("blue_diamond")
    val blue_diamond: List<Any>,

    @SerializedName("bronze")
    val bronze: List<Any>,

    @SerializedName("emerald")
    val emerald: List<Any>,

    @SerializedName("gold")
    val gold: List<Any>,

    @SerializedName("jadeite")
    val jadeite: List<Any>,

    @SerializedName("musgravite")
    val musgravite: List<Any>,

    @SerializedName("pink_diamond")
    val pink_diamond: List<Any>,

    @SerializedName("platinum")
    val platinum: List<Any>,

    @SerializedName("red_beryl")
    val red_beryl: List<Any>,

    @SerializedName("rhodium")
    val rhodium: List<Any>,

    @SerializedName("ruby")
    val ruby: List<Any>,

    @SerializedName("silver")
    val silver: List<Any>,

    @SerializedName("tanzanite")
    val tanzanite: List<Any>
)