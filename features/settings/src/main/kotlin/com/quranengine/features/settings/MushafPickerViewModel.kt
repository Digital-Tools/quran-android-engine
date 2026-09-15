package com.quranengine.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.domain.readingservice.ReadingPreferences
import com.quranengine.domain.readingservice.ReadingResourcesService
import com.quranengine.model.qurankit.Reading
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MushafPickerItem(
    val reading: Reading,
    val isActive: Boolean,
    // Only ever populated for the active reading — ReadingResourcesService
    // only reports status for whichever reading is currently selected, and
    // with the single-active-mushaf model every other reading's files are
    // guaranteed absent, so there is nothing else to report.
    val status: ReadingResourcesService.ResourceStatus?,
)

data class MushafPickerUiState(
    val items: List<MushafPickerItem> = emptyList(),
)

@HiltViewModel
class MushafPickerViewModel @Inject constructor(
    private val readingPreferences: ReadingPreferences,
    readingResourcesService: ReadingResourcesService,
) : ViewModel() {

    val uiState: StateFlow<MushafPickerUiState> = combine(
        readingPreferences.readingFlow,
        readingResourcesService.status,
    ) { active, status ->
        MushafPickerUiState(
            items = Reading.sortedReadings.map { reading ->
                MushafPickerItem(
                    reading = reading,
                    isActive = reading == active,
                    status = status.takeIf { reading == active },
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MushafPickerUiState(
            items = Reading.sortedReadings.map { reading ->
                MushafPickerItem(reading, isActive = reading == readingPreferences.reading, status = null)
            },
        ),
    )

    /** Selecting a mushaf is the entire "download" action — ReadingResourcesService
     * reacts to this preference change by downloading it (if remote) and deleting
     * whatever was previously downloaded. */
    fun select(reading: Reading) {
        readingPreferences.reading = reading
    }
}
