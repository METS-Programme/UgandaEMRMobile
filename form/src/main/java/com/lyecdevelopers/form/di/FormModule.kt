package com.lyecdevelopers.form.di

import com.lyecdevelopers.core.data.remote.FormApi
import com.lyecdevelopers.form.data.repository.FormRepositoryImpl
import com.lyecdevelopers.form.domain.repository.FormRepository
import com.lyecdevelopers.form.domain.usecase.FormsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FormModule {

    @Provides
    @Singleton
    fun provideFormsUseCase(repository: FormRepository): FormsUseCase {
        return FormsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideFormRepository(
        formApi: FormApi,
    ): FormRepository {
        return FormRepositoryImpl(
            formApi = formApi
        )
    }
}