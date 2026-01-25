package com.bikemanager.di

import com.bikemanager.data.local.BikeLocalDataSource
import com.bikemanager.data.local.BikeManagerDatabase
import com.bikemanager.data.local.DatabaseDriverFactory
import com.bikemanager.data.local.MaintenanceLocalDataSource
import com.bikemanager.data.repository.AuthRepositoryImpl
import com.bikemanager.data.repository.BikeRepositoryImpl
import com.bikemanager.data.repository.MaintenanceRepositoryImpl
import com.bikemanager.data.repository.SyncRepositoryImpl
import com.bikemanager.domain.repository.AuthRepository
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.domain.repository.SyncRepository
import com.bikemanager.domain.usecase.auth.GetCurrentUserUseCase
import com.bikemanager.domain.usecase.auth.SignInUseCase
import com.bikemanager.domain.usecase.auth.SignOutUseCase
import com.bikemanager.domain.usecase.bike.AddBikeUseCase
import com.bikemanager.domain.usecase.bike.GetBikesUseCase
import com.bikemanager.domain.usecase.bike.UpdateBikeUseCase
import com.bikemanager.domain.usecase.maintenance.AddMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.DeleteMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.GetMaintenancesUseCase
import com.bikemanager.domain.usecase.maintenance.MarkMaintenanceDoneUseCase
import com.bikemanager.domain.usecase.sync.ObserveAndSyncFromCloudUseCase
import com.bikemanager.domain.usecase.sync.PullFromCloudUseCase
import com.bikemanager.presentation.auth.AuthViewModel
import com.bikemanager.presentation.bikes.BikesViewModel
import com.bikemanager.presentation.maintenances.MaintenancesViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Shared Koin module for dependency injection.
 * Contains all common dependencies that work on both Android and iOS.
 */
val sharedModule: Module = module {
    // Database
    single { BikeManagerDatabase(get<DatabaseDriverFactory>().createDriver()) }

    // Local Data Sources
    single { BikeLocalDataSource(get()) }
    single { MaintenanceLocalDataSource(get()) }

    // Repositories (using GitLive Firebase SDK - works on all platforms)
    single<BikeRepository> { BikeRepositoryImpl(get()) }
    single<MaintenanceRepository> { MaintenanceRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl() }
    single<SyncRepository> { SyncRepositoryImpl() }

    // Bike Use Cases (with sync support)
    factory { GetBikesUseCase(get()) }
    factory { AddBikeUseCase(get(), get()) }
    factory { UpdateBikeUseCase(get(), get()) }

    // Maintenance Use Cases (with sync support)
    factory { GetMaintenancesUseCase(get()) }
    factory { AddMaintenanceUseCase(get(), get(), get()) }
    factory { MarkMaintenanceDoneUseCase(get(), get(), get()) }
    factory { DeleteMaintenanceUseCase(get(), get(), get()) }

    // Sync Use Cases
    factory { PullFromCloudUseCase(get(), get(), get()) }
    factory { ObserveAndSyncFromCloudUseCase(get(), get(), get()) }

    // Auth Use Cases
    factory { GetCurrentUserUseCase(get()) }
    factory { SignInUseCase(get()) }
    factory { SignOutUseCase(get()) }

    // ViewModels
    factory { AuthViewModel(get(), get(), get()) }
    factory { BikesViewModel(get(), get(), get(), get<ObserveAndSyncFromCloudUseCase>()) }
    factory { (bikeId: Long) -> MaintenancesViewModel(bikeId, get(), get(), get(), get(), get()) }
}
