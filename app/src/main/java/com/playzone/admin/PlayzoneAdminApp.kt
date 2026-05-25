package com.playzone.admin

import android.app.Application
import com.playzone.admin.service.AdminFCMService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PlayzoneAdminApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Buat notification channels saat app pertama kali dibuka
        AdminFCMService.createNotificationChannels(this)
    }
}
