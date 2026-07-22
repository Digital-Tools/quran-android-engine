package com.quranengine.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.domain.annotationservice.LastPageService
import com.quranengine.domain.qurantextkit.FontSizePreferences
import com.quranengine.domain.readingservice.ReadingPreferences
import com.quranengine.features.home.HomeViewType
import com.quranengine.features.home.SurahSortOrder
import com.quranengine.model.quranannotations.LastPage
import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurankit.Quarter
import com.quranengine.model.qurankit.Sura
import com.quranengine.ui.theme.AppearanceMode
import com.quranengine.ui.theme.ThemeStyle
import com.quranengine.ui.theme.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(
    private val quran: Quran,
    private val lastPageService: LastPageService,
    private val readingPreferences: ReadingPreferences,
    private val fontSizePreferences: FontSizePreferences,
    private val themePreferences: ThemePreferences,
) : ViewModel() {

    val lastPages: StateFlow<List<LastPage>> = lastPageService.lastPages(quran)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _homeViewType = MutableStateFlow(HomeViewType.SURAS)
    val homeViewType: StateFlow<HomeViewType> = _homeViewType.asStateFlow()

    private val _homeSortOrder = MutableStateFlow(SurahSortOrder.ASCENDING)
    val homeSortOrder: StateFlow<SurahSortOrder> = _homeSortOrder.asStateFlow()

    val themeStyle: StateFlow<ThemeStyle> = themePreferences.themeStyleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), themePreferences.themeStyle)

    val appearanceMode: StateFlow<AppearanceMode> = themePreferences.appearanceModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), themePreferences.appearanceMode)

    val suras: List<Sura> get() = quran.suras

    val quarters: List<Quarter> get() = quran.quarters

    fun setHomeViewType(type: HomeViewType) {
        _homeViewType.value = type
    }

    fun toggleSortOrder() {
        _homeSortOrder.value = when (_homeSortOrder.value) {
            SurahSortOrder.ASCENDING -> SurahSortOrder.DESCENDING
            SurahSortOrder.DESCENDING -> SurahSortOrder.ASCENDING
        }
    }
}
