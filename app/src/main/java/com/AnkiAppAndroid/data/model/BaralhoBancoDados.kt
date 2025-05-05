package com.AnkiAppAndroid.data.model

import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi

data class BaralhoBancoDados @OptIn(ExperimentalUuidApi::class) constructor(
    val titulo: String,
    val cartas: MutableList<Card>,
    val idUsuario: UUID,
    val idBaralho: String
)