package com.lyecdevelopers.sync.di

import com.lyecdevelopers.core.data.local.dao.FormDao
import com.lyecdevelopers.core.data.remote.FormApi
import com.lyecdevelopers.sync.data.repository.SyncRepositoryImpl
import com.lyecdevelopers.sync.domain.repository.SyncRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SyncModule {
    @Provides
    @Singleton
    fun provideFormRepository(
        formApi: FormApi,
        formDao: FormDao,
    ): SyncRepository {
        return SyncRepositoryImpl(
            formApi = formApi,
            formDao = formDao
        )
    }
}