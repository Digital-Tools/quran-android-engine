package com.quranengine.domain.annotationservice

import com.quranengine.core.preferences.PreferenceKey
import com.quranengine.core.preferences.PreferenceTransformer
import com.quranengine.core.preferences.Preferences
import com.quranengine.core.preferences.TransformedPreference
import com.quranengine.data.annotation.persistence.NotePersistence
import com.quranengine.data.annotation.persistence.VersePersistenceModel
import com.quranengine.model.quranannotations.Note
import com.quranengine.model.qurankit.AyahNumber
import com.quranengine.model.qurankit.Quran
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteService(
    private val persistence: NotePersistence,
    private val analytics: AnalyticsLibrary,
    preferences: Preferences,
) {
    private val lastUsedHighlightColorDelegate = TransformedPreference(
        key = LAST_USED_NOTE_HIGHLIGHT_COLOR_KEY,
        preferences = preferences,
        transformer = PreferenceTransformer.enumTransformer(
            defaultValue = { DEFAULT_LAST_USED_NOTE_HIGHLIGHT_COLOR },
            valueOf = { raw -> Note.Color.entries.firstOrNull { it.value == raw } },
            toRaw = { it.value },
        ),
    )
    private var lastUsedHighlightColor: Note.Color by lastUsedHighlightColorDelegate

    fun color(from: List<Note>): Note.Color =
        from.maxByOrNull { it.modifiedDate }?.color ?: lastUsedHighlightColor

    suspend fun updateHighlight(verses: List<AyahNumber>, color: Note.Color, quran: Quran): Note {
        lastUsedHighlightColor = color
        analytics.logEvent("Highlight", verses.size.toString())
        val persistenceVerses = verses.map { it.toVersePersistenceModel() }
        val model = persistence.setNote(null, persistenceVerses, color.value)
        return model.toNote(quran)
    }

    suspend fun setNote(note: String, verses: Set<AyahNumber>, color: Note.Color) {
        lastUsedHighlightColor = color
        analytics.logEvent("UpdateNote", verses.size.toString())
        val persistenceVerses = verses.map { it.toVersePersistenceModel() }
        persistence.setNote(note, persistenceVerses, color.value)
    }

    suspend fun setNote(note: String, verses: Set<AyahNumber>) {
        setNote(note = note, verses = verses, color = lastUsedHighlightColor)
    }

    suspend fun removeNotes(verses: List<AyahNumber>) {
        analytics.logEvent("Unhighlight", verses.size.toString())
        val persistenceVerses = verses.map { it.toVersePersistenceModel() }
        persistence.removeNotes(persistenceVerses)
    }

    fun notes(quran: Quran): Flow<List<Note>> =
        persistence.notes().map { notes -> notes.map { it.toNote(quran) } }

    companion object {
        private val DEFAULT_LAST_USED_NOTE_HIGHLIGHT_COLOR = Note.Color.RED
        private val LAST_USED_NOTE_HIGHLIGHT_COLOR_KEY = PreferenceKey(
            "lastUsedNoteHighlightColor",
            DEFAULT_LAST_USED_NOTE_HIGHLIGHT_COLOR.value,
        )
    }
}

private fun AyahNumber.toVersePersistenceModel() =
    VersePersistenceModel(sura = sura.suraNumber, ayah = ayah)

private fun com.quranengine.data.annotation.persistence.NotePersistenceModel.toNote(quran: Quran): Note {
    return Note(
        verses = verses.mapNotNull { v ->
            AyahNumber(quran, v.sura, v.ayah)
        }.toSet(),
        modifiedDate = java.time.Instant.ofEpochMilli(modifiedDate),
        color = Note.Color.entries.firstOrNull { it.value == color } ?: Note.Color.RED,
        note = note,
    )
}
