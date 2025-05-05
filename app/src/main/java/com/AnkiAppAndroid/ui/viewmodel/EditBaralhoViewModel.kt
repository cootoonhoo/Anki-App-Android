// ui/viewmodel/EditBaralhoViewModel.kt
package com.AnkiAppAndroid.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AnkiAppAndroid.data.api.FlashcardApiService
import com.AnkiAppAndroid.data.database.AppDatabase
import com.AnkiAppAndroid.data.model.Card
import com.AnkiAppAndroid.data.model.CardType
import com.AnkiAppAndroid.data.model.BaralhoBancoDados
import com.AnkiAppAndroid.data.model.api.ApiBaralho
import com.AnkiAppAndroid.data.model.api.ApiCard
import com.AnkiAppAndroid.data.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditBaralhoViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = FlashcardApiService()
    private val usuarioRepository: UsuarioRepository

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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentBaralhoId = MutableStateFlow<String?>(null)
    val currentBaralhoId: StateFlow<String?> = _currentBaralhoId.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        usuarioRepository = UsuarioRepository(database.usuarioDao())
    }

    fun loadBaralhoById(mongoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = apiService.getBaralhoById(mongoId)
                if (result.isSuccess) {
                    val apiBaralho = result.getOrThrow()
                    _currentBaralhoId.value = apiBaralho.id

                    // Converter ApiBaralho para BaralhoBancoDados
                    val cards = apiBaralho.cartas.map { apiCard ->
                        Card(
                            topico = apiCard.topico,
                            tipo = when(apiCard.tipo.uppercase()) {
                                "MULTIPLE_CHOICE" -> CardType.MULTIPLE_CHOICE
                                else -> CardType.MULTIPLE_CHOICE
                            },
                            pergunta = apiCard.pergunta,
                            resposta = apiCard.resposta,
                            alternativas = apiCard.alternativas,
                            localizacao = apiCard.localizacao,
                            proximaRevisao = apiCard.proxima_revisao
                        )
                    }

                    val userUuid = apiBaralho.id_usuario
                    _mongoBaralho.value = BaralhoBancoDados(
                        cartas = cards.toMutableList(),
                        idUsuario = UUID.fromString(userUuid),
                        idBaralho = apiBaralho.id,
                        titulo = apiBaralho.titulo
                    )
                } else {
                    _snackbarMessage.value = "Erro ao carregar baralho: ${result.exceptionOrNull()?.message}"
                    loadMockBaralho()
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "Erro ao carregar baralho: ${e.message}"
                // Carrega mock como fallback
                loadMockBaralho()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMockBaralho() {
        val mockBaralho = BaralhoBancoDados(
            titulo = "Baralho de Teste",
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
            idBaralho = "6816f5d4edf01e426694e63f"
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
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val userUuid = usuarioRepository.getOrCreateUsuarioUuid()

                    // Converter para o formato da API
                    val apiCards = baralho.cartas.map { card ->
                        ApiCard(
                            topico = card.topico,
                            tipo = "multipleChoice",
                            pergunta = card.pergunta,
                            resposta = card.resposta,
                            alternativas = card.alternativas,
                            localizacao = card.localizacao,
                            proxima_revisao = card.proximaRevisao
                        )
                    }

                    val apiBaralho = ApiBaralho(
                        id = _currentBaralhoId.value ?: "", // Usar ID existente ou vazio para novo
                        cartas = apiCards,
                        id_usuario = userUuid,
                        titulo = baralho.titulo
                    )

                    val result = if (_currentBaralhoId.value != null) {
                        // Atualizar baralho existente
                        apiService.updateBaralho(_currentBaralhoId.value!!, apiBaralho)
                    } else {
                        // Criar novo baralho
                        apiService.createBaralho(apiBaralho)
                    }

                    if (result.isSuccess) {
                        _snackbarMessage.value = "Baralho salvo com sucesso!"
                        _saveEvent.value = true

                        // Atualizar o ID do baralho se for um novo baralho
                        if (_currentBaralhoId.value == null) {
                            _currentBaralhoId.value = result.getOrThrow().id
                        }
                    } else {
                        _snackbarMessage.value = "Erro ao salvar baralho: ${result.exceptionOrNull()?.message}"
                    }
                } catch (e: Exception) {
                    _snackbarMessage.value = "Erro ao salvar baralho: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
}