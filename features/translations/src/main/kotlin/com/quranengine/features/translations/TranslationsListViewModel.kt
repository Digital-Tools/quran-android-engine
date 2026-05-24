package com.quranengine.features.translations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.data.batchdownloader.DownloadBatchResponse
import com.quranengine.data.batchdownloader.DownloadsObserver
import com.quranengine.domain.translationservice.LocalTranslationsRetriever
import com.quranengine.domain.translationservice.SelectedTranslationsPreferences
import com.quranengine.domain.translationservice.TranslationDeleter
import com.quranengine.domain.translationservice.TranslationsDownloader
import com.quranengine.domain.translationservice.TranslationsRepository
import com.quranengine.domain.translationservice.firstMatches
import com.quranengine.model.qurantext.Translation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

data class TranslationsListUiState(
    val selectedItems: List<TranslationItemState> = emptyList(),
    val downloadedItems: List<TranslationItemState> = emptyList(),
    val availableByLanguage: Map<String, List<TranslationItemState>> = emptyMap(),
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class TranslationsListViewModel @Inject constructor(
    private val translationsRepository: TranslationsRepository,
    private val localTranslationsRetriever: LocalTranslationsRetriever,
    private val selectedTranslationsPreferences: SelectedTranslationsPreferences,
    private val translationsDownloader: TranslationsDownloader,
    private val translationDeleter: TranslationDeleter,
) : ViewModel() {

    private val downloadsObserver = DownloadsObserver<Int>(
        extractKey = { batch -> allTranslations.value.firstMatches(batch)?.id },
        showError = { Timber.e(it, "Download error") },
    )

    private val allTranslations = MutableStateFlow<List<Translation>>(emptyList())
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<TranslationsListUiState> = combine(
        allTranslations,
        selectedTranslationsPreferences.selectedTranslationIdsFlow,
        downloadsObserver.progressFlow,
        _isRefreshing,
    ) { translations, selectedIds, progressMap, refreshing ->
        buildUiState(translations, selectedIds, progressMap, refreshing)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TranslationsListUiState())

    init {
        loadTranslations()
        observeRunningDownloads()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                translationsRepository.downloadAndSyncTranslations()
                reloadLocal()
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh translations")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun download(translation: Translation) {
        viewModelScope.launch {
            try {
                val response = translationsDownloader.download(translation)
                downloadsObserver.observe(setOf(response))
                response.awaitCompletion()
                val error = response.getError()
                if (error != null) {
                    Timber.e(error, "Download failed for ${translation.displayName}")
                } else {
                    selectedTranslationsPreferences.select(translation.id)
                }
                reloadLocal()
            } catch (e: Exception) {
                Timber.e(e, "Download failed for ${translation.displayName}")
            }
        }
    }

    fun delete(translation: Translation) {
        viewModelScope.launch {
            try {
                translationDeleter.deleteTranslation(translation)
                reloadLocal()
            } catch (e: Exception) {
                Timber.e(e, "Delete failed for ${translation.displayName}")
            }
        }
    }

    fun select(translation: Translation) {
        selectedTranslationsPreferences.select(translation.id)
    }

    fun deselect(translation: Translation) {
        selectedTranslationsPreferences.deselect(translation.id)
    }

    fun moveSelectedTranslation(fromIndex: Int, toIndex: Int) {
        val currentIds = selectedTranslationsPreferences.selectedTranslationIds.toMutableList()
        if (fromIndex !in currentIds.indices || toIndex !in currentIds.indices) return
        val item = currentIds.removeAt(fromIndex)
        currentIds.add(toIndex, item)
        selectedTranslationsPreferences.selectedTranslationIds = currentIds
    }

    private fun loadTranslations() {
        viewModelScope.launch {
            try {
                reloadLocal()
            } catch (e: Exception) {
                Timber.e(e, "Failed to load translations")
            }
        }
    }

    private fun observeRunningDownloads() {
        viewModelScope.launch {
            try {
                val running = translationsDownloader.runningTranslationDownloads()
                if (running.isNotEmpty()) {
                    downloadsObserver.observe(running.toSet())
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to observe running downloads")
            }
        }
    }

    private suspend fun reloadLocal() {
        val translations = localTranslationsRetriever.getLocalTranslations()
        allTranslations.value = translations
    }

    private fun buildUiState(
        translations: List<Translation>,
        selectedIds: List<Int>,
        progressMap: Map<Int, Double>,
        refreshing: Boolean,
    ): TranslationsListUiState {
        val selectedIdSet = selectedIds.toSet()

        fun toItemState(translation: Translation): TranslationItemState {
            val progress = progressMap[translation.id]
            val downloadProgress = when {
                progress != null -> TranslationItemState.DownloadProgress.Downloading(progress.toFloat())
                translation.isDownloaded && translation.needsUpgrade -> TranslationItemState.DownloadProgress.NeedsUpgrade
                translation.isDownloaded -> TranslationItemState.DownloadProgress.Downloaded
                else -> TranslationItemState.DownloadProgress.NotDownloaded
            }
            return TranslationItemState(translation, downloadProgress)
        }

        // Selected translations in preference order (downloaded only)
        val byId = translations.associateBy { it.id }
        val selectedItems = selectedIds.mapNotNull { id ->
            byId[id]?.takeIf { it.isDownloaded }?.let { toItemState(it) }
        }

        // Downloaded but not selected
        val downloadedItems = translations
            .filter { it.isDownloaded && it.id !in selectedIdSet }
            .sorted()
            .map { toItemState(it) }

        // Available (not downloaded), grouped by language code
        val availableByLanguage = translations
            .filter { !it.isDownloaded }
            .sorted()
            .map { toItemState(it) }
            .groupBy { it.translation.languageCode }
            .toSortedMap()

        return TranslationsListUiState(
            selectedItems = selectedItems,
            downloadedItems = downloadedItems,
            availableByLanguage = availableByLanguage,
            isRefreshing = refreshing,
        )
    }
}
