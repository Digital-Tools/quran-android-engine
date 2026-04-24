package com.quranengine.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.domain.quranaudiokit.AudioPreferences
import com.quranengine.domain.qurantextkit.FontSizePreferences
import com.quranengine.domain.readingservice.ReadingPreferences
import com.quranengine.model.quranaudio.AudioEnd
import com.quranengine.model.qurankit.Reading
import com.quranengine.model.qurantext.FontSize
import com.quranengine.ui.theme.AppearanceMode
import com.quranengine.ui.theme.ThemeStyle
import com.quranengine.ui.theme.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val themeStyle: ThemeStyle = ThemeStyle.defaultStyle,
    val appearanceMode: AppearanceMode = AppearanceMode.defaultMode,
    val arabicFontSize: FontSize = FontSize.LARGE,
    val translationFontSize: FontSize = FontSize.LARGE,
    val audioEnd: AudioEnd = AudioEnd.JUZ,
    val reading: Reading = Reading.HAFS_1405,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val fontSizePreferences: FontSizePreferences,
    private val audioPreferences: AudioPreferences,
    private val readingPreferences: ReadingPreferences,
    private val themePreferences: ThemePreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
            themeStyle = themePreferences.themeStyle,
            appearanceMode = themePreferences.appearanceMode,
            arabicFontSize = fontSizePreferences.arabicFontSize,
            translationFontSize = fontSizePreferences.translationFontSize,
            audioEnd = audioPreferences.audioEnd,
            reading = readingPreferences.reading,
        )
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            fontSizePreferences.arabicFontSizeFlow.collect { size ->
                _state.update { it.copy(arabicFontSize = size) }
            }
        }
        viewModelScope.launch {
            fontSizePreferences.translationFontSizeFlow.collect { size ->
                _state.update { it.copy(translationFontSize = size) }
            }
        }
        viewModelScope.launch {
            readingPreferences.readingFlow.collect { reading ->
                _state.update { it.copy(reading = reading) }
            }
        }
        viewModelScope.launch {
            themePreferences.themeStyleFlow.collect { style ->
                _state.update { it.copy(themeStyle = style) }
            }
        }
        viewModelScope.launch {
            themePreferences.appearanceModeFlow.collect { mode ->
                _state.update { it.copy(appearanceMode = mode) }
            }
        }
    }

    fun setThemeStyle(style: ThemeStyle) {
        themePreferences.themeStyle = style
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        themePreferences.appearanceMode = mode
    }

    fun setArabicFontSize(size: FontSize) {
        fontSizePreferences.arabicFontSize = size
    }

    fun setTranslationFontSize(size: FontSize) {
        fontSizePreferences.translationFontSize = size
    }

    fun setAudioEnd(audioEnd: AudioEnd) {
        audioPreferences.audioEnd = audioEnd
        _state.update { it.copy(audioEnd = audioEnd) }
    }

    fun setReading(reading: Reading) {
        readingPreferences.reading = reading
    }
}
