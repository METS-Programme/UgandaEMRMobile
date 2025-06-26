package com.lyecdevelopers.form.di

import android.content.Context
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineProvider
import com.lyecdevelopers.core.data.local.dao.FormDao
import com.lyecdevelopers.core.data.local.dao.PatientDao
import com.lyecdevelopers.core.data.remote.FormApi
import com.lyecdevelopers.form.data.repository.FormRepositoryImpl
import com.lyecdevelopers.form.data.repository.PatientRepositoryImpl
import com.lyecdevelopers.form.domain.repository.FormRepository
import com.lyecdevelopers.form.domain.repository.PatientRepository
import com.lyecdevelopers.form.domain.usecase.FormsUseCase
import com.lyecdevelopers.form.domain.usecase.PatientsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FormModule {

    @Provides
    @Singleton
    fun provideFhirEngine(@ApplicationContext context: Context): FhirEngine {
        return FhirEngineProvider.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFormsUseCase(repository: FormRepository): FormsUseCase {
        return FormsUseCase(repository)
    }

    @Provides
    @Singleton
    fun providePatientsUseCase(repository: PatientRepository): PatientsUseCase {
        return PatientsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideFormRepository(
        formApi: FormApi,
        formDao: FormDao,
    ): FormRepository {
        return FormRepositoryImpl(
            formApi = formApi,
            formDao = formDao,
        )
    }

    @Provides
    @Singleton
    fun providePatientRepository(
        patientDao: PatientDao,
        fhirEngine: FhirEngine,
    ): PatientRepository {
        return PatientRepositoryImpl(
            patientDao = patientDao, fhirEngine = fhirEngine
        )
    }
}