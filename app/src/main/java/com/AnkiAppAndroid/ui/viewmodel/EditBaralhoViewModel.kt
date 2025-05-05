// ui/viewmodel/EditBaralhoViewModel.kt
package com.AnkiAppAndroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.AnkiAppAndroid.data.model.Card
import com.AnkiAppAndroid.data.model.CardType
import com.AnkiAppAndroid.data.model.BaralhoBancoDados
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class EditBaralhoViewModel : ViewModel() {

    private val _mongoBaralho = MutableStateFlow<BaralhoBancoDados?>(null)
    val mongoBaralho: StateFlow<BaralhoBancoDados?> = _mongoBaralho.asStateFlow()

    private val _showAddCardDialog = MutableStateFlow(false)
    val showAddCardDialog: StateFlow<Boolean> = _showAddCardDialog.asStateFlow()

    private val _editingCard = MutableStateFlow<Card?>(null)
    val editingCard: StateFlow<Card?> = _editingCard.asStateFlow()

    private val _saveEvent = MutableStateFlow<Boolean>(false)
    val saveEvent: StateFlow<Boolean> = _saveEvent.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun loadMockBaralho() {
        val mockBaralho = BaralhoBancoDados(
            cartas = mutableListOf(
                Card(
                    topico = "Física",
                    tipo = CardType.MULTIPLE_CHOICE,
                    pergunta = "Qual é a unidade de medida da força no Sistema Internacional?",
                    resposta = 2,
                    alternativas = listOf("Joule", "Watt", "Newton", "Pascal"),
                    localizacao = "Casa",
                    proximaRevisao = "2025-04-11T19:00:00.000Z"
                )
            ),
            idUsuario = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            titulo = "teste",
            id = "teste"
        )
        _mongoBaralho.value = mockBaralho
    }

    fun showAddCardDialog() {
        _editingCard.value = null
        _showAddCardDialog.value = true
    }

    fun hideAddCardDialog() {
        _showAddCardDialog.value = false
        _editingCard.value = null
    }

    fun editCard(card: Card) {
        _editingCard.value = card
        _showAddCardDialog.value = true
    }

    fun addOrUpdateCard(
        topico: String,
        pergunta: String,
        alternativas: List<String>,
        respostaIndex: Int,
        localizacao: String?
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val currentCard = _editingCard.value

        val newCard = Card(
            topico = topico,
            tipo = CardType.MULTIPLE_CHOICE,
            pergunta = pergunta,
            resposta = respostaIndex,
            alternativas = alternativas,
            localizacao = localizacao,
            proximaRevisao = dateFormat.format(Date())
        )

        _mongoBaralho.value?.let { baralho ->
            val newCartas = baralho.cartas.toMutableList()

            if (currentCard != null) {
                val index = newCartas.indexOf(currentCard)
                if (index != -1) {
                    newCartas[index] = newCard
                }
            } else {
                newCartas.add(newCard)
            }

            _mongoBaralho.value = baralho.copy(cartas = newCartas)
        }

        hideAddCardDialog()
    }

    fun deleteCard(card: Card) {
        _mongoBaralho.value?.let { baralho ->
            val newCartas = baralho.cartas.toMutableList()
            newCartas.remove(card)
            _mongoBaralho.value = baralho.copy(cartas = newCartas)
        }
    }

    fun saveBaralho() {
        _mongoBaralho.value?.let { baralho ->
            // Aqui seria a chamada para o backend
            // Por enquanto, apenas mostra o snackbar
            _snackbarMessage.value = "Salvar o baralho no backend"
            _saveEvent.value = true
        }
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
}