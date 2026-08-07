package com.aicoding.assistant.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY pinned DESC, updatedAt DESC")
    fun observeAll(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ChatEntity?

    @Query("SELECT * FROM chats WHERE title LIKE '%' || :q || '%' ESCAPE '\\' ORDER BY updatedAt DESC")
    fun search(q: String): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE pinned = 1 ORDER BY updatedAt DESC")
    fun observePinned(): Flow<List<ChatEntity>>

    @Insert
    suspend fun insert(chat: ChatEntity): Long

    @Update
    suspend fun update(chat: ChatEntity)

    @Query("UPDATE chats SET title = :title WHERE id = :id")
    suspend fun rename(id: Long, title: String)

    @Query("UPDATE chats SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("UPDATE chats SET updatedAt = :at WHERE id = :id")
    suspend fun touch(id: Long, at: Long)

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM chats")
    suspend fun count(): Int
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun observeForChat(chatId: Long): Flow<List<MessageEntity>>

    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId")
    suspend fun countForChat(chatId: Long): Int

    @Insert
    suspend fun insert(message: MessageEntity): Long

    @Update
    suspend fun update(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MessageEntity?

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteForChat(chatId: Long)

    @Query("UPDATE messages SET content = :content, error = NULL WHERE id = :id")
    suspend fun updateContent(id: Long, content: String)

    @Query("UPDATE messages SET error = :error WHERE id = :id")
    suspend fun updateError(id: Long, error: String)
}

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY favorite DESC, createdAt DESC")
    fun observeAll(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE title LIKE '%' || :q || '%' OR content LIKE '%' || :q || '%' ORDER BY favorite DESC, createdAt DESC")
    fun search(q: String): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE favorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<PromptEntity>>

    @Query("SELECT DISTINCT category FROM prompts WHERE category != ''")
    suspend fun categories(): List<String>

    @Query("SELECT * FROM prompts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PromptEntity?

    @Insert
    suspend fun insert(prompt: PromptEntity): Long

    @Update
    suspend fun update(prompt: PromptEntity)

    @Query("UPDATE prompts SET favorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean)

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY id ASC")
    fun observeAll(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE enabled = 1")
    suspend fun getAllEnabled(): List<ProviderEntity>

    @Query("SELECT * FROM providers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProviderEntity?

    @Insert
    suspend fun insert(provider: ProviderEntity): Long

    @Update
    suspend fun update(provider: ProviderEntity)

    @Query("UPDATE providers SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM providers WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys WHERE providerId = :providerId")
    suspend fun getForProvider(providerId: Long): List<ApiKeyEntity>

    @Query("SELECT * FROM api_keys WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ApiKeyEntity?

    @Query("SELECT * FROM api_keys WHERE providerId = :providerId AND enabled = 1 ORDER BY priority DESC")
    suspend fun getEnabledForProvider(providerId: Long): List<ApiKeyEntity>

    @Insert
    suspend fun insert(key: ApiKeyEntity): Long

    @Update
    suspend fun update(key: ApiKeyEntity)

    @Query("UPDATE api_keys SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM api_keys WHERE id = :id")
    suspend fun delete(id: Long)
}