package com.quranengine.features.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.quranengine.domain.readingservice.ReadingHighlightStyle
import com.quranengine.model.quranaudio.AudioEnd
import com.quranengine.model.qurantext.FontSize
import com.quranengine.ui.components.AppearanceModeSelector
import com.quranengine.ui.components.ChoicesView
import com.quranengine.ui.components.NoorAccessory
import com.quranengine.ui.components.NoorBasicSection
import com.quranengine.ui.components.NoorListItem
import com.quranengine.ui.components.NoorSection
import com.quranengine.ui.components.ThemeStyleSelector

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToTranslations: () -> Unit = {},
    onNavigateToReciters: () -> Unit = {},
    onNavigateToAudioDownloads: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Theme
        item {
            NoorBasicSection(title = "Theme") {
                ThemeStyleSelector(
                    selectedStyle = state.themeStyle,
                    onStyleSelected = viewModel::setThemeStyle,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }

        // Appearance
        item {
            NoorBasicSection(title = "Appearance") {
                AppearanceModeSelector(
                    selectedMode = state.appearanceMode,
                    onModeSelected = viewModel::setAppearanceMode,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }

        // Font Size
        item {
            NoorBasicSection(title = "Font Size") {
                FontSizeRow(
                    label = "Arabic",
                    currentSize = state.arabicFontSize,
                    onSizeSelected = viewModel::setArabicFontSize,
                )
                FontSizeRow(
                    label = "Translation",
                    currentSize = state.translationFontSize,
                    onSizeSelected = viewModel::setTranslationFontSize,
                )
            }
        }

        // Audio
        item {
            NoorBasicSection(title = "Audio") {
                NoorListItem(
                    title = "Reciters",
                    accessory = NoorAccessory.DisclosureIndicator,
                    onClick = onNavigateToReciters,
                )
                NoorListItem(
                    title = "Audio Manager",
                    subtitle = "Download a reciter's full Quran",
                    accessory = NoorAccessory.DisclosureIndicator,
                    onClick = onNavigateToAudioDownloads,
                )
                ChoicesView(
                    items = AudioEnd.entries.toList(),
                    selectedItem = state.audioEnd,
                    onItemSelected = viewModel::setAudioEnd,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    label = { audioEnd ->
                        when (audioEnd) {
                            AudioEnd.SURA -> "Sura"
                            AudioEnd.JUZ -> "Juz"
                            AudioEnd.PAGE -> "Page"
                            AudioEnd.QURAN -> "Quran"
                        }
                    },
                )
                NoorListItem(title = "Highlight while playing")
                ChoicesView(
                    items = ReadingHighlightStyle.entries.toList(),
                    selectedItem = state.highlightStyle,
                    onItemSelected = viewModel::setHighlightStyle,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    label = { style ->
                        when (style) {
                            ReadingHighlightStyle.WORD -> "Word"
                            ReadingHighlightStyle.LINE -> "Full line"
                        }
                    },
                )
            }
        }

        // Translations
        item {
            NoorSection(
                items = listOf("Translations"),
                title = "Translations",
            ) { title ->
                NoorListItem(
                    title = title,
                    accessory = NoorAccessory.DisclosureIndicator,
                    onClick = onNavigateToTranslations,
                )
            }
        }

        // About
        item {
            NoorSection(
                items = aboutItems,
                title = "About",
            ) { item ->
                NoorListItem(
                    title = item.title,
                    accessory = NoorAccessory.DisclosureIndicator,
                    onClick = {
                        when (item) {
                            AboutItem.SHARE -> shareMizan(context)
                            AboutItem.REVIEW -> openMizanUrl(context, MizanSettingsLinks.REVIEW)
                            AboutItem.CONTACT -> openMizanUrl(context, MizanSettingsLinks.CONTACT)
                        }
                    },
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// -- Font size helpers --

private val fontSizeOptions = listOf(
    FontSize.X_SMALL,
    FontSize.SMALL,
    FontSize.MEDIUM,
    FontSize.LARGE,
    FontSize.X_LARGE,
    FontSize.XX_LARGE,
    FontSize.XXX_LARGE,
)

@Composable
private fun FontSizeRow(
    label: String,
    currentSize: FontSize,
    onSizeSelected: (FontSize) -> Unit,
) {
    NoorListItem(title = label)
    ChoicesView(
        items = fontSizeOptions,
        selectedItem = currentSize,
        onItemSelected = onSizeSelected,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        label = { size ->
            when (size) {
                FontSize.X_SMALL -> "XS"
                FontSize.SMALL -> "S"
                FontSize.MEDIUM -> "M"
                FontSize.LARGE -> "L"
                FontSize.X_LARGE -> "XL"
                FontSize.XX_LARGE -> "2XL"
                FontSize.XXX_LARGE -> "3XL"
                else -> size.name
            }
        },
    )
}

// -- About section --

private enum class AboutItem(val title: String) {
    SHARE("Share App"),
    REVIEW("Write a Review"),
    CONTACT("Contact Us"),
}

private val aboutItems = AboutItem.entries.toList()

private object MizanSettingsLinks {
    const val SHARE = "https://mizanapp.org"
    const val REVIEW = "https://mizanapp.org"
    const val CONTACT = "https://mizanapp.org/contact"
}

private fun shareMizan(context: Context) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Mizan")
        putExtra(Intent.EXTRA_TEXT, "Mizan\n${MizanSettingsLinks.SHARE}")
    }
    context.startActivity(Intent.createChooser(send, "Share App"))
}

private fun openMizanUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
