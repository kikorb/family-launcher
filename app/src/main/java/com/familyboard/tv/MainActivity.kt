package com.familyboard.tv

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.familyboard.tv.data.*
import com.familyboard.tv.ui.HomeScreen
import com.familyboard.tv.ui.VoiceInputMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

private const val MAX_FAVORITES = 5

class MainActivity : ComponentActivity() {

    private val defaultFavorites = listOf(
        "com.google.android.youtube.tv",
        "com.amazon.amazonvideo.livingroom",
        "com.disney.disneyplus",
        "com.netflix.ninja",
        "com.maxrave.simpmusic"
    )

    private val speechTargets = listOf(
        ComponentName("com.google.android.tts", "com.google.android.apps.speech.tts.googletts.service.GoogleTTSActivity"),
        ComponentName("com.google.android.katniss", "com.google.android.apps.tvsearch.voice.voiceinput.VoiceInputActivity"),
        null
    )

    private val activeSectionState = mutableStateOf(ActiveSection.CENTER)
    private val centerFocusState = mutableStateOf(CenterFocusArea.DOCK)
    private val dockIndexState = mutableIntStateOf(0)
    private val settingsIndexState = mutableIntStateOf(0)
    private val appsGridIndexState = mutableIntStateOf(0)
    private val monthDayIndexState = mutableIntStateOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    private val listColumnState = mutableIntStateOf(0)
    private val taskIndexState = mutableIntStateOf(0)
    private val groceryIndexState = mutableIntStateOf(0)

    private val isDarkModeState = mutableStateOf(true)
    private val wallpaperState = mutableStateOf(WallpaperCatalog.items.first().assetPath)

    private val appsState = mutableStateOf<List<InstalledApp>>(emptyList())
    private var installedPackages: Set<String> = emptySet()

    private val eventsListState = mutableStateListOf<CalendarEvent>()
    private val todosListState = mutableStateListOf<FamilyTask>()
    private val groceriesListState = mutableStateListOf<GroceryItem>()

    private val voiceInputModeState = mutableStateOf<VoiceInputMode>(VoiceInputMode.None)
    private val voiceListeningState = mutableStateOf(false)
    private val entryFormState = mutableStateOf<EntryForm?>(null)
    private val optionsState = mutableStateOf(NotionOptions())
    private val tasksEditModeState = mutableStateOf(false)
    private val groceriesEditModeState = mutableStateOf(false)
    private val calendarChoiceDateState = mutableStateOf<String?>(null)
    private val calendarChoiceIndexState = mutableIntStateOf(0)

    private lateinit var speechLauncher: ActivityResultLauncher<Intent>

    private val prefs by lazy { getSharedPreferences("family_board", Context.MODE_PRIVATE) }

    private val favoritePackagesState = mutableStateOf<List<String>>(emptyList())
    private val appsEditModeState = mutableStateOf(false)
    private val favoritesMessageState = mutableStateOf<String?>(null)
    private var favoritesMessageJob: kotlinx.coroutines.Job? = null

    private val favorites: List<InstalledApp>
        get() = favoritePackagesState.value.mapNotNull { pkg -> appsState.value.firstOrNull { it.packageName == pkg } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WallpaperCatalog.load(assets.list("")?.toSet().orEmpty())
        wallpaperState.value = WallpaperCatalog.items.first().assetPath

        isDarkModeState.value = prefs.getBoolean("dark", true)
        val savedWallpaper = prefs.getString("wallpaper", null)
        if (savedWallpaper != null && WallpaperCatalog.items.any { it.assetPath == savedWallpaper }) {
            wallpaperState.value = savedWallpaper
        }

        optionsState.value = NotionOptions(
            who = loadOptions("opt_who"),
            groceryTypes = loadOptions("opt_grocery_types"),
            eventTypes = loadOptions("opt_event_types")
        )

        val savedFavorites = prefs.getString("favorites", null)
        favoritePackagesState.value =
            if (savedFavorites == null) defaultFavorites
            else savedFavorites.split(",").filter { it.isNotBlank() }.take(MAX_FAVORITES)

        speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val mode = voiceInputModeState.value
            voiceListeningState.value = false
            voiceInputModeState.value = VoiceInputMode.None
            if (result.resultCode == RESULT_OK) {
                val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim()
                if (!text.isNullOrBlank()) addEntry(mode, text)
            }
        }

