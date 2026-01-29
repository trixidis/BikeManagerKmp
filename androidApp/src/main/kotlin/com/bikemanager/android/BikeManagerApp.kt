package com.bikemanager.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.StrictMode
import androidx.work.Configuration
import androidx.work.WorkManager
import com.bikemanager.data.local.DatabaseDriverFactory
import com.bikemanager.di.sharedModule
import com.bikemanager.domain.notification.NotificationConfig
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.dsl.module

class BikeManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())

            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog() // log dans Logcat
                    //.penaltyDeath() // optionnel : crash pour forcer la correction
                    .build()
            )

            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .penaltyLog()
                    .build()
            )
        }

        // Initialize KMPAuth Google Sign-In with build-specific Web Client ID
        // Debug → Dev Firebase, Release → Prod Firebase
        GoogleAuthProvider.create(credentials = GoogleAuthCredentials(serverId = BuildConfig.WEB_CLIENT_ID))

        startKoin {
            androidLogger()
            androidContext(this@BikeManagerApp)
            modules(
                androidModule,
                sharedModule
            )
        }

        // Créer le canal de notification (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        // Configurer WorkManager avec Koin
        val workManagerConfig = Configuration.Builder()
            .setWorkerFactory(workManagerFactory())
            .build()
        WorkManager.initialize(this, workManagerConfig)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationConfig.NOTIFICATION_CHANNEL_ID,
                NotificationConfig.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Rappels pour les maintenances à faire"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
            Napier.d { "Canal de notification créé : ${NotificationConfig.NOTIFICATION_CHANNEL_ID}" }
        }
    }
}

/**
 * Android-specific Koin module.
 * Only provides platform-specific dependencies.
 * All Firebase/Auth logic is now in shared module using GitLive SDK.
 */
val androidModule = module {
    // Database Driver Factory (platform-specific)
    single { DatabaseDriverFactory(get()) }
}
