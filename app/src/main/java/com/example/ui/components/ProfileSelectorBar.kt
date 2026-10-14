package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProfileEntity
import com.example.ui.theme.OnAccentBlue

@Composable
fun ProfileSelectorBar(
    selectedProfile: String,
    profiles: List<ProfileEntity>,
    onSelectProfile: (String) -> Unit,
    onManageProfiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Todos" chip
        val isTodos = selectedProfile == "Todos"
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(if (isTodos) colors.primary else colors.surface)
                .border(
                    1.dp,
                    if (isTodos) colors.primary else colors.outline.copy(alpha = 0.7f),
                    RoundedCornerShape(100.dp)
                )
                .clickable { onSelectProfile("Todos") }
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("profile_filter_Todos")
        ) {
            Text(
                text = "Todos",
                color = if (isTodos) OnAccentBlue else colors.onSurface,
                fontSize = 12.sp,
                fontWeight = if (isTodos) FontWeight.Bold else FontWeight.Medium
            )
        }

        // Each individual profile
        profiles.forEach { profile ->
            val isSelected = selectedProfile == profile.name
            val profileColor = try {
                Color(android.graphics.Color.parseColor(profile.colorHex))
            } catch (e: Exception) {
                colors.primary
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isSelected) colors.primary else colors.surface)
                    .border(
                        1.dp,
                        if (isSelected) colors.primary else colors.outline.copy(alpha = 0.7f),
                        RoundedCornerShape(100.dp)
                    )
                    .clickable { onSelectProfile(profile.name) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("profile_filter_${profile.name}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) OnAccentBlue else profileColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = profile.name,
                        color = if (isSelected) OnAccentBlue else colors.onSurface,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Add / Manage profiles button chip
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.6f))
                .border(1.dp, colors.outline.copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                .clickable { onManageProfiles() }
                .padding(horizontal = 10.dp, vertical = 7.dp)
                .testTag("manage_profiles_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Gerenciar perfis",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Perfis",
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
