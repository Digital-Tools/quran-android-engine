package com.quranengine.domain.quranaudiokit

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

    companion object {
        private val AUDIO_END_KEY = PreferenceKey("audioEndKey", AudioEnd.JUZ.ordinal)
        private val PLAYBACK_RATE_KEY = PreferenceKey("audioPlaybackRate", 1.0f)
    }
}
