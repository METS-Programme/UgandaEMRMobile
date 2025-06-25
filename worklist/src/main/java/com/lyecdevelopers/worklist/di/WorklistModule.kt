package com.lyecdevelopers.worklist.di

import com.lyecdevelopers.core.data.local.dao.VisitSummaryDao
import com.lyecdevelopers.worklist.data.repository.VisitRepositoryImpl
import com.lyecdevelopers.worklist.domain.repository.VisitRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorklistModule {


    @Provides
    @Singleton
    fun provideVisitSummaryRepository(
        visitSummaryDao: VisitSummaryDao,
    ): VisitRepository {
        return VisitRepositoryImpl(
            visitSummaryDao = visitSummaryDao,
        )
    }
}
