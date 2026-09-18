package com.riyaz.rsscoreadmin.data

data class EndpointConfiguration(
    val service: String = "rss-core",
    val version: Int = 1,
    val primary: String = "https://rsscore.cv",
    val secondary: String? = null,
    val discovery: String? = null,
    val active: String = "https://rsscore.cv",
    val lastVerified: String? = null,
    val configurationVersion: String? = null,
    val lastUpdated: String? = null
)

enum class HealthState { HEALTHY, DEGRADED, OFFLINE, UNKNOWN, NOT_CONFIGURED }

data class EndpointHealth(
    val endpoint: String,
    val state: HealthState = HealthState.UNKNOWN,
    val httpStatus: Int? = null,
    val responseMs: Long? = null,
    val checkedAt: String? = null,
    val errorCategory: String? = null,
    val serviceIdentity: String? = null
)