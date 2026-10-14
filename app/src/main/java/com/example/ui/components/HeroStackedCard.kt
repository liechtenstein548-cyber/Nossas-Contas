package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BillEntity
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueDark
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.SpaceGroteskFamily
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
fun HeroStackedCard(
    bills: List<BillEntity>,
    onTogglePaid: (BillEntity) -> Unit,
    onClickEdit: (BillEntity) -> Unit,
    onAddNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingBills = bills.filter { !it.isPaid }
    val featuredBills = if (pendingBills.isNotEmpty()) pendingBills else bills
    var currentIndex by remember(featuredBills.size) { mutableIntStateOf(0) }

    val safeIndex = if (featuredBills.isNotEmpty()) {
        currentIndex.coerceIn(0, featuredBills.size - 1)
    } else 0
    val activeBill = featuredBills.getOrNull(safeIndex)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp, bottom = 6.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Stacked Card Layer 2 (Furthest back blur/shadow layer)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .height(24.dp)
                .offset(y = (-12).dp)
                .scale(0.96f)
                .clip(RoundedCornerShape(26.dp))
                .background(ElectricBlue.copy(alpha = 0.25f))
        )

        // Stacked Card Layer 1 (Middle layer just behind main card)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .height(30.dp)
                .offset(y = (-6).dp)
                .scale(0.98f)
                .clip(RoundedCornerShape(26.dp))
                .background(ElectricBlue.copy(alpha = 0.55f))
        )

        // Main Foreground Focal Card (Matches the vibrant Blue Card in Reference)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp), spotColor = ElectricBlue.copy(alpha = 0.35f))
                .clip(RoundedCornerShape(28.dp))
                .background(ElectricBlue)
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .testTag("hero_stacked_card")
        ) {
            if (activeBill != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Row: Avatar Stack on Left & Due Time Pill on Right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar pile + profile name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            AvatarPile(profileName = activeBill.profileName)

                            if (featuredBills.size > 1) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${safeIndex + 1}/${featuredBills.size}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SpaceGroteskFamily
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right Pill: Due Date / Time Status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = DateUtils.formatDueStatus(activeBill.dueDate, activeBill.isPaid),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SpaceGroteskFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Priority / Category Pill (Matching Reference White Pill) + Navigation arrows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = if (DateUtils.isOverdue(activeBill.dueDate, activeBill.isPaid)) Color(0xFFEF4444) else ElectricBlueDark,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (activeBill.category.isNotBlank()) activeBill.category else "Prioritária",
                                    color = if (DateUtils.isOverdue(activeBill.dueDate, activeBill.isPaid)) Color(0xFFEF4444) else ElectricBlueDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SpaceGroteskFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Navigation arrows if multiple cards
                        if (featuredBills.size > 1) {
                            Row {
                                IconButton(
                                    onClick = {
                                        currentIndex = if (currentIndex > 0) currentIndex - 1 else featuredBills.size - 1
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Anterior",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        currentIndex = if (currentIndex < featuredBills.size - 1) currentIndex + 1 else 0
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Próximo",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Card Title and Big Monetary Value (proportioned to never clip)
                    AnimatedContent(
                        targetState = activeBill,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "card_content_animation"
                    ) { bill ->
                        Column {
                            Text(
                                text = bill.name,
                                color = Color.White,
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SpaceGroteskFamily,
                                lineHeight = 26.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyFormatter.format(bill.amount),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = SpaceGroteskFamily,
                                letterSpacing = (-0.5).sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bottom Slider / Action Pill (Compact and Responsive)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Wide interactive pill button ("Toque para pagar >>>")
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(100.dp))
                                .clickable { onTogglePaid(activeBill) }
                                .padding(horizontal = 6.dp)
                                .testTag("hero_card_pay_slider"),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Circular checkmark emblem
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (activeBill.isPaid) Color(0xFF10B981) else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (activeBill.isPaid) Color.White else ElectricBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (activeBill.isPaid) "Conta Paga!" else "Marcar como pago",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PlusJakartaSansFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Floating Circular Pencil/Edit Button (Matching Reference)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable { onClickEdit(activeBill) }
                                .testTag("hero_card_edit_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar conta",
                                tint = ElectricBlue,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            } else {
                // Empty Hero Card (Encouraging state)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✨", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tudo em dia!",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGroteskFamily
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Nenhuma conta pendente para pagar agora.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontFamily = PlusJakartaSansFamily
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.White)
                            .clickable { onAddNew() }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = "+ Adicionar Nova Conta",
                            color = ElectricBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGroteskFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarPile(profileName: String) {
    val avatars = listOf(
        Color(0xFFFBBF24) to "👤",
        Color(0xFF38BDF8) to "⭐",
        Color(0xFFEC4899) to "💙"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(62.dp)
                .height(26.dp)
        ) {
            avatars.forEachIndexed { i, (bgColor, emoji) ->
                Box(
                    modifier = Modifier
                        .offset(x = (i * 18).dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(bgColor)
                        .border(1.5.dp, ElectricBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 11.sp)
                }
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = profileName.ifEmpty { "Conjunto" },
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PlusJakartaSansFamily,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
