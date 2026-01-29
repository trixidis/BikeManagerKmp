package com.bikemanager.domain.notification

/**
 * Configuration des notifications de rappel de maintenance.
 * isDebugMode est défini via expect/actual pour chaque plateforme.
 */
expect object NotificationConfig {
    /**
     * Indique si l'application est en mode debug.
     * Défini via expect/actual dans androidMain et iosMain.
     */
    val isDebugMode: Boolean

    /**
     * Identifiant du canal de notification (Android 8+).
     */
    val NOTIFICATION_CHANNEL_ID: String

    /**
     * Nom du canal de notification (Android 8+).
     */
    val NOTIFICATION_CHANNEL_NAME: String

    /**
     * Titre par défaut des notifications.
     */
    val NOTIFICATION_TITLE: String

    /**
     * Délai avant affichage de la notification :
     * - Mode debug : 10 secondes (pour tests)
     * - Mode production : 7 jours
     */
    val reminderDelayMillis: Long

    /**
     * Construit le corps de la notification.
     */
    fun buildNotificationBody(maintenanceName: String): String
}
