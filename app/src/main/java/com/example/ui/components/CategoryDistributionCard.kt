package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.viewmodel.CategoryExpense
import com.example.util.CurrencyFormatter

@Composable
fun CategoryDistributionCard(
    categoryExpenses: List<CategoryExpense>,
    onManageCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (categoryExpenses.isEmpty()) return
    val colors = MaterialTheme.colorScheme
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface)
            .border(1.dp, colors.outline.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("category_distribution_card")
    ) {
        // Header with Title, Expand arrow, and "+ Categorias" button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Column {
                    Text(
                        text = "Gastos por Categoria",
                        color = colors.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGroteskFamily
                    )
                    Text(
                        text = if (isExpanded) "Ranking do maior para o menor" else "${categoryExpenses.size} categorias • Toque para ver",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontFamily = PlusJakartaSansFamily
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, colors.outline.copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                        .clickable { onManageCategories() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("header_add_category_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Categorias",
                            color = ElectricBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGroteskFamily
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Recolher categorias" else "Expandir categorias",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Expandable category items list
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                categoryExpenses.forEach { cat ->
                    val animatedPercentage by animateFloatAsState(
                        targetValue = cat.percentageOfTotal,
                        animationSpec = tween(durationMillis = 500),
                        label = "cat_pct_${cat.name}"
                    )
                    val percentInt = (cat.percentageOfTotal * 100).toInt()

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(ElectricBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.name,
                                    color = colors.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = PlusJakartaSansFamily
                                )
                                Text(
                                    text = " (${cat.count})",
                                    color = colors.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontFamily = PlusJakartaSansFamily
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = CurrencyFormatter.format(cat.totalAmount),
                                    color = colors.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SpaceGroteskFamily
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$percentInt%",
                                    color = colors.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = SpaceGroteskFamily
                                )
                            }
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
                                    .fillMaxWidth(animatedPercentage.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(ElectricBlue)
                            )
                        }
                    }
                }
            }
        }
    }
}
