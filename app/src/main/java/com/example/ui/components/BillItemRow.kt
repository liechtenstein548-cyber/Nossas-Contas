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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BillEntity
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.RosePriority
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.theme.SuccessEmerald
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils

@Composable
fun BillItemRow(
    bill: BillEntity,
    onTogglePaid: (BillEntity) -> Unit,
    onClickEdit: (BillEntity) -> Unit,
    onRepeatBill: (BillEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val isOverdue = DateUtils.isOverdue(bill.dueDate, bill.isPaid)
    val isDueSoon = DateUtils.isDueWithin10Days(bill.dueDate, bill.isPaid)
    val statusText = DateUtils.formatDueStatus(bill.dueDate, bill.isPaid)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = if (bill.isPaid) colors.outline.copy(alpha = 0.35f)
                else if (isOverdue) RosePriority.copy(alpha = 0.35f)
                else if (isDueSoon) ElectricBlue.copy(alpha = 0.3f)
                else colors.outline.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClickEdit(bill) }
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("bill_item_${bill.id}")
    ) {
        // Line 1: Checkbox + Name + Amount
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Status Checkbox (Minimum 48dp touch target)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onTogglePaid(bill) }
                    .testTag("toggle_paid_${bill.id}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (bill.isPaid) SuccessEmerald else Color.Transparent)
                        .border(
                            width = 1.8.dp,
                            color = if (bill.isPaid) SuccessEmerald else colors.onSurfaceVariant.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (bill.isPaid) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Paga",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Bill Title & Recurring tag
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = bill.name,
                    color = if (bill.isPaid) colors.onSurfaceVariant.copy(alpha = 0.65f) else colors.onSurface,
                    fontSize = 15.sp,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (bill.isPaid) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (bill.isRecurring) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricBlue.copy(alpha = 0.12f))
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = "Fixa",
                            color = ElectricBlue,
                            fontSize = 9.sp,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            Text(
                text = CurrencyFormatter.format(bill.amount),
                color = if (bill.isPaid) colors.onSurfaceVariant.copy(alpha = 0.6f) else colors.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = SpaceGroteskFamily,
                textDecoration = if (bill.isPaid) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Line 2: Tags (Category, Profile, Due Date) & Compact Action Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp), // Aligned with the text above
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badges row
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Category Capsule
                if (bill.category.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(colors.surfaceVariant)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = bill.category,
                            color = colors.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Profile Capsule
                if (bill.profileName.isNotBlank() && bill.profileName != "Todos") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(colors.surfaceVariant)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = bill.profileName,
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Due status
                Text(
                    text = statusText,
                    color = when {
                        bill.isPaid -> SuccessEmerald
                        isOverdue -> RosePriority
                        isDueSoon -> ElectricBlue
                        else -> colors.onSurfaceVariant
                    },
                    fontSize = 10.sp,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = if (isOverdue || isDueSoon) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick actions on right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = { onRepeatBill(bill) },
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("repeat_bill_action_${bill.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repetir conta",
                        tint = colors.onSurfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = { onClickEdit(bill) },
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("edit_bill_${bill.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Editar conta",
                        tint = colors.onSurfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
