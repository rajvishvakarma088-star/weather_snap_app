package com.weathersnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.weathersnap.domain.model.WeatherReport

@Entity(tableName = "weather_reports")
data class WeatherReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityName: String,
    val country: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,
    val imagePath: String,
    val originalSizeKb: Long,
    val compressedSizeKb: Long,
    val notes: String,
    val timestamp: Long
)

fun WeatherReportEntity.toDomain() = WeatherReport(
    id = id,
    cityName = cityName,
    country = country,
    temperature = temperature,
    condition = condition,
    humidity = humidity,
    windSpeed = windSpeed,
    pressure = pressure,
    imagePath = imagePath,
    originalSizeKb = originalSizeKb,
    compressedSizeKb = compressedSizeKb,
    notes = notes,
    timestamp = timestamp
)
