package com.riyaz.rsscoreadmin.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Only uses RSS Core endpoints verified in the inspected source contract. */
class RssCoreApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun checkEndpoint(endpoint: String): EndpointHealth = withContext(Dispatchers.IO) {
        val started = System.nanoTime()
        try {
            require(endpoint.startsWith("https://")) { "HTTPS is required" }
            val request = Request.Builder().url(endpoint.trimEnd('/') + "/health").get().build()
            client.newCall(request).execute().use { response ->
                val elapsed = (System.nanoTime() - started) / 1_000_000
                val body = response.body?.string().orEmpty()
                val service = runCatching { JSONObject(body).optString("service") }.getOrNull()
                val state = when {
                    response.isSuccessful && service == "rss-core" -> HealthState.HEALTHY
                    response.code in 500..599 -> HealthState.DEGRADED
                    else -> HealthState.DEGRADED
                }
                EndpointHealth(endpoint, state, response.code, elapsed, System.currentTimeMillis().toString(), serviceIdentity = service)
            }
        } catch (t: Throwable) {
            EndpointHealth(endpoint, HealthState.OFFLINE, responseMs = (System.nanoTime() - started) / 1_000_000, checkedAt = System.currentTimeMillis().toString(), errorCategory = t.javaClass.simpleName)
        }
    }

    suspend fun fetchStatus(endpoint: String): CoreStatusResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(endpoint.trimEnd('/') + "/api/v1/status").get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext CoreStatusResult.Unavailable("HTTP ${response.code}")
                val json = JSONObject(response.body?.string().orEmpty())
                CoreStatusResult.Available(
                    json.optString("version").takeIf { it.isNotBlank() },
                    json.optString("runtime").takeIf { it.isNotBlank() },
                    json.optString("storage").takeIf { it.isNotBlank() },
                    json.optString("ai").takeIf { it.isNotBlank() }
                )
            }
        } catch (t: Throwable) {
            CoreStatusResult.Unavailable(t.javaClass.simpleName)
        }
    }

    suspend fun fetchProjects(endpoint: String): CoreProjectsResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(endpoint.trimEnd('/') + "/api/v1/projects").get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext CoreProjectsResult.Unavailable("HTTP ${response.code}")
                val json = JSONObject(response.body?.string().orEmpty())
                val array = json.optJSONArray("projects")
                CoreProjectsResult.Available(buildList {
                    if (array != null) for (i in 0 until array.length()) add(array.optString(i))
                })
            }
        } catch (t: Throwable) {
            CoreProjectsResult.Unavailable(t.javaClass.simpleName)
        }
    }
}

sealed interface CoreStatusResult {
    data class Available(val version: String?, val runtime: String?, val storage: String?, val ai: String?) : CoreStatusResult
    data class Unavailable(val reason: String) : CoreStatusResult
}

sealed interface CoreProjectsResult {
    data class Available(val projects: List<String>) : CoreProjectsResult
    data class Unavailable(val reason: String) : CoreProjectsResult
}