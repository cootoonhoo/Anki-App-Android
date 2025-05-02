package com.AnkiAppAndroid.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.AnkiAppAndroid.data.model.Local
import com.AnkiAppAndroid.ui.viewmodel.LocalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    navController: NavController,
    viewModel: LocalViewModel
) {
    val locais by viewModel.locais.collectAsState()
    val isDialogOpen by viewModel.isDialogOpen.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf<Local?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locais Favoritos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Local")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (locais.isEmpty()) {
                Text(
                    text = "Nenhum local favorito adicionado.\nClique no + para adicionar.",
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(locais) { local ->
                        LocalItem(
                            local = local,
                            onDeleteClick = { showDeleteConfirmDialog = local }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (isDialogOpen) {
                AddLocationDialog(
                    onDismiss = { viewModel.hideDialog() },
                    onConfirm = { nomeLocal ->
                        viewModel.adicionarLocal(nomeLocal)
                        viewModel.hideDialog()
                    }
                )
            }

            showDeleteConfirmDialog?.let { local ->
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = null },
                    title = { Text("Confirmação") },
                    text = { Text("Tem certeza que deseja excluir este local?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteLocal(local)
                                showDeleteConfirmDialog = null
                            }
                        ) {
                            Text("Sim")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteConfirmDialog = null }) {
                            Text("Não")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LocalItem(
    local: Local,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = local.nome,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Excluir Local"
                )
            }
        }
    }
}

@Composable
fun AddLocationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nomeLocal by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Local Favorito") },
        text = {
            Column {
                Text("Digite o nome do local:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nomeLocal,
                    onValueChange = { nomeLocal = it },
                    label = { Text("Nome do Local") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nomeLocal) },
                enabled = nomeLocal.isNotBlank()
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}