// data/api/FlashcardApiService.kt
package com.AnkiAppAndroid.data.api

import android.util.Log
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import com.AnkiAppAndroid.data.model.api.ApiBaralho
import com.AnkiAppAndroid.data.model.api.ApiResponse

class FlashcardApiService {
    private val baseUrl = "http://10.0.2.2:8080"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun deleteBaralho(baralhoId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = client.delete("$baseUrl/baralhos/$baralhoId") {
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                Result.failure(Exception("Falha ao deletar baralho: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("FlashcardApiService", "Erro ao deletar baralho: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getBaralhos(uuid: String): Result<List<ApiBaralho>> = withContext(Dispatchers.IO) {
        try {
            val response: List<ApiBaralho> = client.get("$baseUrl/baralhos").body()

            Result.success(response)
        } catch (e: Exception) {
            Log.e("FlashcardApiService", "Erro ao buscar baralhos: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getBaralhoById(id: String): Result<ApiBaralho> = withContext(Dispatchers.IO) {
        try {
            val response: ApiBaralho = client.get("$baseUrl/baralhos/$id").body()
            Result.success(response)
        } catch (e: Exception) {
            Log.e("FlashcardApiService", "Erro ao buscar baralho por ID: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateBaralho(id: String, baralho: ApiBaralho): Result<ApiBaralho> = withContext(Dispatchers.IO) {
        try {
            val response: ApiBaralho = client.put("$baseUrl/baralhos/$id") {
                contentType(ContentType.Application.Json)
                setBody(baralho)
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            Log.e("FlashcardApiService", "Erro ao atualizar baralho: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun createBaralho(baralho: ApiBaralho): Result<ApiBaralho> = withContext(Dispatchers.IO) {
        try {
            val response: ApiBaralho = client.post("$baseUrl/baralhos") {
                contentType(ContentType.Application.Json)
                setBody(baralho)
            }.body()

            Result.success(response)
        } catch (e: Exception) {
            Log.e("FlashcardApiService", "Erro ao criar baralho: ${e.message}")
            Result.failure(e)
        }
    }
}

