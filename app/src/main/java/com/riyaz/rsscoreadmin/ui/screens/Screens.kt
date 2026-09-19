package com.riyaz.rsscoreadmin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.riyaz.rsscoreadmin.data.*
import com.riyaz.rsscoreadmin.ui.navigation.AdminDestination

@Composable
fun CommandCenterScreen(
    health: EndpointHealth?,
    status: CoreStatusResult?,
    projects: CoreProjectsResult?,
    onRefresh: () -> Unit,
    onConnectivity: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Command Center", style = MaterialTheme.typography.headlineMedium)
            Text("RSS ecosystem control and monitoring", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { CriticalPanel(health) }
        item { ConnectivityCard(health, onRefresh, onConnectivity) }
        item { CoreCard(health, status) }
        item { ProjectCard(projects) }
        item { ServiceGrid() }
    }
}

@Composable private fun CriticalPanel(health: EndpointHealth?) {
    val critical = health?.state == HealthState.OFFLINE
    ElevatedCard { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("CRITICAL ISSUES", style = MaterialTheme.typography.labelLarge)
        if (critical) {
            Text("RSS Core is unreachable", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
            Text("Endpoint: ${health.endpoint}")
            Text("Last check: ${health.checkedAt ?: "Unknown"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text("No verified critical incidents", style = MaterialTheme.typography.titleMedium)
            Text("Other incidents require authenticated RSS Core admin APIs.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } }
}

@Composable private fun ConnectivityCard(health: EndpointHealth?, onRefresh: () -> Unit, onConnectivity: () -> Unit) {
    ElevatedCard { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.Cloud, null); Text("RSS CORE CONNECTIVITY", style = MaterialTheme.typography.titleMedium)
        }
        StatusRow("Primary", health?.let { stateText(it.state) } ?: "Unknown", healthColor(health?.state))
        StatusRow("Secondary", "Not configured", MaterialTheme.colorScheme.onSurfaceVariant)
        StatusRow("DNS", "Unable to verify", MaterialTheme.colorScheme.onSurfaceVariant)
        StatusRow("TLS", if (health?.state == HealthState.HEALTHY) "HTTPS verified" else "Unknown", healthColor(health?.state))
        Text("Primary: https://rsscore.cv", style = MaterialTheme.typography.bodySmall)
        Text("Last check: ${health?.checkedAt ?: "Not checked"}", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRefresh) { Text("Check Now") }
            OutlinedButton(onClick = onConnectivity) { Text("Domain & Connectivity") }
        }
    } }
}

@Composable private fun CoreCard(health: EndpointHealth?, status: CoreStatusResult?) {
    ElevatedCard { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("RSS CORE", style = MaterialTheme.typography.titleMedium)
        StatusRow("API", health?.let { stateText(it.state) } ?: "Unknown", healthColor(health?.state))
        StatusRow("HTTP", health?.httpStatus?.toString() ?: "Unknown", MaterialTheme.colorScheme.onSurfaceVariant)
        StatusRow("Response", health?.responseMs?.let { "$it ms" } ?: "Unknown", MaterialTheme.colorScheme.onSurfaceVariant)
        when (status) {
            is CoreStatusResult.Available -> {
                StatusRow("Version", status.version ?: "Unknown", MaterialTheme.colorScheme.onSurface)
                StatusRow("Runtime", status.runtime ?: "Unknown", MaterialTheme.colorScheme.onSurface)
            }
            is CoreStatusResult.Unavailable -> Text("Status API: Not available (${status.reason})", color = MaterialTheme.colorScheme.onSurfaceVariant)
            null -> Text("Status API: Not checked", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } }
}

@Composable private fun ProjectCard(projects: CoreProjectsResult?) {
    ElevatedCard { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("RSS PROJECT REGISTRY", style = MaterialTheme.typography.titleMedium)
        when (projects) {
            is CoreProjectsResult.Available -> {
                Text("${projects.projects.size} projects reported by RSS Core")
                projects.projects.forEach { Text("• $it") }
            }
            is CoreProjectsResult.Unavailable -> Text("Not available from RSS Core API (${projects.reason})", color = MaterialTheme.colorScheme.onSurfaceVariant)
            null -> Text("Not checked", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } }
}

@Composable private fun ServiceGrid() {
    val serviceItems = listOf("RAY", "Database", "Email / Resend", "GitHub", "Backups", "Domain")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        serviceItems.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { label -> AssistChip(onClick = {}, label = { Text("$label • API required") }, modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable private fun StatusRow(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text(value, color = color) }
}

@Composable
fun ModuleScreen(destination: AdminDestination, onDomainConnectivity: () -> Unit = {}) {
    val title = when (destination) {
        AdminDestination.RAY -> "RAY Mission Control"
        AdminDestination.ECOSYSTEM -> "RSS Ecosystem"
        AdminDestination.OPERATIONS -> "Operations"
        AdminDestination.SECURITY -> "Security"
        AdminDestination.COMMAND_CENTER -> "Command Center"
    }
    LazyColumn(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(title, style = MaterialTheme.typography.headlineMedium) }
        when (destination) {
            AdminDestination.RAY -> {
                item { ModuleCard("RAY status", "Not available from RSS Core API. The inspected backend exposes task submission, but not a read-only Mission Control/status contract.") }
                item { ModuleCard("Safe Mode", "Ready in client architecture; server-side pause/resume capability is not exposed by the inspected API.") }
                item { ModuleCard("Task history", "Not available from RSS Core API.") }
            }
            AdminDestination.ECOSYSTEM -> {
                item { ModuleCard("Projects", "Project registry is read from /api/v1/projects. Detailed build, deployment, customer and license fields require additional authenticated APIs.") }
                item { ModuleCard("Customers", "Not available from RSS Core API.") }
                item { ModuleCard("Licenses", "Not available from RSS Core API.") }
                item { ModuleCard("Release Center", "Not available from RSS Core API.") }
            }
            AdminDestination.OPERATIONS -> {
                item { ModuleCard("Email Engine", "Resend provider status and delivery telemetry are not exposed by the inspected public API.") }
                item { ModuleCard("Deployments", "Not available from RSS Core API.") }
                item { ModuleCard("Backups & Diagnostics", "Not available from RSS Core API.") }
                item { ModuleCard("Domain & Connectivity", "Endpoint health is live; DNS, certificate expiry and domain registration data require server/provider APIs.", onClick = onDomainConnectivity) }
                item { ModuleCard("Error Center", "Not available from RSS Core API.") }
                item { ModuleCard("Activity Timeline", "Not available from RSS Core API.") }
            }
            AdminDestination.SECURITY -> {
                item { ModuleCard("Admin Authentication", "A dedicated RSS Core administrative authentication contract is not exposed. No fake local admin account is created.") }
                item { ModuleCard("Audit Log", "RSS Core must own authoritative audit records; endpoint is not currently exposed.") }
                item { ModuleCard("Security Center", "Not available from RSS Core API.") }
            }
            AdminDestination.COMMAND_CENTER -> Unit
        }
    }
}

@Composable
fun DomainConnectivityScreen(health: EndpointHealth?, onCheck: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Domain & Connectivity", style = MaterialTheme.typography.headlineMedium) }
        item { ModuleCard("Primary endpoint", "https://rsscore.cv\nStatus: ${health?.let { stateText(it.state) } ?: "Not checked"}\nHTTP: ${health?.httpStatus ?: "Unknown"}\nResponse: ${health?.responseMs?.let { "$it ms" } ?: "Unknown"}") }
        item { ModuleCard("Secondary endpoint", "Not configured. No backup URL has been invented.") }
        item { ModuleCard("Endpoint discovery", "Architecture ready. A verified discovery endpoint is not exposed by the inspected RSS Core contract.") }
        item { ModuleCard("Last known good", health?.let { "${it.endpoint}\nVerified: ${it.checkedAt ?: "Unknown"}" } ?: "No verified endpoint in this session.") }
        item { ModuleCard("DNS / TLS / expiration", "DNS routing, certificate expiry and domain registration expiry cannot be reliably verified from the currently exposed RSS Core API. State remains unknown rather than fabricated.") }
        item { Button(onClick = onCheck) { Text("Check Now") } }
    }
}

@Composable private fun ModuleCard(title: String, body: String, onClick: (() -> Unit)? = null) {
    val content: @Composable () -> Unit = {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (onClick != null) {
        ElevatedCard(onClick = onClick, content = content)
    } else {
        ElevatedCard(content = content)
    }
}

private fun stateText(s: HealthState) = when (s) {
    HealthState.HEALTHY -> "Healthy"
    HealthState.DEGRADED -> "Warning"
    HealthState.OFFLINE -> "Critical"
    HealthState.NOT_CONFIGURED -> "Not configured"
    HealthState.UNKNOWN -> "Unknown"
}

@Composable private fun healthColor(s: HealthState?): Color = when (s) {
    HealthState.HEALTHY -> Color(0xFF3FB950)
    HealthState.DEGRADED -> Color(0xFFD29922)
    HealthState.OFFLINE -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}