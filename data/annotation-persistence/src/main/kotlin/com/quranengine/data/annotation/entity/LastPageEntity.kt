package com.quranengine.data.annotation.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "last_pages")
data class LastPageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "page") val page: Int,
    @ColumnInfo(name = "created_on") val createdOn: Long,
    @ColumnInfo(name = "modified_on") val modifiedOn: Long,
)
