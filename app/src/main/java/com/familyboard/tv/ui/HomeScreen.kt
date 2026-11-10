package com.familyboard.tv.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.familyboard.tv.data.*
import com.familyboard.tv.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private val DrawerSlideIn = tween<androidx.compose.ui.unit.IntOffset>(170, easing = FastOutSlowInEasing)
private val DrawerSlideOut = tween<androidx.compose.ui.unit.IntOffset>(120, easing = FastOutSlowInEasing)
private val DrawerFadeIn = tween<Float>(150, easing = LinearOutSlowInEasing)
private val DrawerFadeOut = tween<Float>(100, easing = FastOutLinearInEasing)

data class WatchState(
    val movies: List<WatchItem> = emptyList(),
    val shows: List<WatchItem> = emptyList(),
    val focusedColumn: Int = 0,
    val movieIndex: Int = 0,
    val showIndex: Int = 0,
    val moviesEdit: Boolean = false,
    val showsEdit: Boolean = false,
    val whoOptions: List<String> = emptyList()
)

data class WatchActions(
    val onToggleMovie: (Int) -> Unit = {},
    val onToggleShow: (Int) -> Unit = {},
    val onEditMovie: (Int) -> Unit = {},
    val onEditShow: (Int) -> Unit = {},
    val onToggleMoviesEdit: () -> Unit = {},
    val onToggleShowsEdit: () -> Unit = {},
    val onDictateMovie: () -> Unit = {},
    val onDictateShow: () -> Unit = {},
    val onTypeMovie: () -> Unit = {},
    val onTypeShow: () -> Unit = {}
)

data class SearchUi(
    val isShow: Boolean,
    val query: String,
    val results: List<TmdbResult>?,
    val failed: Boolean = false
)

