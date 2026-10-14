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
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
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
import com.example.data.local.BillEntity
import com.example.ui.theme.ErrorRose
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
fun DueSoonAlertBanner(
    dueBills: List<BillEntity>,
    onTriggerNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (dueBills.isEmpty()) return
    val colors = MaterialTheme.colorScheme
    val totalDueAmount = dueBills.sumOf { it.amount }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.errorContainer.copy(alpha = 0.4f))
            .border(1.dp, ErrorRose.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(16.dp)
            .testTag("due_soon_alert_banner")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ErrorRose.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = ErrorRose,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (dueBills.size == 1) "1 conta vence em até 10 dias" else "${dueBills.size} contas vencem em até 10 dias",
                        color = colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total a pagar: ${CurrencyFormatter.format(totalDueAmount)}",
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            // Notification trigger button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.outline, RoundedCornerShape(100.dp))
                    .clickable { onTriggerNotification() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("send_notification_test_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Enviar lembrete",
                        tint = colors.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Alertar",
                        color = colors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // First 2 bill items chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            dueBills.take(2).forEach { bill ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${bill.name} • ${DateUtils.formatDueStatus(bill.dueDate, bill.isPaid)}",
                        color = colors.onSurface,
                        fontSize = 11.sp
                    )
                }
            }
            if (dueBills.size > 2) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "+${dueBills.size - 2}",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
