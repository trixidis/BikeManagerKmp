package com.bikemanager.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bikemanager.presentation.auth.AuthUiState
import com.bikemanager.presentation.auth.AuthViewModelMvi
import com.bikemanager.ui.Strings
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import org.koin.compose.koinInject

@Composable
fun LoginScreenContent(
    viewModel: AuthViewModelMvi = koinInject(),
    onSignedIn: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate when authenticated
    when (uiState) {
        is AuthUiState.Authenticated -> {
            onSignedIn()
        }
        else -> {}
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App title
            Text(
                text = Strings.APP_NAME,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Gérez l'entretien de vos motos",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(64.dp))

            when (uiState) {
                is AuthUiState.Loading, is AuthUiState.Checking -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }

                is AuthUiState.Error -> {
                    Text(
                        text = (uiState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GoogleSignInButtonContent(
                        onSuccess = {
                            // KMPAuth has signed in to Firebase, refresh auth state
                            viewModel.checkAuthState()
                        },
                        onError = { error ->
                            viewModel.setError(error)
                        }
                    )
                }

                is AuthUiState.NotAuthenticated -> {
                    GoogleSignInButtonContent(
                        onSuccess = {
                            // KMPAuth has signed in to Firebase, refresh auth state
                            viewModel.checkAuthState()
                        },
                        onError = { error ->
                            viewModel.setError(error)
                        }
                    )
                }

                is AuthUiState.Authenticated -> {
                    // Will navigate via the effect above
                }
            }
        }
    }
}

@Composable
private fun GoogleSignInButtonContent(
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    GoogleButtonUiContainerFirebase(
        onResult = { result ->
            result.fold(
                onSuccess = { firebaseUser ->
                    // KMPAuth has signed the user in to Firebase
                    if (firebaseUser != null) {
                        onSuccess()
                    } else {
                        onError("Utilisateur non trouvé")
                    }
                },
                onFailure = { exception ->
                    onError(exception.message ?: "Erreur de connexion Google")
                }
            )
        }
    ) {
        GoogleSignInButton(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            text = "Se connecter avec Google"
        ) {
            this.onClick()
        }
    }
}
