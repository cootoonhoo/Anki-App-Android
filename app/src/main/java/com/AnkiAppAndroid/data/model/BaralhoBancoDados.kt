package com.AnkiAppAndroid.data.model

import java.util.UUID

data class BaralhoBancoDados(
    val id: String,
    val titulo: String,
    val cartas: MutableList<Card>,
    val idUsuario: UUID
)