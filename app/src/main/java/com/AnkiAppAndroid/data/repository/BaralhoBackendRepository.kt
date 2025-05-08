package com.AnkiAppAndroid.data.repository

import com.AnkiAppAndroid.data.database.BaralhoService
import com.AnkiAppAndroid.data.model.BaralhoBancoDados
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class BaralhoBackendRepository(
    private val service: BaralhoService = BaralhoService          // injete via DI se quiser
) {

    /*----------- LISTAGEM (Flow) -----------*/
    fun listarFlow(): Flow<List<BaralhoBancoDados>> = flow {
        emit(service.getAll())          // dispara GET e emite
    }.flowOn(Dispatchers.IO)

    /*----------- CRUD simples (suspend) ----*/
    suspend fun listar(): List<BaralhoBancoDados> =
        service.getAll()

    suspend fun obter(id: String): BaralhoBancoDados =
        service.getById(id)

    suspend fun criar(baralho: BaralhoBancoDados): BaralhoBancoDados =
        service.create(baralho)

    suspend fun atualizar(baralho: BaralhoBancoDados): BaralhoBancoDados =
        service.update(baralho)

    suspend fun deletar(id: String): Boolean =
        service.delete(id)
}