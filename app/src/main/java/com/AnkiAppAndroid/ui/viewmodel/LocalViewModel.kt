package com.AnkiAppAndroid.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.AnkiAppAndroid.data.database.AppDatabase
import com.AnkiAppAndroid.data.model.Local
import com.AnkiAppAndroid.data.repository.LocalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LocalRepository
    private val _locais = MutableStateFlow<List<Local>>(emptyList())
    val locais: StateFlow<List<Local>> = _locais.asStateFlow()

    private val _currentLocal = MutableStateFlow<Local?>(null)
    val currentLocal: StateFlow<Local?> = _currentLocal.asStateFlow()

    private val _isDialogOpen = MutableStateFlow(false)
    val isDialogOpen: StateFlow<Boolean> = _isDialogOpen.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LocalRepository(database.localDao())
        viewModelScope.launch {
            repository.allLocais.collect { locais ->
                _locais.value = locais
            }
        }
    }

    fun adicionarLocal(nome: String) {
        if (nome.isNotBlank()) {
            viewModelScope.launch {
                repository.insertLocal(Local(nome = nome))
            }
        }
    }

    fun showDialog() {
        _isDialogOpen.value = true
    }

    fun hideDialog() {
        _isDialogOpen.value = false
    }

    fun getLocalById(id: Long): Local? {
        var local: Local? = null
        viewModelScope.launch {
            local = repository.getLocalById(id)
        }
        return local
    }

    fun fetchLocalById(id: Long) {
        viewModelScope.launch {
            _currentLocal.value = repository.getLocalById(id)
        }
    }

    fun deleteLocal(local: Local) {
        viewModelScope.launch {
            repository.deleteLocal(local)
        }
    }
} 