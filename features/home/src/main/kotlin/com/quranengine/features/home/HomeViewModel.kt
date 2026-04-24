package com.quranengine.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.core.localization.Localizer
import com.quranengine.domain.annotationservice.LastPageService
import com.quranengine.domain.qurantextkit.localizedName
import com.quranengine.model.quranannotations.LastPage
import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurankit.Sura
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val quran: Quran,
    private val lastPageService: LastPageService,
    private val localizer: Localizer,
) : ViewModel() {

    private val _viewType = MutableStateFlow(HomeViewType.SURAS)
    val viewType: StateFlow<HomeViewType> = _viewType.asStateFlow()

    private val _sortOrder = MutableStateFlow(SurahSortOrder.ASCENDING)
    val sortOrder: StateFlow<SurahSortOrder> = _sortOrder.asStateFlow()

    val lastPages: StateFlow<List<LastPage>> = lastPageService.lastPages(quran)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suras: List<Sura> get() = quran.suras

    val quarters: List<QuarterItem>
        get() = quran.quarters.map { quarter ->
            QuarterItem(
                quarter = quarter,
                juz = quarter.juz,
                localizedName = quarter.localizedName(localizer),
                localizedJuzName = quarter.juz.localizedName(localizer),
                pageDescription = quarter.page.localizedName(localizer),
            )
        }

    fun setViewType(type: HomeViewType) {
        _viewType.value = type
    }

    fun toggleSortOrder() {
        _sortOrder.value = when (_sortOrder.value) {
            SurahSortOrder.ASCENDING -> SurahSortOrder.DESCENDING
            SurahSortOrder.DESCENDING -> SurahSortOrder.ASCENDING
        }
    }
}
