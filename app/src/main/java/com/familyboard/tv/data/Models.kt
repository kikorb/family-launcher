package com.familyboard.tv.data

import android.graphics.Bitmap

data class CalendarEvent(
    val id: String,
    val title: String,
    val date: String,
    val dayOfMonth: Int,
    val type: String,
    val repeatsYearly: Boolean = false
)

data class FamilyTask(
    val id: String,
    val text: String,
    val assignee: String,
    val completed: Boolean,
    val completedAt: Long? = null
)

data class GroceryItem(
    val id: String,
    val text: String,
    val category: String,
    val completed: Boolean,
    val completedAt: Long? = null
)

data class InstalledApp(
    val id: String,
    val name: String,
    val packageName: String,
    val color: Long,
    val iconEmoji: String,
    val isFavorite: Boolean,
    val iconBitmap: Bitmap? = null
)

data class WeatherInfo(
    val tempC: Int,
    val condition: String,
    val iconEmoji: String
)

enum class ActiveSection {
    CENTER,
    MONTH,
    LISTS,
    APPS,
    WATCH,
    SETTINGS
}

enum class CenterFocusArea {
    DOCK,
    ARROW_UP,
    ARROW_DOWN,
    ARROW_LEFT,
    ARROW_RIGHT
}

data class WallpaperItem(
    val id: String,
    val name: String,
    val assetPath: String,
    val subtitle: String = ""
)

private const val SEVEN_DAYS_MS = 7L * 24L * 60L * 60L * 1000L

fun List<FamilyTask>.visibleTasks(now: Long = System.currentTimeMillis()): List<FamilyTask> =
    filter { !it.completed || it.completedAt == null || now - it.completedAt < SEVEN_DAYS_MS }

fun List<GroceryItem>.visibleGroceries(now: Long = System.currentTimeMillis()): List<GroceryItem> =
    filter { !it.completed || it.completedAt == null || now - it.completedAt < SEVEN_DAYS_MS }

val BUILT_IN_WALLPAPERS = listOf(
    WallpaperItem("fedora37", "Fedora 37", "file:///android_asset/wallpaper_fedora37.jpg", "Incluido"),
    WallpaperItem("fedora42", "Fedora 42", "file:///android_asset/wallpaper_fedora42.jpg", "Incluido"),
    WallpaperItem("fedora43", "Fedora 43", "file:///android_asset/wallpaper_fedora43.jpg", "Incluido")
)

data class NotionOptions(
    val who: List<String> = emptyList(),
    val groceryTypes: List<String> = emptyList(),
    val eventTypes: List<String> = emptyList(),
    val watchWho: List<String> = emptyList()
)

enum class EntryKind { TASK, GROCERY, EVENT, MOVIE, SHOW, WALLPAPER }

data class EntryForm(
    val kind: EntryKind,
    val editId: String? = null,
    val initialText: String = "",
    val initialOption: String = "",
    val dateStr: String? = null,
    val initialRepeat: Boolean = false
)

object WallpaperCatalog {
    var builtIns: List<WallpaperItem> = BUILT_IN_WALLPAPERS
        private set

    fun load(bundledAssets: Set<String>) {
        builtIns = BUILT_IN_WALLPAPERS.filter { it.assetPath.substringAfterLast('/') in bundledAssets }
            .ifEmpty { BUILT_IN_WALLPAPERS.take(1) }
    }
}

data class WallpaperResult(
    val id: String,
    val source: String,
    val thumbUrl: String,
    val fullUrl: String,
    val label: String
)

data class WallpaperSearchUi(
    val query: String,
    val results: List<WallpaperResult>?,
    val hasPrev: Boolean = false,
    val hasMore: Boolean = false,
    val busy: String? = null,
    val message: String? = null,
    val failed: Boolean = false
)

fun WallpaperItem.isCustom(): Boolean = !assetPath.startsWith("file:///android_asset/")

private fun isLeapYear(year: Int) = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

fun CalendarEvent.occursOn(dateStr: String): Boolean {
    if (date == dateStr) return true
    if (!repeatsYearly || date.length < 10 || dateStr.length < 10) return false
    val firstYear = date.substring(0, 4).toIntOrNull() ?: return false
    val year = dateStr.substring(0, 4).toIntOrNull() ?: return false
    if (year < firstYear) return false
    val monthDay = date.substring(5, 10)
    val targetMonthDay = dateStr.substring(5, 10)
    return monthDay == targetMonthDay ||
        (monthDay == "02-29" && targetMonthDay == "02-28" && !isLeapYear(year))
}

data class WatchItem(
    val id: String,
    val title: String,
    val year: Int?,
    val posterUrl: String,
    val rating: Double?,
    val overview: String,
    val who: String,
    val watched: Boolean,
    val watchedAt: Long? = null,
    val tmdbId: Long? = null,
    val createdAt: Long = 0L
)

data class TmdbResult(
    val id: Long,
    val title: String,
    val year: Int?,
    val posterUrl: String,
    val rating: Double?,
    val overview: String,
    val isShow: Boolean
) {
    val link: String get() = "https://www.themoviedb.org/${if (isShow) "tv" else "movie"}/$id"
}

fun List<WatchItem>.visibleWatch(now: Long = System.currentTimeMillis()): List<WatchItem> =
    filter { !it.watched || it.watchedAt == null || now - it.watchedAt < 7L * 24L * 60L * 60L * 1000L }
