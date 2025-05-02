package com.AnkiAppAndroid.data.repository

import com.AnkiAppAndroid.data.dao.LocalDao
import com.AnkiAppAndroid.data.model.Local
import kotlinx.coroutines.flow.Flow

class LocalRepository(private val localDao: LocalDao) {

    val allLocais: Flow<List<Local>> = localDao.getAllLocais()

    suspend fun insertLocal(local: Local): Long {
        return localDao.insertLocal(local)
    }

    suspend fun updateLocal(local: Local) {
        localDao.updateLocal(local)
    }

    suspend fun deleteLocal(local: Local) {
        localDao.deleteLocal(local)
    }

    suspend fun getLocalById(id: Long): Local? {
        return localDao.getLocalById(id)
    }
} 