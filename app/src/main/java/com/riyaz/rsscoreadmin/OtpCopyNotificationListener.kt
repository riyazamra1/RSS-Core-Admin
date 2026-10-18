package com.riyaz.rsscoreadmin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat

class OtpCopyNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        if (packageName !in setOf("com.google.android.gm", "com.microsoft.office.outlook", "com.samsung.android.email.provider")) return
        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = buildList {
            add(extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty())
            add(extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty())
            extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.forEach { add(it?.toString().orEmpty()) }
        }.joinToString(" ")
        val combined = "$title $text"
        if (!combined.contains("RSS Core", ignoreCase = true) && !combined.contains("RSC-", ignoreCase = true)) return
        val match = Regex("""RSC[- ]?(\d{3})[- ]?(\d{3})""", RegexOption.IGNORE_CASE).find(combined) ?: return
        val otp = "RSC-${match.groupValues[1]}-${match.groupValues[2]}"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "rss_core_otp_copy"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(NotificationChannel(channelId, "RSS Core OTP copy", NotificationManager.IMPORTANCE_HIGH))
        }
        val copyIntent = Intent(this, OtpCopyReceiver::class.java).setAction(OtpCopyReceiver.ACTION_COPY)
            .putExtra(OtpCopyReceiver.EXTRA_OTP, otp)
        val pending = PendingIntent.getBroadcast(
            this, otp.hashCode(), copyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        manager.notify(otp.hashCode(), NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("RSS Core verification code")
            .setContentText("$otp — tap Copy OTP to copy")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$otp\nUse Copy OTP to paste it into the sign-in screen."))
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_menu_set_as, "Copy OTP", pending)
            .setAutoCancel(true)
            .build())
    }
}
