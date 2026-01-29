package com.bikemanager.domain.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker
import com.bikemanager.domain.repository.BikeRepository
import io.github.aakira.napier.Napier
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.bikemanager.domain.common.Result as DomainResult

/**
 * Worker qui affiche une notification de rappel de maintenance.
 * Vérifie que la bike existe toujours avant d'afficher la notification.
 */
class MaintenanceNotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val bikeRepository: BikeRepository by inject()

    override suspend fun doWork(): ListenableWorker.Result {
        val notificationId = inputData.getString(NotificationSchedulerImpl.KEY_NOTIFICATION_ID)
        val title = inputData.getString(NotificationSchedulerImpl.KEY_TITLE)
        val body = inputData.getString(NotificationSchedulerImpl.KEY_BODY)
        val bikeId = inputData.getString(NotificationSchedulerImpl.KEY_BIKE_ID)
        val bikeName = inputData.getString(NotificationSchedulerImpl.KEY_BIKE_NAME)
        val countingMethod = inputData.getString(NotificationSchedulerImpl.KEY_COUNTING_METHOD)
        val openTab = inputData.getString(NotificationSchedulerImpl.KEY_OPEN_TAB)

        // Vérification des données
        if (notificationId == null || title == null || body == null || bikeId == null) {
            Napier.w { "Données de notification manquantes, annulation" }
            return ListenableWorker.Result.failure()
        }

        // Vérifier que la bike existe toujours
        return when (val result = bikeRepository.getBikeById(bikeId)) {
            is DomainResult.Success -> {
                if (result.value != null) {
                    Napier.d { "Bike $bikeId existe, affichage de la notification $notificationId" }
                    showNotification(
                        notificationId = notificationId,
                        title = title,
                        body = body,
                        bikeId = bikeId,
                        bikeName = bikeName ?: "",
                        countingMethod = countingMethod ?: "KILOMETERS",
                        openTab = openTab ?: "todo"
                    )
                    ListenableWorker.Result.success()
                } else {
                    Napier.w { "Bike $bikeId n'existe plus, annulation de la notification $notificationId" }
                    ListenableWorker.Result.success() // Success pour ne pas retry
                }
            }
            is DomainResult.Failure -> {
                Napier.w { "Erreur lors de la récupération de la bike $bikeId : ${result.error.message}" }
                ListenableWorker.Result.success() // Success pour ne pas retry
            }
        }
    }

    private fun showNotification(
        notificationId: String,
        title: String,
        body: String,
        bikeId: String,
        bikeName: String,
        countingMethod: String,
        openTab: String
    ) {
        val deepLinkUri = Uri.parse(
            "bikemanager://maintenance?bikeId=$bikeId&bikeName=$bikeName&countingMethod=$countingMethod&openTab=$openTab"
        )

        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId.hashCode(),
            intent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(context, NotificationConfig.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Icône système par défaut
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId.hashCode(), notification)

        Napier.d { "Notification affichée : $notificationId" }
    }
}
