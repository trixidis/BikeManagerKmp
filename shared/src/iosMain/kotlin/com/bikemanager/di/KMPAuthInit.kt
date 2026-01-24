package com.bikemanager.di

import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider

/**
 * Initializes KMPAuth for iOS with the given client ID.
 * Must be called from Swift after Firebase is configured.
 */
object KMPAuthInitializer {
    fun initialize(clientId: String) {
        GoogleAuthProvider.create(credentials = GoogleAuthCredentials(serverId = clientId))
    }
}
