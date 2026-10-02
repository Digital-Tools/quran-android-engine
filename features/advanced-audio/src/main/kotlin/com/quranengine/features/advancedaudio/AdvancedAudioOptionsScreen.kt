package com.quranengine.features.advancedaudio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.quranengine.core.audioplayer.RepetitionDelay
import com.quranengine.core.audioplayer.Runs
import com.quranengine.core.audioplayer.VerseDelay
import com.quranengine.domain.qurantextkit.englishName
import com.quranengine.ui.components.ChoicesView
import com.quranengine.ui.components.NoorAccessory
import com.quranengine.ui.components.NoorBasicSection
import com.quranengine.ui.components.NoorListItem
import com.quranengine.ui.theme.QuranTheme
import kotlinx.coroutines.launch

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
    val verseDelay by viewModel.verseDelay.collectAsState()
    val repetitionDelay by viewModel.repetitionDelay.collectAsState()
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

            // Play each verse: repeat count + pause after each verse
            item {
                NoorBasicSection(title = "Play each verse") {
                    RepeatCountRow(runs = verseRuns, onRunsChanged = viewModel::setVerseRuns)
                    PauseChoices(
                        title = "Pause After Each Verse",
                        description = "Choose how long to pause after each verse. 1× waits " +
                            "about as long as the verse took to play, giving you time to " +
                            "repeat or memorize before continuing.",
                        items = VerseDelay.entries,
                        selected = verseDelay,
                        onSelected = viewModel::setVerseDelay,
                        label = ::verseDelayLabel,
                    )
                }
            }

            // Play set of verses: repeat count + pause between repetitions
            item {
                NoorBasicSection(title = "Play set of verses") {
                    RepeatCountRow(runs = listRuns, onRunsChanged = viewModel::setListRuns)
                    PauseChoices(
                        title = "Pause Between Repetitions (seconds)",
                        description = "Sets how long to pause before the next repetition starts.",
                        items = RepetitionDelay.entries,
                        selected = repetitionDelay,
                        onSelected = viewModel::setRepetitionDelay,
                        label = ::repetitionDelayLabel,
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

private fun runsLabel(runs: Runs): String =
    if (runs == Runs.INDEFINITE) "Loop" else "${runs.maxRuns}×"

private fun verseDelayLabel(delay: VerseDelay): String =
    if (delay == VerseDelay.NONE) "Off" else "${formatMultiplier(delay.multiplier)}×"

private fun repetitionDelayLabel(delay: RepetitionDelay): String =
    if (delay == RepetitionDelay.NONE) "Off" else "${delay.seconds.toInt()}s"

private fun formatMultiplier(value: Double): String =
    if (value == value.toInt().toDouble()) value.toInt().toString() else value.toString().removePrefix("0")
        .let { if (it.startsWith(".")) "0$it" else it }

/** Loop first, then 1× through 100×, like iOS. */
private val wheelRuns: List<Runs> = listOf(Runs.INDEFINITE) + (1..100).map(Runs::of)

/** "Repeat Count" row showing the choice; tapping expands an inline wheel. */
@Composable
private fun RepeatCountRow(runs: Runs, onRunsChanged: (Runs) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val accent = if (expanded) QuranTheme.mizanGold else QuranTheme.colors.secondaryText
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Repeat Count",
                style = MaterialTheme.typography.bodyLarge,
                color = QuranTheme.colors.text,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (runs == Runs.INDEFINITE) "Loop ∞" else runsLabel(runs),
                style = MaterialTheme.typography.bodyLarge,
                color = accent,
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = accent,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .rotate(if (expanded) 180f else 0f),
            )
        }
        if (expanded) {
            RunsWheel(selected = runs, onSelected = onRunsChanged)
        }
    }
}

/** A snapping wheel of [wheelRuns]; the centered row is the selection. */
@Composable
private fun RunsWheel(selected: Runs, onSelected: (Runs) -> Unit) {
    val itemHeight = 40.dp
    val visibleItems = 5
    val initialIndex = wheelRuns.indexOf(selected).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    val scope = rememberCoroutineScope()

    // Index of the row in the middle slot (top padding is two rows).
    val centeredIndex by remember {
        derivedStateOf {
            val offsetRows = if (listState.firstVisibleItemScrollOffset > itemHeightPx / 2) 1 else 0
            (listState.firstVisibleItemIndex + offsetRows).coerceIn(0, wheelRuns.lastIndex)
        }
    }
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val choice = wheelRuns[centeredIndex]
            if (choice != selected) onSelected(choice)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight * visibleItems),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(itemHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(QuranTheme.colors.secondaryBackground),
        )
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItems / 2)),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(wheelRuns) { index, option ->
                val distance = kotlin.math.abs(index - centeredIndex)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable { scope.launch { listState.animateScrollToItem(index) } },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (option == Runs.INDEFINITE) "Loop ∞" else runsLabel(option),
                        style = if (distance == 0) {
                            MaterialTheme.typography.titleLarge
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        color = QuranTheme.colors.text.copy(
                            alpha = when (distance) {
                                0 -> 1f
                                1 -> 0.45f
                                else -> 0.2f
                            },
                        ),
                    )
                }
            }
        }
    }
}

/** A pause option as segmented choices with a title and an explanation. */
@Composable
private fun <T> PauseChoices(
    title: String,
    description: String,
    items: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    label: (T) -> String,
) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = QuranTheme.colors.secondaryText,
        )
        ChoicesView(
            items = items,
            selectedItem = selected,
            onItemSelected = onSelected,
            modifier = Modifier.padding(vertical = 8.dp),
            label = label,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = QuranTheme.colors.secondaryText,
        )
    }
}

private fun formatPlaybackRate(rate: Float): String = "${rate}x"
