package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.SuccessEmerald
import com.example.ui.viewmodel.MonthSummary
import com.example.util.CurrencyFormatter

@Composable
fun HeroSummaryCard(
    summary: MonthSummary,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val animatedProgress by animateFloatAsState(
        targetValue = summary.progressFraction,
        animationSpec = tween(durationMillis = 650),
        label = "hero_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .border(1.dp, colors.outline.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("hero_summary_card")
    ) {
        // Top caption & status badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RESUMO DO MÊS",
                color = colors.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceGroteskFamily,
                letterSpacing = 1.2.sp
            )

            if (summary.totalBillsCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(colors.surfaceVariant)
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${summary.paidBillsCount}/${summary.totalBillsCount} pagas",
                        color = colors.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = SpaceGroteskFamily
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Big numeric value
        Text(
            text = CurrencyFormatter.format(summary.totalMonth),
            color = colors.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            fontFamily = SpaceGroteskFamily,
            lineHeight = 32.sp,
            letterSpacing = (-0.5).sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("total_month_amount")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Clean breakdown: Total Pago & Falta Pagar in one elegant row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pago
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SuccessEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Pago",
                        color = colors.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontFamily = PlusJakartaSansFamily
                    )
                    Text(
                        text = CurrencyFormatter.format(summary.totalPaid),
                        color = SuccessEmerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGroteskFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // A pagar
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ElectricBlue)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Pendente",
                        color = colors.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontFamily = PlusJakartaSansFamily
                    )
                    Text(
                        text = CurrencyFormatter.format(summary.remainingToPay),
                        color = colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGroteskFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Integrated Slim Progress Bar if bills exist
        if (summary.totalBillsCount > 0) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progresso de pagamentos",
                    color = colors.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontFamily = PlusJakartaSansFamily
                )
                Text(
                    text = "${summary.progressPercent}%",
                    color = ElectricBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGroteskFamily
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(colors.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(100.dp))
                        .background(
                            if (summary.progressPercent == 100) SuccessEmerald else ElectricBlue
                        )
                )
            }
        }
    }
}