        refreshInstalledApps()

        setContent {
            var weather by remember { mutableStateOf(WeatherInfo(21, "Soleado", "☀️")) }

            LaunchedEffect(Unit) {
                while (true) {
                    runCatching { weather = WeatherClient.fetchGranadaWeather() }
                    val synced = BuildConfig.PERSONAL_NOTION_TOKEN.isNotEmpty() && syncNotionData()
                    delay(if (synced) 10 * 60 * 1000L else 30 * 1000L)
                }
            }

            HomeScreen(
                installedApps = appsState.value,
                dockApps = favorites,
                favoritePackages = favoritePackagesState.value,
                appsEditMode = appsEditModeState.value,
                favoritesMessage = favoritesMessageState.value,
                onToggleAppsEditMode = { toggleAppsEditMode() },
                onToggleFavorite = { toggleFavorite(it) },
                events = eventsListState,
                todos = todosListState,
                groceries = groceriesListState,
                weather = weather,
                activeSection = activeSectionState.value,
                onActiveSectionChange = {
                    activeSectionState.value = it
                    appsEditModeState.value = false
                    if (it == ActiveSection.CENTER) centerFocusState.value = CenterFocusArea.DOCK
                },
                centerFocus = centerFocusState.value,
                onCenterFocusChange = { centerFocusState.value = it },
                dockFocusedIndex = dockIndexState.intValue,
                onDockIndexChange = { dockIndexState.intValue = it },
                settingsFocusedIndex = settingsIndexState.intValue,
                onSettingsIndexChange = { settingsIndexState.intValue = it },
                appsGridFocusedIndex = appsGridIndexState.intValue,
                onAppsGridIndexChange = { appsGridIndexState.intValue = it },
                monthDayFocusedIndex = monthDayIndexState.intValue,
                onMonthDayIndexChange = { monthDayIndexState.intValue = it },
                listFocusedColumn = listColumnState.intValue,
                onListColumnChange = { listColumnState.intValue = it },
                taskFocusedIndex = taskIndexState.intValue,
                onTaskIndexChange = { taskIndexState.intValue = it },
                groceryFocusedIndex = groceryIndexState.intValue,
                onGroceryIndexChange = { groceryIndexState.intValue = it },
                isDarkMode = isDarkModeState.value,
                onToggleDarkMode = { setDarkMode(!isDarkModeState.value) },
                currentWallpaper = wallpaperState.value,
                onSelectWallpaper = { setWallpaper(it) },
                onLaunchApp = { pkg -> launchAppPackage(pkg) },
                onToggleTask = { toggleTask(it) },
                onToggleGrocery = { toggleGrocery(it) },
                voiceInputMode = voiceInputModeState.value,
                voiceListening = voiceListeningState.value,
                onStartVoiceTask = { startVoice(VoiceInputMode.AddTask) },
                onStartVoiceGrocery = { startVoice(VoiceInputMode.AddGrocery) },
                onDismissVoice = {
                    voiceInputModeState.value = VoiceInputMode.None
                    voiceListeningState.value = false
                },
                onStartTextTask = { entryFormState.value = EntryForm(EntryKind.TASK) },
                onStartTextGrocery = { entryFormState.value = EntryForm(EntryKind.GROCERY) },
                tasksEditMode = tasksEditModeState.value,
                groceriesEditMode = groceriesEditModeState.value,
                onToggleTasksEdit = { tasksEditModeState.value = !tasksEditModeState.value },
                onToggleGroceriesEdit = { groceriesEditModeState.value = !groceriesEditModeState.value },
                onEditTask = { editTask(it) },
                onEditGrocery = { editGrocery(it) },
                onOpenCalendarChoice = { openCalendarChoice(it) },
                calendarChoiceDate = calendarChoiceDateState.value,
                calendarChoiceIndex = calendarChoiceIndexState.intValue,
                calendarChoiceEvents = calendarChoiceEvents(),
                onCalendarChoose = { executeCalendarChoice(it) },
                onDismissCalendarChoice = { calendarChoiceDateState.value = null },
                entryForm = entryFormState.value,
                taskWhoOptions = optionsState.value.who,
                groceryTypeOptions = optionsState.value.groceryTypes,
                eventTypeOptions = optionsState.value.eventTypes,
                onSubmitEntry = { text, option -> submitEntry(text, option) },
                onCancelEntry = { entryFormState.value = null }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshInstalledApps()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        resetToHome()
    }

    private fun toggleAppsEditMode() {
        appsEditModeState.value = !appsEditModeState.value
        showFavoritesMessage(null)
    }

    private fun showFavoritesMessage(message: String?) {
        favoritesMessageJob?.cancel()
        favoritesMessageState.value = message
        if (message != null) {
            favoritesMessageJob = lifecycleScope.launch {
                delay(3000)
                favoritesMessageState.value = null
            }
        }
    }

    private fun toggleFavorite(packageName: String) {
        val current = favoritePackagesState.value
        val updated = when {
            packageName in current -> current - packageName
            current.size >= MAX_FAVORITES -> {
                showFavoritesMessage("Ya tienes $MAX_FAVORITES favoritas. Quita una antes de añadir otra.")
                return
            }
            else -> current + packageName
        }
        favoritePackagesState.value = updated
        prefs.edit().putString("favorites", updated.joinToString(",")).apply()
        showFavoritesMessage(null)
        val dockSize = favorites.size
        if (dockIndexState.intValue >= dockSize) dockIndexState.intValue = (dockSize - 1).coerceAtLeast(0)
    }

    private fun resetToHome() {
        appsEditModeState.value = false
        tasksEditModeState.value = false
        groceriesEditModeState.value = false
        entryFormState.value = null
        calendarChoiceDateState.value = null
        voiceInputModeState.value = VoiceInputMode.None
        voiceListeningState.value = false
        activeSectionState.value = ActiveSection.CENTER
        centerFocusState.value = CenterFocusArea.DOCK
        dockIndexState.intValue = 0
    }

    private fun setDarkMode(dark: Boolean) {
        isDarkModeState.value = dark
        prefs.edit().putBoolean("dark", dark).apply()
    }

    private fun setWallpaper(path: String) {
        wallpaperState.value = path
        prefs.edit().putString("wallpaper", path).apply()
    }

    private fun startVoice(mode: VoiceInputMode) {
        voiceInputModeState.value = mode
        val prompt = when (mode) {
            is VoiceInputMode.AddTask -> "Dicta una nueva tarea familiar"
            is VoiceInputMode.AddGrocery -> "Dicta un artículo para la lista de compra"
            is VoiceInputMode.AddCalendarEvent -> "Dicta el evento para ese día"
            else -> return
        }
        launchSpeechRecognizer(prompt)
    }

    private fun launchSpeechRecognizer(prompt: String) {
        voiceListeningState.value = true
        val base = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-ES")
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        for (target in speechTargets) {
            try {
                speechLauncher.launch(Intent(base).apply { if (target != null) component = target })
                return
            } catch (e: Exception) {
            }
        }
        voiceListeningState.value = false
        voiceInputModeState.value = VoiceInputMode.None
    }

    private fun calendarChoiceEvents(): List<CalendarEvent> {
        val date = calendarChoiceDateState.value ?: return emptyList()
        return eventsListState.filter { it.date == date && !it.id.startsWith("local_") }
    }

    private fun openCalendarChoice(dateStr: String) {
        calendarChoiceIndexState.intValue = 0
        calendarChoiceDateState.value = dateStr
    }

    private fun executeCalendarChoice(index: Int) {
        val date = calendarChoiceDateState.value ?: return
        val events = calendarChoiceEvents()
        calendarChoiceDateState.value = null
        when {
            index == 0 -> startVoice(VoiceInputMode.AddCalendarEvent(date))
            index == 1 -> entryFormState.value = EntryForm(EntryKind.EVENT, dateStr = date)
            else -> events.getOrNull(index - 2)?.let {
                entryFormState.value = EntryForm(EntryKind.EVENT, it.id, it.title, it.type, date)
            }
        }
    }

    private fun editTask(visibleIndex: Int) {
        val item = todosListState.visibleTasks().getOrNull(visibleIndex) ?: return
        if (item.id.startsWith("local_")) return
        entryFormState.value = EntryForm(EntryKind.TASK, item.id, item.text, item.assignee)
    }

    private fun editGrocery(visibleIndex: Int) {
        val item = groceriesListState.visibleGroceries().getOrNull(visibleIndex) ?: return
        if (item.id.startsWith("local_")) return
        entryFormState.value = EntryForm(EntryKind.GROCERY, item.id, item.text, item.category)
    }

    private fun submitEntry(text: String, option: String) {
        val form = entryFormState.value ?: return
        entryFormState.value = null
        val token = BuildConfig.PERSONAL_NOTION_TOKEN
        if (form.editId == null) {
            val mode = when (form.kind) {
                EntryKind.TASK -> VoiceInputMode.AddTask
                EntryKind.GROCERY -> VoiceInputMode.AddGrocery
                EntryKind.EVENT -> VoiceInputMode.AddCalendarEvent(form.dateStr ?: return)
            }
            addEntry(mode, text, option)
            return
        }
        when (form.kind) {
            EntryKind.TASK -> {
                val idx = todosListState.indexOfFirst { it.id == form.editId }
                if (idx >= 0) todosListState[idx] = todosListState[idx].copy(text = text, assignee = option)
                if (token.isNotEmpty()) lifecycleScope.launch { NotionClient.updateTask(form.editId, text, option, token) }
            }
            EntryKind.GROCERY -> {
                val idx = groceriesListState.indexOfFirst { it.id == form.editId }
                if (idx >= 0) groceriesListState[idx] = groceriesListState[idx].copy(text = text, category = option)
                if (token.isNotEmpty()) lifecycleScope.launch { NotionClient.updateGrocery(form.editId, text, option, token) }
            }
            EntryKind.EVENT -> {
                val idx = eventsListState.indexOfFirst { it.id == form.editId }
                if (idx >= 0) eventsListState[idx] = eventsListState[idx].copy(title = text, type = option)
                if (token.isNotEmpty()) lifecycleScope.launch { NotionClient.updateCalendarEvent(form.editId, text, option, token) }
            }
        }
    }

    private fun loadOptions(key: String): List<String> =
        prefs.getString(key, "")?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()

    private fun saveOptions(options: NotionOptions) {
        prefs.edit()
            .putString("opt_who", options.who.joinToString("\n"))
            .putString("opt_grocery_types", options.groceryTypes.joinToString("\n"))
            .putString("opt_event_types", options.eventTypes.joinToString("\n"))
            .apply()
    }

    private fun addEntry(mode: VoiceInputMode, text: String, option: String = "") {
        val token = BuildConfig.PERSONAL_NOTION_TOKEN
        val localId = "local_${System.nanoTime()}"
        when (mode) {
            is VoiceInputMode.AddTask -> {
                todosListState.add(0, FamilyTask(localId, text, option, false))
                taskIndexState.intValue = 0
                persistCreate(token, localId, { NotionClient.createTask(text, option, token) }) { newId ->
                    val idx = todosListState.indexOfFirst { it.id == localId }
                    if (idx >= 0) todosListState[idx] = todosListState[idx].copy(id = newId)
                }
            }
            is VoiceInputMode.AddGrocery -> {
                groceriesListState.add(0, GroceryItem(localId, text, option, false))
                groceryIndexState.intValue = 0
                persistCreate(token, localId, { NotionClient.createGrocery(text, option, token) }) { newId ->
                    val idx = groceriesListState.indexOfFirst { it.id == localId }
                    if (idx >= 0) groceriesListState[idx] = groceriesListState[idx].copy(id = newId)
                }
            }
            is VoiceInputMode.AddCalendarEvent -> {
                val day = mode.dateStr.split("-").lastOrNull()?.toIntOrNull() ?: 1
                eventsListState.add(CalendarEvent(localId, text, mode.dateStr, day, option))
                persistCreate(token, localId, { NotionClient.createCalendarEvent(text, mode.dateStr, option, token) }) { newId ->
                    val idx = eventsListState.indexOfFirst { it.id == localId }
                    if (idx >= 0) eventsListState[idx] = eventsListState[idx].copy(id = newId)
                }
            }
            is VoiceInputMode.None -> Unit
        }
    }

    private fun persistCreate(token: String, localId: String, create: suspend () -> String?, onCreated: (String) -> Unit) {
        if (token.isEmpty()) return
        lifecycleScope.launch {
            val newId = create()
            if (newId != null) onCreated(newId)
        }
    }

    private fun toggleTask(visibleIndex: Int) {
        val item = todosListState.visibleTasks().getOrNull(visibleIndex) ?: return
        val idx = todosListState.indexOfFirst { it.id == item.id }
        if (idx < 0) return
        val next = !item.completed
        todosListState[idx] = item.copy(completed = next, completedAt = if (next) System.currentTimeMillis() else null)
        val token = BuildConfig.PERSONAL_NOTION_TOKEN
        if (token.isNotEmpty() && !item.id.startsWith("local_")) {
            lifecycleScope.launch { NotionClient.updateTaskStatus(item.id, next, token) }
        }
    }

    private fun toggleGrocery(visibleIndex: Int) {
        val item = groceriesListState.visibleGroceries().getOrNull(visibleIndex) ?: return
        val idx = groceriesListState.indexOfFirst { it.id == item.id }
        if (idx < 0) return
        val next = !item.completed
        groceriesListState[idx] = item.copy(completed = next, completedAt = if (next) System.currentTimeMillis() else null)
        val token = BuildConfig.PERSONAL_NOTION_TOKEN
        if (token.isNotEmpty() && !item.id.startsWith("local_")) {
            lifecycleScope.launch { NotionClient.updateGroceryStatus(item.id, next, token) }
        }
    }

    private suspend fun syncNotionData(): Boolean {
        val data = NotionClient.fetchAllFamilyData(BuildConfig.PERSONAL_NOTION_TOKEN)
        data.events?.let { replaceKeepingLocal(eventsListState, it) { e -> e.id } }
        data.todos?.let { replaceKeepingLocal(todosListState, it) { t -> t.id } }
        data.groceries?.let { replaceKeepingLocal(groceriesListState, it) { g -> g.id } }
        data.options?.let {
            optionsState.value = it
            saveOptions(it)
        }
        return data.events != null && data.todos != null && data.groceries != null && data.options != null
    }

    private fun <T> replaceKeepingLocal(target: MutableList<T>, fresh: List<T>, idOf: (T) -> String) {
        val pending = target.filter { idOf(it).startsWith("local_") }
        target.clear()
        target.addAll(pending)
        target.addAll(fresh)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        handleBack()
    }

    private fun handleBack() {
        when {
            entryFormState.value != null -> entryFormState.value = null
            calendarChoiceDateState.value != null -> calendarChoiceDateState.value = null
            voiceListeningState.value -> {
                voiceListeningState.value = false
                voiceInputModeState.value = VoiceInputMode.None
            }
            appsEditModeState.value -> appsEditModeState.value = false
            activeSectionState.value == ActiveSection.LISTS && (tasksEditModeState.value || groceriesEditModeState.value) -> {
                tasksEditModeState.value = false
                groceriesEditModeState.value = false
            }
            activeSectionState.value != ActiveSection.CENTER -> {
                activeSectionState.value = ActiveSection.CENTER
                centerFocusState.value = CenterFocusArea.DOCK
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_ESCAPE) {
            if (event.action == KeyEvent.ACTION_DOWN) handleBack()
            return true
        }
        if (event.action != KeyEvent.ACTION_DOWN) return super.dispatchKeyEvent(event)

        if (entryFormState.value != null) return super.dispatchKeyEvent(event)

        if (calendarChoiceDateState.value != null) {
            val lastIndex = 1 + calendarChoiceEvents().size
            val idx = calendarChoiceIndexState.intValue
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> if (idx == 1) calendarChoiceIndexState.intValue = 0
                KeyEvent.KEYCODE_DPAD_RIGHT -> if (idx == 0) calendarChoiceIndexState.intValue = 1
                KeyEvent.KEYCODE_DPAD_UP -> if (idx >= 2) calendarChoiceIndexState.intValue = if (idx == 2) 0 else idx - 1
                KeyEvent.KEYCODE_DPAD_DOWN -> if (idx <= 1 && lastIndex >= 2) calendarChoiceIndexState.intValue = 2
                    else if (idx in 2 until lastIndex) calendarChoiceIndexState.intValue = idx + 1
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> executeCalendarChoice(idx)
            }
            return true
        }

        if (voiceListeningState.value) return super.dispatchKeyEvent(event)

        val handled = when (activeSectionState.value) {
            ActiveSection.CENTER -> handleCenterKey(event.keyCode)
            ActiveSection.SETTINGS -> handleSettingsKey(event.keyCode)
            ActiveSection.APPS -> handleAppsKey(event.keyCode)
            ActiveSection.MONTH -> handleMonthKey(event.keyCode)
            ActiveSection.LISTS -> handleListsKey(event.keyCode)
        }
        return handled || super.dispatchKeyEvent(event)
    }

    private fun handleCenterKey(keyCode: Int): Boolean {
        val favs = favorites
        val favsCount = if (favs.isNotEmpty()) favs.size else 1
        val isSelect = keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER
        when (centerFocusState.value) {
            CenterFocusArea.DOCK -> when (keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (dockIndexState.intValue > 0) dockIndexState.intValue--
                    else centerFocusState.value = CenterFocusArea.ARROW_LEFT
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (dockIndexState.intValue < favsCount - 1) dockIndexState.intValue++
                    else centerFocusState.value = CenterFocusArea.ARROW_RIGHT
                }
                KeyEvent.KEYCODE_DPAD_UP -> centerFocusState.value = CenterFocusArea.ARROW_UP
                KeyEvent.KEYCODE_DPAD_DOWN -> centerFocusState.value = CenterFocusArea.ARROW_DOWN
                else -> if (isSelect) {
                    favs.getOrNull(dockIndexState.intValue)?.let { launchAppPackage(it.packageName) }
                } else return false
            }
            CenterFocusArea.ARROW_LEFT -> when {
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> { centerFocusState.value = CenterFocusArea.DOCK; dockIndexState.intValue = 0 }
                keyCode == KeyEvent.KEYCODE_DPAD_LEFT || isSelect -> { activeSectionState.value = ActiveSection.SETTINGS; settingsIndexState.intValue = 0 }
                keyCode == KeyEvent.KEYCODE_DPAD_UP -> centerFocusState.value = CenterFocusArea.ARROW_UP
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> centerFocusState.value = CenterFocusArea.ARROW_DOWN
                else -> return false
            }
            CenterFocusArea.ARROW_RIGHT -> when {
                keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> { centerFocusState.value = CenterFocusArea.DOCK; dockIndexState.intValue = favsCount - 1 }
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || isSelect -> openLists()
                keyCode == KeyEvent.KEYCODE_DPAD_UP -> centerFocusState.value = CenterFocusArea.ARROW_UP
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> centerFocusState.value = CenterFocusArea.ARROW_DOWN
                else -> return false
            }
            CenterFocusArea.ARROW_UP -> when {
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> centerFocusState.value = CenterFocusArea.DOCK
                keyCode == KeyEvent.KEYCODE_DPAD_UP || isSelect -> {
                    activeSectionState.value = ActiveSection.MONTH
                    monthDayIndexState.intValue = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                }
                keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> centerFocusState.value = CenterFocusArea.ARROW_LEFT
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> centerFocusState.value = CenterFocusArea.ARROW_RIGHT
                else -> return false
            }
            CenterFocusArea.ARROW_DOWN -> when {
                keyCode == KeyEvent.KEYCODE_DPAD_UP -> centerFocusState.value = CenterFocusArea.DOCK
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN || isSelect -> {
                    activeSectionState.value = ActiveSection.APPS
                    appsGridIndexState.intValue = 0
                    appsEditModeState.value = false
                }
                keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> centerFocusState.value = CenterFocusArea.ARROW_LEFT
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> centerFocusState.value = CenterFocusArea.ARROW_RIGHT
                else -> return false
            }
        }
        return true
    }

