package com.bikemanager.domain.usecase.notification

import com.bikemanager.domain.common.AppError
import com.bikemanager.domain.common.ErrorHandler
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.notification.NotificationConfig
import com.bikemanager.domain.notification.NotificationScheduler
import kotlinx.coroutines.CancellationException

/**
 * Use case pour planifier un rappel de maintenance.
 */
class ScheduleMaintenanceReminderUseCase(
    private val notificationScheduler: NotificationScheduler
) {
    /**
     * Planifie un rappel pour une maintenance "à faire".
     *
     * @param maintenanceId ID de la maintenance
     * @param maintenanceName Nom de la maintenance
     * @param bikeId ID de la moto
     * @param bikeName Nom de la moto
     * @param countingMethod Méthode de comptage de la moto
     * @return Result<Unit>
     */
    suspend operator fun invoke(
        maintenanceId: String,
        maintenanceName: String,
        bikeId: String,
        bikeName: String,
        countingMethod: CountingMethod
    ): Result<Unit> = try {
        val deepLinkData = mapOf(
            "bikeId" to bikeId,
            "bikeName" to bikeName,
            "countingMethod" to countingMethod.name,
            "openTab" to "todo"
        )

        notificationScheduler.scheduleReminder(
            notificationId = maintenanceId,
            title = NotificationConfig.NOTIFICATION_TITLE,
            body = NotificationConfig.buildNotificationBody(maintenanceName),
            delayMillis = NotificationConfig.reminderDelayMillis,
            deepLinkData = deepLinkData
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Failure(ErrorHandler.handle(e))
    }
}
