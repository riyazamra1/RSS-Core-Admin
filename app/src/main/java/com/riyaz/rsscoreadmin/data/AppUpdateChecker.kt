package com.riyaz.rsscoreadmin.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AppUpdate(
    val version: String,
    val downloadUrl: String,
    val releaseNotes: String
)

object AppUpdateChecker {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .callTimeout(12, TimeUnit.SECONDS)
        .build()

    suspend fun check(currentVersion: String): AppUpdate? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://api.github.com/repos/riyazamra1/RSS-Core-Admin/releases/latest")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .build()
        client.newCall(request).execute().use { response ->
            if (response.code == 404) return@withContext null // No published release yet.
            if (!response.isSuccessful) return@withContext null
            val json = JSONObject(response.body?.string().orEmpty())
            if (json.optBoolean("draft", true) || json.optBoolean("prerelease", true)) return@withContext null
            val tag = json.optString("tag_name").removePrefix("v").removePrefix("V")
            if (tag.isBlank() || compareVersions(tag, currentVersion) <= 0) return@withContext null
            val assets = json.optJSONArray("assets") ?: return@withContext null
            var apkUrl: String? = null
            for (i in 0 until assets.length()) {
                val asset = assets.optJSONObject(i) ?: continue
                if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.optString("browser_download_url").takeIf { it.startsWith("https://") }
                    if (apkUrl != null) break
                }
            }
            val url = apkUrl ?: return@withContext null
            AppUpdate(tag, url, json.optString("body").take(1200))
        }
    }

    private fun compareVersions(a: String, b: String): Int {
        val left = a.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        val right = b.substringBefore('-').split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(left.size, right.size)) {
            val diff = (left.getOrElse(i) { 0 }).compareTo(right.getOrElse(i) { 0 })
            if (diff != 0) return diff
        }
        return 0
    }
}