@Composable
fun HomeScreen(
    installedApps: List<InstalledApp>,
    dockApps: List<InstalledApp>,
    favoritePackages: List<String>,
    appsEditMode: Boolean,
    favoritesMessage: String?,
    onToggleAppsEditMode: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    events: List<CalendarEvent>,
    todos: List<FamilyTask>,
    groceries: List<GroceryItem>,
    weather: WeatherInfo,
    activeSection: ActiveSection,
    onActiveSectionChange: (ActiveSection) -> Unit,
    centerFocus: CenterFocusArea,
    onCenterFocusChange: (CenterFocusArea) -> Unit,
    dockFocusedIndex: Int,
    onDockIndexChange: (Int) -> Unit,
    settingsFocusedIndex: Int,
    appsGridFocusedIndex: Int,
    monthDayFocusedIndex: Int,
    monthOffset: Int = 0,
    onChangeMonth: (Int) -> Unit = {},
    calendarEditMode: Boolean = false,
    onToggleCalendarEdit: () -> Unit = {},
    listFocusedColumn: Int,
    taskFocusedIndex: Int,
    groceryFocusedIndex: Int,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    currentWallpaper: String,
    wallpapers: List<WallpaperItem>,
    wallpapersEditMode: Boolean = false,
    onWallpaperClick: (WallpaperItem) -> Unit = {},
    onToggleWallpapersEdit: () -> Unit = {},
    onSearchWallpapers: () -> Unit = {},
    onDictateWallpapers: () -> Unit = {},
    wallpaperSearch: WallpaperSearchUi? = null,
    wallpaperSearchIndex: Int = 0,
    onWallpaperPick: (Int) -> Unit = {},
    onWallpaperDismiss: () -> Unit = {},
    onLaunchApp: (String) -> Unit,
    onToggleTask: (Int) -> Unit,
    onToggleGrocery: (Int) -> Unit,
    voiceInputMode: VoiceInputMode = VoiceInputMode.None,
    voiceListening: Boolean = false,
    onStartVoiceTask: () -> Unit = {},
    onStartVoiceGrocery: () -> Unit = {},
    onDismissVoice: () -> Unit = {},
    onStartTextTask: () -> Unit = {},
    onStartTextGrocery: () -> Unit = {},
    tasksEditMode: Boolean = false,
    groceriesEditMode: Boolean = false,
    onToggleTasksEdit: () -> Unit = {},
    onToggleGroceriesEdit: () -> Unit = {},
    onEditTask: (Int) -> Unit = {},
    onEditGrocery: (Int) -> Unit = {},
    watch: WatchState = WatchState(),
    watchActions: WatchActions = WatchActions(),
    search: SearchUi? = null,
    searchIndex: Int = 0,
    onSearchPick: (Int) -> Unit = {},
    onSearchDismiss: () -> Unit = {},
    onOpenCalendarChoice: (String) -> Unit = {},
    calendarChoiceDate: String? = null,
    calendarChoiceIndex: Int = 0,
    calendarChoiceEvents: List<CalendarEvent> = emptyList(),
    onCalendarChoose: (Int) -> Unit = {},
    onDismissCalendarChoice: () -> Unit = {},
    entryForm: EntryForm? = null,
    taskWhoOptions: List<String> = emptyList(),
    groceryTypeOptions: List<String> = emptyList(),
    eventTypeOptions: List<String> = emptyList(),
    onSubmitEntry: (String, String, Boolean) -> Unit = { _, _, _ -> },
    onCancelEntry: () -> Unit = {}
) {
    val primaryTextColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val secondaryTextColor = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569)
    val defaultArrowColor = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF0F172A).copy(alpha = 0.75f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xFF070B12) else Color(0xFFF1F5F9))
    ) {
        AsyncImage(
            model = currentWallpaper,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDarkMode) {
                        Brush.verticalGradient(listOf(Color(0x66000000), Color(0x22000000), Color(0x77000000)))
                    } else {
                        Brush.verticalGradient(listOf(Color(0x44FFFFFF), Color(0x11FFFFFF), Color(0x55FFFFFF)))
                    }
                )
        )

        if (activeSection == ActiveSection.CENTER) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 64.dp, end = 64.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EdgeArrow("▲", centerFocus == CenterFocusArea.ARROW_UP, defaultArrowColor) {
                        onActiveSectionChange(ActiveSection.MONTH)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    WeekStrip(weather = weather, events = events, isDarkMode = isDarkMode)
                }

                AmbientClockHero(textColor = primaryTextColor)

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RefinedFavoritesDock(
                        apps = dockApps,
                        isDockFocused = centerFocus == CenterFocusArea.DOCK,
                        focusedIndex = dockFocusedIndex,
                        primaryTextColor = primaryTextColor,
                        onSelectIndex = {
                            onCenterFocusChange(CenterFocusArea.DOCK)
                            onDockIndexChange(it)
                        },
                        onLaunch = onLaunchApp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    EdgeArrow("▼", centerFocus == CenterFocusArea.ARROW_DOWN, defaultArrowColor) {
                        onActiveSectionChange(ActiveSection.APPS)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EdgeArrow("◀", centerFocus == CenterFocusArea.ARROW_LEFT, defaultArrowColor) {
                    onActiveSectionChange(ActiveSection.WATCH)
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CountBubble("🎬", watch.movies.visibleWatch().count { !it.watched }, isDarkMode)
                    CountBubble("📺", watch.shows.visibleWatch().count { !it.watched }, isDarkMode)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CountBubble("📋", todos.count { !it.completed }, isDarkMode)
                    CountBubble("🛒", groceries.count { !it.completed }, isDarkMode)
                }
                EdgeArrow("▶", centerFocus == CenterFocusArea.ARROW_RIGHT, defaultArrowColor) {
                    onActiveSectionChange(ActiveSection.LISTS)
                }
            }
        }

        AnimatedVisibility(
            visible = activeSection == ActiveSection.WATCH,
            enter = slideInHorizontally(DrawerSlideIn) { -it / 4 } + fadeIn(DrawerFadeIn),
            exit = slideOutHorizontally(DrawerSlideOut) { -it / 4 } + fadeOut(DrawerFadeOut)
        ) {
            WatchScreen(watch = watch, actions = watchActions, isDarkMode = isDarkMode)
        }

        AnimatedVisibility(
            visible = activeSection == ActiveSection.SETTINGS,
            enter = slideInVertically(DrawerSlideIn) { it / 4 } + fadeIn(DrawerFadeIn),
            exit = slideOutVertically(DrawerSlideOut) { it / 4 } + fadeOut(DrawerFadeOut)
        ) {
            SettingsWallpaperScreen(
                isDarkMode = isDarkMode,
                focusedIndex = settingsFocusedIndex,
                currentWallpaper = currentWallpaper,
                wallpapers = wallpapers,
                wallpapersEditMode = wallpapersEditMode,
                onToggleDarkMode = onToggleDarkMode,
                onWallpaperClick = onWallpaperClick,
                onToggleWallpapersEdit = onToggleWallpapersEdit,
                onSearchWallpapers = onSearchWallpapers,
                onDictateWallpapers = onDictateWallpapers
            )
        }

        AnimatedVisibility(
            visible = activeSection == ActiveSection.MONTH,
            enter = slideInVertically(DrawerSlideIn) { -it / 4 } + fadeIn(DrawerFadeIn),
            exit = slideOutVertically(DrawerSlideOut) { -it / 4 } + fadeOut(DrawerFadeOut)
        ) {
            MonthCalendarScreen(
                events = events,
                isDarkMode = isDarkMode,
                focusedDay = monthDayFocusedIndex,
                monthOffset = monthOffset,
                editMode = calendarEditMode,
                onChangeMonth = onChangeMonth,
                onToggleEditMode = onToggleCalendarEdit,
                onOpenCalendarChoice = onOpenCalendarChoice
            )
        }

        AnimatedVisibility(
            visible = activeSection == ActiveSection.LISTS,
            enter = slideInHorizontally(DrawerSlideIn) { it / 4 } + fadeIn(DrawerFadeIn),
            exit = slideOutHorizontally(DrawerSlideOut) { it / 4 } + fadeOut(DrawerFadeOut)
        ) {
            FamilyListsScreen(
                todos = todos,
                groceries = groceries,
                isDarkMode = isDarkMode,
                focusedColumn = listFocusedColumn,
                focusedTaskIndex = taskFocusedIndex,
                focusedGroceryIndex = groceryFocusedIndex,
                onToggleTask = onToggleTask,
                onToggleGrocery = onToggleGrocery,
                onStartVoiceTask = onStartVoiceTask,
                onStartVoiceGrocery = onStartVoiceGrocery,
                onStartTextTask = onStartTextTask,
                onStartTextGrocery = onStartTextGrocery,
                tasksEditMode = tasksEditMode,
                groceriesEditMode = groceriesEditMode,
                onToggleTasksEdit = onToggleTasksEdit,
                onToggleGroceriesEdit = onToggleGroceriesEdit,
                onEditTask = onEditTask,
                onEditGrocery = onEditGrocery
            )
        }

        AnimatedVisibility(
            visible = activeSection == ActiveSection.APPS,
            enter = slideInVertically(DrawerSlideIn) { it / 4 } + fadeIn(DrawerFadeIn),
            exit = slideOutVertically(DrawerSlideOut) { it / 4 } + fadeOut(DrawerFadeOut)
        ) {
            AllAppsDrawerScreen(
                apps = installedApps,
                isDarkMode = isDarkMode,
                focusedAppIndex = appsGridFocusedIndex,
                favoritePackages = favoritePackages,
                editMode = appsEditMode,
                message = favoritesMessage,
                onToggleEditMode = onToggleAppsEditMode,
                onToggleFavorite = onToggleFavorite,
                onOpenSettings = { onActiveSectionChange(ActiveSection.SETTINGS) },
                onLaunch = onLaunchApp
            )
        }

        if (voiceListening) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
                    .clickable { onDismissVoice() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDarkMode) Color(0xFF1E293B) else Color.White)
                        .border(1.5.dp, FocusCyan, RoundedCornerShape(24.dp))
                        .padding(horizontal = 48.dp, vertical = 36.dp)
                ) {
                    Text(text = "🎙️", fontSize = 48.sp)
                    Text(
                        text = when (voiceInputMode) {
                            is VoiceInputMode.AddTask -> "Dicta una nueva tarea"
                            is VoiceInputMode.AddGrocery -> "Dicta un artículo para la compra"
                            is VoiceInputMode.AddMovie -> "Dicta el título de la película"
                            is VoiceInputMode.AddShow -> "Dicta el título de la serie"
                            is VoiceInputMode.SearchWallpaper -> "Dicta qué fondos buscar"
                            is VoiceInputMode.AddCalendarEvent -> "Dicta el evento para ${voiceInputMode.dateStr}"
                            else -> "Escuchando voz..."
                        },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                    Text(
                        text = "Habla con el micrófono del mando de la TV",
                        fontSize = 15.sp,
                        color = secondaryTextColor
                    )
                }
            }
        }

        if (calendarChoiceDate != null) {
            CalendarChoiceOverlay(
                dateStr = calendarChoiceDate,
                events = calendarChoiceEvents,
                editMode = calendarEditMode,
                selectedIndex = calendarChoiceIndex,
                isDarkMode = isDarkMode,
                onChoose = onCalendarChoose,
                onDismiss = onDismissCalendarChoice
            )
        }

        if (entryForm != null) {
            EntryFormOverlay(
                form = entryForm,
                options = when (entryForm.kind) {
                    EntryKind.TASK -> taskWhoOptions
                    EntryKind.GROCERY -> groceryTypeOptions
                    EntryKind.EVENT -> eventTypeOptions
                    EntryKind.MOVIE, EntryKind.SHOW -> watch.whoOptions
                    EntryKind.WALLPAPER -> emptyList()
                },
                isDarkMode = isDarkMode,
                onSubmit = onSubmitEntry,
                onCancel = onCancelEntry
            )
        }

        if (wallpaperSearch != null) {
            WallpaperSearchOverlay(
                ui = wallpaperSearch,
                selectedIndex = wallpaperSearchIndex,
                isDarkMode = isDarkMode,
                onPick = onWallpaperPick,
                onDismiss = onWallpaperDismiss
            )
        }

        if (search != null) {
            SearchOverlay(
                ui = search,
                selectedIndex = searchIndex,
                isDarkMode = isDarkMode,
                onPick = onSearchPick,
                onDismiss = onSearchDismiss
            )
        }
    }
}

private fun formatDateEs(dateStr: String): String = try {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
    SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "ES")).format(parsed!!).replaceFirstChar { it.uppercase() }
} catch (e: Exception) {
    dateStr
}

