package com.AnkiAppAndroid.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.AnkiAppAndroid.data.dao.BaralhoDao
import com.AnkiAppAndroid.data.dao.LocalDao
import com.AnkiAppAndroid.data.model.Baralho
import com.AnkiAppAndroid.data.model.Local

@Database(entities = [Baralho::class, Local::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun baralhoDao(): BaralhoDao
    abstract fun localDao(): LocalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "anki_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}