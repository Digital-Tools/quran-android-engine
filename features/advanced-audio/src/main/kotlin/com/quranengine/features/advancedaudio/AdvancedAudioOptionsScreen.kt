package com.quranengine.features.advancedaudio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.IconButton
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.quranengine.domain.qurantextkit.arabicSuraName
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Sura
import com.quranengine.ui.components.NoorDivider
import com.quranengine.ui.theme.QuranFontFamilies
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
    val endAt by viewModel.endAt.collectAsState()

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
                title = {},
                navigationIcon = {
                    TextButton(onClick = viewModel::dismiss) {
                        Text("Cancel", color = QuranTheme.mizanGold)
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::play,
                        enabled = reciter != null,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = QuranTheme.mizanGold,
                        )
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
            // Reciter
            item {
                NoorBasicSection {
                    NoorListItem(
                        title = reciter?.let(viewModel::localizedName) ?: "Loading reciter...",
                        accessory = NoorAccessory.DisclosureIndicator,
                        onClick = onNavigateToReciterList,
                    )
                }
            }

            // One card, as on iOS: From and To open a surah + ayah wheel in
            // place, on the current selection; then End at.
            item {
                NoorBasicSection(title = "Playback ayah range") {
                    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
                    BoundaryRow(
                        title = "From",
                        verse = fromVerse,
                        isExpanded = expanded == "from",
                        onClick = { expanded = if (expanded == "from") null else "from" },
                    )
                    if (expanded == "from") {
                        AyahWheelPicker(
                            selection = fromVerse,
                            minimum = null,
                            onSelection = viewModel::selectFrom,
                        )
                    }
                    NoorDivider()
                    BoundaryRow(
                        title = "To",
                        verse = toVerse,
                        isExpanded = expanded == "to",
                        onClick = { expanded = if (expanded == "to") null else "to" },
                    )
                    if (expanded == "to") {
                        AyahWheelPicker(
                            selection = toVerse,
                            minimum = fromVerse,
                            onSelection = viewModel::selectTo,
                        )
                    }
                    NoorDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "End at",
                            style = MaterialTheme.typography.bodySmall,
                            color = QuranTheme.colors.secondaryText,
                            modifier = Modifier.padding(end = 10.dp),
                        )
                        ChoicesView(
                            items = EndAtChoice.entries,
                            selectedItem = endAt,
                            onItemSelected = viewModel::setEndAt,
                            modifier = Modifier.weight(1f),
                            label = EndAtChoice::label,
                        )
                    }
                }
            }

            // Playback speed: the same choices as the audio bar's speed chip.
            item {
                NoorBasicSection(title = "Playback Speed") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        PlaybackRates.forEach { rate ->
                            ChoicePill(
                                label = formatPlaybackRate(rate),
                                selected = rate == playbackRate,
                                onClick = { viewModel.setPlaybackRate(rate) },
                            )
                        }
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

/** "Az-Zukhruf <surah name glyph> · 43:1" */
@Composable
private fun BoundaryRow(
    title: String,
    verse: AyahNumber,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val accent = if (isExpanded) QuranTheme.mizanGold else QuranTheme.colors.secondaryText
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = QuranTheme.colors.text,
        )
        Spacer(modifier = Modifier.weight(1f).padding(horizontal = 8.dp))
        Text(
            text = suraLabel(verse.sura, suffix = " · ${verse.sura.suraNumber}:${verse.ayah}"),
            style = MaterialTheme.typography.bodyLarge,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(3f, fill = false),
        )
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Close" else "Choose",
            tint = accent,
            modifier = Modifier
                .padding(start = 4.dp)
                .rotate(if (isExpanded) 180f else 0f),
        )
    }
}

/** A surah name with its calligraphy glyph. */
private fun suraLabel(sura: Sura, prefix: String = "", suffix: String = ""): AnnotatedString =
    buildAnnotatedString {
        append(prefix)
        append(sura.englishName())
        append(" ")
        withStyle(SpanStyle(fontFamily = QuranFontFamilies.suraNames)) {
            append(sura.arabicSuraName())
        }
        append(suffix)
    }

/**
 * Surah wheel and ayah wheel side by side, opening on [selection]. A To
 * picker passes From as [minimum] so it can't go before it.
 */