    private fun openLists() {
        tasksEditModeState.value = false
        groceriesEditModeState.value = false
        activeSectionState.value = ActiveSection.LISTS
        listColumnState.intValue = 0
        taskIndexState.intValue = if (todosListState.visibleTasks().isEmpty()) -1 else 0
        groceryIndexState.intValue = if (groceriesListState.visibleGroceries().isEmpty()) -1 else 0
    }

    private fun handleSettingsKey(keyCode: Int): Boolean {
        val wallpapersCount = WallpaperCatalog.items.size
        val idx = settingsIndexState.intValue
        val inThemeCard = idx in 0..3
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP -> settingsIndexState.intValue =
                if (inThemeCard) (if (idx > 0) idx - 1 else 3)
                else if (idx > 10) idx - 1 else 10 + wallpapersCount - 1
            KeyEvent.KEYCODE_DPAD_DOWN -> settingsIndexState.intValue =
                if (inThemeCard) (if (idx < 3) idx + 1 else 0)
                else if (idx < 10 + wallpapersCount - 1) idx + 1 else 10
            KeyEvent.KEYCODE_DPAD_LEFT -> if (!inThemeCard) settingsIndexState.intValue = (idx - 10).coerceIn(0, 3)
            KeyEvent.KEYCODE_DPAD_RIGHT -> if (inThemeCard) settingsIndexState.intValue = 10 + idx.coerceAtMost(wallpapersCount - 1)
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> when (idx) {
                0 -> setDarkMode(true)
                1 -> setDarkMode(false)
                2 -> launchAppPackage("com.android.tv.settings")
                3 -> activeSectionState.value = ActiveSection.CENTER
                else -> WallpaperCatalog.items.getOrNull(idx - 10)?.let { setWallpaper(it.assetPath) }
            }
            else -> return false
        }
        return true
    }

    private fun handleAppsKey(keyCode: Int): Boolean {
        val apps = appsState.value
        val count = apps.size
        if (count == 0) return false
        val idx = appsGridIndexState.intValue
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> if (idx > 0) appsGridIndexState.intValue--
            KeyEvent.KEYCODE_DPAD_RIGHT -> if (idx in 0 until count - 1) appsGridIndexState.intValue++
            KeyEvent.KEYCODE_DPAD_UP -> when {
                idx >= 4 -> appsGridIndexState.intValue -= 4
                idx >= 0 -> appsGridIndexState.intValue = -1
            }
            KeyEvent.KEYCODE_DPAD_DOWN -> when {
                idx == -1 -> appsGridIndexState.intValue = 0
                idx + 4 < count -> appsGridIndexState.intValue += 4
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> when {
                idx == -1 -> toggleAppsEditMode()
                appsEditModeState.value -> apps.getOrNull(idx)?.let { toggleFavorite(it.packageName) }
                else -> apps.getOrNull(idx)?.let { launchAppPackage(it.packageName) }
            }
            else -> return false
        }
        return true
    }

    private fun handleMonthKey(keyCode: Int): Boolean {
        val cal = Calendar.getInstance()
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val day = monthDayIndexState.intValue
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> if (day > 1) monthDayIndexState.intValue--
            KeyEvent.KEYCODE_DPAD_RIGHT -> if (day < daysInMonth) monthDayIndexState.intValue++
            KeyEvent.KEYCODE_DPAD_UP -> if (day > 7) monthDayIndexState.intValue -= 7
            KeyEvent.KEYCODE_DPAD_DOWN -> if (day + 7 <= daysInMonth) monthDayIndexState.intValue += 7
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER ->
                openCalendarChoice("%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, day))
            else -> return false
        }
        return true
    }

    private fun handleListsKey(keyCode: Int): Boolean {
        val sizes = intArrayOf(todosListState.visibleTasks().size, groceriesListState.visibleGroceries().size)
        val col = listColumnState.intValue
        val indexState = if (col == 0) taskIndexState else groceryIndexState
        val idx = indexState.intValue
        val editMode = if (col == 0) tasksEditModeState else groceriesEditModeState

        fun landingIndex(targetCol: Int, from: Int): Int {
            val size = sizes[targetCol]
            return if (size == 0) -1 else from.coerceIn(0, size - 1)
        }

        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> when {
                idx in -2..-1 -> indexState.intValue = idx - 1
                idx == -3 && col == 1 -> { listColumnState.intValue = 0; taskIndexState.intValue = -1 }
                idx >= 0 && col == 1 -> { listColumnState.intValue = 0; taskIndexState.intValue = landingIndex(0, idx) }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> when {
                idx in -3..-2 -> indexState.intValue = idx + 1
                idx == -1 && col == 0 -> { listColumnState.intValue = 1; groceryIndexState.intValue = -3 }
                idx >= 0 && col == 0 -> { listColumnState.intValue = 1; groceryIndexState.intValue = landingIndex(1, idx) }
            }
            KeyEvent.KEYCODE_DPAD_UP -> if (idx >= 0) indexState.intValue = idx - 1
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                val size = sizes[col]
                if (idx < 0) { if (size > 0) indexState.intValue = 0 }
                else if (idx < size - 1) indexState.intValue = idx + 1
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> when {
                idx == -3 -> editMode.value = !editMode.value
                idx == -2 -> entryFormState.value = EntryForm(if (col == 0) EntryKind.TASK else EntryKind.GROCERY)
                idx == -1 -> startVoice(if (col == 0) VoiceInputMode.AddTask else VoiceInputMode.AddGrocery)
                editMode.value -> if (col == 0) editTask(idx) else editGrocery(idx)
                col == 0 -> toggleTask(idx)
                else -> toggleGrocery(idx)
            }
            else -> return false
        }
        return true
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) return drawable.bitmap
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(192) else 128
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(192) else 128
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun refreshInstalledApps() {
        val pm = packageManager
        val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER) }
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val resolveInfos = (pm.queryIntentActivities(leanbackIntent, 0) + pm.queryIntentActivities(launcherIntent, 0))
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != packageName }

        val packages = resolveInfos.map { it.activityInfo.packageName }.toSet()
        if (packages == installedPackages && appsState.value.isNotEmpty()) return
        installedPackages = packages

        val out = mutableListOf<InstalledApp>()
        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            val color = when {
                pkg.contains("simpmusic") -> 0xFFFF2D55
                pkg.contains("netflix") -> 0xFFE50914
                pkg.contains("youtube") -> 0xFFFF0000
                pkg.contains("spotify") -> 0xFF1DB954
                pkg.contains("disney") -> 0xFF113CCF
                pkg.contains("amazon") -> 0xFF00A8E1
                pkg.contains("plex") -> 0xFFE5A00D
                pkg.contains("apple") -> 0xFF1C1917
                pkg.contains("wbd") -> 0xFF002BE7
                else -> 0xFF334155
            }
            val icon = try { info.loadIcon(pm)?.let { drawableToBitmap(it) } } catch (e: Exception) { null }
            out.add(InstalledApp(pkg, info.loadLabel(pm).toString(), pkg, color, "📺", defaultFavorites.contains(pkg), icon))
        }
        if (out.none { it.packageName == "com.android.tv.settings" }) {
            val icon = try { pm.getApplicationIcon("com.android.tv.settings").let { drawableToBitmap(it) } } catch (e: Exception) { null }
            out.add(InstalledApp("settings", "Ajustes TV", "com.android.tv.settings", 0xFF475569, "⚙️", false, icon))
        }

        appsState.value = out.sortedBy { it.name.lowercase() }
        if (dockIndexState.intValue >= favorites.size) dockIndexState.intValue = 0
    }

    private fun launchAppPackage(pkg: String) {
        try {
            if (pkg == "com.android.tv.settings") {
                startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            }
            val launchIntent = packageManager.getLeanbackLaunchIntentForPackage(pkg)
                ?: packageManager.getLaunchIntentForPackage(pkg)
            launchIntent?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        } catch (e: Exception) {
        }
    }
}
