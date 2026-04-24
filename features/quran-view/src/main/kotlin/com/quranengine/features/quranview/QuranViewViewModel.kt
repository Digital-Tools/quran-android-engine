package com.quranengine.features.quranview

import android.graphics.RectF
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.core.localization.Localizer
import com.quranengine.data.sqlite.ReadOnlyDatabase
import com.quranengine.data.wordframe.SqliteWordFramePersistence
import com.quranengine.domain.annotationservice.LastPageService
import com.quranengine.domain.annotationservice.LastPageUpdater
import com.quranengine.domain.annotationservice.PageBookmarkService
import com.quranengine.domain.imageservice.ImageDataService
import com.quranengine.domain.qurantextkit.englishName
import com.quranengine.domain.qurantextkit.QuranContentStatePreferences
import com.quranengine.domain.qurantextkit.QuranTextDataService
import com.quranengine.domain.qurantextkit.localizedName
import com.quranengine.domain.qurantextkit.localizedQuarterName
import com.quranengine.domain.readingservice.ReadingAssetsInstaller
import com.quranengine.domain.readingservice.ReadingPreferences
import com.quranengine.domain.readingservice.imageResources
import com.quranengine.domain.readingservice.localPath
import com.quranengine.features.qurancontent.ContentState
import com.quranengine.features.quranimage.ContentImageState
import com.quranengine.features.qurantranslation.TranslationItem
import com.quranengine.model.qurangeometry.WordFrameCollection
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Page
import com.quranengine.model.qurankit.Reading
import com.quranengine.model.qurankit.arrayTo
import com.quranengine.model.qurantext.QuranMode
import com.quranengine.model.qurantext.TranslationText
import com.quranengine.domain.translationservice.SelectedTranslationsPreferences
import com.quranengine.ui.quran.ImageDecorations
import com.quranengine.ui.quran.WordHighlight
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
    private val selectedTranslationsPreferences: SelectedTranslationsPreferences,
    private val pageBookmarkService: PageBookmarkService,
    private val lastPageService: LastPageService,
    private val lastPageUpdater: LastPageUpdater,
    private val readingAssetsInstaller: ReadingAssetsInstaller,
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
    private var bookmarkedPageNumbers: Set<Int> = emptySet()
    private var highlightedAyah: AyahNumber? = null
    private var bookmarkObservationJob: Job? = null
    private var lastPageUpdaterConfigured = false

    init {
        observeReaderPreferences()
        observeReading()
        observeSelectedTranslations()
    }

    fun toggleBars() {
        _state.update { current ->
            current.copy(barsVisible = !current.barsVisible)
        }
    }

    fun setReadingAyah(ayah: AyahNumber?) {
        highlightedAyah = ayah
        refreshArabicHighlights()
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
                    content.state.withReadingHighlight(highlightedAyah, content.verseHighlightRectsByAyah)
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
                ContentState.Loaded(loadTranslationPageContent(page))
            } catch (error: Exception) {
                ContentState.Error(error)
            }
            _translationContentStates.update { it + (page.pageNumber to result) }
        }
    }

    private fun refreshArabicHighlights() {
        _pageContentStates.update { current ->
            current.mapValues { (pageNumber, state) ->
                val baseContent = basePageContents[pageNumber] ?: return@mapValues state
                ContentState.Loaded(
                    baseContent.state.withReadingHighlight(highlightedAyah, baseContent.verseHighlightRectsByAyah)
                )
            }
        }
    }

    private suspend fun loadTranslationPageContent(page: Page): TranslationPageContent {
        val localTranslations = quranTextDataService.localTranslationRetriever.getLocalTranslations()
        val selectedTranslations = selectedTranslationsPreferences.selectedTranslations(localTranslations)
        if (selectedTranslations.isEmpty()) {
            return TranslationPageContent(
                placeholderTitle = "No translations selected",
                placeholderMessage = "Download and select at least one translation from the Translations screen.",
            )
        }

        val verses = page.firstVerse.arrayTo(page.lastVerse).toList()
        val verseTexts = quranTextDataService.textForVerses(verses, selectedTranslations)
        val items = buildList {
            add(TranslationItem.PageHeader(page.pageNumber))
            verses.forEachIndexed { index, ayah ->
                val verseText = verseTexts[ayah] ?: return@forEachIndexed

                if (ayah == ayah.sura.firstVerse) {
                    add(
                        TranslationItem.SuraName(
                            sura = ayah.sura.suraNumber,
                            suraName = ayah.sura.localizedDisplayTitle(localizer),
                        )
                    )
                }
                if (index > 0) {
                    add(TranslationItem.VerseSeparator(verse = ayah.ayah))
                }
                add(
                    TranslationItem.ArabicText(
                        verse = ayah.ayah,
                        text = verseText.fullArabicText(),
                    )
                )
                selectedTranslations.forEachIndexed { translationIndex, translation ->
                    add(
                        TranslationItem.TranslatorName(
                            verse = ayah.ayah,
                            translationId = translation.id.toLong(),
                            name = translation.translationName,
                        )
                    )
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
                                )
                            )
                        }
                    }
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
                val verseHighlightRectsByAyah = page.firstVerse.arrayTo(page.lastVerse)
                    .mapNotNull { ayah ->
                        rawWordFrames.wordFramesForVerse(ayah)
                            .map { RectF(it.rect) }
                            .takeIf { it.isNotEmpty() }
                            ?.let { ayah to it }
                    }
                    .toMap()

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
                        pageNumber = page.pageNumber.toString(),
                        isLoading = false,
                    ),
                    verseHighlightRectsByAyah = verseHighlightRectsByAyah,
                )
            } finally {
                database.close()
            }
        }
}

private data class PageImageContent(
    val state: ContentImageState,
    val verseHighlightRectsByAyah: Map<AyahNumber, List<RectF>>,
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

private fun com.quranengine.model.qurankit.Sura.localizedDisplayTitle(localizer: Localizer): String {
    val localized = localizedName(localizer, withNumber = true)
    return if (localized.contains("sura_names[")) {
        "${suraNumber}. ${englishName()}"
    } else {
        localized
    }
}

private fun com.quranengine.model.qurantext.VerseText.fullArabicText(): String =
    buildList {
        addAll(arabicPrefix)
        add(arabicText)
        addAll(arabicSuffix)
    }.filter { it.isNotBlank() }
        .joinToString(separator = " ")

private fun ContentImageState.withReadingHighlight(
    ayah: AyahNumber?,
    verseHighlightRectsByAyah: Map<AyahNumber, List<RectF>>,
): ContentImageState {
    val wordHighlights = ayah?.let { currentAyah ->
        verseHighlightRectsByAyah[currentAyah]?.map { bounds ->
            WordHighlight(rect = RectF(bounds))
        }
    }.orEmpty()

    return copy(
        decorations = decorations.copy(wordHighlights = wordHighlights),
    )
}
