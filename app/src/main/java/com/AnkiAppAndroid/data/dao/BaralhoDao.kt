package com.AnkiAppAndroid.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.AnkiAppAndroid.data.model.Baralho
import kotlinx.coroutines.flow.Flow

@Dao
interface BaralhoDao {
    @Query("SELECT * FROM baralhos ORDER BY titulo ASC")
    fun getAllBaralhos(): Flow<List<Baralho>>

    @Insert
    suspend fun insertBaralho(baralho: Baralho): Long

    @Update
    suspend fun updateBaralho(baralho: Baralho)

    @Delete
    suspend fun deleteBaralho(baralho: Baralho)

    @Query("SELECT * FROM baralhos WHERE id = :id")
    suspend fun getBaralhoById(id: Long): Baralho?
}