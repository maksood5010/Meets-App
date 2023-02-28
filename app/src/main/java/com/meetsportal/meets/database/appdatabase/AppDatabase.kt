package com.meetsportal.meets.database.appdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.meetsportal.meets.database.entity.Pinned
import androidx.sqlite.db.SupportSQLiteDatabase

import com.meetsportal.meets.database.dao.ContactDao
import com.meetsportal.meets.database.dao.PinnedDao
import com.meetsportal.meets.database.entity.Contact
import com.meetsportal.meets.database.entity.ContactFTS


@Database(exportSchema = false,entities = arrayOf(Pinned::class,Contact::class,ContactFTS::class),version = 2 )
abstract class AppDatabase : RoomDatabase() {


    abstract fun pinDao():PinnedDao
    abstract fun contactDao():ContactDao

    companion object{
        private var instance: AppDatabase? = null
        val DATABASE_NAME = "MeetDataBase.db"

        @Synchronized
        fun getInstance(ctx: Context): AppDatabase {
            if(instance == null)
                instance = Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build()

            return instance!!

        }

        private val roomCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                //populateDatabase(instance!!)
            }


        }

    }


}