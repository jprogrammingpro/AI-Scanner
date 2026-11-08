package com.jprogrammingpro.aiscanner.data

import kotlinx.coroutines.flow.Flow

class DocumentRepository(
    private val dao: DocumentDao,
    private val storage: PageStorage,
) {
    fun observeAll(): Flow<List<DocumentWithPages>> = dao.observeAll()

    fun observe(id: Long): Flow<DocumentWithPages?> = dao.observe(id)

    suspend fun create(title: String, mode: String, imagePaths: List<String>): Long {
        val now = System.currentTimeMillis()
        val id = dao.insertDocument(
            DocumentEntity(title = title, mode = mode, createdAt = now, updatedAt = now),
        )
        dao.insertPages(
            imagePaths.mapIndexed { index, path ->
                PageEntity(documentId = id, position = index, imagePath = path)
            },
        )
        return id
    }

    suspend fun rename(id: Long, title: String) = dao.rename(id, title, System.currentTimeMillis())

    suspend fun delete(id: Long) {
        val doc = dao.get(id) ?: return
        dao.delete(id)
        storage.delete(doc.pages.map { it.imagePath })
    }
}
