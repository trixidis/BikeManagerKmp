package com.bikemanager.android

import android.app.Application
import com.bikemanager.android.data.remote.SyncRepositoryImpl
import com.bikemanager.android.data.repository.AuthRepositoryImpl
import com.bikemanager.data.local.DatabaseDriverFactory
import com.bikemanager.di.sharedModule
import com.bikemanager.domain.repository.AuthRepository
import com.bikemanager.domain.repository.SyncRepository
import com.bikemanager.domain.usecase.auth.GetCurrentUserUseCase
import com.bikemanager.domain.usecase.auth.SignInUseCase
import com.bikemanager.domain.usecase.auth.SignOutUseCase
import com.bikemanager.domain.usecase.bike.AddBikeUseCase
import com.bikemanager.domain.usecase.bike.UpdateBikeUseCase
import com.bikemanager.domain.usecase.maintenance.AddMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.DeleteMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.MarkMaintenanceDoneUseCase
import com.bikemanager.domain.usecase.sync.PullFromCloudUseCase
import com.bikemanager.presentation.auth.AuthViewModel
import com.bikemanager.presentation.bikes.BikesViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class BikeManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@BikeManagerApp)
            modules(
                sharedModule,
                androidModule // Android module AFTER shared to override use cases
            )
        }
    }
}

val androidModule = module {
    // Database
    single { DatabaseDriverFactory(get()) }

    // Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseDatabase.getInstance() }

    // Auth Repository
    single<AuthRepository> { AuthRepositoryImpl(get()) }

    // Sync Repository
    single<SyncRepository> { SyncRepositoryImpl(get(), get()) }

    // Auth Use Cases
    factory { GetCurrentUserUseCase(get()) }
    factory { SignInUseCase(get()) }
    factory { SignOutUseCase(get()) }

    // Auth ViewModel
    factory { AuthViewModel(get(), get(), get()) }

    // Use cases with sync support (these will override the shared module definitions)
    factory { AddBikeUseCase(get(), get()) }
    factory { UpdateBikeUseCase(get(), get()) }
    factory { AddMaintenanceUseCase(get(), get(), get()) }
    factory { MarkMaintenanceDoneUseCase(get(), get(), get()) }
    factory { DeleteMaintenanceUseCase(get(), get(), get()) }
    factory { PullFromCloudUseCase(get(), get(), get()) }

    // Override BikesViewModel to include cloud pull
    factory { BikesViewModel(get(), get(), get(), get()) }
}
