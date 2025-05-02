package com.AnkiAppAndroid.data.repository

import com.AnkiAppAndroid.data.dao.BaralhoDao
import com.AnkiAppAndroid.data.model.Baralho
import kotlinx.coroutines.flow.Flow

class BaralhoRepository(private val baralhoDao: BaralhoDao) {

    val allBaralhos: Flow<List<Baralho>> = baralhoDao.getAllBaralhos()

    suspend fun insertBaralho(baralho: Baralho): Long {
        return baralhoDao.insertBaralho(baralho)
    }

    suspend fun updateBaralho(baralho: Baralho) {
        baralhoDao.updateBaralho(baralho)
    }

    suspend fun deleteBaralho(baralho: Baralho) {
        baralhoDao.deleteBaralho(baralho)
    }

    suspend fun getBaralhoById(id: Long): Baralho? {
        return baralhoDao.getBaralhoById(id)
    }
}