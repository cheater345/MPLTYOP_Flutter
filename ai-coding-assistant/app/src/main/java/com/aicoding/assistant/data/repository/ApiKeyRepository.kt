package com.aicoding.assistant.data.repository

import com.aicoding.assistant.data.local.ApiKeyEntity
import com.aicoding.assistant.data.local.ApiKeyDao
import com.aicoding.assistant.data.security.CryptoManager
import com.aicoding.assistant.domain.model.ApiKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ApiKeyRepository(private val dao: ApiKeyDao) {

    suspend fun getForProvider(providerId: Long): List<ApiKey> = withContext(Dispatchers.IO) {
        dao.getForProvider(providerId).map { it.toDomain() }
    }

    suspend fun getEnabledForProvider(providerId: Long): List<ApiKey> = withContext(Dispatchers.IO) {
        dao.getEnabledForProvider(providerId).map { it.toDomain() }
    }

    suspend fun add(providerId: Long, value: String, priority: Int) = withContext(Dispatchers.IO) {
        dao.insert(
            ApiKeyEntity(
                providerId = providerId,
                valueEncrypted = CryptoManager.encrypt(value),
                priority = priority,
                enabled = true
            )
        )
    }

    suspend fun setEnabled(id: Long, enabled: Boolean) = withContext(Dispatchers.IO) {
        dao.setEnabled(id, enabled)
    }

    suspend fun setPriority(id: Long, priority: Int) = withContext(Dispatchers.IO) {
        val key = dao.getById(id) ?: return@withContext
        dao.update(key.copy(priority = priority))
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.delete(id)
    }

    suspend fun recordUsage(id: Long) = withContext(Dispatchers.IO) {
        val key = dao.getById(id) ?: return@withContext
        dao.update(
            key.copy(
                usageCount = key.usageCount + 1,
                lastUsedAt = System.currentTimeMillis(),
                consecutiveFailures = 0
            )
        )
    }

    suspend fun recordRotation(id: Long) = withContext(Dispatchers.IO) {
        val key = dao.getById(id) ?: return@withContext
        dao.update(
            key.copy(
                consecutiveFailures = key.consecutiveFailures + 1,
                lastUsedAt = System.currentTimeMillis()
            )
        )
    }

    private fun ApiKeyEntity.toDomain() = ApiKey(
        id = id,
        providerId = providerId,
        value = CryptoManager.tryDecrypt(valueEncrypted) ?: "",
        priority = priority,
        enabled = enabled,
        usageCount = usageCount,
        lastUsedAt = lastUsedAt,
        consecutiveFailures = consecutiveFailures,
    )
}