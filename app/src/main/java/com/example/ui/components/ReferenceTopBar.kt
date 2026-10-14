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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.SpaceGroteskFamily
import com.example.util.DateUtils
import java.time.YearMonth

@Composable
fun ReferenceTopBar(
    currentYearMonth: YearMonth,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onOpenMonthPicker: () -> Unit,
    onCheckNotifications: () -> Unit,
    onOpenSpaceSync: () -> Unit = {},
    hasDueSoon: Boolean,
    userName: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        // Top Row: Avatar + Name on Left (Flexible), Action circles on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Profile Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { onOpenSpaceSync() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                // Circular Avatar
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                        )
                        .border(
                            1.5.dp,
                            ElectricBlue.copy(alpha = 0.5f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💳", fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (!userName.isNullOrBlank()) userName.trim() else "Nossas Contas",
                            color = colors.onBackground,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGroteskFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("topbar_user_name")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verificado",
                            tint = ElectricBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = DateUtils.formatMonthYearHeader(currentYearMonth),
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PlusJakartaSansFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Action Circular Buttons (36dp for guaranteed fit on all phones)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Month selector button
                IconButton(
                    onClick = onOpenMonthPicker,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.outline.copy(alpha = 0.6f), CircleShape)
                        .testTag("topbar_month_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Mês",
                        tint = colors.onSurface,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Theme Toggle Button (Light/Dark Mode)
                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.outline.copy(alpha = 0.6f), CircleShape)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDarkMode) "Mudar para modo claro" else "Mudar para modo escuro",
                        tint = if (isDarkMode) ElectricBlue else colors.onSurface,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Notification Bell
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = onCheckNotifications,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.outline.copy(alpha = 0.6f), CircleShape)
                            .testTag("topbar_bell_button")
                    ) {
                        Icon(
                            imageVector = if (hasDueSoon) Icons.Default.Notifications else Icons.Outlined.Notifications,
                            contentDescription = "Notificações",
                            tint = if (hasDueSoon) ElectricBlue else colors.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (hasDueSoon) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp, end = 2.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .border(1.dp, colors.surface, CircleShape)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Editorial Greeting (Proportioned to avoid overflow)
        Column {
            val greeting = if (!userName.isNullOrBlank()) {
                "Olá, ${userName.trim()}!"
            } else {
                "Olá!"
            }
            Text(
                text = greeting,
                color = colors.onBackground,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceGroteskFamily,
                letterSpacing = (-0.6).sp,
                lineHeight = 28.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Tudo sob controle este mês.",
                color = colors.onSurfaceVariant,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PlusJakartaSansFamily,
                letterSpacing = (-0.3).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
