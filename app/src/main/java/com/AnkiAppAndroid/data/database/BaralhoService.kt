package com.AnkiAppAndroid.data.database

import com.AnkiAppAndroid.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object BaralhoService {

    private val client = OkHttpClient()
    private val json   = Json { ignoreUnknownKeys = true }
    private val JSON   = "application/json".toMediaType()

    private const val BASE = "http://10.0.2.2:8080/baralhos"   // ajuste se mudar host

    /*--------------------------- GET /baralhos ---------------------------*/
    suspend fun getAll(): List<BaralhoBancoDados> = withContext(Dispatchers.IO) {
        val req  = Request.Builder().url(BASE).get().build()
        val body = client.newCall(req).execute().body?.string()
            ?: error("Corpo vazio")

        json.decodeFromString<List<BaralhoDto>>(body)
            .map { it.toDomain() }
    }

    /*--------------------------- GET /baralhos/{id} ----------------------*/
    suspend fun getById(id: String): BaralhoBancoDados = withContext(Dispatchers.IO) {
        val req  = Request.Builder().url("$BASE/$id").get().build()
        val body = client.newCall(req).execute().body?.string()
            ?: error("Corpo vazio")

        json.decodeFromString<BaralhoDto>(body).toDomain()
    }

    /*--------------------------- POST /baralhos --------------------------*/
    suspend fun create(baralho: BaralhoBancoDados): BaralhoBancoDados = withContext(Dispatchers.IO) {
        val bodyReq = json.encodeToString(baralho.toDto())
            .toRequestBody(JSON)

        val req  = Request.Builder().url(BASE).post(bodyReq).build()
        val body = client.newCall(req).execute().body?.string()
            ?: error("Corpo vazio")

        json.decodeFromString<BaralhoDto>(body).toDomain()
    }

    /*--------------------------- PUT /baralhos/{id} ----------------------*/
    suspend fun update(baralho: BaralhoBancoDados): BaralhoBancoDados = withContext(Dispatchers.IO) {
        val bodyReq = json.encodeToString(baralho.toDto())
            .toRequestBody(JSON)

        val req  = Request.Builder()
            .url("$BASE/${baralho.id}")
            .put(bodyReq)
            .build()

        val body = client.newCall(req).execute().body?.string()
            ?: error("Corpo vazio")

        json.decodeFromString<BaralhoDto>(body).toDomain()
    }

    /*--------------------------- DELETE /baralhos/{id} -------------------*/
    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        val req  = Request.Builder().url("$BASE/$id").delete().build()
        val res  = client.newCall(req).execute()
        res.code == 204 || res.code == 200
    }
}