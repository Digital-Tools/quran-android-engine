package com.quranengine.features.reciterlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.core.localization.Localizer
import com.quranengine.domain.reciterservice.DownloadedRecitersService
import com.quranengine.domain.reciterservice.RecentRecitersService
import com.quranengine.domain.reciterservice.ReciterDataRetriever
import com.quranengine.domain.reciterservice.ReciterPreferences
import com.quranengine.domain.reciterservice.localizedName
import com.quranengine.model.quranaudio.Reciter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReciterListState(
    val recent: List<Reciter> = emptyList(),
    val downloaded: List<Reciter> = emptyList(),
    val english: List<Reciter> = emptyList(),
    val arabic: List<Reciter> = emptyList(),
    val selectedReciter: Reciter? = null,
)

@HiltViewModel
class ReciterListViewModel @Inject constructor(
    private val reciterDataRetriever: ReciterDataRetriever,
    private val recentRecitersService: RecentRecitersService,
    private val downloadedRecitersService: DownloadedRecitersService,
    private val reciterPreferences: ReciterPreferences,
    private val localizer: Localizer,
) : ViewModel() {

    private val _state = MutableStateFlow(ReciterListState())
    val state: StateFlow<ReciterListState> = _state.asStateFlow()
    private var allReciters: List<Reciter> = emptyList()

    init {
        loadReciters()
    }

    private fun loadReciters() {
        viewModelScope.launch {
            allReciters = reciterDataRetriever.getReciters()
            val recent = recentRecitersService.recentReciters(allReciters)
            val downloaded = downloadedRecitersService.downloadedReciters(allReciters)
            val english = allReciters.filter { it.category != Reciter.Category.ARABIC }
            val arabic = allReciters.filter { it.category == Reciter.Category.ARABIC }
            val selectedReciter = allReciters.firstOrNull { it.id == reciterPreferences.lastSelectedReciterId }

            _state.value = _state.value.copy(
                recent = recent,
                downloaded = downloaded,
                english = english,
                arabic = arabic,
                selectedReciter = selectedReciter,
            )
        }
    }

    fun selectReciter(reciter: Reciter) {
        reciterPreferences.lastSelectedReciterId = reciter.id
        recentRecitersService.updateRecentRecitersList(reciter)
        _state.value = _state.value.copy(
            recent = recentRecitersService.recentReciters(allReciters),
            selectedReciter = reciter,
        )
    }

    fun localizedName(reciter: Reciter): String =
        reciter.localizedName(localizer)
}
