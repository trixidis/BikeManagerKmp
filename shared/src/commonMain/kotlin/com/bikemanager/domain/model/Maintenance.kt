package com.bikemanager.domain.model

/**
 * Represents a maintenance record for a bike.
 *
 * @property id Firebase key (unique identifier)
 * @property name Type of maintenance (e.g., "Vidange", "Pneu avant")
 * @property value Kilometers or hours at which maintenance was done (-1 if not defined)
 * @property date Timestamp in milliseconds when maintenance was done (0 if not done)
 * @property isDone Whether the maintenance has been completed
 * @property bikeId Firebase key of the parent bike
 */
data class Maintenance(
    val id: String = "",
    val name: String,
    val value: Float = -1f,
    val date: Long = 0,
    val isDone: Boolean = false,
    val bikeId: String
)
