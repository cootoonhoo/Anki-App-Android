package com.AnkiAppAndroid.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.AnkiAppAndroid.ui.viewmodel.BaralhoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    baralhoId: Long,
    navController: NavController,
    viewModel: BaralhoViewModel
) {
    var baralhoTitulo by remember { mutableStateOf("Baralho") }

    // Carrega o título do baralho
    LaunchedEffect(baralhoId) {
        val baralho = viewModel.getBaralhoById(baralhoId)
        baralho?.let {
            baralhoTitulo = it.titulo
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(baralhoTitulo) },
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Implementar mecanismo de resposta dos cards",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}