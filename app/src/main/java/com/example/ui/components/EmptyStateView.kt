package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OnAccentBlue

@Composable
fun EmptyStateView(
    onAddBill: () -> Unit,
    onCopyPreviousMonth: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.outline.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .testTag("empty_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(colors.surfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ReceiptLong,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Nenhuma conta cadastrada neste mês.",
            color = colors.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onAddBill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = OnAccentBlue
                ),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .height(40.dp)
                    .testTag("empty_add_bill_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Nova conta",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (onCopyPreviousMonth != null) {
                OutlinedButton(
                    onClick = onCopyPreviousMonth,
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("copy_prev_month_empty_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = colors.primary
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Copiar mês anterior",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary
                    )
                }
            }
        }
    }
}
