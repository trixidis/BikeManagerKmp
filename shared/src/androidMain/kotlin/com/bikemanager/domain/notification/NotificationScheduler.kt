package com.bikemanager.domain.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bikemanager.domain.common.ErrorHandler
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit
import com.bikemanager.domain.common.Result as DomainResult

/**
 * Implémentation Android de NotificationScheduler utilisant WorkManager.
 */
class NotificationSchedulerImpl(
    private val context: Context
) : NotificationScheduler {

    private val workManager = WorkManager.getInstance(context)

    override suspend fun scheduleReminder(
        notificationId: String,
        title: String,
        body: String,
        delayMillis: Long,
        deepLinkData: Map<String, String>
    ): DomainResult<Unit> = try {
        val inputData = Data.Builder()
            .putString(KEY_NOTIFICATION_ID, notificationId)
            .putString(KEY_TITLE, title)
            .putString(KEY_BODY, body)
            .putString(KEY_BIKE_ID, deepLinkData["bikeId"])
            .putString(KEY_BIKE_NAME, deepLinkData["bikeName"])
            .putString(KEY_COUNTING_METHOD, deepLinkData["countingMethod"])
            .putString(KEY_OPEN_TAB, deepLinkData["openTab"])
            .build()

        val workRequest = OneTimeWorkRequestBuilder<MaintenanceNotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(notificationId)
            .build()

        workManager.enqueueUniqueWork(
            notificationId,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )

        Napier.d { "Notification planifiée : $notificationId, délai : ${delayMillis}ms" }
        DomainResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Napier.e("Erreur lors de la planification de la notification", e)
        DomainResult.Failure(ErrorHandler.handle(e))
    }

    override suspend fun cancelReminder(notificationId: String): DomainResult<Unit> = try {
        workManager.cancelUniqueWork(notificationId)
        Napier.d { "Notification annulée : $notificationId" }
        DomainResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Napier.e("Erreur lors de l'annulation de la notification", e)
        DomainResult.Failure(ErrorHandler.handle(e))
    }

    companion object {
        const val KEY_NOTIFICATION_ID = "notification_id"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_BIKE_ID = "bike_id"
        const val KEY_BIKE_NAME = "bike_name"
        const val KEY_COUNTING_METHOD = "counting_method"
        const val KEY_OPEN_TAB = "open_tab"
    }
}

/**
 * Factory pour créer l'instance Android de NotificationScheduler.
 */
actual fun createNotificationScheduler(): NotificationScheduler {
    // Le context sera injecté via Koin
    error("NotificationScheduler doit être créé via Koin avec le contexte Android")
}
