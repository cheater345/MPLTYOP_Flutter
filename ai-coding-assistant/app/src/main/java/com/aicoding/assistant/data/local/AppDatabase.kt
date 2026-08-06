package com.aicoding.assistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromList(value: List<String>): String = value.joinToString("\u0001")

    @TypeConverter
    fun toList(value: String): List<String> = if (value.isEmpty()) emptyList() else value.split("\u0001")
}

@Database(
    entities = [
        ChatEntity::class,
        MessageEntity::class,
        PromptEntity::class,
        ProviderEntity::class,
        ApiKeyEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun promptDao(): PromptDao
    abstract fun providerDao(): ProviderDao
    abstract fun apiKeyDao(): ApiKeyDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ai_coding_assistant.db"
                ).build().also { instance = it }
            }
    }
}