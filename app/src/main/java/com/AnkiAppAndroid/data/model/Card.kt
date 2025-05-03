package com.AnkiAppAndroid.data.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Card(
    val topico: String,
    val tipo: CardType,
    val pergunta: String,
    val resposta: Int,
    val alternativas: List<String>,
    val localizacao: String?,
    val proximaRevisao: String
)

enum class CardType {
    MULTIPLE_CHOICE
}

data class BaralhoBandoDados @OptIn(ExperimentalUuidApi::class) constructor(
    val cartas: List<Card>,
    val idUsuario: Uuid
)