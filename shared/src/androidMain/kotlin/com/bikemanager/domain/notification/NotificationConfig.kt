package com.bikemanager.domain.notification

/**
 * Implémentation Android de NotificationConfig.
 * Utilise le système de build pour déterminer le mode debug.
 */
actual object NotificationConfig {
    // Note: Nous ne pouvons pas accéder à BuildConfig ici car il est dans le module androidApp
    // Nous utiliserons une approche différente : vérifier via System property
    actual val isDebugMode: Boolean
        get() {
            return try {
                // Essayer de détecter le mode debug via la propriété système
                val debuggable = System.getProperty("debug.enabled") == "true"
                debuggable
            } catch (e: Exception) {
                // Par défaut, mode production pour éviter les notifications de test en prod
                false
            }
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
