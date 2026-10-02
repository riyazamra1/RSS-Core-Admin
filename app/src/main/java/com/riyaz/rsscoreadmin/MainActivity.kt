package com.riyaz.rsscoreadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.riyaz.rsscoreadmin.data.*
import com.riyaz.rsscoreadmin.ui.navigation.AdminDestination
import com.riyaz.rsscoreadmin.ui.screens.*
import com.riyaz.rsscoreadmin.ui.theme.RssCoreAdminTheme
import kotlinx.coroutines.launch
import org.json.JSONObject

private class AdminViewModel(private val application: android.app.Application) : ViewModel() {
    private val api = RssCoreApi(application)
    private val session = SecureSessionStore(application)
    private val endpoint = EndpointConfiguration()

    var authenticated by mutableStateOf(session.getAccessToken() != null); private set
    var challengeId by mutableStateOf<String?>(null); private set
    var busy by mutableStateOf(false); private set
    var authError by mutableStateOf<String?>(null); private set
    var health by mutableStateOf<EndpointHealth?>(null); private set
    var status by mutableStateOf<CoreStatusResult?>(null); private set
    var projects by mutableStateOf<CoreProjectsResult?>(null); private set
    var moduleData by mutableStateOf<JSONObject?>(null); private set
    var moduleError by mutableStateOf<String?>(null); private set

    fun login(email: String, password: String) {
        if (busy) return
        viewModelScope.launch {
            busy = true; authError = null
            try {
                challengeId = api.login(email.trim(), password).optString("challenge_id").ifBlank { null }
                if (challengeId == null) authError = "Administrator verification challenge was not returned."
            } catch (t: Throwable) { authError = t.message ?: "Sign in failed" }
            finally { busy = false }
        }
    }

    fun verifyOtp(otp: String) {
        val challenge = challengeId ?: return
        if (busy) return
        viewModelScope.launch {
            busy = true; authError = null
            try {
                val token = api.verifyOtp(challenge, otp.filter(Char::isDigit)).optString("token")
                if (token.isBlank()) error("Session token was not returned")
                session.saveAccessToken(token)
                challengeId = null
                authenticated = true
                refresh()
            } catch (t: Throwable) { authError = t.message ?: "Verification failed" }
            finally { busy = false }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { api.logout() }
            session.clear()
            authenticated = false
            challengeId = null
            moduleData = null
        }
    }

    fun refresh() {
        if (busy && !authenticated) return
        viewModelScope.launch {
            health = api.checkEndpoint(endpoint.primary)
            if (health?.state == HealthState.HEALTHY) {
                status = api.fetchStatus(endpoint.active)
                projects = api.fetchProjects(endpoint.active)
            } else { status = null; projects = null }
        }
    }

    fun loadModule(destination: AdminDestination) {
        if (!authenticated || destination == AdminDestination.COMMAND_CENTER || destination == AdminDestination.SECURITY || destination == AdminDestination.SETTINGS) return
        viewModelScope.launch {
            moduleError = null
            try {
                val path = when (destination) {
                    AdminDestination.ACCOUNTS -> "/api/v1/admin/registration-requests?status=pending"
                    AdminDestination.PROJECTS -> "/api/v1/admin/projects"
                    AdminDestination.PRICING -> "/api/v1/admin/pricing"
                    AdminDestination.ENTITLEMENTS -> "/api/v1/admin/entitlements"
                    AdminDestination.FEATURES -> "/api/v1/admin/features"
                    AdminDestination.CONFIG -> "/api/v1/admin/config"
                    AdminDestination.LIMITS -> "/api/v1/admin/limits"
                    AdminDestination.MAINTENANCE -> "/api/v1/admin/maintenance"
                    AdminDestination.ADS -> "/api/v1/admin/ads"
                    AdminDestination.AUDIT -> "/api/v1/admin/audit?limit=200"
                    AdminDestination.SYSTEM -> "/api/v1/status"
                    else -> return@launch
                }
                moduleData = api.adminGet(path)
            } catch (t: Throwable) {
                if (t is AdminApiException && t.code == 401) { logout(); return@launch }
                moduleError = t.message ?: "Request failed"
            }
        }
    }

    fun mutate(destination: AdminDestination, method: String, path: String, body: JSONObject, onDone: (() -> Unit)? = null) {
        if (!authenticated || busy) return
        viewModelScope.launch {
            busy = true; moduleError = null
            try {
                when (method) {
                    "POST" -> api.adminPost(path, body)
                    "PUT" -> api.adminPut(path, body)
                    "DELETE" -> api.adminDelete(path, body)
                }
                loadModule(destination)
                onDone?.invoke()
            } catch (t: Throwable) { moduleError = t.message ?: "Update failed" }
            finally { busy = false }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RssCoreAdminTheme { AdminApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminApp(vm: AdminViewModel = viewModel()) {
    if (!vm.authenticated) {
        LoginScreen(vm.challengeId != null, vm.busy, vm.authError, vm::login, vm::verifyOtp)
        return
    }

    var selected by remember { mutableStateOf(AdminDestination.COMMAND_CENTER) }
    LaunchedEffect(Unit) { vm.refresh() }
    LaunchedEffect(selected) { vm.loadModule(selected) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selected.title) },
                actions = {
                    IconButton(onClick = { if (selected == AdminDestination.COMMAND_CENTER) vm.refresh() else vm.loadModule(selected) }) { Icon(Icons.Default.Refresh, "Refresh") }
                    IconButton(onClick = vm::logout) { Icon(Icons.Default.ExitToApp, "Sign out") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val destinations = listOf(
                    AdminDestination.COMMAND_CENTER to Icons.Default.Dashboard,
                    AdminDestination.ACCOUNTS to Icons.Default.People,
                    AdminDestination.PRICING to Icons.Default.Payments,
                    AdminDestination.ENTITLEMENTS to Icons.Default.CardMembership,
                    AdminDestination.SECURITY to Icons.Default.Security
                )
                destinations.forEach { (d, icon) ->
                    NavigationBarItem(selected = selected == d, onClick = { selected = d }, icon = { Icon(icon, d.title) }, label = { Text(d.shortTitle) })
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (selected) {
                AdminDestination.COMMAND_CENTER -> CommandCenterScreen(vm.health, vm.status, vm.projects, vm::refresh)
                AdminDestination.SECURITY -> SecurityScreen(vm::logout)
                AdminDestination.SETTINGS -> SettingsScreen()
                else -> AdminModuleScreen(selected, vm.moduleData, vm.moduleError, vm.busy, vm::mutate, vm::loadModule)
            }
        }
    }
}