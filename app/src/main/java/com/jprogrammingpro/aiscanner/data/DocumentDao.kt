package com.jprogrammingpro.aiscanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Transaction
    @Query("SELECT * FROM documents ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<DocumentWithPages>>

    @Transaction
    @Query("SELECT * FROM documents WHERE id = :id")
    fun observe(id: Long): Flow<DocumentWithPages?>

    @Transaction
    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun get(id: Long): DocumentWithPages?

    @Insert
    suspend fun insertDocument(document: DocumentEntity): Long

    @Insert
    suspend fun insertPages(pages: List<PageEntity>)

    @Query("UPDATE documents SET title = :title, updated_at = :now WHERE id = :id")
    suspend fun rename(id: Long, title: String, now: Long)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun delete(id: Long)
}
