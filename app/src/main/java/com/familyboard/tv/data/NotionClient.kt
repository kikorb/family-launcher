package com.familyboard.tv.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.concurrent.TimeUnit

data class FamilyData(
    val events: List<CalendarEvent>?,
    val todos: List<FamilyTask>?,
    val groceries: List<GroceryItem>?,
    val options: NotionOptions?,
    val movies: List<WatchItem>?,
    val shows: List<WatchItem>?
)

object NotionClient {
    private const val TAG = "FamilyBoardNotion"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
    private val JSON = "application/json".toMediaType()
    private const val NOTION_BASE = "https://api.notion.com/v1"
    private const val NOTION_VERSION = "2022-06-28"

    private const val DB_CALENDAR = "4122de7c-053e-80f6-bff7-ce0192a61e40"
    private const val DB_TODO = "4122de7c-053e-8034-8f3f-d309d27c582a"
    private const val DB_SHOPPING = "4122de7c-053e-8068-95f6-cebbba0eb495"
    const val DB_MOVIES = "4142de7c-053e-8193-9678-c32f5eba8005"
    const val DB_SHOWS = "4142de7c-053e-81c5-8dd3-dc00f4769c5d"

    private const val REPEAT_PROPERTY = "Repetir cada año"
    private const val SEVEN_DAYS_MS = 7L * 24L * 60L * 60L * 1000L
    private const val MAX_PAGES = 5

    suspend fun fetchAllFamilyData(token: String): FamilyData = withContext(Dispatchers.IO) {
        FamilyData(
            events = runCatching { fetchCalendar(token) }.onFailure { Log.w(TAG, "calendar sync failed", it) }.getOrNull(),
            todos = runCatching { fetchTodos(token) }.onFailure { Log.w(TAG, "todos sync failed", it) }.getOrNull(),
            groceries = runCatching { fetchGroceries(token) }.onFailure { Log.w(TAG, "groceries sync failed", it) }.getOrNull(),
            options = runCatching { fetchOptions(token) }.onFailure { Log.w(TAG, "options sync failed", it) }.getOrNull(),
            movies = runCatching { fetchWatch(DB_MOVIES, token) }.onFailure { Log.w(TAG, "movies sync failed", it) }.getOrNull(),
            shows = runCatching { fetchWatch(DB_SHOWS, token) }.onFailure { Log.w(TAG, "shows sync failed", it) }.getOrNull()
        )
    }

    private fun request(url: String, token: String) = Request.Builder()
        .url(url)
        .header("Authorization", "Bearer $token")
        .header("Notion-Version", NOTION_VERSION)

