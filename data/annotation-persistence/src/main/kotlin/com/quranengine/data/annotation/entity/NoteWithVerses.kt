package com.quranengine.data.annotation.entity

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithVerses(
    @Embedded val note: NoteEntity,
    @Relation(parentColumn = "id", entityColumn = "note_id")
    val verses: List<NoteVerseEntity>,
)
