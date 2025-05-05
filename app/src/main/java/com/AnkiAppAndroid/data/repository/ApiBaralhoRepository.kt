// data/repository/ApiBaralhoRepository.kt
package com.AnkiAppAndroid.data.repository

import com.AnkiAppAndroid.data.api.FlashcardApiService
import com.AnkiAppAndroid.data.dao.BaralhoDao
import com.AnkiAppAndroid.data.model.Baralho
import com.AnkiAppAndroid.data.model.Card
import com.AnkiAppAndroid.data.model.CardType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ApiBaralhoRepository(
    private val apiService: FlashcardApiService,
    private val baralhoDao: BaralhoDao,
    private val usuarioRepository: UsuarioRepository
) {
    suspend fun syncBaralhos(): Flow<List<Baralho>> = flow {
        val uuid = usuarioRepository.getOrCreateUsuarioUuid()
        val result = apiService.getBaralhos(uuid)

        if (result.isSuccess) {
            val apiBaralhos = result.getOrThrow()

            val currentBaralhos = baralhoDao.getAllBaralhosSync()
            for (baralho in currentBaralhos) {
                baralhoDao.deleteBaralho(baralho)
            }

            // Converter os baralhos da API para o modelo local
            val localBaralhos = apiBaralhos.map { apiBaralho ->
                val titulo = apiBaralho.titulo
                Baralho(
                    id = 0, // O Room vai gerar um novo ID
                    titulo = titulo
                )
            }

            val insertedBaralhos = localBaralhos.map { baralho ->
                val id = baralhoDao.insertBaralho(baralho)
                baralho.copy(id = id)
            }

            emit(insertedBaralhos)
        } else {
            emit(baralhoDao.getAllBaralhosSync())
        }
    }

    // Outros métodos conforme necessário
}