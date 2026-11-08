package com.familyboard.tv.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
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
    onSettingsIndexChange: (Int) -> Unit,
    appsGridFocusedIndex: Int,
    onAppsGridIndexChange: (Int) -> Unit,
    monthDayFocusedIndex: Int,
    onMonthDayIndexChange: (Int) -> Unit,
    listFocusedColumn: Int,
    onListColumnChange: (Int) -> Unit,
    taskFocusedIndex: Int,
    onTaskIndexChange: (Int) -> Unit,
    groceryFocusedIndex: Int,
    onGroceryIndexChange: (Int) -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    currentWallpaper: String,
    onSelectWallpaper: (String) -> Unit,
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
    onSubmitEntry: (String, String) -> Unit = { _, _ -> },
    onCancelEntry: () -> Unit = {}
) {
    // Intercept Back button: return to clean Wallpaper Center
    BackHandler(enabled = activeSection != ActiveSection.CENTER) {
        onActiveSectionChange(ActiveSection.CENTER)
    }

    val primaryTextColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val secondaryTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)
    val defaultArrowColor = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF0F172A).copy(alpha = 0.75f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xFF070B12) else Color(0xFFF1F5F9))
    ) {
        // 1. Fullscreen Wallpaper Backdrop
        AsyncImage(
            model = currentWallpaper,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Ambient overlay gradient for high readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDarkMode) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x66000000),
                                Color(0x22000000),
                                Color(0x77000000)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x44FFFFFF),
                                Color(0x11FFFFFF),
                                Color(0x55FFFFFF)
                            )
                        )
                    }
                )
        )

        // 2. CENTER LANDING SCREEN WITH 4 REFINED SNEAK EDGES
        if (activeSection == ActiveSection.CENTER) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Edge Sneak: Weather + Rolling 7-Day Bubbles (Today + 6 Days) + Arrow ▲
                RefinedTopCalendarSneak(
                    weather = weather,
                    events = events,
                    isDarkMode = isDarkMode,
                    isArrowFocused = centerFocus == CenterFocusArea.ARROW_UP,
                    defaultArrowColor = defaultArrowColor,
                    onOpenMonth = {
                        onActiveSectionChange(ActiveSection.MONTH)
                    }
                )

                // Clean Center Area: Spacious Wallpaper with Ambient Clock
                AmbientClockHero(
                    textColor = primaryTextColor,
                    dateColor = secondaryTextColor
                )

                // Bottom Edge Sneak: Centered ▼ Arrow above + 5 Floating App Circles (with real built-in icons)
                RefinedFavoritesDock(
                    apps = dockApps,
                    isDockFocused = centerFocus == CenterFocusArea.DOCK,
                    focusedIndex = dockFocusedIndex,
                    isArrowFocused = centerFocus == CenterFocusArea.ARROW_DOWN,
                    defaultArrowColor = defaultArrowColor,
                    primaryTextColor = primaryTextColor,
                    onSelectIndex = {
                        onCenterFocusChange(CenterFocusArea.DOCK)
                        onDockIndexChange(it)
                    },
                    onLaunch = onLaunchApp,
                    onOpenAllApps = {
                        onActiveSectionChange(ActiveSection.APPS)
                    }
                )
            }

            // Left Edge Sneak: Settings & Wallpaper Menu (◀)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                RefinedLeftSettingsSneak(
                    isDarkMode = isDarkMode,
                    isArrowFocused = centerFocus == CenterFocusArea.ARROW_LEFT,
                    defaultArrowColor = defaultArrowColor,
                    onOpenSettings = {
                        onActiveSectionChange(ActiveSection.SETTINGS)
                    }
                )
            }

            // Right Edge Sneak: Family Tasks & Grocery Lists (▶)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 28.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                RefinedRightListsSneak(
                    todoCount = todos.count { !it.completed },
                    groceryCount = groceries.count { !it.completed },
                    isDarkMode = isDarkMode,
                    isArrowFocused = centerFocus == CenterFocusArea.ARROW_RIGHT,
                    defaultArrowColor = defaultArrowColor,
                    onOpenLists = {
                        onActiveSectionChange(ActiveSection.LISTS)
                    }
                )
            }
        }

        // 3. LOCKED SECTION: LEFT (◀) - Settings, Theme & Wallpaper Menu
        AnimatedVisibility(
            visible = activeSection == ActiveSection.SETTINGS,
            enter = slideInHorizontally(DrawerSlideIn) { -it / 4 } + fadeIn(DrawerFadeIn),
            exit = slideOutHorizontally(DrawerSlideOut) { -it / 4 } + fadeOut(DrawerFadeOut)
        ) {
            SettingsWallpaperScreen(
                isDarkMode = isDarkMode,
                focusedIndex = settingsFocusedIndex,
                currentWallpaper = currentWallpaper,
                onToggleDarkMode = onToggleDarkMode,
                onSelectWallpaper = onSelectWallpaper,
                onClose = { onActiveSectionChange(ActiveSection.CENTER) }
            )
        }

        // 4. LOCKED SECTION: UP (▲) - Full Month Clean Calendar (Monday to Sunday)
        AnimatedVisibility(
            visible = activeSection == ActiveSection.MONTH,
            enter = slideInVertically(DrawerSlideIn) { -it / 4 } + fadeIn(DrawerFadeIn),
            exit = slideOutVertically(DrawerSlideOut) { -it / 4 } + fadeOut(DrawerFadeOut)
        ) {
            MonthCalendarScreen(
                events = events,
                isDarkMode = isDarkMode,
                focusedDay = monthDayFocusedIndex,
                onOpenCalendarChoice = onOpenCalendarChoice,
                onClose = { onActiveSectionChange(ActiveSection.CENTER) }
            )
        }

        // 5. LOCKED SECTION: RIGHT (▶) - Family Tasks & Grocery Dual Board
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
                onEditGrocery = onEditGrocery,
                onClose = { onActiveSectionChange(ActiveSection.CENTER) }
            )
        }

        // 6. LOCKED SECTION: DOWN (▼) - All Applications Drawer (with real built-in icons)
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
                onLaunch = onLaunchApp,
                onClose = { onActiveSectionChange(ActiveSection.CENTER) }
            )
        }

        // 7. VOICE LISTENING AMBIENT DIALOG OVERLAY
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
                            is VoiceInputMode.AddCalendarEvent -> "Dicta el evento para ${(voiceInputMode as VoiceInputMode.AddCalendarEvent).dateStr}"
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
                },
                isDarkMode = isDarkMode,
                onSubmit = onSubmitEntry,
                onCancel = onCancelEntry
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
            Text(text = "Nuevo evento", fontSize = 14.sp, color = subtextColor)
            Text(text = formatDateEs(dateStr), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OverlayButton("🎤 Dictar", selectedIndex == 0, isDarkMode, { onChoose(0) })
                OverlayButton("⌨ Escribir", selectedIndex == 1, isDarkMode, { onChoose(1) })
            }
            if (events.isNotEmpty()) {
                Text(text = "Editar eventos de este día", fontSize = 13.sp, color = subtextColor)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    events.forEachIndexed { i, ev ->
                        OverlayButton(
                            label = "✎ ${ev.title}" + if (ev.type.isNotBlank()) "  ·  ${ev.type}" else "",
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

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun EntryFormOverlay(
    form: EntryForm,
    options: List<String>,
    isDarkMode: Boolean,
    onSubmit: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    var text by remember { mutableStateOf(form.initialText) }
    var selected by remember { mutableStateOf(form.initialOption) }
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var fieldFocused by remember { mutableStateOf(false) }
    var addFocused by remember { mutableStateOf(false) }
    var cancelFocused by remember { mutableStateOf(false) }
    val chipRequesters = remember(options) { (listOf("") + options).associateWith { androidx.compose.ui.focus.FocusRequester() } }
    val addRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    val isEdit = form.editId != null
    val optionLabel = if (form.kind == EntryKind.TASK) "Quién" else "Tipo"
    val heading = when (form.kind) {
        EntryKind.TASK -> if (isEdit) "Editar tarea" else "Nueva tarea familiar"
        EntryKind.GROCERY -> if (isEdit) "Editar artículo" else "Nuevo artículo de la compra"
        EntryKind.EVENT -> (if (isEdit) "Editar evento: " else "Nuevo evento: ") + formatDateEs(form.dateStr ?: "")
    }

    fun submit() {
        val value = text.trim()
        if (value.isNotEmpty()) onSubmit(value, selected) else onCancel()
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
                    (chipRequesters[selected] ?: chipRequesters[""] ?: addRequester).requestFocus()
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusProperties { down = chipRequesters[selected] ?: chipRequesters[""] ?: addRequester }
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
                    (listOf("") + options).forEach { option ->
                        var chipFocused by remember { mutableStateOf(false) }
                        val isSelected = option == selected
                        Box(
                            modifier = Modifier
                                .focusRequester(chipRequesters.getValue(option))
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
                                .clickable { selected = option }
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

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OverlayButton(
                    label = if (isEdit) "✓ Guardar" else "✓ Añadir",
                    focused = addFocused,
                    isDarkMode = isDarkMode,
                    onClick = { submit() },
                    modifier = Modifier
                        .focusRequester(addRequester)
                        .onFocusChanged { addFocused = it.isFocused }
                )
                OverlayButton(
                    label = "✕ Cancelar",
                    focused = cancelFocused,
                    isDarkMode = isDarkMode,
                    onClick = onCancel,
                    modifier = Modifier.onFocusChanged { cancelFocused = it.isFocused }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// REFINED LEFT SNEAK: 2 BUBBLES (🎨 & 🌓) + ARROW ◀
// -----------------------------------------------------------------------------

@Composable
fun RefinedLeftSettingsSneak(
    isDarkMode: Boolean,
    isArrowFocused: Boolean,
    defaultArrowColor: Color,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onOpenSettings() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Theme / Settings Bubble
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isDarkMode) Color(0x66475569) else Color(0xCCFFFFFF))
                    .border(
                        width = 1.dp,
                        color = if (isDarkMode) Color(0x44FFFFFF) else Color(0x18000000),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = if (isDarkMode) "🌙" else "☀️", fontSize = 14.sp)
                Text(
                    text = if (isDarkMode) "Oscuro" else "Claro",
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Wallpaper Bubble
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isDarkMode) Color(0x667C3AED) else Color(0xCCFFFFFF))
                    .border(
                        width = 1.dp,
                        color = if (isDarkMode) Color(0x44A78BFA) else Color(0x18000000),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "🎨", fontSize = 14.sp)
                Text(
                    text = "Fondo",
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Arrow Left ◀ (Highlighted when focused)
        Box(
            modifier = Modifier
                .scale(if (isArrowFocused) 1.35f else 1f)
                .clip(CircleShape)
                .background(if (isArrowFocused) Color(0x3338BDF8) else Color.Transparent)
                .border(
                    width = if (isArrowFocused) 2.dp else 0.dp,
                    color = if (isArrowFocused) FocusCyan else Color.Transparent,
                    shape = CircleShape
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "◀",
                color = if (isArrowFocused) FocusCyan else defaultArrowColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

// -----------------------------------------------------------------------------
// REFINED TOP SNEAK: BARE WEATHER + ROLLING TODAY + 6 DAYS + CENTERED ▲ ARROW
// -----------------------------------------------------------------------------

data class DayPillInfo(
    val dayLabel: String,
    val dayNum: Int,
    val dateStr: String,
    val isToday: Boolean,
    val events: List<CalendarEvent>
)

@Composable
fun RefinedTopCalendarSneak(
    weather: WeatherInfo,
    events: List<CalendarEvent>,
    isDarkMode: Boolean,
    isArrowFocused: Boolean,
    defaultArrowColor: Color,
    onOpenMonth: () -> Unit
) {
    var dayStamp by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            dayStamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        }
    }
    val eventsSnapshot = events.toList()

    val dayPills = remember(dayStamp, eventsSnapshot) {
        val todayCal = Calendar.getInstance()
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfDayName = SimpleDateFormat("EEE", Locale("es", "ES"))

        (0..6).map { offset ->
            val dayCal = (todayCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
            }
            val dateStr = sdfDate.format(dayCal.time)
            val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)
            val dayName = if (offset == 0) "HOY" else sdfDayName.format(dayCal.time).uppercase().take(3)

            // Match real events from Notion for this date or dayOfMonth
            val matchingEvents = events.filter { it.occursOn(dateStr) }

            DayPillInfo(
                dayLabel = dayName,
                dayNum = dayNum,
                dateStr = dateStr,
                isToday = offset == 0,
                events = matchingEvents
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(0.96f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Bare Weather
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Text(
                    text = weather.iconEmoji,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${weather.tempC}°C",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                    fontSize = 14.sp
                )
            }

            // 7 Rolling Day Bubbles (Today + 6 Days)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dayPills.forEach { pill ->
                    val hasBirthday = pill.events.any { it.type.contains("Cumple", ignoreCase = true) }
                    val hasEvent = pill.events.isNotEmpty()

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (hasBirthday) (if (isDarkMode) Color(0x66F43F5E) else Color(0xF2FFE4E6))
                                else if (pill.isToday) (if (isDarkMode) Color(0x660284C7) else Color(0xF2E0F2FE))
                                else if (isDarkMode) Color(0x990F172A)
                                else Color(0xF2FFFFFF)
                            )
                            .border(
                                width = if (hasBirthday || pill.isToday) 1.5.dp else 1.dp,
                                color = if (hasBirthday) Color(0xFFF43F5E)
                                else if (pill.isToday) FocusCyan
                                else if (isDarkMode) Color(0x33FFFFFF)
                                else Color(0x18000000),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pill.dayLabel,
                                fontWeight = FontWeight.Bold,
                                color = if (hasBirthday) (if (isDarkMode) Color(0xFFFDA4AF) else Color(0xFFBE123C))
                                else if (pill.isToday) (if (isDarkMode) FocusCyan else Color(0xFF0369A1))
                                else if (isDarkMode) Color(0xFF94A3B8)
                                else Color(0xFF64748B),
                                fontSize = 10.sp
                            )
                            Text(
                                text = "${pill.dayNum}",
                                fontWeight = FontWeight.Bold,
                                color = if (pill.isToday) (if (isDarkMode) FocusCyan else Color(0xFF0369A1))
                                else if (isDarkMode) Color.White
                                else Color(0xFF0F172A),
                                fontSize = 12.sp
                            )
                        }

                        // Real Event Only — No hardcoded placeholders!
                        if (hasEvent) {
                            val ev = pill.events.first()
                            val prefix = if (ev.type.contains("Cumple", ignoreCase = true)) "🎂 " else "📌 "
                            Text(
                                text = "$prefix${ev.title}",
                                color = if (hasBirthday) (if (isDarkMode) Color(0xFFFFE4E6) else Color(0xFFE11D48))
                                else if (isDarkMode) Color(0xFFE0F2FE) else Color(0xFF0369A1),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(text = "—", color = Color(0x33888888), fontSize = 9.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Centered Arrow ▲ below row (Highlighted when focused)
        Box(
            modifier = Modifier
                .scale(if (isArrowFocused) 1.35f else 1f)
                .clip(CircleShape)
                .background(if (isArrowFocused) Color(0x3338BDF8) else Color.Transparent)
                .border(
                    width = if (isArrowFocused) 2.dp else 0.dp,
                    color = if (isArrowFocused) FocusCyan else Color.Transparent,
                    shape = CircleShape
                )
                .clickable { onOpenMonth() }
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▲",
                color = if (isArrowFocused) FocusCyan else defaultArrowColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

// -----------------------------------------------------------------------------
// REFINED BOTTOM DOCK: CENTERED ▼ ARROW ABOVE + 5 FLOATING APP CIRCLES (BUILT-IN ICONS)
// -----------------------------------------------------------------------------

@Composable
fun RefinedFavoritesDock(
    apps: List<InstalledApp>,
    isDockFocused: Boolean,
    focusedIndex: Int,
    isArrowFocused: Boolean,
    defaultArrowColor: Color,
    primaryTextColor: Color,
    onSelectIndex: (Int) -> Unit,
    onLaunch: (String) -> Unit,
    onOpenAllApps: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Centered Arrow ▼ above the apps (Highlighted when focused)
        Box(
            modifier = Modifier
                .scale(if (isArrowFocused) 1.35f else 1f)
                .clip(CircleShape)
                .background(if (isArrowFocused) Color(0x3338BDF8) else Color.Transparent)
                .border(
                    width = if (isArrowFocused) 2.dp else 0.dp,
                    color = if (isArrowFocused) FocusCyan else Color.Transparent,
                    shape = CircleShape
                )
                .clickable { onOpenAllApps() }
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▼",
                color = if (isArrowFocused) FocusCyan else defaultArrowColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        // 5 Floating Circular App Icons with remote focus state & built-in icons
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
                            Text(
                                text = app.iconEmoji,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
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
}

// -----------------------------------------------------------------------------
// REFINED RIGHT SNEAK: ARROW ▶ LEFT + 2 FLOATING BUBBLES
// -----------------------------------------------------------------------------

@Composable
fun RefinedRightListsSneak(
    todoCount: Int,
    groceryCount: Int,
    isDarkMode: Boolean,
    isArrowFocused: Boolean,
    defaultArrowColor: Color,
    onOpenLists: () -> Unit
) {
    Row(
        modifier = Modifier.clickable { onOpenLists() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Arrow Right ▶ (Highlighted when focused)
        Box(
            modifier = Modifier
                .scale(if (isArrowFocused) 1.35f else 1f)
                .clip(CircleShape)
                .background(if (isArrowFocused) Color(0x3338BDF8) else Color.Transparent)
                .border(
                    width = if (isArrowFocused) 2.dp else 0.dp,
                    color = if (isArrowFocused) FocusCyan else Color.Transparent,
                    shape = CircleShape
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▶",
                color = if (isArrowFocused) FocusCyan else defaultArrowColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        // 2 Floating Glass Bubbles
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Tasks Bubble
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isDarkMode) Color(0x550284C7) else Color(0xDD0284C7))
                    .border(
                        width = 1.dp,
                        color = Color(0x4438BDF8),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "📋", fontSize = 14.sp)
                Text(text = "$todoCount", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // Grocery Bubble
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isDarkMode) Color(0x55059669) else Color(0xDD059669))
                    .border(
                        width = 1.dp,
                        color = Color(0x4434D399),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "🛒", fontSize = 14.sp)
                Text(text = "$groceryCount", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// AMBIENT CLOCK HERO
// -----------------------------------------------------------------------------

@Composable
fun AmbientClockHero(textColor: Color, dateColor: Color) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES")) }
    val clockShadow = androidx.compose.ui.graphics.Shadow(if (textColor == Color.White) Color(0xAA000000) else Color(0xAAFFFFFF), blurRadius = 14f)
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
            color = dateColor,
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
    onToggleDarkMode: () -> Unit,
    onSelectWallpaper: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDarkMode) Color(0x14FFFFFF) else Color(0xEEFFFFFF)
    val cardBorder = if (isDarkMode) Color(0x22FFFFFF) else Color(0x18000000)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xDD0F172A) else Color(0xF0F8FAFC))
            .padding(ScreenPadding)
    ) {
        // Simplified Minimal Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ajustes & Personalización",
                color = titleColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Light
            )

            // Volver button (Focus index 3)
            val isVolverFocused = focusedIndex == 3
            Box(
                modifier = Modifier
                    .scale(if (isVolverFocused) 1.08f else 1f)
                    .clip(RoundedCornerShape(50.dp))
                    .background(if (isVolverFocused) Color(0x3338BDF8) else if (isDarkMode) Color(0x22FFFFFF) else Color(0x15000000))
                    .border(
                        width = if (isVolverFocused) 2.dp else 1.dp,
                        color = if (isVolverFocused) FocusCyan else if (isDarkMode) Color(0x44FFFFFF) else Color(0x22000000),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .clickable { onClose() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "✕ Volver",
                    color = if (isVolverFocused) FocusCyan else titleColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }

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

            val wallpapers = WallpaperCatalog.items
            val wallpaperListState = rememberLazyListState()
            LaunchedEffect(focusedIndex) {
                if (focusedIndex >= 10) wallpaperListState.ensureVisible(focusedIndex - 10)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(1.5.dp, cardBorder, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Text(text = "🖼️ Fondos de Pantalla", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = titleColor)
                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    state = wallpaperListState,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(wallpapers) { wIdx, wall ->
                        val isWallFocused = focusedIndex == 10 + wIdx
                        val isWallSelected = currentWallpaper == wall.assetPath

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(if (isWallFocused) 1.02f else 1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isWallFocused) Color(0x3338BDF8) else if (isWallSelected) Color(0x180284C7) else Color(0x0A000000))
                                .border(
                                    width = if (isWallFocused) 2.5.dp else if (isWallSelected) 1.5.dp else 1.dp,
                                    color = if (isWallFocused) FocusCyan else if (isWallSelected) Color(0xFF0284C7) else cardBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectWallpaper(wall.assetPath) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (isWallSelected) "✓" else "○",
                                color = if (isWallSelected) FocusCyan else subtextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = wall.name, fontWeight = FontWeight.Bold, color = titleColor, fontSize = 15.sp)
                                if (wall.subtitle.isNotEmpty()) {
                                    Text(text = wall.subtitle, fontSize = 11.sp, color = subtextColor)
                                }
                            }
                            if (isWallSelected) {
                                Text(text = "Activo", color = FocusCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
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
private fun ScreenHeader(title: String, titleColor: Color, isDarkMode: Boolean, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = titleColor, fontSize = 30.sp, fontWeight = FontWeight.Light)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(if (isDarkMode) Color(0x22FFFFFF) else Color(0x15000000))
                .border(1.dp, if (isDarkMode) Color(0x44FFFFFF) else Color(0x22000000), RoundedCornerShape(50.dp))
                .clickable { onClose() }
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(text = "✕ Volver", color = titleColor, fontWeight = FontWeight.Medium)
        }
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
    onOpenCalendarChoice: (String) -> Unit = {},
    onClose: () -> Unit
) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDarkMode) Color(0x14FFFFFF) else Color(0xEEFFFFFF)
    val cardBorder = if (isDarkMode) Color(0x22FFFFFF) else Color(0x18000000)

    val now = remember { Calendar.getInstance() }
    val monthTitle = remember {
        SimpleDateFormat("MMMM yyyy", Locale("es", "ES")).format(now.time).replaceFirstChar { it.uppercase() }
    }
    val daysInMonth = remember { now.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val todayNum = remember { now.get(Calendar.DAY_OF_MONTH) }
    val emptyLeadingDays = remember {
        val firstDayCal = (now.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        (firstDayCal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
    }
    val weekCount = (emptyLeadingDays + daysInMonth + 6) / 7
    val weekdayHeaders = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xDD0F172A) else Color(0xF0F8FAFC))
            .padding(ScreenPadding)
    ) {
        ScreenHeader(monthTitle, titleColor, isDarkMode, onClose)

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

                        Box(
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
                                    color = if (isFocused) FocusCyan else if (isToday) FocusCyan.copy(alpha = 0.6f) else cardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onOpenCalendarChoice(dateStr) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Column {
                                Text(
                                    text = "$day",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isFocused || isToday) FocusCyan else titleColor
                                )
                                dayEvents.take(2).forEach { ev ->
                                    val isBirthday = ev.type.contains("Cumple", ignoreCase = true)
                                    Text(
                                        text = "${if (isBirthday) "🎂" else "📌"} ${ev.title}",
                                        fontSize = 10.sp,
                                        color = if (isBirthday) (if (isDarkMode) Color(0xFFFDA4AF) else Color(0xFFE11D48))
                                        else if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (dayEvents.size > 2) {
                                    Text(text = "+${dayEvents.size - 2}", fontSize = 10.sp, color = subtextColor)
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

private data class ListRowData(val text: String, val completed: Boolean, val trailing: String, val trailingColor: Color)

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
    val cardBg = if (isDarkMode) Color(0x14FFFFFF) else Color(0xEEFFFFFF)
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
                        .background(if (isItemFocused) Color(0x3338BDF8) else if (isDarkMode) Color(0x18FFFFFF) else Color(0xFAFFFFFF))
                        .border(
                            width = if (isItemFocused) 2.dp else if (editMode) 1.5.dp else 1.dp,
                            color = if (isItemFocused) FocusCyan else if (editMode) amber else cardBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { if (editMode) onEdit(index) else onToggle(index) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${if (row.completed) "☑" else "○"} ${row.text}",
                        fontSize = 15.sp,
                        color = if (row.completed) subtextColor else titleColor,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (editMode) {
                        Text(text = "✎", fontSize = 16.sp, color = amber, modifier = Modifier.padding(start = 8.dp))
                    } else if (row.trailing.isNotBlank()) {
                        Text(
                            text = row.trailing,
                            fontSize = 12.sp,
                            color = row.trailingColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp)
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
    onEditGrocery: (Int) -> Unit = {},
    onClose: () -> Unit
) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val groceryColor = if (isDarkMode) Color(0xFF34D399) else Color(0xFF059669)

    val visibleTodos = todos.visibleTasks()
    val visibleGroceries = groceries.visibleGroceries()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkMode) Color(0xDD0F172A) else Color(0xF0F8FAFC))
            .padding(ScreenPadding)
    ) {
        ScreenHeader("Tareas & Compra", titleColor, isDarkMode, onClose)

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
    onLaunch: (String) -> Unit,
    onClose: () -> Unit
) {
    val titleColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardBg = if (isDarkMode) Color(0x14FFFFFF) else Color(0xEEFFFFFF)
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
            .background(if (isDarkMode) Color(0xDD0F172A) else Color(0xF0F8FAFC))
            .padding(ScreenPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Aplicaciones", color = titleColor, fontSize = 30.sp, fontWeight = FontWeight.Light)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                val editFocused = focusedAppIndex == -1
                Row(
                    modifier = Modifier
                        .scale(if (editFocused) 1.06f else 1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (editFocused) FocusCyan
                            else if (editMode) Color(0x33FBBF24)
                            else if (isDarkMode) Color(0x22FFFFFF) else Color(0x15000000)
                        )
                        .border(
                            width = if (editFocused || editMode) 2.dp else 1.dp,
                            color = if (editFocused) FocusCyan else if (editMode) starColor else if (isDarkMode) Color(0x44FFFFFF) else Color(0x22000000),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .clickable { onToggleEditMode() }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editMode) "✓ Listo  ${favoritePackages.size}/5" else "★ Favoritas  ${favoritePackages.size}/5",
                        fontWeight = FontWeight.Medium,
                        color = if (editFocused) Color.Black else titleColor
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (isDarkMode) Color(0x22FFFFFF) else Color(0x15000000))
                        .border(1.dp, if (isDarkMode) Color(0x44FFFFFF) else Color(0x22000000), RoundedCornerShape(50.dp))
                        .clickable { onClose() }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(text = "✕ Volver", color = titleColor, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message
                ?: if (editMode) "Pulsa OK sobre una app para añadirla o quitarla del inicio (máximo 5)" else " ",
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
