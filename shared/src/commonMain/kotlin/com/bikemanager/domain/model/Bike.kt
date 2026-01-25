package com.bikemanager.domain.model

/**
 * Represents a motorcycle in the application.
 *
 * @property id Firebase key (unique identifier)
 * @property name Display name of the bike
 * @property countingMethod Method used to track maintenance (KM or HOURS)
 */
data class Bike(
    val id: String = "",
    val name: String,
    val countingMethod: CountingMethod = CountingMethod.KM
)
