package com.bikemanager.ui.utils

/**
 * Format an integer with thousand separators (spaces).
 * Example: 12500 -> "12 500"
 */
expect fun formatNumber(value: Int): String

/**
 * Format a float with thousand separators (spaces) and 1 decimal.
 * Example: 12500.5 -> "12 500.5"
 */
expect fun formatNumberDecimal(value: Float): String
