package com.familyboard.tv.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object WeatherClient {
    private val client = OkHttpClient()
    private const val GRANADA_URL =
        "https://api.open-meteo.com/v1/forecast?latitude=37.1773&longitude=-3.5986&current_weather=true&timezone=auto"

    suspend fun fetchGranadaWeather(): WeatherInfo = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(GRANADA_URL).build()
            client.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext fallback()
                val body = resp.body?.string() ?: return@withContext fallback()
                val json = JSONObject(body)
                val cur = json.getJSONObject("current_weather")
                val temp = cur.getDouble("temperature").toInt()
                val code = cur.getInt("weathercode")

                val (condition, emoji) = when (code) {
                    0 -> "Despejado" to "☀️"
                    1, 2, 3 -> "Parcialmente nublado" to "⛅"
                    45, 48 -> "Niebla" to "🌫️"
                    51, 53, 55, 61, 63, 65 -> "Lluvia" to "🌦️"
                    80, 81, 82 -> "Chubascos" to "🌧️"
                    95, 96, 99 -> "Tormenta" to "⛈️"
                    else -> "Nublado" to "☁️"
                }
                WeatherInfo(temp, condition, emoji)
            }
        } catch (e: Exception) {
            Log.w("FamilyBoardWeather", "weather fetch failed", e)
            fallback()
        }
    }

    private fun fallback() = WeatherInfo(15, "Parcialmente nublado", "⛅")
}
