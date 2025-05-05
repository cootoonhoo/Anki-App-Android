package com.AnkiAppAndroid.data.mapper

import com.AnkiAppAndroid.data.dto.BaralhoDto
import com.AnkiAppAndroid.data.dto.CartaDto
import com.AnkiAppAndroid.data.model.BaralhoBancoDados
import com.AnkiAppAndroid.data.model.Card
import com.AnkiAppAndroid.data.model.CardType
import java.util.UUID

/* ---------- de REMOTE p/ DOMÍNIO ---------- */

fun BaralhoDto.toDomain(): BaralhoBancoDados =
    BaralhoBancoDados(
        id          = id,
        titulo      = titulo,
        cartas      = cartas.map { it.toDomain() }.toMutableList(),
        idUsuario   = UUID.fromString(idUsuario)
    )

fun CartaDto.toDomain(): Card = Card(
    topico         = topico,
    tipo           = CardType.fromBackend(tipo),   // ← trocado
    pergunta       = pergunta,
    resposta       = resposta,
    alternativas   = alternativas,
    localizacao    = localizacao.ifBlank { null },
    proximaRevisao = proximaRevisao
)

/* ---------- de DOMÍNIO p/ REMOTE (POST/PUT) ---------- */

fun BaralhoBancoDados.toDto(): BaralhoDto =
    BaralhoDto(
        id          = id,
        titulo      = titulo,
        cartas      = cartas.map { it.toDto() },
        idUsuario   = idUsuario.toString()
    )

fun Card.toDto(): CartaDto = CartaDto(
    topico         = topico,
    tipo           = tipo.backendValue,            // ← usa o valor que o backend espera
    pergunta       = pergunta,
    resposta       = resposta,
    alternativas   = alternativas,
    localizacao    = localizacao ?: "",
    proximaRevisao = proximaRevisao
)