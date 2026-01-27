package com.bikemanager.ui.utils

actual fun formatNumber(value: Int): String {
    return String.format("%,d", value).replace(',', ' ')
}

actual fun formatNumberDecimal(value: Float): String {
    return String.format("%,.1f", value).replace(',', ' ')
}
