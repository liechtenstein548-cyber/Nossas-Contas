package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SpaceGroteskFamily

enum class DockTab {
    HOME,
    CALENDAR,
    CATEGORIES,
    PROFILES
}

@Composable
fun FloatingDockBar(
    currentTab: DockTab,
    onSelectTab: (DockTab) -> Unit,
    onAddNewBill: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    // Outer container respects safe-area insets (navigationBarsPadding) so 3-button & gesture navs never overlap
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dimensions scaled up by 10% (1.10x) preserving visual harmony
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(100.dp),
                    spotColor = Color.Black.copy(alpha = 0.28f)
                )
                .clip(RoundedCornerShape(100.dp))
                .background(colors.surface)
                .border(1.dp, colors.outline.copy(alpha = 0.6f), RoundedCornerShape(100.dp))
                .padding(horizontal = 9.dp, vertical = 7.dp)
                .testTag("floating_dock_bar"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Home Pill (10% larger: icon 20dp, text 13sp, padding 15/11dp x 9dp)
            val isHome = currentTab == DockTab.HOME
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isHome) ElectricBlue else Color.Transparent)
                    .clickable { onSelectTab(DockTab.HOME) }
                    .padding(horizontal = if (isHome) 15.dp else 11.dp, vertical = 9.dp)
                    .testTag("dock_home_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Início",
                        tint = if (isHome) Color.White else colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    if (isHome) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Início",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGroteskFamily
                        )
                    }
                }
            }

            // Calendar / Month Switch Tab (42dp circle, 20dp icon)
            val isCal = currentTab == DockTab.CALENDAR
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isCal) colors.surfaceVariant else Color.Transparent)
                    .clickable { onSelectTab(DockTab.CALENDAR) }
                    .testTag("dock_calendar_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Mês",
                    tint = if (isCal) ElectricBlue else colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Categories Tab (42dp circle, 20dp icon)
            val isCat = currentTab == DockTab.CATEGORIES
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isCat) colors.surfaceVariant else Color.Transparent)
                    .clickable { onSelectTab(DockTab.CATEGORIES) }
                    .testTag("dock_categories_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = "Categorias",
                    tint = if (isCat) ElectricBlue else colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Profiles Tab (42dp circle, 20dp icon)
            val isProf = currentTab == DockTab.PROFILES
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isProf) colors.surfaceVariant else Color.Transparent)
                    .clickable { onSelectTab(DockTab.PROFILES) }
                    .testTag("dock_profiles_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = "Perfis",
                    tint = if (isProf) ElectricBlue else colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Prominent Action: "+ Nova Conta" circular button (44dp circle, 22dp icon)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(ElectricBlue)
                    .clickable { onAddNewBill() }
                    .testTag("dock_add_bill_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nova Conta",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
