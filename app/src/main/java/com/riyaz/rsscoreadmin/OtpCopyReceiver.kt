package com.riyaz.rsscoreadmin

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast

class OtpCopyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_COPY) return
        val otp = intent.getStringExtra(EXTRA_OTP)?.takeIf { it.matches(Regex("""RSC-\d{3}-\d{3}""")) } ?: return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("RSS Core OTP", otp))
        Toast.makeText(context, "RSS Core OTP copied", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val ACTION_COPY = "com.riyaz.rsscoreadmin.COPY_OTP"
        const val EXTRA_OTP = "rss_core_otp"
    }
}
