package com.quranengine.domain.quranaudiokit

import com.quranengine.core.audioplayer.RepetitionDelay
import com.quranengine.core.audioplayer.Runs
import com.quranengine.core.audioplayer.VerseDelay
import com.quranengine.core.preferences.Preference
import com.quranengine.core.preferences.PreferenceKey
import com.quranengine.core.preferences.PreferenceTransformer
import com.quranengine.core.preferences.Preferences
import com.quranengine.core.preferences.TransformedPreference
import com.quranengine.model.quranaudio.AudioEnd

class AudioPreferences(preferences: Preferences) {

    var audioEnd: AudioEnd by TransformedPreference(
        AUDIO_END_KEY,
        preferences,
        PreferenceTransformer.enumTransformer(
            defaultValue = { AudioEnd.JUZ },
            valueOf = { raw -> AudioEnd.entries.firstOrNull { it.ordinal == raw } },
            toRaw = { it.ordinal },
        ),
    )

    var playbackRate: Float by Preference(PLAYBACK_RATE_KEY, preferences)

    /** "Play each verse" repeat count, kept across launches and used by every play. */
    var verseRuns: Runs by TransformedPreference(VERSE_RUNS_KEY, preferences, runsTransformer())

    /** "Play the range" repeat count, kept across launches and used by every play. */
    var listRuns: Runs by TransformedPreference(LIST_RUNS_KEY, preferences, runsTransformer())

    /** Pause after each verse, as a multiple of its length. Same key/raw values as iOS. */
    var verseDelay: VerseDelay by TransformedPreference(
        VERSE_DELAY_KEY,
        preferences,
        PreferenceTransformer(
            rawToValue = { VerseDelay.fromId(it) ?: VerseDelay.NONE },
            valueToRaw = { it.id },
        ),
    )

    /** Pause before the set of verses repeats. Same key/raw values as iOS. */
    var repetitionDelay: RepetitionDelay by TransformedPreference(
        REPETITION_DELAY_KEY,
        preferences,
        PreferenceTransformer(
            rawToValue = { RepetitionDelay.fromId(it) ?: RepetitionDelay.ONE_SECOND },
            valueToRaw = { it.id },
        ),
    )

    companion object {
        private val AUDIO_END_KEY = PreferenceKey("audioEndKey", AudioEnd.JUZ.ordinal)
        private val PLAYBACK_RATE_KEY = PreferenceKey("audioPlaybackRate", 1.0f)
        private val VERSE_RUNS_KEY = PreferenceKey("audioVerseRuns", Runs.ONE.maxRuns)
        private val LIST_RUNS_KEY = PreferenceKey("audioListRuns", Runs.ONE.maxRuns)
        private val VERSE_DELAY_KEY = PreferenceKey("audioVerseDelay", VerseDelay.NONE.id)
        private val REPETITION_DELAY_KEY =
            PreferenceKey("audioRepetitionDelay", RepetitionDelay.ONE_SECOND.id)

        // Stored as the repeat count (matches iOS), so custom counts round-trip.
        private fun runsTransformer() = PreferenceTransformer<Int, Runs>(
            rawToValue = Runs::of,
            valueToRaw = { it.maxRuns },
        )
    }
}
