package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.SpaceGroteskFamily

@Composable
fun PillFilterRow(
    selectedTab: String,
    totalCount: Int,
    pendingCount: Int,
    paidCount: Int,
    onSelectTab: (String) -> Unit,
    selectedProfile: String,
    profiles: List<ProfileEntity>,
    onSelectProfile: (String) -> Unit,
    onManageProfiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Primary Status Pills (Matching Reference "All 6", "Today's tasks 3", etc.)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                "Todas" to totalCount,
                "Pendentes" to pendingCount,
                "Pagas" to paidCount
            )

            tabs.forEach { (tabName, count) ->
                val isSelected = selectedTab == tabName
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isSelected) ElectricBlue else colors.surface)
                        .border(
                            1.dp,
                            if (isSelected) ElectricBlue else colors.outline.copy(alpha = 0.6f),
                            RoundedCornerShape(100.dp)
                        )
                        .clickable { onSelectTab(tabName) }
                        .padding(horizontal = 18.dp, vertical = 11.dp)
                        .testTag("filter_pill_$tabName"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$tabName $count",
                        color = if (isSelected) Color.White else colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontFamily = SpaceGroteskFamily
                    )
                }
            }
        }

        // Row 2: Secondary Profile Filter Sub-bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Todos" profile
            val isAllProfiles = selectedProfile == "Todos"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isAllProfiles) colors.primary.copy(alpha = 0.14f) else colors.surface.copy(alpha = 0.6f))
                    .border(
                        1.dp,
                        if (isAllProfiles) colors.primary else colors.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(100.dp)
                    )
                    .clickable { onSelectProfile("Todos") }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Todos perfis",
                    color = if (isAllProfiles) colors.primary else colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PlusJakartaSansFamily
                )
            }

            profiles.forEach { p ->
                val isSelected = selectedProfile == p.name
                val pColor = try {
                    Color(android.graphics.Color.parseColor(p.colorHex))
                } catch (e: Exception) {
                    ElectricBlue
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isSelected) colors.primary.copy(alpha = 0.14f) else colors.surface.copy(alpha = 0.6f))
                        .border(
                            1.dp,
                            if (isSelected) colors.primary else colors.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(100.dp)
                        )
                        .clickable { onSelectProfile(p.name) }
                        .padding(horizontal = 11.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(pColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = p.name,
                            color = if (isSelected) colors.primary else colors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = PlusJakartaSansFamily
                        )
                    }
                }
            }

            // Quick add profile
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(colors.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { onManageProfiles() }
                    .padding(horizontal = 9.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Perfis",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
            }
        }
    }
}
