package com.bikemanager.domain.usecase.notification

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.ErrorHandler
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.notification.NotificationScheduler
import kotlinx.coroutines.CancellationException

/**
 * Use case pour annuler un rappel de maintenance planifié.
 */
class CancelMaintenanceReminderUseCase(
    private val notificationScheduler: NotificationScheduler
) {
    /**
     * Annule le rappel pour une maintenance.
     *
     * @param maintenanceId ID de la maintenance dont le rappel doit être annulé
     * @return Result<Unit>
     */
    suspend operator fun invoke(maintenanceId: String): Result<Unit> = try {
        notificationScheduler.cancelReminder(maintenanceId)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Failure(ErrorHandler.handle(e))
    }
}
