package com.bikemanager.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Bouton "Sign in with Apple" platform-specific.
 *
 * Implémentations :
 * - iOS : Bouton natif Apple avec style officiel (noir, logo Apple)
 * - Android : Material3 Button stylé selon Apple guidelines
 *
 * @param onClick Callback appelé lorsque l'utilisateur clique sur le bouton
 * @param modifier Modifier Compose pour personnalisation du layout
 * @param enabled Si false, le bouton est désactivé (état loading par exemple)
 */
@Composable
expect fun AppleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)
