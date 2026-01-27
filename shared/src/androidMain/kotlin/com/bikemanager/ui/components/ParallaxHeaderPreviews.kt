package com.bikemanager.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.ui.theme.BikeManagerTheme

/**
 * Preview for ParallaxHeader with Done tab (blue gradient)
 */
@Preview(name = "ParallaxHeader - Done Tab", showBackground = true)
@Composable
private fun ParallaxHeaderDonePreview() {
    BikeManagerTheme {
        ParallaxHeader(
            bikeName = "Yamaha MT-07",
            totalValue = 12500f,
            countingMethod = CountingMethod.KM,
            activeTab = TabVariant.DONE,
            onBackClick = {}
        )
    }
}

/**
 * Preview for ParallaxHeader with Todo tab (teal gradient)
 */
@Preview(name = "ParallaxHeader - Todo Tab", showBackground = true)
@Composable
private fun ParallaxHeaderTodoPreview() {
    BikeManagerTheme {
        ParallaxHeader(
            bikeName = "Honda CBR 600",
            totalValue = 156.5f,
            countingMethod = CountingMethod.HOURS,
            activeTab = TabVariant.TODO,
            onBackClick = {}
        )
    }
}

/**
 * Preview with long bike name
 */
@Preview(name = "ParallaxHeader - Long Name", showBackground = true)
@Composable
private fun ParallaxHeaderLongNamePreview() {
    BikeManagerTheme {
        ParallaxHeader(
            bikeName = "Kawasaki Ninja ZX-10R Special Edition",
            totalValue = 45789f,
            countingMethod = CountingMethod.KM,
            activeTab = TabVariant.DONE,
            onBackClick = {}
        )
    }
}
