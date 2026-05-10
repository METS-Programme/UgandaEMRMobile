package com.lyecdevelopers.scanner.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object QrScannerModule {
    // Scanner dependencies can be provided here if needed
    // ViewModel is provided by HiltViewModelFactory via @HiltViewModel
}
