package com.weathersnap.data.remote.model

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    val results: List<GeocodingResult>? = null
)

data class GeocodingResult(
    val id: Long,
    val name: String,
    val country: String? = null,
    val latitude: Double,
    val longitude: Double
)
