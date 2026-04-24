package com.quranengine.data.annotation.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.quranengine.data.annotation.entity.NoteEntity
import com.quranengine.data.annotation.entity.NoteVerseEntity
import com.quranengine.data.annotation.entity.NoteWithVerses
import kotlinx.coroutines.flow.Flow

@Dao
abstract class NoteDao {

    @Transaction
    @Query("SELECT * FROM notes ORDER BY modified_on DESC")
    abstract fun observeNotes(): Flow<List<NoteWithVerses>>

    @Transaction
    @Query("SELECT * FROM notes ORDER BY modified_on DESC")
    abstract suspend fun getAll(): List<NoteWithVerses>

    @Insert
    abstract suspend fun insertNote(note: NoteEntity): Long

    @Insert
    abstract suspend fun insertVerses(verses: List<NoteVerseEntity>)

    @Update
    abstract suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    abstract suspend fun deleteNote(noteId: Long)

    @Query("DELETE FROM note_verses WHERE note_id = :noteId")
    abstract suspend fun deleteVersesForNote(noteId: Long)

    @Transaction
    @Query(
        """
        SELECT * FROM notes WHERE id IN (
            SELECT note_id FROM note_verses WHERE sura = :sura AND ayah = :ayah
        )
        """
    )
    abstract suspend fun findNoteByVerse(sura: Int, ayah: Int): NoteWithVerses?

    @Transaction
    @Query(
        """
        SELECT DISTINCT n.* FROM notes n
        INNER JOIN note_verses nv ON n.id = nv.note_id
        WHERE (nv.sura || ':' || nv.ayah) IN (:verseKeys)
        """
    )
    abstract suspend fun findNotesByVerseKeys(verseKeys: List<String>): List<NoteWithVerses>

    /**
     * Upsert a note for the given verses. If an existing note is associated with any of the
     * provided verses, that note is updated. Otherwise a new note is created.
     *
     * Returns the resulting [NoteWithVerses].
     */
    @Transaction
    open suspend fun setNote(
        text: String?,
        verses: List<NoteVerseEntity>,
        color: Int,
        now: Long,
    ): NoteWithVerses {
        // Look for an existing note that shares any of the provided verses
        val existingNote = if (verses.isNotEmpty()) {
            findNoteByVerse(verses.first().sura, verses.first().ayah)
        } else {
            null
        }

        val noteId: Long
        if (existingNote != null) {
            noteId = existingNote.note.id
            updateNote(
                existingNote.note.copy(
                    note = text,
                    color = color,
                    modifiedOn = now,
                )
            )
            // Replace verses: remove old, insert new
            deleteVersesForNote(noteId)
        } else {
            noteId = insertNote(
                NoteEntity(
                    note = text,
                    color = color,
                    createdOn = now,
                    modifiedOn = now,
                )
            )
        }

        if (verses.isNotEmpty()) {
            insertVerses(verses.map { it.copy(noteId = noteId) })
        }

        return NoteWithVerses(
            note = NoteEntity(
                id = noteId,
                note = text,
                color = color,
                createdOn = existingNote?.note?.createdOn ?: now,
                modifiedOn = now,
            ),
            verses = verses.map { it.copy(noteId = noteId) },
        )
    }

    /**
     * Remove all notes associated with the given verses. Returns the deleted notes.
     */
    @Transaction
    open suspend fun removeNotesByVerses(verses: List<NoteVerseEntity>): List<NoteWithVerses> {
        if (verses.isEmpty()) return emptyList()

        val verseKeys = verses.map { "${it.sura}:${it.ayah}" }
        val notes = findNotesByVerseKeys(verseKeys)
        for (noteWithVerses in notes) {
            deleteNote(noteWithVerses.note.id)
        }
        return notes
    }
}
