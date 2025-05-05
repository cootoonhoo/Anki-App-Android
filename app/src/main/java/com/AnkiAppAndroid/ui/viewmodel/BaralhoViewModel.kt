package com.AnkiAppAndroid.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.AnkiAppAndroid.data.model.BaralhoBancoDados
import com.AnkiAppAndroid.data.repository.BaralhoBackendRepository
import com.AnkiAppAndroid.data.repository.LocationRepository
import com.AnkiAppAndroid.data.repository.UsuarioRepository
import com.AnkiAppAndroid.utils.LocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

class BaralhoViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = BaralhoBackendRepository()
    private val usuarioRepo: UsuarioRepository

    // Estado dos baralhos vindos do backend
    private val _baralhos = MutableStateFlow<List<BaralhoBancoDados>>(emptyList())
    val baralhos: StateFlow<List<BaralhoBancoDados>> = _baralhos.asStateFlow()

    // Baralho atualmente selecionado / detalhado
    private val _currentBaralho = MutableStateFlow<BaralhoBancoDados?>(null)
    val currentBaralho: StateFlow<BaralhoBancoDados?> = _currentBaralho.asStateFlow()

    // Dialogo “novo baralho”
    private val _isDialogOpen = MutableStateFlow(false)
    val isDialogOpen: StateFlow<Boolean> = _isDialogOpen.asStateFlow()

    // Localização
    private val locationSvc = LocationService(application)
    private val locationRepo: LocationRepository
    private val _currentNearbyLocation = MutableStateFlow<String?>(null)
    val currentNearbyLocation: StateFlow<String?> = _currentNearbyLocation.asStateFlow()
    private var locationJob: Job? = null

    init {
        // DAO apenas para Usuário e Local (se ainda usar Room neles)
        val db = com.AnkiAppAndroid.data.database.AppDatabase.getDatabase(application)
        usuarioRepo = UsuarioRepository(db.usuarioDao())
        locationRepo = LocationRepository(db.locationDao())

        // 1) carrega a lista inicial de baralhos
        carregarBaralhos()

        // 2) inicia rastreamento de localização
        startLocationTracking()
    }

    /** Busca todos os baralhos do backend e preenche o StateFlow */
    fun carregarBaralhos() {
        viewModelScope.launch {
            try {
                _baralhos.value = repo.listar()   // GET /baralhos
            } catch (e: Exception) {
                _baralhos.value = emptyList()
                e.printStackTrace()
            }
        }
    }

    /** Abre o dialogo de “novo baralho” */
    fun showDialog() { _isDialogOpen.value = true }

    /** Fecha o dialogo de “novo baralho” */
    fun hideDialog() { _isDialogOpen.value = false }

    /** Adiciona um baralho novo no backend e recarrega a lista */
    fun adicionarBaralho(titulo: String) {
        if (titulo.isBlank()) return
        viewModelScope.launch {
            try {
                // monta o objeto
                val idUsuario = usuarioRepo.getOrCreateUsuarioUuid()
                val novo = BaralhoBancoDados(
                    id        = "",               // gerado pelo backend
                    titulo    = titulo,
                    cartas    = mutableListOf(),
                    idUsuario = idUsuario
                )
                repo.criar(novo)              // POST /baralhos
                carregarBaralhos()            // refaz GET /baralhos
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Busca um baralho específico e preenche _currentBaralho */
    fun fetchBaralhoById(id: String) {
        viewModelScope.launch {
            _currentBaralho.value = try {
                repo.obter(id)              // GET /baralhos/{id}
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /** Atualiza o baralho no backend (incluindo cartas editadas) e recarrega tudo */
    fun updateBaralho(baralho: BaralhoBancoDados) {
        viewModelScope.launch {
            try {
                repo.atualizar(baralho)      // PUT /baralhos/{id}
                carregarBaralhos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Exclui o baralho no backend e recarrega a lista */
    fun deleteBaralho(id: String) {
        viewModelScope.launch {
            try {
                if (repo.deletar(id))       // DELETE /baralhos/{id}
                    carregarBaralhos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /** Inicia loop de checagem de localização toda vez que o ViewModel for criado */
    private fun startLocationTracking() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            if (locationSvc.hasLocationPermission()) {
                while (isActive) {
                    try {
                        val (latitude, longitude) = locationSvc.getCurrentLocation() ?: return@launch
                        checkNearby(latitude, longitude)
                        delay(600000) // Verificar a cada 10 min
                    } catch (_: Exception) {
                        _currentNearbyLocation.value = null
                    }
                    delay(600_000)  // 10 minutos
                }
            }
        }
    }

    /** Atualiza a propriedade currentNearbyLocation */
    private suspend fun checkNearby(lat: Double, lon: Double) {
        locationRepo.recentLocations.collect { list ->
            val nearby = list.find {
                LocationService.LocationUtils.isNearLocation(lat, lon, it)
            }
            _currentNearbyLocation.value = nearby?.name
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationJob?.cancel()
    }
}
