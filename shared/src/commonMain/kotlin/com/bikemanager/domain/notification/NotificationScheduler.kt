package com.bikemanager.domain.notification

import com.bikemanager.domain.common.Result

/**
 * Interface pour la planification et l'annulation de rappels de maintenance.
 */
interface NotificationScheduler {
    /**
     * Planifie un rappel de maintenance.
     *
     * @param notificationId Identifiant unique de la notification (utiliser maintenance.id)
     * @param title Titre de la notification
     * @param body Corps de la notification
     * @param delayMillis Délai avant affichage de la notification en millisecondes
     * @param deepLinkData Données pour le deep linking (bikeId, bikeName, countingMethod, etc.)
     * @return Result<Unit> - Success si planifié, Error en cas d'échec
     */
    suspend fun scheduleReminder(
        notificationId: String,
        title: String,
        body: String,
        delayMillis: Long,
        deepLinkData: Map<String, String>
    ): Result<Unit>

    /**
     * Annule un rappel de maintenance planifié.
     *
     * @param notificationId Identifiant de la notification à annuler
     * @return Result<Unit> - Success si annulé, Error en cas d'échec
     */
    suspend fun cancelReminder(notificationId: String): Result<Unit>
}

/**
 * Factory expect/actual pour créer l'instance platform-specific de NotificationScheduler.
 */
expect fun createNotificationScheduler(): NotificationScheduler
