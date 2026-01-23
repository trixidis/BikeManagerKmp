package com.bikemanager.domain.model

/**
 * Represents an authenticated user.
 */
data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)
