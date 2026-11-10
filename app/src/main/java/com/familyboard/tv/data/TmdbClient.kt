package com.familyboard.tv.data

import android.util.Log
import com.familyboard.tv.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object TmdbClient {
    private const val TAG = "FamilyBoardTmdb"
    private const val IMAGE_BASE = "https://image.tmdb.org/t/p/w342"
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun search(query: String, isShow: Boolean): List<TmdbResult>? = withContext(Dispatchers.IO) {
        if (BuildConfig.TMDB_API_KEY.isEmpty() || query.isBlank()) return@withContext emptyList()
        try {
            val url = "https://api.themoviedb.org/3/search/${if (isShow) "tv" else "movie"}".toHttpUrl().newBuilder()
                .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
                .addQueryParameter("language", "es-ES")
                .addQueryParameter("include_adult", "false")
                .addQueryParameter("query", query.trim())
                .build()
            client.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val results = JSONObject(resp.body?.string() ?: "{}").optJSONArray("results") ?: return@withContext emptyList()
                (0 until minOf(results.length(), 8)).map { i ->
                    val r = results.getJSONObject(i)
                    val date = r.optString(if (isShow) "first_air_date" else "release_date", "")
                    val poster = r.optString("poster_path", "")
                    TmdbResult(
                        id = r.optLong("id"),
                        title = r.optString(if (isShow) "name" else "title", ""),
                        year = date.take(4).toIntOrNull(),
                        posterUrl = if (poster.isNotEmpty() && poster != "null") IMAGE_BASE + poster else "",
                        rating = r.optDouble("vote_average", 0.0).takeIf { it > 0.0 },
                        overview = r.optString("overview", ""),
                        isShow = isShow
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "search failed", e)
            null
        }
    }
}
