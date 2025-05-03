package com.AnkiAppAndroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.AnkiAppAndroid.data.model.Card
import com.AnkiAppAndroid.data.model.CardType
import com.AnkiAppAndroid.data.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CardsViewModel : ViewModel() {
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    private val _currentCard = MutableStateFlow<Card?>(null)
    val currentCard: StateFlow<Card?> = _currentCard.asStateFlow()

    private val _cardIndex = MutableStateFlow(0)
    val cardIndex: StateFlow<Int> = _cardIndex.asStateFlow()

    private val _totalCards = MutableStateFlow(0)
    val totalCards: StateFlow<Int> = _totalCards.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<Int?>(null)
    val selectedAnswer: StateFlow<Int?> = _selectedAnswer.asStateFlow()

    private val _isAnswerRevealed = MutableStateFlow(false)
    val isAnswerRevealed: StateFlow<Boolean> = _isAnswerRevealed.asStateFlow()

    private val _showDifficultyDialog = MutableStateFlow(false)
    val showDifficultyDialog: StateFlow<Boolean> = _showDifficultyDialog.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow<Difficulty?>(null)
    val selectedDifficulty: StateFlow<Difficulty?> = _selectedDifficulty.asStateFlow()

    fun loadMockCards() {
        // Mock data baseado no JSON fornecido
        val mockCards = listOf(
            Card(
                topico = "Física",
                tipo = CardType.MULTIPLE_CHOICE,
                pergunta = "Qual é a unidade de medida da força no Sistema Internacional?",
                resposta = 2,
                alternativas = listOf("Joule", "Watt", "Newton", "Pascal"),
                localizacao = "Casa",
                proximaRevisao = "2025-04-11T19:00:00.000Z"
            ),
            Card(
                topico = "História",
                tipo = CardType.MULTIPLE_CHOICE,
                pergunta = "Em que ano foi proclamada a independência do Brasil?",
                resposta = 1,
                alternativas = listOf("1820", "1822", "1825", "1889"),
                localizacao = null,
                proximaRevisao = "2025-04-11T19:00:00.000Z"
            ),
            Card(
                topico = "Matemática",
                tipo = CardType.MULTIPLE_CHOICE,
                pergunta = "Qual o valor de π (pi) arredondado para duas casas decimais?",
                resposta = 0,
                alternativas = listOf("3,14", "3,15", "3,16", "3,17"),
                localizacao = "Escola",
                proximaRevisao = "2025-04-11T19:00:00.000Z"
            )
        )

        _cards.value = mockCards
        _totalCards.value = mockCards.size
        _cardIndex.value = 0
        _currentCard.value = mockCards.firstOrNull()
    }

    fun selectAnswer(answerIndex: Int) {
        _selectedAnswer.value = answerIndex
        _isAnswerRevealed.value = true
        _showDifficultyDialog.value = true
    }

    fun selectDifficulty(difficulty: Difficulty) {
        _selectedDifficulty.value = difficulty
        _showDifficultyDialog.value = false
    }

    fun nextCard() {
        val nextIndex = _cardIndex.value + 1
        if (nextIndex < _cards.value.size) {
            _cardIndex.value = nextIndex
            _currentCard.value = _cards.value[nextIndex]
            _selectedAnswer.value = null
            _isAnswerRevealed.value = false
        } else {
            // Fim das cartas - você pode implementar uma lógica aqui
            _currentCard.value = null
        }
    }
}