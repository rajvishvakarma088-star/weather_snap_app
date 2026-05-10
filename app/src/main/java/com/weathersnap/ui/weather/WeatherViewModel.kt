package com.weathersnap.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.data.repository.WeatherRepository
import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.Weather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class WeatherUiState {
    data object Idle : WeatherUiState()
    data object LoadingSuggestions : WeatherUiState()
    data class SuggestionsLoaded(val cities: List<City>) : WeatherUiState()
    data object LoadingWeather : WeatherUiState()
    data class WeatherLoaded(val weather: Weather) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

@OptIn(FlowPreview::class)
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var lastSelectedCity: City? = null
    private var suppressNextSearch = false

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .collect { value ->
                    if (suppressNextSearch) {
                        suppressNextSearch = false
                        return@collect
                    }
                    if (value.length > 2) {
                        loadSuggestions(value)
                    } else {
                        _uiState.value = WeatherUiState.Idle
                    }
                }
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun selectCity(city: City) {
        lastSelectedCity = city
        suppressNextSearch = true
        _query.value = "${city.name}, ${city.country}"
        fetchWeather(city)
    }

    fun retry() {
        lastSelectedCity?.let(::fetchWeather) ?: run {
            val value = query.value
            if (value.length > 2) viewModelScope.launch { loadSuggestions(value) }
        }
    }

    private suspend fun loadSuggestions(query: String) {
        _uiState.value = WeatherUiState.LoadingSuggestions
        runCatching { repository.searchCities(query) }
            .onSuccess { _uiState.value = WeatherUiState.SuggestionsLoaded(it) }
            .onFailure { _uiState.value = WeatherUiState.Error(it.message ?: "Could not load city suggestions") }
    }

    private fun fetchWeather(city: City) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.LoadingWeather
            runCatching { repository.getWeather(city) }
                .onSuccess { _uiState.value = WeatherUiState.WeatherLoaded(it) }
                .onFailure { _uiState.value = WeatherUiState.Error(it.message ?: "Could not load weather") }
        }
    }
}
