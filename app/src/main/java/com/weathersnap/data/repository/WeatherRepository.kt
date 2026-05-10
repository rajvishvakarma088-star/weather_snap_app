package com.weathersnap.data.repository

import com.weathersnap.data.local.WeatherReportEntity
import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.Weather
import com.weathersnap.domain.model.WeatherReport
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun searchCities(query: String): List<City>
    suspend fun getWeather(city: City): Weather
    suspend fun saveReport(report: WeatherReportEntity): Long
    fun getAllReports(): Flow<List<WeatherReport>>
}
