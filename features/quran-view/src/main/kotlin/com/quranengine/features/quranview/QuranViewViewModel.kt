package com.quranengine.features.quranview

import android.graphics.RectF
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.core.localization.Localizer
import com.quranengine.core.localization.NumberFormatters
import com.quranengine.core.localization.format
import com.quranengine.data.sqlite.ReadOnlyDatabase
import com.quranengine.data.wordframe.SqliteWordFramePersistence
import com.quranengine.domain.annotationservice.LastPageService
import com.quranengine.domain.annotationservice.LastPageUpdater
import com.quranengine.domain.annotationservice.NoteService
import com.quranengine.domain.annotationservice.PageBookmarkService
import com.quranengine.domain.imageservice.ImageDataService
import com.quranengine.domain.qurantextkit.AyahShareUseCase
import com.quranengine.domain.qurantextkit.decoratedName
import com.quranengine.domain.qurantextkit.englishName
import com.quranengine.domain.qurantextkit.QuranContentStatePreferences
import com.quranengine.domain.qurantextkit.QuranTextDataService
import com.quranengine.domain.qurantextkit.localizedName
import com.quranengine.domain.qurantextkit.localizedQuarterName
import com.quranengine.domain.readingservice.ReadingAssetsInstaller
import com.quranengine.domain.readingservice.ReadingHighlightStyle
import com.quranengine.domain.readingservice.ReadingPreferences
import com.quranengine.domain.readingservice.imageResources
import com.quranengine.domain.readingservice.localPath
import com.quranengine.domain.translationservice.QuranContentBootstrap
import com.quranengine.features.qurancontent.ContentState
import com.quranengine.features.quranimage.ContentImageState
import com.quranengine.features.qurantranslation.TranslationItem
import com.quranengine.features.audiobanner.AyahPlaybackProgress
import com.quranengine.model.qurangeometry.WordFrameCollection
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Page
import com.quranengine.model.qurankit.Reading
import com.quranengine.model.quranannotations.Note
import com.quranengine.model.qurankit.arrayTo
import com.quranengine.model.qurantext.QuranMode
import com.quranengine.model.qurantext.TranslationText
import com.quranengine.domain.translationservice.SelectedTranslationsPreferences
import com.quranengine.ui.quran.ImageDecorations
import com.quranengine.ui.quran.WordHighlight
import com.quranengine.ui.quran.toLineHighlights
import com.quranengine.ui.theme.QuranColors
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TranslationPageContent(
    val items: List<TranslationItem> = emptyList(),
    val placeholderTitle: String? = null,
    val placeholderMessage: String? = null,
)

