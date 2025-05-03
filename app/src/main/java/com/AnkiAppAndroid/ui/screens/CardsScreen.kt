// ui/screens/CardsScreen.kt (completamente atualizado)
package com.AnkiAppAndroid.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.AnkiAppAndroid.data.model.Card
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.SnackbarDuration
import com.AnkiAppAndroid.ui.components.DifficultyDialog
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.AnkiAppAndroid.ui.components.DifficultyDialog
import kotlinx.coroutines.launch
import com.AnkiAppAndroid.ui.viewmodel.BaralhoViewModel
import com.AnkiAppAndroid.ui.viewmodel.CardsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    baralhoId: Long,
    navController: NavController,
    baralhoViewModel: BaralhoViewModel,
    cardsViewModel: CardsViewModel = viewModel()
) {
    val baralho by baralhoViewModel.currentBaralho.collectAsState()
    val currentCard by cardsViewModel.currentCard.collectAsState()
    val selectedAnswer by cardsViewModel.selectedAnswer.collectAsState()
    val isAnswerRevealed by cardsViewModel.isAnswerRevealed.collectAsState()
    val cardIndex by cardsViewModel.cardIndex.collectAsState()
    val totalCards by cardsViewModel.totalCards.collectAsState()
    val showDifficultyDialog by cardsViewModel.showDifficultyDialog.collectAsState()
    val selectedDifficulty by cardsViewModel.selectedDifficulty.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Busca o baralho pelo ID quando a tela é criada
    LaunchedEffect(baralhoId) {
        baralhoViewModel.fetchBaralhoById(baralhoId)
        // Carrega o mock de cartas - TODO: Criar o método que pega as cartas do banco de dados.
        cardsViewModel.loadMockCards()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(baralho?.titulo ?: "Baralho")
                        Text(
                            text = "Carta ${cardIndex + 1} de $totalCards",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            currentCard?.let { card ->
                CardContent(
                    card = card,
                    selectedAnswer = selectedAnswer,
                    isAnswerRevealed = isAnswerRevealed,
                    onAnswerSelected = { answer ->
                        cardsViewModel.selectAnswer(answer)
                    },
                    onNextCard = {

                        cardsViewModel.nextCard()
                    }
                )
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Não há cartas disponíveis",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (showDifficultyDialog) {
                DifficultyDialog(
                    onDifficultySelected = { difficulty ->
                        cardsViewModel.selectDifficulty(difficulty)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Implementar update do proxima_revisao",
                                duration = SnackbarDuration.Short
                            )
                        }
                        // TODO - Implementar algoritmo de Atulização da próxima exibição usand o cardsViewModel
                    }
                )
            }
        }
    }
}

@Composable
fun CardContent(
    card: Card,
    selectedAnswer: Int?,
    isAnswerRevealed: Boolean,
    onAnswerSelected: (Int) -> Unit,
    onNextCard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Tópico do card
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = card.topico,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }

        // Pergunta
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = card.pergunta,
                modifier = Modifier.padding(16.dp),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }

        // Alternativas
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            card.alternativas.forEachIndexed { index, alternative ->
                AlternativeOption(
                    text = alternative,
                    isSelected = selectedAnswer == index,
                    isCorrect = isAnswerRevealed && index == card.resposta,
                    isIncorrect = isAnswerRevealed && selectedAnswer == index && index != card.resposta,
                    isAnswerRevealed = isAnswerRevealed,
                    onClick = {
                        if (!isAnswerRevealed) {
                            onAnswerSelected(index)
                        }
                    }
                )
            }
        }

        // Botão de ação
        if (isAnswerRevealed) {
            Button(
                onClick = onNextCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Próxima Carta", fontSize = 16.sp)
            }
        }

        // Localização (se disponível)
        card.localizacao?.let { location ->
            Text(
                text = "📍 $location",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AlternativeOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isIncorrect: Boolean,
    isAnswerRevealed: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.15f)
            isIncorrect -> Color(0xFFE53935).copy(alpha = 0.15f)
            isSelected && !isAnswerRevealed -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        animationSpec = tween(300)
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isCorrect -> Color(0xFF4CAF50)
            isIncorrect -> Color(0xFFE53935)
            isSelected && !isAnswerRevealed -> MaterialTheme.colorScheme.primary
            else -> Color.Gray.copy(alpha = 0.5f)
        },
        animationSpec = tween(300)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected || isAnswerRevealed) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = if (isSelected || isCorrect) FontWeight.Bold else FontWeight.Normal
            )

            if (isAnswerRevealed) {
                when {
                    isCorrect -> Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Correto",
                        tint = Color(0xFF4CAF50)
                    )
                    isIncorrect -> Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Incorreto",
                        tint = Color(0xFFE53935)
                    )
                }
            }
        }
    }
}