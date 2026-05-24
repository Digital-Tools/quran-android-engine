package com.quranengine.features.advancedaudio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.quranengine.core.audioplayer.Runs
import com.quranengine.domain.qurantextkit.englishName
import com.quranengine.ui.components.ChoicesView
import com.quranengine.ui.components.NoorAccessory
import com.quranengine.ui.components.NoorBasicSection
import com.quranengine.ui.components.NoorListItem
import com.quranengine.ui.theme.QuranTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedAudioOptionsScreen(
    viewModel: AdvancedAudioOptionsViewModel,
    onNavigateToReciterList: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val reciter by viewModel.reciter.collectAsState()
    val fromVerse by viewModel.fromVerse.collectAsState()
    val toVerse by viewModel.toVerse.collectAsState()
    val verseRuns by viewModel.verseRuns.collectAsState()
    val listRuns by viewModel.listRuns.collectAsState()
    val playbackRate by viewModel.playbackRate.collectAsState()
    val suras = viewModel.suras

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshReciter()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Advanced Audio") },
                navigationIcon = {
                    TextButton(onClick = viewModel::dismiss) {
                        Text("Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = viewModel::play,
                        enabled = reciter != null,
                    ) {
                        Text("Play")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Reciter section
            item {
                NoorBasicSection(title = "Reciter") {
                    NoorListItem(
                        title = reciter?.let(viewModel::localizedName) ?: "Loading reciter...",
                        subtitle = "Tap to change reciter",
                        accessory = NoorAccessory.DisclosureIndicator,
                        onClick = onNavigateToReciterList,
                    )
                }
            }

            // Quick end-point buttons
            item {
                NoorBasicSection(title = "Play To End Of") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextButton(onClick = viewModel::setLastVerseToEndOfPage) {
                            Text("Page")
                        }
                        TextButton(onClick = viewModel::setLastVerseToEndOfSura) {
                            Text("Sura")
                        }
                        TextButton(onClick = viewModel::setLastVerseToEndOfJuz) {
                            Text("Juz")
                        }
                        TextButton(onClick = viewModel::setLastVerseToEndOfQuran) {
                            Text("Quran")
                        }
                    }
                }
            }

            // From verse
            item {
                NoorBasicSection(title = "From") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        var fromSuraExpanded by remember { mutableStateOf(false) }
                        var fromAyahExpanded by remember { mutableStateOf(false) }

                        QuranDropdownSelector(
                            label = "Sura",
                            valueText = "${fromVerse.sura.suraNumber}. ${fromVerse.sura.englishName()}",
                            expanded = fromSuraExpanded,
                            onExpandedChange = { fromSuraExpanded = it },
                            onDismissRequest = { fromSuraExpanded = false },
                            modifier = Modifier.weight(3f),
                        ) {
                            suras.forEach { sura ->
                                DropdownMenuItem(
                                    text = { Text("${sura.suraNumber}. ${sura.englishName()}", color = QuranTheme.colors.text) },
                                    onClick = {
                                        viewModel.selectFromSura(sura.suraNumber)
                                        fromSuraExpanded = false
                                    }
                                )
                            }
                        }

                        QuranDropdownSelector(
                            label = "Ayah",
                            valueText = fromVerse.ayah.toString(),
                            expanded = fromAyahExpanded,
                            onExpandedChange = { fromAyahExpanded = it },
                            onDismissRequest = { fromAyahExpanded = false },
                            modifier = Modifier.weight(1.5f),
                        ) {
                            (1..fromVerse.sura.lastVerse.ayah).forEach { ayahNum ->
                                DropdownMenuItem(
                                    text = { Text(ayahNum.toString(), color = QuranTheme.colors.text) },
                                    onClick = {
                                        viewModel.selectFromAyah(ayahNum)
                                        fromAyahExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // To verse
            item {
                NoorBasicSection(title = "To") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        var toSuraExpanded by remember { mutableStateOf(false) }
                        var toAyahExpanded by remember { mutableStateOf(false) }

                        val availableToSuras = suras.filter { it.suraNumber >= fromVerse.sura.suraNumber }

                        QuranDropdownSelector(
                            label = "Sura",
                            valueText = "${toVerse.sura.suraNumber}. ${toVerse.sura.englishName()}",
                            expanded = toSuraExpanded,
                            onExpandedChange = { toSuraExpanded = it },
                            onDismissRequest = { toSuraExpanded = false },
                            modifier = Modifier.weight(3f),
                        ) {
                            availableToSuras.forEach { sura ->
                                DropdownMenuItem(
                                    text = { Text("${sura.suraNumber}. ${sura.englishName()}", color = QuranTheme.colors.text) },
                                    onClick = {
                                        viewModel.selectToSura(sura.suraNumber)
                                        toSuraExpanded = false
                                    }
                                )
                            }
                        }

                        val availableToAyahs = if (toVerse.sura == fromVerse.sura) {
                            fromVerse.ayah..toVerse.sura.lastVerse.ayah
                        } else {
                            1..toVerse.sura.lastVerse.ayah
                        }

                        QuranDropdownSelector(
                            label = "Ayah",
                            valueText = toVerse.ayah.toString(),
                            expanded = toAyahExpanded,
                            onExpandedChange = { toAyahExpanded = it },
                            onDismissRequest = { toAyahExpanded = false },
                            modifier = Modifier.weight(1.5f),
                        ) {
                            availableToAyahs.forEach { ayahNum ->
                                DropdownMenuItem(
                                    text = { Text(ayahNum.toString(), color = QuranTheme.colors.text) },
                                    onClick = {
                                        viewModel.selectToAyah(ayahNum)
                                        toAyahExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Playback speed
            item {
                NoorBasicSection(title = "Playback Speed") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = formatPlaybackRate(playbackRate),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Slider(
                            value = playbackRate,
                            onValueChange = viewModel::setPlaybackRate,
                            valueRange = 0.5f..2f,
                            steps = 5,
                        )
                    }
                }
            }

            // Verse repetitions
            item {
                NoorBasicSection(title = "Verse Repetitions") {
                    ChoicesView(
                        items = runsOptions,
                        selectedItem = verseRuns,
                        onItemSelected = viewModel::setVerseRuns,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        label = ::runsLabel,
                    )
                }
            }

            // List repetitions
            item {
                NoorBasicSection(title = "List Repetitions") {
                    ChoicesView(
                        items = runsOptions,
                        selectedItem = listRuns,
                        onItemSelected = viewModel::setListRuns,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        label = ::runsLabel,
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun QuranDropdownSelector(
    label: String,
    valueText: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = QuranTheme.colors.secondaryText,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(QuranTheme.colors.background)
                    .clickable { onExpandedChange(true) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = QuranTheme.colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = QuranTheme.colors.secondaryText,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest,
                modifier = Modifier
                    .background(QuranTheme.colors.secondaryBackground)
                    .heightIn(max = 280.dp)
            ) {
                content()
            }
        }
    }
}

private val runsOptions = listOf(Runs.ONE, Runs.TWO, Runs.THREE, Runs.INDEFINITE)

private fun runsLabel(runs: Runs): String = when (runs) {
    Runs.ONE -> "1x"
    Runs.TWO -> "2x"
    Runs.THREE -> "3x"
    Runs.FOUR -> "4x"
    Runs.INDEFINITE -> "Loop"
}

private fun formatPlaybackRate(rate: Float): String = "${rate}x"
