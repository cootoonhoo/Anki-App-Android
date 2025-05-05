// data/service/BaralhoSyncService.kt
package com.AnkiAppAndroid.data.service

import com.AnkiAppAndroid.data.api.FlashcardApiService
import com.AnkiAppAndroid.data.dao.BaralhoDao
import com.AnkiAppAndroid.data.model.Baralho
import com.AnkiAppAndroid.data.model.api.ApiBaralho
import com.AnkiAppAndroid.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BaralhoSyncService(
    private val apiService: FlashcardApiService,
    private val baralhoDao: BaralhoDao,
    private val usuarioRepository: UsuarioRepository
) {
    // Método para sincronizar baralhos da API
    suspend fun syncBaralhos(): Flow<Result<List<Baralho>>> = flow {
        try {
            val uuid = usuarioRepository.getOrCreateUsuarioUuid()
            val apiResult = apiService.getBaralhos(uuid)

            if (apiResult.isSuccess) {
                val apiBaralhos = apiResult.getOrThrow()
                val localBaralhos = updateLocalBaralhosFromApi(apiBaralhos)
                emit(Result.success(localBaralhos))
            } else {
                emit(Result.failure(apiResult.exceptionOrNull() ?: Exception("Falha ao sincronizar")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Método para deletar baralho local e na API
    suspend fun deleteBaralho(baralho: Baralho): Result<Boolean> {
        try {
            // Deletar localmente
            baralhoDao.deleteBaralho(baralho)

            // Deletar na API se tiver ID do MongoDB
            val mongoId = baralho.mongoId
            if (mongoId != null) {
                val apiResult = apiService.deleteBaralho(mongoId)
                if (apiResult.isFailure) {
                    return Result.failure(apiResult.exceptionOrNull() ?: Exception("Falha ao deletar na API"))
                }
            }

            return Result.success(true)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    // Método auxiliar para atualizar baralhos locais com dados da API
    private suspend fun updateLocalBaralhosFromApi(apiBaralhos: List<ApiBaralho>): List<Baralho> {
        val currentBaralhos = baralhoDao.getAllBaralhosSync()
        val existingBaralhosByMongoId = currentBaralhos
            .filter { it.mongoId != null }
            .associateBy { it.mongoId!! }

        val updatedBaralhos = mutableListOf<Baralho>()

        // Processar cada baralho da API
        for (apiBaralho in apiBaralhos) {
            val titulo = apiBaralho.titulo
            val existingBaralho = existingBaralhosByMongoId[apiBaralho.id]

            if (existingBaralho != null) {
                // Atualizar baralho existente
                val updatedBaralho = existingBaralho.copy(titulo = titulo)
                baralhoDao.updateBaralho(updatedBaralho)
                updatedBaralhos.add(updatedBaralho)
            } else {
                // Adicionar novo baralho
                val newBaralho = Baralho(
                    id = 0,
                    titulo = titulo,
                    mongoId = apiBaralho.id
                )
                val id = baralhoDao.insertBaralho(newBaralho)
                updatedBaralhos.add(newBaralho.copy(id = id))
            }
        }

        // Remover baralhos que não existem mais na API
        val apiBaralhoIds = apiBaralhos.map { it.id }.toSet()
        for (localBaralho in currentBaralhos) {
            if (localBaralho.mongoId != null && localBaralho.mongoId !in apiBaralhoIds) {
                baralhoDao.deleteBaralho(localBaralho)
            }
        }

        return updatedBaralhos
    }
}