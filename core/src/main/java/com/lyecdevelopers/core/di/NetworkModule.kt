package com.lyecdevelopers.core.di

import com.lyecdevelopers.core.BuildConfig
import com.lyecdevelopers.core.data.remote.AuthApi
import com.lyecdevelopers.core.data.remote.interceptor.AuthInterceptor
import com.lyecdevelopers.core.model.Config
import com.lyecdevelopers.core.data.remote.interceptor.provideLoggingInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideConfig(): Config {
        return Config(
            baseUrl = BuildConfig.API_BASE_URL,
            apiKey = BuildConfig.API_CLIENT_ID,
            username = BuildConfig.API_SERVER_USERNAME,
            password = BuildConfig.API_SERVER_PASSWORD
        )
    }



    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(provideLoggingInterceptor())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        moshi: Moshi,
        config: Config
    ): Retrofit = Retrofit.Builder()
        .baseUrl(config.baseUrl)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideFormApiService(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)
}