@HiltViewModel
class QuranViewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val localizer: Localizer,
    private val readingPreferences: ReadingPreferences,
    private val quranContentStatePreferences: QuranContentStatePreferences,
    private val quranTextDataService: QuranTextDataService,
    private val ayahShareUseCase: AyahShareUseCase,
    private val selectedTranslationsPreferences: SelectedTranslationsPreferences,
    private val pageBookmarkService: PageBookmarkService,
    private val noteService: NoteService,
    private val lastPageService: LastPageService,
    private val lastPageUpdater: LastPageUpdater,
    private val readingAssetsInstaller: ReadingAssetsInstaller,
    private val quranContentBootstrap: QuranContentBootstrap,
    @Named("baseDir") private val baseDir: File,
) : ViewModel() {

    private val initialPageNumber: Int = savedStateHandle["page"] ?: 1
    private var currentReading: Reading = readingPreferences.reading

    private val _state = MutableStateFlow(
        QuranViewState(
            currentPage = initialPageNumber,
            totalPages = currentReading.quran.pages.size,
            visiblePages = normalizeVisiblePages(
                pageNumbers = listOf(initialPageNumber),
                twoPagesEnabled = quranContentStatePreferences.twoPagesEnabled,
                totalPages = currentReading.quran.pages.size,
            ),
            quranMode = quranContentStatePreferences.quranMode,
            twoPagesEnabled = quranContentStatePreferences.twoPagesEnabled,
        )
    )
    val state: StateFlow<QuranViewState> = _state.asStateFlow()

    private val _pageContentStates = MutableStateFlow<Map<Int, ContentState<ContentImageState>>>(emptyMap())
    val pageContentStates: StateFlow<Map<Int, ContentState<ContentImageState>>> = _pageContentStates.asStateFlow()

    private val _translationContentStates = MutableStateFlow<Map<Int, ContentState<TranslationPageContent>>>(emptyMap())
    val translationContentStates: StateFlow<Map<Int, ContentState<TranslationPageContent>>> = _translationContentStates.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val basePageContents = mutableMapOf<Int, PageImageContent>()
    private val basePageTranslationContents = mutableMapOf<Int, TranslationPageContent>()
    private var bookmarkedPageNumbers: Set<Int> = emptySet()
    private var highlightedAyahProgress: AyahPlaybackProgress? = null
    private var bookmarkObservationJob: Job? = null
    private var noteObservationJob: Job? = null
    private var notesByVerse: Map<AyahNumber, Note> = emptyMap()
    private var highlightStyle: ReadingHighlightStyle = readingPreferences.highlightStyle
    private var lastPageUpdaterConfigured = false

    init {
        observeBootstrap()
        observeReaderPreferences()
        observeReading()
        observeSelectedTranslations()
        observeNotes()
    }

    fun toggleBars() {
        _state.update { current ->
            current.copy(barsVisible = !current.barsVisible)
        }
    }

    fun setBarsVisible(visible: Boolean) {
        _state.update { current ->
            current.copy(barsVisible = visible)
        }
    }

    fun showMessage(message: String) {
        _userMessage.value = message
    }

    fun setReadingAyah(progress: AyahPlaybackProgress?) {
        highlightedAyahProgress = progress
        refreshArabicHighlights()

        if (progress != null) {
            val ayahPage = progress.ayah.page.pageNumber
            val currentVisiblePages = _state.value.visiblePages
            if (ayahPage !in currentVisiblePages) {
                setVisiblePages(listOf(ayahPage))
            }
        }
    }

    fun toggleQuranMode() {
        quranContentStatePreferences.quranMode = when (_state.value.quranMode) {
            QuranMode.ARABIC -> QuranMode.TRANSLATION
            QuranMode.TRANSLATION -> QuranMode.ARABIC
        }
    }

    fun toggleCurrentPageBookmark() {
        updateBookmark(
            pageNumber = _state.value.currentPage,
            toggle = true,
        )
    }

    fun addBookmarkForAyah(ayah: AyahNumber) {
        updateBookmark(
            pageNumber = ayah.page.pageNumber,
            toggle = false,
        )
    }

    /** Highlight verses with the given (or last-used) color. Matches iOS updateHighlight. */
    fun highlightAyah(ayahs: List<AyahNumber>, color: Note.Color? = null) {
        viewModelScope.launch {
            try {
                val effectiveColor = color ?: noteService.color(notesByVerse.values.toList())
                noteService.updateHighlight(ayahs, effectiveColor, currentReading.quran)
                _userMessage.value = if (ayahs.size == 1) {
                    "Highlighted ${ayahs[0].sura.suraNumber}:${ayahs[0].ayah}."
                } else {
                    "Highlighted ${ayahs.size} verses."
                }
            } catch (error: Exception) {
                _userMessage.value = error.localizedMessage ?: "Unable to highlight."
            }
        }
    }

    /** Remove highlight/notes from verses. Matches iOS deleteNotes. */
    fun removeHighlight(ayahs: List<AyahNumber>) {
        viewModelScope.launch {
            try {
                noteService.removeNotes(ayahs)
                _userMessage.value = if (ayahs.size == 1) {
                    "Removed highlight for ${ayahs[0].sura.suraNumber}:${ayahs[0].ayah}."
                } else {
                    "Removed highlights for ${ayahs.size} verses."
                }
            } catch (error: Exception) {
                _userMessage.value = error.localizedMessage ?: "Unable to remove highlight."
            }
        }
    }

    /** Determine the note state for the menu. Matches iOS AyahMenuViewModel.noteState. */
    fun noteStateForVerses(ayahs: List<AyahNumber>): NoteState {
        val notes = ayahs.mapNotNull { notesByVerse[it] }
        if (notes.isEmpty()) return NoteState.NO_HIGHLIGHT
        return if (notes.any { !it.note.isNullOrEmpty() }) NoteState.NOTED else NoteState.HIGHLIGHTED
    }

    /** Get the current highlighting color for the selected verses. */
    fun highlightingColorForVerses(ayahs: List<AyahNumber>): Note.Color {
        val notes = ayahs.mapNotNull { notesByVerse[it] }
        return noteService.color(notes)
    }

    fun shareAyah(ayah: AyahNumber) {
        viewModelScope.launch {
            try {
                ayahShareUseCase.share(ayah)
            } catch (error: Exception) {
                _userMessage.value = error.localizedMessage ?: "Unable to share ayah."
            }
        }
    }

    fun copyAyah(ayah: AyahNumber, copyText: (String) -> Unit) {
        viewModelScope.launch {
            try {
                copyText(ayahShareUseCase.clipboardText(ayah))
                _userMessage.value = "Copied ayah ${ayah.sura.suraNumber}:${ayah.ayah}."
            } catch (error: Exception) {
                _userMessage.value = error.localizedMessage ?: "Unable to copy ayah."
            }
        }
    }

    fun saveNote(ayah: AyahNumber, note: String) {
        val trimmedNote = note.trim()
        if (trimmedNote.isEmpty()) {
            _userMessage.value = "Enter a note before saving."
            return
        }

        viewModelScope.launch {
            try {
                noteService.setNote(note = trimmedNote, verses = setOf(ayah))
                _userMessage.value = "Saved note for ${ayah.sura.suraNumber}:${ayah.ayah}."
            } catch (error: Exception) {
                _userMessage.value = error.localizedMessage ?: "Unable to save note."
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun setVisiblePages(pageNumbers: List<Int>) {
        viewModelScope.launch {
            refreshReader(
                pageNumbers = normalizeVisiblePages(
                    pageNumbers = pageNumbers,
                    twoPagesEnabled = _state.value.twoPagesEnabled,
                    totalPages = currentReading.quran.pages.size,
                ),
                updateLastPage = true,
            )
        }
    }

    private fun observeReaderPreferences() {
        viewModelScope.launch {
            quranContentStatePreferences.quranModeFlow.collect { mode ->
                _state.update { it.copy(quranMode = mode) }
                refreshReader(_state.value.visiblePages, updateLastPage = false)
            }
        }

        viewModelScope.launch {
            quranContentStatePreferences.twoPagesEnabledFlow.collect { enabled ->
                _state.update { it.copy(twoPagesEnabled = enabled) }
                refreshReader(
                    pageNumbers = normalizeVisiblePages(
                        pageNumbers = _state.value.visiblePages,
                        twoPagesEnabled = enabled,
                        totalPages = currentReading.quran.pages.size,
                    ),
                    updateLastPage = false,
                )
            }
        }

        viewModelScope.launch {
            readingPreferences.highlightStyleFlow.collect { style ->
                highlightStyle = style
                refreshArabicHighlights()
            }
        }
    }

    private fun observeReading() {
        viewModelScope.launch {
            readingPreferences.readingFlow.collect { reading ->
                currentReading = reading
                observeBookmarks(reading)
                basePageContents.clear()
                _pageContentStates.value = emptyMap()
                refreshReader(
                    pageNumbers = normalizeVisiblePages(
                        pageNumbers = _state.value.visiblePages,
                        twoPagesEnabled = _state.value.twoPagesEnabled,
                        totalPages = reading.quran.pages.size,
                    ),
                    updateLastPage = false,
                )
            }
        }
    }

    private fun observeBookmarks(reading: Reading) {
        bookmarkObservationJob?.cancel()
        bookmarkObservationJob = viewModelScope.launch {
            pageBookmarkService.pageBookmarks(reading.quran).collect { bookmarks ->
                bookmarkedPageNumbers = bookmarks.map { it.page.pageNumber }.toSet()
                _state.update { current ->
                    current.copy(
                        isCurrentPageBookmarked = current.currentPage in bookmarkedPageNumbers,
                    )
                }
            }
        }
    }

    private fun observeNotes() {
        noteObservationJob?.cancel()
        noteObservationJob = viewModelScope.launch {
            noteService.notes(currentReading.quran).collect { notes ->
                notesByVerse = notes.flatMap { note ->
                    note.verses.map { verse -> verse to note }
                }.toMap()
                // Persisted highlights only take effect once we re-derive the
                // already-loaded page content — the pages themselves aren't
                // reloaded, only their highlight decorations.
                refreshArabicHighlights()
                refreshTranslationHighlights()
            }
        }
    }

    private fun observeSelectedTranslations() {
        viewModelScope.launch {
            selectedTranslationsPreferences.selectedTranslationIdsFlow.collect {
                _translationContentStates.value = emptyMap()
                if (_state.value.quranMode == QuranMode.TRANSLATION) {
                    refreshReader(_state.value.visiblePages, updateLastPage = false)
                }
            }
        }
    }

    private fun observeBootstrap() {
        viewModelScope.launch {
            quranContentBootstrap.ready.first { it }
            _translationContentStates.value = emptyMap()
            if (_state.value.quranMode == QuranMode.TRANSLATION) {
                refreshReader(_state.value.visiblePages, updateLastPage = false)
            }
        }
    }

    private suspend fun refreshReader(
        pageNumbers: List<Int>,
        updateLastPage: Boolean,
    ) {
        val pages = pageNumbers.mapNotNull { currentReading.quran.pages.getOrNull(it - 1) }
            .sortedBy { it.pageNumber }
            .ifEmpty { listOf(currentReading.quran.pages.first()) }

        _state.update { current ->
            current.copy(
                currentPage = pages.first().pageNumber,
                totalPages = currentReading.quran.pages.size,
                visiblePages = pages.map { it.pageNumber },
                isCurrentPageBookmarked = pages.first().pageNumber in bookmarkedPageNumbers,
                title = pages.first().localizedSuraTitle(localizer),
                subtitle = pages.localizedPageSubtitle(localizer),
                firstVerse = pages.first().firstVerse,
                lastVerse = pages.last().lastVerse,
            )
        }

        pages.forEach { page ->
            ensureArabicPageLoaded(reading = currentReading, page = page)
        }

        if (_state.value.quranMode == QuranMode.TRANSLATION) {
            pages.forEach { page ->
                ensureTranslationPageLoaded(page)
            }
        }

        if (updateLastPage) {
            updateLastPage(pages)
        } else {
            configureLastPageUpdaterIfNeeded(pages.first())
        }
    }

    private fun updateBookmark(
        pageNumber: Int,
        toggle: Boolean,
    ) {
        viewModelScope.launch {
            val page = currentReading.pageOrNull(pageNumber)
            if (page == null) {
                _userMessage.value = "Unable to find page $pageNumber."
                return@launch
            }

            try {
                if (toggle && pageNumber in bookmarkedPageNumbers) {
                    pageBookmarkService.removePageBookmark(page)
                    _userMessage.value = "Removed bookmark for page $pageNumber."
                } else {
                    pageBookmarkService.insertPageBookmark(page)
                    _userMessage.value = "Saved bookmark for page $pageNumber."
                }
            } catch (error: Exception) {
                _userMessage.value = error.localizedMessage ?: "Unable to update bookmark."
            }
        }
    }

    private suspend fun configureLastPageUpdaterIfNeeded(initialPage: Page) {
        if (lastPageUpdaterConfigured) return
        lastPageUpdaterConfigured = true
        val existingLastPage = lastPageService.lastPages(initialPage.quran).first().firstOrNull()?.page
        lastPageUpdater.configure(initialPage = initialPage, lastPage = existingLastPage)
    }

    private suspend fun updateLastPage(pages: List<Page>) {
        configureLastPageUpdaterIfNeeded(pages.first())
        lastPageUpdater.updateTo(pages)
    }

    private fun ensureArabicPageLoaded(reading: Reading, page: Page) {
        val currentState = _pageContentStates.value[page.pageNumber]
        if (currentState is ContentState.Loaded || currentState is ContentState.Loading) return

        _pageContentStates.update { it + (page.pageNumber to ContentState.Loading) }
        viewModelScope.launch {
            val result = try {
                val content = loadImageState(reading, page)
                basePageContents[page.pageNumber] = content
                ContentState.Loaded(
                    content.state.withReadingHighlight(
                        highlightedAyahProgress,
                        content.wordFramesByAyah,
                        noteHighlightColors(),
                        highlightStyle,
                    )
                )
            } catch (error: Exception) {
                ContentState.Error(error)
            }
            _pageContentStates.update { it + (page.pageNumber to result) }
        }
    }

    private fun ensureTranslationPageLoaded(page: Page) {
        val currentState = _translationContentStates.value[page.pageNumber]
        if (currentState is ContentState.Loaded || currentState is ContentState.Loading) return

        _translationContentStates.update { it + (page.pageNumber to ContentState.Loading) }
        viewModelScope.launch {
            val result = try {
                val content = loadTranslationPageContent(page)
                basePageTranslationContents[page.pageNumber] = content
                val noteColors = translationNoteColors()
                ContentState.Loaded(content.copy(items = content.items.map { it.withHighlight(noteColors) }))
            } catch (error: Exception) {
                ContentState.Error(error)
            }
            _translationContentStates.update { it + (page.pageNumber to result) }
        }
    }

    private fun refreshArabicHighlights() {
        val noteColors = noteHighlightColors()
        _pageContentStates.update { current ->
            current.mapValues { (pageNumber, state) ->
                val baseContent = basePageContents[pageNumber] ?: return@mapValues state
                ContentState.Loaded(
                    baseContent.state.withReadingHighlight(
                        highlightedAyahProgress,
                        baseContent.wordFramesByAyah,
                        noteColors,
                        highlightStyle,
                    )
                )
            }
        }
    }

    private fun refreshTranslationHighlights() {
        val noteColors = translationNoteColors()
        _translationContentStates.update { current ->
            current.mapValues { (pageNumber, state) ->
                val base = basePageTranslationContents[pageNumber] ?: return@mapValues state
                ContentState.Loaded(
                    base.copy(items = base.items.map { it.withHighlight(noteColors) })
                )
            }
        }
    }

    private fun noteHighlightColors(): Map<AyahNumber, Color> =
        notesByVerse.mapValues { (_, note) -> note.color.toComposeColor().copy(alpha = NOTE_HIGHLIGHT_ALPHA) }

    // TranslationItem is keyed by plain ayah-within-sura Int (see
    // TranslationItemId), not the full AyahNumber, so colors must be
    // re-keyed to match.
    private fun translationNoteColors(): Map<Int, Color> =
        notesByVerse.entries.associate { (ayah, note) ->
            ayah.ayah to note.color.toComposeColor().copy(alpha = NOTE_HIGHLIGHT_ALPHA)
        }

    private suspend fun loadTranslationPageContent(page: Page): TranslationPageContent {
        if (!quranContentBootstrap.ready.value) {
            return TranslationPageContent(
                placeholderTitle = "Preparing translations",
                placeholderMessage = "Bundled translation files are still being installed.",
            )
        }

        val localTranslations = quranTextDataService.localTranslationRetriever.getLocalTranslations()
        val selectedTranslations = selectedTranslationsPreferences.selectedTranslations(localTranslations)

        val verses = page.firstVerse.arrayTo(page.lastVerse).toList()
        val verseTexts = quranTextDataService.textForVerses(verses, selectedTranslations)
        val items = buildList {
            add(
                TranslationItem.PageHeader(
                    page = page.pageNumber,
                    quarterName = page.localizedQuarterTitle(localizer),
                    suraNames = page.localizedSuraTitle(localizer),
                    decoratedSuraName = page.startSura.decoratedName(),
                )
            )
            verses.forEachIndexed { index, ayah ->
                val verseText = verseTexts[ayah] ?: return@forEachIndexed

                // The sura header carries the basmalah, so the verse text below must not
                // repeat it — use `arabicText`, never the prefixed `fullArabicText`.
                if (ayah == ayah.sura.firstVerse) {
                    add(
                        TranslationItem.SuraName(
                            sura = ayah.sura.suraNumber,
                            suraName = ayah.sura.localizedDisplayTitle(localizer, withNumber = false),
                            showBasmala = ayah.sura.startsWithBesmAllah,
                        )
                    )
                }
                add(
                    TranslationItem.ArabicText(
                        verse = ayah.ayah,
                        text = "${verseText.arabicText} ${NumberFormatters.arabic.format(ayah.ayah)}",
                        ayahLabel = localizer.lFormat(
                            "translation.text.ayah-number",
                            arguments = arrayOf(ayah.sura.suraNumber, ayah.ayah),
                        ),
                    )
                )
                selectedTranslations.forEachIndexed { translationIndex, translation ->
                    when (val translationText = verseText.translations[translationIndex]) {
                        is TranslationText.Reference -> {
                            add(
                                TranslationItem.TranslationReferenceVerse(
                                    verse = ayah.ayah,
                                    translationId = translation.id.toLong(),
                                    referenceVerse = translationText.ayah.ayah,
                                )
                            )
                        }
                        is TranslationText.StringText -> {
                            add(
                                TranslationItem.TranslationTextChunk(
                                    verse = ayah.ayah,
                                    translationId = translation.id.toLong(),
                                    chunkIndex = 0,
                                    text = translationText.value.text,
                                    quranRanges = translationText.value.quranRanges,
                                    footnoteRanges = translationText.value.footnoteRanges,
                                    footnotes = translationText.value.footnotes,
                                    readMoreAt = truncationOffset(translationText.value.text),
                                    readMoreLabel = localizer.l("translation.text.read-more"),
                                )
                            )
                        }
                    }
                    // Only worth naming the translator when several are shown side by side.
                    if (selectedTranslations.size > 1) {
                        add(
                            TranslationItem.TranslatorName(
                                verse = ayah.ayah,
                                translationId = translation.id.toLong(),
                                name = translation.translationName,
                            )
                        )
                    }
                }
                if (index < verses.lastIndex) {
                    add(TranslationItem.VerseSeparator(verse = ayah.ayah))
                }
            }
            add(TranslationItem.PageFooter(page.pageNumber))
        }

        return TranslationPageContent(items = items)
    }

    private suspend fun loadImageState(reading: Reading, page: Page): PageImageContent =
        withContext(Dispatchers.IO) {
            readingAssetsInstaller.ensureInstalled(reading)

            val resources = reading.imageResources
            val readingRoot = File(baseDir, "readings/${reading.localPath}")
            val databaseFile = File(readingRoot, resources.databasePath)
            val imagesDirectory = File(readingRoot, resources.imagesPath)

            if (!databaseFile.isFile || !imagesDirectory.isDirectory) {
                throw MissingReadingResourcesException(
                    reading = reading,
                    databaseFile = databaseFile,
                    imagesDirectory = imagesDirectory,
                )
            }

            val database = ReadOnlyDatabase.openFile(databaseFile)
            try {
                val persistence = SqliteWordFramePersistence(database)
                val imageService = ImageDataService(
                    persistence = persistence,
                    imagesDirectory = imagesDirectory,
                )
                val imagePage = imageService.imageForPage(page)
                val rawWordFrames = WordFrameCollection(persistence.wordFrameCollectionForPage(page))
                val wordFramesByAyah = page.firstVerse.arrayTo(page.lastVerse)
                    .associateWith { ayah -> rawWordFrames.wordFramesForVerse(ayah) }

                PageImageContent(
                    state = ContentImageState(
                        bitmap = imagePage.image,
                        decorations = ImageDecorations(
                            imageSize = Size(
                                width = imagePage.image.width.toFloat(),
                                height = imagePage.image.height.toFloat(),
                            ),
                        ),
                        quarterName = page.localizedQuarterTitle(localizer),
                        suraNames = page.localizedSuraTitle(localizer),
                        decoratedSuraName = page.startSura.decoratedName(),
                        pageNumber = page.pageNumber.toString(),
                        isLoading = false,
                        wordFramesByAyah = wordFramesByAyah,
                    ),
                    wordFramesByAyah = wordFramesByAyah,
                )
            } finally {
                database.close()
            }
        }
}

private data class PageImageContent(
    val state: ContentImageState,
    val wordFramesByAyah: Map<AyahNumber, List<com.quranengine.model.qurangeometry.WordFrame>>,
)

private class MissingReadingResourcesException(
    reading: Reading,
    databaseFile: File,
    imagesDirectory: File,
) : IllegalStateException(
    "Reading resources for ${reading.localPath} are unavailable. Expected ${databaseFile.absolutePath} and ${imagesDirectory.absolutePath}."
)

private fun Page.localizedSuraTitle(localizer: Localizer): String {
    val localized = startSura.localizedName(localizer, withNumber = true)
    return if (localized.contains("sura_names[")) {
        "${startSura.suraNumber}. ${startSura.englishName()}"
    } else {
        localized
    }
}

private fun Page.localizedPageTitle(localizer: Localizer): String {
    val localized = localizedName(localizer)
    return if (localized == "quran_page ${pageNumber}") {
        "Page $pageNumber"
    } else {
        localized
    }
}

private fun Page.localizedQuarterTitle(localizer: Localizer): String {
    val localized = localizedQuarterName(localizer)
    return if (localized.contains("juz2_description") || localized.contains("quran_")) {
        "Juz ${startJuz.juzNumber}"
    } else {
        localized
    }
}

private fun List<Page>.localizedPageSubtitle(localizer: Localizer): String {
    if (size <= 1) return first().localizedPageTitle(localizer)
    return "Pages ${first().pageNumber}-${last().pageNumber}"
}

private fun Reading.pageOrNull(pageNumber: Int): Page? =
    quran.pages.getOrNull(pageNumber - 1)

private fun normalizeVisiblePages(
    pageNumbers: List<Int>,
    twoPagesEnabled: Boolean,
    totalPages: Int,
): List<Int> {
    val normalized = pageNumbers
        .filter { it in 1..totalPages }
        .distinct()
        .sorted()
        .ifEmpty { listOf(1) }
    return if (twoPagesEnabled) normalized.take(2) else listOf(normalized.first())
}

/** Longest translation shown before it collapses behind "Read more", as in quran-ios. */
private const val MAX_TRANSLATION_CHUNK = 800

/** Cut on the last word boundary before the limit, or null when the text already fits. */
private fun truncationOffset(text: String): Int? {
    if (text.length <= MAX_TRANSLATION_CHUNK) return null
    val lastSpace = text.lastIndexOf(' ', MAX_TRANSLATION_CHUNK)
    return if (lastSpace > 0) lastSpace else MAX_TRANSLATION_CHUNK
}

private fun com.quranengine.model.qurankit.Sura.localizedDisplayTitle(
    localizer: Localizer,
    withNumber: Boolean = true,
): String {
    val localized = localizedName(localizer, withNumber = withNumber)
    return if (localized.contains("sura_names[")) {
        if (withNumber) "${suraNumber}. ${englishName()}" else englishName()
    } else {
        localized
    }
}

private fun ContentImageState.withReadingHighlight(
    progress: AyahPlaybackProgress?,
    wordFramesByAyah: Map<AyahNumber, List<com.quranengine.model.qurangeometry.WordFrame>>,
    noteColors: Map<AyahNumber, Color>,
    highlightStyle: ReadingHighlightStyle,
): ContentImageState {
    val readingHighlight = progress?.let { current ->
        val frames = wordFramesByAyah[current.ayah].orEmpty()
        when (highlightStyle) {
            // The whole currently-playing ayah, as a continuous bar per line.
            ReadingHighlightStyle.LINE -> frames.toLineHighlights(QuranColors.wordHighlight)
            // Just the single word the reciter is currently on.
            ReadingHighlightStyle.WORD -> frames.wordFrameForProgress(current.progress)
                ?.let { listOf(WordHighlight(rect = RectF(it.rect))) }
                .orEmpty()
        }
    }.orEmpty()

    val noteHighlights = noteColors.flatMap { (ayah, color) ->
        wordFramesByAyah[ayah].orEmpty().toLineHighlights(color)
    }

    return copy(
        // Note highlights first so the reading-progress highlight (the
        // brighter, transient one) draws on top where they might overlap.
        decorations = decorations.copy(wordHighlights = noteHighlights + readingHighlight),
    )
}

/**
 * Alpha applied to a persisted highlight when painted on the page — full
 * opacity (used for the menu's color swatches) would completely obscure the
 * Arabic text or translation underneath.
 */
private const val NOTE_HIGHLIGHT_ALPHA = 0.35f

/** Maps Note.Color to Compose colors, matching AyahMenuSheet's palette. */
private fun Note.Color.toComposeColor(): Color = when (this) {
    Note.Color.RED -> Color(0xFFFF6B8B)
    Note.Color.GREEN -> Color(0xFFC1EC71)
    Note.Color.BLUE -> Color(0xFFADD7FE)
    Note.Color.YELLOW -> Color(0xFFFDEC63)
    Note.Color.PURPLE -> Color(0xFFD8B1FE)
}

/** Applies a persisted highlight color to the item's associated verse, if any. */
private fun TranslationItem.withHighlight(colors: Map<Int, Color>): TranslationItem {
    val verse = when (this) {
        is TranslationItem.VerseSeparator -> verse
        is TranslationItem.ArabicText -> verse
        is TranslationItem.TranslatorName -> verse
        is TranslationItem.TranslationReferenceVerse -> verse
        is TranslationItem.TranslationTextChunk -> verse
        // A sura-name header's `id.ayah` resolves to the sura number, not a
        // verse, so it must never be looked up in a verse-keyed color map.
        is TranslationItem.PageHeader, is TranslationItem.PageFooter, is TranslationItem.SuraName -> null
    } ?: return this

    val color = colors[verse] ?: return if (highlightColor == null) this else clearHighlight()
    return when (this) {
        is TranslationItem.VerseSeparator -> copy(highlightColor = color)
        is TranslationItem.ArabicText -> copy(highlightColor = color)
        is TranslationItem.TranslatorName -> copy(highlightColor = color)
        is TranslationItem.TranslationReferenceVerse -> copy(highlightColor = color)
        is TranslationItem.TranslationTextChunk -> copy(highlightColor = color)
        else -> this
    }
}

private fun TranslationItem.clearHighlight(): TranslationItem = when (this) {
    is TranslationItem.VerseSeparator -> copy(highlightColor = null)
    is TranslationItem.ArabicText -> copy(highlightColor = null)
    is TranslationItem.TranslatorName -> copy(highlightColor = null)
    is TranslationItem.TranslationReferenceVerse -> copy(highlightColor = null)
    is TranslationItem.TranslationTextChunk -> copy(highlightColor = null)
    else -> this
}

private fun List<com.quranengine.model.qurangeometry.WordFrame>.wordFrameForProgress(
    progress: Float?,
): com.quranengine.model.qurangeometry.WordFrame? {
    if (isEmpty()) return null
    val clampedProgress = progress?.coerceIn(0f, 1f) ?: 0f
    val index = (clampedProgress * size).toInt().coerceIn(0, lastIndex)
    return sortedBy { it.word.wordNumber }[index]
}
