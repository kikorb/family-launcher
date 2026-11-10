package com.familyboard.tv.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.familyboard.tv.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

data class WallpaperBatch(val results: List<WallpaperResult>, val lastPage: Int, val failed: Boolean)

object WallpaperClient {
    private const val TAG = "FamilyBoardWallpaper"
    private const val USER_AGENT = "FamilyLauncher/1.2"
    private const val TMDB_THUMB = "https://image.tmdb.org/t/p/w300"
    private const val TMDB_FULL = "https://image.tmdb.org/t/p/original"
    private const val MAX_DOWNLOAD_BYTES = 40L * 1024 * 1024
    private const val TARGET_WIDTH = 1920

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun searchFirst(query: String): WallpaperBatch = coroutineScope {
        val stills = async { runCatching { tmdbStills(query) }.onFailure { Log.w(TAG, "tmdb failed", it) }.getOrNull() }
        val wallhaven = async { runCatching { wallhaven(query, 1) }.onFailure { Log.w(TAG, "wallhaven failed", it) }.getOrNull() }
        val tmdbResults = stills.await()
        val whResults = wallhaven.await()
        if (tmdbResults == null && whResults == null) return@coroutineScope WallpaperBatch(emptyList(), 0, true)
        WallpaperBatch(interleave(tmdbResults.orEmpty(), whResults?.first.orEmpty()), whResults?.second ?: 0, false)
    }

    suspend fun searchMore(query: String, page: Int): WallpaperBatch {
        val result = runCatching { wallhaven(query, page) }.onFailure { Log.w(TAG, "wallhaven page failed", it) }.getOrNull()
            ?: return WallpaperBatch(emptyList(), page, true)
        return WallpaperBatch(result.first, result.second, false)
    }

    private fun interleave(a: List<WallpaperResult>, b: List<WallpaperResult>): List<WallpaperResult> {
        val out = mutableListOf<WallpaperResult>()
        for (i in 0 until maxOf(a.size, b.size)) {
            a.getOrNull(i)?.let { out.add(it) }
            b.getOrNull(i)?.let { out.add(it) }
        }
        return out
    }

    private suspend fun wallhaven(query: String, page: Int): Pair<List<WallpaperResult>, Int> = withContext(Dispatchers.IO) {
        val url = "https://wallhaven.cc/api/v1/search".toHttpUrl().newBuilder()
            .addQueryParameter("q", query.trim())
            .addQueryParameter("categories", "110")
            .addQueryParameter("purity", "100")
            .addQueryParameter("atleast", "1920x1080")
            .addQueryParameter("ratios", "16x9")
            .addQueryParameter("sorting", if (query.isBlank()) "toplist" else "relevance")
            .addQueryParameter("page", page.toString())
            .build()
        client.newCall(Request.Builder().url(url).header("User-Agent", USER_AGENT).build()).execute().use { resp ->
            if (!resp.isSuccessful) throw IllegalStateException("Wallhaven ${resp.code}")
            val json = JSONObject(resp.body?.string() ?: "{}")
            val data = json.optJSONArray("data") ?: return@use emptyList<WallpaperResult>() to page
            val results = (0 until data.length()).map { i ->
                val r = data.getJSONObject(i)
                WallpaperResult(
                    id = r.optString("id"),
                    source = "wallhaven",
                    thumbUrl = r.optJSONObject("thumbs")?.optString("large").orEmpty(),
                    fullUrl = r.optString("path"),
                    label = query.trim().ifEmpty { "Wallhaven" }
                )
            }.filter { it.thumbUrl.isNotEmpty() && it.fullUrl.isNotEmpty() }
            results to (json.optJSONObject("meta")?.optInt("last_page", page) ?: page)
        }
    }

    private suspend fun tmdbStills(query: String): List<WallpaperResult> = withContext(Dispatchers.IO) {
        val key = BuildConfig.TMDB_API_KEY
        if (key.isEmpty() || query.isBlank()) return@withContext emptyList()
        val searchUrl = "https://api.themoviedb.org/3/search/multi".toHttpUrl().newBuilder()
            .addQueryParameter("api_key", key)
            .addQueryParameter("language", "es-ES")
            .addQueryParameter("include_adult", "false")
            .addQueryParameter("query", query.trim())
            .build()
        val titles = client.newCall(Request.Builder().url(searchUrl).build()).execute().use { resp ->
            if (!resp.isSuccessful) throw IllegalStateException("TMDB ${resp.code}")
            val arr = JSONObject(resp.body?.string() ?: "{}").optJSONArray("results") ?: return@use emptyList()
            (0 until arr.length()).map { arr.getJSONObject(it) }
                .filter { it.optString("media_type") == "movie" || it.optString("media_type") == "tv" }
                .take(3)
        }
        coroutineScope {
            titles.map { t ->
                async {
                    val type = t.optString("media_type")
                    val title = t.optString(if (type == "tv") "name" else "title")
                    val imagesUrl = "https://api.themoviedb.org/3/$type/${t.optLong("id")}/images".toHttpUrl().newBuilder()
                        .addQueryParameter("api_key", key)
                        .addQueryParameter("include_image_language", "null,es,en")
                        .build()
                    client.newCall(Request.Builder().url(imagesUrl).build()).execute().use { resp ->
                        if (!resp.isSuccessful) return@use emptyList<WallpaperResult>()
                        val backdrops = JSONObject(resp.body?.string() ?: "{}").optJSONArray("backdrops") ?: return@use emptyList()
                        (0 until backdrops.length()).map { backdrops.getJSONObject(it) }
                            .sortedBy { if (it.isNull("iso_639_1")) 0 else 1 }
                            .take(4)
                            .map {
                                val path = it.optString("file_path")
                                WallpaperResult(
                                    id = "${type}${t.optLong("id")}_${path.trim('/').substringBefore('.')}",
                                    source = "tmdb",
                                    thumbUrl = TMDB_THUMB + path,
                                    fullUrl = TMDB_FULL + path,
                                    label = title
                                )
                            }
                    }
                }
            }.awaitAll().flatten()
        }
    }

    suspend fun download(result: WallpaperResult, dir: File): File? = withContext(Dispatchers.IO) {
        val temp = File(dir, "download.tmp")
        try {
            dir.mkdirs()
            client.newCall(Request.Builder().url(result.fullUrl).header("User-Agent", USER_AGENT).build()).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                resp.body?.byteStream()?.use { input ->
                    temp.outputStream().use { out ->
                        val buffer = ByteArray(64 * 1024)
                        var total = 0L
                        while (true) {
                            val n = input.read(buffer)
                            if (n < 0) break
                            total += n
                            if (total > MAX_DOWNLOAD_BYTES) return@withContext null
                            out.write(buffer, 0, n)
                        }
                    }
                } ?: return@withContext null
            }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(temp.path, bounds)
            if (bounds.outWidth <= 0) return@withContext null
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= TARGET_WIDTH) sample *= 2
            val decoded = BitmapFactory.decodeFile(temp.path, BitmapFactory.Options().apply { inSampleSize = sample })
                ?: return@withContext null
            val scaled = if (decoded.width > TARGET_WIDTH) {
                Bitmap.createScaledBitmap(decoded, TARGET_WIDTH, decoded.height * TARGET_WIDTH / decoded.width, true)
                    .also { if (it !== decoded) decoded.recycle() }
            } else decoded
            val file = File(dir, "wp_${result.source}_${result.id}.jpg")
            file.outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            scaled.recycle()
            file
        } catch (e: Exception) {
            Log.w(TAG, "download failed", e)
            null
        } finally {
            temp.delete()
        }
    }
}
