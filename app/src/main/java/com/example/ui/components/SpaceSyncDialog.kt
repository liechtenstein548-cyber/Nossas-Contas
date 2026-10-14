package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firestore.SpaceDocument
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.SuccessEmerald

@Composable
fun SpaceSyncDialog(
    space: SpaceDocument?,
    onDismiss: () -> Unit,
    onJoinSpace: (String) -> Unit,
    onSignOut: () -> Unit,
    isJoining: Boolean,
    joinError: String?
) {
    val context = LocalContext.current
    var inputCode by remember { mutableStateOf("") }
    val colors = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("space_sync_dialog"),
        containerColor = colors.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (space != null) Icons.Default.CloudDone else Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = if (space != null) "Sincronização em Nuvem" else "Modo de Armazenamento",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGroteskFamily
                    ),
                    color = colors.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (space != null) {
                    Text(
                        text = "Suas contas e despesas são sincronizadas automaticamente em tempo real pela sua Conta Google.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = PlusJakartaSansFamily),
                        color = colors.onSurfaceVariant
                    )

                    // Current Space Card & Code
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, colors.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ESPAÇO ATUAL",
                                    fontSize = 10.sp,
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurfaceVariant
                                )
                                Text(
                                    text = space.name,
                                    fontSize = 15.sp,
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = SuccessEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "${space.memberUids.size} membro(s)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessEmerald
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Código de convite:",
                            fontSize = 11.sp,
                            color = colors.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surface)
                                .border(1.dp, colors.outline, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = space.code,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 4.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = ElectricBlue
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Código do Espaço", space.code)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Código copiado: ${space.code}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar código",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    // Connect to another space section
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Entrar no espaço de outra pessoa:",
                            fontSize = 12.sp,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inputCode,
                                onValueChange = { inputCode = it.take(8).uppercase() },
                                placeholder = { Text("Código (ex: 123456)") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("join_code_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricBlue,
                                    unfocusedBorderColor = colors.outline
                                )
                            )
                            Button(
                                onClick = {
                                    if (inputCode.isNotBlank()) {
                                        onJoinSpace(inputCode.trim())
                                    }
                                },
                                enabled = inputCode.isNotBlank() && !isJoining,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                modifier = Modifier.testTag("join_space_button")
                            ) {
                                if (isJoining) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Entrar", fontSize = 12.sp)
                                }
                            }
                        }
                        if (joinError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = joinError,
                                fontSize = 11.sp,
                                color = colors.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = onSignOut,
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.error)
                    ) {
                        Text("Sair da Conta Google", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Local / Guest mode explanation card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, colors.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "MODO LOCAL ATIVO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGroteskFamily,
                            color = ElectricBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Suas contas e categorias estão salvas no armazenamento deste aparelho.",
                            fontSize = 13.sp,
                            color = colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onSignOut,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Fazer Login com Google", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_space_dialog_button")
            ) {
                Text("Fechar", color = ElectricBlue)
            }
        }
    )
}
