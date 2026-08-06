package com.aicoding.assistant.data.repository

import com.aicoding.assistant.data.local.PromptDao
import com.aicoding.assistant.data.local.PromptEntity
import com.aicoding.assistant.domain.model.Prompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PromptRepository(private val dao: PromptDao) {

    fun observeAll(): Flow<List<Prompt>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun search(q: String): Flow<List<Prompt>> =
        if (q.isBlank()) observeAll() else dao.search(q).map { list -> list.map { it.toDomain() } }

    suspend fun categories(): List<String> = withContext(Dispatchers.IO) { dao.categories() }

    suspend fun getById(id: Long): Prompt? = withContext(Dispatchers.IO) {
        dao.getById(id)?.toDomain()
    }

    suspend fun upsert(prompt: Prompt) = withContext(Dispatchers.IO) {
        val entity = PromptEntity(
            id = prompt.id, title = prompt.title, content = prompt.content,
            category = prompt.category, favorite = prompt.favorite, createdAt = prompt.createdAt,
        )
        if (entity.id == 0L) dao.insert(entity) else dao.update(entity)
    }

    suspend fun toggleFavorite(id: Long, fav: Boolean) = withContext(Dispatchers.IO) {
        dao.setFavorite(id, fav)
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.delete(id)
    }

    private fun PromptEntity.toDomain(): Prompt = Prompt(id, title, content, category, favorite, createdAt)
}