    private fun queryAll(dbId: String, token: String): List<JSONObject> {
        val all = mutableListOf<JSONObject>()
        var cursor: String? = null
        repeat(MAX_PAGES) {
            val payload = JSONObject().put("page_size", 100)
            if (cursor != null) payload.put("start_cursor", cursor)
            val req = request("$NOTION_BASE/databases/$dbId/query", token)
                .post(payload.toString().toRequestBody(JSON))
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) throw IllegalStateException("Notion query failed: ${resp.code}")
                val json = JSONObject(resp.body?.string() ?: "{}")
                val results = json.optJSONArray("results") ?: JSONArray()
                for (i in 0 until results.length()) all.add(results.getJSONObject(i))
                cursor = if (json.optBoolean("has_more", false)) json.optString("next_cursor") else null
            }
            if (cursor == null) return all
        }
        return all
    }

    private fun parseIsoTimestamp(iso: String): Long =
        try { Instant.parse(iso).toEpochMilli() } catch (e: Exception) { System.currentTimeMillis() }

    private fun titleOf(props: JSONObject, name: String): String {
        val arr = props.optJSONObject(name)?.optJSONArray("title") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until arr.length()) sb.append(arr.getJSONObject(i).optString("plain_text", ""))
        return sb.toString()
    }

    private fun titleProperty(text: String) = JSONObject().put(
        "title",
        JSONArray().put(JSONObject().put("text", JSONObject().put("content", text)))
    )

    private fun selectOptions(props: JSONObject, name: String): List<String> {
        val arr = props.optJSONObject(name)?.optJSONObject("select")?.optJSONArray("options") ?: return emptyList()
        return (0 until arr.length()).map { arr.getJSONObject(it).optString("name") }.filter { it.isNotBlank() }
    }

    private fun databaseProperties(dbId: String, token: String): JSONObject {
        val req = request("$NOTION_BASE/databases/$dbId", token).get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw IllegalStateException("Notion schema failed: ${resp.code}")
            return JSONObject(resp.body?.string() ?: "{}").optJSONObject("properties") ?: JSONObject()
        }
    }

    private fun fetchOptions(token: String): NotionOptions {
        val calendarProps = databaseProperties(DB_CALENDAR, token)
        if (!calendarProps.has(REPEAT_PROPERTY)) addRepeatProperty(token)
        return NotionOptions(
            who = selectOptions(databaseProperties(DB_TODO, token), "Quién"),
            groceryTypes = selectOptions(databaseProperties(DB_SHOPPING, token), "Type"),
            eventTypes = selectOptions(calendarProps, "Type"),
            watchWho = selectOptions(databaseProperties(DB_MOVIES, token), "Quién")
        )
    }

    private fun addRepeatProperty(token: String) {
        val body = JSONObject()
            .put("properties", JSONObject().put(REPEAT_PROPERTY, JSONObject().put("checkbox", JSONObject())))
            .toString().toRequestBody(JSON)
        client.newCall(request("$NOTION_BASE/databases/$DB_CALENDAR", token).patch(body).build()).execute().close()
    }

    private fun selectName(props: JSONObject, name: String): String =
        props.optJSONObject(name)?.optJSONObject("select")?.optString("name", "") ?: ""

    private fun selectProperty(name: String?) = JSONObject().put(
        "select",
        if (name.isNullOrBlank()) JSONObject.NULL else JSONObject().put("name", name)
    )

    private fun numberOf(props: JSONObject, name: String): Double? {
        val v = props.optJSONObject(name) ?: return null
        return if (v.isNull("number")) null else v.optDouble("number")
    }

    private fun fetchWatch(dbId: String, token: String): List<WatchItem> {
        val out = mutableListOf<WatchItem>()
        for (item in queryAll(dbId, token)) {
            val props = item.optJSONObject("properties") ?: continue
            val title = titleOf(props, "Name")
            if (title.isEmpty()) continue
            val watched = props.optJSONObject("Visto")?.optBoolean("checkbox", false) ?: false
            val lastEdited = parseIsoTimestamp(item.optString("last_edited_time"))
            val overviewArr = props.optJSONObject("Sinopsis")?.optJSONArray("rich_text")
            val overview = StringBuilder()
            if (overviewArr != null) for (i in 0 until overviewArr.length()) overview.append(overviewArr.getJSONObject(i).optString("plain_text", ""))
            out.add(
                WatchItem(
                    id = item.optString("id"),
                    title = title,
                    year = numberOf(props, "Año")?.toInt(),
                    posterUrl = props.optJSONObject("Póster")?.let { if (it.isNull("url")) "" else it.optString("url", "") } ?: "",
                    rating = numberOf(props, "Nota"),
                    overview = overview.toString(),
                    who = selectName(props, "Quién"),
                    watched = watched,
                    watchedAt = if (watched) lastEdited else null,
                    tmdbId = numberOf(props, "TMDB ID")?.toLong(),
                    createdAt = parseIsoTimestamp(item.optString("created_time"))
                )
            )
        }
        return out.filter { !it.watched || it.watchedAt == null || System.currentTimeMillis() - it.watchedAt < SEVEN_DAYS_MS }
            .sortedWith(compareBy<WatchItem> { it.watched }.thenByDescending { it.createdAt })
    }

    private fun watchProperties(result: TmdbResult, who: String?): JSONObject {
        val props = JSONObject()
            .put("Name", titleProperty(result.title))
            .put("Quién", selectProperty(who))
            .put("TMDB ID", JSONObject().put("number", if (result.id == 0L) JSONObject.NULL else result.id))
            .put("Año", JSONObject().put("number", result.year ?: JSONObject.NULL))
            .put("Nota", JSONObject().put("number", result.rating?.let { Math.round(it * 10) / 10.0 } ?: JSONObject.NULL))
            .put("Póster", JSONObject().put("url", result.posterUrl.ifEmpty { JSONObject.NULL }))
            .put("Enlace", JSONObject().put("url", if (result.id == 0L) JSONObject.NULL else result.link))
            .put(
                "Sinopsis",
                JSONObject().put(
                    "rich_text",
                    org.json.JSONArray().put(JSONObject().put("text", JSONObject().put("content", result.overview.take(1900))))
                )
            )
        return props
    }

    suspend fun createWatch(dbId: String, result: TmdbResult, who: String?, token: String): String? = withContext(Dispatchers.IO) {
        createPage(dbId, watchProperties(result, who).put("Visto", JSONObject().put("checkbox", false)), token)
    }

    suspend fun updateWatch(pageId: String, result: TmdbResult, who: String?, token: String): Boolean = withContext(Dispatchers.IO) {
        patchPage(pageId, watchProperties(result, who), token)
    }

    suspend fun updateWatchWho(pageId: String, who: String?, token: String): Boolean = withContext(Dispatchers.IO) {
        patchPage(pageId, JSONObject().put("Quién", selectProperty(who)), token)
    }

    suspend fun updateWatched(pageId: String, watched: Boolean, token: String): Boolean = withContext(Dispatchers.IO) {
        patchPage(pageId, JSONObject().put("Visto", JSONObject().put("checkbox", watched)), token)
    }

    private fun fetchCalendar(token: String): List<CalendarEvent> {
        val out = mutableListOf<CalendarEvent>()
        for (item in queryAll(DB_CALENDAR, token)) {
            val props = item.optJSONObject("properties") ?: continue
            val title = titleOf(props, "Name")
            val dateStart = props.optJSONObject("Date")?.optJSONObject("date")?.optString("start", "") ?: ""
            val day = dateStart.take(10).split("-").getOrNull(2)?.toIntOrNull() ?: 1
            val type = selectName(props, "Type")
            val repeats = props.optJSONObject(REPEAT_PROPERTY)?.optBoolean("checkbox", false) ?: false
            if (title.isNotEmpty()) out.add(CalendarEvent(item.optString("id"), title, dateStart.take(10), day, type, repeats))
        }
        return out
    }

    private fun fetchTodos(token: String): List<FamilyTask> {
        val out = mutableListOf<FamilyTask>()
        val now = System.currentTimeMillis()
        for (item in queryAll(DB_TODO, token)) {
            val props = item.optJSONObject("properties") ?: continue
            val title = titleOf(props, "Qué")
            val who = selectName(props, "Quién")
            val status = props.optJSONObject("Status")?.optJSONObject("status")?.optString("name", "Not started") ?: "Not started"
            val isDone = status.equals("Done", ignoreCase = true) || status.equals("Completed", ignoreCase = true)
            val lastEdited = parseIsoTimestamp(item.optString("last_edited_time"))
            if (isDone && now - lastEdited > SEVEN_DAYS_MS) continue
            if (title.isNotEmpty()) out.add(FamilyTask(item.optString("id"), title, who, isDone, if (isDone) lastEdited else null))
        }
        return out.sortedBy { it.completed }
    }

    private fun fetchGroceries(token: String): List<GroceryItem> {
        val out = mutableListOf<GroceryItem>()
        val now = System.currentTimeMillis()
        for (item in queryAll(DB_SHOPPING, token)) {
            val props = item.optJSONObject("properties") ?: continue
            val title = titleOf(props, "Name")
            val isPurchased = props.optJSONObject("Purchased")?.optBoolean("checkbox", false) ?: false
            val lastEdited = parseIsoTimestamp(item.optString("last_edited_time"))
            if (isPurchased && now - lastEdited > SEVEN_DAYS_MS) continue
            if (title.isNotEmpty()) out.add(GroceryItem(item.optString("id"), title, selectName(props, "Type"), isPurchased, if (isPurchased) lastEdited else null))
        }
        return out.sortedBy { it.completed }
    }

    private fun patchPage(pageId: String, properties: JSONObject, token: String): Boolean = try {
        val body = JSONObject().put("properties", properties).toString().toRequestBody(JSON)
        client.newCall(request("$NOTION_BASE/pages/$pageId", token).patch(body).build()).execute().use { it.isSuccessful }
    } catch (e: Exception) { false }

    private fun createPage(dbId: String, properties: JSONObject, token: String): String? = try {
        val body = JSONObject()
            .put("parent", JSONObject().put("database_id", dbId))
            .put("properties", properties)
            .toString().toRequestBody(JSON)
        client.newCall(request("$NOTION_BASE/pages", token).post(body).build()).execute().use { resp ->
            if (resp.isSuccessful) JSONObject(resp.body?.string() ?: "{}").optString("id").ifEmpty { null } else null
        }
    } catch (e: Exception) { null }

    suspend fun updateTaskStatus(pageId: String, isDone: Boolean, token: String): Boolean = withContext(Dispatchers.IO) {
        val status = JSONObject().put("status", JSONObject().put("name", if (isDone) "Done" else "Not started"))
        patchPage(pageId, JSONObject().put("Status", status), token)
    }

    suspend fun updateGroceryStatus(pageId: String, isPurchased: Boolean, token: String): Boolean = withContext(Dispatchers.IO) {
        patchPage(pageId, JSONObject().put("Purchased", JSONObject().put("checkbox", isPurchased)), token)
    }

    suspend fun createTask(text: String, who: String?, token: String): String? = withContext(Dispatchers.IO) {
        val props = JSONObject()
            .put("Qué", titleProperty(text))
            .put("Status", JSONObject().put("status", JSONObject().put("name", "Not started")))
        if (!who.isNullOrBlank()) props.put("Quién", selectProperty(who))
        createPage(DB_TODO, props, token)
    }

    suspend fun createGrocery(text: String, type: String?, token: String): String? = withContext(Dispatchers.IO) {
        val props = JSONObject()
            .put("Name", titleProperty(text))
            .put("Purchased", JSONObject().put("checkbox", false))
        if (!type.isNullOrBlank()) props.put("Type", selectProperty(type))
        createPage(DB_SHOPPING, props, token)
    }

    suspend fun createCalendarEvent(text: String, date: String, type: String?, repeatsYearly: Boolean, token: String): String? = withContext(Dispatchers.IO) {
        val props = JSONObject()
            .put("Name", titleProperty(text))
            .put("Date", JSONObject().put("date", JSONObject().put("start", date)))
            .put(REPEAT_PROPERTY, JSONObject().put("checkbox", repeatsYearly))
        if (!type.isNullOrBlank()) props.put("Type", selectProperty(type))
        createPage(DB_CALENDAR, props, token)
    }

    suspend fun updateTask(pageId: String, text: String, who: String?, token: String): Boolean = withContext(Dispatchers.IO) {
        patchPage(pageId, JSONObject().put("Qué", titleProperty(text)).put("Quién", selectProperty(who)), token)
    }

    suspend fun updateGrocery(pageId: String, text: String, type: String?, token: String): Boolean = withContext(Dispatchers.IO) {
        patchPage(pageId, JSONObject().put("Name", titleProperty(text)).put("Type", selectProperty(type)), token)
    }

    suspend fun updateCalendarEvent(pageId: String, text: String, type: String?, repeatsYearly: Boolean, token: String): Boolean = withContext(Dispatchers.IO) {
        patchPage(
            pageId,
            JSONObject()
                .put("Name", titleProperty(text))
                .put("Type", selectProperty(type))
                .put(REPEAT_PROPERTY, JSONObject().put("checkbox", repeatsYearly)),
            token
        )
    }
}
