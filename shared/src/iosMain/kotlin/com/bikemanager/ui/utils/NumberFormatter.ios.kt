package com.bikemanager.ui.utils

actual fun formatNumber(value: Int): String {
    val str = value.toString()
    return str.reversed().chunked(3).joinToString(" ").reversed()
}

actual fun formatNumberDecimal(value: Float): String {
    // Split integer and decimal parts
    val parts = value.toString().split('.')
    val intPart = parts[0].toIntOrNull() ?: 0
    val decPart = if (parts.size > 1) parts[1].take(1) else "0"

    return "${formatNumber(intPart)}.$decPart"
}
