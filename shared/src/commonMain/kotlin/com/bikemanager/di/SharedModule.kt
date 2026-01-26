package com.bikemanager.di

import com.bikemanager.data.repository.AuthRepositoryImpl
import com.bikemanager.data.repository.BikeRepositoryImpl
import com.bikemanager.data.repository.MaintenanceRepositoryImpl
import com.bikemanager.domain.repository.AuthRepository
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.domain.usecase.auth.GetCurrentUserUseCase
import com.bikemanager.domain.usecase.auth.SignInUseCase
import com.bikemanager.domain.usecase.auth.SignOutUseCase
import com.bikemanager.domain.usecase.bike.AddBikeUseCase
import com.bikemanager.domain.usecase.bike.DeleteBikeUseCase
import com.bikemanager.domain.usecase.bike.GetBikesUseCase
import com.bikemanager.domain.usecase.bike.UpdateBikeUseCase
import com.bikemanager.domain.usecase.maintenance.AddMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.DeleteMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.GetMaintenancesUseCase
import com.bikemanager.domain.usecase.maintenance.MarkMaintenanceDoneUseCase
import com.bikemanager.presentation.auth.AuthViewModelMvi
import com.bikemanager.presentation.bikes.BikesViewModelMvi
import com.bikemanager.presentation.maintenances.MaintenancesViewModelMvi
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Shared Koin module for dependency injection.
 * Uses Firebase Realtime Database with offline persistence as single source of truth.
 */
val sharedModule: Module = module {
    // Repositories (Firebase with offline persistence)
    single<AuthRepository> { AuthRepositoryImpl() }
    single<BikeRepository> { BikeRepositoryImpl(get()) }
    single<MaintenanceRepository> { MaintenanceRepositoryImpl(get()) }

    // Bike Use Cases
    factory { GetBikesUseCase(get()) }
    factory { AddBikeUseCase(get()) }
    factory { UpdateBikeUseCase(get()) }
    factory { DeleteBikeUseCase(get(), get()) }

    // Maintenance Use Cases
    factory { GetMaintenancesUseCase(get()) }
    factory { AddMaintenanceUseCase(get()) }
    factory { MarkMaintenanceDoneUseCase(get()) }
    factory { DeleteMaintenanceUseCase(get()) }

    // Auth Use Cases
    factory { GetCurrentUserUseCase(get()) }
    factory { SignInUseCase(get()) }
    factory { SignOutUseCase(get()) }

    // ViewModels (MVI Pattern)
    single { AuthViewModelMvi(get(), get(), get()) }
    single { BikesViewModelMvi(get(), get(), get(), get()) }
    factory { (bikeId: String) -> MaintenancesViewModelMvi(bikeId, get(), get(), get(), get()) }
}
