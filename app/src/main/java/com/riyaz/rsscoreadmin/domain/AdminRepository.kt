package com.riyaz.rsscoreadmin.domain

import com.riyaz.rsscoreadmin.data.*

class AdminRepository(private val api: RssCoreApi) {
    suspend fun checkPrimary(config: EndpointConfiguration) = api.checkEndpoint(config.primary)
    suspend fun checkSecondary(config: EndpointConfiguration): EndpointHealth? = config.secondary?.let(api::checkEndpoint)
    suspend fun status(endpoint: String) = api.fetchStatus(endpoint)
    suspend fun projects(endpoint: String) = api.fetchProjects(endpoint)
}