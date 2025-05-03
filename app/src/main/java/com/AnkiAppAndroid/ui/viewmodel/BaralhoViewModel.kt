package com.AnkiAppAndroid.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.AnkiAppAndroid.data.database.AppDatabase
import com.AnkiAppAndroid.data.model.Baralho
import com.AnkiAppAndroid.data.repository.BaralhoRepository
import com.AnkiAppAndroid.data.repository.LocationRepository
import com.AnkiAppAndroid.utils.LocationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BaralhoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BaralhoRepository
    private val _baralhos = MutableStateFlow<List<Baralho>>(emptyList())
    val baralhos: StateFlow<List<Baralho>> = _baralhos.asStateFlow()

    private val _currentBaralho = MutableStateFlow<Baralho?>(null)
    val currentBaralho: StateFlow<Baralho?> = _currentBaralho.asStateFlow()

    private val locationService = LocationService(application)
    private val locationRepository: LocationRepository
    private var locationTrackingJob: Job? = null

    private val _currentNearbyLocation = MutableStateFlow<String?>(null)
    val currentNearbyLocation: StateFlow<String?> = _currentNearbyLocation.asStateFlow()

    private val _isDialogOpen = MutableStateFlow(false)
    val isDialogOpen: StateFlow<Boolean> = _isDialogOpen.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BaralhoRepository(database.baralhoDao())
        locationRepository = LocationRepository(database.locationDao())
        viewModelScope.launch {
            repository.allBaralhos.collect { baralhos ->
                _baralhos.value = baralhos
            }
        }
        startLocationTracking()
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
            repository.deleteBaralho(baralho)
        }
    }
}