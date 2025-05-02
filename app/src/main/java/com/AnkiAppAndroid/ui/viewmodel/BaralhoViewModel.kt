package com.AnkiAppAndroid.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.AnkiAppAndroid.data.database.AppDatabase
import com.AnkiAppAndroid.data.model.Baralho
import com.AnkiAppAndroid.data.repository.BaralhoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BaralhoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BaralhoRepository
    private val _baralhos = MutableStateFlow<List<Baralho>>(emptyList())
    val baralhos: StateFlow<List<Baralho>> = _baralhos.asStateFlow()

    private val _currentBaralho = MutableStateFlow<Baralho?>(null)
    val currentBaralho: StateFlow<Baralho?> = _currentBaralho.asStateFlow()


    private val _isDialogOpen = MutableStateFlow(false)
    val isDialogOpen: StateFlow<Boolean> = _isDialogOpen.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BaralhoRepository(database.baralhoDao())
        viewModelScope.launch {
            repository.allBaralhos.collect { baralhos ->
                _baralhos.value = baralhos
            }
        }
    }

    fun adicionarBaralho(titulo: String) {
        if (titulo.isNotBlank()) {
            viewModelScope.launch {
                repository.insertBaralho(Baralho(titulo = titulo))
            }
        }
    }

    fun showDialog() {
        _isDialogOpen.value = true
    }

    fun hideDialog() {
        _isDialogOpen.value = false
    }

    fun getBaralhoById(id: Long): Baralho? {
        var baralho: Baralho? = null
        viewModelScope.launch {
            baralho = repository.getBaralhoById(id)
        }
        return baralho
    }

    fun fetchBaralhoById(id: Long) {
        viewModelScope.launch {
            _currentBaralho.value = repository.getBaralhoById(id)
        }
    }

    fun deleteBaralho(baralho: Baralho) {
        viewModelScope.launch {
            repository.deleteBaralho(baralho)
        }
    }
}