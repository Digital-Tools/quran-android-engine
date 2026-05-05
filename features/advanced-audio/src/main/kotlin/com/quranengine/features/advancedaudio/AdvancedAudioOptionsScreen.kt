package com.quranengine.features.advancedaudio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.quranengine.core.audioplayer.Runs
import com.quranengine.ui.components.ChoicesView
import com.quranengine.ui.components.NoorAccessory
import com.quranengine.ui.components.NoorBasicSection
import com.quranengine.ui.components.NoorListItem

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
            TopAppBar(
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
                    AyahStepperRow(
                        ayah = fromVerse,
                        onPrevious = viewModel::stepFromBackward,
                        onNext = viewModel::stepFromForward,
                    )
                }
            }

            // To verse
            item {
                NoorBasicSection(title = "To") {
                    AyahStepperRow(
                        ayah = toVerse,
                        onPrevious = viewModel::stepToBackward,
                        onNext = viewModel::stepToForward,
                    )
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

private val runsOptions = listOf(Runs.ONE, Runs.TWO, Runs.THREE, Runs.INDEFINITE)

private fun runsLabel(runs: Runs): String = when (runs) {
    Runs.ONE -> "1x"
    Runs.TWO -> "2x"
    Runs.THREE -> "3x"
    Runs.FOUR -> "4x"
    Runs.INDEFINITE -> "Loop"
}

@Composable
private fun AyahStepperRow(
    ayah: com.quranengine.model.qurankit.AyahNumber,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    NoorListItem(
        title = formatAyah(ayah),
        subtitle = "Adjust the playback boundary one ayah at a time",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(
            onClick = onPrevious,
            enabled = ayah.previous != null,
        ) {
            Text("Previous")
        }
        TextButton(
            onClick = onNext,
            enabled = ayah.next != null,
        ) {
            Text("Next")
        }
    }
}

private fun formatAyah(ayah: com.quranengine.model.qurankit.AyahNumber): String =
    "Sura ${ayah.sura.suraNumber}, Ayah ${ayah.ayah}"

private fun formatPlaybackRate(rate: Float): String = "${rate}x"
