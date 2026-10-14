package com.example.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.local.BillEntity
import com.example.ui.theme.ErrorRose

@Composable
fun DeleteConfirmDialog(
    bill: BillEntity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.onSurface,
        textContentColor = colors.onSurfaceVariant,
        title = {
            Text(
                text = "Excluir conta",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = "Deseja realmente excluir \"${bill.name}\"? Esta ação não poderá ser desfeita.",
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = ErrorRose),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Text(
                    text = "Excluir",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = colors.onSurfaceVariant)
            ) {
                Text(text = "Cancelar")
            }
        }
    )
}
