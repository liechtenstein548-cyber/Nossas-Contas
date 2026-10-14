package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProfileEntity
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.OnAccentBlue

@Composable
fun ProfileManagerDialog(
    profiles: List<ProfileEntity>,
    onDismiss: () -> Unit,
    onCreateProfile: (name: String, colorHex: String) -> Unit,
    onDeleteProfile: (ProfileEntity) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var newProfileName by remember { mutableStateOf("") }
    val colorPresets = listOf(
        // Tons de Azul
        "#2563EB", // Royal Blue
        "#1D4ED8", // Azul Escuro
        "#0284C7", // Azul Oceano
        "#38BDF8", // Cyan / Céu
        "#60A5FA", // Azul Claro
        // Tons de Verde
        "#10B981", // Esmeralda
        "#059669", // Verde Floresta
        "#16A34A", // Verde Folha
        "#84CC16", // Verde Limão
        // Tons de Amarelo e Dourado
        "#F59E0B", // Âmbar
        "#EAB308", // Amarelo Ouro
        "#FACC15", // Amarelo Claro
        // Tons de Laranja
        "#F97316", // Laranja Vivo
        "#EA580C", // Laranja Queimado
        "#FB923C", // Laranja Pêssego
        // Tons de Vermelho
        "#EF4444", // Vermelho Coral
        "#DC2626", // Vermelho Rubi
        "#B91C1C", // Vermelho Carmesim
        // Tons de Roxo e Violeta
        "#6366F1", // Indigo
        "#8B5CF6", // Violeta
        "#7C3AED", // Roxo Real
        "#A855F7", // Púrpura
        "#C084FC", // Lavanda
        // Tons de Rosa e Magenta
        "#EC4899", // Rosa Pink
        "#DB2777", // Magenta
        "#F43F5E", // Framboesa
        "#F472B6", // Rosa Pastel
        // Tons de Turquesa e Teal
        "#14B8A6", // Turquesa
        "#0D9488", // Teal Profundo
        "#06B6D4", // Ciano Vivo
        // Tons Neutros e Ardósia
        "#475569", // Ardósia
        "#64748B", // Cinza Azulado
        "#78716C", // Pedra Quente
        "#334155"  // Carvão
    )
    var selectedColor by remember { mutableStateOf(colorPresets.first()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var profileToDeleteConfirm by remember { mutableStateOf<ProfileEntity?>(null) }

    val dismissWithKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = dismissWithKeyboard,
        containerColor = colors.surface,
        modifier = Modifier.testTag("profile_manager_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perfis de Usuário",
                    color = colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = dismissWithKeyboard, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Crie perfis para dividir despesas ou acompanhar quem paga cada conta:",
                    color = colors.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Existing profiles list
                Text(
                    text = "PERFIS ATIVOS",
                    color = colors.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profiles.forEach { profile ->
                        val pColor = try {
                            Color(android.graphics.Color.parseColor(profile.colorHex))
                        } catch (e: Exception) {
                            colors.primary
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfaceVariant)
                                .border(1.dp, colors.outline, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(pColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = profile.name,
                                    color = colors.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (profiles.size > 1) {
                                IconButton(
                                    onClick = { profileToDeleteConfirm = profile },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir perfil",
                                        tint = ErrorRose.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // New Profile Form
                Text(
                    text = "CRIAR NOVO PERFIL",
                    color = colors.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newProfileName,
                    onValueChange = {
                        newProfileName = it
                        errorMessage = null
                    },
                    placeholder = { Text("Nome (ex: João, Maria, Casa)", color = colors.onSurfaceVariant) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedContainerColor = colors.surfaceVariant,
                        unfocusedContainerColor = colors.surfaceVariant,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline,
                        cursorColor = colors.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_profile_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Color presets
                Text(
                    text = "Cor de identificação",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(colorPresets) { hex ->
                        val isSelected = selectedColor == hex
                        val c = Color(android.graphics.Color.parseColor(hex))
                        val isLight = try {
                            val parsed = android.graphics.Color.parseColor(hex)
                            (0.299 * android.graphics.Color.red(parsed) +
                             0.587 * android.graphics.Color.green(parsed) +
                             0.114 * android.graphics.Color.blue(parsed)) > 150
                        } catch (e: Exception) {
                            false
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) colors.onSurface else colors.outline.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selecionada",
                                    tint = if (isLight) Color(0xFF1E293B) else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = ErrorRose,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (newProfileName.isBlank()) {
                            errorMessage = "Digite um nome para o perfil."
                            return@Button
                        }
                        onCreateProfile(newProfileName.trim(), selectedColor)
                        newProfileName = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = OnAccentBlue
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("create_profile_button")
                ) {
                    Text(
                        text = "+ Adicionar perfil",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {}
    )

    profileToDeleteConfirm?.let { profile ->
        AlertDialog(
            onDismissRequest = { profileToDeleteConfirm = null },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Excluir Perfil",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colors.onSurface
                )
            },
            text = {
                Text(
                    text = "Deseja realmente excluir o perfil \"${profile.name}\"?",
                    color = colors.onSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = profile
                        profileToDeleteConfirm = null
                        onDeleteProfile(target)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRose)
                ) {
                    Text("Excluir", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { profileToDeleteConfirm = null }
                ) {
                    Text("Cancelar", color = colors.onSurfaceVariant)
                }
            }
        )
    }
}
