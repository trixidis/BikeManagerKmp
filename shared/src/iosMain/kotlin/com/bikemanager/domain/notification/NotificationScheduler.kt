package com.bikemanager.domain.notification

import com.bikemanager.domain.common.ErrorHandler
import com.bikemanager.domain.repository.BikeRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.UserNotifications.*
import kotlin.coroutines.resume
import com.bikemanager.domain.common.Result as DomainResult

/**
 * Implémentation iOS de NotificationScheduler utilisant UNUserNotificationCenter.
 */
class NotificationSchedulerImpl(
    private val bikeRepository: BikeRepository
) : NotificationScheduler {

    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun scheduleReminder(
        notificationId: String,
        title: String,
        body: String,
        delayMillis: Long,
        deepLinkData: Map<String, String>
    ): DomainResult<Unit> = try {
        // Demander les permissions (si pas déjà accordées)
        val isAuthorized = requestAuthorization()
        if (!isAuthorized) {
            Napier.w { "Permissions de notification refusées" }
            return DomainResult.Success(Unit) // Fonctionnement silencieux
        }

        // Créer le contenu de la notification
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(body)
            setSound(UNNotificationSound.defaultSound())

            // Ajouter les données de deep link dans userInfo
            setUserInfo(mapOf(
                "bikeId" to deepLinkData["bikeId"],
                "bikeName" to deepLinkData["bikeName"],
                "countingMethod" to deepLinkData["countingMethod"],
                "openTab" to deepLinkData["openTab"]
            ))
        }

        // Créer le trigger (délai en secondes)
        val delaySeconds = delayMillis / 1000.0
        val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = delaySeconds,
            repeats = false
        )

        // Créer la requête de notification
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = notificationId,
            content = content,
            trigger = trigger
        )

        // Planifier la notification
        suspendCancellableCoroutine { continuation ->
            notificationCenter.addNotificationRequest(request) { error: NSError? ->
                if (error != null) {
                    Napier.e { "Erreur lors de la planification de la notification : ${error.localizedDescription}" }
                    continuation.resume(DomainResult.Failure(ErrorHandler.handle(Exception(error.localizedDescription))))
                } else {
                    Napier.d { "Notification planifiée : $notificationId, délai : ${delayMillis}ms" }
                    continuation.resume(DomainResult.Success(Unit))
                }
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Napier.e("Erreur lors de la planification de la notification", e)
        DomainResult.Failure(ErrorHandler.handle(e))
    }

    override suspend fun cancelReminder(notificationId: String): DomainResult<Unit> = try {
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf(notificationId))
        Napier.d { "Notification annulée : $notificationId" }
        DomainResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Napier.e("Erreur lors de l'annulation de la notification", e)
        DomainResult.Failure(ErrorHandler.handle(e))
    }

    /**
     * Demande les permissions de notification.
     * @return true si autorisé, false sinon
     */
    private suspend fun requestAuthorization(): Boolean = suspendCancellableCoroutine { continuation ->
        notificationCenter.requestAuthorizationWithOptions(
            options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        ) { granted, error ->
            if (error != null) {
                Napier.w { "Erreur lors de la demande de permission : ${error.localizedDescription}" }
                continuation.resume(false)
            } else {
                continuation.resume(granted)
            }
        }
    }
}

/**
 * Factory pour créer l'instance iOS de NotificationScheduler.
 */
actual fun createNotificationScheduler(): NotificationScheduler {
    // Le bikeRepository sera injecté via Koin
    error("NotificationScheduler doit être créé via Koin avec le BikeRepository")
}
