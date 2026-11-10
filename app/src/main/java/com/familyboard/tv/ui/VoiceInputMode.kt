package com.familyboard.tv.ui

sealed class VoiceInputMode {
    object None : VoiceInputMode()
    object AddTask : VoiceInputMode()
    object AddGrocery : VoiceInputMode()
    object AddMovie : VoiceInputMode()
    object AddShow : VoiceInputMode()
    object SearchWallpaper : VoiceInputMode()
    data class AddCalendarEvent(val dateStr: String) : VoiceInputMode()
}
