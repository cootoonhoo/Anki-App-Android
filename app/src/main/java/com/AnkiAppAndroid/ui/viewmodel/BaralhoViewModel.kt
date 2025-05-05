package com.AnkiAppAndroid.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.AnkiAppAndroid.data.api.FlashcardApiService
import com.AnkiAppAndroid.data.database.AppDatabase
import com.AnkiAppAndroid.data.model.Baralho
import com.AnkiAppAndroid.data.repository.ApiBaralhoRepository
import com.AnkiAppAndroid.data.repository.BaralhoRepository
import com.AnkiAppAndroid.data.repository.LocationRepository
import com.AnkiAppAndroid.data.repository.UsuarioRepository
import com.AnkiAppAndroid.data.service.BaralhoSyncService
import com.AnkiAppAndroid.utils.LocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BaralhoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BaralhoRepository
    private val usuarioRepository : UsuarioRepository
    private val apiRepository: ApiBaralhoRepository
    private val _baralhos = MutableStateFlow<List<Baralho>>(emptyList())
    val baralhos: StateFlow<List<Baralho>> = _baralhos.asStateFlow()
    private val apiService = FlashcardApiService()

    private val _currentBaralho = MutableStateFlow<Baralho?>(null)
    val currentBaralho: StateFlow<Baralho?> = _currentBaralho.asStateFlow()

    private val locationService = LocationService(application)
    private val locationRepository: LocationRepository
    private var locationTrackingJob: Job? = null

    private val _currentNearbyLocation = MutableStateFlow<String?>(null)
    val currentNearbyLocation: StateFlow<String?> = _currentNearbyLocation.asStateFlow()

    private val _isDialogOpen = MutableStateFlow(false)
    val isDialogOpen: StateFlow<Boolean> = _isDialogOpen.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val syncService: BaralhoSyncService

    init {
        val database = AppDatabase.getDatabase(application)
        usuarioRepository = UsuarioRepository(database.usuarioDao())
        repository = BaralhoRepository(database.baralhoDao())
        locationRepository = LocationRepository(database.locationDao())

        val apiService = FlashcardApiService()
        apiRepository = ApiBaralhoRepository(apiService, database.baralhoDao(), usuarioRepository)
        syncService = BaralhoSyncService(apiService, database.baralhoDao(), usuarioRepository)

        viewModelScope.launch {
            repository.allBaralhos.collect { baralhos ->
                _baralhos.value = baralhos
            }
        }
        startLocationTracking()
        fetchBaralhosFromApi()
    }

    private fun fetchBaralhosFromApi() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val uuid = usuarioRepository.getOrCreateUsuarioUuid()
                val result = apiService.getBaralhos(uuid)

                if (result.isSuccess) {
                    val apiBaralhos = result.getOrThrow()

                    // Limpar os baralhos locais existentes
                    val currentBaralhos = repository.getAllBaralhosSync()
                    for (baralho in currentBaralhos) {
                        repository.deleteBaralho(baralho)
                    }

                    for (apiBaralho in apiBaralhos) {
                        val titulo = apiBaralho.titulo
                        val baralho = Baralho(
                            id = 0,
                            titulo = titulo,
                            mongoId = apiBaralho.id
                        )
                        repository.insertBaralho(baralho)
                    }
                } else {
                    _error.value = "Erro ao buscar baralhos: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar baralhos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshBaralhos() {
        fetchBaralhosFromApi()
    }

    private fun startLocationTracking() {
        locationTrackingJob?.cancel()
        locationTrackingJob = viewModelScope.launch {
            if (locationService.hasLocationPermission()) {
                while (isActive) {
                    try {
                        val (latitude, longitude) = locationService.getCurrentLocation() ?: return@launch
                        checkNearbyLocations(latitude, longitude)
                        delay(600000) // Verificar a cada 10 min
                    } catch (e: Exception) {
                        _currentNearbyLocation.value = null
                        delay(600000)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        locationTrackingJob?.cancel()
    }

    private suspend fun checkNearbyLocations(currentLat: Double, currentLon: Double) {
        locationRepository.recentLocations.collect { locations ->
            val nearbyLocation = locations.find { location ->
                LocationService.LocationUtils.isNearLocation(currentLat, currentLon, location)
            }
            _currentNearbyLocation.value = nearbyLocation?.name
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
            try {
                _isLoading.value = true

                val result = syncService.deleteBaralho(baralho)
                if (result.isFailure) {
                    _error.value = "Erro ao deletar baralho: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _error.value = "Erro ao deletar baralho: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}