@Composable
private fun OverlayButton(
    label: String,
    focused: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .scale(if (focused) 1.06f else 1f)
            .clip(RoundedCornerShape(50.dp))
            .background(if (focused) FocusCyan else if (isDarkMode) Color(0x33FFFFFF) else Color(0x18000000))
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) FocusCyan else if (isDarkMode) Color(0x44FFFFFF) else Color(0x22000000),
                shape = RoundedCornerShape(50.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (focused) Color.Black else if (isDarkMode) Color.White else Color(0xFF0F172A)
        )
    }
}

@Composable
private fun CalendarChoiceOverlay(
    dateStr: String,
    events: List<CalendarEvent>,
    editMode: Boolean,
    selectedIndex: Int,
    isDarkMode: Boolean,
    onChoose: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(if (isDarkMode) Color(0xFF1E293B) else Color.White)
                .border(1.5.dp, FocusCyan, RoundedCornerShape(24.dp))
                .padding(horizontal = 48.dp, vertical = 28.dp)
        ) {
            Text(text = if (editMode) "Editar evento" else "Nuevo evento", fontSize = 14.sp, color = subtextColor)
            Text(text = formatDateEs(dateStr), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = titleColor)
            if (!editMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OverlayButton("🎤 Dictar", selectedIndex == 0, isDarkMode, { onChoose(0) })
                    OverlayButton("⌨ Escribir", selectedIndex == 1, isDarkMode, { onChoose(1) })
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    events.forEachIndexed { i, ev ->
                        OverlayButton(
                            label = "✎ ${ev.title}" + (if (ev.type.isNotBlank()) "  ·  ${ev.type}" else "") + (if (ev.repeatsYearly) "  ↻" else ""),
                            focused = selectedIndex == i + 2,
                            isDarkMode = isDarkMode,
                            onClick = { onChoose(i + 2) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun EntryFormOverlay(
    form: EntryForm,
    options: List<String>,
    isDarkMode: Boolean,
    onSubmit: (String, String, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    var text by remember { mutableStateOf(form.initialText) }
    var repeat by remember { mutableStateOf(form.initialRepeat) }
    var repeatTouched by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(form.initialOption) }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var fieldFocused by remember { mutableStateOf(false) }
    var addFocused by remember { mutableStateOf(false) }
    var cancelFocused by remember { mutableStateOf(false) }
    val optionList = remember(options) { listOf("") + options }
    val chipRequesters = remember(options) { optionList.associateWith { androidx.compose.ui.focus.FocusRequester() } }
    val addRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val cancelRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val toggleRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val stop = androidx.compose.ui.focus.FocusRequester.Cancel
    val hasToggle = form.kind == EntryKind.EVENT
    val belowChips = if (hasToggle) toggleRequester else addRequester
    val selectedChip = if (options.isNotEmpty()) chipRequesters[selected] else null
    val aboveButtons = if (hasToggle) toggleRequester else (selectedChip ?: focusRequester)

    val isEdit = form.editId != null
    val isWatch = form.kind == EntryKind.MOVIE || form.kind == EntryKind.SHOW
    val optionLabel = if (form.kind == EntryKind.TASK || isWatch) "Quién" else "Tipo"
    val heading = when (form.kind) {
        EntryKind.WALLPAPER -> "Buscar fondos de pantalla"
        EntryKind.MOVIE -> if (isEdit) "Editar película" else "Nueva película"
        EntryKind.SHOW -> if (isEdit) "Editar serie" else "Nueva serie"
        EntryKind.TASK -> if (isEdit) "Editar tarea" else "Nueva tarea familiar"
        EntryKind.GROCERY -> if (isEdit) "Editar artículo" else "Nuevo artículo de la compra"
        EntryKind.EVENT -> (if (isEdit) "Editar evento: " else "Nuevo evento: ") + formatDateEs(form.dateStr ?: "")
    }

    fun submit() {
        val value = text.trim()
        if (value.isNotEmpty()) onSubmit(value, selected, repeat && form.kind == EntryKind.EVENT) else onCancel()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .padding(top = 28.dp)
                .fillMaxWidth(0.78f)
                .clip(RoundedCornerShape(24.dp))
                .background(if (isDarkMode) Color(0xFF1E293B) else Color.White)
                .border(1.5.dp, FocusCyan, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Text(text = heading, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = titleColor)

            androidx.compose.foundation.text.BasicTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = titleColor, fontSize = 22.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(FocusCyan),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = {
                    keyboard?.hide()
                    (selectedChip ?: belowChips).requestFocus()
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusProperties {
                        down = selectedChip ?: belowChips
                        up = stop
                        left = stop
                        right = stop
                    }
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        fieldFocused = it.isFocused
                        if (!it.isFocused) keyboard?.hide()
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDarkMode) Color(0x22FFFFFF) else Color(0x0F000000))
                    .border(
                        width = if (fieldFocused) 2.5.dp else 1.dp,
                        color = if (fieldFocused) FocusCyan else subtextColor,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                decorationBox = { inner ->
                    Box {
                        if (text.isEmpty()) Text(text = "Escribe aquí…", fontSize = 22.sp, color = subtextColor)
                        inner()
                    }
                }
            )

            if (options.isNotEmpty()) {
                Text(text = optionLabel, fontSize = 13.sp, color = subtextColor, fontWeight = FontWeight.Medium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    optionList.forEachIndexed { index, option ->
                        var chipFocused by remember { mutableStateOf(false) }
                        val isSelected = option == selected
                        Box(
                            modifier = Modifier
                                .focusRequester(chipRequesters.getValue(option))
                                .focusProperties {
                                    left = if (index > 0) chipRequesters.getValue(optionList[index - 1]) else stop
                                    right = if (index < optionList.lastIndex) chipRequesters.getValue(optionList[index + 1]) else stop
                                    up = focusRequester
                                    down = belowChips
                                }
                                .onFocusChanged { chipFocused = it.isFocused }
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    if (chipFocused) FocusCyan
                                    else if (isSelected) Color(0x4438BDF8)
                                    else if (isDarkMode) Color(0x22FFFFFF) else Color(0x12000000)
                                )
                                .border(
                                    width = if (chipFocused || isSelected) 2.dp else 1.dp,
                                    color = if (chipFocused) FocusCyan else if (isSelected) FocusCyan else if (isDarkMode) Color(0x44FFFFFF) else Color(0x22000000),
                                    shape = RoundedCornerShape(50.dp)
                                )
                                .clickable {
                                    selected = option
                                    if (form.kind == EntryKind.EVENT && form.editId == null && !repeatTouched) {
                                        repeat = option.contains("cumple", ignoreCase = true)
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = (if (isSelected) "✓ " else "") + option.ifEmpty { "Sin asignar" },
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (chipFocused) Color.Black else titleColor
                            )
                        }
                    }
                }
            }

            if (form.kind == EntryKind.EVENT) {
                var repeatFocused by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .focusRequester(toggleRequester)
                        .focusProperties {
                            up = selectedChip ?: focusRequester
                            down = addRequester
                            left = stop
                            right = stop
                        }
                        .onFocusChanged { repeatFocused = it.isFocused }
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (repeatFocused) FocusCyan
                            else if (repeat) Color(0x4438BDF8)
                            else if (isDarkMode) Color(0x22FFFFFF) else Color(0x12000000)
                        )
                        .border(
                            width = if (repeatFocused || repeat) 2.dp else 1.dp,
                            color = if (repeatFocused || repeat) FocusCyan else if (isDarkMode) Color(0x44FFFFFF) else Color(0x22000000),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .clickable {
                            repeat = !repeat
                            repeatTouched = true
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = (if (repeat) "☑" else "☐") + "  ↻ Repetir cada año",
                        fontSize = 15.sp,
                        fontWeight = if (repeat) FontWeight.Bold else FontWeight.Medium,
                        color = if (repeatFocused) Color.Black else titleColor
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OverlayButton(
                    label = when {
                        form.kind == EntryKind.WALLPAPER -> "🔎 Buscar"
                        isWatch && (!isEdit || text.trim() != form.initialText.trim()) -> "🔎 Buscar"
                        isEdit -> "✓ Guardar"
                        else -> "✓ Añadir"
                    },
                    focused = addFocused,
                    isDarkMode = isDarkMode,
                    onClick = { submit() },
                    modifier = Modifier
                        .focusRequester(addRequester)
                        .focusProperties {
                            up = aboveButtons
                            down = stop
                            left = stop
                            right = cancelRequester
                        }
                        .onFocusChanged { addFocused = it.isFocused }
                )
                OverlayButton(
                    label = "✕ Cancelar",
                    focused = cancelFocused,
                    isDarkMode = isDarkMode,
                    onClick = onCancel,
                    modifier = Modifier
                        .focusRequester(cancelRequester)
                        .focusProperties {
                            up = aboveButtons
                            down = stop
                            left = addRequester
                            right = stop
                        }
                        .onFocusChanged { cancelFocused = it.isFocused }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// HOME PIECES: WEEK STRIP, DOCK, EDGE ARROWS, COUNT BUBBLES, CLOCK
// -----------------------------------------------------------------------------

fun eventAccent(type: String): Color = when {
    type.contains("cumple", ignoreCase = true) -> Color(0xFFF43F5E)
    type.contains("fiesta", ignoreCase = true) -> Color(0xFF8B5CF6)
    type.contains("médico", ignoreCase = true) || type.contains("medico", ignoreCase = true) -> Color(0xFF14B8A6)
    type.contains("excursi", ignoreCase = true) -> Color(0xFF10B981)
    type.contains("tarea", ignoreCase = true) -> Color(0xFFF59E0B)
    else -> Color(0xFF0284C7)
}

private fun eventEmoji(type: String): String = when {
    type.contains("cumple", ignoreCase = true) -> "🎂 "
    type.contains("fiesta", ignoreCase = true) -> "🎉 "
    type.contains("médico", ignoreCase = true) || type.contains("medico", ignoreCase = true) -> "🩺 "
    type.contains("excursi", ignoreCase = true) -> "🚌 "
    else -> ""
}

@Composable
private fun EventChip(event: CalendarEvent, isDarkMode: Boolean, compact: Boolean = false) {
    val accent = eventAccent(event.type)
    val shape = RoundedCornerShape(if (compact) 6.dp else 8.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(accent.copy(alpha = if (isDarkMode) 0.40f else 0.26f))
            .border(1.dp, accent.copy(alpha = 0.85f), shape)
            .padding(horizontal = 6.dp, vertical = if (compact) 1.dp else 3.dp)
    ) {
        Text(
            text = (if (compact) "" else eventEmoji(event.type)) + event.title,
            fontSize = if (compact) 9.sp else 10.sp,
            lineHeight = if (compact) 11.sp else 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDarkMode) Color.White else Color(0xFF0F172A),
            maxLines = if (compact) 1 else 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EdgeArrow(symbol: String, focused: Boolean, defaultColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .scale(if (focused) 1.3f else 1f)
            .clip(CircleShape)
            .background(if (focused) Color(0x3338BDF8) else Color.Transparent)
            .border(
                width = if (focused) 2.dp else 0.dp,
                color = if (focused) FocusCyan else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = if (focused) FocusCyan else defaultColor,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun CountBubble(icon: String, count: Int, isDarkMode: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isDarkMode) Color(0x990F172A) else Color(0xE6FFFFFF))
            .border(1.dp, if (isDarkMode) Color(0x33FFFFFF) else Color(0x18000000), RoundedCornerShape(50.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, fontSize = 14.sp)
        Text(
            text = "$count",
            color = if (isDarkMode) Color.White else Color(0xFF0F172A),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class DayPillInfo(
    val dayLabel: String,
    val dayNum: Int,
    val isToday: Boolean,
    val events: List<CalendarEvent>
)

private const val MAX_DAY_CHIPS = 5

@Composable
fun WeekStrip(weather: WeatherInfo, events: List<CalendarEvent>, isDarkMode: Boolean) {
    var dayStamp by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            dayStamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        }
    }
    val eventsSnapshot = events.toList()
    val dayPills = remember(dayStamp, eventsSnapshot) {
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sdfDayName = SimpleDateFormat("EEE", Locale("es", "ES"))
        (0..6).map { offset ->
            val dayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offset) }
            val dateStr = sdfDate.format(dayCal.time)
            DayPillInfo(
                dayLabel = if (offset == 0) "HOY" else sdfDayName.format(dayCal.time).uppercase().take(3),
                dayNum = dayCal.get(Calendar.DAY_OF_MONTH),
                isToday = offset == 0,
                events = eventsSnapshot.filter { it.occursOn(dateStr) }
            )
        }
    }
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val mutedColor = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(end = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = weather.iconEmoji, fontSize = 26.sp, textAlign = TextAlign.Center)
            Text(text = "${weather.tempC}°C", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
        }
        dayPills.forEach { pill ->
            val accent = if (pill.isToday) FocusCyan else null
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (pill.isToday) (if (isDarkMode) Color(0x660284C7) else Color(0xE6E0F2FE))
                        else if (isDarkMode) Color(0x990F172A) else Color(0xE6FFFFFF)
                    )
                    .border(
                        width = if (pill.isToday) 1.5.dp else 1.dp,
                        color = accent ?: if (isDarkMode) Color(0x33FFFFFF) else Color(0x18000000),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pill.dayLabel,
                        fontWeight = FontWeight.Bold,
                        color = if (pill.isToday) (if (isDarkMode) FocusCyan else Color(0xFF0369A1)) else mutedColor,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${pill.dayNum}",
                        fontWeight = FontWeight.Bold,
                        color = if (pill.isToday) (if (isDarkMode) FocusCyan else Color(0xFF0369A1)) else textColor,
                        fontSize = 14.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(if (isDarkMode) Color(0x33FFFFFF) else Color(0x22000000))
                )
                val overflow = pill.events.size > MAX_DAY_CHIPS
                val shown = if (overflow) pill.events.take(MAX_DAY_CHIPS - 1) else pill.events
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    shown.forEach { EventChip(it, isDarkMode) }
                    if (overflow) {
                        Text(
                            text = "+${pill.events.size - shown.size} más",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = mutedColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RefinedFavoritesDock(
    apps: List<InstalledApp>,
    isDockFocused: Boolean,
    focusedIndex: Int,
    primaryTextColor: Color,
    onSelectIndex: (Int) -> Unit,
    onLaunch: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.forEachIndexed { index, app ->
            val isSelected = isDockFocused && index == focusedIndex
            Column(
                modifier = Modifier
                    .scale(if (isSelected) 1.2f else 1f)
                    .clickable {
                        onSelectIndex(index)
                        onLaunch(app.packageName)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (app.iconBitmap != null) Color.Transparent else Color(app.color))
                        .border(
                            width = if (isSelected) 3.5.dp else 1.5.dp,
                            color = if (isSelected) FocusCyan else Color(0x44FFFFFF),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.iconBitmap != null) {
                        Image(
                            bitmap = app.iconBitmap.asImageBitmap(),
                            contentDescription = app.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(text = app.iconEmoji, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = app.name,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) FocusCyan else primaryTextColor,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AmbientClockHero(textColor: Color) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES")) }
    val clockShadow = androidx.compose.ui.graphics.Shadow(
        if (textColor == Color.White) Color(0xAA000000) else Color(0xAAFFFFFF),
        blurRadius = 14f
    )
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            kotlinx.coroutines.delay(15_000L)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = timeFormat.format(now),
            color = textColor,
            fontSize = 88.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-2).sp,
            style = androidx.compose.ui.text.TextStyle(shadow = clockShadow)
        )
        Text(
            text = dateFormat.format(now).replaceFirstChar { it.uppercase() },
            color = textColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            style = androidx.compose.ui.text.TextStyle(shadow = clockShadow)
        )
    }
}

// -----------------------------------------------------------------------------
// DRAWER 1 (LEFT ◀): SETTINGS & PERSONALIZATION (THEME & WALLPAPERS)
// -----------------------------------------------------------------------------

@Composable
fun SettingsWallpaperScreen(
    isDarkMode: Boolean,
    focusedIndex: Int,
    currentWallpaper: String,
    wallpapers: List<WallpaperItem>,
    wallpapersEditMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onWallpaperClick: (WallpaperItem) -> Unit,
    onToggleWallpapersEdit: () -> Unit,
    onSearchWallpapers: () -> Unit,
    onDictateWallpapers: () -> Unit
) {
    val context = LocalContext.current
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDarkMode) Color(0x14FFFFFF) else Color(0xD9FFFFFF)
    val cardBorder = if (isDarkMode) Color(0x22FFFFFF) else Color(0x18000000)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xDD0F172A) else Color(0xE3F8FAFC))
            .padding(ScreenPadding)
    ) {
        Text(
            text = "Ajustes & Personalización",
            color = titleColor,
            fontSize = 30.sp,
            fontWeight = FontWeight.Light
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left Card: Theme & Display
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(1.5.dp, cardBorder, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Text(text = "🌓 Modo de Color", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                Spacer(modifier = Modifier.height(18.dp))

                // Option 0: Dark mode
                val isDarkFocused = focusedIndex == 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(if (isDarkFocused) 1.03f else 1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDarkFocused) Color(0x3338BDF8) else if (isDarkMode) Color(0x180284C7) else Color(0x0A000000))
                        .border(
                            width = if (isDarkFocused) 2.5.dp else if (isDarkMode) 1.5.dp else 1.dp,
                            color = if (isDarkFocused) FocusCyan else if (isDarkMode) Color(0xFF0284C7) else Color(0x18000000),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { if (!isDarkMode) onToggleDarkMode() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = "🌙", fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Modo Oscuro", fontWeight = FontWeight.Bold, color = titleColor, fontSize = 16.sp)
                        Text(text = "Ideal para noche y fondos oscuros", fontSize = 12.sp, color = subtextColor)
                    }
                    if (isDarkMode) {
                        Text(text = "✓", color = FocusCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 1: Light mode
                val isLightFocused = focusedIndex == 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(if (isLightFocused) 1.03f else 1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isLightFocused) Color(0x3338BDF8) else if (!isDarkMode) Color(0x180284C7) else Color(0x0A000000))
                        .border(
                            width = if (isLightFocused) 2.5.dp else if (!isDarkMode) 1.5.dp else 1.dp,
                            color = if (isLightFocused) FocusCyan else if (!isDarkMode) Color(0xFF0284C7) else Color(0x18000000),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { if (isDarkMode) onToggleDarkMode() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = "☀️", fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Modo Claro", fontWeight = FontWeight.Bold, color = titleColor, fontSize = 16.sp)
                        Text(text = "Alto contraste para fondos iluminados", fontSize = 12.sp, color = subtextColor)
                    }
                    if (!isDarkMode) {
                        Text(text = "✓", color = FocusCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Option 2: Direct Shortcut to Android TV Settings
                val isSettingsFocused = focusedIndex == 2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(if (isSettingsFocused) 1.03f else 1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSettingsFocused) Color(0x3338BDF8) else Color(0x12888888))
                        .border(
                            width = if (isSettingsFocused) 2.5.dp else 1.dp,
                            color = if (isSettingsFocused) FocusCyan else cardBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            try {
                                context.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            } catch (e: Exception) {}
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = "⚙️", fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Ajustes de TV", fontWeight = FontWeight.Bold, color = titleColor, fontSize = 16.sp)
                        Text(text = "WiFi, Pantalla, Sonido y Sistema Android", fontSize = 12.sp, color = subtextColor)
                    }
                    Text(text = "▶", color = subtextColor, fontSize = 16.sp)
                }
            }

            val wallpaperListState = rememberLazyListState()
            LaunchedEffect(focusedIndex) {
                if (focusedIndex >= 10) wallpaperListState.ensureVisible(focusedIndex - 10)
            }
            val amber = Color(0xFFFBBF24)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(1.5.dp, cardBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Text(text = "🖼️ Fondos de Pantalla", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                Text(
                    text = if (wallpapersEditMode) "Pulsa OK sobre un fondo tuyo para quitarlo" else "${wallpapers.size} fondos",
                    fontSize = 12.sp,
                    color = if (wallpapersEditMode) amber else subtextColor
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    HeaderChip(
                        label = if (wallpapersEditMode) "✓ Listo" else "✎ Editar",
                        focused = focusedIndex == 7,
                        isDarkMode = isDarkMode,
                        titleColor = titleColor,
                        cardBorder = cardBorder,
                        active = wallpapersEditMode,
                        onClick = onToggleWallpapersEdit
                    )
                    HeaderChip("🔎 Buscar", focusedIndex == 8, isDarkMode, titleColor, cardBorder, onClick = onSearchWallpapers)
                    HeaderChip("🎤 Dictar", focusedIndex == 9, isDarkMode, titleColor, cardBorder, onClick = onDictateWallpapers)
                }
                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    state = wallpaperListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(wallpapers) { wIdx, wall ->
                        val isWallFocused = focusedIndex == 10 + wIdx
                        val isWallSelected = currentWallpaper == wall.assetPath
                        val removable = wallpapersEditMode && wall.isCustom()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(if (isWallFocused) 1.02f else 1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isWallFocused) Color(0x3338BDF8) else if (isWallSelected) Color(0x180284C7) else Color(0x0A000000))
                                .border(
                                    width = if (isWallFocused) 2.5.dp else if (isWallSelected) 1.5.dp else 1.dp,
                                    color = if (isWallFocused) FocusCyan else if (isWallSelected) Color(0xFF0284C7) else if (removable) amber else cardBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onWallpaperClick(wall) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = wall.assetPath,
                                contentDescription = wall.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(width = 80.dp, height = 45.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = wall.name,
                                    fontWeight = FontWeight.Bold,
                                    color = titleColor,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (wall.subtitle.isNotEmpty()) {
                                    Text(text = wall.subtitle, fontSize = 11.sp, color = subtextColor)
                                }
                            }
                            when {
                                removable -> Text(text = "🗑", fontSize = 16.sp, color = amber)
                                isWallSelected -> Text(text = "✓ Activo", color = FocusCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WallpaperSearchOverlay(
    ui: WallpaperSearchUi,
    selectedIndex: Int,
    isDarkMode: Boolean,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val results = ui.results
    val cellCount = (results?.size ?: 0) + (if (ui.hasPrev) 1 else 0) + (if (ui.hasMore) 1 else 0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .background(if (isDarkMode) Color(0xFF1E293B) else Color.White)
                .border(1.5.dp, FocusCyan, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Text(text = "Fondos: «${ui.query}»", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Text(
                text = when {
                    ui.busy != null -> ui.busy
                    ui.message != null -> ui.message
                    results == null -> "Buscando…"
                    ui.failed -> "No se pudo buscar. Revisa la conexión."
                    results.isEmpty() -> "Sin resultados. Prueba con otra búsqueda."
                    else -> "OK pone el fondo en la tele. Puedes elegir varios. Atrás para salir."
                },
                fontSize = 13.sp,
                color = if (ui.message != null) FocusCyan else subtextColor
            )
            if (results != null) {
                val cells = results.map<WallpaperResult, WallpaperResult?> { it } +
                    (if (ui.hasPrev) listOf(null) else emptyList()) +
                    (if (ui.hasMore) listOf(null) else emptyList())
                cells.indices.chunked(5).forEach { rowIdx ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        rowIdx.forEach { i ->
                            val focused = i == selectedIndex
                            val item = cells[i]
                            val navLabel = if (i == results.size && ui.hasPrev) "◀ Antes" else "Más ▶"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(16f / 9f)
                                    .scale(if (focused) 1.05f else 1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDarkMode) Color(0x22FFFFFF) else Color(0x14000000))
                                    .border(
                                        width = if (focused) 3.dp else 1.dp,
                                        color = if (focused) FocusCyan else if (isDarkMode) Color(0x33FFFFFF) else Color(0x22000000),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onPick(i) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (item != null) {
                                    AsyncImage(
                                        model = item.thumbUrl,
                                        contentDescription = item.label,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(text = navLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = titleColor)
                                }
                            }
                        }
                        repeat(5 - rowIdx.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SHARED HELPERS
// -----------------------------------------------------------------------------

private val ScreenPadding = PaddingValues(horizontal = 48.dp, vertical = 28.dp)

private suspend fun LazyListState.ensureVisible(index: Int) {
    if (index < 0) {
        scrollToItem(0)
        return
    }
    val info = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
    when {
        info == null -> animateScrollToItem(index)
        info.offset + info.size > layoutInfo.viewportEndOffset ->
            animateScrollBy((info.offset + info.size - layoutInfo.viewportEndOffset + 12).toFloat())
        info.offset < layoutInfo.viewportStartOffset ->
            animateScrollBy((info.offset - layoutInfo.viewportStartOffset - 12).toFloat())
    }
}

private suspend fun LazyGridState.ensureVisible(index: Int) {
    val info = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
    when {
        info == null -> animateScrollToItem(index)
        info.offset.y + info.size.height > layoutInfo.viewportEndOffset ->
            animateScrollBy((info.offset.y + info.size.height - layoutInfo.viewportEndOffset + 12).toFloat())
        info.offset.y < layoutInfo.viewportStartOffset ->
            animateScrollBy((info.offset.y - layoutInfo.viewportStartOffset - 12).toFloat())
    }
}

@Composable
private fun ScreenHeader(title: String, titleColor: Color) {
    Text(text = title, color = titleColor, fontSize = 30.sp, fontWeight = FontWeight.Light)
}

@Composable
private fun MonthArrow(symbol: String, focused: Boolean, isDarkMode: Boolean, titleColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .scale(if (focused) 1.12f else 1f)
            .clip(CircleShape)
            .background(if (focused) FocusCyan else if (isDarkMode) Color(0x22FFFFFF) else Color(0x15000000))
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) FocusCyan else if (isDarkMode) Color(0x44FFFFFF) else Color(0x22000000),
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (focused) Color.Black else titleColor)
    }
}

// -----------------------------------------------------------------------------
// DRAWER 2 (UP ▲): FULL MONTH CLEAN CALENDAR (MONDAY TO SUNDAY)
// -----------------------------------------------------------------------------

@Composable
fun MonthCalendarScreen(
    events: List<CalendarEvent>,
    isDarkMode: Boolean,
    focusedDay: Int,
    monthOffset: Int = 0,
    editMode: Boolean = false,
    onChangeMonth: (Int) -> Unit = {},
    onToggleEditMode: () -> Unit = {},
    onOpenCalendarChoice: (String) -> Unit = {}
) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDarkMode) Color(0x14FFFFFF) else Color(0xD9FFFFFF)
    val cardBorder = if (isDarkMode) Color(0x22FFFFFF) else Color(0x18000000)
    val amber = Color(0xFFFBBF24)

    val now = remember(monthOffset) {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, monthOffset)
        }
    }
    val monthTitle = remember(monthOffset) {
        SimpleDateFormat("MMMM yyyy", Locale("es", "ES")).format(now.time).replaceFirstChar { it.uppercase() }
    }
    val daysInMonth = remember(monthOffset) { now.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val todayNum = if (monthOffset == 0) Calendar.getInstance().get(Calendar.DAY_OF_MONTH) else -1
    val emptyLeadingDays = remember(monthOffset) { (now.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7 }
    val weekCount = (emptyLeadingDays + daysInMonth + 6) / 7
    val weekdayHeaders = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xDD0F172A) else Color(0xE3F8FAFC))
            .padding(ScreenPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MonthArrow("◀", focusedDay == 0, isDarkMode, titleColor) { onChangeMonth(-1) }
                Text(
                    text = monthTitle,
                    color = titleColor,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.widthIn(min = 260.dp),
                    textAlign = TextAlign.Center
                )
                MonthArrow("▶", focusedDay == -1, isDarkMode, titleColor) { onChangeMonth(1) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (editMode) Text(text = "Pulsa OK sobre un día para editar sus eventos", fontSize = 12.sp, color = amber)
                HeaderChip(
                    label = if (editMode) "✓ Listo" else "✎ Editar",
                    focused = focusedDay == -2,
                    isDarkMode = isDarkMode,
                    titleColor = titleColor,
                    cardBorder = cardBorder,
                    active = editMode,
                    onClick = onToggleEditMode
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            weekdayHeaders.forEach { name ->
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = subtextColor
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (week in 0 until weekCount) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (col in 0 until 7) {
                        val day = week * 7 + col - emptyLeadingDays + 1
                        if (day < 1 || day > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f))
                            continue
                        }
                        val dateStr = "%04d-%02d-%02d".format(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, day)
                        val dayEvents = events.filter { it.occursOn(dateStr) }
                        val isFocused = day == focusedDay
                        val isToday = day == todayNum
                        val overflow = dayEvents.size > 4
                        val shown = if (overflow) dayEvents.take(3) else dayEvents

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isFocused) Color(0x3338BDF8)
                                    else if (isToday) (if (isDarkMode) Color(0x2238BDF8) else Color(0x1838BDF8))
                                    else cardBg
                                )
                                .border(
                                    width = if (isFocused) 2.5.dp else if (isToday) 1.5.dp else 1.dp,
                                    color = if (isFocused) FocusCyan else if (isToday) FocusCyan.copy(alpha = 0.6f) else if (editMode && dayEvents.isNotEmpty()) amber else cardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onOpenCalendarChoice(dateStr) }
                                .padding(horizontal = 5.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$day",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isFocused || isToday) FocusCyan else titleColor,
                                modifier = Modifier.widthIn(min = 16.dp)
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                shown.forEach { EventChip(it, isDarkMode, compact = true) }
                                if (overflow) {
                                    Text(
                                        text = "+${dayEvents.size - shown.size} más",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = subtextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// DRAWER 3 (RIGHT ▶): FAMILY TO-DO & GROCERY LISTS (7-DAY FILTER)
// -----------------------------------------------------------------------------

private data class ListRowData(
    val text: String,
    val completed: Boolean,
    val trailing: String,
    val trailingColor: Color,
    val posterUrl: String? = null,
    val detail: String = ""
)

@Composable
private fun HeaderChip(
    label: String,
    focused: Boolean,
    isDarkMode: Boolean,
    titleColor: Color,
    cardBorder: Color,
    active: Boolean = false,
    onClick: () -> Unit
) {
    val amber = Color(0xFFFBBF24)
    Row(
        modifier = Modifier
            .scale(if (focused) 1.08f else 1f)
            .clip(RoundedCornerShape(50.dp))
            .background(
                if (focused) FocusCyan
                else if (active) Color(0x33FBBF24)
                else (if (isDarkMode) Color(0x33FFFFFF) else Color(0x18000000))
            )
            .border(
                width = if (focused || active) 2.dp else 1.dp,
                color = if (focused) FocusCyan else if (active) amber else cardBorder,
                shape = RoundedCornerShape(50.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (focused) Color.Black else titleColor
        )
    }
}

@Composable
private fun ListCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    rows: List<ListRowData>,
    columnFocused: Boolean,
    focusedIndex: Int,
    editMode: Boolean,
    isDarkMode: Boolean,
    onToggle: (Int) -> Unit,
    onEdit: (Int) -> Unit,
    onToggleEditMode: () -> Unit,
    onDictate: () -> Unit,
    onType: () -> Unit
) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDarkMode) Color(0x14FFFFFF) else Color(0xD9FFFFFF)
    val cardBorder = if (isDarkMode) Color(0x22FFFFFF) else Color(0x18000000)
    val amber = Color(0xFFFBBF24)
    val listState = rememberLazyListState()

    LaunchedEffect(focusedIndex, columnFocused, rows.size) {
        if (columnFocused) listState.ensureVisible(focusedIndex)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(
                width = if (columnFocused) 2.dp else 1.dp,
                color = if (columnFocused) FocusCyan.copy(alpha = 0.5f) else cardBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(18.dp)
    ) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
        Text(
            text = if (editMode) "Pulsa OK sobre un elemento para editarlo" else subtitle,
            fontSize = 12.sp,
            color = if (editMode) amber else subtextColor
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            HeaderChip(
                label = if (editMode) "✓ Listo" else "✎ Editar",
                focused = columnFocused && focusedIndex == -3,
                isDarkMode = isDarkMode,
                titleColor = titleColor,
                cardBorder = cardBorder,
                active = editMode,
                onClick = onToggleEditMode
            )
            HeaderChip("⌨ Añadir", columnFocused && focusedIndex == -2, isDarkMode, titleColor, cardBorder, onClick = onType)
            HeaderChip("🎤 Dictar", columnFocused && focusedIndex == -1, isDarkMode, titleColor, cardBorder, onClick = onDictate)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            itemsIndexed(rows) { index, row ->
                val isItemFocused = columnFocused && index == focusedIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(if (isItemFocused) 1.02f else 1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isItemFocused) Color(0x3338BDF8) else if (isDarkMode) Color(0x18FFFFFF) else Color(0xE8FFFFFF))
                        .border(
                            width = if (isItemFocused) 2.dp else if (editMode) 1.5.dp else 1.dp,
                            color = if (isItemFocused) FocusCyan else if (editMode) amber else cardBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { if (editMode) onEdit(index) else onToggle(index) }
                        .padding(if (row.posterUrl != null) 8.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (row.posterUrl != null) {
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 64.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isDarkMode) Color(0x33FFFFFF) else Color(0x14000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (row.posterUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = row.posterUrl,
                                    contentDescription = row.text,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(text = "🎞", fontSize = 20.sp)
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${if (row.completed) "☑" else "○"} ${row.text}",
                            fontSize = 15.sp,
                            color = if (row.completed) subtextColor else titleColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (row.detail.isNotEmpty()) {
                            Text(text = row.detail, fontSize = 12.sp, color = subtextColor, maxLines = 1)
                        }
                    }
                    if (editMode) {
                        Text(text = "✎", fontSize = 16.sp, color = amber)
                    } else if (row.trailing.isNotBlank()) {
                        Text(
                            text = row.trailing,
                            fontSize = 12.sp,
                            color = row.trailingColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FamilyListsScreen(
    todos: List<FamilyTask>,
    groceries: List<GroceryItem>,
    isDarkMode: Boolean,
    focusedColumn: Int,
    focusedTaskIndex: Int,
    focusedGroceryIndex: Int,
    onToggleTask: (Int) -> Unit,
    onToggleGrocery: (Int) -> Unit,
    onStartVoiceTask: () -> Unit = {},
    onStartVoiceGrocery: () -> Unit = {},
    onStartTextTask: () -> Unit = {},
    onStartTextGrocery: () -> Unit = {},
    tasksEditMode: Boolean = false,
    groceriesEditMode: Boolean = false,
    onToggleTasksEdit: () -> Unit = {},
    onToggleGroceriesEdit: () -> Unit = {},
    onEditTask: (Int) -> Unit = {},
    onEditGrocery: (Int) -> Unit = {}
) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val groceryColor = if (isDarkMode) Color(0xFF34D399) else Color(0xFF059669)

    val visibleTodos = todos.visibleTasks()
    val visibleGroceries = groceries.visibleGroceries()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xDD0F172A) else Color(0xE3F8FAFC))
            .padding(ScreenPadding)
    ) {
        ScreenHeader("Tareas & Compra", titleColor)

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ListCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                title = "Tareas Familiares",
                subtitle = "${visibleTodos.count { !it.completed }} pendientes",
                rows = visibleTodos.map { ListRowData(it.text, it.completed, it.assignee, subtextColor) },
                columnFocused = focusedColumn == 0,
                focusedIndex = focusedTaskIndex,
                editMode = tasksEditMode,
                isDarkMode = isDarkMode,
                onToggle = onToggleTask,
                onEdit = onEditTask,
                onToggleEditMode = onToggleTasksEdit,
                onDictate = onStartVoiceTask,
                onType = onStartTextTask
            )
            ListCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                title = "Lista de la Compra",
                subtitle = "${visibleGroceries.count { !it.completed }} artículos",
                rows = visibleGroceries.map { ListRowData(it.text, it.completed, it.category, groceryColor) },
                columnFocused = focusedColumn == 1,
                focusedIndex = focusedGroceryIndex,
                editMode = groceriesEditMode,
                isDarkMode = isDarkMode,
                onToggle = onToggleGrocery,
                onEdit = onEditGrocery,
                onToggleEditMode = onToggleGroceriesEdit,
                onDictate = onStartVoiceGrocery,
                onType = onStartTextGrocery
            )
        }
    }
}

// -----------------------------------------------------------------------------
// DRAWER 4 (DOWN ▼): ALL APPLICATIONS DRAWER (BUILT-IN ICONS)
// -----------------------------------------------------------------------------

@Composable
fun AllAppsDrawerScreen(
    apps: List<InstalledApp>,
    isDarkMode: Boolean,
    focusedAppIndex: Int,
    favoritePackages: List<String>,
    editMode: Boolean,
    message: String?,
    onToggleEditMode: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onLaunch: (String) -> Unit
) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDarkMode) Color(0x14FFFFFF) else Color(0xD9FFFFFF)
    val cardBorder = if (isDarkMode) Color(0x22FFFFFF) else Color(0x18000000)
    val starColor = Color(0xFFFBBF24)
    val gridState = rememberLazyGridState()

    LaunchedEffect(focusedAppIndex) {
        if (focusedAppIndex in apps.indices) gridState.ensureVisible(focusedAppIndex)
        else if (focusedAppIndex < 0) gridState.scrollToItem(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xDD0F172A) else Color(0xE3F8FAFC))
            .padding(ScreenPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Aplicaciones", color = titleColor, fontSize = 30.sp, fontWeight = FontWeight.Light)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                HeaderChip("⚙ Ajustes", focusedAppIndex == -2, isDarkMode, titleColor, cardBorder, onClick = onOpenSettings)
                HeaderChip(
                    label = if (editMode) "✓ Listo" else "✎ Editar",
                    focused = focusedAppIndex == -1,
                    isDarkMode = isDarkMode,
                    titleColor = titleColor,
                    cardBorder = cardBorder,
                    active = editMode,
                    onClick = onToggleEditMode
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message
                ?: if (editMode) "Pulsa OK sobre una app para añadirla o quitarla del inicio (${favoritePackages.size}/5)" else " ",
            color = if (message != null) starColor else subtextColor,
            fontSize = 13.sp,
            fontWeight = if (message != null) FontWeight.Bold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(4.dp))

        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)
        ) {
            itemsIndexed(apps) { index, app ->
                val isFocused = index == focusedAppIndex
                val isFavorite = app.packageName in favoritePackages
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(if (isFocused) 1.04f else 1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isFocused) Color(0x3338BDF8) else cardBg)
                        .border(
                            width = if (isFocused) 2.5.dp else if (editMode && isFavorite) 1.5.dp else 1.dp,
                            color = if (isFocused) FocusCyan else if (editMode && isFavorite) starColor else cardBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { if (editMode) onToggleFavorite(app.packageName) else onLaunch(app.packageName) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (app.iconBitmap != null) Color.Transparent else Color(app.color)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (app.iconBitmap != null) {
                            Image(
                                bitmap = app.iconBitmap.asImageBitmap(),
                                contentDescription = app.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(text = app.iconEmoji, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = app.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = titleColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = if (editMode) (if (isFavorite) "Quitar de favoritas" else "Añadir a favoritas") else app.packageName,
                            fontSize = 10.sp,
                            color = if (editMode) (if (isFavorite) starColor else subtextColor) else subtextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isFavorite) {
                        Text(text = "★", color = starColor, fontSize = 18.sp)
                    }
                }
            }
        }
    }
}


@Composable
fun WatchScreen(watch: WatchState, actions: WatchActions, isDarkMode: Boolean) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val whoColor = if (isDarkMode) Color(0xFFA5B4FC) else Color(0xFF4F46E5)

    fun rows(items: List<WatchItem>) = items.visibleWatch().map { item ->
        val detail = listOfNotNull(
            item.year?.toString(),
            item.rating?.let { "★ %.1f".format(Locale.US, it) }
        ).joinToString("  ·  ")
        ListRowData(item.title, item.watched, item.who, whoColor, item.posterUrl, detail)
    }

    val movies = watch.movies.visibleWatch()
    val shows = watch.shows.visibleWatch()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xDD0F172A) else Color(0xE3F8FAFC))
            .padding(ScreenPadding)
    ) {
        ScreenHeader("Películas & Series", titleColor)
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ListCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                title = "🎬 Películas",
                subtitle = "${movies.count { !it.watched }} por ver",
                rows = rows(watch.movies),
                columnFocused = watch.focusedColumn == 0,
                focusedIndex = watch.movieIndex,
                editMode = watch.moviesEdit,
                isDarkMode = isDarkMode,
                onToggle = actions.onToggleMovie,
                onEdit = actions.onEditMovie,
                onToggleEditMode = actions.onToggleMoviesEdit,
                onDictate = actions.onDictateMovie,
                onType = actions.onTypeMovie
            )
            ListCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                title = "📺 Series",
                subtitle = "${shows.count { !it.watched }} por ver",
                rows = rows(watch.shows),
                columnFocused = watch.focusedColumn == 1,
                focusedIndex = watch.showIndex,
                editMode = watch.showsEdit,
                isDarkMode = isDarkMode,
                onToggle = actions.onToggleShow,
                onEdit = actions.onEditShow,
                onToggleEditMode = actions.onToggleShowsEdit,
                onDictate = actions.onDictateShow,
                onType = actions.onTypeShow
            )
        }
    }
}

@Composable
fun SearchOverlay(
    ui: SearchUi,
    selectedIndex: Int,
    isDarkMode: Boolean,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val results = ui.results
    val cells = (results?.size ?: 0) + 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(if (isDarkMode) Color(0xFF1E293B) else Color.White)
                .border(1.5.dp, FocusCyan, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "${if (ui.isShow) "Serie" else "Película"}: «${ui.query}»",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            when {
                results == null -> Text(text = "Buscando…", fontSize = 15.sp, color = subtextColor)
                ui.failed -> Text(text = "No se pudo buscar. Puedes añadir solo el título.", fontSize = 15.sp, color = subtextColor)
                results.isEmpty() -> Text(text = "Sin resultados. Puedes añadir solo el título.", fontSize = 15.sp, color = subtextColor)
                else -> Text(text = "Elige el correcto con OK", fontSize = 13.sp, color = subtextColor)
            }
            if (results != null) {
                (0 until cells).chunked(5).forEach { rowCells ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        rowCells.forEach { i ->
                            val focused = i == selectedIndex
                            val result = results.getOrNull(i)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(if (focused) 1.05f else 1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (focused) Color(0x3338BDF8) else if (isDarkMode) Color(0x18FFFFFF) else Color(0x0F000000))
                                    .border(
                                        width = if (focused) 2.5.dp else 1.dp,
                                        color = if (focused) FocusCyan else if (isDarkMode) Color(0x33FFFFFF) else Color(0x22000000),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onPick(i) }
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 80.dp, height = 118.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDarkMode) Color(0x33FFFFFF) else Color(0x14000000)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (result != null && result.posterUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = result.posterUrl,
                                            contentDescription = result.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(text = if (result == null) "＋" else "🎞", fontSize = 26.sp, color = titleColor)
                                    }
                                }
                                Text(
                                    text = result?.title ?: "Solo el título",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = titleColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                                Text(
                                    text = if (result == null) "sin carátula" else listOfNotNull(
                                        result.year?.toString(),
                                        result.rating?.let { "★ %.1f".format(Locale.US, it) }
                                    ).joinToString("  "),
                                    fontSize = 11.sp,
                                    color = subtextColor
                                )
                            }
                        }
                        repeat(5 - rowCells.size) { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}
