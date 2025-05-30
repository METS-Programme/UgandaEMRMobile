package com.lyecdevelopers.core.di

import com.lyecdevelopers.core.model.Form
import com.lyecdevelopers.core.model.Patient
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MoshiModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }


    @Provides
    @Singleton
    fun provideFormAdapter(moshi: Moshi): JsonAdapter<Form> =
        moshi.adapter(Form::class.java)

    @Provides
    @Singleton
    fun providePatientAdapter(moshi: Moshi): JsonAdapter<Patient> =
        moshi.adapter(Patient::class.java)
}