package com.lyecdevelopers.sync.di

import com.lyecdevelopers.core.data.local.dao.EncounterDao
import com.lyecdevelopers.core.data.local.dao.FormDao
import com.lyecdevelopers.core.data.local.dao.PatientDao
import com.lyecdevelopers.core.data.remote.FormApi
import com.lyecdevelopers.sync.data.repository.SyncRepositoryImpl
import com.lyecdevelopers.sync.domain.repository.SyncRepository
import com.lyecdevelopers.sync.domain.usecase.SyncUseCase
import com.squareup.moshi.JsonAdapter
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
        patientDao: PatientDao,
        encounterDao: EncounterDao,
        listOfMapAdapter: JsonAdapter<List<Map<String, Any>>>,
    ): SyncRepository {
        return SyncRepositoryImpl(
            formApi = formApi,
            formDao = formDao,
            patientDao = patientDao,
            encounterDao = encounterDao,
            listOfMapAdapter = listOfMapAdapter
        )
    }

    @Provides
    @Singleton
    fun provideSyncUseCase(repository: SyncRepository): SyncUseCase {
        return SyncUseCase(repository)
    }
}