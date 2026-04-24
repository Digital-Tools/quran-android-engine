package com.quranengine.data.annotation

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quranengine.data.annotation.dao.LastPageDao
import com.quranengine.data.annotation.dao.NoteDao
import com.quranengine.data.annotation.dao.PageBookmarkDao
import com.quranengine.data.annotation.entity.LastPageEntity
import com.quranengine.data.annotation.entity.NoteEntity
import com.quranengine.data.annotation.entity.NoteVerseEntity
import com.quranengine.data.annotation.entity.PageBookmarkEntity

@Database(
    entities = [
        LastPageEntity::class,
        PageBookmarkEntity::class,
        NoteEntity::class,
        NoteVerseEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class QuranAnnotationsDatabase : RoomDatabase() {
    abstract fun lastPageDao(): LastPageDao
    abstract fun pageBookmarkDao(): PageBookmarkDao
    abstract fun noteDao(): NoteDao
}
