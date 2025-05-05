package com.AnkiAppAndroid.data.mapper

import com.AnkiAppAndroid.data.model.Baralho
import com.AnkiAppAndroid.data.model.Card
import com.AnkiAppAndroid.data.model.CardType
import com.AnkiAppAndroid.data.model.api.ApiBaralho
import com.AnkiAppAndroid.data.model.api.ApiCard
import java.util.*

object ApiModelMapper {
    fun mapApiBaralhoToLocal(apiBaralho: ApiBaralho): Baralho {
        return Baralho(
            id = 0, // ID será gerado pelo Room
            titulo = apiBaralho.titulo ?: "Baralho sem título"
        )
    }

    fun mapApiCardToLocal(apiCard: ApiCard): Card {
        return Card(
            topico = apiCard.topico,
            tipo = CardType.valueOf(apiCard.tipo.uppercase()),
            pergunta = apiCard.pergunta,
            resposta = apiCard.resposta,
            alternativas = apiCard.alternativas,
            localizacao = apiCard.localizacao,
            proximaRevisao = apiCard.proxima_revisao
        )
    }

    fun mapLocalBaralhoToApi(baralho: Baralho, cards: List<Card>, userUuid: String): ApiBaralho {
        val apiCards = cards.map { card ->
            ApiCard(
                topico = card.topico,
                tipo = card.tipo.name,
                pergunta = card.pergunta,
                resposta = card.resposta,
                alternativas = card.alternativas,
                localizacao = card.localizacao,
                proxima_revisao = card.proximaRevisao
            )
        }

        return ApiBaralho(
            id = baralho.id.toString(),
            cartas = apiCards,
            id_usuario = userUuid,
            titulo = baralho.titulo
        )
    }
}