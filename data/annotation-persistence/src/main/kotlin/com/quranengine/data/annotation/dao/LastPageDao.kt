package com.quranengine.data.annotation.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.quranengine.data.annotation.entity.LastPageEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class LastPageDao {

    companion object {
        const val MAX_LAST_PAGES = 3
    }

    @Query("SELECT * FROM last_pages ORDER BY modified_on DESC")
    abstract fun observeLastPages(): Flow<List<LastPageEntity>>

    @Query("SELECT * FROM last_pages ORDER BY modified_on DESC")
    abstract suspend fun getAll(): List<LastPageEntity>

    @Insert
    abstract suspend fun insert(entity: LastPageEntity): Long

    @Query("UPDATE last_pages SET page = :newPage, modified_on = :modifiedOn WHERE page = :oldPage")
    abstract suspend fun updatePage(oldPage: Int, newPage: Int, modifiedOn: Long): Int

    @Query("DELETE FROM last_pages WHERE page = :page")
    abstract suspend fun deleteByPage(page: Int)

    @Query("SELECT COUNT(*) FROM last_pages")
    abstract suspend fun count(): Int

    @Query("DELETE FROM last_pages WHERE id IN (SELECT id FROM last_pages ORDER BY modified_on ASC LIMIT :excess)")
    abstract suspend fun deleteOldest(excess: Int)

    @Transaction
    open suspend fun insertWithOverflow(entity: LastPageEntity): Long {
        // Remove any existing entry for the same page to avoid duplicates
        deleteByPage(entity.page)
        val currentCount = count()
        if (currentCount >= MAX_LAST_PAGES) {
            val excess = currentCount - MAX_LAST_PAGES + 1
            deleteOldest(excess)
        }
        return insert(entity)
    }
}
