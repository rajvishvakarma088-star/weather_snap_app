package com.weathersnap.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weathersnap.data.local.WeatherReportEntity
import com.weathersnap.data.repository.WeatherRepository
import com.weathersnap.domain.model.Weather
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {
    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    fun onNotesChange(value: String) {
        _notes.value = value
    }

    fun saveReport(
        weather: Weather,
        imagePath: String,
        originalSizeKb: Long,
        compressedSizeKb: Long,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            _saving.value = true
            repository.saveReport(
                WeatherReportEntity(
                    cityName = weather.cityName,
                    country = weather.country,
                    temperature = weather.temperature,
                    condition = weather.condition,
                    humidity = weather.humidity,
                    windSpeed = weather.windSpeed,
                    pressure = weather.pressure,
                    imagePath = imagePath,
                    originalSizeKb = originalSizeKb,
                    compressedSizeKb = compressedSizeKb,
                    notes = notes.value,
                    timestamp = System.currentTimeMillis()
                )
            )
            _saving.value = false
            onSaved()
        }
    }
}
