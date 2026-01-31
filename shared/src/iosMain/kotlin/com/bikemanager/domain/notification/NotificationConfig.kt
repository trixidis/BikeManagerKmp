package com.bikemanager.domain.notification

import platform.Foundation.NSBundle

/**
 * Implémentation iOS de NotificationConfig.
 * Utilise la configuration de build pour déterminer le mode.
 */
actual object NotificationConfig {
    actual val isDebugMode: Boolean
        get() {
            // En iOS, on vérifie si c'est une build Debug en regardant si le preprocessor DEBUG est défini
            // On peut aussi vérifier via les configurations de build
            // Pour l'instant, on retourne true pour les tests (à ajuster selon la config iOS)
            val isDebug = NSBundle.mainBundle.objectForInfoDictionaryKey("IS_DEBUG") as? Boolean
            return isDebug ?: false // Par défaut, mode production
        }

    actual val NOTIFICATION_CHANNEL_ID: String = "maintenance_reminders"

    actual val NOTIFICATION_CHANNEL_NAME: String = "Rappels d'entretien"

    actual val NOTIFICATION_TITLE: String = "Rappel d'entretien"

    actual val reminderDelayMillis: Long
        get() = if (isDebugMode) {
            10_000L // 10 secondes
        } else {
            7 * 24 * 60 * 60 * 1000L // 7 jours
        }

    actual fun buildNotificationBody(maintenanceName: String): String {
        return "N'oubliez pas : $maintenanceName"
    }
}
