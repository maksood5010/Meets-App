package com.meetsportal.meets.database.dao

import androidx.room.*
import com.meetsportal.meets.database.entity.Pinned

@Dao
interface PinnedDao {

    @Insert
    fun insert(pin : Pinned)

    @Update
    fun update(pin: Pinned)

    @Query("delete FROM pinned WHERE sid = :sid")
    fun delete(sid: String)

    @Query("delete from pinned")
    fun deleteAllNotes()

    @Query("select * from pinned order by time desc")
    fun getAllNotes(): List<Pinned>
}