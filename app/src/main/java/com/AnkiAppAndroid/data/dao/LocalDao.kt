package com.AnkiAppAndroid.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.AnkiAppAndroid.data.model.Local
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalDao {
    @Query("SELECT * FROM locais ORDER BY nome ASC")
    fun getAllLocais(): Flow<List<Local>>

    @Insert
    suspend fun insertLocal(local: Local): Long

    @Update
    suspend fun updateLocal(local: Local)

    @Delete
    suspend fun deleteLocal(local: Local)

    @Query("SELECT * FROM locais WHERE id = :id")
    suspend fun getLocalById(id: Long): Local?
} 