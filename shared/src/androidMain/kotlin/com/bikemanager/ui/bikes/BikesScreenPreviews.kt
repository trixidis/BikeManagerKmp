package com.bikemanager.ui.bikes

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bikemanager.domain.model.Bike
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.presentation.bikes.BikesUiState
import com.bikemanager.ui.theme.BikeManagerTheme

/**
 * Sample garage used by the bikes list previews and screenshot tests.
 * Mixes km and hours counting methods, short and long names, and a bike without history.
 */
internal val previewBikes = listOf(
    Bike(id = "1", name = "Yamaha MT-07", countingMethod = CountingMethod.KM),
    Bike(id = "2", name = "KTM 450 EXC-F", countingMethod = CountingMethod.HOURS),
    Bike(id = "3", name = "Kawasaki Ninja ZX-10R Special Edition", countingMethod = CountingMethod.KM),
    Bike(id = "4", name = "Honda CRF 250", countingMethod = CountingMethod.HOURS),
)

internal val previewBikeTotals = mapOf(
    "1" to 12_500f,
    "2" to 156.5f,
    "3" to 45_789f,
    // "4" volontairement absent : moto sans entretien enregistré (affiche 0)
)

@Composable
internal fun BikesListPreviewContent(darkTheme: Boolean) {
    BikeManagerTheme(darkTheme = darkTheme) {
        BikesScreen(
            uiState = BikesUiState.Success(previewBikes),
            bikeTotals = previewBikeTotals,
            onAddBikeClick = {},
            onBikeClick = {},
            onEditBikeClick = {},
            onBikeLongPress = {},
            onSignOutClick = {},
            onDeleteAccountClick = {},
        )
    }
}

@Preview(name = "Bikes list - Dark", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
internal fun BikesListDarkPreview() = BikesListPreviewContent(darkTheme = true)

@Preview(name = "Bikes list - Light", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
internal fun BikesListLightPreview() = BikesListPreviewContent(darkTheme = false)
