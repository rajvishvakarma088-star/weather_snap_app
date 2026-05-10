package com.weathersnap.data.repository

import com.weathersnap.data.local.WeatherReportDao
import com.weathersnap.data.local.WeatherReportEntity
import com.weathersnap.data.local.toDomain
import com.weathersnap.data.remote.GeocodingApi
import com.weathersnap.data.remote.WeatherApi
import com.weathersnap.domain.model.City
import com.weathersnap.domain.model.Weather
import com.weathersnap.domain.model.WeatherReport
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val geocodingApi: GeocodingApi,
    private val weatherApi: WeatherApi,
    private val reportDao: WeatherReportDao
) : WeatherRepository {
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val geocodingCache = object : LinkedHashMap<String, List<City>>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, List<City>>?): Boolean {
            return size > 32
        }
    }

    override suspend fun searchCities(query: String): List<City> = withContext(ioDispatcher) {
        val key = query.trim().lowercase()
        if (key.isBlank()) return@withContext emptyList()
        geocodingCache[key]?.let { return@withContext it }

        val cities = geocodingApi.searchCities(query = key)
            .results
            .orEmpty()
            .map {
                City(
                    id = it.id,
                    name = it.name,
                    country = it.country.orEmpty(),
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }
        geocodingCache[key] = cities
        cities
    }

    override suspend fun getWeather(city: City): Weather = withContext(ioDispatcher) {
        val current = weatherApi.getCurrentWeather(
            latitude = city.latitude,
            longitude = city.longitude
        ).current

        Weather(
            cityName = city.name,
            country = city.country,
            temperature = current.temperature,
            condition = current.weatherCode.toWmoCondition(),
            humidity = current.humidity,
            windSpeed = current.windSpeed,
            pressure = current.pressure
        )
    }

    override suspend fun saveReport(report: WeatherReportEntity): Long = withContext(ioDispatcher) {
        reportDao.insert(report)
    }

    override fun getAllReports(): Flow<List<WeatherReport>> {
        return reportDao.getAllReports()
            .map { reports -> reports.map { it.toDomain() } }
            .flowOn(ioDispatcher)
    }
}

private fun Int.toWmoCondition(): String = when (this) {
    0 -> "Clear sky"
    1 -> "Mainly clear"
    2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Fog"
    51, 53, 55 -> "Drizzle"
    56, 57 -> "Freezing drizzle"
    61, 63, 65 -> "Rain"
    66, 67 -> "Freezing rain"
    71, 73, 75 -> "Snow fall"
    77 -> "Snow grains"
    80, 81, 82 -> "Rain showers"
    85, 86 -> "Snow showers"
    95 -> "Thunderstorm"
    96, 99 -> "Thunderstorm with hail"
    else -> "Unknown"
}
