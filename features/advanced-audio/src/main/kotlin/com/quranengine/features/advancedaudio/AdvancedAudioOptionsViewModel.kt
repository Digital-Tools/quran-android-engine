package com.quranengine.features.advancedaudio

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quranengine.core.audioplayer.RepetitionDelay
import com.quranengine.core.audioplayer.Runs
import com.quranengine.core.audioplayer.VerseDelay
import com.quranengine.core.localization.Localizer
import com.quranengine.domain.quranaudiokit.AudioPreferences
import com.quranengine.domain.reciterservice.ReciterDataRetriever
import com.quranengine.domain.reciterservice.ReciterPreferences
import com.quranengine.domain.reciterservice.localizedName
import com.quranengine.model.quranaudio.Reciter
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Quran
import com.quranengine.model.qurankit.Sura
import com.quranengine.model.qurankit.lastayahfinder.JuzBasedLastAyahFinder
import com.quranengine.model.qurankit.lastayahfinder.LastAyahFinder
import com.quranengine.model.qurankit.lastayahfinder.PageBasedLastAyahFinder
import com.quranengine.model.qurankit.lastayahfinder.QuranBasedLastAyahFinder
import com.quranengine.model.qurankit.lastayahfinder.SuraBasedLastAyahFinder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    /** The End at segment; picking a To ayah by hand makes it Custom. */
    val endAt = MutableStateFlow(deduceEndAt(initialStart, initialEnd))
    val verseRuns = MutableStateFlow(audioPreferences.verseRuns)
    val listRuns = MutableStateFlow(audioPreferences.listRuns)
    val verseDelay = MutableStateFlow(audioPreferences.verseDelay)
    val repetitionDelay = MutableStateFlow(audioPreferences.repetitionDelay)
    val playbackRate = MutableStateFlow(audioPreferences.playbackRate)

    val suras: List<Sura> = quran.suras

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

    /** From the wheel: any ayah of the Quran. */
    fun selectFrom(ayah: AyahNumber) = updateFromVerse(ayah)

    /** From the wheel: To can't go before From. */
    fun selectTo(ayah: AyahNumber) = updateToVerse(ayah)

    fun selectFromSura(suraNumber: Int) {
        val s = Sura(quran, suraNumber) ?: return
        updateFromVerse(fromVerse.value.selecting(s, minimum = null))
    }

    fun selectFromAyah(ayahNumber: Int) {
        val s = fromVerse.value.sura
        val newAyah = AyahNumber(s, ayahNumber) ?: return
        updateFromVerse(newAyah)
    }

    fun selectToSura(suraNumber: Int) {
        val s = Sura(quran, suraNumber) ?: return
        updateToVerse(toVerse.value.selecting(s, minimum = fromVerse.value))
    }

    fun selectToAyah(ayahNumber: Int) {
        val s = toVerse.value.sura
        val newAyah = AyahNumber(s, ayahNumber) ?: return
        updateToVerse(newAyah)
    }

    fun localizedName(reciter: Reciter): String =
        reciter.localizedName(localizer)

    fun setEndAt(choice: EndAtChoice) {
        endAt.value = choice
        applyEndAt()
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
        audioPreferences.verseRuns = runs
        verseRuns.value = runs
    }

    fun setListRuns(runs: Runs) {
        audioPreferences.listRuns = runs
        listRuns.value = runs
    }

    fun setVerseDelay(delay: VerseDelay) {
        audioPreferences.verseDelay = delay
        verseDelay.value = delay
    }

    fun setRepetitionDelay(delay: RepetitionDelay) {
        audioPreferences.repetitionDelay = delay
        repetitionDelay.value = delay
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
        if (endAt.value == EndAtChoice.CUSTOM) {
            if (toVerse.value < ayah) toVerse.value = ayah
        } else {
            applyEndAt()
        }
    }

    private fun updateToVerse(ayah: AyahNumber) {
        toVerse.value = if (ayah < fromVerse.value) fromVerse.value else ayah
        endAt.value = EndAtChoice.CUSTOM
    }

    private fun applyEndAt() {
        val finder = endAt.value.lastAyahFinder ?: return
        toVerse.value = finder.findLastAyah(fromVerse.value)
    }
}

/** The End at segments, in the order iOS shows them. */
enum class EndAtChoice(val label: String) {
    CUSTOM("Custom"),
    PAGE("Page"),
    SURA("Surah"),
    JUZ("Juz'"),
    QURAN("Quran"),
    ;

    val lastAyahFinder: LastAyahFinder?
        get() = when (this) {
            CUSTOM -> null
            PAGE -> PageBasedLastAyahFinder()
            SURA -> SuraBasedLastAyahFinder()
            JUZ -> JuzBasedLastAyahFinder()
            QURAN -> QuranBasedLastAyahFinder()
        }
}

/**
 * An end ayah can match several boundaries (the end of Al-Fatihah also ends
 * page 1); like iOS, prefer surah, then juz, page and Quran.
 */
internal fun deduceEndAt(start: AyahNumber, end: AyahNumber): EndAtChoice =
    listOf(EndAtChoice.SURA, EndAtChoice.JUZ, EndAtChoice.PAGE, EndAtChoice.QURAN)
        .firstOrNull { it.lastAyahFinder?.findLastAyah(start) == end }
        ?: EndAtChoice.CUSTOM

/**
 * Moving the wheel to [sura] keeps this ayah number where [sura] has it
 * (clamped to its last ayah, and not before [minimum]) instead of jumping
 * back to ayah 1. Same as AyahWheelPickerModel.selecting on iOS.
 */
internal fun AyahNumber.selecting(sura: Sura, minimum: AyahNumber?): AyahNumber {
    val first = if (minimum != null && minimum.sura == sura) minimum.ayah else 1
    val ayah = ayah.coerceIn(first, sura.lastVerse.ayah)
    return AyahNumber(sura, ayah)!!
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
