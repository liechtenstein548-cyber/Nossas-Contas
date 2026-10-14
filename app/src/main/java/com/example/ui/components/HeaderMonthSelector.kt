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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBlue
import com.example.util.DateUtils
import java.time.YearMonth

@Composable
fun HeaderMonthSelector(
    currentYearMonth: YearMonth,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onCheckNotifications: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToCurrentMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentMonth = currentYearMonth == YearMonth.now()
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Clean Top Bar: Title on left, Action icons on right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Minhas Contas",
                    color = colors.onBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.testTag("app_title")
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Controle Mensal & Finanças",
                    color = colors.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Compact Action Icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Notifications check button
                IconButton(
                    onClick = onCheckNotifications,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                        .testTag("check_notifications_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Verificar lembretes",
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Theme toggle button
                IconButton(
                    onClick = onToggleDarkMode,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDarkMode) "Mudar para modo claro" else "Mudar para modo escuro",
                        tint = if (isDarkMode) AccentBlue else colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Clean Month Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.outline.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("prev_month_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Mês anterior",
                    tint = colors.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = DateUtils.formatMonthYearHeader(currentYearMonth),
                    color = colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("current_month_label")
                )

                if (!isCurrentMonth) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(colors.primary.copy(alpha = 0.14f))
                            .clickable { onJumpToCurrentMonth() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("jump_today_button")
                    ) {
                        Text(
                            text = "Hoje",
                            color = colors.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("next_month_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Próximo mês",
                    tint = colors.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
