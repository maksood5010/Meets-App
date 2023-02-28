package com.meetsportal.meets.database.dao

import androidx.room.*
import com.meetsportal.meets.database.entity.Contact

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contact : Contact)

    @Update
    fun update(contact: Contact)

    @Query("delete FROM contact WHERE id = :id")
    fun delete(id: String)

    @Query("delete from contact")
    fun deleteAllNotes()

    @Query("select * from contact order by name desc")
    fun getAllContacts(): List<Contact>

    @Query("""
  SELECT *
  FROM contact
  JOIN contact_fts ON contact.name = contact_fts.name
  WHERE contact_fts MATCH :query
  """)
    fun search(query: String): List<Contact>

}