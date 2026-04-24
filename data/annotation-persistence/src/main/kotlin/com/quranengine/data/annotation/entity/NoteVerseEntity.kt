package com.quranengine.data.annotation.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "note_verses",
    primaryKeys = ["note_id", "sura", "ayah"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["note_id"])],
)
data class NoteVerseEntity(
    @ColumnInfo(name = "note_id") val noteId: Long,
    @ColumnInfo(name = "sura") val sura: Int,
    @ColumnInfo(name = "ayah") val ayah: Int,
)
