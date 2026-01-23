package com.bikemanager.di

import com.bikemanager.data.local.BikeLocalDataSource
import com.bikemanager.data.local.BikeManagerDatabase
import com.bikemanager.data.local.DatabaseDriverFactory
import com.bikemanager.data.local.MaintenanceLocalDataSource
import com.bikemanager.data.repository.BikeRepositoryImpl
import com.bikemanager.data.repository.MaintenanceRepositoryImpl
import com.bikemanager.domain.repository.BikeRepository
import com.bikemanager.domain.repository.MaintenanceRepository
import com.bikemanager.domain.usecase.bike.AddBikeUseCase
import com.bikemanager.domain.usecase.bike.GetBikesUseCase
import com.bikemanager.domain.usecase.bike.UpdateBikeUseCase
import com.bikemanager.domain.usecase.maintenance.AddMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.DeleteMaintenanceUseCase
import com.bikemanager.domain.usecase.maintenance.GetMaintenancesUseCase
import com.bikemanager.domain.usecase.maintenance.MarkMaintenanceDoneUseCase
import com.bikemanager.presentation.bikes.BikesViewModel
import com.bikemanager.presentation.maintenances.MaintenancesViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Shared Koin module for dependency injection.
 */
val sharedModule: Module = module {
    // Database
    single { BikeManagerDatabase(get<DatabaseDriverFactory>().createDriver()) }

    // Local Data Sources
    single { BikeLocalDataSource(get()) }
    single { MaintenanceLocalDataSource(get()) }

    // Repositories
    single<BikeRepository> { BikeRepositoryImpl(get()) }
    single<MaintenanceRepository> { MaintenanceRepositoryImpl(get()) }

    // Bike Use Cases
    factory { GetBikesUseCase(get()) }
    factory { AddBikeUseCase(get()) }
    factory { UpdateBikeUseCase(get()) }

    // Maintenance Use Cases
    factory { GetMaintenancesUseCase(get()) }
    factory { AddMaintenanceUseCase(get()) }
    factory { MarkMaintenanceDoneUseCase(get()) }
    factory { DeleteMaintenanceUseCase(get()) }

    // ViewModels
    factory { BikesViewModel(get(), get(), get()) }
    factory { (bikeId: Long) -> MaintenancesViewModel(bikeId, get(), get(), get(), get(), get()) }
}
