package com.bikemanager.di

import com.bikemanager.domain.notification.NotificationScheduler
import com.bikemanager.domain.notification.NotificationSchedulerImpl
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Module iOS pour les dépendances platform-specific.
 */
actual val platformModule: Module = module {
    single<NotificationScheduler> { NotificationSchedulerImpl(get()) }
}
