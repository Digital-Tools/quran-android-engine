package com.quranengine.data.annotation.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quranengine.data.annotation.entity.PageBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PageBookmarkDao {

    @Query("SELECT * FROM page_bookmarks ORDER BY created_on DESC")
    fun observeBookmarks(): Flow<List<PageBookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PageBookmarkEntity): Long

    @Query("DELETE FROM page_bookmarks WHERE page = :page")
    suspend fun deleteByPage(page: Int)

    @Query("DELETE FROM page_bookmarks")
    suspend fun deleteAll()
}
