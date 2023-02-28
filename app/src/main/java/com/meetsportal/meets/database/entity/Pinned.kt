package com.meetsportal.meets.database.entity

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "pinned")
data class Pinned (

    @PrimaryKey val sid : String,

    @NonNull
    val time : Long,

    )

