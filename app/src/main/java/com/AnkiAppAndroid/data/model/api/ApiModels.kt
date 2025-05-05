package com.AnkiAppAndroid.data.model.api

import kotlinx.serialization.Serializable

@Serializable
data class ApiCard(
    val topico: String,
    val tipo: String,
    val pergunta: String,
    val resposta: Int,
    val alternativas: List<String>,
    val localizacao: String?,
    val proxima_revisao: String
)

@Serializable
data class ApiBaralho(
    val id: String,
    val titulo: String,
    val cartas: List<ApiCard>,
    val id_usuario: String,

)

@Serializable
data class ApiResponse<T>(
    val data: T,
    val message: String? = null,
    val error: String? = null
)