@Composable
private fun AyahWheelPicker(
    selection: AyahNumber,
    minimum: AyahNumber?,
    onSelection: (AyahNumber) -> Unit,
) {
    val quran = selection.quran
    val suras = remember(quran, minimum?.sura?.suraNumber) {
        quran.suras.filter { minimum == null || it.suraNumber >= minimum.sura.suraNumber }
    }
    val firstAyah = if (minimum != null && minimum.sura == selection.sura) minimum.ayah else 1
    val ayahs = (firstAyah..selection.sura.lastVerse.ayah).toList()
    Row(modifier = Modifier.fillMaxWidth()) {
        WheelPicker(
            items = suras,
            selected = selection.sura,
            onSelected = { sura -> onSelection(selection.selecting(sura, minimum)) },
            modifier = Modifier.weight(1f),
        ) { sura, emphasis ->
            Text(
                text = suraLabel(sura, prefix = "${sura.suraNumber} · "),
                style = if (emphasis == 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                color = QuranTheme.colors.text.copy(alpha = wheelAlpha(emphasis)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .width(1.dp)
                .height(WheelItemHeight * (WheelVisibleItems - 1))
                .background(QuranTheme.colors.secondaryText.copy(alpha = 0.25f)),
        )
        // A new surah has a new list of ayahs: start a fresh wheel on the
        // selected ayah instead of keeping the old scroll offset.
        key(selection.sura.suraNumber, firstAyah) {
            WheelPicker(
                items = ayahs,
                selected = selection.ayah,
                onSelected = { ayah -> AyahNumber(selection.sura, ayah)?.let(onSelection) },
                modifier = Modifier.width(132.dp),
            ) { ayah, emphasis ->
                Text(
                    text = "Ayah $ayah",
                    style = if (emphasis == 0) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                    color = QuranTheme.colors.text.copy(alpha = wheelAlpha(emphasis)),
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

private val PlaybackRates = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

private fun formatPlaybackRate(rate: Float): String {
    val text = if (rate == rate.toLong().toFloat()) {
        rate.toLong().toString()
    } else {
        rate.toString().trimEnd('0').trimEnd('.')
    }
    return "$text×"
}

@Composable
private fun ChoicePill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = if (selected) Color.White else QuranTheme.colors.text,
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (selected) QuranTheme.mizanGold.copy(alpha = 0.85f) else QuranTheme.colors.background,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
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
    WheelPicker(items = wheelRuns, selected = selected, onSelected = onSelected) { option, emphasis ->
        Text(
            text = if (option == Runs.INDEFINITE) "Loop ∞" else runsLabel(option),
            style = if (emphasis == 0) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            color = QuranTheme.colors.text.copy(alpha = wheelAlpha(emphasis)),
        )
    }
}

private val WheelItemHeight = 40.dp
private const val WheelVisibleItems = 5

private fun wheelAlpha(distance: Int): Float = when (distance) {
    0 -> 1f
    1 -> 0.45f
    else -> 0.2f
}

/**
 * A snapping wheel like the iOS picker: the centered row is the selection,
 * it opens on [selected] and follows it when it changes from outside (for
 * example End at moving To). [item] gets the row's distance from the
 * center for its styling.
 */
@Composable
private fun <T> WheelPicker(
    items: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    item: @Composable (item: T, distance: Int) -> Unit,
) {
    val selectedIndex = items.indexOf(selected).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val itemHeightPx = with(LocalDensity.current) { WheelItemHeight.toPx() }
    val scope = rememberCoroutineScope()

    // Index of the row in the middle slot (top padding is two rows).
    val centeredIndex by remember(items) {
        derivedStateOf {
            val offsetRows = if (listState.firstVisibleItemScrollOffset > itemHeightPx / 2) 1 else 0
            (listState.firstVisibleItemIndex + offsetRows).coerceIn(0, items.lastIndex)
        }
    }
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress && items.isNotEmpty()) {
            val choice = items[centeredIndex]
            if (choice != selected) onSelected(choice)
        }
    }
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress && centeredIndex != selectedIndex) {
            listState.scrollToItem(selectedIndex)
        }
    }

    Box(
        modifier = modifier.height(WheelItemHeight * WheelVisibleItems),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .height(WheelItemHeight)
                .clip(RoundedCornerShape(10.dp))
                .background(QuranTheme.colors.background),
        )
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState),
            contentPadding = PaddingValues(vertical = WheelItemHeight * (WheelVisibleItems / 2)),
            modifier = Modifier.fillMaxSize(),
        ) {
            itemsIndexed(items) { index, option ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WheelItemHeight)
                        .clickable { scope.launch { listState.animateScrollToItem(index) } }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    item(option, kotlin.math.abs(index - centeredIndex))
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

