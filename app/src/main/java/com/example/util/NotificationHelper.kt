package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.BillEntity

object NotificationHelper {
    private const val CHANNEL_ID = "channel_bills_due"
    private const val CHANNEL_NAME = "Lembretes de Vencimento"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificações para contas a vencer nos próximos 10 dias"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Checks bills and triggers a system notification if any bills are due within 10 days.
     */
    fun notifyDueBills(context: Context, dueBills: List<BillEntity>) {
        if (dueBills.isEmpty() || !hasNotificationPermission(context)) return
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = if (dueBills.size == 1) {
            "Conta próxima do vencimento!"
        } else {
            "${dueBills.size} contas vencem nos próximos 10 dias!"
        }
        val totalAmount = dueBills.sumOf { it.amount }
        val names = dueBills.take(3).joinToString(", ") { it.name }
        val contentText = "${CurrencyFormatter.format(totalAmount)} a pagar em breve: $names${if (dueBills.size > 3) " e outras" else ""}"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Atenção aos vencimentos nos próximos 10 dias:\n" +
                dueBills.joinToString("\n") { bill ->
                    "• ${bill.name}: ${CurrencyFormatter.format(bill.amount)} (${DateUtils.formatDueStatus(bill.dueDate, bill.isPaid)})"
                }
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Permission revoked
        }
    }
}
