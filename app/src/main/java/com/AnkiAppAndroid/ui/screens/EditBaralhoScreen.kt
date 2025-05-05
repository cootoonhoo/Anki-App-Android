package com.AnkiAppAndroid.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.AnkiAppAndroid.data.model.Card
import com.AnkiAppAndroid.ui.components.AddEditCardDialog
import com.AnkiAppAndroid.ui.navigation.Screen
import com.AnkiAppAndroid.ui.viewmodel.BaralhoViewModel
import com.AnkiAppAndroid.ui.viewmodel.EditBaralhoViewModel
import com.AnkiAppAndroid.ui.viewmodel.LocationViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBaralhoScreen(
    baralhoId: Long,
    navController: NavController,
    baralhoViewModel: BaralhoViewModel,
    locationViewModel: LocationViewModel,
    editViewModel: EditBaralhoViewModel = viewModel()
) {
    val baralho by baralhoViewModel.currentBaralho.collectAsState()
    val mongoBaralho by editViewModel.mongoBaralho.collectAsState()
    val showAddCardDialog by editViewModel.showAddCardDialog.collectAsState()
    val editingCard by editViewModel.editingCard.collectAsState()
    val snackbarMessage by editViewModel.snackbarMessage.collectAsState()
    val isLoading by baralhoViewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(baralhoId) {
        baralhoViewModel.fetchBaralhoById(baralhoId)
    }

    LaunchedEffect(baralho) {
        baralho?.let { localBaralho ->
            localBaralho.mongoId?.let { mongoId ->
                editViewModel.loadBaralhoById(mongoId)
            } ?: run {
                editViewModel.loadMockBaralho()
            }
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                editViewModel.clearSnackbarMessage()
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Excluir Baralho") },
            text = { Text("Tem certeza que deseja excluir este baralho? Esta ação não pode ser desfeita e excluirá o baralho tanto do aplicativo quanto do servidor.") },
            confirmButton = {
                Button(
                    onClick = {
                        baralho?.let {
                            baralhoViewModel.deleteBaralho(it)
                            showDeleteConfirmation = false
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar ${baralho?.titulo ?: "Baralho"}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { editViewModel.saveBaralho() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = "Salvar Baralho"
                            )
                        }
                    }
                    IconButton(
                        onClick = { showDeleteConfirmation = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir Baralho",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editViewModel.showAddCardDialog() },
                text = { Text("Adicionar Carta") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (mongoBaralho?.cartas?.isEmpty() == true) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma carta adicionada.\nClique no + para adicionar.",
                        fontSize = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                mongoBaralho?.cartas?.let { cards ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(cards) { card ->
                            CardItem(
                                card = card,
                                onEditClick = { editViewModel.editCard(card) },
                                onDeleteClick = { editViewModel.deleteCard(card) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            if (showAddCardDialog) {
                AddEditCardDialog(
                    card = editingCard,
                    locationViewModel = locationViewModel,
                    onDismiss = { editViewModel.hideAddCardDialog() },
                    onConfirm = { topico, pergunta, alternativas, resposta, localizacao ->
                        editViewModel.addOrUpdateCard(
                            topico = topico,
                            pergunta = pergunta,
                            alternativas = alternativas,
                            respostaIndex = resposta,
                            localizacao = localizacao
                        )
                    }
                )
            }
        }
    }
}
@Composable
fun CardItem(
    card: Card,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Tópico
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = card.topico,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // Pergunta
            Text(
                text = card.pergunta,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Resposta correta
            Text(
                text = "Resposta: ${card.alternativas[card.resposta]}",
                fontSize = 14.sp,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Localização
            card.localizacao?.let { location ->
                Text(
                    text = "📍 $location",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Ações
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}