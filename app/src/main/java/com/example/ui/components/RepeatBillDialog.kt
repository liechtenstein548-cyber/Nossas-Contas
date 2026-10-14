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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.BillEntity
import com.example.ui.theme.OnAccentBlue
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
fun RepeatBillDialog(
    bill: BillEntity,
    onConfirm: (monthsCount: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var selectedMonths by remember { mutableIntStateOf(3) }
    val options = listOf(1, 2, 3, 6, 12)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, colors.outline, RoundedCornerShape(24.dp))
                .testTag("repeat_bill_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header: Icon + Title + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Repetir Conta",
                            color = colors.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bill details preview card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, colors.outline.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = bill.name,
                        color = colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Valor: ${CurrencyFormatter.format(bill.amount)}",
                            color = colors.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Venc: ${DateUtils.formatDueDateShort(bill.dueDate)}",
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Repetir nos próximos:",
                    color = colors.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Quick chips for number of months
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { count ->
                        val isSelected = selectedMonths == count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) colors.primary else colors.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) colors.primary else colors.outline,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMonths = count }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (count == 1) "1 mês" else "$count m",
                                color = if (isSelected) OnAccentBlue else colors.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "A conta será duplicada para os próximos $selectedMonths meses com o mesmo valor e dia de vencimento, com status pendente.",
                    color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Actions: Confirm & Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            color = colors.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { onConfirm(selectedMonths) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = OnAccentBlue
                        ),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(46.dp)
                            .testTag("confirm_repeat_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Replicar ($selectedMonths)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
