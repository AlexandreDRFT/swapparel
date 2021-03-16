package fr.swapparel.extensions

data class WeatherPrediction(val targetTemperatures: List<Int>, val objectiveWeather: List<String>, val highestWindSpeed: Int)