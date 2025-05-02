package com.AnkiAppAndroid.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.AnkiAppAndroid.ui.navigation.Screen
import com.AnkiAppAndroid.ui.viewmodel.BaralhoViewModel

// ui/screens/EditBaralhoScreen.kt (atualizado)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBaralhoScreen(
    baralhoId: Long,
    navController: NavController,
    viewModel: BaralhoViewModel
) {
    // Carrega o baralho pelo ID
    LaunchedEffect(baralhoId) {
        viewModel.fetchBaralhoById(baralhoId)
    }

    val baralho by viewModel.currentBaralho.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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
                    // Botão de excluir
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir Baralho"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Implementar edição do baralho",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }

        // Diálogo de confirmação para exclusão
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Confirmação") },
                text = { Text("Tem certeza que deseja excluir este baralho?") },
                confirmButton = {
                    Button(
                        onClick = {
                            baralho?.let {
                                viewModel.deleteBaralho(it)
                                showDeleteConfirmDialog = false
                                // Volta para a tela home após excluir
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            }
                        }
                    ) {
                        Text("Sim")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteConfirmDialog = false }
                    ) {
                        Text("Não")
                    }
                }
            )
        }
    }
}