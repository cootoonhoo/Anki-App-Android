// ui/viewmodel/CardsViewModel.kt
package com.AnkiAppAndroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.AnkiAppAndroid.data.model.Card
import com.AnkiAppAndroid.data.model.Difficulty
import com.AnkiAppAndroid.data.repository.BaralhoBackendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CardsViewModel : ViewModel() {

    private val repo = BaralhoBackendRepository()  // GET /baralhos/{id} :contentReference[oaicite:0]{index=0}:contentReference[oaicite:1]{index=1}

    // Lista completa de cartas do baralho
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    // Carta atualmente exibida
    private val _currentCard = MutableStateFlow<Card?>(null)
    val currentCard: StateFlow<Card?> = _currentCard.asStateFlow()

    // Índice da carta na lista
    private val _cardIndex = MutableStateFlow(0)
    val cardIndex: StateFlow<Int> = _cardIndex.asStateFlow()

    // Total de cartas
    private val _totalCards = MutableStateFlow(0)
    val totalCards: StateFlow<Int> = _totalCards.asStateFlow()

    // Controle de resposta e revisão
    private val _selectedAnswer = MutableStateFlow<Int?>(null)
    val selectedAnswer: StateFlow<Int?> = _selectedAnswer.asStateFlow()

    private val _isAnswerRevealed = MutableStateFlow(false)
    val isAnswerRevealed: StateFlow<Boolean> = _isAnswerRevealed.asStateFlow()

    private val _showDifficultyDialog = MutableStateFlow(false)
    val showDifficultyDialog: StateFlow<Boolean> = _showDifficultyDialog.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow<Difficulty?>(null)
    val selectedDifficulty: StateFlow<Difficulty?> = _selectedDifficulty.asStateFlow()

    private val _correctAnswers = MutableStateFlow(0)
    val correctAnswers: StateFlow<Int> = _correctAnswers.asStateFlow()

    private val _showSummary = MutableStateFlow(false)
    val showSummary: StateFlow<Boolean> = _showSummary.asStateFlow()

    /**
     * Fetch all cards for the given deck ID from the backend,
     * then initialize session state.
     */
    fun fetchCards(baralhoId: String) {
        viewModelScope.launch {
            try {
                val baralho = repo.obter(baralhoId)   // GET /baralhos/{id} :contentReference[oaicite:2]{index=2}:contentReference[oaicite:3]{index=3}
                _cards.value = baralho.cartas
                _totalCards.value = baralho.cartas.size
                resetSession()
            } catch (e: Exception) {
                e.printStackTrace()
                // Optionally expose an error StateFlow here
            }
        }
    }

    fun selectAnswer(answerIndex: Int) {
        _selectedAnswer.value = answerIndex
        _isAnswerRevealed.value = true
        _currentCard.value?.let { card ->
            if (answerIndex == card.resposta) {
                _correctAnswers.value += 1
            }
        }
        _showDifficultyDialog.value = true
    }

    fun selectDifficulty(difficulty: Difficulty) {
        _selectedDifficulty.value = difficulty
        _showDifficultyDialog.value = false
    }

    fun nextCard() {
        val next = _cardIndex.value + 1
        if (next < _cards.value.size) {
            _cardIndex.value = next
            _currentCard.value = _cards.value[next]
            _selectedAnswer.value = null
            _isAnswerRevealed.value = false
        } else {
            _currentCard.value = null
            _showSummary.value = true
        }
    }

    fun resetSession() {
        _correctAnswers.value = 0
        _cardIndex.value = 0
        _currentCard.value = _cards.value.firstOrNull()
        _selectedAnswer.value = null
        _isAnswerRevealed.value = false
        _selectedDifficulty.value = null
        _showSummary.value = false
    }
}
