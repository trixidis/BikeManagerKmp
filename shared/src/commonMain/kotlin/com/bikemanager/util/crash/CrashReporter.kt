package com.bikemanager.util.crash

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics

/**
 * Thin wrapper around Firebase Crashlytics (via GitLive KMP SDK).
 * Fatal (uncaught) crashes are captured automatically once the SDK is initialized;
 * this object is used to additionally report handled/non-fatal errors and context.
 *
 * All calls are defensive: Crashlytics may be unavailable (e.g. JVM unit tests, where
 * no FirebaseApp is initialized), and telemetry must never crash the app it's monitoring.
 */
object CrashReporter {

    /**
     * Records a non-fatal error so it shows up in the Crashlytics dashboard.
     * @param context Optional breadcrumb describing what the app was doing.
     */
    fun recordException(throwable: Throwable, context: String? = null) {
        runCatching {
            if (context != null) {
                Firebase.crashlytics.log(context)
            }
            Firebase.crashlytics.recordException(throwable)
        }
    }

    /** Adds a breadcrumb log line included in the next crash/error report. */
    fun log(message: String) {
        runCatching { Firebase.crashlytics.log(message) }
    }

    /** Associates subsequent reports with the given user id (pass empty string to clear). */
    fun setUserId(userId: String) {
        runCatching { Firebase.crashlytics.setUserId(userId) }
    }
}
