package com.quranengine.data.annotation.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "color") val color: Int,
    @ColumnInfo(name = "created_on") val createdOn: Long,
    @ColumnInfo(name = "modified_on") val modifiedOn: Long,
)
