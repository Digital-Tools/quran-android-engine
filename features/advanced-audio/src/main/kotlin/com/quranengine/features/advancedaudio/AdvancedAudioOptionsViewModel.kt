package com.quranengine.features.advancedaudio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.core.audioplayer.Runs
import com.quranengine.core.localization.Localizer
import com.quranengine.domain.quranaudiokit.AudioPreferences
import com.quranengine.domain.reciterservice.ReciterDataRetriever
import com.quranengine.domain.reciterservice.ReciterPreferences
import com.quranengine.domain.reciterservice.localizedName
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurankit.lastayahfinder.JuzBasedLastAyahFinder
import com.quranengine.model.qurankit.lastayahfinder.PageBasedLastAyahFinder
import com.quranengine.model.qurankit.lastayahfinder.QuranBasedLastAyahFinder
import com.quranengine.model.qurankit.lastayahfinder.SuraBasedLastAyahFinder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AdvancedAudioOptionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quran: Quran,
    private val audioPreferences: AudioPreferences,
    private val reciterDataRetriever: ReciterDataRetriever,
    private val reciterPreferences: ReciterPreferences,
    private val localizer: Localizer,
) : ViewModel() {
    private val defaultStart = quran.suras.first().firstVerse
    private val initialStart = savedStateHandle.ayahArgument(
        quran = quran,
        suraKey = "fromSura",
        ayahKey = "fromAyah",
    ) ?: defaultStart
    private val initialEnd = savedStateHandle.ayahArgument(
        quran = quran,
        suraKey = "toSura",
        ayahKey = "toAyah",
    )?.takeIf { it >= initialStart } ?: initialStart

    private val _reciter = MutableStateFlow<Reciter?>(null)
    val reciter: StateFlow<Reciter?> = _reciter.asStateFlow()

    val fromVerse = MutableStateFlow(initialStart)
    val toVerse = MutableStateFlow(initialEnd)
    val verseRuns = MutableStateFlow(Runs.ONE)
    val listRuns = MutableStateFlow(Runs.ONE)
    val playbackRate = MutableStateFlow(audioPreferences.playbackRate)

    private val _dismissed = MutableStateFlow(false)
    val dismissed: StateFlow<Boolean> = _dismissed.asStateFlow()

    private val _playRequest = MutableStateFlow<AdvancedAudioOptions?>(null)
    val playRequest: StateFlow<AdvancedAudioOptions?> = _playRequest.asStateFlow()

    init {
        refreshReciter()
    }

    fun refreshReciter() {
        viewModelScope.launch {
            val reciters = reciterDataRetriever.getReciters()
            _reciter.value = reciters.firstOrNull { it.id == reciterPreferences.lastSelectedReciterId }
                ?: reciters.firstOrNull()
        }
    }

    fun localizedName(reciter: Reciter): String =
        reciter.localizedName(localizer)

    fun setLastVerseToEndOfPage() {
        val finder = PageBasedLastAyahFinder()
        toVerse.update { finder.findLastAyah(fromVerse.value) }
    }

    fun setLastVerseToEndOfSura() {
        val finder = SuraBasedLastAyahFinder()
        toVerse.update { finder.findLastAyah(fromVerse.value) }
    }

    fun setLastVerseToEndOfJuz() {
        val finder = JuzBasedLastAyahFinder()
        toVerse.update { finder.findLastAyah(fromVerse.value) }
    }

    fun setLastVerseToEndOfQuran() {
        val finder = QuranBasedLastAyahFinder()
        toVerse.update { finder.findLastAyah(fromVerse.value) }
    }

    fun stepFromBackward() {
        fromVerse.value.previous?.let { updateFromVerse(it) }
    }

    fun stepFromForward() {
        fromVerse.value.next?.let { updateFromVerse(it) }
    }

    fun stepToBackward() {
        toVerse.value.previous?.let { updateToVerse(it) }
    }

    fun stepToForward() {
        toVerse.value.next?.let { updateToVerse(it) }
    }

    fun setVerseRuns(runs: Runs) {
        verseRuns.value = runs
    }

    fun setListRuns(runs: Runs) {
        listRuns.value = runs
    }

    fun setPlaybackRate(rate: Float) {
        val rounded = (rate * 4f).roundToInt() / 4f
        audioPreferences.playbackRate = rounded
        playbackRate.value = rounded
    }

    fun play() {
        val from = fromVerse.value
        val to = toVerse.value
        val validEnd = if (to < from) from else to
        val selectedReciter = reciter.value ?: return
        _playRequest.value = AdvancedAudioOptions(
            reciter = selectedReciter,
            start = from,
            end = validEnd,
            verseRuns = verseRuns.value,
            listRuns = listRuns.value,
            playbackRate = playbackRate.value,
        )
    }

    fun dismiss() {
        _dismissed.value = true
    }

    private fun updateFromVerse(ayah: AyahNumber) {
        fromVerse.value = ayah
        if (toVerse.value < ayah) {
            toVerse.value = ayah
        }
    }

    private fun updateToVerse(ayah: AyahNumber) {
        toVerse.value = if (ayah < fromVerse.value) fromVerse.value else ayah
    }
}

private fun SavedStateHandle.ayahArgument(
    quran: Quran,
    suraKey: String,
    ayahKey: String,
): AyahNumber? {
    val sura = get<Int>(suraKey) ?: return null
    val ayah = get<Int>(ayahKey) ?: return null
    return AyahNumber(quran, sura, ayah)
}
