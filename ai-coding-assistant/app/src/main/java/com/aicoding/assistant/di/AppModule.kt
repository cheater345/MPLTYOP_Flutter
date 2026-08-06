package com.aicoding.assistant.di

import android.content.Context
import com.aicoding.assistant.data.local.ApiKeyDao
import com.aicoding.assistant.data.local.AppDatabase
import com.aicoding.assistant.data.local.ChatDao
import com.aicoding.assistant.data.local.MessageDao
import com.aicoding.assistant.data.local.ProviderDao
import com.aicoding.assistant.data.local.PromptDao
import com.aicoding.assistant.data.remote.ModelFetcher
import com.aicoding.assistant.data.repository.ApiKeyRepository
import com.aicoding.assistant.data.repository.ChatRepository
import com.aicoding.assistant.data.repository.ProviderRepository
import com.aicoding.assistant.data.repository.PromptRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getInstance(context)

    @Provides
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

    @Provides
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()

    @Provides
    fun providePromptDao(db: AppDatabase): PromptDao = db.promptDao()

    @Provides
    fun provideProviderDao(db: AppDatabase): ProviderDao = db.providerDao()

    @Provides
    fun provideApiKeyDao(db: AppDatabase): ApiKeyDao = db.apiKeyDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideApiKeyRepository(dao: ApiKeyDao): ApiKeyRepository = ApiKeyRepository(dao)

    @Provides
    @Singleton
    fun provideProviderRepository(
        dao: ProviderDao,
        @ApplicationContext context: Context,
    ): ProviderRepository = ProviderRepository(dao, context)

    @Provides
    @Singleton
    fun providePromptRepository(dao: PromptDao): PromptRepository = PromptRepository(dao)

    @Provides
    @Singleton
    fun provideChatRepository(
        chatDao: ChatDao,
        messageDao: MessageDao,
        providerRepository: ProviderRepository,
        apiKeyRepository: ApiKeyRepository,
    ): ChatRepository = ChatRepository(chatDao, messageDao, providerRepository, apiKeyRepository)

    @Provides
    @Singleton
    fun provideModelFetcher(client: OkHttpClient): ModelFetcher = ModelFetcher(client)
}