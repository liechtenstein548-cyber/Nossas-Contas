package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.SpaceGroteskFamily
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun SignInScreen(
    onAuthSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💳", fontSize = 38.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Nossas Contas",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGroteskFamily,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Controle financeiro mensal sincronizado na nuvem e com acesso offline",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PlusJakartaSansFamily),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Feature Highlights Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureRow(
                    icon = Icons.Default.CloudSync,
                    title = "Sincronização em Nuvem",
                    desc = "Acesse suas contas em qualquer aparelho"
                )
                FeatureRow(
                    icon = Icons.Default.Group,
                    title = "Compartilhar Despesas",
                    desc = "Divida e acompanhe contas com seu parceiro(a)"
                )
                FeatureRow(
                    icon = Icons.Default.Smartphone,
                    title = "Modo Offline Disponível",
                    desc = "Funciona mesmo sem internet com armazenamento local"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = PlusJakartaSansFamily),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Google Sign-In Button
            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    launchGoogleSignIn(
                        context = context,
                        credentialManager = credentialManager,
                        scope = coroutineScope,
                        onSuccess = {
                            isLoading = false
                            onAuthSuccess()
                        },
                        onError = { error ->
                            isLoading = false
                            errorMessage = error
                        },
                        onCancelled = {
                            isLoading = false
                        }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_sign_in_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                fontWeight = FontWeight.Black,
                                color = ElectricBlue,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Entrar com o Google",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = SpaceGroteskFamily
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Continue as Guest (Modo Local) Button
            OutlinedButton(
                onClick = onContinueAsGuest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("continue_as_guest_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Smartphone,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continuar como Visitante (Modo Local)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PlusJakartaSansFamily
                    )
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ElectricBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGroteskFamily
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = PlusJakartaSansFamily),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun findActivity(context: Context): Activity? {
    var current = context
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

private fun launchGoogleSignIn(
    context: Context,
    credentialManager: CredentialManager,
    scope: CoroutineScope,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onCancelled: () -> Unit
) {
    val clientId = try {
        val id = context.getString(R.string.default_web_client_id)
        if (id.isNotBlank()) id else "255411802371-c2tg0qfpip4vhb2tr054thqtbuihcb67.apps.googleusercontent.com"
    } catch (e: Exception) {
        "255411802371-c2tg0qfpip4vhb2tr054thqtbuihcb67.apps.googleusercontent.com"
    }

    val activity = findActivity(context)
    if (activity == null) {
        onError("Não foi possível acessar a Activity principal.")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val result = withTimeoutOrNull(10000L) {
                credentialManager.getCredential(activity, request)
            }
            if (result == null) {
                onError("Nenhuma conta Google respondeu no aparelho. Use o 'Modo Visitante' se estiver no emulador sem conta Google!")
                return@launch
            }

            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                onSuccess()
            } else {
                onError("Tipo de credencial incompatível.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In cancelado: ${e.message}", e)
            onError("Login cancelado. Você pode entrar como Visitante se preferir!")
        } catch (e: Exception) {
            Log.e("Auth", "Falha no Google Sign-In", e)
            val msg = e.localizedMessage ?: ""
            if (msg.contains("No credentials", ignoreCase = true) || msg.contains("16", ignoreCase = true)) {
                onError("Nenhuma conta Google configurada neste dispositivo. Entre como Visitante para usar agora!")
            } else {
                onError("Falha na autenticação Google: $msg")
            }
        }
    }
}
