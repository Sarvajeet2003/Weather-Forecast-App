package com.kamesh.weatherapplication.Database

data class WeatherData(
    var id: Long?,
    var city: String,
    var latitude: Double,
    var longitude: Double,
    var currentTemperature: Double,
    var maxTemperature: Double,
    var minTemperature: Double,
    var pressure: Int,
    var humidity: Int,
    var windSpeed: Double
)


