package com.bikemanager.domain.usecase.maintenance

import com.bikemanager.domain.common.Result
import com.bikemanager.domain.export.MaintenanceHistoryExporter
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance

/**
 * Use case for exporting a bike's completed maintenance history to a shareable PDF.
 * Intended for resale: only done maintenances are exported, most recent first.
 */
class ExportMaintenanceHistoryUseCase(
    private val exporter: MaintenanceHistoryExporter
) {
    suspend operator fun invoke(
        bikeName: String,
        countingMethod: CountingMethod,
        doneMaintenances: List<Maintenance>
    ): Result<Unit> {
        val sortedMaintenances = doneMaintenances.sortedByDescending { it.date }
        return exporter.exportAndShare(bikeName, countingMethod, sortedMaintenances)
    }
}
