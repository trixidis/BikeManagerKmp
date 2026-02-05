package com.bikemanager.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bikemanager.shared.generated.resources.Res
import bikemanager.shared.generated.resources.ic_apple_logo
import bikemanager.shared.generated.resources.sign_in_with_apple
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Implémentation Android du bouton Apple Sign In.
 * Utilise un Material3 Button stylé selon les guidelines Apple.
 *
 * Style :
 * - Background noir (conforme aux guidelines Apple)
 * - Texte blanc
 * - Logo Apple blanc
 * - Hauteur 50dp (standard Apple)
 */
@Composable
actual fun AppleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White,
            disabledContainerColor = Color.Black.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo Apple
            Image(
                painter = painterResource(Res.drawable.ic_apple_logo),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(Res.string.sign_in_with_apple),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
