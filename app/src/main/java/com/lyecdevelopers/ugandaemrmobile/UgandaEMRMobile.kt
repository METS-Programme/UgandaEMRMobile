package com.lyecdevelopers.ugandaemrmobile

import android.app.Application
import com.lyecdevelopers.core.utils.AppLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class UgandaEMRMobile : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.init()
    }
}




