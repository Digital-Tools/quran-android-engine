package com.quranengine.data.annotation.persistence

import com.quranengine.data.annotation.dao.NoteDao
import com.quranengine.data.annotation.entity.NoteVerseEntity
import com.quranengine.data.annotation.entity.NoteWithVerses
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNotePersistence(
    private val dao: NoteDao,
) : NotePersistence {

    override fun notes(): Flow<List<NotePersistenceModel>> =
        dao.observeNotes().map { list ->
            list.map { it.toModel() }
        }

    override suspend fun setNote(
        note: String?,
        verses: List<VersePersistenceModel>,
        color: Int,
    ): NotePersistenceModel {
        val verseEntities = verses.map {
            NoteVerseEntity(noteId = 0, sura = it.sura, ayah = it.ayah)
        }
        val now = System.currentTimeMillis()
        val result = dao.setNote(text = note, verses = verseEntities, color = color, now = now)
        return result.toModel()
    }

    override suspend fun removeNotes(
        with: List<VersePersistenceModel>,
    ): List<NotePersistenceModel> {
        val verseEntities = with.map {
            NoteVerseEntity(noteId = 0, sura = it.sura, ayah = it.ayah)
        }
        return dao.removeNotesByVerses(verseEntities).map { it.toModel() }
    }

    private fun NoteWithVerses.toModel() = NotePersistenceModel(
        verses = verses.map { VersePersistenceModel(sura = it.sura, ayah = it.ayah) }.toSet(),
        modifiedDate = note.modifiedOn,
        note = note.note,
        color = note.color,
    )
}
